package insurance.policy;

import insurance.common.Money;
import insurance.common.PolicyPeriod;

/**
 * Dữ liệu đầu vào bất biến dùng để khởi tạo một hợp đồng bảo hiểm mới.
 */
public record PolicyRequest(String id, PolicyPeriod period, Money coverageLimit) {
}
