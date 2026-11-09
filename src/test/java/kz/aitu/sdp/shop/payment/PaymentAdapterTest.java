package kz.aitu.sdp.shop.payment;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentAdapterTest {
    @Test
    void adapterConvertsDecimalAmountToExactMinorUnits() {
        LegacyPaymentService service = new LegacyPaymentService();
        PaymentAdapter adapter = new PaymentAdapter(service);

        assertTrue(adapter.pay(new BigDecimal("123.45")));

        assertEquals(12345L, service.getLastAmountInTiyn());
        assertEquals(1, service.getAttempts());
    }

    @Test
    void declineIsReturnedAndNextPaymentCanSucceed() {
        LegacyPaymentService service = new LegacyPaymentService();
        PaymentAdapter adapter = new PaymentAdapter(service);
        service.declineNextPayment();

        assertFalse(adapter.pay(new BigDecimal("100")));
        assertTrue(adapter.pay(new BigDecimal("100")));
        assertEquals(2, service.getAttempts());
    }

    @Test
    void invalidAmountNeverReachesLegacyService() {
        LegacyPaymentService service = new LegacyPaymentService();
        PaymentAdapter adapter = new PaymentAdapter(service);

        assertThrows(IllegalArgumentException.class, () -> adapter.pay(new BigDecimal("0.001")));
        assertThrows(IllegalArgumentException.class, () -> adapter.pay(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> adapter.pay(null));
        assertEquals(0, service.getAttempts());
    }

    @Test
    void numericOverflowIsRejectedBeforeCharging() {
        LegacyPaymentService service = new LegacyPaymentService();
        PaymentAdapter adapter = new PaymentAdapter(service);

        assertThrows(IllegalArgumentException.class,
                () -> adapter.pay(new BigDecimal("92233720368547758.08")));
        assertEquals(0, service.getAttempts());
    }

    @Test
    void zeroAmountIsSupported() {
        LegacyPaymentService service = new LegacyPaymentService();

        assertTrue(new PaymentAdapter(service).pay(BigDecimal.ZERO));
        assertEquals(0, service.getLastAmountInTiyn());
    }
}
