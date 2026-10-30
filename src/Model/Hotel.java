package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hotel {
    private String id;
    private String name;
    private String city;
    private String address;
    private int starRating;
    private final List<Room> rooms = new ArrayList<Room>();
    private double averageRating;
    private int reviewCount;
    private boolean flaggedForAdminReview;

    public Hotel(String id, String name, String city, String address, int starRating) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.starRating = starRating;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public int getStarRating() { return starRating; }
    public void setStarRating(int starRating) { this.starRating = starRating; }
    public List<Room> getRooms() { return Collections.unmodifiableList(rooms); }
    public double getAverageRating() { return averageRating; }
    public int getReviewCount() { return reviewCount; }
    public boolean isFlaggedForAdminReview() { return flaggedForAdminReview; }

    public void addRoom(Room room) { rooms.add(room); }

    public void updateReviewSummary(double average, int count) {
        averageRating = average;
        reviewCount = count;
        flaggedForAdminReview = count >= 10 && average < 2.5;
    }
}
