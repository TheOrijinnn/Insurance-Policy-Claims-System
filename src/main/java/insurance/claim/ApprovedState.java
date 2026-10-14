package insurance.claim;

final class ApprovedState implements ClaimState {
    static final ApprovedState INSTANCE = new ApprovedState();

    private ApprovedState() {}

    @Override
    public String name() {
        return "Approved";
    }

    @Override
    public void pay(Claim c) {
        c.moveTo(PaidState.INSTANCE);
    }
}
