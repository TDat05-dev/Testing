package dojo.supermarket.generators;

import dojo.supermarket.model.ProductUnit;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import java.util.List;

/**
 * Bộ generator dùng chung cho toàn bộ property test và stateful test của nhóm.
 * Nguyên tắc: chỉ sinh dữ liệu HỢP LỆ theo nghiệp vụ, để lỗi tìm ra là lỗi thật của hệ thống,
 * không phải lỗi do dữ liệu test vô nghĩa.
 * Generator KHÔNG sinh tên sản phẩm: nơi dựng giỏ hàng tự đặt tên "sp0", "sp1"... theo vị trí,
 * nhờ vậy tên luôn khác nhau (FakeCatalog lưu giá theo tên, trùng tên sẽ ghi đè giá).
 */
public final class SupermarketGenerators {

    private static final int MAX_LINES_PER_CART = 10;

    private SupermarketGenerators() {
    }

    /** Một dòng hàng trong giỏ: đơn vị bán + đơn giá + số lượng mua. */
    public record Line(ProductUnit unit, double unitPrice, double quantity) {
    }

    /** Đơn giá dương, làm tròn 2 chữ số thập phân như giá tiền thật. */
    public static Arbitrary<Double> unitPrices() {
        return Arbitraries.doubles().between(0.01, 100.0).ofScale(2);
    }

    /** Số lượng hàng bán theo cái: số nguyên 1..20. */
    public static Arbitrary<Double> eachQuantities() {
        return Arbitraries.integers().between(1, 20).map(Integer::doubleValue);
    }

    /** Số lượng hàng bán theo kg: số thập phân 0.1..10.0. */
    public static Arbitrary<Double> kiloQuantities() {
        return Arbitraries.doubles().between(0.1, 10.0).ofScale(1);
    }

    /** Dòng hàng bán theo cái. */
    public static Arbitrary<Line> eachLines() {
        return Combinators.combine(unitPrices(), eachQuantities())
                .as((price, qty) -> new Line(ProductUnit.EACH, price, qty));
    }

    /** Dòng hàng bán theo kg. */
    public static Arbitrary<Line> kiloLines() {
        return Combinators.combine(unitPrices(), kiloQuantities())
                .as((price, qty) -> new Line(ProductUnit.KILO, price, qty));
    }

    /** Một dòng hàng bất kỳ: chọn ngẫu nhiên giữa hàng bán theo cái và hàng bán theo kg. */
    public static Arbitrary<Line> lines() {
        return Arbitraries.oneOf(eachLines(), kiloLines());
    }

    /** Giỏ hàng: danh sách 0..10 dòng hàng. */
    public static Arbitrary<List<Line>> carts() {
        return lines().list().ofMaxSize(MAX_LINES_PER_CART);
    }
}
