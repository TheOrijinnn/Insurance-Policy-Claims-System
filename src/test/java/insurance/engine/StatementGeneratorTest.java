package insurance.engine;

import insurance.claim.Claim;
import insurance.claim.ClaimRegistry;
import insurance.common.Money;
import insurance.common.PolicyPeriod;
import insurance.policy.AutoPolicy;
import insurance.policy.HealthPolicy;
import insurance.policy.HomePolicy;
import insurance.policy.Policy;
import insurance.policy.Policyholder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatementGeneratorTest {

    @Test
    @DisplayName("Statement có policy hiệu lực, policy hết hạn bị lọc")
    void testStatementFiltersExpiredPolicies() {
        System.out.println("\n========== [DEMO - STATEMENT GENERATOR: ACTIVE & EXPIRED FILTERING] ==========");

        // Ngày sao kê: 15/06/2026
        LocalDate statementDate = LocalDate.of(2026, 6, 15);

        // Hợp đồng 1: Hiệu lực trong năm 2026 (2026-01-01 -> 2027-01-01) -> CÒN HẠN
        PolicyPeriod period2026 = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        Policy activeAuto = new AutoPolicy("POL-ACTIVE-AUTO", period2026, Money.of("100000000"));

        // Hợp đồng 2: Hiệu lực trong năm 2025 (2025-01-01 -> 2026-01-01) -> ĐÃ HẾT HẠN
        PolicyPeriod period2025 = new PolicyPeriod(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1));
        Policy expiredHome = new HomePolicy("POL-EXPIRED-HOME", period2025, Money.of("200000000"));

        // Hợp đồng 3: Hiệu lực trong năm 2026 (2026-01-01 -> 2027-01-01) -> CÒN HẠN
        Policy activeHealth = new HealthPolicy("POL-ACTIVE-HEALTH", period2026, Money.of("50000000"));

        // Hợp đồng 4: Hiệu lực từ 2027-01-01 -> CHƯA CÓ HIỆU LỰC
        PolicyPeriod period2027 = new PolicyPeriod(LocalDate.of(2027, 1, 1), LocalDate.of(2028, 1, 1));
        Policy futureAuto = new AutoPolicy("POL-FUTURE-AUTO", period2027, Money.of("100000000"));

        Policyholder holder = new Policyholder("H-001", "Nguyen Van An");
        holder.addPolicy(activeAuto);
        holder.addPolicy(expiredHome);
        holder.addPolicy(activeHealth);
        holder.addPolicy(futureAuto);

        StatementGenerator generator = new StatementGenerator();

        // 1. Kiểm tra lọc qua danh sách
        List<Policy> activePolicies = generator.activePolicies(holder, statementDate);
        List<Policy> inactivePolicies = generator.inactivePolicies(holder, statementDate);

        assertEquals(2, activePolicies.size());
        assertTrue(activePolicies.contains(activeAuto));
        assertTrue(activePolicies.contains(activeHealth));
        assertFalse(activePolicies.contains(expiredHome));

        assertEquals(List.of(expiredHome, futureAuto), inactivePolicies);

        // 2. Kiểm tra văn bản sao kê sinh ra
        String statementText = generator.generate(holder, statementDate, new ClaimRegistry());
        System.out.println(statementText);

        assertTrue(statementText.contains("POL-ACTIVE-AUTO"));
        assertTrue(statementText.contains("POL-ACTIVE-HEALTH"));
        assertTrue(statementText.contains("CÁC HỢP ĐỒNG CÒN HIỆU LỰC:"));
        assertTrue(statementText.contains("[POL-EXPIRED-HOME] Home (Hết hạn ngày: 2026-01-01)"));
        assertTrue(statementText.contains("[POL-FUTURE-AUTO] Auto (Hiệu lực từ ngày: 2027-01-01)"));
        assertTrue(statementText.contains("(Chưa có yêu cầu bồi thường nào)"));

        System.out.println("========== [HOÀN THÀNH STATEMENT GENERATOR TEST THÀNH CÔNG] ==========\n");
    }

    @Test
    @DisplayName("Statement hiển thị lịch sử bồi thường: claim đã chi trả, claim bị từ chối kèm lý do, claim chưa duyệt")
    void testStatementWithClaims() {
        PolicyPeriod period2026 = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        Policy auto = new AutoPolicy("POL-AUTO", period2026, Money.of("100000000"));

        Policyholder holder = new Policyholder("H-002", "Tran Thi Binh");
        holder.addPolicy(auto);

        Claim claim = new Claim("CLM-99", auto, Money.of("50000000"), LocalDate.of(2026, 5, 20));
        ClaimsProcessor processor = new ClaimsProcessor();
        processor.process(claim);

        Claim rejected = new Claim("CLM-100", auto, Money.of("8000000"), LocalDate.of(2026, 5, 21));
        rejected.startReview();
        rejected.reject("Thiếu hồ sơ giám định");

        Claim pending = new Claim("CLM-101", auto, Money.of("6000000"), LocalDate.of(2026, 5, 22));

        ClaimRegistry registry = new ClaimRegistry();
        registry.add(claim);
        registry.add(rejected);
        registry.add(pending);

        StatementGenerator generator = new StatementGenerator();
        String text = generator.generate(holder, LocalDate.of(2026, 6, 1), registry);

        assertTrue(text.contains("[CLM-99] Hợp đồng: POL-AUTO | Tổn thất: 50.000.000 | Trạng thái: Paid | Đã duyệt: 45.000.000"));
        assertTrue(text.contains("[CLM-100] Hợp đồng: POL-AUTO | Tổn thất: 8.000.000 | Trạng thái: Rejected | Từ chối: Thiếu hồ sơ giám định"));
        assertTrue(text.contains("[CLM-101] Hợp đồng: POL-AUTO | Tổn thất: 6.000.000 | Trạng thái: Submitted | Chưa duyệt"));
    }
}
