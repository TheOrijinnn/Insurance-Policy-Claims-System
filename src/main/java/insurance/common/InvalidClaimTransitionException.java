package insurance.common;

public class InvalidClaimTransitionException extends InsuranceException {
    private final String stateName;
    private final String action;

    public InvalidClaimTransitionException(String stateName, String action) {
        super("Cannot perform action '" + action + "' in state '" + stateName + "'");
        this.stateName = stateName;
        this.action = action;
    }

    public String stateName() {
        return stateName;
    }

    public String action() {
        return action;
    }

    public String getStateName() {
        return stateName;
    }

    public String getAction() {
        return action;
    }
}
