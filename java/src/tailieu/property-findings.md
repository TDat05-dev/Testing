# Kết quả ban đầu của kiểm thử property

Chạy ngày 14/11/2026 trên mã hiện tại, chưa có nhánh lỗi gài. Bản quy tắc nghiệp vụ ngày 19/11/2026 xác nhận các counterexample dưới đây vi phạm QT4–QT6. Các property thất bại được giữ `@Disabled` theo quy ước nhóm để ghi nhận lỗi gốc; có thể thêm `seed` vào `@Property` trên bản làm việc riêng để tái hiện. Giá và số lượng được tính bằng `double`, sai số so sánh là 1e-6.

| Property | Quy tắc / issue | Seed | Phản ví dụ sau shrinking | Kỳ vọng / thực tế |
|---|---|---|---|---|
| threeForTwoMatchesFormula | QT4 / KAN-43 | 1707090295488917934 | KILO, giá 0.01, số lượng 3.1 | 0.021 / 0.02 |
| twoForAmountMatchesFormula | QT5 / KAN-52 | -9037903569730832225 | KILO, giá 0.01, số lượng 2.1, giá combo 0.01 | 0.011 / 0.01 |
| fiveForAmountMatchesFormula | QT5 / KAN-52 | -7480986489718339011 | KILO, giá 0.01, số lượng 5.1, giá combo 0.01 | 0.011 / 0.01 |
| offerNeverRaisesTotal | QT6 / KAN-44 | 7732435664593712205 | EACH, giá 0.01, lượng đầu vào 1, combo 2 giá 0.03 | Hàm checkout thêm gấp đôi lượng đầu vào: giá thường 0.02, sau offer 0.03 |

Theo bộ quy tắc nghiệp vụ của nhóm, cả ba trường hợp là lỗi thật: KILO lẻ vẫn được tính phần dư theo QT4/QT5; combo có giá từ bằng giá lẻ trở lên không được áp dụng theo QT6. `threeForTwoMatchesFormula` gắn KAN-43, `offerNeverRaisesTotal` gắn KAN-44, còn hai property combo gắn KAN-52.

Ví dụ shrinking: THREE_FOR_TWO bắt đầu với KILO giá 1.17 và lượng 4.4, sau hai bước còn giá 0.01 và lượng 3.1. Một lượt thống kê gồm 3.594 dòng sinh ra cả EACH và KILO và cả bốn loại offer; đây là quan sát của lượt chạy, không phải bảo đảm phân bố cố định.

## Trạng thái theo yêu cầu nhóm

Generator `cartsWithOffers()` vẫn cho phép giá combo tùy ý và các property công thức dùng `lines()`, gồm cả KILO lẻ. Không thu hẹp generator về EACH hay thay `offerNeverRaisesTotal` bằng property chỉ nhận offer có lợi. Bốn property được đánh dấu `@Disabled` để ghi nhận lỗi gốc theo quy ước nhóm; các mã issue lần lượt là KAN-43, KAN-44 và KAN-52.

Bảng trên là kết quả chạy lịch sử ngày 14/11/2026; chưa chạy lại sau lần chỉnh này, nên không dùng số đếm cũ làm kết quả xác minh hiện tại.

Ngày 19/11/2026, lần thử `mvn test` trong terminal bị hệ điều hành chặn với `Access is denied`; đây không phải kết quả test pass/fail. IntelliJ không có trên máy để chạy theo đúng IDE được yêu cầu. Cần chạy lại trong IntelliJ trước khi chốt kết quả.
