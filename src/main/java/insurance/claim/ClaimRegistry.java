package insurance.claim;

import insurance.policy.Policy;
import insurance.policy.Policyholder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lưu trữ danh sách các yêu cầu bồi thường theo thứ tự ghi nhận và hỗ trợ truy vấn theo chủ hợp đồng.
 */
public final class ClaimRegistry {
    private final List<Claim> claims = new ArrayList<>();

    public void add(Claim claim) {
        Objects.requireNonNull(claim, "Claim cannot be null");
        if (claims.contains(claim)) {
            throw new IllegalArgumentException("Duplicate claim instance is not allowed: " + claim.id());
        }
        claims.add(claim);
    }

    public List<Claim> all() {
        return List.copyOf(claims);
    }

    public List<Claim> byHolder(Policyholder h) {
        Objects.requireNonNull(h, "Policyholder cannot be null");
        List<Policy> holderPolicies = h.policies();
        return claims.stream()
                .filter(c -> holderPolicies.contains(c.policy()))
                .toList();
    }
}
