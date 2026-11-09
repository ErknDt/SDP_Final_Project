package kz.aitu.sdp.shop.checkout;

import java.util.LinkedHashMap;
import java.util.Map;
import kz.aitu.sdp.shop.cart.CartQuote;
import kz.aitu.sdp.shop.cart.OrderLine;
import kz.aitu.sdp.shop.cart.ShoppingCart;
import kz.aitu.sdp.shop.contracts.Catalog;
import kz.aitu.sdp.shop.contracts.PaymentGateway;

public final class CheckoutFacade {
    private final Catalog catalog;
    private final PaymentGateway payment;
    private boolean checkoutInProgress;

    public CheckoutFacade(Catalog catalog, PaymentGateway payment) {
        if (catalog == null || payment == null) {
            throw new IllegalArgumentException("Catalog and payment gateway are required.");
        }
        this.catalog = catalog;
        this.payment = payment;
    }

    public OrderResult placeOrder(ShoppingCart cart) {
        if (cart == null) {
            throw new IllegalArgumentException("Cart is required.");
        }
        if (checkoutInProgress || cart.isEmpty()) {
            throw new IllegalStateException("Checkout requires an available facade and a nonempty cart.");
        }
        checkoutInProgress = true;
        try {
            CartQuote quote = cart.quote(catalog);
            Map<String, Integer> quantities = quantities(quote);
            for (Map.Entry<String, Integer> item : quantities.entrySet()) {
                if (catalog.getStock(item.getKey()) < item.getValue()) {
                    throw new IllegalStateException("Insufficient stock for " + item.getKey());
                }
            }
            if (!payment.pay(quote.total())) {
                return new OrderResult(false, "Payment declined. The cart is ready for another attempt.", quote);
            }
            catalog.decreaseStock(quantities);
            cart.clear();
            return new OrderResult(true, "Order confirmed.", quote);
        } finally {
            checkoutInProgress = false;
        }
    }

    private Map<String, Integer> quantities(CartQuote quote) {
        Map<String, Integer> quantities = new LinkedHashMap<>();
        try {
            for (OrderLine line : quote.lines()) {
                quantities.merge(line.productId(), line.quantity(), Math::addExact);
            }
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Total product quantity is too large.", exception);
        }
        return quantities;
    }
}
