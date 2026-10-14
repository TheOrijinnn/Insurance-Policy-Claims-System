# Quyết Định Kiến Trúc & Thiết Kế (Architectural Decisions)

## Các quyết định chốt thiết kế

### 1. [GIẢ ĐỊNH] Kiểm tra loss hợp lệ trong Claim
- **Nội dung**: `loss <= 0` (hoặc `null`) sẽ ném `IllegalArgumentException` ngay trong constructor của `Claim`.
- **Lý do**: Cách này tránh payout âm lọt vào `Policy` và đảm bảo tiền tổn thất luôn dương trước khi thực hiện quy trình duyệt bồi thường (`approvePayout`).

### 2. [GIẢ ĐỊNH] Liên kết Policy ↔ Policyholder
- **Nội dung**: `Policy` không giữ tham chiếu tới `Policyholder`; thay vào đó, `Policyholder` giữ danh sách các `Policy`.
- **Lý do**: `ClaimRegistry.byHolder(h)` sẽ lọc các claim có policy nằm trong `h.policies()`. Bằng cách này, `Policy` không cần biết ai sở hữu nó, giảm phụ thuộc vòng và tách biệt trách nhiệm giữa thực thể hợp đồng và chủ hợp đồng.

### 3. [QUYẾT ĐỊNH] Sử dụng `Optional<Money>` cho `approvedPayout` trong `Claim`
- **Nội dung**: `claim.approvedPayout()` trả về kiểu `Optional<Money>`, `empty` khi claim chưa được duyệt (`Approved`/`Paid`), và chứa giá trị số tiền bồi thường sau khi `approve()` thành công.
- **Lý do**: Phục vụ cho `StatementGenerator` hiển thị báo cáo rõ ràng, tránh trả về `null` và phòng ngừa lỗi `NullPointerException`.

---

## [TRACE NGHIỆP VỤ] Trace chi tiết hành động `pay()` khi Claim ở trạng thái `Submitted` (T2)

Luồng thực thi khi gọi `claim.pay()` lúc mới nộp yêu cầu:
1. **Lời gọi**: Caller gọi phương thức công khai `claim.pay()`.
2. **Ủy quyền (Delegation)**: `Claim.pay()` chuyển tiếp xử lý cho đối tượng trạng thái hiện tại: `state.pay(this)`.
3. **Đối tượng xử lý**: Lúc này `state` đang là `SubmittedState.INSTANCE`.
4. **Hành vi kế thừa**: `SubmittedState` không override hàm `pay(Claim c)` mà sử dụng hành vi mặc định `default` định nghĩa tại interface `ClaimState`.
5. **Ném ngoại lệ**:
   - `ClaimState.pay(Claim c)` gọi hàm trợ giúp `private illegal("pay")`.
   - `illegal("pay")` khởi tạo và ném:
     ```java
     throw new InvalidClaimTransitionException(name(), "pay");
     ```
   - Giá trị trả về của `name()` là `"Submitted"`.
6. **Kết quả**: Ngoại lệ `InvalidClaimTransitionException` được ném ra với:
   - `stateName() = "Submitted"`
   - `action() = "pay"`
7. **Bảo toàn trạng thái**: Trạng thái của `claim` không thay đổi, vẫn là `SubmittedState`.
