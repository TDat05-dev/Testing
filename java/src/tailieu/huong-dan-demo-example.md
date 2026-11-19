# Hướng dẫn demo bộ Example-Based

## Chuẩn bị

Mở thư mục `java` bằng IntelliJ (Open as Project), JDK 17 trở lên. Chạy `mvn test` trong thư mục `java` để kiểm tra môi trường; ba ca Example có `@Disabled` sẽ hiện Skipped. Mở sẵn file `Dac_ta_test_case_Example.xlsx` ở sheet "Test Cases" và sheet "Thiết kế". Nếu Maven báo thiếu dependency, chuẩn bị mạng hoặc cache dependency trước buổi demo.

## 1. Thiết kế ca kiểm thử (khoảng 2 phút)

1. Mở sheet "Thiết kế", phần THREE_FOR_TWO. Giải thích ba kỹ thuật của bài 5:
   - Phân vùng tương đương: số lượng < 3 / ≥ 3; EACH / KILO; có / không có khuyến mãi.
   - Phân tích giá trị biên: 2, 3, 4 quanh ngưỡng 3 và 5, 6, 7 quanh ngưỡng 6.
   - Bảng quyết định: 3 điều kiện cho ra 4 luật, mỗi luật ít nhất một ca.
2. Chuyển sang sheet "Test Cases", chỉ một dòng (ví dụ THREE_FOR_TWO_03) theo mẫu ca kiểm thử của bài 4: mã, mô tả, điều kiện tiên quyết, các bước, dữ liệu, kết quả mong đợi, truy vết.

## 2. Từ bảng đặc tả sang code (khoảng 2 phút)

1. Mở `ThreeForTwoTest.java`, chỉ khối `@CsvSource`: mỗi dòng là một ca trong Excel, cột đầu là mã ca kiểm thử.
2. Mở `CheckoutFixture.java`: điều kiện tiên quyết dùng chung (catalog, Teller, giỏ hàng rỗng) và hàm `checkout()` in ra kết quả thực tế.
3. Chuột phải vào package `example`, chọn Run 'Tests in example'. Trong cây kết quả, tên mỗi ca hiện đúng mã trong Excel (ví dụ `THREE_FOR_TWO_03: mua 3 cái giá 0.99 → ...`). Bấm vào một ca để thấy dòng "Kết quả thực tế".

## 3. Demo một ca phát hiện lỗi thật (khoảng 2 phút)

Chỉ thực hiện trên bản sao làm việc hoặc trước khi nhóm đóng băng test.

1. Trong `ThreeForTwoTest.java`, tạm comment dòng `@Disabled` phía trên `kiloProductFractionalQuantity`.
2. Chạy class `ThreeForTwoTest`. Ca THREE_FOR_TWO_10 thất bại: `Expected: 2.5`, `Actual: 2.0`; dòng kết quả thực tế cho thấy số tiền giảm là 1.50, tức hệ thống tặng 1.5 kg thay vì 1 kg.
3. Giải thích: ca này được chọn từ luật R4 của bảng quyết định (KILO với số lượng lẻ), cho thấy kỹ thuật hộp đen dẫn tới lỗi mà người viết test chọn ngẫu nhiên dễ bỏ sót.
4. Khôi phục `@Disabled` rồi chạy lại cả package.

## 4. Mutation testing (khoảng 1 phút)

1. Trong thư mục `java`, chạy:
   `mvn test-compile org.pitest:pitest-maven:mutationCoverage "-DtargetTests=dojo.supermarket.example.*"`
2. Mở `java/target/pit-reports/index.html`, so sánh mutation score của bộ Example với số liệu nền 14% (chỉ có test gốc).
3. Mỗi lần chạy PIT ghi đè báo cáo cũ; chụp màn hình trước khi chạy bộ khác.

## 5. Kết nối với phần Property-Based

Kết thúc bằng bảng đối chiếu trong `example-findings.md`: bộ Example phát hiện lỗi số lượng âm mà bộ Property không chạm tới, còn bộ Property phát hiện lỗi combo với hàng KILO mà bộ Example bỏ sót. Chuyển lời cho phần demo của Quân.

## Bàn giao

Gửi Khanh `example-findings.md` và file Excel để đưa số liệu vào báo cáo kết quả. Nhờ Quân review các file trong `example/`, tập trung vào việc chọn ca kiểm thử và kết quả mong đợi. Các bước gửi, review và xác nhận cần thành viên nhóm thực hiện; tài liệu này không khẳng định chúng đã diễn ra.
