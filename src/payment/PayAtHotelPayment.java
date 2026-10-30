package payment;

import java.math.BigDecimal;

public class PayAtHotelPayment implements PaymentMethod {
    @Override public String getName() { return "Pay at Hotel"; }
    @Override public boolean processPayment(BigDecimal amount) { return amount.signum() > 0; }
}
