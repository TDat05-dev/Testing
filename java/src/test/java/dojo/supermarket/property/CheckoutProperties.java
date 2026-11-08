package dojo.supermarket.property;

import dojo.supermarket.generators.SupermarketGenerators;
import dojo.supermarket.generators.SupermarketGenerators.Line;
import dojo.supermarket.model.FakeCatalog;
import dojo.supermarket.model.Product;
import dojo.supermarket.model.Receipt;
import dojo.supermarket.model.ShoppingCart;
import dojo.supermarket.model.SupermarketCatalog;
import dojo.supermarket.model.Teller;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property cho trường hợp KHÔNG có khuyến mãi.
 * Đây là các property chắc chắn đúng trên code gốc, dùng để xác nhận generator viết đúng
 * trước khi chuyển sang property có khuyến mãi (OfferProperties).
 */
class CheckoutProperties {

    // Sai số cho phép khi so sánh double (cộng dồn nhiều số thực sẽ lệch rất nhỏ)
    private static final double EPSILON = 1e-6;

    @Provide
    Arbitrary<List<Line>> carts() {
        return SupermarketGenerators.carts();
    }

    /** Test oracle: không có khuyến mãi thì tổng tiền = Σ(đơn giá × số lượng). */
    @Property
    void withoutOffersTotalIsSumOfLinePrices(@ForAll("carts") List<Line> lines) {
        Receipt receipt = checkout(lines);

        double expected = lines.stream()
                .mapToDouble(line -> line.unitPrice() * line.quantity())
                .sum();

        assertEquals(expected, receipt.getTotalPrice(), EPSILON);
    }

    /** Invariant: mỗi dòng trong giỏ sinh ra đúng 1 dòng trên hóa đơn, không có giảm giá. */
    @Property
    void withoutOffersReceiptMirrorsCart(@ForAll("carts") List<Line> lines) {
        Receipt receipt = checkout(lines);

        assertEquals(lines.size(), receipt.getItems().size());
        assertTrue(receipt.getDiscounts().isEmpty());
    }

    /**
     * Dựng catalog + giỏ hàng từ dữ liệu sinh ra, rồi thanh toán (không có khuyến mãi).
     * Tên sản phẩm đặt theo vị trí ("sp0", "sp1"...) nên không bao giờ trùng nhau.
     */
    private static Receipt checkout(List<Line> lines) {
        SupermarketCatalog catalog = new FakeCatalog();
        ShoppingCart cart = new ShoppingCart();

        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            Product product = new Product("sp" + i, line.unit());
            catalog.addProduct(product, line.unitPrice());
            cart.addItemQuantity(product, line.quantity());
        }
        return new Teller(catalog).checksOutArticlesFrom(cart);
    }
}
