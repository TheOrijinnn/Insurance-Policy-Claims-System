package insurance.common;

public class InvalidClaimTransitionException extends InsuranceException {
    private final String stateName;
    private final String action;

    public InvalidClaimTransitionException(String stateName, String action) {
        super("Invalid claim transition from state: " + stateName + " with action: " + action);
        this.stateName = stateName;
        this.action = action;
    }

    public String getStateName() {
        return stateName;
    }

    public String getAction() {
        return action;
    }
}
