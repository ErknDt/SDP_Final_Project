package kz.aitu.sdp.shop.cart;

import java.math.BigDecimal;
import kz.aitu.sdp.shop.catalog.ProductCatalog;
import kz.aitu.sdp.shop.discount.NoDiscountStrategy;
import kz.aitu.sdp.shop.discount.PercentageDiscountStrategy;
import kz.aitu.sdp.shop.product.GiftWrapDecorator;
import kz.aitu.sdp.shop.product.Laptop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest {
    private ProductCatalog catalog;
    private ShoppingCart cart;

    @BeforeEach
    void createCart() {
        catalog = new ProductCatalog();
        catalog.addProduct("A", new BigDecimal("100000.00"), 5);
        cart = new ShoppingCart(new NoDiscountStrategy());
        cart.addItem(new GiftWrapDecorator(new Laptop.Builder("A", "Laptop").build()), 2);
    }

    @Test
    void quoteCombinesCatalogPriceSurchargesAndQuantity() {
        assertEquals(new BigDecimal("206000.00"), cart.quote(catalog).subtotal());
    }

    @Test
    void replacementStrategyChangesTotalOnSameCart() {
        cart.setStrategy(new PercentageDiscountStrategy(10));

        assertEquals(new BigDecimal("185400.00"), cart.quote(catalog).total());
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void livePricesUpdateNewQuoteAndPreserveOldQuote() {
        CartQuote previous = cart.quote(catalog);

        catalog.updatePrice("A", new BigDecimal("90000.00"));

        assertEquals(new BigDecimal("206000.00"), previous.total());
        assertEquals(new BigDecimal("186000.00"), cart.quote(catalog).total());
    }

    @Test
    void rejectedStrategyPreservesCurrentBehavior() {
        assertThrows(IllegalArgumentException.class, () -> cart.setStrategy(null));

        assertEquals(new BigDecimal("206000.00"), cart.quote(catalog).total());
    }

    @Test
    void cartAndQuoteCollectionsAreProtected() {
        assertThrows(UnsupportedOperationException.class, () -> cart.getItems().clear());
        assertThrows(UnsupportedOperationException.class, () -> cart.quote(catalog).lines().clear());
    }

    @Test
    void invalidQuantityPreservesOriginalCartLine() {
        assertThrows(IllegalArgumentException.class, () -> cart.updateQuantity(0, 0));

        assertEquals(2, cart.getItems().get(0).quantity());
    }

    @Test
    void cartSupportsQuantityChangesAndRemoval() {
        cart.updateQuantity(0, 1);
        assertEquals(new BigDecimal("103000.00"), cart.quote(catalog).total());

        cart.removeItem(0);

        assertTrue(cart.isEmpty());
        assertEquals(new BigDecimal("0.00"), cart.quote(catalog).total());
    }

    @Test
    void invalidStrategyResultIsRejectedBeforeReturningQuote() {
        cart.setStrategy(subtotal -> subtotal.add(BigDecimal.ONE));

        assertThrows(IllegalArgumentException.class, () -> cart.quote(catalog));
    }
}
