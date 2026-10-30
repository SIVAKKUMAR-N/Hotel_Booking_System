package Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Room {
    private String id;
    private String roomNumber;
    private int capacity;
    private BigDecimal pricePerNight;
    private final List<MaintenancePeriod> maintenancePeriods =
            new ArrayList<MaintenancePeriod>();

    protected Room(String id, String roomNumber, int capacity, BigDecimal pricePerNight) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
    }

    public abstract String getRoomType();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
    public List<MaintenancePeriod> getMaintenancePeriods() {
        return Collections.unmodifiableList(maintenancePeriods);
    }

    public void addMaintenancePeriod(MaintenancePeriod period) {
        maintenancePeriods.add(period);
    }

    public boolean isUnderMaintenance(LocalDate checkIn, LocalDate checkOut) {
        for (MaintenancePeriod period : maintenancePeriods) {
            if (period.overlaps(checkIn, checkOut)) {
                return true;
            }
        }
        return false;
    }
}
