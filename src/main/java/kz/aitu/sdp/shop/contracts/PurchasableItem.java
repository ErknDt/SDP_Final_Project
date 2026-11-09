package kz.aitu.sdp.shop.contracts;

import java.math.BigDecimal;

/**
 * A configured product, optionally wrapped with additional services.
 */
public interface PurchasableItem {

    /**
     * Returns the nonblank catalog identifier of the underlying product.
     * Decorators preserve this identifier.
     */
    String getProductId();

    /**
     * Returns a nonblank description including configuration and services.
     */
    String getDescription();

    /**
     * Returns the nonnull, nonnegative configuration and service surcharge
     * per unit, with at most two decimal places.
     * The current catalog base price is added separately.
     */
    BigDecimal getAdditionalPrice();
}
