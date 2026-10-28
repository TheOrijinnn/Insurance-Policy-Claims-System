package insurance;

import insurance.claim.Claim;
import insurance.claim.ClaimRegistry;
import insurance.common.InsuranceException;
import insurance.common.InvalidClaimTransitionException;
import insurance.common.Money;
import insurance.common.PolicyPeriod;
import insurance.engine.ClaimsProcessor;
import insurance.engine.StatementGenerator;
import insurance.policy.AutoPolicy;
import insurance.policy.HealthPolicy;
import insurance.policy.HomePolicy;
import insurance.policy.Policy;
import insurance.policy.PolicyFactory;
import insurance.policy.PolicyRequest;
import insurance.policy.Policyholder;

import java.time.LocalDate;
import java.util.List;

/**
 * Demo end-to-end: phát hành hợp đồng, xử lý bồi thường đa hình, chặn thao tác sai và in sao kê.
 * Đồng thời là composition root: nơi duy nhất đăng ký các loại Policy vào PolicyFactory.
 */
public class Main {
    private static final PolicyPeriod YEAR_2026 = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
    private static final PolicyPeriod YEAR_2025 = new PolicyPeriod(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1));
    private static final Money LIMIT_100M = Money.of("100000000");
    private static final Money LOSS_50M = Money.of("50000000");
    private static final LocalDate LOSS_DATE = LocalDate.of(2026, 6, 15);

    private final ClaimsProcessor processor = new ClaimsProcessor();
    private final ClaimRegistry registry = new ClaimRegistry();
    private int claimSeq = 0;

    public static void main(String[] args) {
        new Main().run();
    }

    public static PolicyFactory defaultPolicyFactory() {
        return new PolicyFactory()
                .register("AUTO", r -> new AutoPolicy(r.id(), r.period(), r.coverageLimit()))
                .register("HOME", r -> new HomePolicy(r.id(), r.period(), r.coverageLimit()))
                .register("HEALTH", r -> new HealthPolicy(r.id(), r.period(), r.coverageLimit()));
    }

    private void run() {
        PolicyFactory factory = defaultPolicyFactory();
        Policyholder an = new Policyholder("H-001", "Nguyễn Văn An");

        section("1. Phát hành hợp đồng qua PolicyFactory (tạo theo tên loại, không if/switch)");
        Policy auto = issue(factory, an, "AUTO", "POL-AUTO-01", YEAR_2026);
        Policy home = issue(factory, an, "HOME", "POL-HOME-01", YEAR_2026);
        Policy health = issue(factory, an, "HEALTH", "POL-HEALTH-01", YEAR_2026);
        issue(factory, an, "HOME", "POL-HOME-25", YEAR_2025);

        section("2. Happy path + đa hình: cùng tổn thất 50.000.000, cùng ClaimsProcessor, 3 loại hợp đồng");
        for (Policy p : List.of(auto, home, health)) {
            process(newClaim(p, LOSS_50M, LOSS_DATE));
        }

        section("3. Từ chối claim rồi gọi pay() -> Claim tự chặn chuyển trạng thái sai");
        Claim rejected = newClaim(home, Money.of("8000000"), LOSS_DATE);
        rejected.startReview();
        rejected.reject("Thiếu hồ sơ giám định");
        System.out.println("   " + rejected.id() + " -> " + rejected.stateName()
                + " (lý do: " + rejected.rejectionReason().orElseThrow() + ")");
        try {
            rejected.pay();
        } catch (InvalidClaimTransitionException e) {
            System.out.println("   " + rejected.id() + ".pay() bị chặn: " + e.getMessage()
                    + " -> vẫn " + rejected.stateName());
        }

        section("4. Hạn mức bồi thường: Auto (đã duyệt 45.000.000) nhận thêm 3 claim 50.000.000");
        for (int i = 0; i < 3; i++) {
            process(newClaim(auto, LOSS_50M, LOSS_DATE));
        }

        section("5. Tổn thất ngoài kỳ hiệu lực (2027-01-01) -> Policy từ chối, claim chuyển Rejected");
        process(newClaim(health, LOSS_50M, LocalDate.of(2027, 1, 1)));

        section("6. Sao kê cho chủ hợp đồng tại ngày 2026-12-01");
        System.out.print(new StatementGenerator().generate(an, LocalDate.of(2026, 12, 1), registry));
    }

    private static Policy issue(PolicyFactory factory, Policyholder holder, String type, String id, PolicyPeriod period) {
        Policy p = factory.create(type, new PolicyRequest(id, period, LIMIT_100M));
        holder.addPolicy(p);
        System.out.printf("   [%s] %-6s | hạn mức %s | phí %s | hiệu lực %s -> %s%n",
                p.id(), p.typeName(), p.coverageLimit(), p.calculatePremium(), period.start(), period.end());
        return p;
    }

    private Claim newClaim(Policy policy, Money loss, LocalDate lossDate) {
        Claim c = new Claim(String.format("CLM-%03d", ++claimSeq), policy, loss, lossDate);
        registry.add(c);
        return c;
    }

    private void process(Claim c) {
        String prefix = String.format("   %s [%s, tổn thất %s] -> ", c.id(), c.policy().typeName(), c.loss());
        try {
            Money payout = processor.process(c);
            System.out.println(prefix + c.stateName() + " | chi trả " + payout
                    + " | hạn mức còn lại " + c.policy().remainingLimit());
        } catch (InsuranceException e) {
            System.out.println(prefix + c.stateName() + " | " + e.getMessage());
        }
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("== " + title);
    }
}
