package kz.aitu.sdp.shop.discount;

import java.math.BigDecimal;
import java.util.List;
import kz.aitu.sdp.shop.contracts.DiscountStrategy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscountTest {
    @Test
    void noDiscountReturnsZero() {
        assertEquals(new BigDecimal("0.00"),
                new NoDiscountStrategy().calculateDiscount(new BigDecimal("100.00")));
    }

    @Test
    void percentageRoundsHalfUp() {
        assertEquals(new BigDecimal("0.01"),
                new PercentageDiscountStrategy(10).calculateDiscount(new BigDecimal("0.05")));
    }

    @Test
    void fullPercentageDiscountEqualsSubtotal() {
        assertEquals(new BigDecimal("125.00"),
                new PercentageDiscountStrategy(100).calculateDiscount(new BigDecimal("125")));
    }

    @Test
    void thresholdIncludesExactBoundary() {
        DiscountStrategy strategy = new ThresholdDiscountStrategy(
                new BigDecimal("100.00"), new BigDecimal("15.00"));

        assertEquals(new BigDecimal("0.00"), strategy.calculateDiscount(new BigDecimal("99.99")));
        assertEquals(new BigDecimal("15.00"), strategy.calculateDiscount(new BigDecimal("100.00")));
    }

    @Test
    void fixedDiscountIsCappedAtSubtotal() {
        DiscountStrategy strategy = new ThresholdDiscountStrategy(BigDecimal.ZERO, new BigDecimal("500"));

        assertEquals(new BigDecimal("10.00"), strategy.calculateDiscount(new BigDecimal("10")));
    }

    @Test
    void zeroSubtotalIsSupportedByEveryStrategy() {
        for (DiscountStrategy strategy : strategies()) {
            assertEquals(new BigDecimal("0.00"), strategy.calculateDiscount(BigDecimal.ZERO));
        }
    }

    @Test
    void everyStrategyRejectsInvalidSubtotal() {
        for (DiscountStrategy strategy : strategies()) {
            assertThrows(IllegalArgumentException.class, () -> strategy.calculateDiscount(null));
            assertThrows(IllegalArgumentException.class,
                    () -> strategy.calculateDiscount(new BigDecimal("-1")));
            assertThrows(IllegalArgumentException.class,
                    () -> strategy.calculateDiscount(new BigDecimal("0.001")));
        }
    }

    @Test
    void invalidStrategyConfigurationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscountStrategy(101));
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscountStrategy(-1));
        assertThrows(IllegalArgumentException.class,
                () -> new ThresholdDiscountStrategy(new BigDecimal("-1"), BigDecimal.ZERO));
    }

    private List<DiscountStrategy> strategies() {
        return List.of(new NoDiscountStrategy(), new PercentageDiscountStrategy(10),
                new ThresholdDiscountStrategy(new BigDecimal("100"), new BigDecimal("10")));
    }
}
