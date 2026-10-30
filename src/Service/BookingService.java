package Service;

import Model.Booking;
import Model.Guest;
import Model.Hotel;
import Model.Payment;
import Model.Review;
import Model.Room;
import Repository.BookingRepository;
import Repository.GuestRepository;
import Repository.HotelRepository;
import Repository.ReviewRepository;
import enums.BookingStatus;
import enums.PaymentStatus;
import payment.PayAtHotelPayment;
import payment.PaymentMethod;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BookingService {
    private static final int HOLD_MINUTES = 15;
    private static final int MAX_STAY_NIGHTS = 15;
    private final HotelRepository hotels;
    private final GuestRepository guests;
    private final BookingRepository bookings;
    private final ReviewRepository reviews;
    private final IdGenerator ids;

    public BookingService(HotelRepository hotels, GuestRepository guests,
                          BookingRepository bookings, ReviewRepository reviews, IdGenerator ids) {
        this.hotels = hotels;
        this.guests = guests;
        this.bookings = bookings;
        this.reviews = reviews;
        this.ids = ids;
    }

    public List<Room> searchAvailableRooms(String city, LocalDate checkIn,
                                           LocalDate checkOut, int guestCount) {
        validateDates(checkIn, checkOut);
        if (guestCount < 1) throw new IllegalArgumentException("Guest count must be at least one.");
        List<Room> result = new ArrayList<Room>();
        for (Hotel hotel : hotels.all()) {
            if (!hotel.getCity().equalsIgnoreCase(city.trim())) continue;
            for (Room room : hotel.getRooms()) {
                if (room.getCapacity() >= guestCount
                        && !room.isUnderMaintenance(checkIn, checkOut)
                        && isAvailable(room, checkIn, checkOut, null)) {
                    result.add(room);
                }
            }
        }
        Collections.sort(result, new Comparator<Room>() {
            @Override
            public int compare(Room first, Room second) {
                return first.getPricePerNight().compareTo(second.getPricePerNight());
            }
        });
        return result;
    }

    public Booking createBooking(Guest guest, List<Room> selectedRooms, LocalDate checkIn,
                                 LocalDate checkOut, int guestCount) {
        if (guest == null || guests.findByEmail(guest.getEmail()) == null) {
            throw new IllegalArgumentException("Register a guest before booking.");
        }
        validateDates(checkIn, checkOut);
        if (guestCount < 1) throw new IllegalArgumentException("Guest count must be at least one.");
        if (selectedRooms == null || selectedRooms.isEmpty() || selectedRooms.size() > 3) {
            throw new IllegalArgumentException("Choose between 1 and 3 rooms.");
        }

        Hotel hotel = hotels.findHotelForRoom(selectedRooms.get(0).getId());
        if (hotel == null) throw new IllegalArgumentException("Selected room was not found.");
        List<String> chosenIds = new ArrayList<String>();
        for (Room room : selectedRooms) {
            Hotel roomHotel = hotels.findHotelForRoom(room.getId());
            if (roomHotel == null || !roomHotel.getId().equals(hotel.getId())) {
                throw new IllegalArgumentException("All rooms must be in the same hotel.");
            }
            if (room.getCapacity() < guestCount) {
                throw new IllegalArgumentException("Guest count exceeds room " + room.getRoomNumber()
                        + "'s capacity.");
            }
            if (chosenIds.contains(room.getId())) {
                throw new IllegalArgumentException("The same room cannot be selected twice.");
            }
            if (room.isUnderMaintenance(checkIn, checkOut)
                    || !isAvailable(room, checkIn, checkOut, null)) {
                throw new IllegalArgumentException("Room " + room.getRoomNumber()
                        + " is not available for those dates.");
            }
            chosenIds.add(room.getId());
        }

        Booking booking = new Booking(ids.nextBookingId(), guest, hotel, selectedRooms,
                checkIn, checkOut, guestCount, calculatePrice(selectedRooms, checkIn, checkOut));
        bookings.add(booking);
        return booking;
    }

    public void makePayment(String bookingId, PaymentMethod method, boolean onlinePaymentSucceeded) {
        Booking booking = requireBooking(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Only a pending booking can be paid.");
        }
        if (isHoldExpired(booking)) {
            booking.setStatus(BookingStatus.CANCELLED);
            throw new IllegalStateException("The 15-minute payment hold has expired.");
        }

        long nights = ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
        if (method instanceof PayAtHotelPayment && nights > 2) {
            throw new IllegalArgumentException("Pay at Hotel is allowed only for stays up to 2 nights.");
        }
        boolean paid = onlinePaymentSucceeded && method.processPayment(booking.getTotalPrice());
        PaymentStatus paymentStatus = method instanceof PayAtHotelPayment
                ? PaymentStatus.PENDING : (paid ? PaymentStatus.PAID : PaymentStatus.FAILED);
        booking.addPayment(new Payment(booking.getTotalPrice(), LocalDateTime.now(),
                method.getName(), paymentStatus));
        if (paid || method instanceof PayAtHotelPayment) {
            booking.setStatus(BookingStatus.CONFIRMED);
        }
    }

    public BigDecimal cancelBooking(String bookingId, boolean hotelCancelled) {
        Booking booking = requireBooking(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT
                && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("This booking cannot be cancelled in its current state.");
        }

        BigDecimal refundRate;
        if (hotelCancelled) {
            refundRate = BigDecimal.ONE;
        } else {
            LocalDateTime checkIn = booking.getCheckIn().atStartOfDay();
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(checkIn.minusHours(48))) {
                refundRate = BigDecimal.ONE;
            } else if (!now.isAfter(checkIn.minusHours(24))) {
                refundRate = new BigDecimal("0.50");
            } else {
                refundRate = BigDecimal.ZERO;
            }
        }

        BigDecimal refund = BigDecimal.ZERO;
        for (Payment payment : booking.getPayments()) {
            if (payment.getStatus() == PaymentStatus.PAID) {
                BigDecimal amount = payment.getAmount().multiply(refundRate)
                        .setScale(2, RoundingMode.HALF_UP);
                payment.setRefundAmount(amount);
                payment.setStatus(amount.signum() > 0
                        ? PaymentStatus.REFUNDED : PaymentStatus.CANCELLED);
                refund = refund.add(amount);
            } else if (payment.getStatus() == PaymentStatus.PENDING
                    || payment.getStatus() == PaymentStatus.FAILED) {
                payment.setStatus(PaymentStatus.CANCELLED);
            }
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return refund.setScale(2, RoundingMode.HALF_UP);
    }

    public void checkIn(String bookingId, String idProofNumber) {
        Booking booking = requireBooking(bookingId);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can check in.");
        }
        if (!LocalDate.now().equals(booking.getCheckIn())) {
            throw new IllegalStateException("Check-in is allowed only on the check-in date.");
        }
        if (!booking.getGuest().getIdProofNumber().equalsIgnoreCase(idProofNumber.trim())) {
            throw new IllegalArgumentException("ID proof could not be verified.");
        }
        booking.setStatus(BookingStatus.CHECKED_IN);
    }

    public void checkOut(String bookingId) {
        Booking booking = requireBooking(bookingId);
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Only checked-in guests can check out.");
        }
        if (LocalDate.now().isBefore(booking.getCheckOut())) {
            throw new IllegalStateException("Check-out is allowed on or after the check-out date.");
        }
        booking.setStatus(BookingStatus.CHECKED_OUT);
    }

    public void changeDates(String bookingId, LocalDate newCheckIn, LocalDate newCheckOut) {
        Booking booking = requireBooking(bookingId);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can change dates.");
        }
        LocalDateTime allowedUntil = booking.getCheckIn().atStartOfDay().minusHours(48);
        if (!LocalDateTime.now().isBefore(allowedUntil)) {
            throw new IllegalStateException("Change dates more than 48 hours before check-in.");
        }
        validateDates(newCheckIn, newCheckOut);
        for (Room room : booking.getRooms()) {
            if (room.isUnderMaintenance(newCheckIn, newCheckOut)
                    || !isAvailable(room, newCheckIn, newCheckOut, booking.getId())) {
                throw new IllegalStateException("Room " + room.getRoomNumber()
                        + " is not available for the new dates.");
            }
        }
        booking.setDates(newCheckIn, newCheckOut);
        booking.setTotalPrice(calculatePrice(booking.getRooms(), newCheckIn, newCheckOut));
    }

    public Review addReview(String bookingId, int rating, String comment) {
        Booking booking = requireBooking(bookingId);
        if (booking.getStatus() != BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException("Only checked-out guests can review.");
        }
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating must be from 1 to 5.");
        if (LocalDate.now().isAfter(booking.getCheckOut().plusDays(30))) {
            throw new IllegalStateException("Reviews must be submitted within 30 days of check-out.");
        }
        if (reviews.hasReviewForBooking(bookingId)) {
            throw new IllegalStateException("A booking can have only one review.");
        }
        Review review = new Review(ids.nextReviewId(), booking, rating, comment, LocalDate.now());
        reviews.add(review);
        List<Review> hotelReviews = reviews.forHotel(booking.getHotel().getId());
        int ratingSum = 0;
        for (Review item : hotelReviews) ratingSum += item.getRating();
        booking.getHotel().updateReviewSummary((double) ratingSum / hotelReviews.size(),
                hotelReviews.size());
        return review;
    }

    public void updateExpiredHoldsAndNoShows() {
        LocalDateTime now = LocalDateTime.now();
        for (Booking booking : bookings.all()) {
            if (booking.getStatus() == BookingStatus.PENDING_PAYMENT && isHoldExpired(booking)) {
                booking.setStatus(BookingStatus.CANCELLED);
                for (Payment payment : booking.getPayments()) {
                    if (payment.getStatus() == PaymentStatus.PENDING
                            || payment.getStatus() == PaymentStatus.FAILED) {
                        payment.setStatus(PaymentStatus.EXPIRED);
                    }
                }
            } else if (booking.getStatus() == BookingStatus.CONFIRMED
                    && !now.isBefore(booking.getCheckIn().plusDays(1).atTime(12, 0))) {
                booking.setStatus(BookingStatus.NO_SHOW);
            }
        }
    }

    private boolean isAvailable(Room room, LocalDate checkIn, LocalDate checkOut,
                                String ignoredBookingId) {
        for (Booking booking : bookings.all()) {
            if (ignoredBookingId != null && booking.getId().equalsIgnoreCase(ignoredBookingId)) continue;
            if (!hasRoom(booking, room.getId())
                    || !checkIn.isBefore(booking.getCheckOut())
                    || !booking.getCheckIn().isBefore(checkOut)) continue;
            if (booking.getStatus() == BookingStatus.CONFIRMED
                    || booking.getStatus() == BookingStatus.CHECKED_IN
                    || (booking.getStatus() == BookingStatus.PENDING_PAYMENT
                    && !isHoldExpired(booking))) return false;
        }
        return true;
    }

    private boolean hasRoom(Booking booking, String roomId) {
        for (Room room : booking.getRooms()) {
            if (room.getId().equalsIgnoreCase(roomId)) return true;
        }
        return false;
    }

    private boolean isHoldExpired(Booking booking) {
        return !booking.getCreatedAt().plusMinutes(HOLD_MINUTES).isAfter(LocalDateTime.now());
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in cannot be in the past.");
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) throw new IllegalArgumentException("Check-out must be after check-in.");
        if (nights > MAX_STAY_NIGHTS) throw new IllegalArgumentException("Maximum stay is 15 nights.");
    }

    private BigDecimal calculatePrice(List<Room> rooms, LocalDate checkIn, LocalDate checkOut) {
        BigDecimal total = BigDecimal.ZERO;
        for (Room room : rooms) {
            BigDecimal tax = room.getPricePerNight().compareTo(new BigDecimal("7500")) < 0
                    ? new BigDecimal("0.12") : new BigDecimal("0.18");
            for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
                BigDecimal nightlyPrice = room.getPricePerNight();
                // Friday and Saturday night rates include a 20% surcharge before GST.
                if (night.getDayOfWeek().getValue() == 5 || night.getDayOfWeek().getValue() == 6) {
                    nightlyPrice = nightlyPrice.multiply(new BigDecimal("1.20"));
                }
                total = total.add(nightlyPrice.multiply(BigDecimal.ONE.add(tax)));
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private Booking requireBooking(String id) {
        Booking booking = bookings.find(id);
        if (booking == null) throw new IllegalArgumentException("Booking not found.");
        return booking;
    }
}
