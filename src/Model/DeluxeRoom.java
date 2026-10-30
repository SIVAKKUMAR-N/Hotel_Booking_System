package Model;

import java.math.BigDecimal;

public class DeluxeRoom extends Room {
    public DeluxeRoom(String id, String number, int capacity, BigDecimal price) {
        super(id, number, capacity, price);
    }
    @Override public String getRoomType() { return "Deluxe"; }
}
