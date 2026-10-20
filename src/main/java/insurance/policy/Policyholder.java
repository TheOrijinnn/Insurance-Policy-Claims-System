package insurance.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Đại diện cho chủ hợp đồng bảo hiểm cùng danh sách các hợp đồng mà họ sở hữu.
 */
public final class Policyholder {
    private final String id;
    private final String name;
    private final List<Policy> policies = new ArrayList<>();

    public Policyholder(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Policyholder id cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Policyholder name cannot be null or blank");
        }
        this.id = id;
        this.name = name;
    }

    public void addPolicy(Policy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("Policy cannot be null");
        }
        if (policies.contains(policy)) {
            throw new IllegalArgumentException("Duplicate policy instance is not allowed: " + policy.id());
        }
        policies.add(policy);
    }

    public List<Policy> policies() {
        return List.copyOf(policies);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }
}
