package Model;

import java.math.BigDecimal;

public class DoubleRoom extends Room {
    public DoubleRoom(String id, String number, int capacity, BigDecimal price) {
        super(id, number, capacity, price);
    }
    @Override public String getRoomType() { return "Double"; }
}
