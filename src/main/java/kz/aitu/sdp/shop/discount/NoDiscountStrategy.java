package kz.aitu.sdp.shop.discount;

import java.math.BigDecimal;
import kz.aitu.sdp.shop.common.Money;
import kz.aitu.sdp.shop.contracts.DiscountStrategy;

public final class NoDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        Money.requireValid(subtotal);
        return new BigDecimal("0.00");
    }
}
