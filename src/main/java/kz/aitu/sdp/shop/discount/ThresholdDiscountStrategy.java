package kz.aitu.sdp.shop.discount;

import java.math.BigDecimal;
import kz.aitu.sdp.shop.common.Money;
import kz.aitu.sdp.shop.contracts.DiscountStrategy;

public final class ThresholdDiscountStrategy implements DiscountStrategy {
    private final BigDecimal threshold;
    private final BigDecimal discount;

    public ThresholdDiscountStrategy(BigDecimal threshold, BigDecimal discount) {
        this.threshold = Money.requireValid(threshold);
        this.discount = Money.requireValid(discount);
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        BigDecimal validSubtotal = Money.requireValid(subtotal);
        if (validSubtotal.compareTo(threshold) < 0) {
            return new BigDecimal("0.00");
        }
        return discount.min(validSubtotal);
    }
}
