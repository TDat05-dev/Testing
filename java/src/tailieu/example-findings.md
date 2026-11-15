# Kết quả kiểm thử Example-Based

Chạy ngày 15/11/2026 trên mã gốc (nhánh `main`, chưa có nhánh lỗi gài) bằng JUnit 5 trong IntelliJ và `mvn test`. Bộ gồm 34 ca kiểm thử trong package `dojo.supermarket.example`, thiết kế bằng Phân vùng tương đương (PVTĐ), Phân tích giá trị biên (PTGTB) và Bảng quyết định. Đặc tả chi tiết từng ca nằm trong `Dac_ta_test_case_Example.xlsx`. Kết quả mong đợi được tính tay theo đặc tả nghiệp vụ, không lấy từ kết quả chạy của mã. Tiền so sánh với sai số ±0.001.

## Tổng hợp

| Chức năng | Số ca | Đạt | Không đạt |
|---|---|---|---|
| THREE_FOR_TWO | 10 | 9 | 1 |
| TEN_PERCENT_DISCOUNT | 7 | 7 | 0 |
| TWO_FOR_AMOUNT | 6 | 5 | 1 |
| FIVE_FOR_AMOUNT | 6 | 6 | 0 |
| Giỏ hàng (EDGE) | 5 | 4 | 1 |
| **Tổng** | **34** | **31** | **3** |

Ba ca không đạt được tạm `@Disabled` kèm mã Jira để `mvn test` của nhóm vẫn xanh. Kết quả `mvn test` sau khi tắt: các ca Example chạy 31 đạt, 3 bỏ qua (Skipped). Có thể bỏ `@Disabled` để chạy lại; khi chạy đích danh một method đã `@Disabled` trong IntelliJ, IntelliJ vẫn chạy method đó.

## Các ca không đạt

| Mã ca | Kỹ thuật chọn ca | Dữ liệu | Kỳ vọng | Thực tế | Jira |
|---|---|---|---|---|---|
| THREE_FOR_TWO_10 | PVTĐ: KILO số lượng lẻ; Bảng quyết định R4 | KILO, giá 1.00, số lượng 3.5 | Tổng 2.50 (tặng 1 kg) | Tổng 2.00, giảm 1.50 (tặng 1.5 kg) | KAN-43 |
| TWO_FOR_AMOUNT_06 | PVTĐ: giá combo > 2 × đơn giá (lớp không hợp lệ) | EACH, giá 1.00, combo 2 giá 5.00, số lượng 2 | Tổng ≤ 2.00 | Tổng 5.00 | KAN-44 |
| EDGE_03 | PVTĐ: số lượng < 0 (lớp không hợp lệ) | EACH, giá 1.00, số lượng −3 | Tổng ≥ 0 hoặc từ chối dữ liệu | Tổng −3.00 | KAN-45 |

**Nguyên nhân dự đoán (theo đọc mã `ShoppingCart.handleOffers` và `addItemQuantity`):**

- KAN-43: số lần được khuyến mãi tính theo phần nguyên `(int) quantity`, nhưng tiền gốc để trừ lại tính theo `quantity` thực, nên phần lẻ 0.5 kg cũng được giảm.
- KAN-44: khi giá combo lớn hơn giá mua lẻ, khoản "giảm giá" thành số dương và được cộng vào hóa đơn; mã không chọn mức giá có lợi hơn cho khách.
- KAN-45: `addItemQuantity` không kiểm tra số lượng, chấp nhận số âm và số 0.

Ba nguyên nhân trên là suy luận từ mã, cần TV4 xác minh. KAN-43 và KAN-44 còn phụ thuộc vào cách nhóm thống nhất quy tắc nghiệp vụ (khuyến mãi với hàng bán theo kg; có cho phép cấu hình combo đắt hơn giá lẻ hay không).

## Đối chiếu với kết quả Property-Based (property-findings.md)

| Lỗi | Example-Based | Property-Based |
|---|---|---|
| THREE_FOR_TWO với KILO số lượng lẻ | Phát hiện (THREE_FOR_TWO_10) | Phát hiện (threeForTwoMatchesFormula) |
| Khuyến mãi combo làm tăng giá | Phát hiện (TWO_FOR_AMOUNT_06) | Phát hiện (offerNeverRaisesTotal) |
| TWO_FOR_AMOUNT / FIVE_FOR_AMOUNT với KILO số lượng lẻ | Không phát hiện – không có ca nào kết hợp combo với hàng KILO | Phát hiện |
| Số lượng âm | Phát hiện (EDGE_03) | Không phát hiện – generator chỉ sinh số lượng dương |

Bộ Example bỏ sót tổ hợp "combo + hàng KILO" vì khi thiết kế, hàng KILO chỉ được xét cho THREE_FOR_TWO và TEN_PERCENT_DISCOUNT. Ngược lại, bộ Property chỉ sinh dữ liệu hợp lệ nên không chạm tới lớp không hợp lệ "số lượng âm". Hai phương pháp bổ trợ nhau; đây là nhận xét trên lượt chạy hiện tại, chưa phải kết luận cuối cùng của đồ án.

## Mutation testing (PIT)

Chưa chạy riêng cho bộ Example. Lệnh chạy (trong thư mục `java`):

```powershell
mvn test-compile org.pitest:pitest-maven:mutationCoverage "-DtargetTests=dojo.supermarket.example.*"
```

Số liệu nền trước khi có bộ Example (chỉ test gốc của kata): 56 đột biến, diệt 8 (14%), 44 đột biến không được test nào chạy qua. Điền kết quả của bộ Example vào đây sau khi chạy:

| Chỉ số | Giá trị |
|---|---|
| Line coverage | … |
| Mutation score (Killed) | … |
| No coverage | … |
| Test strength | … |
