package insurance.engine;

import insurance.claim.Claim;
import insurance.claim.ClaimRegistry;
import insurance.common.Money;
import insurance.policy.Policy;
import insurance.policy.Policyholder;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Trình tạo báo cáo sao kê hợp đồng và bồi thường cho chủ hợp đồng (Statement Generator).
 * Tự động lọc bỏ các hợp đồng không còn hiệu lực tại thời điểm lập báo cáo.
 */
public class StatementGenerator {

    /**
     * Lọc danh sách các hợp đồng còn hiệu lực tại ngày truy vấn.
     */
    public List<Policy> activePolicies(Policyholder holder, LocalDate asOfDate) {
        Objects.requireNonNull(holder, "Policyholder cannot be null");
        Objects.requireNonNull(asOfDate, "asOfDate cannot be null");
        return holder.policies().stream()
                .filter(p -> p.isActiveOn(asOfDate))
                .toList();
    }

    /**
     * Lọc danh sách các hợp đồng đã hết hạn hoặc chưa có hiệu lực tại ngày truy vấn.
     */
    public List<Policy> expiredPolicies(Policyholder holder, LocalDate asOfDate) {
        Objects.requireNonNull(holder, "Policyholder cannot be null");
        Objects.requireNonNull(asOfDate, "asOfDate cannot be null");
        return holder.policies().stream()
                .filter(p -> !p.isActiveOn(asOfDate))
                .toList();
    }

    /**
     * Tạo báo cáo sao kê dạng văn bản cho chủ hợp đồng tại một thời điểm nhất định.
     */
    public String generate(Policyholder holder, LocalDate asOfDate) {
        return generate(holder, asOfDate, null);
    }

    /**
     * Tạo báo cáo sao kê chi tiết gồm cả thông tin hợp đồng hiệu lực và lịch sử bồi thường.
     */
    public String generate(Policyholder holder, LocalDate asOfDate, ClaimRegistry registry) {
        Objects.requireNonNull(holder, "Policyholder cannot be null");
        Objects.requireNonNull(asOfDate, "asOfDate cannot be null");

        List<Policy> activeList = activePolicies(holder, asOfDate);
        List<Policy> expiredList = expiredPolicies(holder, asOfDate);

        System.out.println("[StatementGenerator] Tạo báo cáo sao kê cho khách hàng: " + holder.name()
                + " (Mã: " + holder.id() + ") tính đến ngày " + asOfDate);
        System.out.println("[StatementGenerator] -> Số hợp đồng hiệu lực: " + activeList.size()
                + " | Số hợp đồng hết hạn bị lọc: " + expiredList.size());

        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append("             BÁO CÁO SAO KÊ HỢP ĐỒNG BẢO HIỂM              \n");
        sb.append("============================================================\n");
        sb.append(String.format("Khách hàng: %s (Mã: %s)\n", holder.name(), holder.id()));
        sb.append(String.format("Thời điểm sao kê: %s\n", asOfDate));
        sb.append("------------------------------------------------------------\n");
        sb.append("I. CÁC HỢP ĐỒNG CÒN HIỆU LỰC:\n");

        Money totalPremium = Money.ZERO;
        Money totalPayout = Money.ZERO;
        Money totalRemaining = Money.ZERO;

        if (activeList.isEmpty()) {
            sb.append("  (Không có hợp đồng nào đang có hiệu lực)\n");
        } else {
            for (Policy p : activeList) {
                Money premium = p.calculatePremium();
                Money payout = p.totalPayout();
                Money remaining = p.remainingLimit();

                totalPremium = totalPremium.plus(premium);
                totalPayout = totalPayout.plus(payout);
                totalRemaining = totalRemaining.plus(remaining);

                sb.append(String.format("  - [%s] %s | Hạn mức: %s | Phí: %s | Đã chi trả: %s | Còn lại: %s\n",
                        p.id(), p.typeName(), p.coverageLimit(), premium, payout, remaining));
            }
        }

        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("TỔNG CỘNG: Phí: %s | Đã bồi thường: %s | Hạn mức còn lại: %s\n",
                totalPremium, totalPayout, totalRemaining));

        if (!expiredList.isEmpty()) {
            sb.append("------------------------------------------------------------\n");
            sb.append("II. CÁC HỢP ĐỒNG ĐÃ HẾT HẠN (ĐÃ LỌC BỎ KHỎI SAO KÊ HIỆU LỰC):\n");
            for (Policy p : expiredList) {
                sb.append(String.format("  - [%s] %s (Hết hạn ngày: %s)\n",
                        p.id(), p.typeName(), p.period().end()));
            }
        }

        if (registry != null) {
            sb.append("------------------------------------------------------------\n");
            sb.append("III. LỊCH SỬ YÊU CẦU BỒI THƯỜNG:\n");
            List<Claim> claims = registry.byHolder(holder);
            if (claims.isEmpty()) {
                sb.append("  (Chưa có yêu cầu bồi thường nào)\n");
            } else {
                for (Claim c : claims) {
                    sb.append(String.format("  - [%s] Hợp đồng: %s | Tổn thất: %s | Trạng thái: %s | Đã duyệt: %s\n",
                            c.id(), c.policy().id(), c.loss(), c.stateName(),
                            c.approvedPayout().map(Money::toString).orElse("Chưa duyệt")));
                }
            }
        }

        sb.append("============================================================\n");
        return sb.toString();
    }
}
