package insurance.engine;

import insurance.claim.Claim;
import insurance.common.Money;

import java.util.List;
import java.util.Objects;

/**
 * Điều phối và tự động hóa quy trình xử lý yêu cầu bồi thường (E2E Claim Processing).
 */
public class ClaimsProcessor {

    /**
     * Xử lý trọn gói quy trình bồi thường (E2E):
     * Submitted -> UnderReview -> Approved -> Paid.
     *
     * @param claim Yêu cầu bồi thường cần xử lý
     * @return Số tiền bồi thường thực tế đã được phê duyệt và chi trả
     */
    public Money process(Claim claim) {
        Objects.requireNonNull(claim, "Claim cannot be null");

        System.out.println("[ClaimsProcessor] Bắt đầu quy trình xử lý yêu cầu bồi thường " + claim.id()
                + " cho hợp đồng " + claim.policy().id() + " (Tổn thất: " + claim.loss() + " VND)");

        // 1. Chuyển sang UnderReview
        claim.startReview();
        System.out.println("[ClaimsProcessor] -> Chuyển trạng thái: " + claim.stateName());

        // 2. Thẩm định và duyệt chi trả
        claim.approve();
        Money approved = claim.approvedPayout().orElse(Money.ZERO);
        System.out.println("[ClaimsProcessor] -> Đã duyệt chi trả: " + approved
                + " VND | Hạn mức còn lại của hợp đồng: " + claim.policy().remainingLimit() + " VND");

        // 3. Thực hiện chi trả
        claim.pay();
        System.out.println("[ClaimsProcessor] -> Đã hoàn tất chi trả. Trạng thái cuối: " + claim.stateName());

        return approved;
    }

    /**
     * Xử lý danh sách các yêu cầu bồi thường liên tiếp.
     */
    public List<Money> processAll(List<Claim> claims) {
        Objects.requireNonNull(claims, "Claims list cannot be null");
        return claims.stream().map(this::process).toList();
    }
}
