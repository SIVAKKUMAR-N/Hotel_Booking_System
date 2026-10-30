package CLI;

import Model.Admin;
import Model.Booking;
import Model.DeluxeRoom;
import Model.DoubleRoom;
import Model.Guest;
import Model.Hotel;
import Model.HotelManager;
import Model.Payment;
import Model.Room;
import Model.SingleRoom;
import Model.SuiteRoom;
import Model.User;
import Repository.BookingRepository;
import Repository.GuestRepository;
import Repository.HotelRepository;
import Repository.ReviewRepository;
import Service.BookingService;
import Service.HotelService;
import Service.IdGenerator;
import enums.BookingStatus;
import payment.CardPayment;
import payment.PayAtHotelPayment;
import payment.PaymentMethod;
import payment.UpiPayment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleApp {
    private final Scanner scanner = new Scanner(System.in);
    // The same repositories are shared by every menu operation.
    private final HotelRepository hotels = new HotelRepository();
    private final GuestRepository guests = new GuestRepository();
    private final BookingRepository bookings = new BookingRepository();
    private final ReviewRepository reviews = new ReviewRepository();
    private final IdGenerator ids = new IdGenerator();
    private final HotelService hotelService = new HotelService(hotels, bookings, ids);
    private final BookingService bookingService =
            new BookingService(hotels, guests, bookings, reviews, ids);

    public ConsoleApp() {
        addSampleData();
    }

    public void run() {
        boolean running = true;
        while (running) {
            bookingService.updateExpiredHoldsAndNoShows();
            printMenu();
            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1: registerGuest(); break;
                    case 2: addHotelAndRooms(); break;
                    case 3: searchRooms(); break;
                    case 4: createBooking(); break;
                    case 5: makePayment(); break;
                    case 6: viewBookings(); break;
                    case 7: cancelBooking(false); break;
                    case 8: checkInOrOut(); break;
                    case 9: addReview(); break;
                    case 10: viewHotelsAndRooms(); break;
                    case 11:
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    case 12: addMaintenancePeriod(); break;
                    case 13: cancelBooking(true); break;
                    case 14: changeBookingDates(); break;
                    case 15: deleteHotel(); break;
                    default: System.out.println("Choose a menu number from 1 to 15.");
                }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                System.out.println("Unable to complete that action: " + exception.getMessage());
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("=== Hotel Booking System ===");
        System.out.println("1. Register guest");
        System.out.println("2. Add hotel and rooms (Admin / Hotel Manager)");
        System.out.println("3. Search available rooms");
        System.out.println("4. Create booking");
        System.out.println("5. Make payment");
        System.out.println("6. View bookings");
        System.out.println("7. Cancel booking");
        System.out.println("8. Check in / Check out");
        System.out.println("9. Add review");
        System.out.println("10. View hotels and rooms");
        System.out.println("11. Exit");
        System.out.println("12. Add room maintenance dates");
        System.out.println("13. Cancel booking for hotel (full refund)");
        System.out.println("14. Change dates of confirmed booking");
        System.out.println("15. Delete hotel");
    }

    private void registerGuest() {
        String name = readRequired("Name: ");
        String phone = readRequired("Phone: ");
        String email = readRequired("Email: ");
        if (guests.findByEmail(email) != null) {
            throw new IllegalArgumentException("A guest with that email is already registered.");
        }
        String proofType = readRequired("ID proof type: ");
        String proofNumber = readRequired("ID proof number: ");
        Guest guest = new Guest(ids.nextGuestId(), name, email, phone, proofType, proofNumber);
        guests.add(guest);
        System.out.println("Guest registered. Guest ID: " + guest.getId());
    }

    private void addHotelAndRooms() {
        User staff = readStaff();
        Hotel hotel = hotelService.addHotel(staff, readRequired("Hotel name: "),
                readRequired("City: "), readRequired("Address: "), readInt("Star rating (1-5): "));
        System.out.println("Hotel added with ID " + hotel.getId() + ".");
        while (readYesNo("Add a room? (y/n): ")) {
            Room room = readRoom();
            hotelService.addRoom(staff, hotel.getId(), room);
            System.out.println("Room " + room.getRoomNumber() + " added with ID " + room.getId());
        }
    }

    private User readStaff() {
        System.out.println("1. Admin  2. Hotel Manager");
        int role = readInt("Staff role: ");
        String name = readRequired("Staff name: ");
        String email = readRequired("Staff email: ");
        String phone = readRequired("Staff phone: ");
        if (role == 1) return new Admin(ids.nextUserId(), name, email, phone);
        if (role == 2) return new HotelManager(ids.nextUserId(), name, email, phone);
        throw new IllegalArgumentException("Choose 1 for Admin or 2 for Hotel Manager.");
    }

    private Room readRoom() {
        String roomNumber = readRequired("Room number: ");
        System.out.println("Room type: 1. Single  2. Double  3. Deluxe  4. Suite");
        int type = readInt("Room type: ");
        int capacity = readInt("Guest capacity: ");
        BigDecimal price = readMoney("Price per night: ");
        String id = ids.nextRoomId();
        switch (type) {
            case 1: return new SingleRoom(id, roomNumber, capacity, price);
            case 2: return new DoubleRoom(id, roomNumber, capacity, price);
            case 3: return new DeluxeRoom(id, roomNumber, capacity, price);
            case 4: return new SuiteRoom(id, roomNumber, capacity, price);
            default: throw new IllegalArgumentException("Choose a room type from 1 to 4.");
        }
    }

    private void searchRooms() {
        String city = readRequired("City: ");
        LocalDate checkIn = readDate("Check-in date (YYYY-MM-DD): ");
        LocalDate checkOut = readDate("Check-out date (YYYY-MM-DD): ");
        int guestCount = readInt("Number of guests: ");
        List<Room> available = bookingService.searchAvailableRooms(city, checkIn, checkOut, guestCount);
        if (available.isEmpty()) {
            System.out.println("No available rooms match your search.");
        } else {
            printRooms(available);
        }
    }

    private void createBooking() {
        Guest guest = guests.findByEmail(readRequired("Registered guest email: "));
        if (guest == null) throw new IllegalArgumentException("Register that guest before booking.");
        String city = readRequired("City: ");
        LocalDate checkIn = readDate("Check-in date (YYYY-MM-DD): ");
        LocalDate checkOut = readDate("Check-out date (YYYY-MM-DD): ");
        int guestCount = readInt("Number of guests: ");
        List<Room> available = bookingService.searchAvailableRooms(city, checkIn, checkOut, guestCount);
        if (available.isEmpty()) {
            System.out.println("No available rooms match your search.");
            return;
        }
        printRooms(available);
        String[] roomIds = readRequired("Enter 1 to 3 room IDs separated by commas: ").split(",");
        if (roomIds.length > 3) throw new IllegalArgumentException("A booking can include at most 3 rooms.");
        List<Room> selected = new ArrayList<Room>();
        for (String value : roomIds) {
            String roomId = value.trim();
            Room room = hotels.findRoom(roomId);
            if (room == null || !containsRoom(available, roomId)) {
                throw new IllegalArgumentException("Room " + roomId + " is not available.");
            }
            selected.add(room);
        }
        Booking booking = bookingService.createBooking(guest, selected, checkIn, checkOut, guestCount);
        System.out.println("Booking ID: " + booking.getId());
        System.out.println("Total including GST and weekend surcharge: ₹" + booking.getTotalPrice());
        System.out.println("Pay within 15 minutes to keep the room hold.");
    }

    private void makePayment() {
        String bookingId = readRequired("Booking ID: ");
        Booking booking = requireBooking(bookingId);
        System.out.println("1. UPI  2. Card  3. Pay at Hotel");
        int option = readInt("Payment method: ");
        PaymentMethod method;
        switch (option) {
            case 1: method = new UpiPayment(); break;
            case 2: method = new CardPayment(); break;
            case 3: method = new PayAtHotelPayment(); break;
            default: throw new IllegalArgumentException("Choose a payment option from 1 to 3.");
        }
        boolean successful = method instanceof PayAtHotelPayment
                || readYesNo("Simulate successful payment of ₹" + booking.getTotalPrice() + "? (y/n): ");
        bookingService.makePayment(bookingId, method, successful);
        System.out.println("Payment recorded. Booking status: " + booking.getStatus());
    }

    private void viewBookings() {
        if (bookings.all().isEmpty()) {
            System.out.println("There are no bookings yet.");
            return;
        }
        for (Booking booking : bookings.all()) {
            System.out.println(booking.getId() + " | " + booking.getGuest().getName()
                    + " | " + booking.getHotel().getName() + " | " + roomNumbers(booking.getRooms())
                    + " | " + booking.getCheckIn() + " to " + booking.getCheckOut()
                    + " | ₹" + booking.getTotalPrice() + " | " + booking.getStatus());
            for (Payment payment : booking.getPayments()) {
                System.out.println("  Payment: " + payment.getMethod() + " | ₹" + payment.getAmount()
                        + " | " + payment.getDate() + " | " + payment.getStatus()
                        + " | Refund ₹" + payment.getRefundAmount());
            }
        }
    }

    private void cancelBooking(boolean hotelCancelled) {
        if (hotelCancelled) readStaff();
        String bookingId = readRequired("Booking ID: ");
        BigDecimal refund = bookingService.cancelBooking(bookingId, hotelCancelled);
        System.out.println("Booking cancelled. Refund amount: ₹" + refund);
    }

    private void checkInOrOut() {
        String bookingId = readRequired("Booking ID: ");
        Booking booking = requireBooking(bookingId);
        System.out.println("1. Check in  2. Check out");
        int action = readInt("Action: ");
        if (action == 1) {
            bookingService.checkIn(bookingId, readRequired("Registered ID proof number: "));
            System.out.println("Guest checked in.");
        } else if (action == 2) {
            bookingService.checkOut(bookingId);
            System.out.println("Guest checked out.");
        } else {
            throw new IllegalArgumentException("Choose 1 or 2.");
        }
    }

    private void addReview() {
        String bookingId = readRequired("Booking ID: ");
        int rating = readInt("Rating (1-5): ");
        String comment = readLine("Comment (optional): ").trim();
        bookingService.addReview(bookingId, rating, comment);
        Hotel hotel = requireBooking(bookingId).getHotel();
        System.out.printf("Review saved. Hotel average rating: %.2f%n", hotel.getAverageRating());
        if (hotel.isFlaggedForAdminReview()) System.out.println("Hotel flagged for admin review.");
    }

    private void viewHotelsAndRooms() {
        if (hotels.all().isEmpty()) {
            System.out.println("There are no hotels.");
            return;
        }
        for (Hotel hotel : hotels.all()) {
            System.out.printf("%s | %s | %s | %d stars | Average review %.2f%s%n",
                    hotel.getId(), hotel.getName(), hotel.getCity(), hotel.getStarRating(),
                    hotel.getAverageRating(), hotel.isFlaggedForAdminReview()
                            ? " | FLAGGED FOR ADMIN REVIEW" : "");
            for (Room room : hotel.getRooms()) {
                System.out.println("  " + room.getId() + " | Room " + room.getRoomNumber()
                        + " | " + room.getRoomType() + " | Capacity " + room.getCapacity()
                        + " | ₹" + room.getPricePerNight() + " per night");
            }
        }
    }

    private void addMaintenancePeriod() {
        Room room = hotels.findRoom(readRequired("Room ID: "));
        if (room == null) throw new IllegalArgumentException("Room not found.");
        hotelService.addMaintenance(room, readDate("Maintenance start (YYYY-MM-DD): "),
                readDate("Maintenance end, inclusive (YYYY-MM-DD): "));
        System.out.println("Maintenance period saved.");
    }

    private void changeBookingDates() {
        String bookingId = readRequired("Booking ID: ");
        bookingService.changeDates(bookingId, readDate("New check-in (YYYY-MM-DD): "),
                readDate("New check-out (YYYY-MM-DD): "));
        System.out.println("Booking dates changed. Updated total: ₹"
                + requireBooking(bookingId).getTotalPrice());
    }

    private void deleteHotel() {
        readStaff();
        hotelService.deleteHotel(readRequired("Hotel ID: "));
        System.out.println("Hotel deleted.");
    }

    private void printRooms(List<Room> rooms) {
        for (Room room : rooms) {
            Hotel hotel = hotels.findHotelForRoom(room.getId());
            System.out.println(room.getId() + " | " + hotel.getName() + " (" + hotel.getCity()
                    + ") | Room " + room.getRoomNumber() + " | " + room.getRoomType()
                    + " | Capacity " + room.getCapacity() + " | ₹"
                    + room.getPricePerNight() + " per night");
        }
    }

    private boolean containsRoom(List<Room> rooms, String id) {
        for (Room room : rooms) {
            if (room.getId().equalsIgnoreCase(id)) return true;
        }
        return false;
    }

    private String roomNumbers(List<Room> rooms) {
        StringBuilder result = new StringBuilder();
        for (Room room : rooms) {
            if (result.length() > 0) result.append(", ");
            result.append(room.getRoomNumber());
        }
        return result.toString();
    }

    private Booking requireBooking(String id) {
        Booking booking = bookings.find(id);
        if (booking == null) throw new IllegalArgumentException("Booking not found.");
        return booking;
    }

    private void addSampleData() {
        Hotel bengaluru = new Hotel("H1", "Maple City Hotel", "Bengaluru",
                "12 MG Road, Bengaluru", 4);
        bengaluru.addRoom(new SingleRoom("R1", "101", 1, new BigDecimal("3200.00")));
        bengaluru.addRoom(new DoubleRoom("R2", "102", 2, new BigDecimal("5200.00")));
        bengaluru.addRoom(new DeluxeRoom("R3", "201", 3, new BigDecimal("8200.00")));
        Hotel chennai = new Hotel("H2", "Seaside Retreat", "Chennai",
                "8 Marina Road, Chennai", 5);
        chennai.addRoom(new DoubleRoom("R4", "301", 2, new BigDecimal("6800.00")));
        chennai.addRoom(new SuiteRoom("R5", "401", 4, new BigDecimal("12500.00")));
        hotels.add(bengaluru);
        hotels.add(chennai);
    }

    private String readRequired(String prompt) {
        while (true) {
            String value = readLine(prompt).trim();
            if (!value.isEmpty()) return value;
            System.out.println("This value cannot be empty.");
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt).trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a whole number.");
            }
        }
    }

    private BigDecimal readMoney(String prompt) {
        while (true) {
            try {
                BigDecimal value = new BigDecimal(readLine(prompt).trim());
                if (value.signum() > 0) return value.setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException exception) {
                // Show the same friendly prompt for malformed and non-positive amounts.
            }
            System.out.println("Enter a valid amount greater than zero.");
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(readLine(prompt).trim());
            } catch (DateTimeParseException exception) {
                System.out.println("Use YYYY-MM-DD format.");
            }
        }
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            String answer = readLine(prompt).trim();
            if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) return true;
            if (answer.equalsIgnoreCase("n") || answer.equalsIgnoreCase("no")) return false;
            System.out.println("Enter y or n.");
        }
    }
}
