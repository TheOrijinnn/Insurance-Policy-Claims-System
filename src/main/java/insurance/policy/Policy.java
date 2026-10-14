package insurance.policy;

import insurance.common.CoverageLimitExceededException;
import insurance.common.Money;
import insurance.common.PolicyNotActiveException;
import insurance.common.PolicyPeriod;
import java.time.LocalDate;

public abstract class Policy {
    protected final String id;
    protected final PolicyPeriod period;
    protected final Money coverageLimit;

    protected Policy(String id, PolicyPeriod period, Money coverageLimit) {
        this.id = id;
        this.period = period;
        this.coverageLimit = coverageLimit;
    }

    /**
     * Hợp đồng của approvePayout:
     * 
     * Mục                              | Nội dung chốt
     * ---------------------------------|----------------------------------------------------------
     * Đầu vào                          | loss > 0 (Claim kiểm trước khi gọi), lossDate
     * Trả về                           | Số tiền payout thực tế đã được duyệt (đã cap)
     * Ném PolicyNotActiveException     | lossDate ngoài period
     * Ném CoverageLimitExceededException | payout theo luật > 0 mà remainingLimit = 0
     * Tác dụng phụ                     | totalPayout += payout, chỉ khi thành công
     * Tính nguyên tử                   | Nếu ném exception thì totalPayout không đổi
     */
    public final Money approvePayout(Money loss, LocalDate lossDate) {
        throw new UnsupportedOperationException("TODO");
    }

    public final void requireActiveOn(LocalDate d) {
        throw new UnsupportedOperationException("TODO");
    }

    public final boolean isActiveOn(LocalDate d) {
        throw new UnsupportedOperationException("TODO");
    }

    public final Money remainingLimit() {
        throw new UnsupportedOperationException("TODO");
    }

    public final Money totalPayout() {
        throw new UnsupportedOperationException("TODO");
    }

    public final Money coverageLimit() {
        throw new UnsupportedOperationException("TODO");
    }

    public final String id() {
        return id;
    }

    public abstract Money calculatePremium();

    protected abstract Money calculatePayout(Money loss);

    public abstract String typeName();
}
