package kz.aitu.sdp.shop.product;

import java.math.BigDecimal;
import kz.aitu.sdp.shop.contracts.PurchasableItem;

public final class GiftWrapDecorator extends ProductDecorator {
    private static final BigDecimal PRICE = new BigDecimal("3000.00");

    public GiftWrapDecorator(PurchasableItem wrapped) {
        super(wrapped);
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + gift wrap";
    }

    @Override
    public BigDecimal getAdditionalPrice() {
        return wrapped.getAdditionalPrice().add(PRICE);
    }
}
