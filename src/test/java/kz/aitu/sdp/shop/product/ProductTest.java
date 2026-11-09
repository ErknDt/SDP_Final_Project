package kz.aitu.sdp.shop.product;

import java.math.BigDecimal;
import kz.aitu.sdp.shop.contracts.PurchasableItem;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {
    @Test
    void builderCreatesDefaultConfiguration() {
        Laptop laptop = new Laptop.Builder("CAMPUS", "Campus Laptop").build();

        assertEquals("Campus Laptop, 8 GB RAM, 256 GB SSD, Silver", laptop.getDescription());
        assertEquals(new BigDecimal("0.00"), laptop.getAdditionalPrice());
    }

    @Test
    void configurationAndServicesCombineTheirSurcharges() {
        PurchasableItem laptop = new Laptop.Builder("CAMPUS", "Campus Laptop")
                .ramGb(16).storageGb(512).color("Black").build();
        laptop = new ExtendedWarrantyDecorator(new GiftWrapDecorator(laptop));

        assertEquals(new BigDecimal("63000.00"), laptop.getAdditionalPrice());
        assertEquals("CAMPUS", laptop.getProductId());
        assertTrue(laptop.getDescription().endsWith("+ gift wrap + extended warranty"));
    }

    @Test
    void reusingBuilderPreservesPreviouslyBuiltLaptop() {
        Laptop.Builder builder = new Laptop.Builder("CAMPUS", "Campus Laptop");
        Laptop original = builder.build();

        Laptop upgraded = builder.ramGb(32).build();

        assertEquals(new BigDecimal("0.00"), original.getAdditionalPrice());
        assertEquals(new BigDecimal("50000.00"), upgraded.getAdditionalPrice());
    }

    @Test
    void unsupportedConfigurationIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Laptop.Builder("CAMPUS", "Laptop").ramGb(12).build());
        assertThrows(IllegalArgumentException.class,
                () -> new Laptop.Builder("CAMPUS", "Laptop").storageGb(500).build());
    }

    @Test
    void blankProductDataIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Laptop.Builder(" ", "Laptop").build());
        assertThrows(IllegalArgumentException.class,
                () -> new Laptop.Builder("CAMPUS", "Laptop").color(null).build());
    }

    @Test
    void decoratorsRequireWrappedProduct() {
        assertThrows(IllegalArgumentException.class, () -> new GiftWrapDecorator(null));
        assertThrows(IllegalArgumentException.class, () -> new ExtendedWarrantyDecorator(null));
    }
}
