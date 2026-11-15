# Chương 5 Hệ thống thực nghiệm và bộ kiểm thử Example-Based

## 5.1 Hệ thống được kiểm thử

Đối tượng kiểm thử là bản Java của SupermarketReceipt-Refactoring-Kata (Emily Bache, giấy phép MIT), một hệ thống tính hóa đơn siêu thị. Nhóm chỉ giữ thư mục `java/` của kata và không sửa mã nghiệp vụ trong `src/main`; mọi kiểm thử gọi qua giao diện công khai của hệ thống.

Các lớp chính trong package `dojo.supermarket.model`:

| Lớp | Vai trò |
|---|---|
| `Product` | Sản phẩm, gồm tên và đơn vị bán (`ProductUnit`: EACH – theo cái, KILO – theo kg) |
| `SupermarketCatalog` | Giao diện tra cứu đơn giá; khi kiểm thử dùng `FakeCatalog` lưu giá theo tên sản phẩm |
| `ShoppingCart` | Giỏ hàng; `addItemQuantity` thêm sản phẩm và cộng dồn số lượng theo sản phẩm; `handleOffers` tính giảm giá |
| `Teller` | Thu ngân; giữ danh sách khuyến mãi (`addSpecialOffer`) và lập hóa đơn (`checksOutArticlesFrom`) |
| `Receipt` | Hóa đơn gồm các dòng hàng (`ReceiptItem`) và các dòng giảm giá (`Discount`); `getTotalPrice` = tổng tiền hàng + tổng giảm giá (giảm giá mang giá trị âm) |
| `SpecialOfferType` | Bốn loại khuyến mãi: THREE_FOR_TWO, TEN_PERCENT_DISCOUNT, TWO_FOR_AMOUNT, FIVE_FOR_AMOUNT |

Luồng thanh toán: tạo sản phẩm và đăng ký giá vào catalog → gắn khuyến mãi cho sản phẩm qua `Teller` → thêm sản phẩm vào `ShoppingCart` → `Teller.checksOutArticlesFrom` tạo một dòng hóa đơn cho mỗi lần thêm hàng, rồi gọi `handleOffers` để tính giảm giá trên tổng số lượng của từng sản phẩm.

Ý nghĩa tham số khuyến mãi (`argument`): TEN_PERCENT_DISCOUNT là phần trăm giảm; TWO_FOR_AMOUNT và FIVE_FOR_AMOUNT là giá trọn gói cho 2 và 5 sản phẩm; THREE_FOR_TWO không dùng tham số.

## 5.2 Môi trường và công cụ

| Thành phần | Công cụ |
|---|---|
| Ngôn ngữ, build | Java 17+ (nhóm dùng Temurin 21), Maven |
| Example-Based Testing | JUnit 5.11 (`@Test`, `@ParameterizedTest`, `@CsvSource`) |
| Property-Based Testing | jqwik 1.9.2 |
| Mutation testing | PIT 1.17.0 kèm pitest-junit5-plugin |
| Quản lý mã nguồn, công việc | Git/GitHub (mỗi thành viên một nhánh, merge qua Pull Request), Jira |

Surefire được cấu hình chạy các lớp có tên kết thúc bằng `Test`, `Tests` hoặc `Properties`. PIT chỉ đột biến mã nghiệp vụ (`dojo.supermarket.model.*`); bộ test cần chạy chọn bằng tham số `targetTests`, nhờ đó đo riêng được bộ Example và bộ Property.

## 5.3 Thiết kế bộ Example-Based

Bộ Example gồm 34 ca kiểm thử, thiết kế bằng các kỹ thuật hộp đen của bài 5; mỗi ca được ghi theo mẫu ca kiểm thử của bài 4 trong `Dac_ta_test_case_Example.xlsx`. Kết quả mong đợi được tính tay theo đặc tả nghiệp vụ.

**Phân vùng tương đương.** Các điều kiện được chia thành lớp hợp lệ và không hợp lệ: số lượng so với ngưỡng khuyến mãi; đơn vị EACH / KILO; sản phẩm có / không có khuyến mãi; giá combo thấp hơn / cao hơn giá mua lẻ; số lượng dương / bằng 0 / âm. Mỗi lớp có ít nhất một ca đại diện.

