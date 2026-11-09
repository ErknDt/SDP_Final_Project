package kz.aitu.sdp.shop.contracts;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Stores current base prices, available stock and product subscriptions.
 * Prices use KZT, are nonnegative and have at most two decimal places.
 *
 * <p>Blank identifiers, null arguments and negative values are rejected with
 * IllegalArgumentException. Operations on existing products also reject unknown
 * identifiers. Stock quantities are integers.
 *
 * <p>State changes synchronously notify the affected products' observers.
 * A failing callback must allow delivery to the remaining observers and must
 * not make a completed catalog update appear to have failed. The catalog
 * implementation reports callback failures separately.
 */
public interface Catalog {

    /**
     * Registers a product with its initial base price and stock.
     * Rejects duplicate identifiers with IllegalArgumentException.
     */
    void addProduct(String productId, BigDecimal unitPrice, int stock);

    /**
     * Returns the current base price for one unit, before item surcharges.
     */
    BigDecimal getUnitPrice(String productId);

    /**
     * Returns the current available stock.
     */
    int getStock(String productId);

    /**
     * Updates the base price and notifies the product's observers.
     */
    void updatePrice(String productId, BigDecimal unitPrice);

    /**
     * Sets the available stock and notifies the product's observers.
     */
    void updateStock(String productId, int stock);

    /**
     * Decreases stock for a complete order, then notifies affected observers.
     * Validate every entry before changing any stock. If validation fails,
     * preserve every product's stock and send no notifications.
     *
     * @param quantities a nonempty map of product identifiers to positive counts
     * @throws IllegalArgumentException if the map or any entry is invalid
     * @throws IllegalStateException if any product has insufficient stock
     */
    void decreaseStock(Map<String, Integer> quantities);

    /**
     * Subscribes an observer to future changes of one product.
     * Register the same observer instance at most once per product.
     */
    void subscribe(String productId, ProductObserver observer);

    /**
     * Removes a subscription. An absent subscription is a no-op.
     */
    void unsubscribe(String productId, ProductObserver observer);
}
