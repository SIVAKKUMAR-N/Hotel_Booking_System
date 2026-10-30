package Model;

import java.time.LocalDate;

public class MaintenancePeriod {
    private final LocalDate startDate;
    private final LocalDate endDate;

    public MaintenancePeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }

    // Checkout is exclusive, but a maintenance end date is inclusive.
    public boolean overlaps(LocalDate checkIn, LocalDate checkOut) {
        return startDate.isBefore(checkOut) && !endDate.isBefore(checkIn);
    }
}
