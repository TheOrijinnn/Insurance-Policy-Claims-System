package insurance.policy;

import insurance.claim.Claim;
import insurance.common.Money;
import insurance.common.PolicyPeriod;
import insurance.engine.ClaimsProcessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TravelPolicyOcpTest {

    @Test
    @DisplayName("T6: OCP - TravelPolicy định nghĩa trong src/test, tạo qua PolicyFactory, xử lý qua ClaimsProcessor không sửa đổi -> payout 35.000.000")
    void testT6_OpenClosedPrinciple_TravelPolicyExtension() {
        System.out.println("\n========== [DEMO - T6 OCP: MỞ RỘNG TRAVELPOLICY TRONG SRC/TEST] ==========");
        PolicyPeriod period2026 = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        Money limit100m = Money.of("100000000");

        // 1. Khởi tạo PolicyFactory và đăng ký loại Policy mới 'TRAVEL' hoàn toàn tại runtime/test
        PolicyFactory factory = new PolicyFactory();
        factory.register("TRAVEL", r -> new TravelPolicy(r.id(), r.period(), r.coverageLimit()));
        System.out.println("[T6 OCP] Đã đăng ký thành công creator cho loại hợp đồng 'TRAVEL' vào PolicyFactory");

        // 2. Tạo đối tượng TravelPolicy thông qua Factory (không phụ thuộc vào concrete class trong core)
        Policy travelPolicy = factory.create("TRAVEL", new PolicyRequest("POL-TRAV-001", period2026, limit100m));
        assertNotNull(travelPolicy);
        assertEquals("Travel", travelPolicy.typeName());
        assertEquals(Money.of("1500000"), travelPolicy.calculatePremium());
        System.out.println("[T6 OCP] Khởi tạo thành công hợp đồng: " + travelPolicy.id()
                + " | Loại: " + travelPolicy.typeName() + " | Phí bảo hiểm: " + travelPolicy.calculatePremium());

        // 3. Tạo Claim và duyệt chi trả với loss = 50.000.000 VND
        Claim claim = new Claim("CLM-TRAV-01", travelPolicy, Money.of("50000000"), LocalDate.of(2026, 7, 20));
        Money payout = new ClaimsProcessor().process(claim);
        System.out.println("[T6 OCP] Duyệt chi trả yêu cầu bồi thường tổn thất 50.000.000 VND -> Payout: " + payout + " VND");

        // Tiêu chí Done: Payout 35.000.000 VND, trạng thái Paid (ClaimsProcessor không cần sửa)
        assertEquals(Money.of("35000000"), payout);
        assertEquals(Money.of("35000000"), travelPolicy.totalPayout());
        assertEquals(Money.of("65000000"), travelPolicy.remainingLimit());
        assertEquals("Paid", claim.stateName());

        System.out.println("========== [HOÀN THÀNH T6 OCP: PAYOUT 35.000.000 VND, SRC/MAIN KHÔNG THAY ĐỔI] ==========\n");
    }
}
