package payment;

import java.math.BigDecimal;

public class CardPayment implements PaymentMethod {
    @Override public String getName() { return "Card"; }
    @Override public boolean processPayment(BigDecimal amount) { return amount.signum() > 0; }
}
