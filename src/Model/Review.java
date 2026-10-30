package Model;

import java.time.LocalDate;

public class Review {
    private final String id;
    private final Booking booking;
    private final int rating;
    private final String comment;
    private final LocalDate submittedOn;

    public Review(String id, Booking booking, int rating, String comment, LocalDate submittedOn) {
        this.id = id;
        this.booking = booking;
        this.rating = rating;
        this.comment = comment;
        this.submittedOn = submittedOn;
    }

    public String getId() { return id; }
    public Booking getBooking() { return booking; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDate getSubmittedOn() { return submittedOn; }
}
