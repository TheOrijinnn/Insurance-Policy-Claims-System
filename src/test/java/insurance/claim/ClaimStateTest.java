package insurance.claim;

import insurance.common.InvalidClaimTransitionException;
import insurance.common.Money;
import insurance.common.PolicyPeriod;
import insurance.policy.AutoPolicy;
import insurance.policy.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ClaimStateTest {

    private PolicyPeriod period;
    private Policy policy;
    private LocalDate lossDate;
    private Money loss;
    private Map<String, Consumer<Claim>> actions;
    private Map<String, Supplier<Claim>> stateFixtures;

    @BeforeEach
    void setUp() {
        period = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        policy = new AutoPolicy("POL-001", period, Money.of("100000000"));
        lossDate = LocalDate.of(2026, 6, 15);
        loss = Money.of("50000000");

        actions = Map.of(
                "startReview", Claim::startReview,
                "approve", Claim::approve,
                "reject", c -> c.reject("Valid rejection reason"),
                "pay", Claim::pay
        );

        stateFixtures = Map.of(
                "Submitted", () -> new Claim("CLM-001", policy, loss, lossDate),
                "UnderReview", () -> {
                    Claim c = new Claim("CLM-001", policy, loss, lossDate);
                    c.startReview();
                    return c;
                },
                "Approved", () -> {
                    Claim c = new Claim("CLM-001", policy, loss, lossDate);
                    c.startReview();
                    c.approve();
                    return c;
                },
                "Rejected", () -> {
                    Claim c = new Claim("CLM-001", policy, loss, lossDate);
                    c.startReview();
                    c.reject("Initial rejection reason");
                    return c;
                },
                "Paid", () -> {
                    Claim c = new Claim("CLM-001", policy, loss, lossDate);
                    c.startReview();
                    c.approve();
                    c.pay();
                    return c;
                }
        );
    }

    private Claim in(String fromState) {
        return stateFixtures.get(fromState).get();
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                // Hàng Submitted (3 ô ✗, trong đó Submitted.pay là T2)
                Arguments.of("Submitted", "approve", Money.ZERO),
                Arguments.of("Submitted", "reject", Money.ZERO),
                Arguments.of("Submitted", "pay", Money.ZERO),

                // Hàng UnderReview (2 ô ✗)
                Arguments.of("UnderReview", "startReview", Money.ZERO),
                Arguments.of("UnderReview", "pay", Money.ZERO),

                // Hàng Approved (3 ô ✗, trong đó Approved.approve là T3)
                Arguments.of("Approved", "startReview", Money.of("45000000")),
                Arguments.of("Approved", "approve", Money.of("45000000")),
                Arguments.of("Approved", "reject", Money.of("45000000")),

                // Hàng Rejected (4 ô ✗)
                Arguments.of("Rejected", "startReview", Money.ZERO),
                Arguments.of("Rejected", "approve", Money.ZERO),
                Arguments.of("Rejected", "reject", Money.ZERO),
                Arguments.of("Rejected", "pay", Money.ZERO),

                // Hàng Paid (4 ô ✗)
                Arguments.of("Paid", "startReview", Money.of("45000000")),
                Arguments.of("Paid", "approve", Money.of("45000000")),
                Arguments.of("Paid", "reject", Money.of("45000000")),
                Arguments.of("Paid", "pay", Money.of("45000000"))
        );
    }

    @ParameterizedTest(name = "{0}.{1}")
    @MethodSource("invalidTransitions")
    void testInvalidTransitionThrowsAndPreservesState(String from, String action, Money expectedTotalPayout) {
        Claim c = in(from);
        var ex = assertThrows(InvalidClaimTransitionException.class, () -> actions.get(action).accept(c));
        assertEquals(from, ex.stateName());
        assertEquals(action, ex.action());
        assertEquals(from, c.stateName());
        assertEquals(expectedTotalPayout, policy.totalPayout());
    }

    // ==========================================
    // 4 ô chuyển trạng thái hợp lệ (✓)
    // ==========================================

    @Test
    @DisplayName("Submitted.startReview -> UnderReview")
    void testSubmittedStartReview() {
        Claim c = in("Submitted");
        c.startReview();
        assertEquals("UnderReview", c.stateName());
    }

    @Test
    @DisplayName("UnderReview.approve -> Approved, approvedPayout = 45tr, totalPayout = 45tr")
    void testUnderReviewApprove() {
        Claim c = in("UnderReview");
        c.approve();
        assertEquals("Approved", c.stateName());
        assertEquals(Money.of("45000000"), c.approvedPayout().orElseThrow());
        assertEquals(Money.of("45000000"), policy.totalPayout());
    }

    @Test
    @DisplayName("UnderReview.reject -> Rejected")
    void testUnderReviewReject() {
        Claim c = in("UnderReview");
        c.reject("Documents expired");
        assertEquals("Rejected", c.stateName());
        assertEquals("Documents expired", c.rejectionReason().orElseThrow());
        assertEquals(Money.ZERO, policy.totalPayout());
    }

    @Test
    @DisplayName("Approved.pay -> Paid")
    void testApprovedPay() {
        Claim c = in("Approved");
        c.pay();
        assertEquals("Paid", c.stateName());
        assertEquals(Money.of("45000000"), c.approvedPayout().orElseThrow());
        assertEquals(Money.of("45000000"), policy.totalPayout());
    }

    // ==========================================
    // Test lẻ bắt buộc ở Block 4 (ca approve ngoài kỳ hiệu lực: xem T7 trong PolymorphismAndPeriodTest)
    // ==========================================

    @Test
    @DisplayName("reject('') ném IllegalArgumentException và claim vẫn UnderReview")
    void testRejectEmptyReasonStaysUnderReview() {
        Claim c = in("UnderReview");
        assertThrows(IllegalArgumentException.class, () -> c.reject(""));
        assertEquals("UnderReview", c.stateName());
        assertTrue(c.rejectionReason().isEmpty());
    }

    // ==========================================
    // Block 6: T3 và T4 (kiểm tra approvedPayout và chuỗi 45 / 45 / 10 / chặn ở mức Claim)
    // ==========================================

    @Test
    @DisplayName("T3: Approve xong, gọi approve() lần 2 -> InvalidClaimTransitionException, totalPayout vẫn 45tr, approvedPayout vẫn 45tr")
    void testT3_ApproveTwiceBlockedAndPreservesPayouts() {
        Claim c = in("Approved");
        assertEquals(Money.of("45000000"), c.approvedPayout().orElseThrow());
        assertEquals(Money.of("45000000"), policy.totalPayout());

        InvalidClaimTransitionException ex = assertThrows(InvalidClaimTransitionException.class, c::approve);
        assertEquals("Approved", ex.stateName());
        assertEquals("approve", ex.action());
        assertEquals("Approved", c.stateName());
        assertEquals(Money.of("45000000"), c.approvedPayout().orElseThrow());
        assertEquals(Money.of("45000000"), policy.totalPayout());
    }

    @Test
    @DisplayName("T4: Một AutoPolicy, 3 claim loss 50tr duyệt (45tr, 45tr, 10tr), claim thứ 4 startReview rồi approve -> CoverageLimitExceededException")
    void testT4_FourClaimsCoverageLimitCappingAndBlocking() {
        Claim c1 = new Claim("CLM-1", policy, loss, lossDate);
        Claim c2 = new Claim("CLM-2", policy, loss, lossDate);
        Claim c3 = new Claim("CLM-3", policy, loss, lossDate);
        Claim c4 = new Claim("CLM-4", policy, loss, lossDate);

        c1.startReview();
        c1.approve();
        assertEquals("Approved", c1.stateName());
        assertEquals(Money.of("45000000"), c1.approvedPayout().orElseThrow());

        c2.startReview();
        c2.approve();
        assertEquals("Approved", c2.stateName());
        assertEquals(Money.of("45000000"), c2.approvedPayout().orElseThrow());

        c3.startReview();
        c3.approve();
        assertEquals("Approved", c3.stateName());
        assertEquals(Money.of("10000000"), c3.approvedPayout().orElseThrow());
        assertEquals(Money.of("100000000"), policy.totalPayout());

        c4.startReview();
        assertThrows(insurance.common.CoverageLimitExceededException.class, c4::approve);
        assertEquals("UnderReview", c4.stateName());
        assertTrue(c4.approvedPayout().isEmpty());
        assertEquals(Money.of("100000000"), policy.totalPayout());
    }
}
