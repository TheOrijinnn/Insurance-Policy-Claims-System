# Quyết Định Kiến Trúc & Thiết Kế (Architectural Decisions)

## Các quyết định chốt thiết kế

### 1. [GIẢ ĐỊNH] Kiểm tra loss hợp lệ trong Claim
- **Nội dung**: `loss <= 0` sẽ ném `IllegalArgumentException` ngay trong constructor của `Claim`.
- **Lý do**: Cách này tránh payout âm lọt vào `Policy` và đảm bảo tiền tổn thất luôn dương trước khi thực hiện quy trình duyệt bồi thường (`approvePayout`).

### 2. [GIẢ ĐỊNH] Liên kết Policy ↔ Policyholder
- **Nội dung**: `Policy` không giữ tham chiếu tới `Policyholder`; thay vào đó, `Policyholder` giữ danh sách các `Policy`.
- **Lý do**: `ClaimRegistry.byHolder(h)` sẽ lọc các claim có policy nằm trong `h.policies()`. Bằng cách này, `Policy` không cần biết ai sở hữu nó, giảm phụ thuộc vòng và tách biệt trách nhiệm giữa thực thể hợp đồng và chủ hợp đồng.
