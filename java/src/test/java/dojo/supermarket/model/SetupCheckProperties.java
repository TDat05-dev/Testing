package dojo.supermarket.model;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SetupCheckProperties {
    @Property
    void additionIsCommutative(@ForAll int a, @ForAll int b) {
        assertEquals(a + b, b + a);
    }
}