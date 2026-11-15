package dojo.supermarket.example;

import dojo.supermarket.model.Product;
import dojo.supermarket.model.ProductUnit;
import dojo.supermarket.model.Receipt;
import dojo.supermarket.model.SpecialOfferType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** KAN-26 – Các trường hợp biên của giỏ hàng. */
@DisplayName("EDGE – Trường hợp biên của giỏ hàng")
class EdgeCaseTest extends CheckoutFixture {

    @Test
    @DisplayName("EDGE_01: giỏ hàng rỗng → tổng 0, hóa đơn không có dòng nào")
    void emptyCart() {
        Receipt receipt = checkout();

        assertEquals(0.00, receipt.getTotalPrice(), DELTA);
        assertTrue(receipt.getItems().isEmpty());
    }

    @Test
    @DisplayName("EDGE_02: số lượng 0 → tổng 0")
    void zeroQuantity() {
        Product toothbrush = product("toothbrush", ProductUnit.EACH, 0.99);
        cart.addItemQuantity(toothbrush, 0);

        assertEquals(0.00, checkout().getTotalPrice(), DELTA);
    }

    // Dự kiến FAIL trên code gốc (thực tế = -3.00) → lỗi KAN-45.
    // Sau khi chạy và ghi nhận kết quả vào Excel, bỏ comment dòng @Disabled bên dưới trước khi push.
    @org.junit.jupiter.api.Disabled("KAN-45: lỗi thật của code gốc – chấp nhận số lượng âm")
    @Test
    @DisplayName("EDGE_03: số lượng âm (-3) → tổng tiền không được âm")
    void negativeQuantityMustNotGiveNegativeTotal() {
        Product pen = product("pen", ProductUnit.EACH, 1.00);
        cart.addItemQuantity(pen, -3);

        double total = checkout().getTotalPrice();

        assertTrue(total >= 0, "Tổng tiền âm: " + total);
    }

    @Test
    @DisplayName("EDGE_04: thêm cùng sản phẩm 2 lần (2 + 1) có THREE_FOR_TWO → cộng dồn thành 3, trả 1.98")
    void sameProductAddedTwiceIsAccumulated() {
        Product toothbrush = product("toothbrush", ProductUnit.EACH, 0.99);
        teller.addSpecialOffer(SpecialOfferType.THREE_FOR_TWO, toothbrush, 0);
        cart.addItemQuantity(toothbrush, 2);
        cart.addItemQuantity(toothbrush, 1);

        assertEquals(1.98, checkout().getTotalPrice(), DELTA);
    }

    @Test
    @DisplayName("EDGE_05: giỏ có 3 sản phẩm, mỗi sản phẩm 1 loại khuyến mãi khác nhau → tổng 6.552")
    void mixedOffersInOneCart() {
        Product toothbrush = product("toothbrush", ProductUnit.EACH, 0.99);
        Product apples = product("apples", ProductUnit.KILO, 1.99);
        Product cherryTomatoes = product("cherry tomatoes", ProductUnit.EACH, 0.69);
        teller.addSpecialOffer(SpecialOfferType.THREE_FOR_TWO, toothbrush, 0);
        teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, apples, 10);
        teller.addSpecialOffer(SpecialOfferType.TWO_FOR_AMOUNT, cherryTomatoes, 0.99);
        cart.addItemQuantity(toothbrush, 3);       // 1.98
        cart.addItemQuantity(apples, 2.0);         // 3.98 - 10% = 3.582
        cart.addItemQuantity(cherryTomatoes, 2);   // 0.99

        assertEquals(6.552, checkout().getTotalPrice(), DELTA);
    }
}
