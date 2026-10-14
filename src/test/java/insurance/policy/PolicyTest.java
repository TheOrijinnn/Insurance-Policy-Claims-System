package insurance.policy;

import insurance.common.CoverageLimitExceededException;
import insurance.common.Money;
import insurance.common.PolicyNotActiveException;
import insurance.common.PolicyPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PolicyTest {

    private PolicyPeriod period2026;
    private Money limit100m;
    private LocalDate validLossDate;
    private LocalDate invalidLossDate;

    @BeforeEach
    void setUp() {
        period2026 = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        limit100m = Money.of("100000000");
        validLossDate = LocalDate.of(2026, 6, 15);
        invalidLossDate = LocalDate.of(2027, 1, 1);
    }

    @Test
    @DisplayName("Premium Auto / Home / Health = 2.500.000 / 1.500.000 / 4.000.000")
    void testPremiums() {
        AutoPolicy auto = new AutoPolicy("POL-AUTO", period2026, limit100m);
        HomePolicy home = new HomePolicy("POL-HOME", period2026, limit100m);
        HealthPolicy health = new HealthPolicy("POL-HEALTH", period2026, limit100m);

        assertEquals(Money.of("2500000"), auto.calculatePremium());
        assertEquals(Money.of("1500000"), home.calculatePremium());
        assertEquals(Money.of("4000000"), health.calculatePremium());
    }

    @Test
    @DisplayName("approvePayout(50tr) trên Auto = 45.000.000; totalPayout 45tr, remainingLimit 55tr")
    void testApprovePayoutAuto() {
        AutoPolicy auto = new AutoPolicy("POL-AUTO", period2026, limit100m);
        Money loss50m = Money.of("50000000");

        Money payout = auto.approvePayout(loss50m, validLossDate);

        assertEquals(Money.of("45000000"), payout);
        assertEquals(Money.of("45000000"), auto.totalPayout());
        assertEquals(Money.of("55000000"), auto.remainingLimit());
    }

    @Test
    @DisplayName("approvePayout(50tr) trên Home = 42.500.000")
    void testApprovePayoutHome() {
        HomePolicy home = new HomePolicy("POL-HOME", period2026, limit100m);
        Money loss50m = Money.of("50000000");

        Money payout = home.approvePayout(loss50m, validLossDate);

        assertEquals(Money.of("42500000"), payout);
        assertEquals(Money.of("42500000"), home.totalPayout());
        assertEquals(Money.of("57500000"), home.remainingLimit());
    }

    @Test
    @DisplayName("approvePayout(50tr) trên Health = 38.400.000")
    void testApprovePayoutHealth() {
        HealthPolicy health = new HealthPolicy("POL-HEALTH", period2026, limit100m);
        Money loss50m = Money.of("50000000");

        Money payout = health.approvePayout(loss50m, validLossDate);

        assertEquals(Money.of("38400000"), payout);
        assertEquals(Money.of("38400000"), health.totalPayout());
        assertEquals(Money.of("61600000"), health.remainingLimit());
    }

    @Test
    @DisplayName("Auto, loss 3.000.000 -> payout 0, totalPayout vẫn 0, không ném exception")
    void testAutoLossLessThanDeductible() {
        AutoPolicy auto = new AutoPolicy("POL-AUTO", period2026, limit100m);
        Money loss3m = Money.of("3000000");

        Money payout = auto.approvePayout(loss3m, validLossDate);

        assertEquals(Money.ZERO, payout);
        assertEquals(Money.ZERO, auto.totalPayout());
        assertEquals(limit100m, auto.remainingLimit());
    }

    @Test
    @DisplayName("lossDate = 2027-01-01 ngoài kỳ -> PolicyNotActiveException, totalPayout không đổi")
    void testLossDateOutOfPeriodThrows() {
        AutoPolicy auto = new AutoPolicy("POL-AUTO", period2026, limit100m);
        Money loss50m = Money.of("50000000");

        PolicyNotActiveException ex = assertThrows(PolicyNotActiveException.class,
                () -> auto.approvePayout(loss50m, invalidLossDate));

        assertEquals("POL-AUTO", ex.policyId());
        assertEquals(invalidLossDate, ex.date());
        assertEquals(Money.ZERO, auto.totalPayout());
    }

    @Test
    @DisplayName("Khi remainingLimit = 0 mà payout > 0 -> ném CoverageLimitExceededException, totalPayout không đổi")
    void testCoverageLimitExceeded() {
        AutoPolicy auto = new AutoPolicy("POL-AUTO", period2026, Money.of("45000000"));
        Money loss50m = Money.of("50000000");

        // First payout uses up all 45m limit
        Money firstPayout = auto.approvePayout(loss50m, validLossDate);
        assertEquals(Money.of("45000000"), firstPayout);
        assertEquals(Money.ZERO, auto.remainingLimit());

        // Second payout should throw CoverageLimitExceededException
        CoverageLimitExceededException ex = assertThrows(CoverageLimitExceededException.class,
                () -> auto.approvePayout(loss50m, validLossDate));

        assertEquals("POL-AUTO", ex.policyId());
        assertEquals(Money.ZERO, ex.remaining());
        assertEquals(Money.of("45000000"), auto.totalPayout());
    }
}
