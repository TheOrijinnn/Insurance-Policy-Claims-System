package insurance.policy;

import insurance.Main;
import insurance.common.Money;
import insurance.common.PolicyPeriod;
import insurance.common.UnknownPolicyTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PolicyFactoryTest {

    private PolicyFactory factory;
    private PolicyRequest request;

    @BeforeEach
    void setUp() {
        factory = Main.defaultPolicyFactory();

        PolicyPeriod period2026 = new PolicyPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        request = new PolicyRequest("POL-100", period2026, Money.of("100000000"));
    }

    @Test
    @DisplayName("3 loại: create AUTO/HOME/HEALTH với limit 100tr -> typeName() Auto/Home/Health")
    void testCreateThreeStandardTypes() {
        Policy auto = factory.create("AUTO", request);
        Policy home = factory.create("HOME", request);
        Policy health = factory.create("HEALTH", request);

        assertEquals("Auto", auto.typeName());
        assertEquals("Home", home.typeName());
        assertEquals("Health", health.typeName());
    }

    @Test
    @DisplayName("Chữ thường: create('auto', r) -> ra Auto")
    void testCreateCaseInsensitiveAndTrimmed() {
        Policy auto = factory.create("  auto  ", request);
        assertEquals("Auto", auto.typeName());
    }

    @Test
    @DisplayName("Loại lạ: create('TRAVEL', r) khi chưa đăng ký -> UnknownPolicyTypeException, type() = 'TRAVEL'")
    void testCreateUnknownTypeThrows() {
        UnknownPolicyTypeException ex = assertThrows(UnknownPolicyTypeException.class,
                () -> factory.create("TRAVEL", request));
        assertEquals("TRAVEL", ex.type());
    }

    @Test
    @DisplayName("Đăng ký trùng: register('AUTO', ...) hai lần -> IllegalArgumentException")
    void testDuplicateRegistrationThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> factory.register("auto", r -> new AutoPolicy(r.id(), r.period(), r.coverageLimit())));
    }
}
