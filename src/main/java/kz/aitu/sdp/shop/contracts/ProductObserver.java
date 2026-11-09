package kz.aitu.sdp.shop.contracts;

import java.math.BigDecimal;

/**
 * Receives the current product state after a catalog change.
 */
public interface ProductObserver {

    /**
     * Handles a synchronous notification for a subscribed product.
     * Callbacks receive the updated base price and available stock.
     */
    void onProductChanged(String productId, BigDecimal unitPrice, int stock);
}
