package Model;

import enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private final BigDecimal amount;
    private final LocalDateTime date;
    private final String method;
    private PaymentStatus status;
    private BigDecimal refundAmount = BigDecimal.ZERO;

    public Payment(BigDecimal amount, LocalDateTime date, String method, PaymentStatus status) {
        this.amount = amount;
        this.date = date;
        this.method = method;
        this.status = status;
    }

    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getDate() { return date; }
    public String getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
}
