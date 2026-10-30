package Model;

import java.math.BigDecimal;

public class SuiteRoom extends Room {
    public SuiteRoom(String id, String number, int capacity, BigDecimal price) {
        super(id, number, capacity, price);
    }
    @Override public String getRoomType() { return "Suite"; }
}
