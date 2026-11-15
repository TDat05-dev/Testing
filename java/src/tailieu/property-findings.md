# Kết quả ban đầu của kiểm thử property

Chạy ngày 14/11/2026 trên mã hiện tại, chưa có nhánh lỗi gài. Đây là các nghi vấn để TV4 xác minh và thống nhất quy tắc nghiệp vụ, chưa phải kết luận về lỗi gài. Các property thất bại được tạm `@Disabled`; có thể bỏ annotation để chạy lại, hoặc thêm `seed` vào `@Property` để tái hiện. Giá và số lượng được tính bằng `double`, sai số so sánh là 1e-6.

| Property | Seed | Phản ví dụ sau shrinking | Kỳ vọng / thực tế |
|---|---|---|---|
| threeForTwoMatchesFormula | 1707090295488917934 | KILO, giá 0.01, số lượng 3.1 | 0.021 / 0.02 |
| twoForAmountMatchesFormula | -9037903569730832225 | KILO, giá 0.01, số lượng 2.1, giá combo 0.01 | 0.011 / 0.01 |
| fiveForAmountMatchesFormula | -7480986489718339011 | KILO, giá 0.01, số lượng 5.1, giá combo 0.01 | 0.011 / 0.01 |
| offerNeverRaisesTotal | 7732435664593712205 | EACH, giá 0.01, lượng đầu vào 1, combo 2 giá 0.03 | Hàm checkout thêm gấp đôi lượng đầu vào: giá thường 0.02, sau offer 0.03 |

Ba công thức combo giả định phần lẻ của hàng KILO được tính theo đơn giá; cần nhóm xác nhận khuyến mãi combo có thực sự áp dụng cho KILO. Property cuối giả định ưu đãi bất lợi không được làm tăng giá; giá combo sinh dương nhưng có thể cao hơn giá thường, nên cũng cần xác nhận quy tắc nghiệp vụ.

Ví dụ shrinking: THREE_FOR_TWO bắt đầu với KILO giá 1.17 và lượng 4.4, sau hai bước còn giá 0.01 và lượng 3.1. Một lượt thống kê gồm 3.594 dòng sinh ra cả EACH và KILO và cả bốn loại offer; đây là quan sát của lượt chạy, không phải bảo đảm phân bố cố định.
