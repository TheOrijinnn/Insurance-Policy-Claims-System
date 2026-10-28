# ClaimFlow - Insurance Policy & Claims System

Hệ thống quản lý hợp đồng bảo hiểm (Auto, Home, Health) và xử lý yêu cầu bồi thường: tính phí và tiền bồi thường đa hình theo từng loại hợp đồng, quy trình claim `Submitted → UnderReview → Approved/Rejected → Paid`, chặn vượt hạn mức bồi thường trong kỳ hiệu lực và in sao kê cho chủ hợp đồng.

## Yêu cầu môi trường

- JDK 17 trở lên (`java -version`)
- Maven 3.8 trở lên (`mvn -version`)

JUnit 5 và các plugin Maven được tải tự động ở lần build đầu tiên (cần Internet lần đầu).

## Build, test và chạy demo

```bash
git clone https://github.com/TheOrijinnn/Insurance-Policy-Claims-System.git
cd Insurance-Policy-Claims-System

mvn clean test              # biên dịch + chạy toàn bộ test
mvn -q compile exec:java    # chạy demo insurance.Main
```

Chạy demo không qua plugin exec:

```bash
mvn -q compile
java -cp target/classes insurance.Main
```

> Windows: nếu tiếng Việt hiển thị lỗi trong console, chạy `chcp 65001` trước rồi chạy lại lệnh.

## Demo `insurance.Main`

| Bước | Kịch bản | Thể hiện |
| --- | --- | --- |
| 1 | Phát hành Auto/Home/Health (và 1 hợp đồng Home năm 2025 đã hết hạn) qua `PolicyFactory` | Tạo theo tên loại, không `if`/`switch` |
| 2 | Cùng tổn thất 50.000.000, cùng một `ClaimsProcessor` → Auto 45.000.000 / Home 42.500.000 / Health 38.400.000, trạng thái `Paid` | Happy path end-to-end + đa hình |
| 3 | Claim bị từ chối, sau đó gọi `pay()` → `InvalidClaimTransitionException`, vẫn `Rejected` | `Claim` tự bảo vệ chuyển trạng thái |
| 4 | Auto nhận thêm 3 claim → 45.000.000, 10.000.000 (cắt trần), claim thứ 3 bị `Rejected` vì vượt hạn mức | `Policy` tự bảo vệ hạn mức |
| 5 | Tổn thất ngoài kỳ hiệu lực → `Rejected` (`PolicyNotActiveException`) | Kiểm tra kỳ hiệu lực |
| 6 | Sao kê: hợp đồng còn hiệu lực, hợp đồng không hiệu lực, lịch sử claim | Sao kê cho chủ hợp đồng |

## Cấu trúc mã nguồn

```
src/main/java/insurance/
├── Main.java       demo + composition root (đăng ký các loại Policy vào PolicyFactory)
├── common/         Money, PolicyPeriod, các exception nghiệp vụ
├── policy/         Policy (abstract) + AutoPolicy / HomePolicy / HealthPolicy,
│                   PolicyFactory, PolicyCreator, PolicyRequest, Policyholder
├── claim/          Claim, ClaimState + 5 trạng thái (State pattern), ClaimRegistry
└── engine/         ClaimsProcessor (xử lý claim), StatementGenerator (sao kê)
```

Thêm một loại hợp đồng mới: viết lớp con của `Policy` và đăng ký một dòng trong `Main.defaultPolicyFactory()`. `ClaimsProcessor`, `ClaimState` và `StatementGenerator` không cần sửa (xem T6).

## Kiểm thử

`mvn test` chạy toàn bộ test JUnit 5. Các kịch bản chính:

| Mã | Kịch bản | Test |
| --- | --- | --- |
| T1 | Happy path E2E qua `ClaimsProcessor`: Auto, tổn thất 50tr → chi trả 45tr, còn lại 55tr, `Paid` | `engine/ClaimsProcessorTest` |
| T2 | `pay()` khi đang `Submitted` → bị chặn, giữ nguyên trạng thái (cùng 15 ô sai khác của ma trận 5×4) | `claim/ClaimStateTest` |
| T3 | `approve()` lần 2 → bị chặn, không chi trả 2 lần | `claim/ClaimStateTest` |
| T4 | 4 claim trên 1 Auto: 45tr, 45tr, 10tr (cắt trần), lần 4 `CoverageLimitExceededException` | `claim/ClaimStateTest` |
| T5 | Đa hình: cùng tổn thất 50tr qua cùng `ClaimsProcessor` → 45tr / 42,5tr / 38,4tr | `policy/PolymorphismAndPeriodTest` |
| T6 | OCP: `TravelPolicy` chỉ có trong `src/test`, tạo qua `PolicyFactory`, xử lý qua `ClaimsProcessor` → 35tr | `policy/TravelPolicyOcpTest` |
| T7 | Tổn thất ngoài kỳ hiệu lực → `PolicyNotActiveException`, không đổi trạng thái/tiền | `policy/PolymorphismAndPeriodTest` |

Log chạy test lần cuối khi đóng băng code: [`docs/test-log.txt`](docs/test-log.txt).

## Tài liệu

- [`docs/decisions.md`](docs/decisions.md): các quyết định thiết kế, ma trận chuyển trạng thái, trace luồng `approve()`.
