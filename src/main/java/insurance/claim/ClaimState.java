package insurance.claim;

import insurance.common.InvalidClaimTransitionException;

interface ClaimState {
    String name();

    default void startReview(Claim c) {
        throw illegal("startReview");
    }

    default void approve(Claim c) {
        throw illegal("approve");
    }

    default void reject(Claim c, String reason) {
        throw illegal("reject");
    }

    default void pay(Claim c) {
        throw illegal("pay");
    }

    private InvalidClaimTransitionException illegal(String action) {
        return new InvalidClaimTransitionException(name(), action);
    }
}
