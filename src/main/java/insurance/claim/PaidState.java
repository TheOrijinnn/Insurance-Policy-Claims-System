package insurance.claim;

final class PaidState implements ClaimState {
    static final PaidState INSTANCE = new PaidState();

    private PaidState() {}

    @Override
    public String name() {
        return "Paid";
    }
}
