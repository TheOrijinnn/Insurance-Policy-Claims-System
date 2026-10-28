package insurance.engine;

import insurance.claim.Claim;
import insurance.common.InsuranceException;
import insurance.common.Money;
import insurance.common.PolicyNotActiveException;
import insurance.common.PolicyPeriod;
import insurance.policy.AutoPolicy;
import insurance.policy.Policy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ClaimsProcessorTest {

    @Test
    @DisplayName("T1: E2E ClaimsProcessor xử lý bồi thường trọn gói - payout 45.000.000, remaining 55.000.000")
    void testT1_E2E_ClaimProcessing() {
        System.out.println("\n========== [DEMO - T1 E2E CLAIM PROCESSING] ==========");
        PolicyPeriod period2026 = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        Policy autoPolicy = new AutoPolicy("POL-AUTO-T1", period2026, Money.of("100000000"));
        Claim claim = new Claim("CLM-T1", autoPolicy, Money.of("50000000"), LocalDate.of(2026, 6, 15));

        ClaimsProcessor processor = new ClaimsProcessor();
        Money payout = processor.process(claim);

        // Tiêu chí Done: T1 chạy pass, payout 45.000.000, remaining 55.000.000
        assertEquals(Money.of("45000000"), payout);
        assertEquals(Money.of("45000000"), autoPolicy.totalPayout());
        assertEquals(Money.of("55000000"), autoPolicy.remainingLimit());
        assertEquals("Paid", claim.stateName());
        assertEquals(Money.of("45000000"), claim.approvedPayout().orElseThrow());
        System.out.println("========== [HOÀN THÀNH T1 E2E: PAYOUT 45.000.000, REMAINING 55.000.000] ==========\n");
    }

    @Test
    @DisplayName("Policy từ chối chi trả (ngoài kỳ hiệu lực) -> claim chuyển Rejected kèm lý do, ngoại lệ được ném tiếp, không chi tiền")
    void testPolicyRefusalRejectsClaim() {
        PolicyPeriod period2026 = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        Policy autoPolicy = new AutoPolicy("POL-AUTO-REJ", period2026, Money.of("100000000"));
        Claim claim = new Claim("CLM-REJ", autoPolicy, Money.of("50000000"), LocalDate.of(2027, 1, 1));

        PolicyNotActiveException ex = assertThrows(PolicyNotActiveException.class,
                () -> new ClaimsProcessor().process(claim));

        assertEquals("Rejected", claim.stateName());
        assertEquals(ex.getMessage(), claim.rejectionReason().orElseThrow());
        assertTrue(claim.approvedPayout().isEmpty());
        assertEquals(Money.ZERO, autoPolicy.totalPayout());
    }

    @Test
    @DisplayName("Loại hợp đồng mới ném InsuranceException không có thông báo -> claim vẫn Rejected, lý do là tên ngoại lệ")
    void testRefusalWithoutMessageStillRejectsClaim() {
        PolicyPeriod period2026 = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        Policy silent = new AutoPolicy("POL-SILENT", period2026, Money.of("100000000")) {
            @Override
            protected Money calculatePayout(Money loss) {
                throw new InsuranceException(null);
            }
        };
        Claim claim = new Claim("CLM-SILENT", silent, Money.of("50000000"), LocalDate.of(2026, 6, 15));

        assertThrows(InsuranceException.class, () -> new ClaimsProcessor().process(claim));
        assertEquals("Rejected", claim.stateName());
        assertEquals("InsuranceException", claim.rejectionReason().orElseThrow());
    }
}
