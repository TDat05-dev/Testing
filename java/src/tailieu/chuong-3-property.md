# Chương 3 Các mẫu property trong hệ thống thanh toán siêu thị

## 3.1 Mục tiêu và phạm vi

Property-Based Testing (PBT) kiểm tra một quy tắc tổng quát trên nhiều bộ dữ liệu được sinh tự động. Trong đồ án, jqwik sinh giỏ hàng, đơn giá, số lượng và khuyến mãi; mỗi property xác nhận một quan hệ phải đúng đối với dữ liệu thuộc phạm vi của nó. Khi property thất bại, jqwik ghi seed và tìm cách rút gọn dữ liệu để tạo phản ví dụ dễ phân tích hơn. Một lượt chạy thành công không chứng minh hệ thống đúng với mọi đầu vào.

Đối tượng kiểm thử là luồng `ShoppingCart` → `Teller` → `Receipt`. Các kiểm thử sử dụng `FakeCatalog` và gọi giao diện công khai của hệ thống; mã nghiệp vụ không bị sửa. So sánh số thực dùng sai số 10⁻⁶. Công thức khuyến mãi và giả định về hàng KILO cần được nhóm xác nhận theo đặc tả, đặc biệt với những property đang thất bại.

## 3.2 Dữ liệu sinh và cách dựng hóa đơn

`SupermarketGenerators` định nghĩa `Line` gồm đơn vị bán, đơn giá và số lượng. Giá nằm trong 0.01–100 với hai chữ số thập phân; hàng EACH có số lượng nguyên 1–20, còn KILO có số lượng 0.1–10 với một chữ số thập phân. Giỏ có từ 0 đến 10 dòng; mỗi dòng được đặt tên duy nhất để `FakeCatalog` không ghi đè giá. `OfferSpec` biểu diễn bốn loại ưu đãi: THREE_FOR_TWO, TEN_PERCENT_DISCOUNT với tỷ lệ 1–100, TWO_FOR_AMOUNT và FIVE_FOR_AMOUNT với giá combo dương. Mỗi dòng có thể không có offer.

Giá combo không bị giới hạn phải thấp hơn giá thông thường. Đây là lựa chọn có chủ đích để kiểm tra giả định rằng khuyến mãi không làm tăng hóa đơn; nếu nghiệp vụ yêu cầu cấm cấu hình đó, nhóm cần đổi tiền đề kiểm thử sau khi xác nhận. Generator hiện chủ yếu sinh đầu vào hợp lệ; nó không chứng minh cách hệ thống xử lý số lượng âm hoặc dữ liệu không hợp lệ.

`Statistics.collect` ghi phân bố đơn vị và offer. Ví dụ một lượt chạy thu được 3.612 dòng, có cả EACH, KILO, bốn loại offer và trường hợp không offer. Kết quả này mô tả dữ liệu thực tế của lượt chạy, không bảo đảm các phân vùng luôn được bao phủ và không phải độ bao phủ câu lệnh hay nhánh.

## 3.3 Invariant Bất biến

Invariant là điều kiện phải đúng đối với mọi trạng thái hoặc kết quả trong phạm vi tiền đề. Hai invariant trong `OfferProperties` yêu cầu tổng hóa đơn không âm và tổng giảm giá không lớn hơn tổng tiền hàng trước giảm:

```java
assertTrue(checkout(cart, true, false, false).getTotalPrice() >= -EPS);
double discount = -receipt.getDiscounts().stream()
        .mapToDouble(Discount::discountAmount).sum();
assertTrue(discount <= gross(cart) + EPS);
```

Chúng có thể phát hiện trường hợp tính giảm giá quá mức hoặc kết quả âm. Vì property thứ hai kiểm tra tổng giảm giá ròng, một khoản giảm bất thường có thể bị khoản tăng giá khác bù trừ; property không bảo đảm từng dòng giảm giá đều hợp lệ.

## 3.4 Commutativity Tính giao hoán

Thay đổi thứ tự thêm các sản phẩm khác nhau không được làm thay đổi tổng tiền, nếu các sản phẩm, số lượng, giá và offer giữ nguyên. `orderDoesNotChangeTotal` dựng một giỏ theo thứ tự ban đầu và một giỏ theo thứ tự đảo ngược:

