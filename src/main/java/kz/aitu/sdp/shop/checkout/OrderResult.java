package kz.aitu.sdp.shop.checkout;

import kz.aitu.sdp.shop.cart.CartQuote;

public record OrderResult(boolean successful, String message, CartQuote quote) {
    public OrderResult {
        if (message == null || message.isBlank() || quote == null) {
            throw new IllegalArgumentException("An order message and quote are required.");
        }
    }
}
