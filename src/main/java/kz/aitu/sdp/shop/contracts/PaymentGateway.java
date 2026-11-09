package kz.aitu.sdp.shop.contracts;

import java.math.BigDecimal;

/**
 * The payment interface used by checkout and implemented by a payment adapter.
 */
public interface PaymentGateway {

    /**
     * Attempts one payment in the project's shared currency, KZT.
     *
     * @param amount the nonnull, nonnegative amount with at most two decimal places
     * @return true when payment succeeds, or false when payment is declined
     * @throws IllegalArgumentException if the amount is invalid
     */
    boolean pay(BigDecimal amount);
}
