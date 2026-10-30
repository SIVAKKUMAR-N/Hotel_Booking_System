package Service;

import Model.Admin;
import Model.Hotel;
import Model.HotelManager;
import Model.MaintenancePeriod;
import Model.Room;
import Model.User;
import Repository.BookingRepository;
import Repository.HotelRepository;
import enums.BookingStatus;
import java.time.LocalDate;

public class HotelService {
    private final HotelRepository hotels;
    private final BookingRepository bookings;
    private final IdGenerator ids;

    public HotelService(HotelRepository hotels, BookingRepository bookings, IdGenerator ids) {
        this.hotels = hotels;
        this.bookings = bookings;
        this.ids = ids;
    }

    public Hotel addHotel(User actor, String name, String city, String address, int stars) {
        requireStaff(actor);
        if (name.trim().isEmpty() || city.trim().isEmpty() || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Hotel name, city, and address are required.");
        }
        if (stars < 1 || stars > 5) throw new IllegalArgumentException("Stars must be from 1 to 5.");
        Hotel hotel = new Hotel(ids.nextHotelId(), name.trim(), city.trim(), address.trim(), stars);
        hotels.add(hotel);
        return hotel;
    }

    public void addRoom(User actor, String hotelId, Room room) {
        requireStaff(actor);
        Hotel hotel = hotels.find(hotelId);
        if (hotel == null) throw new IllegalArgumentException("Hotel not found.");
        if (room.getCapacity() < 1 || room.getPricePerNight().signum() <= 0) {
            throw new IllegalArgumentException("Room capacity and price must be greater than zero.");
        }
        for (Room existing : hotel.getRooms()) {
            if (existing.getRoomNumber().equalsIgnoreCase(room.getRoomNumber())) {
                throw new IllegalArgumentException("Room number must be unique within a hotel.");
            }
        }
        hotel.addRoom(room);
    }

    public void addMaintenance(Room room, LocalDate start, LocalDate end) {
        if (start.isBefore(LocalDate.now()) || end.isBefore(start)) {
            throw new IllegalArgumentException("Maintenance dates must be today or later and end after start.");
        }
        room.addMaintenancePeriod(new MaintenancePeriod(start, end));
    }

    public void deleteHotel(String hotelId) {
        Hotel hotel = hotels.find(hotelId);
        if (hotel == null) throw new IllegalArgumentException("Hotel not found.");
        for (Model.Booking booking : bookings.all()) {
            if (booking.getHotel().getId().equalsIgnoreCase(hotelId)
                    && booking.getStatus() == BookingStatus.CONFIRMED
                    && !booking.getCheckIn().isBefore(LocalDate.now())) {
                throw new IllegalStateException("Hotel has an upcoming confirmed booking.");
            }
        }
        hotels.delete(hotelId);
    }

    private void requireStaff(User actor) {
        if (!(actor instanceof Admin) && !(actor instanceof HotelManager)) {
            throw new IllegalArgumentException("Only Admin or Hotel Manager can manage hotels.");
        }
    }
}
