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

        Policyholder holder = new Policyholder("H-001", "Nguyen Van An");
        holder.addPolicy(activeAuto);
        holder.addPolicy(expiredHome);
        holder.addPolicy(activeHealth);

        StatementGenerator generator = new StatementGenerator();

        // 1. Kiểm tra lọc qua danh sách
        List<Policy> activePolicies = generator.activePolicies(holder, statementDate);
        List<Policy> expiredPolicies = generator.expiredPolicies(holder, statementDate);

        assertEquals(2, activePolicies.size());
        assertTrue(activePolicies.contains(activeAuto));
        assertTrue(activePolicies.contains(activeHealth));
        assertFalse(activePolicies.contains(expiredHome));

        assertEquals(1, expiredPolicies.size());
        assertTrue(expiredPolicies.contains(expiredHome));

        // 2. Kiểm tra văn bản sao kê sinh ra
        String statementText = generator.generate(holder, statementDate);
        System.out.println(statementText);

        assertTrue(statementText.contains("POL-ACTIVE-AUTO"));
        assertTrue(statementText.contains("POL-ACTIVE-HEALTH"));
        assertTrue(statementText.contains("CÁC HỢP ĐỒNG CÒN HIỆU LỰC:"));
        assertTrue(statementText.contains("[POL-EXPIRED-HOME] Home (Hết hạn ngày: 2026-01-01)"));

        System.out.println("========== [HOÀN THÀNH STATEMENT GENERATOR TEST THÀNH CÔNG] ==========\n");
    }

    @Test
    @DisplayName("Statement hiển thị đúng lịch sử bồi thường từ ClaimRegistry")
    void testStatementWithClaims() {
        PolicyPeriod period2026 = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        Policy auto = new AutoPolicy("POL-AUTO", period2026, Money.of("100000000"));

        Policyholder holder = new Policyholder("H-002", "Tran Thi Binh");
        holder.addPolicy(auto);

        Claim claim = new Claim("CLM-99", auto, Money.of("50000000"), LocalDate.of(2026, 5, 20));
        ClaimsProcessor processor = new ClaimsProcessor();
        processor.process(claim);

        ClaimRegistry registry = new ClaimRegistry();
        registry.add(claim);

        StatementGenerator generator = new StatementGenerator();
        String text = generator.generate(holder, LocalDate.of(2026, 6, 1), registry);

        assertTrue(text.contains("CLM-99"));
        assertTrue(text.contains("Paid"));
        assertTrue(text.contains("45.000.000"));
    }
}
