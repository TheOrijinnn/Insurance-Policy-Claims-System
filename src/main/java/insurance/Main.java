package insurance;

import insurance.claim.Claim;
import insurance.claim.ClaimRegistry;
import insurance.common.InvalidClaimTransitionException;
import insurance.common.Money;
import insurance.common.PolicyPeriod;
import insurance.policy.AutoPolicy;
import insurance.policy.HealthPolicy;
import insurance.policy.HomePolicy;
import insurance.policy.Policy;
import insurance.policy.PolicyFactory;
import insurance.policy.PolicyRequest;
import insurance.policy.Policyholder;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("   CLAIMFLOW SYSTEM - DEMO CONSOLE RUN & VERIFICATION    ");
        System.out.println("==========================================================");

        // 1. Khởi tạo PolicyFactory và đăng ký các loại Policy
        PolicyFactory factory = defaultPolicyFactory();
        System.out.println("[1] PolicyFactory khoi tao thanh cong voi Map-based registration (OCP).");

        // 2. Tạo Policyholder và phát hành hợp đồng
        PolicyPeriod period2026 = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        Policyholder an = new Policyholder("H-001", "Nguyen Van An");

        Policy autoPolicy = factory.create("AUTO", new PolicyRequest("POL-AUTO-01", period2026, Money.of("100000000")));
        an.addPolicy(autoPolicy);

        System.out.println("[2] Phat hanh hop dong cho " + an.name() + ":");
        System.out.println("    - Loai: " + autoPolicy.typeName());
        System.out.println("    - Phi bao hiem (Premium): " + autoPolicy.calculatePremium());
        System.out.println("    - Han muc (Coverage Limit): " + autoPolicy.coverageLimit());

        // 3. Demo State Workflow cho Claim (Happy Path)
        System.out.println("\n[3] Demo State Pattern tren Claim (Happy path):");
        Claim claim1 = new Claim("CLM-001", autoPolicy, Money.of("50000000"), LocalDate.of(2026, 6, 15));
        System.out.println("    - Khoi tao Claim: State = " + claim1.stateName() + ", Loss = " + claim1.loss());

        claim1.startReview();
        System.out.println("    - Sau startReview(): State = " + claim1.stateName());

        claim1.approve();
        System.out.println("    - Sau approve(): State = " + claim1.stateName() +
                ", So tien duyet = " + claim1.approvedPayout().orElse(Money.ZERO) +
                ", Tong da chi Policy = " + autoPolicy.totalPayout() +
                ", Han muc con lai = " + autoPolicy.remainingLimit());

        claim1.pay();
        System.out.println("    - Sau pay(): State = " + claim1.stateName());

        // 4. Demo Chặn hành động sai quy trình (T2: Submitted -> pay)
        System.out.println("\n[4] Demo chan hanh dong sai quy trinh (State Pattern Protection):");
        Claim claim2 = new Claim("CLM-002", autoPolicy, Money.of("10000000"), LocalDate.of(2026, 7, 1));
        try {
            System.out.println("    - Thu goi pay() truc tiep khi Claim o trang thai " + claim2.stateName() + "...");
            claim2.pay();
        } catch (InvalidClaimTransitionException e) {
            System.out.println("    -> CHAN THANH CONG! Exception: " + e.getMessage());
            System.out.println("    -> Trang thai hien tai van duoc bao toan: " + claim2.stateName());
        }

        // 5. Lưu vào ClaimRegistry
        ClaimRegistry registry = new ClaimRegistry();
        registry.add(claim1);
        registry.add(claim2);
        System.out.println("\n[5] ClaimRegistry tong so claim da ghi nhan: " + registry.all().size());
        System.out.println("    - So claim thuoc ve " + an.name() + ": " + registry.byHolder(an).size());

        System.out.println("\n==========================================================");
        System.out.println("   DEMO HOAN TAT: TAT CA BAT BIEN & STATE HOAT DONG DUNG! ");
        System.out.println("==========================================================");
    }

    public static PolicyFactory defaultPolicyFactory() {
        PolicyFactory factory = new PolicyFactory();
        factory.register("AUTO", r -> new AutoPolicy(r.id(), r.period(), r.coverageLimit()))
               .register("HOME", r -> new HomePolicy(r.id(), r.period(), r.coverageLimit()))
               .register("HEALTH", r -> new HealthPolicy(r.id(), r.period(), r.coverageLimit()));
        return factory;
    }
}
