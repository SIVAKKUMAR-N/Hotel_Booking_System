package payment;

import java.math.BigDecimal;

public interface PaymentMethod {
    String getName();
    boolean processPayment(BigDecimal amount);
}
