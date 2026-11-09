package kz.aitu.sdp.shop.cart;

import kz.aitu.sdp.shop.contracts.PurchasableItem;

public record CartItem(PurchasableItem item, int quantity) {
    public CartItem {
        if (item == null || quantity <= 0) {
            throw new IllegalArgumentException("A product and positive quantity are required.");
        }
    }
}
