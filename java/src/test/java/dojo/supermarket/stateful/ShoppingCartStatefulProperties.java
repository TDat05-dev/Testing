package dojo.supermarket.stateful;

import dojo.supermarket.model.FakeCatalog;
import dojo.supermarket.model.Product;
import dojo.supermarket.model.ProductUnit;
import dojo.supermarket.model.Receipt;
import dojo.supermarket.model.ShoppingCart;
import dojo.supermarket.model.SpecialOfferType;
import dojo.supermarket.model.Teller;
import net.jqwik.api.*;
import net.jqwik.api.state.Action;
import net.jqwik.api.state.ActionChain;
import net.jqwik.api.state.Transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShoppingCartStatefulProperties {

    private static final double EPS = 1e-6;

    /** Hệ thống thật + model, cùng cấu hình sản phẩm/khuyến mãi. */
    static final class CartState {
        final FakeCatalog catalog = new FakeCatalog();
        final Teller teller = new Teller(catalog);
        final ShoppingCart cart = new ShoppingCart();
        final CartModel model = new CartModel();
        final Product[] products = new Product[5];

        CartState() {
            setup(0, "sp0", 1.00, null, 0);
            setup(1, "sp1", 0.99, SpecialOfferType.THREE_FOR_TWO, 0);
            setup(2, "sp2", 10.00, SpecialOfferType.TEN_PERCENT_DISCOUNT, 20);
            setup(3, "sp3", 1.00, SpecialOfferType.TWO_FOR_AMOUNT, 1.50);   // combo < 2 x giá lẻ
            setup(4, "sp4", 1.00, SpecialOfferType.FIVE_FOR_AMOUNT, 4.00);  // combo < 5 x giá lẻ
        }

        private void setup(int i, String name, double price, SpecialOfferType offer, double arg) {
            products[i] = new Product(name, ProductUnit.EACH);
            catalog.addProduct(products[i], price);
            if (offer != null) teller.addSpecialOffer(offer, products[i], arg);
            model.define(name, price, offer, arg);
        }
    }

    /** Thao tác: thêm một sản phẩm ngẫu nhiên với số lượng nguyên 1..12, rồi checkout và so với model. */
    static class AddItemAction implements Action.Independent<CartState> {
        @Override
        public Arbitrary<Transformer<CartState>> transformer() {
            return Combinators.combine(
                    Arbitraries.integers().between(0, 4),
                    Arbitraries.integers().between(1, 12)
            ).as((idx, qty) -> Transformer.mutate(
                    "add sp" + idx + " x" + qty,
                    state -> {
                        state.cart.addItemQuantity(state.products[idx], qty);
                        state.model.add("sp" + idx, qty);

                        Receipt receipt = state.teller.checksOutArticlesFrom(state.cart);

                        assertEquals(state.model.expectedTotal(), receipt.getTotalPrice(), EPS,
                                "Tổng tiền lệch model sau: add sp" + idx + " x" + qty);
                        assertEquals(state.model.addCount(), receipt.getItems().size(),
                                "Số dòng hóa đơn phải bằng số lần addItemQuantity");
                    }));
        }
    }

    @Provide
    Arbitrary<ActionChain<CartState>> chains() {
        return ActionChain.startWith(CartState::new)
                .withAction(new AddItemAction())
                .withMaxTransformations(15);
    }

    @Property
    void cartMatchesModel(@ForAll("chains") ActionChain<CartState> chain) {
        chain.run();
    }
    @Disabled("KAN-45: chấp nhận số lượng âm")
    @Property
    void totalNeverNegative(@ForAll @net.jqwik.api.constraints.IntRange(min = -20, max = 20) int qty) {
        FakeCatalog catalog = new FakeCatalog();
        Product p = new Product("sp0", ProductUnit.EACH);
        catalog.addProduct(p, 1.00);
        ShoppingCart cart = new ShoppingCart();
        cart.addItemQuantity(p, qty);
        org.junit.jupiter.api.Assertions.assertTrue(
                new Teller(catalog).checksOutArticlesFrom(cart).getTotalPrice() >= -EPS);
    }
}