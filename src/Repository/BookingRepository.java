package Repository;

import Model.Booking;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BookingRepository {
    private final Map<String, Booking> bookings = new LinkedHashMap<String, Booking>();

    public void add(Booking booking) { bookings.put(booking.getId(), booking); }
    public Booking find(String id) { return bookings.get(id.toUpperCase()); }
    public List<Booking> all() { return new ArrayList<Booking>(bookings.values()); }
}
