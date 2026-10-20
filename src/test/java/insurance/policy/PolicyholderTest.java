package insurance.policy;

import insurance.common.Money;
import insurance.common.PolicyPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolicyholderTest {

    private PolicyPeriod period2026;
    private Money limit100m;

    @BeforeEach
    void setUp() {
        period2026 = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        limit100m = Money.of("100000000");
    }

    @Test
    @DisplayName("Thêm 3 policy thì policies().size() == 3 và gọi policies().add(...) ném UnsupportedOperationException")
    void testAddThreePoliciesAndUnmodifiableView() {
        Policyholder holder = new Policyholder("H-01", "Nguyen Van An");
        Policy p1 = new AutoPolicy("P-01", period2026, limit100m);
        Policy p2 = new HomePolicy("P-02", period2026, limit100m);
        Policy p3 = new HealthPolicy("P-03", period2026, limit100m);

        holder.addPolicy(p1);
        holder.addPolicy(p2);
        holder.addPolicy(p3);

        List<Policy> policies = holder.policies();
        assertEquals(3, policies.size());
        assertEquals(List.of(p1, p2, p3), policies);
        assertEquals("H-01", holder.id());
        assertEquals("Nguyen Van An", holder.name());

        Policy extra = new AutoPolicy("P-04", period2026, limit100m);
        assertThrows(UnsupportedOperationException.class, () -> policies.add(extra));
    }

    @Test
    @DisplayName("addPolicy kiểm tra null và trùng đối tượng")
    void testAddNullOrDuplicatePolicyThrows() {
        Policyholder holder = new Policyholder("H-01", "Nguyen Van An");
        Policy p1 = new AutoPolicy("P-01", period2026, limit100m);

        assertThrows(IllegalArgumentException.class, () -> holder.addPolicy(null));

        holder.addPolicy(p1);
        assertThrows(IllegalArgumentException.class, () -> holder.addPolicy(p1));
        assertEquals(1, holder.policies().size());
    }
}
