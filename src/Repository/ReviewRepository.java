package Repository;

import Model.Review;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReviewRepository {
    private final Map<String, Review> reviews = new LinkedHashMap<String, Review>();

    public void add(Review review) { reviews.put(review.getId(), review); }

    public boolean hasReviewForBooking(String bookingId) {
        for (Review review : reviews.values()) {
            if (review.getBooking().getId().equalsIgnoreCase(bookingId)) return true;
        }
        return false;
    }

    public List<Review> forHotel(String hotelId) {
        List<Review> result = new ArrayList<Review>();
        for (Review review : reviews.values()) {
            if (review.getBooking().getHotel().getId().equalsIgnoreCase(hotelId)) {
                result.add(review);
            }
        }
        return result;
    }
}
