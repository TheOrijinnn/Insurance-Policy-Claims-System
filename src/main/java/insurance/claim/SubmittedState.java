package insurance.claim;

final class SubmittedState implements ClaimState {
    static final SubmittedState INSTANCE = new SubmittedState();

    private SubmittedState() {}

    @Override
    public String name() {
        return "Submitted";
    }

    @Override
    public void startReview(Claim c) {
        c.moveTo(UnderReviewState.INSTANCE);
    }
}
