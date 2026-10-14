package insurance.claim;

import insurance.common.Money;
import insurance.policy.Policy;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public final class Claim {
    private final String id;
    private final Policy policy;
    private final Money loss;
    private final LocalDate lossDate;

    private ClaimState state = SubmittedState.INSTANCE;
    private Money approvedPayout = null;
    private String rejectionReason = null;

    public Claim(String id, Policy policy, Money loss, LocalDate lossDate) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Claim id cannot be null or empty");
        }
        if (policy == null) {
            throw new IllegalArgumentException("Policy cannot be null");
        }
        if (loss == null || !loss.isPositive()) {
            throw new IllegalArgumentException("Loss must be positive");
        }
        if (lossDate == null) {
            throw new IllegalArgumentException("Loss date cannot be null");
        }
        this.id = id;
        this.policy = policy;
        this.loss = loss;
        this.lossDate = lossDate;
    }

    public void startReview() {
        state.startReview(this);
    }

    public void approve() {
        state.approve(this);
    }

    public void reject(String reason) {
        state.reject(this, reason);
    }

    public void pay() {
        state.pay(this);
    }

    // Package-private methods: phục vụ cho State Pattern chuyển đổi trạng thái
    void moveTo(ClaimState newState) {
        this.state = Objects.requireNonNull(newState, "New state cannot be null");
    }

    void recordApproval(Money payout) {
        this.approvedPayout = Objects.requireNonNull(payout, "Approved payout cannot be null");
    }

    void recordRejection(String reason) {
        this.rejectionReason = Objects.requireNonNull(reason, "Rejection reason cannot be null");
    }

    public String id() {
        return id;
    }

    public Policy policy() {
        return policy;
    }

    public Money loss() {
        return loss;
    }

    public LocalDate lossDate() {
        return lossDate;
    }

    public String stateName() {
        return state.name();
    }

    public Optional<Money> approvedPayout() {
        return Optional.ofNullable(approvedPayout);
    }

    public Optional<String> rejectionReason() {
        return Optional.ofNullable(rejectionReason);
    }
}
