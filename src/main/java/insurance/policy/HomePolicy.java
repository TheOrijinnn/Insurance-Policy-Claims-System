package insurance.policy;

import insurance.common.Money;
import insurance.common.PolicyPeriod;

public class HomePolicy extends Policy {
    public HomePolicy(String id, PolicyPeriod period, Money coverageLimit) {
        super(id, period, coverageLimit);
    }

    @Override
    public Money calculatePremium() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    protected Money calculatePayout(Money loss) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String typeName() {
        throw new UnsupportedOperationException("TODO");
    }
}
