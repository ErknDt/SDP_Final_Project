package kz.aitu.sdp.shop.payment;

import java.math.BigDecimal;
import kz.aitu.sdp.shop.common.Money;
import kz.aitu.sdp.shop.contracts.PaymentGateway;

public final class PaymentAdapter implements PaymentGateway {
    private final LegacyPaymentService service;

    public PaymentAdapter(LegacyPaymentService service) {
        if (service == null) {
            throw new IllegalArgumentException("Payment service is required.");
        }
        this.service = service;
    }

    @Override
    public boolean pay(BigDecimal amount) {
        BigDecimal validAmount = Money.requireValid(amount);
        long minorUnits;
        try {
            minorUnits = validAmount.movePointRight(2).longValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Payment amount is too large.", exception);
        }
        return "APPROVED".equals(service.charge(minorUnits, "KZT"));
    }
}
