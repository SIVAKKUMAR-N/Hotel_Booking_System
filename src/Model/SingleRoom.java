package Model;

import java.math.BigDecimal;

public class SingleRoom extends Room {
    public SingleRoom(String id, String number, int capacity, BigDecimal price) {
        super(id, number, capacity, price);
    }
    @Override public String getRoomType() { return "Single"; }
}
