# Hướng dẫn demo property và shrinking

## Chuẩn bị

Mở thư mục `java` bằng IntelliJ hoặc terminal, dùng JDK 17 trở lên và Maven với các dependency của `pom.xml` đã cài. Chạy `mvn test` để kiểm tra môi trường; bốn property có `@Disabled` sẽ bị bỏ qua. Chạy riêng bộ của Quân bằng `mvn -Dtest=CheckoutProperties,OfferProperties test`. Nếu Maven báo thiếu dependency hoặc không kết nối được kho, chuẩn bị mạng hoặc cache dependency trước buổi demo. Một lượt chạy bằng JUnit Platform trực tiếp đã cho 9 property thành công và 4 bị bỏ qua; đây không thay thế kết quả `mvn test` của toàn bộ dự án.

## Giới thiệu dữ liệu và property

1. Mở `SupermarketGenerators.java`, giới thiệu `Line`, `OfferSpec`, giỏ hàng, dải giá và lượng, cùng trường hợp không có offer.
2. Mở `OfferProperties.java`, giải thích bất biến tổng không âm, tính giao hoán khi đảo thứ tự và quan hệ thêm x hai lần so với thêm 2x. `checkout` trong file này dùng lượng gấp đôi giá trị của mỗi `Line` cho cả hai nhánh so sánh; khi dùng `single`, nó chia đôi lượng trước để hóa đơn nhận đúng lượng gốc.
3. Chỉ vào thống kê `unit` và `offer` trong kết quả jqwik. Kiểm tra EACH, KILO, bốn offer và NONE có xuất hiện trong lượt chạy; thống kê này không phải chỉ số phủ nhánh.

## Demo một phản ví dụ

Chỉ thực hiện trên bản sao làm việc hoặc trước khi nhóm đóng băng test. Không thay đổi bộ test sau khi nhóm đã đóng băng.

1. Trong `OfferProperties.java`, tạm bỏ `@Disabled` ngay trên `threeForTwoMatchesFormula` và đổi `@Property` thành `@Property(seed = "1707090295488917934")`.
2. Từ thư mục `java`, chạy `mvn "-Dtest=OfferProperties#threeForTwoMatchesFormula" test`. Property dự kiến thất bại; màn hình jqwik hiển thị seed, mẫu ban đầu và mẫu đã rút gọn. Bộ nhớ mẫu thất bại của jqwik hoặc thay đổi phiên bản/mã có thể ảnh hưởng thứ tự tái hiện.
3. Đối chiếu kết quả đã ghi: mẫu ban đầu KILO giá 1.17, lượng 4.4; mẫu sau hai bước shrinking là giá 0.01, lượng 3.1. Công thức giả định giảm đúng 1 kg dự kiến 0.021; kết quả là 0.02. Giải thích rằng shrinking đơn giản hóa đầu vào vẫn gây thất bại để dễ tìm nguyên nhân, không sửa lỗi tự động.
4. Khôi phục `@Disabled` và `@Property` ban đầu, rồi chạy lại bộ test. Không kết luận đây là lỗi nghiệp vụ cho đến khi nhóm xác nhận quy tắc combo với hàng KILO.

## Bàn giao cho Khanh và Đạt

Gửi Khanh `property-findings.md` để xác minh bốn nghi vấn cùng seed, dữ liệu và giả định nghiệp vụ; sau đó ghi kết quả xác minh vào công cụ theo dõi nhóm. Nhờ Đạt review `SupermarketGenerators.java`, `CheckoutProperties.java` và `OfferProperties.java`, tập trung vào phạm vi dữ liệu, công thức oracle và mức độ chặt của property. Các bước gửi, review và xác nhận cần thành viên nhóm thực hiện; tài liệu này không khẳng định chúng đã diễn ra.
