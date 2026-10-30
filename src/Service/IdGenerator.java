package Service;

public class IdGenerator {
    private int hotelId = 3;
    private int roomId = 6;
    private int guestId = 1;
    private int userId = 1;
    private int bookingId = 1;
    private int reviewId = 1;

    public String nextHotelId() { return "H" + hotelId++; }
    public String nextRoomId() { return "R" + roomId++; }
    public String nextGuestId() { return "G" + guestId++; }
    public String nextUserId() { return "U" + userId++; }
    public String nextBookingId() { return "B" + bookingId++; }
    public String nextReviewId() { return "REV" + reviewId++; }
}
