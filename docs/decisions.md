# Quyết Định Kiến Trúc & Thiết Kế (Architectural Decisions)

## Mô tả một câu cho các lớp mới (Sun–Tue)
- **`Policyholder`**: Đại diện cho một khách hàng bảo hiểm và quản lý danh sách bất biến các `Policy` mà họ sở hữu (không cho phép trùng thực thể).
- **`PolicyFactory`**: Khởi tạo `Policy` theo tên loại đã chuẩn hóa thông qua bảng ánh xạ `Map<String, PolicyCreator>` mà không dùng `if`/`switch` theo loại.
- **`ClaimRegistry`**: Bộ sưu tập lưu trữ các `Claim` theo đúng thứ tự thêm vào và cung cấp truy vấn lọc theo `Policyholder`.

---

## Các quyết định chốt thiết kế & Giả định

### 1. [GIẢ ĐỊNH] Kiểm tra `loss > 0` ở cả `Claim` và `Policy.approvePayout`
- **Nội dung**:
  - `Claim` kiểm tra `loss != null && loss.isPositive()` ngay trong constructor để báo lỗi sớm (`IllegalArgumentException`).
  - `Policy.approvePayout(Money loss, LocalDate lossDate)` cũng tự kiểm tra `if (loss == null || !loss.isPositive()) throw new IllegalArgumentException(...)` ở dòng đầu tiên.
- **Lý do (chống bypass bất biến)**: `approvePayout` là phương thức `public`. Nếu ai đó gọi trực tiếp `homePolicy.approvePayout(Money.of("-10000000"), date)` mà không đi qua `Claim`, công thức `loss * 0.85` của `HomePolicy` sẽ sinh ra `raw = -8.500.000`, làm `totalPayout` bị giảm và phá vỡ bất biến coverage limit. Do đó `Policy` bắt buộc phải tự bảo vệ bất biến của chính nó.

### 2. [GIẢ ĐỊNH] Từ chối đăng ký trùng loại trong `PolicyFactory`
- **Nội dung**: `PolicyFactory.register(type, creator)` chuẩn hóa khóa bằng `trim().toUpperCase(Locale.ROOT)` và ném `IllegalArgumentException` nếu loại bảo hiểm đó đã được đăng ký trước đó.
- **Lý do**: Tránh việc vô tình ghi đè (silent overwrite) một `PolicyCreator` đã cấu hình trong composition root.

### 3. [GIẢ ĐỊNH] Liên kết `Policy` ↔ `Policyholder` và lọc `ClaimRegistry.byHolder`
- **Nội dung**: `Policy` không giữ tham chiếu ngược tới `Policyholder`; chỉ `Policyholder` giữ danh sách `List<Policy>`. `ClaimRegistry.byHolder(Policyholder h)` lọc các claim thỏa mãn `h.policies().contains(c.policy())`.
- **Lý do**: `Policy` là một thực thể định danh (không override `equals`/`hashCode`), nên `contains` so sánh theo danh tính đối tượng (object identity) chính xác từng hợp đồng đã gán cho chủ hợp đồng, đồng thời loại bỏ phụ thuộc hai chiều giữa `Policy` và `Policyholder`.

### 4. [QUYẾT ĐỊNH] Sử dụng `Optional<Money>` cho `approvedPayout` trong `Claim`
- **Nội dung**: `claim.approvedPayout()` trả về kiểu `Optional<Money>`, rỗng (`Optional.empty()`) khi claim chưa được duyệt (`Submitted`, `UnderReview`, `Rejected`), và chứa số tiền thực tế đã duyệt sau khi chuyển sang `Approved`/`Paid`.
- **Lý do**: Phục vụ cho `StatementGenerator` hiển thị báo cáo rõ ràng, tránh trả về `null` và phòng ngừa lỗi `NullPointerException`.

---

## Tự Review (Mốc Wed 21/10 - Không nhìn code)

### 1. Bảng ma trận chuyển trạng thái 5 × 4 của `Claim`

