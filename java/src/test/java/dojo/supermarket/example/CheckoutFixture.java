package dojo.supermarket.example;

import dojo.supermarket.model.Discount;
import dojo.supermarket.model.FakeCatalog;
import dojo.supermarket.model.Product;
import dojo.supermarket.model.ProductUnit;
import dojo.supermarket.model.Receipt;
import dojo.supermarket.model.ShoppingCart;
import dojo.supermarket.model.SupermarketCatalog;
import dojo.supermarket.model.Teller;

/**
 * Điều kiện tiên quyết dùng chung cho mọi Example-Based test:
 * một catalog rỗng, một Teller và một giỏ hàng rỗng.
 */
abstract class CheckoutFixture {

    protected static final double DELTA = 0.001;

    protected final SupermarketCatalog catalog = new FakeCatalog();
    protected final Teller teller = new Teller(catalog);
    protected final ShoppingCart cart = new ShoppingCart();

    /** Tạo sản phẩm và đăng ký đơn giá vào catalog. */
    protected Product product(String name, ProductUnit unit, double unitPrice) {
        Product product = new Product(name, unit);
        catalog.addProduct(product, unitPrice);
        return product;
    }

    /**
     * Thanh toán giỏ hàng và IN RA kết quả thực tế
     */
    protected Receipt checkout() {
        Receipt receipt = teller.checksOutArticlesFrom(cart);
        System.out.printf("Kết quả thực tế: Tổng tiền = %.4f; Số tiền giảm = %.4f; Số dòng hóa đơn = %d; Số dòng giảm giá = %d%n",
                receipt.getTotalPrice(), totalDiscount(receipt),
                receipt.getItems().size(), receipt.getDiscounts().size());
        return receipt;
    }

    /** Tổng số tiền được giảm trên hóa đơn (số dương). */
    protected static double totalDiscount(Receipt receipt) {
        return -receipt.getDiscounts().stream().mapToDouble(Discount::discountAmount).sum();
    }
}
