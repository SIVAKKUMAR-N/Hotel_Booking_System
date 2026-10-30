package Model;

import enums.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Booking {
    private final String id;
    private final Guest guest;
    private final Hotel hotel;
    private final List<Room> rooms;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private final int guestCount;
    private BookingStatus status = BookingStatus.PENDING_PAYMENT;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private BigDecimal totalPrice;
    private final List<Payment> payments = new ArrayList<Payment>();

    public Booking(String id, Guest guest, Hotel hotel, List<Room> rooms, LocalDate checkIn,
                   LocalDate checkOut, int guestCount, BigDecimal totalPrice) {
        this.id = id;
        this.guest = guest;
        this.hotel = hotel;
        this.rooms = new ArrayList<Room>(rooms);
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guestCount = guestCount;
        this.totalPrice = totalPrice;
    }

    public String getId() { return id; }
    public Guest getGuest() { return guest; }
    public Hotel getHotel() { return hotel; }
    public List<Room> getRooms() { return Collections.unmodifiableList(rooms); }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public int getGuestCount() { return guestCount; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public List<Payment> getPayments() { return Collections.unmodifiableList(payments); }
    public void setDates(LocalDate checkIn, LocalDate checkOut) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }
    public void setStatus(BookingStatus status) { this.status = status; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public void addPayment(Payment payment) { payments.add(payment); }
}
