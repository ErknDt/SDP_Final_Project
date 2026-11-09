package kz.aitu.sdp.shop.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import kz.aitu.sdp.shop.common.Money;
import kz.aitu.sdp.shop.contracts.DiscountStrategy;

public final class PercentageDiscountStrategy implements DiscountStrategy {
    private final int percentage;

    public PercentageDiscountStrategy(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100.");
        }
        this.percentage = percentage;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        return Money.requireValid(subtotal).multiply(BigDecimal.valueOf(percentage))
                .movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
    }
}
