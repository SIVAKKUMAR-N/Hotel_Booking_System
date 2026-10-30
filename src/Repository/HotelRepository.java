package Repository;

import Model.Hotel;
import Model.Room;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HotelRepository {
    private final Map<String, Hotel> hotels = new LinkedHashMap<String, Hotel>();

    public void add(Hotel hotel) { hotels.put(hotel.getId(), hotel); }
    public Hotel find(String id) { return hotels.get(id.toUpperCase()); }
    public void delete(String id) { hotels.remove(id.toUpperCase()); }
    public List<Hotel> all() { return new ArrayList<Hotel>(hotels.values()); }

    public Room findRoom(String roomId) {
        for (Hotel hotel : hotels.values()) {
            for (Room room : hotel.getRooms()) {
                if (room.getId().equalsIgnoreCase(roomId)) return room;
            }
        }
        return null;
    }

    public Hotel findHotelForRoom(String roomId) {
        for (Hotel hotel : hotels.values()) {
            for (Room room : hotel.getRooms()) {
                if (room.getId().equalsIgnoreCase(roomId)) return hotel;
            }
        }
        return null;
    }
}
