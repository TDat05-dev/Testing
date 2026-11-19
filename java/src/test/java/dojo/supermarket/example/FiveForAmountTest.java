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

/** KAN-25 – Khuyến mãi FIVE_FOR_AMOUNT (5 cái giá combo). Đặc tả: mã FIVE_FOR_AMOUNT_xx. */
@DisplayName("FIVE_FOR_AMOUNT – 5 sản phẩm giá combo")
class FiveForAmountTest extends CheckoutFixture {

    /** Giá trị biên quanh ngưỡng 5 (4, 5, 6) và ngưỡng 10 (10, 11). */
    @ParameterizedTest(name = "{0}: mua {1} tuýp giá 1.79, combo 5 tuýp 7.49 → tổng {2}")
    @CsvSource({
            "FIVE_FOR_AMOUNT_01, 4,  7.16",
            "FIVE_FOR_AMOUNT_02, 5,  7.49",
            "FIVE_FOR_AMOUNT_03, 6,  9.28",
            "FIVE_FOR_AMOUNT_04, 10, 14.98",
            "FIVE_FOR_AMOUNT_05, 11, 16.77"
    })
    void eachProductWithComboOffer(String testCaseId, int quantity, double expectedTotal) {
        Product toothpaste = product("toothpaste", ProductUnit.EACH, 1.79);
        teller.addSpecialOffer(SpecialOfferType.FIVE_FOR_AMOUNT, toothpaste, 7.49);
        cart.addItemQuantity(toothpaste, quantity);

        assertEquals(expectedTotal, checkout().getTotalPrice(), DELTA);
    }

    @Test
    @DisplayName("FIVE_FOR_AMOUNT_06: khuyến mãi gắn cho sản phẩm khác → không được giảm")
    void offerOnOtherProductGivesNoDiscount() {
        Product toothpaste = product("toothpaste", ProductUnit.EACH, 1.79);
        Product other = product("other", ProductUnit.EACH, 5.00);
        teller.addSpecialOffer(SpecialOfferType.FIVE_FOR_AMOUNT, other, 7.49);
        cart.addItemQuantity(toothpaste, 5);

        Receipt receipt = checkout();

        assertEquals(8.95, receipt.getTotalPrice(), DELTA);
        assertTrue(receipt.getDiscounts().isEmpty());
    }
}
