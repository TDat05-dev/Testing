package dojo.supermarket.stateful;

import dojo.supermarket.model.SpecialOfferType;

import java.util.HashMap;
import java.util.Map;

/**
 * Model đối chiếu cho ShoppingCart: Map tên sản phẩm -> số lượng cộng dồn,
 * cộng công thức khuyến mãi tính tay theo đặc tả nghiệp vụ.
 * Phạm vi: hàng EACH, số lượng nguyên dương, giá combo thấp hơn giá mua lẻ.
 */
public final class CartModel {

    public record Rule(double unitPrice, SpecialOfferType offer, double argument) { }

    private final Map<String, Rule> rules = new HashMap<>();
    private final Map<String, Integer> quantities = new HashMap<>();
    private int addCount = 0;

    public void define(String name, double unitPrice, SpecialOfferType offer, double argument) {
        rules.put(name, new Rule(unitPrice, offer, argument));
    }

    public void add(String name, int quantity) {
        quantities.merge(name, quantity, Integer::sum);
        addCount++;
    }

    public int addCount() {
        return addCount;
    }

    public double expectedTotal() {
        double total = 0;
        for (var e : quantities.entrySet()) {
            total += lineTotal(rules.get(e.getKey()), e.getValue());
        }
        return total;
    }

    static double lineTotal(Rule r, int q) {
        double u = r.unitPrice();
        if (r.offer() == null) return q * u;
        return switch (r.offer()) {
            case THREE_FOR_TWO        -> (q - q / 3) * u;                       // cứ 3 tặng 1
            case TEN_PERCENT_DISCOUNT -> q * u * (1 - r.argument() / 100.0);
            case TWO_FOR_AMOUNT       -> (q / 2) * r.argument() + (q % 2) * u;  // q / 2 là chia nguyên
            case FIVE_FOR_AMOUNT      -> (q / 5) * r.argument() + (q % 5) * u;
        };
    }
}