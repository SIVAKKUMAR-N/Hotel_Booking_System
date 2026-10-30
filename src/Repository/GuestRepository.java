package Repository;

import Model.Guest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GuestRepository {
    private final Map<String, Guest> guests = new LinkedHashMap<String, Guest>();

    public void add(Guest guest) { guests.put(guest.getId(), guest); }
    public List<Guest> all() { return new ArrayList<Guest>(guests.values()); }

    public Guest findByEmail(String email) {
        for (Guest guest : guests.values()) {
            if (guest.getEmail().equalsIgnoreCase(email)) return guest;
        }
        return null;
    }
}
