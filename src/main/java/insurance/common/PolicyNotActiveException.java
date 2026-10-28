package insurance.common;

import java.time.LocalDate;

public class PolicyNotActiveException extends InsuranceException {
    private final String policyId;
    private final LocalDate date;

    public PolicyNotActiveException(String policyId, LocalDate date) {
        super("Policy " + policyId + " is not active on " + date);
        this.policyId = policyId;
        this.date = date;
    }

    public String policyId() {
        return policyId;
    }

    public LocalDate date() {
        return date;
    }
}
