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

/** KAN-23 – Khuyến mãi TEN_PERCENT_DISCOUNT (giảm theo %). Đặc tả: mã TEN_PERCENT_xx. */
@DisplayName("TEN_PERCENT_DISCOUNT – Giảm giá theo phần trăm")
class TenPercentDiscountTest extends CheckoutFixture {

    /** Phân vùng: đơn vị EACH / KILO, nhiều mức %; Giá trị biên của %: 0 và 100. */
    @ParameterizedTest(name = "{0}: {3} {1} giá {2}, giảm {4}% → tổng {5}")
    @CsvSource({
            "TEN_PERCENT_01, EACH, 0.99,  1,   10,  0.891",
            "TEN_PERCENT_02, EACH, 0.99,  5,   10,  4.455",
            "TEN_PERCENT_03, KILO, 1.99,  2.5, 10,  4.4775",
            "TEN_PERCENT_04, EACH, 10.00, 1,   20,  8.00",
            "TEN_PERCENT_05, EACH, 10.00, 1,   0,   10.00",
            "TEN_PERCENT_06, EACH, 10.00, 1,   100, 0.00"
    })
    void productWithPercentOffer(String testCaseId, ProductUnit unit, double unitPrice,
                                 double quantity, double percent, double expectedTotal) {
        Product item = product("item", unit, unitPrice);
        teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, item, percent);
        cart.addItemQuantity(item, quantity);

        assertEquals(expectedTotal, checkout().getTotalPrice(), DELTA);
    }

    @Test
    @DisplayName("TEN_PERCENT_07: khuyến mãi gắn cho sản phẩm khác → không được giảm")
    void offerOnOtherProductGivesNoDiscount() {
        Product toothbrush = product("toothbrush", ProductUnit.EACH, 0.99);
        Product other = product("other", ProductUnit.EACH, 5.00);
        teller.addSpecialOffer(SpecialOfferType.TEN_PERCENT_DISCOUNT, other, 10);
        cart.addItemQuantity(toothbrush, 1);

        Receipt receipt = checkout();

        assertEquals(0.99, receipt.getTotalPrice(), DELTA);
        assertTrue(receipt.getDiscounts().isEmpty());
    }
}