```java
assertEquals(checkout(cart, true, false, false).getTotalPrice(),
             checkout(cart, true, true, false).getTotalPrice(), EPS);
```

Property có thể phát hiện việc tính tiền phụ thuộc thứ tự thêm hàng. Ở đây chỉ so sánh một thứ tự với thứ tự đảo ngược, không phải mọi hoán vị và không so sánh thứ tự hiển thị trên hóa đơn.

## 3.5 Metamorphic Quan hệ biến đổi

Kiểm thử metamorphic so sánh kết quả giữa các đầu vào có quan hệ đã biết, hữu ích khi không cần tính toàn bộ hóa đơn bằng tay. `addingTwiceMatchesAddingDouble` so sánh việc thêm lượng x hai lần với thêm 2x một lần cho từng sản phẩm:

```java
assertEquals(checkout(cart, true, false, false).getTotalPrice(),
             checkout(cart, true, false, true).getTotalPrice(), EPS);
```

Kiểm thử có thể bắt lỗi cộng dồn số lượng hoặc áp offer khác nhau giữa hai cách thêm. `offerNeverRaisesTotal` so sánh cùng giỏ khi có và không có offer. Property thứ hai hiện bị tạm tắt vì combo giá 0.03 cho hai sản phẩm giá 0.01 làm hóa đơn tăng từ 0.02 lên 0.03. Đây là nghi vấn phụ thuộc vào quy tắc kinh doanh, không tự động chứng minh lỗi khi đặc tả chưa xác nhận.

## 3.6 Oracle Công thức đối chiếu

Oracle tính kết quả mong đợi độc lập rồi so sánh với hệ thống. `CheckoutProperties` tính tổng không offer bằng tổng của đơn giá nhân số lượng. Với phần trăm giảm p, số lượng q và đơn giá u, `percentageMatchesFormula` kiểm tra:

```java
double expected = line.quantity() * line.unitPrice() * (1 - percent / 100.0);
assertEquals(expected, single(line, offer).getTotalPrice(), EPS);
```

Với THREE_FOR_TWO, số lượng miễn phí được giả định là `floor(q/3)`, do đó tổng là `(q - floor(q/3)) × u`. Với combo n sản phẩm giá a, tổng dự kiến là `floor(q/n) × a + (q - n × floor(q/n)) × u`, với n bằng 2 hoặc 5. Các công thức này có thể bắt sai số lượng miễn phí, sai giá combo hoặc bỏ sót phần dư. `wholeItemComboFormulas` hiện kiểm tra cả ba công thức trên hàng EACH.

Ba property kiểm tra công thức combo trên cả EACH và KILO đang tạm tắt vì phản ví dụ KILO có phần thập phân cho kết quả khác công thức kỳ vọng. Ví dụ THREE_FOR_TWO với 3.1 kg, giá 0.01: oracle dự kiến 0.021, thực tế 0.02. Đó là nghi vấn cho thấy phần lẻ bị bỏ qua trong phép tính; cần xác nhận offer có áp dụng cho KILO và quy định làm tròn trước khi kết luận.

## 3.7 Shrinking và tái hiện

Khi `threeForTwoMatchesFormula` thất bại trong lượt chạy ban đầu, jqwik rút dữ liệu từ KILO giá 1.17, số lượng 4.4 xuống giá 0.01, số lượng 3.1 sau hai bước. Mẫu nhỏ hơn giúp thấy 0.1 kg còn dư là yếu tố liên quan; shrinking không bảo đảm tìm phản ví dụ nhỏ nhất theo mọi cách đo.

Seed và bốn phản ví dụ được ghi trong `property-findings.md`; hướng dẫn thực hiện demo nằm trong `huong-dan-demo-property.md`. Kết quả chạy hiện tại có 9 kiểm thử/property hoạt động thành công và 4 property bị bỏ qua, bao gồm cả hai property không offer. Các con số này chỉ phản ánh lượt chạy đã ghi nhận, không phải kết quả mutation testing hay số lỗi được nhóm xác nhận.
