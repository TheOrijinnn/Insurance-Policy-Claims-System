package insurance.claim;

import insurance.common.Money;
import insurance.common.PolicyPeriod;
import insurance.policy.AutoPolicy;
import insurance.policy.HealthPolicy;
import insurance.policy.HomePolicy;
import insurance.policy.Policy;
import insurance.policy.Policyholder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClaimRegistryTest {

    private PolicyPeriod period2026;
    private Money limit100m;
    private LocalDate lossDate;

    @BeforeEach
    void setUp() {
        period2026 = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        limit100m = Money.of("100000000");
        lossDate = LocalDate.of(2026, 6, 15);
    }

    @Test
    @DisplayName("byHolder(An) ra 2 claim đúng thứ tự, byHolder(Bình) ra 1, holder chưa có claim ra rỗng, all().add(...) ném UnsupportedOperationException")
    void testRegistryFilteringOrderAndImmutability() {
        Policyholder an = new Policyholder("H-AN", "An");
        Policyholder binh = new Policyholder("H-BINH", "Binh");
        Policyholder cuong = new Policyholder("H-CUONG", "Cuong");

        Policy anPolicy1 = new AutoPolicy("P-AN-1", period2026, limit100m);
        Policy anPolicy2 = new HomePolicy("P-AN-2", period2026, limit100m);
        Policy binhPolicy = new HealthPolicy("P-BINH-1", period2026, limit100m);

        an.addPolicy(anPolicy1);
        an.addPolicy(anPolicy2);
        binh.addPolicy(binhPolicy);

        Claim cAn1 = new Claim("C-AN-1", anPolicy1, Money.of("20000000"), lossDate);
        Claim cBinh1 = new Claim("C-BINH-1", binhPolicy, Money.of("15000000"), lossDate);
        Claim cAn2 = new Claim("C-AN-2", anPolicy2, Money.of("30000000"), lossDate);

        ClaimRegistry registry = new ClaimRegistry();
        registry.add(cAn1);
        registry.add(cBinh1);
        registry.add(cAn2);

        assertEquals(List.of(cAn1, cAn2), registry.byHolder(an));
        assertEquals(List.of(cBinh1), registry.byHolder(binh));
        assertTrue(registry.byHolder(cuong).isEmpty());
        assertEquals(List.of(cAn1, cBinh1, cAn2), registry.all());

        Claim extra = new Claim("C-EXTRA", anPolicy1, Money.of("10000000"), lossDate);
        assertThrows(UnsupportedOperationException.class, () -> registry.all().add(extra));
    }

    @Test
    @DisplayName("add() cùng một Claim hai lần -> IllegalArgumentException, registry không bị nhân đôi")
    void testAddDuplicateClaimThrows() {
        Claim claim = new Claim("C-1", new AutoPolicy("P-1", period2026, limit100m), Money.of("20000000"), lossDate);
        ClaimRegistry registry = new ClaimRegistry();
        registry.add(claim);

        assertThrows(IllegalArgumentException.class, () -> registry.add(claim));
        assertEquals(List.of(claim), registry.all());
    }
}
