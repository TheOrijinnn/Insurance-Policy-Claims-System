package insurance.policy;

import insurance.claim.Claim;
import insurance.common.Money;
import insurance.common.PolicyNotActiveException;
import insurance.common.PolicyPeriod;
import insurance.engine.ClaimsProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PolymorphismAndPeriodTest {

    private PolicyPeriod period2026;
    private Money limit100m;
    private LocalDate validLossDate;
    private LocalDate outOfPeriodLossDate;

    @BeforeEach
    void setUp() {
        period2026 = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        limit100m = Money.of("100000000");
        validLossDate = LocalDate.of(2026, 6, 15);
        outOfPeriodLossDate = LocalDate.of(2027, 1, 1);
    }

    @Test
    @DisplayName("T5: Đa hình qua ClaimsProcessor - Auto / Home / Health với loss 50tr ra 45tr / 42,5tr / 38,4tr")
    void testT5_PolymorphicPayouts() {
        System.out.println("\n========== [DEMO - T5 ĐA HÌNH AUTO / HOME / HEALTH] ==========");
        Money loss50m = Money.of("50000000");
        ClaimsProcessor processor = new ClaimsProcessor(); // cùng một processor, không biết loại hợp đồng

        // 1. AutoPolicy: deductible 5.000.000 -> payout = 50tr - 5tr = 45tr
        Policy auto = new AutoPolicy("POL-AUTO", period2026, limit100m);
        Money autoPayout = processor.process(new Claim("CLM-AUTO", auto, loss50m, validLossDate));

        // 2. HomePolicy: factor 0.85 -> payout = 50tr * 0.85 = 42,5tr
        Policy home = new HomePolicy("POL-HOME", period2026, limit100m);
        Money homePayout = processor.process(new Claim("CLM-HOME", home, loss50m, validLossDate));

        // 3. HealthPolicy: deductible 2.000.000, factor 0.80 -> payout = (50tr - 2tr) * 0.80 = 38,4tr
        Policy health = new HealthPolicy("POL-HEALTH", period2026, limit100m);
        Money healthPayout = processor.process(new Claim("CLM-HEALTH", health, loss50m, validLossDate));

        System.out.println("[T5 Polymorphism] Đánh giá tổn thất 50.000.000 VND trên 3 loại hợp đồng:");
        System.out.println("  + AutoPolicy   -> Payout: " + autoPayout + " VND (Kỳ vọng: 45.000.000 VND)");
        System.out.println("  + HomePolicy   -> Payout: " + homePayout + " VND (Kỳ vọng: 42.500.000 VND)");
        System.out.println("  + HealthPolicy -> Payout: " + healthPayout + " VND (Kỳ vọng: 38.400.000 VND)");

        // Tiêu chí Done: T5 ra 45tr / 42,5tr / 38,4tr
        assertEquals(Money.of("45000000"), autoPayout);
        assertEquals(Money.of("42500000"), homePayout);
        assertEquals(Money.of("38400000"), healthPayout);
        assertEquals(Money.of("55000000"), auto.remainingLimit());
        assertEquals(Money.of("57500000"), home.remainingLimit());
        assertEquals(Money.of("61600000"), health.remainingLimit());

        System.out.println("========== [HOÀN THÀNH T5: KẾT QUẢ CHUẨN XÁC 45TR / 42,5TR / 38,4TR] ==========\n");
    }

    @Test
    @DisplayName("T7: Yêu cầu bồi thường ngoài kỳ hiệu lực ném PolicyNotActiveException và bảo toàn bất biến")
    void testT7_OutOfPeriodClaimThrowsException() {
        System.out.println("\n========== [DEMO - T7 BỒI THƯỜNG NGOÀI KỲ HIỆU LỰC] ==========");
        Policy auto = new AutoPolicy("POL-AUTO-T7", period2026, limit100m);
        Claim outOfPeriodClaim = new Claim("CLM-T7", auto, Money.of("50000000"), outOfPeriodLossDate);

        System.out.println("[T7 Out-Of-Period] Tạo Claim ngày " + outOfPeriodLossDate
                + " ngoài kỳ hạn hợp đồng [" + period2026.start() + " đến " + period2026.end() + ")");

        outOfPeriodClaim.startReview();
        System.out.println("[T7 Out-Of-Period] Chuyển trạng thái sang: " + outOfPeriodClaim.stateName());

        PolicyNotActiveException ex = assertThrows(PolicyNotActiveException.class, outOfPeriodClaim::approve);
        System.out.println("[T7 Out-Of-Period] Đã chặn thành công với ngoại lệ: " + ex.getClass().getSimpleName()
                + " - " + ex.getMessage());

        // Kiểm tra bảo toàn trạng thái và số tiền
        assertEquals("UnderReview", outOfPeriodClaim.stateName());
        assertTrue(outOfPeriodClaim.approvedPayout().isEmpty());
        assertEquals(Money.ZERO, auto.totalPayout());
        assertEquals(limit100m, auto.remainingLimit());
        System.out.println("[T7 Out-Of-Period] Bất biến được bảo toàn: Total payout = "
                + auto.totalPayout() + " VND, Remaining limit = " + auto.remainingLimit() + " VND");

        System.out.println("========== [HOÀN THÀNH T7: CHẶN BỒI THƯỜNG NGOÀI KỲ HIỆU LỰC THÀNH CÔNG] ==========\n");
    }
}
