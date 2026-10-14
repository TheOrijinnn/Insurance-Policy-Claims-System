package insurance.policy;

import insurance.common.Money;
import insurance.common.PolicyPeriod;

public class HealthPolicy extends Policy {
    private static final Money DEDUCTIBLE = Money.of("2000000");
    private static final String PAYOUT_FACTOR = "0.80";
    private static final String PREMIUM_RATE = "0.04";
    private static final String TYPE_NAME = "Health";

    public HealthPolicy(String id, PolicyPeriod period, Money coverageLimit) {
        super(id, period, coverageLimit);
    }

    @Override
    public Money calculatePremium() {
        return coverageLimit().times(PREMIUM_RATE);
    }

    @Override
    protected Money calculatePayout(Money loss) {
        return loss.minus(DEDUCTIBLE).atLeastZero().times(PAYOUT_FACTOR);
    }

    @Override
    public String typeName() {
        return TYPE_NAME;
    }
}
