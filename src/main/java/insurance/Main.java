package insurance;

import insurance.policy.AutoPolicy;
import insurance.policy.HealthPolicy;
import insurance.policy.HomePolicy;
import insurance.policy.PolicyFactory;

public class Main {
    public static void main(String[] args) {
        PolicyFactory factory = defaultPolicyFactory();
        System.out.println("ClaimFlow initialized with " + factory.getClass().getSimpleName());
    }

    public static PolicyFactory defaultPolicyFactory() {
        PolicyFactory factory = new PolicyFactory();
        factory.register("AUTO", r -> new AutoPolicy(r.id(), r.period(), r.coverageLimit()))
               .register("HOME", r -> new HomePolicy(r.id(), r.period(), r.coverageLimit()))
               .register("HEALTH", r -> new HealthPolicy(r.id(), r.period(), r.coverageLimit()));
        return factory;
    }
}
