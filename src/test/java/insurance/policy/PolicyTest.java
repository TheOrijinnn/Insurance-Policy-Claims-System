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
    @DisplayName("Chuỗi Health: loss 50tr, 80tr, 10tr -> 38,4tr; 61,6tr (cap từ 62,4tr); lần 3 ném exception")
    void testHealthSequenceCappingAndBlocking() {
        HealthPolicy health = new HealthPolicy("POL-HEALTH", period2026, limit100m);

        assertEquals(Money.of("38400000"), health.approvePayout(Money.of("50000000"), validLossDate));
        assertEquals(Money.of("61600000"), health.approvePayout(Money.of("80000000"), validLossDate));
        assertEquals(Money.of("100000000"), health.totalPayout());
        assertEquals(Money.ZERO, health.remainingLimit());

        CoverageLimitExceededException ex = assertThrows(CoverageLimitExceededException.class,
                () -> health.approvePayout(Money.of("10000000"), validLossDate));
        assertEquals("POL-HEALTH", ex.policyId());
        assertEquals(Money.ZERO, ex.remaining());
        assertEquals(Money.of("100000000"), health.totalPayout());
    }

    @Test
    @DisplayName("Biên: raw = remaining -> Auto loss 60tr rồi 50tr ra 55tr rồi 45tr, remaining = 0")
    void testBoundaryRawEqualsRemaining() {
        AutoPolicy auto = new AutoPolicy("POL-AUTO", period2026, limit100m);

        assertEquals(Money.of("55000000"), auto.approvePayout(Money.of("60000000"), validLossDate));
        assertEquals(Money.of("45000000"), auto.approvePayout(Money.of("50000000"), validLossDate));
        assertEquals(Money.ZERO, auto.remainingLimit());
        assertEquals(Money.of("100000000"), auto.totalPayout());
    }

    @Test
    @DisplayName("Hết limit mà raw = 0: Auto đã đầy, loss 3tr -> payout 0, không ném, totalPayout vẫn 100tr")
    void testFullLimitWithZeroRawPayoutDoesNotThrow() {
        AutoPolicy auto = new AutoPolicy("POL-AUTO", period2026, limit100m);
        auto.approvePayout(Money.of("60000000"), validLossDate);
        auto.approvePayout(Money.of("50000000"), validLossDate);
        assertEquals(Money.ZERO, auto.remainingLimit());

        Money payout = auto.approvePayout(Money.of("3000000"), validLossDate);
        assertEquals(Money.ZERO, payout);
        assertEquals(Money.of("100000000"), auto.totalPayout());
        assertEquals(Money.ZERO, auto.remainingLimit());
    }

    @Test
    @DisplayName("Subtype tính ra payout âm -> Policy chặn về 0, totalPayout và hạn mức không đổi")
    void testNegativeRawPayoutFromSubtypeIsClampedToZero() {
        Policy faulty = new AutoPolicy("POL-FAULTY", period2026, limit100m) {
            @Override
            protected Money calculatePayout(Money loss) {
                return loss.minus(Money.of("10000000"));
            }
        };

        assertEquals(Money.ZERO, faulty.approvePayout(Money.of("5000000"), validLossDate));
        assertEquals(Money.ZERO, faulty.totalPayout());
        assertEquals(limit100m, faulty.remainingLimit());
    }

    @Test
    @DisplayName("Ngoài kỳ: lossDate = 2027-01-01 và 2025-12-31 -> PolicyNotActiveException, totalPayout không đổi")
    void testLossDateOutOfPeriodThrows() {
        AutoPolicy auto = new AutoPolicy("POL-AUTO", period2026, limit100m);
        Money loss50m = Money.of("50000000");
        LocalDate beforeStart = LocalDate.of(2025, 12, 31);

        PolicyNotActiveException exAfter = assertThrows(PolicyNotActiveException.class,
                () -> auto.approvePayout(loss50m, invalidLossDate));
        assertEquals("POL-AUTO", exAfter.policyId());
        assertEquals(invalidLossDate, exAfter.date());
        assertEquals(Money.ZERO, auto.totalPayout());

        PolicyNotActiveException exBefore = assertThrows(PolicyNotActiveException.class,
                () -> auto.approvePayout(loss50m, beforeStart));
        assertEquals("POL-AUTO", exBefore.policyId());
        assertEquals(beforeStart, exBefore.date());
        assertEquals(Money.ZERO, auto.totalPayout());
    }

    @Test
    @DisplayName("Loss không dương: loss 0 và -1 -> IllegalArgumentException, totalPayout không đổi")
    void testNonPositiveLossThrows() {
        HomePolicy home = new HomePolicy("POL-HOME", period2026, limit100m);
        home.approvePayout(Money.of("50000000"), validLossDate);
        Money expectedTotal = Money.of("42500000");

        assertThrows(IllegalArgumentException.class,
                () -> home.approvePayout(Money.ZERO, validLossDate));
        assertEquals(expectedTotal, home.totalPayout());

        assertThrows(IllegalArgumentException.class,
                () -> home.approvePayout(Money.of("-1"), validLossDate));
        assertEquals(expectedTotal, home.totalPayout());
    }
}
