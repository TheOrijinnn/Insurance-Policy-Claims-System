package insurance.common;

public class CoverageLimitExceededException extends InsuranceException {
    private final String policyId;
    private final Money remaining;

    public CoverageLimitExceededException(String policyId, Money remaining) {
        super("Coverage limit exceeded for policy " + policyId + ", remaining limit: " + remaining);
        this.policyId = policyId;
        this.remaining = remaining;
    }

    public String policyId() {
        return policyId;
    }

    public Money remaining() {
        return remaining;
    }
}
