package insurance.policy;

import insurance.common.Money;
import insurance.common.PolicyPeriod;

public class HomePolicy extends Policy {
    private static final String PAYOUT_FACTOR = "0.85";
    private static final String PREMIUM_RATE = "0.01";
    private static final Money BASE_FEE = Money.of("500000");
    private static final String TYPE_NAME = "Home";

    public HomePolicy(String id, PolicyPeriod period, Money coverageLimit) {
        super(id, period, coverageLimit);
    }

    @Override
    public Money calculatePremium() {
        return coverageLimit().times(PREMIUM_RATE).plus(BASE_FEE);
    }

    @Override
    protected Money calculatePayout(Money loss) {
        return loss.times(PAYOUT_FACTOR);
    }

    @Override
    public String typeName() {
        return TYPE_NAME;
    }
}
