package kz.aitu.sdp.shop.contracts;

import java.math.BigDecimal;

/**
 * An interchangeable rule for calculating a cart's discount amount.
 */
public interface DiscountStrategy {

    /**
     * Calculates the amount to subtract from the subtotal.
     * The result must be between zero and the subtotal, inclusive.
     * Round monetary results to two decimal places using HALF_UP.
     *
     * @param subtotal the nonnull, nonnegative total before discount,
     *                 with at most two decimal places
     * @return the nonnull discount amount
     * @throws IllegalArgumentException if the subtotal is invalid
     */
    BigDecimal calculateDiscount(BigDecimal subtotal);
}