| Từ trạng thái \ Hành động | `startReview()` | `approve()` | `reject("lý do")` | `pay()` |
| :--- | :---: | :---: | :---: | :---: |
| **`Submitted`** | ✓ → `UnderReview` | ✗ | ✗ | ✗ **(T2)** |
| **`UnderReview`** | ✗ | ✓ → `Approved` | ✓ → `Rejected` | ✗ |
| **`Approved`** | ✗ | ✗ **(T3)** | ✗ | ✓ → `Paid` |
| **`Rejected`** | ✗ | ✗ | ✗ | ✗ |
| **`Paid`** | ✗ | ✗ | ✗ | ✗ |

*(16 ô ✗ đều ném `InvalidClaimTransitionException(stateName, action)` và giữ nguyên trạng thái cũng như `totalPayout`).*

### 2. Trace luồng `claim.approve()` từ `UnderReviewState` đến `Policy.approvePayout`
1. Caller gọi `claim.approve()`, ủy quyền cho `state.approve(this)` (đang là `UnderReviewState.INSTANCE`).
2. `UnderReviewState.approve(c)` gọi `c.policy().approvePayout(c.loss(), c.lossDate())`:
   - Kiểm tra `loss.isPositive()`, nếu không ném `IllegalArgumentException`.
   - Gọi `requireActiveOn(lossDate)`, nếu ngoài kỳ hạn hợp đồng ném `PolicyNotActiveException`.
   - Tính `raw = calculatePayout(loss)` theo công thức đa hình của từng dòng `Policy` (`AutoPolicy`, `HomePolicy`, `HealthPolicy`).
   - Tính hạn mức còn lại `remaining = coverageLimit.minus(totalPayout)`.
   - Nếu `raw.isPositive() && remaining.isZero()` thì ném `CoverageLimitExceededException(id, remaining)`.
   - Cắt trần theo hạn mức còn lại: `payout = raw.min(remaining)`.
   - Cộng dồn `totalPayout = totalPayout.plus(payout)` ở dòng cuối cùng và trả về `payout`.
3. Nhận `payout` thành công, `UnderReviewState` gọi tiếp `c.recordApproval(payout)` (package-private) để lưu `approvedPayout`.
4. Cuối cùng gọi `c.moveTo(ApprovedState.INSTANCE)` (package-private) để chuyển trạng thái sang `Approved`.

### 3. Giải thích vì sao cập nhật `totalPayout` phải là dòng cuối cùng và thứ tự trong `UnderReviewState.approve`
- **Trong `Policy.approvePayout`**: Phép gán `totalPayout = totalPayout.plus(payout)` được đặt ở dòng cuối cùng sau mọi bước kiểm tra (`loss > 0`, `requireActiveOn`, `CoverageLimitExceededException`) và tính toán (`calculatePayout`, `min`). Nếu bất kỳ ngoại lệ nào xảy ra ở phía trên, phương thức thoát ngay lập tức trước khi chạm tới dòng gán này, đảm bảo **tính nguyên tử (failure atomicity)** — `totalPayout` và `remainingLimit` hoàn toàn không bị biến đổi dở dang.
- **Trong `UnderReviewState.approve`**: Thứ tự bắt buộc là `approvePayout` → `recordApproval` → `moveTo`. Nếu `approvePayout` ném ngoại lệ (`PolicyNotActiveException` hoặc `CoverageLimitExceededException`), cả `approvedPayout` lẫn `state` của `Claim` đều chưa bị chạm tới (vẫn ở `UnderReview` và `approvedPayout` vẫn rỗng).

---

## [TRACE NGHIỆP VỤ] Trace chi tiết hành động `pay()` khi Claim ở trạng thái `Submitted` (T2)

1. **Lời gọi**: Caller gọi phương thức công khai `claim.pay()`.
2. **Ủy quyền (Delegation)**: `Claim.pay()` chuyển tiếp xử lý cho đối tượng trạng thái hiện tại: `state.pay(this)`.
3. **Đối tượng xử lý**: Lúc này `state` đang là `SubmittedState.INSTANCE`.
4. **Hành vi kế thừa**: `SubmittedState` không override hàm `pay(Claim c)` mà sử dụng hành vi mặc định `default` định nghĩa tại interface `ClaimState`.
5. **Ném ngoại lệ**: `ClaimState.pay(Claim c)` ném `new InvalidClaimTransitionException(name(), "pay")` với `stateName() = "Submitted"` và `action() = "pay"`.
6. **Bảo toàn trạng thái**: Trạng thái của `claim` không thay đổi, vẫn là `Submitted`.
