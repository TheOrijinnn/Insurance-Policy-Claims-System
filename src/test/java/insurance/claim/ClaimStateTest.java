package insurance.claim;

import insurance.common.InvalidClaimTransitionException;
import insurance.common.Money;
import insurance.common.PolicyPeriod;
import insurance.policy.AutoPolicy;
import insurance.policy.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ClaimStateTest {

    private Policy policy;
    private LocalDate lossDate;
    private Money loss;

    @BeforeEach
    void setUp() {
        PolicyPeriod period = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        policy = new AutoPolicy("POL-001", period, Money.of("100000000"));
        lossDate = LocalDate.of(2026, 6, 15);
        loss = Money.of("50000000");
    }

    private Claim createSubmittedClaim() {
        return new Claim("CLM-001", policy, loss, lossDate);
    }

    private Claim createUnderReviewClaim() {
        Claim claim = createSubmittedClaim();
        claim.startReview();
        return claim;
    }

    private Claim createRejectedClaim() {
        Claim claim = createUnderReviewClaim();
        claim.reject("Fraudulent documentation");
        return claim;
    }

    private Claim createApprovedClaim() {
        Claim claim = createUnderReviewClaim();
        claim.approve();
        return claim;
    }

    // ==========================================
    // 1. Trạng thái Submitted (4 ô: 1 hợp lệ, 3 ném exception)
    // ==========================================

    @Test
    @DisplayName("Submitted: startReview() -> chuyển thành công sang UnderReview")
    void testSubmitted_startReview_success() {
        Claim claim = createSubmittedClaim();
        assertEquals("Submitted", claim.stateName());

        claim.startReview();
        assertEquals("UnderReview", claim.stateName());
    }

    @Test
    @DisplayName("Submitted: approve() -> ném InvalidClaimTransitionException")
    void testSubmitted_approve_throws() {
        Claim claim = createSubmittedClaim();

        InvalidClaimTransitionException ex = assertThrows(InvalidClaimTransitionException.class, claim::approve);
        assertEquals("Submitted", ex.stateName());
        assertEquals("approve", ex.action());
    }

    @Test
    @DisplayName("Submitted: reject() -> ném InvalidClaimTransitionException")
    void testSubmitted_reject_throws() {
        Claim claim = createSubmittedClaim();

        InvalidClaimTransitionException ex = assertThrows(InvalidClaimTransitionException.class,
                () -> claim.reject("No evidence"));
        assertEquals("Submitted", ex.stateName());
        assertEquals("reject", ex.action());
    }

    @Test
    @DisplayName("T2: Submitted: pay() bị chặn -> kiểm tra stateName = 'Submitted' và action = 'pay'")
    void testSubmitted_pay_blocked_T2() {
        Claim claim = createSubmittedClaim();

        InvalidClaimTransitionException ex = assertThrows(InvalidClaimTransitionException.class, claim::pay);
        assertEquals("Submitted", ex.stateName());
        assertEquals("pay", ex.action());
    }

    // ==========================================
    // 2. Trạng thái UnderReview (startReview ✗, reject ✓, pay ✗, approve ✓)
    // ==========================================

    @Test
    @DisplayName("UnderReview: startReview() -> ném InvalidClaimTransitionException")
    void testUnderReview_startReview_throws() {
        Claim claim = createUnderReviewClaim();

        InvalidClaimTransitionException ex = assertThrows(InvalidClaimTransitionException.class, claim::startReview);
        assertEquals("UnderReview", ex.stateName());
        assertEquals("startReview", ex.action());
    }

    @Test
    @DisplayName("UnderReview: reject(reason) -> chuyển thành công sang Rejected")
    void testUnderReview_reject_success() {
        Claim claim = createUnderReviewClaim();

        claim.reject("Documents expired");
        assertEquals("Rejected", claim.stateName());
        assertEquals("Documents expired", claim.rejectionReason().orElseThrow());
    }

    @Test
    @DisplayName("UnderReview: reject với reason rỗng -> ném IllegalArgumentException")
    void testUnderReview_reject_emptyReason_throws() {
        Claim claim = createUnderReviewClaim();

        assertThrows(IllegalArgumentException.class, () -> claim.reject(""));
        assertThrows(IllegalArgumentException.class, () -> claim.reject("   "));
        assertThrows(IllegalArgumentException.class, () -> claim.reject(null));
    }

    @Test
    @DisplayName("UnderReview: pay() -> ném InvalidClaimTransitionException")
    void testUnderReview_pay_throws() {
        Claim claim = createUnderReviewClaim();

        InvalidClaimTransitionException ex = assertThrows(InvalidClaimTransitionException.class, claim::pay);
        assertEquals("UnderReview", ex.stateName());
        assertEquals("pay", ex.action());
    }

    @Test
    @DisplayName("UnderReview: approve() -> chuyển thành công sang Approved và lưu approvedPayout")
    void testUnderReview_approve_success() {
        Claim claim = createUnderReviewClaim();

        claim.approve();
        assertEquals("Approved", claim.stateName());
        assertTrue(claim.approvedPayout().isPresent());
        assertEquals(Money.of("45000000"), claim.approvedPayout().get());
    }

    // ==========================================
    // 3. Trạng thái Rejected (cả 4 hành động ném exception)
    // ==========================================

    @Test
    @DisplayName("Rejected: cả 4 hành động đều ném InvalidClaimTransitionException")
    void testRejected_allActions_throw() {
        Claim claim = createRejectedClaim();
        assertEquals("Rejected", claim.stateName());

        assertThrows(InvalidClaimTransitionException.class, claim::startReview);
        assertThrows(InvalidClaimTransitionException.class, claim::approve);
        assertThrows(InvalidClaimTransitionException.class, () -> claim.reject("New reason"));
        assertThrows(InvalidClaimTransitionException.class, claim::pay);
    }

    // ==========================================
    // 4. Trạng thái Approved & Paid
    // ==========================================

    @Test
    @DisplayName("Approved: pay() -> chuyển thành công sang Paid")
    void testApproved_pay_success() {
        Claim claim = createApprovedClaim();
        assertEquals("Approved", claim.stateName());

        claim.pay();
        assertEquals("Paid", claim.stateName());
    }

    @Test
    @DisplayName("Paid: cả 4 hành động đều ném InvalidClaimTransitionException")
    void testPaid_allActions_throw() {
        Claim claim = createApprovedClaim();
        claim.pay();
        assertEquals("Paid", claim.stateName());

        assertThrows(InvalidClaimTransitionException.class, claim::startReview);
        assertThrows(InvalidClaimTransitionException.class, claim::approve);
        assertThrows(InvalidClaimTransitionException.class, () -> claim.reject("Late rejection"));
        assertThrows(InvalidClaimTransitionException.class, claim::pay);
    }

    // ==========================================
    // 5. Kiểm tra ràng buộc đầu vào Claim Constructor [GIẢ ĐỊNH]
    // ==========================================

    @Test
    @DisplayName("[GIẢ ĐỊNH] loss không dương (<= 0) ném IllegalArgumentException ngay trong constructor")
    void testClaimConstructor_nonPositiveLoss_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Claim("C1", policy, Money.ZERO, lossDate));
        assertThrows(IllegalArgumentException.class, () -> new Claim("C2", policy, Money.of("-100000"), lossDate));
        assertThrows(IllegalArgumentException.class, () -> new Claim("C3", policy, null, lossDate));
    }
}
