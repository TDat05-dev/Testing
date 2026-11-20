package dojo.supermarket.model;

import java.util.Objects;

public record Product(String name, ProductUnit unit) {

    @Override
    public boolean equals(Object other) {
        return other instanceof Product product
                && Objects.equals(name, product.name());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
