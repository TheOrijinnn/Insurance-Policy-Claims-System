package insurance.policy;

import insurance.common.CoverageLimitExceededException;
import insurance.common.Money;
import insurance.common.PolicyNotActiveException;
import insurance.common.PolicyPeriod;

import java.time.LocalDate;
import java.util.Objects;

public abstract class Policy {
    private final String id;
    private final PolicyPeriod period;
    private final Money coverageLimit;
    private Money totalPayout;

    protected Policy(String id, PolicyPeriod period, Money coverageLimit) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Policy id cannot be null or empty");
        }
        if (period == null) {
            throw new IllegalArgumentException("Policy period cannot be null");
        }
        if (coverageLimit == null || !coverageLimit.isPositive()) {
            throw new IllegalArgumentException("Coverage limit must be positive");
        }
        this.id = id;
        this.period = period;
        this.coverageLimit = coverageLimit;
        this.totalPayout = Money.ZERO;
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
        // 1. requireActiveOn: ném PolicyNotActiveException nếu lossDate ngoài period
        requireActiveOn(lossDate);

        // 2. calculatePayout theo luật của từng subtype
        Money calculated = calculatePayout(loss);

        // 3. tính remainingLimit
        Money remaining = remainingLimit();

        // 4. kiểm tra ném exception: payout theo luật > 0 mà remainingLimit = 0
        if (calculated.isPositive() && remaining.isZero()) {
            throw new CoverageLimitExceededException(id, remaining);
        }

        // 5. cap theo remaining limit
        Money actualPayout = calculated.min(remaining);

        // 6. chỉ cập nhật totalPayout khi thành công
        this.totalPayout = this.totalPayout.plus(actualPayout);

        return actualPayout;
    }

    public final void requireActiveOn(LocalDate d) {
        if (!isActiveOn(d)) {
            throw new PolicyNotActiveException(id, d);
        }
    }

    public final boolean isActiveOn(LocalDate d) {
        return period.contains(d);
    }

    public final Money remainingLimit() {
        return coverageLimit.minus(totalPayout);
    }

    public final Money totalPayout() {
        return totalPayout;
    }

    public final Money coverageLimit() {
        return coverageLimit;
    }

    public final PolicyPeriod period() {
        return period;
    }

    public final String id() {
        return id;
    }

    public abstract Money calculatePremium();

    protected abstract Money calculatePayout(Money loss);

    public abstract String typeName();
}
