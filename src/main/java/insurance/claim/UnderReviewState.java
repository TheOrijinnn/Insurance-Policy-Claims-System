package insurance.claim;

import insurance.common.Money;

final class UnderReviewState implements ClaimState {
    static final UnderReviewState INSTANCE = new UnderReviewState();

    private UnderReviewState() {}

    @Override
    public String name() {
        return "UnderReview";
    }

    @Override
    public void approve(Claim c) {
        Money p = c.policy().approvePayout(c.loss(), c.lossDate());
        c.recordApproval(p);
        c.moveTo(ApprovedState.INSTANCE);
    }

    @Override
    public void reject(Claim c, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason cannot be empty");
        }
        c.recordRejection(reason);
        c.moveTo(RejectedState.INSTANCE);
    }
}
