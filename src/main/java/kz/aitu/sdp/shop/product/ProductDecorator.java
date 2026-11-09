package kz.aitu.sdp.shop.product;

import kz.aitu.sdp.shop.contracts.PurchasableItem;

public abstract class ProductDecorator implements PurchasableItem {
    protected final PurchasableItem wrapped;

    protected ProductDecorator(PurchasableItem wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("A wrapped product is required.");
        }
        this.wrapped = wrapped;
    }

    @Override
    public String getProductId() {
        return wrapped.getProductId();
    }
}
