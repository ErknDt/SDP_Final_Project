package kz.aitu.sdp.shop.cart;

import java.math.BigDecimal;
import kz.aitu.sdp.shop.common.Money;

public record OrderLine(String productId, String description, int quantity, BigDecimal unitPrice) {
    public OrderLine {
        if (productId == null || productId.isBlank() || description == null
                || description.isBlank() || quantity <= 0) {
            throw new IllegalArgumentException("Order line data is invalid.");
        }
        unitPrice = Money.requireValid(unitPrice);
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
