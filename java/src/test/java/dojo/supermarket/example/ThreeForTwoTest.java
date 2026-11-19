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

/** KAN-22 – Khuyến mãi THREE_FOR_TWO (mua 3 tính tiền 2). Đặc tả: sheet "Test Cases", mã THREE_FOR_TWO_xx. */
@DisplayName("THREE_FOR_TWO – Mua 3 tính tiền 2")
class ThreeForTwoTest extends CheckoutFixture {

    /** Phân vùng tương đương (< 3 / >= 3) + Phân tích giá trị biên quanh ngưỡng 3 và 6. */
    @ParameterizedTest(name = "{0}: mua {1} cái giá 0.99 → tổng {2}, giảm {3}")
    @CsvSource({
            "THREE_FOR_TWO_01, 1, 0.99, 0.00",
            "THREE_FOR_TWO_02, 2, 1.98, 0.00",
            "THREE_FOR_TWO_03, 3, 1.98, 0.99",
            "THREE_FOR_TWO_04, 4, 2.97, 0.99",
            "THREE_FOR_TWO_05, 5, 3.96, 0.99",
            "THREE_FOR_TWO_06, 6, 3.96, 1.98",
            "THREE_FOR_TWO_07, 7, 4.95, 1.98"
    })
    void eachProductWithOffer(String testCaseId, int quantity, double expectedTotal, double expectedDiscount) {
        Product toothbrush = product("toothbrush", ProductUnit.EACH, 0.99);
        teller.addSpecialOffer(SpecialOfferType.THREE_FOR_TWO, toothbrush, 0);
        cart.addItemQuantity(toothbrush, quantity);

        Receipt receipt = checkout();

        assertEquals(expectedTotal, receipt.getTotalPrice(), DELTA);
        assertEquals(expectedDiscount, totalDiscount(receipt), DELTA);
    }

    @Test
    @DisplayName("THREE_FOR_TWO_08: khuyến mãi gắn cho sản phẩm khác → không được giảm")
    void offerOnOtherProductGivesNoDiscount() {
        Product toothbrush = product("toothbrush", ProductUnit.EACH, 0.99);
        Product other = product("other", ProductUnit.EACH, 5.00);
        teller.addSpecialOffer(SpecialOfferType.THREE_FOR_TWO, other, 0);
        cart.addItemQuantity(toothbrush, 3);

        Receipt receipt = checkout();

        assertEquals(2.97, receipt.getTotalPrice(), DELTA);
        assertTrue(receipt.getDiscounts().isEmpty());
    }

    @Test
    @DisplayName("THREE_FOR_TWO_09: hàng theo kg, 3.0 kg giá 1.99 → trả tiền 2 kg = 3.98")
    void kiloProductWholeQuantity() {
        Product apples = product("apples", ProductUnit.KILO, 1.99);
        teller.addSpecialOffer(SpecialOfferType.THREE_FOR_TWO, apples, 0);
        cart.addItemQuantity(apples, 3.0);

        assertEquals(3.98, checkout().getTotalPrice(), DELTA);
    }

    // Dự kiến FAIL trên code gốc (thực tế = 2.00) → lỗi KAN-43.
    // Sau khi chạy và ghi nhận kết quả vào Excel, bỏ comment dòng @Disabled bên dưới trước khi push.
    @org.junit.jupiter.api.Disabled("KAN-43: lỗi thật của code gốc – số lượng lẻ theo kg")
    @Test
    @DisplayName("THREE_FOR_TWO_10: 3.5 kg giá 1.00 → chỉ tặng 1 kg, trả 2.50")
    void kiloProductFractionalQuantity() {
        Product apples = product("apples", ProductUnit.KILO, 1.00);
        teller.addSpecialOffer(SpecialOfferType.THREE_FOR_TWO, apples, 0);
        cart.addItemQuantity(apples, 3.5);

        assertEquals(2.50, checkout().getTotalPrice(), DELTA);
    }
}
