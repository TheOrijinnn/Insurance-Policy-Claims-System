package insurance.policy;

import insurance.common.Money;
import insurance.common.PolicyPeriod;

public class AutoPolicy extends Policy {
    private static final Money DEDUCTIBLE = Money.of("5000000");
    private static final String PREMIUM_RATE = "0.025";
    private static final String TYPE_NAME = "Auto";

    public AutoPolicy(String id, PolicyPeriod period, Money coverageLimit) {
        super(id, period, coverageLimit);
    }

    @Override
    public Money calculatePremium() {
        return coverageLimit().times(PREMIUM_RATE);
    }

    @Override
    protected Money calculatePayout(Money loss) {
        return loss.minus(DEDUCTIBLE).atLeastZero();
    }

    @Override
    public String typeName() {
        return TYPE_NAME;
    }
}