**Phân tích giá trị biên.** Lỗi thường nằm ở ngưỡng, nên mỗi ngưỡng được thử ở giá trị biên − 1, biên và biên + 1:

| Khuyến mãi | Ngưỡng | Giá trị thử |
|---|---|---|
| THREE_FOR_TWO | 3 và 6 | 2, 3, 4 và 5, 6, 7 |
| TWO_FOR_AMOUNT | 2 | 1, 2, 3 |
| FIVE_FOR_AMOUNT | 5 và 10 | 4, 5, 6 và 10, 11 |
| TEN_PERCENT_DISCOUNT | 0% và 100% | 0, 100 |
| Giỏ hàng | số dòng = 0, số lượng = 0 | giỏ rỗng, số lượng 0 |

**Bảng quyết định cho THREE_FOR_TWO.**

| | R1 | R2 | R3 | R4 |
|---|---|---|---|---|
| C1: Sản phẩm có khuyến mãi THREE_FOR_TWO? | N | Y | Y | Y |
| C2: Số lượng ≥ 3? | – | N | Y | Y |
| C3: Hàng KILO với số lượng lẻ? | – | – | N | Y |
| A1: Không giảm giá | X | X | | |
| A2: Giảm floor(số lượng / 3) × đơn giá | | | X | X |
| Ca kiểm thử | 08 | 01, 02 | 03–07, 09 | 10 |

**Chuyển đổi trạng thái.** Giỏ hàng đã có một sản phẩm, thêm tiếp cùng sản phẩm thì số lượng phải được cộng dồn trước khi tính khuyến mãi (EDGE_04: thêm 2 rồi 1 với THREE_FOR_TWO → được tặng 1).

Phân bố ca theo chức năng: THREE_FOR_TWO 10, TEN_PERCENT_DISCOUNT 7, TWO_FOR_AMOUNT 6, FIVE_FOR_AMOUNT 6, giỏ hàng 5.

## 5.4 Cấu trúc mã nguồn kiểm thử

```
java/src/test/java/dojo/supermarket/
├── model/       FakeCatalog (dùng chung), SupermarketTest (test gốc của kata)
├── example/     Bộ Example-Based (TV2)
├── generators/  Generator jqwik dùng chung (TV3)
├── property/    Bộ Property-Based (TV3)
└── stateful/    Stateful / model-based test (TV4)
```

Trong package `example`, lớp trừu tượng `CheckoutFixture` gom điều kiện tiên quyết dùng chung (catalog, `Teller`, giỏ hàng rỗng) và hàm `checkout()` in ra kết quả thực tế của mỗi ca. JUnit tạo đối tượng mới cho mỗi ca nên các ca độc lập với nhau. Các ca chỉ khác dữ liệu (chuỗi giá trị biên) được viết bằng `@ParameterizedTest` với `@CsvSource`, mỗi dòng CSV là một ca trong Excel; các ca có các bước khác biệt viết bằng `@Test`. Mã ca kiểm thử xuất hiện trong tên hiển thị khi chạy (`@DisplayName` hoặc tham số `name`), giúp truy vết hai chiều giữa Excel và code.

Ba ca phát hiện lỗi thật của mã gốc (THREE_FOR_TWO_10, TWO_FOR_AMOUNT_06, EDGE_03) được giữ lại trong bộ test và tạm `@Disabled` kèm mã Jira; kết quả chi tiết ở `example-findings.md`.

## 5.5 Giới hạn

Bộ Example chỉ kiểm tra các giá trị được chọn khi thiết kế. Khi thiết kế, hàng KILO chỉ được xét cho THREE_FOR_TWO và TEN_PERCENT_DISCOUNT, nên tổ hợp combo với hàng KILO không được kiểm tra; bộ Property-Based đã phát hiện lỗi ở chính tổ hợp này. Kết quả mong đợi phụ thuộc vào cách nhóm hiểu quy tắc nghiệp vụ ở những chỗ đặc tả chưa rõ (khuyến mãi với hàng bán theo kg, combo đắt hơn giá lẻ); khi nhóm thống nhất lại quy tắc, các ca liên quan cần được xem lại.
