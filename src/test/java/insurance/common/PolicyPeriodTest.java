package insurance.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PolicyPeriodTest {

    private final PolicyPeriod period2026 = new PolicyPeriod(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2027, 1, 1)
    );

    @Test
    @DisplayName("Test 4 ca biên của contains: 2026-01-01 (T), 2026-12-31 (T), 2027-01-01 (F), 2025-12-31 (F)")
    void testBoundaryDates() {
        assertTrue(period2026.contains(LocalDate.of(2026, 1, 1)), "Start date should be inclusive (start <= d)");
        assertTrue(period2026.contains(LocalDate.of(2026, 12, 31)), "Last day of 2026 should be true");
        assertFalse(period2026.contains(LocalDate.of(2027, 1, 1)), "End date should be exclusive (d < end)");
        assertFalse(period2026.contains(LocalDate.of(2025, 12, 31)), "Day before period should be false");
    }

    @Test
    @DisplayName("Constructor ném IllegalArgumentException nếu start không trước end")
    void testInvalidPeriodThrows() {
        LocalDate d1 = LocalDate.of(2026, 6, 1);
        LocalDate d2 = LocalDate.of(2026, 6, 1);
        LocalDate d3 = LocalDate.of(2026, 5, 1);

        assertThrows(IllegalArgumentException.class, () -> new PolicyPeriod(d1, d2), "start == end must throw");
        assertThrows(IllegalArgumentException.class, () -> new PolicyPeriod(d1, d3), "start > end must throw");
    }
}
