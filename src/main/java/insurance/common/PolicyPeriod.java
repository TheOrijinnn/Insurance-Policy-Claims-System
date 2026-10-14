package insurance.common;

import java.time.LocalDate;

public final class PolicyPeriod {
    private final LocalDate start;
    private final LocalDate end;

    public PolicyPeriod(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }

    public LocalDate start() {
        return start;
    }

    public LocalDate end() {
        return end;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    /**
     * Kiểm tra xem ngày d có nằm trong kỳ bảo hiểm hay không.
     * Quy tắc: start <= d < end
     */
    public boolean contains(LocalDate d) {
        throw new UnsupportedOperationException("TODO");
    }
}
