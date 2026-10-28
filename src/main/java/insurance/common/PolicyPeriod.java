package insurance.common;

import java.time.LocalDate;
import java.util.Objects;

public record PolicyPeriod(LocalDate start, LocalDate end) {
    public PolicyPeriod {
        Objects.requireNonNull(start, "Start date cannot be null");
        Objects.requireNonNull(end, "End date cannot be null");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start date (" + start + ") must be before end date (" + end + ")");
        }
    }

    public boolean contains(LocalDate d) {
        if (d == null) {
            return false;
        }
        return !d.isBefore(start) && d.isBefore(end);
    }
}
