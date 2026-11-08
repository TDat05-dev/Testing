# Đồ án Kiểm thử phần mềm: Property-Based Testing

Nghiên cứu và ứng dụng **Property-Based Testing (PBT)** bằng **jqwik**, so sánh với **Example-Based Testing (JUnit 5)** thông qua **mutation testing (PIT)**.

Hệ thống thực nghiệm: bản Java của [SupermarketReceipt-Refactoring-Kata](https://github.com/emilybache/SupermarketReceipt-Refactoring-Kata) (Emily Bache, giấy phép MIT – xem `license.txt`). Đây là hệ thống tính hóa đơn siêu thị gồm giỏ hàng, bảng giá và 4 loại khuyến mãi.

- **Jira:** https://doankiemthu.atlassian.net (project `KAN`)

## Thành viên

| Vai trò | Thành viên | Package / phần việc |
|---|---|---|
| TV1 – Lý thuyết, báo cáo, gài lỗi | Phạm Lê Phú | Báo cáo, slide, nhánh lỗi gài (giữ riêng) |
| TV2 – Setup, Example-Based Testing | Đạt Nguyễn Lê Tiến | `example` |
| TV3 – Generator, Property-Based Testing | Quân Trần Hoàng | `generators`, `property` |
| TV4 – Stateful testing, đánh giá | Khanh Nguyễn Thạch Bảo | `stateful`, chạy PIT |

## Cần cài trên máy

| Công cụ | Ghi chú |
|---|---|
| **JDK 17+** (khuyên dùng Temurin 21) | https://adoptium.net |
| **IntelliJ IDEA** (Community là đủ) | Có sẵn Maven bên trong |
| **Git** | Git for Windows |
| Maven dòng lệnh | Không bắt buộc – chỉ cần nếu muốn gõ `mvn` trong Terminal |

Thư viện (JUnit 5, jqwik, ApprovalTests, PIT) **không cần cài tay**: Maven tự tải theo `java/pom.xml` ở lần chạy đầu (cần có mạng).

## Bắt đầu

```powershell
git clone https://github.com/TDat05-dev/Testing.git
```

Trong IntelliJ: **File → Open → chọn thư mục `Testing\java` → Open as Project**.

> ⚠️ Mở thư mục **`java`**, không mở thư mục gốc `Testing` – nếu không IntelliJ sẽ không nhận Maven project.

Đợi IntelliJ tải thư viện xong, rồi chạy **Maven → Lifecycle → test**. Thấy `BUILD SUCCESS` là xong.

## Chạy test

> ⚠️ Mọi lệnh `mvn` phải chạy **trong thư mục `java/`** (nơi có `pom.xml`).

```powershell
cd java
mvn test
```

Hoặc trong IntelliJ: bấm ▶ cạnh tên class / method test.

## Chạy PIT (mutation testing)

```powershell
cd java

# Toàn bộ test
mvn test-compile org.pitest:pitest-maven:mutationCoverage

# Chỉ bộ Example-Based
mvn test-compile org.pitest:pitest-maven:mutationCoverage "-DtargetTests=dojo.supermarket.example.*"

# Chỉ bộ Property-Based (property + stateful)
mvn test-compile org.pitest:pitest-maven:mutationCoverage "-DtargetTests=dojo.supermarket.property.*,dojo.supermarket.stateful.*"
```

Báo cáo: mở `java/target/pit-reports/index.html` bằng trình duyệt.

> Trên PowerShell phải để **cả cụm** `"-DtargetTests=..."` trong dấu nháy kép.
> Mỗi lần chạy sẽ **ghi đè** báo cáo cũ – chụp hoặc copy kết quả trước khi chạy bộ tiếp theo.

| Chỉ số trong log | Ý nghĩa |
|---|---|
| `Killed X (Y%)` | Mutation score – tỉ lệ lỗi đột biến bị test bắt được (chỉ số chính để so sánh) |
| `Mutations with no coverage` | Đột biến ở dòng code không test nào chạy qua |
| `Test strength` | Tỉ lệ bắt được trong số đột biến mà test có chạy qua (đo độ chặt của assert) |

## Cấu trúc thư mục

```
java/
├── pom.xml
└── src/
    ├── main/java/dojo/supermarket/model/      ← code nghiệp vụ gốc – KHÔNG SỬA
    └── test/java/dojo/supermarket/
        ├── model/        FakeCatalog (dùng chung), SupermarketTest (test gốc của kata)
        ├── example/      Example-Based tests (JUnit)            – TV2
        ├── generators/   SupermarketGenerators (jqwik Arbitrary) – TV3, dùng chung
        ├── property/     Property tests (@Property)              – TV3
        └── stateful/     Stateful / model-based tests            – TV4
```

## Quy ước

### Đặt tên file test

| Loại | Tên file | Ví dụ |
|---|---|---|
| Example-Based (JUnit `@Test`) | `...Test.java` | `ThreeForTwoTest.java` |
| Property / Stateful (jqwik `@Property`) | `...Properties.java` | `OfferProperties.java` |

> Maven chỉ chạy file có đuôi `Test`, `Tests` hoặc `Properties`. Đặt tên khác → test **không chạy** dù vẫn `BUILD SUCCESS`.

- Test chỉ gọi **public API** (`Teller`, `ShoppingCart`, `Receipt`...) – kiểm thử hộp đen.
- Generator đặt trong `generators/SupermarketGenerators`, không viết lại trong từng file test.

### Git

- Mỗi người làm trên **nhánh riêng** (TV2: `tdat`), không commit thẳng vào `main`.
- Làm xong một task → `git push` → mở **Pull Request vào `main`** → có người review → merge.
- Commit message bắt đầu bằng **mã Jira**, ví dụ: `KAN-34: thêm property tổng tiền ≥ 0`.
- Trước khi bắt đầu task mới, cập nhật nhánh:
  ```powershell
  git checkout main
  git pull
  git checkout <nhánh-của-mình>
  git merge main
  ```
- Trước khi push: `mvn test` phải xanh. Property **cố ý fail** để ghi nhận lỗi thật của code gốc thì đánh dấu `@Disabled("KAN-xx: lý do")`.

### Jira

- Kéo issue qua **To Do → In Progress → Done** khi làm.
- Lỗi do PBT tìm ra: ghi vào issue tương ứng gồm **tên property, phản ví dụ sau Shrinking, seed**.

### Kiểm thử mù

TV1 gài lỗi vào code trên một nhánh **không đẩy lên repo này** cho đến khi bộ test của nhóm đã được **đóng băng**. Không ai hỏi hay xem trước danh sách lỗi gài.

## Lỗi thường gặp

| Thông báo | Cách xử lý |
|---|---|
| `there is no POM in this directory` | Chưa `cd java` |
| `Cannot resolve symbol 'Property'` / `'Assertions'` | File test đặt nhầm trong `src/main` – chuyển sang `src/test` |
| Không có tab Maven / không hiện ▶ | Mở sai thư mục – mở lại thư mục `java` bằng Open as Project |
| `Plugin ... not found` (gạch đỏ trong `pom.xml`) | Chạy lệnh PIT một lần để Maven tải plugin, rồi Reload |
| Test mới viết không chạy khi `mvn test` | Tên file chưa kết thúc bằng `Test` hoặc `Properties` |
| `Connection timed out` khi tải thư viện | Mạng chặn Maven Central – đổi mạng (4G) rồi chạy lại |
