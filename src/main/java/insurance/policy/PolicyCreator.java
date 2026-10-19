package insurance.policy;

/**
 * Giao diện hàm định nghĩa cách tạo một đối tượng Policy từ PolicyRequest.
 */
@FunctionalInterface
public interface PolicyCreator {
    Policy create(PolicyRequest r);
}
