package payment;

import java.math.BigDecimal;

public class UpiPayment implements PaymentMethod {
    @Override public String getName() { return "UPI"; }
    @Override public boolean processPayment(BigDecimal amount) { return amount.signum() > 0; }
}
