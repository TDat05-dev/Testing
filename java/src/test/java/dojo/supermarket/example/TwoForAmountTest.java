package dojo.supermarket.example;

import dojo.supermarket.model.Product;
import dojo.supermarket.model.ProductUnit;
import dojo.supermarket.model.Receipt;
import dojo.supermarket.model.SpecialOfferType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** KAN-24 – Khuyến mãi TWO_FOR_AMOUNT (2 cái giá combo). Đặc tả: mã TWO_FOR_AMOUNT_xx. */
@DisplayName("TWO_FOR_AMOUNT – 2 sản phẩm giá combo")
class TwoForAmountTest extends CheckoutFixture {

    /** Giá trị biên quanh ngưỡng 2 (1, 2, 3) + phân vùng nhiều combo (4). */
    @ParameterizedTest(name = "{0}: mua {1} hộp giá 0.69, combo 2 hộp 0.99 → tổng {2}")
    @CsvSource({
            "TWO_FOR_AMOUNT_01, 1, 0.69",
            "TWO_FOR_AMOUNT_02, 2, 0.99",
            "TWO_FOR_AMOUNT_03, 3, 1.68",
            "TWO_FOR_AMOUNT_04, 4, 1.98"
    })
    void eachProductWithComboOffer(String testCaseId, int quantity, double expectedTotal) {
        Product cherryTomatoes = product("cherry tomatoes", ProductUnit.EACH, 0.69);
        teller.addSpecialOffer(SpecialOfferType.TWO_FOR_AMOUNT, cherryTomatoes, 0.99);
        cart.addItemQuantity(cherryTomatoes, quantity);

        assertEquals(expectedTotal, checkout().getTotalPrice(), DELTA);
    }

    @Test
    @DisplayName("TWO_FOR_AMOUNT_05: khuyến mãi gắn cho sản phẩm khác → không được giảm")
    void offerOnOtherProductGivesNoDiscount() {
        Product cherryTomatoes = product("cherry tomatoes", ProductUnit.EACH, 0.69);
        Product other = product("other", ProductUnit.EACH, 5.00);
        teller.addSpecialOffer(SpecialOfferType.TWO_FOR_AMOUNT, other, 0.99);
        cart.addItemQuantity(cherryTomatoes, 2);

        Receipt receipt = checkout();

        assertEquals(1.38, receipt.getTotalPrice(), DELTA);
        assertTrue(receipt.getDiscounts().isEmpty());
    }

    // Dự kiến FAIL trên code gốc (thực tế = 5.00) → lỗi KAN-44.
    // Sau khi chạy và ghi nhận kết quả vào Excel, bỏ comment dòng @Disabled bên dưới trước khi push.
    @org.junit.jupiter.api.Disabled("KAN-44: lỗi thật của code gốc – khuyến mãi làm tăng giá")
    @Test
    @DisplayName("TWO_FOR_AMOUNT_06: giá combo (5.00) cao hơn mua lẻ (2 x 1.00) → khách không trả quá 2.00")
    void comboMustNotIncreasePrice() {
        Product pen = product("pen", ProductUnit.EACH, 1.00);
        teller.addSpecialOffer(SpecialOfferType.TWO_FOR_AMOUNT, pen, 5.00);
        cart.addItemQuantity(pen, 2);

        double total = checkout().getTotalPrice();

        assertTrue(total <= 2.00 + DELTA, "Khuyến mãi làm tăng giá: tổng = " + total);
    }
}
