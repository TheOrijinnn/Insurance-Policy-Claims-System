package insurance.engine;

import insurance.claim.Claim;
import insurance.common.InsuranceException;
import insurance.common.Money;

import java.util.Objects;

/**
 * Điều phối và tự động hóa quy trình xử lý yêu cầu bồi thường (E2E Claim Processing).
 * Không rẽ nhánh theo loại hợp đồng: số tiền bồi thường do chính Policy tính (đa hình).
 */
public class ClaimsProcessor {

    /**
     * Xử lý trọn gói quy trình bồi thường (E2E):
     * Submitted -> UnderReview -> Approved -> Paid.
     * Nếu Policy từ chối chi trả (ngoài kỳ hiệu lực, hết hạn mức), claim chuyển sang Rejected
     * với lý do là thông báo lỗi, rồi ngoại lệ được ném tiếp cho caller.
     *
     * @param claim Yêu cầu bồi thường cần xử lý
     * @return Số tiền bồi thường thực tế đã được phê duyệt và chi trả
     */
    public Money process(Claim claim) {
        Objects.requireNonNull(claim, "Claim cannot be null");
        claim.startReview();
        try {
            claim.approve();
        } catch (InsuranceException e) {
            claim.reject(e.getMessage());
            throw e;
        }
        claim.pay();
        return claim.approvedPayout().orElseThrow();
    }
}
