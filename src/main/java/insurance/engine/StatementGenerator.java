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
 * Chỉ các hợp đồng còn hiệu lực tại ngày sao kê được tính vào phần tổng cộng.
 */
public class StatementGenerator {
    private static final String LINE = "------------------------------------------------------------\n";
    private static final String DOUBLE_LINE = "============================================================\n";

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
    public List<Policy> inactivePolicies(Policyholder holder, LocalDate asOfDate) {
        Objects.requireNonNull(holder, "Policyholder cannot be null");
        Objects.requireNonNull(asOfDate, "asOfDate cannot be null");
        return holder.policies().stream()
                .filter(p -> !p.isActiveOn(asOfDate))
                .toList();
    }

    /**
     * Tạo báo cáo sao kê gồm các hợp đồng còn hiệu lực và lịch sử yêu cầu bồi thường.
     */
    public String generate(Policyholder holder, LocalDate asOfDate, ClaimRegistry registry) {
        Objects.requireNonNull(registry, "ClaimRegistry cannot be null");
        List<Policy> activeList = activePolicies(holder, asOfDate);
        List<Policy> inactiveList = inactivePolicies(holder, asOfDate);

        StringBuilder sb = new StringBuilder();
        sb.append(DOUBLE_LINE);
        sb.append("             BÁO CÁO SAO KÊ HỢP ĐỒNG BẢO HIỂM              \n");
        sb.append(DOUBLE_LINE);
        sb.append(String.format("Khách hàng: %s (Mã: %s)\n", holder.name(), holder.id()));
        sb.append(String.format("Thời điểm sao kê: %s\n", asOfDate));
        sb.append(LINE);
        sb.append("I. CÁC HỢP ĐỒNG CÒN HIỆU LỰC:\n");

        Money totalPremium = Money.ZERO;
        Money totalPayout = Money.ZERO;
        Money totalRemaining = Money.ZERO;

        if (activeList.isEmpty()) {
            sb.append("  (Không có hợp đồng nào đang có hiệu lực)\n");
        }
        for (Policy p : activeList) {
            Money premium = p.calculatePremium();
            totalPremium = totalPremium.plus(premium);
            totalPayout = totalPayout.plus(p.totalPayout());
            totalRemaining = totalRemaining.plus(p.remainingLimit());

            sb.append(String.format("  - [%s] %s | Hạn mức: %s | Phí: %s | Đã duyệt chi: %s | Còn lại: %s\n",
                    p.id(), p.typeName(), p.coverageLimit(), premium, p.totalPayout(), p.remainingLimit()));
        }

        sb.append(LINE);
        sb.append(String.format("TỔNG CỘNG (HĐ còn hiệu lực): Phí: %s | Đã duyệt chi: %s | Hạn mức còn lại: %s\n",
                totalPremium, totalPayout, totalRemaining));

        if (!inactiveList.isEmpty()) {
            sb.append(LINE);
            sb.append("II. CÁC HỢP ĐỒNG KHÔNG CÓ HIỆU LỰC TẠI NGÀY SAO KÊ (KHÔNG TÍNH VÀO TỔNG):\n");
            for (Policy p : inactiveList) {
                String when = asOfDate.isBefore(p.period().start())
                        ? "Hiệu lực từ ngày: " + p.period().start()
                        : "Hết hạn ngày: " + p.period().end();
                sb.append(String.format("  - [%s] %s (%s)\n", p.id(), p.typeName(), when));
            }
        }

        sb.append(LINE);
        sb.append("III. LỊCH SỬ YÊU CẦU BỒI THƯỜNG:\n");
        List<Claim> claims = registry.byHolder(holder);
        if (claims.isEmpty()) {
            sb.append("  (Chưa có yêu cầu bồi thường nào)\n");
        }
        for (Claim c : claims) {
            sb.append(String.format("  - [%s] Hợp đồng: %s | Tổn thất: %s | Trạng thái: %s | %s\n",
                    c.id(), c.policy().id(), c.loss(), c.stateName(), outcome(c)));
        }

        sb.append(DOUBLE_LINE);
        return sb.toString();
    }

    private static String outcome(Claim c) {
        return c.approvedPayout().map(p -> "Đã duyệt: " + p)
                .or(() -> c.rejectionReason().map(r -> "Từ chối: " + r))
                .orElse("Chưa duyệt");
    }
}
