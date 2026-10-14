package insurance.common;

public class UnknownPolicyTypeException extends InsuranceException {
    private final String type;

    public UnknownPolicyTypeException(String type) {
        super("Unknown policy type: " + type);
        this.type = type;
    }

    public String type() {
        return type;
    }

    public String getType() {
        return type;
    }
}
