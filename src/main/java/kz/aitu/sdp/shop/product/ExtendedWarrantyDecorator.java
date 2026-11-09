package kz.aitu.sdp.shop.product;

import java.math.BigDecimal;
import kz.aitu.sdp.shop.contracts.PurchasableItem;

public final class ExtendedWarrantyDecorator extends ProductDecorator {
    private static final BigDecimal PRICE = new BigDecimal("25000.00");

    public ExtendedWarrantyDecorator(PurchasableItem wrapped) {
        super(wrapped);
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + extended warranty";
    }

    @Override
    public BigDecimal getAdditionalPrice() {
        return wrapped.getAdditionalPrice().add(PRICE);
    }
}
