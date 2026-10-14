package insurance.claim;

final class RejectedState implements ClaimState {
    static final RejectedState INSTANCE = new RejectedState();

    private RejectedState() {}

    @Override
    public String name() {
        return "Rejected";
    }
}
