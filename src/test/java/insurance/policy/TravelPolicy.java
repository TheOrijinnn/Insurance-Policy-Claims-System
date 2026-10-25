package insurance.policy;

import insurance.common.Money;
import insurance.common.PolicyPeriod;

/**
 * Lớp mở rộng TravelPolicy được định nghĩa hoàn toàn trong src/test nhằm chứng minh
 * nguyên lý Open-Closed Principle (OCP): Mở rộng loại bảo hiểm mới mà không sửa mã nguồn trong src/main.
 */
public class TravelPolicy extends Policy {
    private static final String PAYOUT_FACTOR = "0.70";
    private static final String PREMIUM_RATE = "0.015";
    private static final String TYPE_NAME = "Travel";

    public TravelPolicy(String id, PolicyPeriod period, Money coverageLimit) {
        super(id, period, coverageLimit);
    }

    @Override
    public Money calculatePremium() {
        return coverageLimit().times(PREMIUM_RATE);
    }

    @Override
    protected Money calculatePayout(Money loss) {
        // Chi trả 70% tổn thất phát sinh (với loss = 50.000.000 -> payout = 35.000.000)
        return loss.times(PAYOUT_FACTOR);
    }

    @Override
    public String typeName() {
        return TYPE_NAME;
    }
}
