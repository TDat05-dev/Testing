package dojo.supermarket.property;

import dojo.supermarket.generators.SupermarketGenerators;
import dojo.supermarket.generators.SupermarketGenerators.*;
import dojo.supermarket.model.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.statistics.Statistics;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class OfferProperties {
    private static final double EPS = 1e-6;
    @Provide Arbitrary<List<OfferedLine>> carts() { return SupermarketGenerators.cartsWithOffers(); }
    @Provide Arbitrary<Line> lines() { return SupermarketGenerators.lines(); }
    @Provide Arbitrary<Line> eachLines() { return SupermarketGenerators.eachLines(); }
    @Provide Arbitrary<Double> prices() { return SupermarketGenerators.unitPrices(); }

    private Receipt checkout(List<OfferedLine> lines, boolean withOffers, boolean reverse, boolean split) {
        FakeCatalog catalog = new FakeCatalog();
        ShoppingCart cart = new ShoppingCart();
        Teller teller = new Teller(catalog);
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) indexes.add(i);
        if (reverse) Collections.reverse(indexes);
        for (int i : indexes) {
            OfferedLine entry = lines.get(i);
            Line line = entry.line();
            Product product = new Product("sp" + i, line.unit());
            catalog.addProduct(product, line.unitPrice());
            if (withOffers && entry.offer() != null)
                teller.addSpecialOffer(entry.offer().type(), product, entry.offer().argument());
            if (split) {
                cart.addItemQuantity(product, line.quantity());
                cart.addItemQuantity(product, line.quantity());
            } else cart.addItemQuantity(product, line.quantity() * 2);
        }
        return teller.checksOutArticlesFrom(cart);
    }
    private Receipt single(Line line, OfferSpec offer) {
        Line half = new Line(line.unit(), line.unitPrice(), line.quantity() / 2);
        return checkout(List.of(new OfferedLine(half, offer)), true, false, false);
    }
    private double gross(List<OfferedLine> lines) {
        return lines.stream().mapToDouble(x -> 2 * x.line().unitPrice() * x.line().quantity()).sum();
    }
    @Property void totalNeverNegative(@ForAll("carts") List<OfferedLine> cart) {
        assertTrue(checkout(cart, true, false, false).getTotalPrice() >= -EPS);
    }
    @Property void discountNeverExceedsGross(@ForAll("carts") List<OfferedLine> cart) {
        Receipt receipt = checkout(cart, true, false, false);
        double discount = -receipt.getDiscounts().stream().mapToDouble(Discount::discountAmount).sum();
        assertTrue(discount <= gross(cart) + EPS);
    }
    @Property void orderDoesNotChangeTotal(@ForAll("carts") List<OfferedLine> cart) {
        assertEquals(checkout(cart, true, false, false).getTotalPrice(), checkout(cart, true, true, false).getTotalPrice(), EPS);
    }
    @Property void addingTwiceMatchesAddingDouble(@ForAll("carts") List<OfferedLine> cart) {
        assertEquals(checkout(cart, true, false, false).getTotalPrice(), checkout(cart, true, false, true).getTotalPrice(), EPS);
    }
    @Disabled("Combo price may exceed regular price; see property-findings.md")
    @Property void offerNeverRaisesTotal(@ForAll("carts") List<OfferedLine> cart) {
        assertTrue(checkout(cart, true, false, false).getTotalPrice() <= checkout(cart, false, false, false).getTotalPrice() + EPS);
    }
    @Property void percentageMatchesFormula(@ForAll("lines") Line line, @ForAll @IntRange(min=1, max=100) int percent) {
        double expected = line.quantity() * line.unitPrice() * (1 - percent / 100.0);
        assertEquals(expected, single(line, new OfferSpec(SpecialOfferType.TEN_PERCENT_DISCOUNT, percent)).getTotalPrice(), EPS);
    }
    @Disabled("Fractional KILO quantities lose the remainder; see property-findings.md")
    @Property void threeForTwoMatchesFormula(@ForAll("lines") Line line) {
        double expected = (line.quantity() - Math.floor(line.quantity() / 3)) * line.unitPrice();
        assertEquals(expected, single(line, new OfferSpec(SpecialOfferType.THREE_FOR_TWO, 0)).getTotalPrice(), EPS);
    }
    @Disabled("Fractional KILO quantities lose the remainder; see property-findings.md")
    @Property void twoForAmountMatchesFormula(@ForAll("lines") Line line, @ForAll("prices") double price) {
        double groups = Math.floor(line.quantity() / 2);
        double expected = groups * price + (line.quantity() - groups * 2) * line.unitPrice();
        assertEquals(expected, single(line, new OfferSpec(SpecialOfferType.TWO_FOR_AMOUNT, price)).getTotalPrice(), EPS);
    }
    @Disabled("Fractional KILO quantities lose the remainder; see property-findings.md")
    @Property void fiveForAmountMatchesFormula(@ForAll("lines") Line line, @ForAll("prices") double price) {
        double groups = Math.floor(line.quantity() / 5);
        double expected = groups * price + (line.quantity() - groups * 5) * line.unitPrice();
        assertEquals(expected, single(line, new OfferSpec(SpecialOfferType.FIVE_FOR_AMOUNT, price)).getTotalPrice(), EPS);
    }
    @Property void wholeItemComboFormulas(@ForAll("eachLines") Line line, @ForAll("prices") double price) {
        threeForTwoMatchesFormula(line);
        twoForAmountMatchesFormula(line, price);
        fiveForAmountMatchesFormula(line, price);
    }
    @Property void generatorCoverage(@ForAll("carts") List<OfferedLine> cart) {
        for (OfferedLine line : cart) {
            Statistics.label("unit").collect(line.line().unit());
            Statistics.label("offer").collect(line.offer() == null ? "NONE" : line.offer().type().name());
        }
    }
}
