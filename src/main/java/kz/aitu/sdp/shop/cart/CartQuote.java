package kz.aitu.sdp.shop.cart;

import java.math.BigDecimal;
import java.util.List;
import kz.aitu.sdp.shop.common.Money;

public record CartQuote(List<OrderLine> lines, BigDecimal subtotal,
                        BigDecimal discount, BigDecimal total) {
    public CartQuote {
        if (lines == null) {
            throw new IllegalArgumentException("Order lines are required.");
        }
        lines = List.copyOf(lines);
        subtotal = Money.requireValid(subtotal);
        discount = Money.requireValid(discount);
        total = Money.requireValid(total);
        BigDecimal lineSum = new BigDecimal("0.00");
        for (OrderLine line : lines) {
            lineSum = lineSum.add(line.lineTotal());
        }
        if (lineSum.compareTo(subtotal) != 0
                || subtotal.subtract(discount).compareTo(total) != 0) {
            throw new IllegalArgumentException("Quote amounts do not match its lines and discount.");
        }
    }
}
