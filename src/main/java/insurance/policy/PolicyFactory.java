package insurance.policy;

import insurance.common.UnknownPolicyTypeException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Khởi tạo hợp đồng bảo hiểm theo loại thông qua bảng ánh xạ creator đã đăng ký, không rẽ nhánh theo loại.
 */
public final class PolicyFactory {
    private final Map<String, PolicyCreator> creators = new HashMap<>();

    public PolicyFactory register(String type, PolicyCreator creator) {
        String key = normalizeKey(type);
        Objects.requireNonNull(creator, "PolicyCreator cannot be null");
        if (creators.putIfAbsent(key, creator) != null) {
            throw new IllegalArgumentException("Duplicate policy type registration: " + key);
        }
        return this;
    }

    public Policy create(String type, PolicyRequest r) {
        String key = normalizeKey(type);
        Objects.requireNonNull(r, "PolicyRequest cannot be null");
        PolicyCreator creator = creators.get(key);
        if (creator == null) {
            throw new UnknownPolicyTypeException(key);
        }
        return creator.create(r);
    }

    private static String normalizeKey(String type) {
        return Objects.requireNonNull(type, "Policy type cannot be null")
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}
