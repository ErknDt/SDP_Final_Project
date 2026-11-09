package kz.aitu.sdp.shop.checkout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import kz.aitu.sdp.shop.cart.ShoppingCart;
import kz.aitu.sdp.shop.catalog.ProductCatalog;
import kz.aitu.sdp.shop.discount.NoDiscountStrategy;
import kz.aitu.sdp.shop.payment.LegacyPaymentService;
import kz.aitu.sdp.shop.payment.PaymentAdapter;
import kz.aitu.sdp.shop.product.GiftWrapDecorator;
import kz.aitu.sdp.shop.product.Laptop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CheckoutFacadeTest {
    private ProductCatalog catalog;
    private ShoppingCart cart;
    private LegacyPaymentService payment;
    private CheckoutFacade facade;

    @BeforeEach
    void createCheckout() {
        catalog = new ProductCatalog();
        catalog.addProduct("A", new BigDecimal("100.00"), 5);
        cart = new ShoppingCart(new NoDiscountStrategy());
        cart.addItem(new Laptop.Builder("A", "Laptop").build(), 2);
        payment = new LegacyPaymentService();
        facade = new CheckoutFacade(catalog, new PaymentAdapter(payment));
    }

    @Test
    void successfulCheckoutChargesUpdatesStockAndClearsCart() {
        List<Integer> observedStock = new ArrayList<>();
        catalog.subscribe("A", (id, price, stock) -> observedStock.add(stock));

        OrderResult result = facade.placeOrder(cart);

        assertTrue(result.successful());
        assertEquals(new BigDecimal("200.00"), result.quote().total());
        assertEquals(20000, payment.getLastAmountInTiyn());
        assertEquals(List.of(3), observedStock);
        assertTrue(cart.isEmpty());
    }

    @Test
    void declinedCheckoutPreservesCartAndStockForRetry() {
        payment.declineNextPayment();

        assertFalse(facade.placeOrder(cart).successful());
        assertEquals(5, catalog.getStock("A"));
        assertFalse(cart.isEmpty());

        assertTrue(facade.placeOrder(cart).successful());
        assertEquals(3, catalog.getStock("A"));
        assertEquals(2, payment.getAttempts());
    }

    @Test
    void insufficientStockIsRejectedBeforePayment() {
        catalog.updateStock("A", 1);

        assertThrows(IllegalStateException.class, () -> facade.placeOrder(cart));

        assertEquals(0, payment.getAttempts());
        assertEquals(1, catalog.getStock("A"));
        assertFalse(cart.isEmpty());
    }

    @Test
    void differentConfigurationsShareTheSameStockLimit() {
        cart.addItem(new GiftWrapDecorator(new Laptop.Builder("A", "Laptop").build()), 4);

        assertThrows(IllegalStateException.class, () -> facade.placeOrder(cart));
        assertEquals(0, payment.getAttempts());
        assertEquals(5, catalog.getStock("A"));
    }

    @Test
    void repeatingSuccessfulCheckoutCannotChargeEmptyCart() {
        facade.placeOrder(cart);

        assertThrows(IllegalStateException.class, () -> facade.placeOrder(cart));
        assertEquals(1, payment.getAttempts());
    }

    @Test
    void observerFailureCannotTurnCommittedOrderIntoFailedCheckout() {
        catalog.subscribe("A", (id, price, stock) -> { throw new IllegalStateException("Listener failed"); });

        assertTrue(facade.placeOrder(cart).successful());

        assertEquals(1, payment.getAttempts());
        assertEquals(3, catalog.getStock("A"));
        assertTrue(cart.isEmpty());
    }

    @Test
    void aggregateQuantityOverflowIsRejectedBeforePayment() {
        cart.updateQuantity(0, Integer.MAX_VALUE);
        cart.addItem(new Laptop.Builder("A", "Laptop").build(), 1);

        assertThrows(IllegalArgumentException.class, () -> facade.placeOrder(cart));
        assertEquals(0, payment.getAttempts());
    }

    @Test
    void reentrantCheckoutIsRejectedDuringStockNotification() {
        List<Boolean> rejected = new ArrayList<>();
        catalog.subscribe("A", (id, price, stock) -> rejected.add(
                assertThrows(IllegalStateException.class, () -> facade.placeOrder(cart)) != null));

        facade.placeOrder(cart);

        assertEquals(List.of(true), rejected);
        assertEquals(1, payment.getAttempts());
    }
}
