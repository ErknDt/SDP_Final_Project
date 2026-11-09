package kz.aitu.sdp.shop.catalog;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kz.aitu.sdp.shop.contracts.ProductObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductCatalogTest {
    private ProductCatalog catalog;

    @BeforeEach
    void createCatalog() {
        catalog = new ProductCatalog();
        catalog.addProduct("A", new BigDecimal("100.00"), 5);
        catalog.addProduct("B", new BigDecimal("200.00"), 1);
    }

    @Test
    void priceChangesNotifyAllSubscribersWithUpdatedState() {
        List<String> calls = new ArrayList<>();
        catalog.subscribe("A", (id, price, stock) -> calls.add("first " + price + " " + stock));
        catalog.subscribe("A", (id, price, stock) -> calls.add("second " + price + " " + stock));

        catalog.updatePrice("A", new BigDecimal("90.00"));

        assertEquals(List.of("first 90.00 5", "second 90.00 5"), calls);
    }

    @Test
    void duplicateSubscriptionIsDeliveredOnce() {
        List<Integer> calls = new ArrayList<>();
        ProductObserver observer = (id, price, stock) -> calls.add(stock);
        catalog.subscribe("A", observer);
        catalog.subscribe("A", observer);

        catalog.updateStock("A", 4);

        assertEquals(List.of(4), calls);
    }

    @Test
    void unsubscribeStopsFutureNotifications() {
        List<Integer> calls = new ArrayList<>();
        ProductObserver observer = (id, price, stock) -> calls.add(stock);
        catalog.subscribe("A", observer);
        catalog.unsubscribe("A", observer);

        catalog.updateStock("A", 4);

        assertTrue(calls.isEmpty());
    }

    @Test
    void observerCanUnsubscribeDuringCallback() {
        List<Integer> calls = new ArrayList<>();
        ProductObserver observer = new ProductObserver() {
            @Override
            public void onProductChanged(String id, BigDecimal price, int stock) {
                calls.add(stock);
                catalog.unsubscribe(id, this);
            }
        };
        catalog.subscribe("A", observer);

        catalog.updateStock("A", 4);
        catalog.updateStock("A", 3);

        assertEquals(List.of(4), calls);
    }

    @Test
    void failingObserverPreservesUpdateAndOtherNotifications() {
        List<Integer> calls = new ArrayList<>();
        catalog.subscribe("A", (id, price, stock) -> { throw new IllegalStateException("Listener failed"); });
        catalog.subscribe("A", (id, price, stock) -> calls.add(stock));

        assertDoesNotThrow(() -> catalog.updateStock("A", 4));

        assertEquals(4, catalog.getStock("A"));
        assertEquals(List.of(4), calls);
    }

    @Test
    void insufficientBulkOrderChangesNoStockAndSendsNoEvents() {
        List<Integer> calls = new ArrayList<>();
        catalog.subscribe("A", (id, price, stock) -> calls.add(stock));
        Map<String, Integer> quantities = new LinkedHashMap<>();
        quantities.put("A", 2);
        quantities.put("B", 2);

        assertThrows(IllegalStateException.class, () -> catalog.decreaseStock(quantities));

        assertEquals(5, catalog.getStock("A"));
        assertEquals(1, catalog.getStock("B"));
        assertTrue(calls.isEmpty());
    }

    @Test
    void bulkOrderCommitsAllStockBeforeNotifications() {
        List<Integer> otherStock = new ArrayList<>();
        catalog.subscribe("A", (id, price, stock) -> otherStock.add(catalog.getStock("B")));

        catalog.decreaseStock(Map.of("A", 2, "B", 1));

        assertEquals(3, catalog.getStock("A"));
        assertEquals(List.of(0), otherStock);
    }

    @Test
    void rejectedPriceUpdatePreservesOldPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> catalog.updatePrice("A", new BigDecimal("1.001")));

        assertEquals(new BigDecimal("100.00"), catalog.getUnitPrice("A"));
    }

    @Test
    void invalidCatalogInputIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> catalog.getStock("missing"));
        assertThrows(IllegalArgumentException.class, () -> catalog.updateStock("A", -1));
        assertThrows(IllegalArgumentException.class, () -> catalog.subscribe("A", null));
        assertThrows(IllegalArgumentException.class, () -> catalog.decreaseStock(Map.of("A", 0)));
        assertThrows(IllegalArgumentException.class,
                () -> catalog.addProduct("A", BigDecimal.ZERO, 0));
    }

    @Test
    void subscriptionsAreSpecificToTheirProduct() {
        List<String> calls = new ArrayList<>();
        catalog.subscribe("A", (id, price, stock) -> calls.add(id));

        catalog.updateStock("B", 0);

        assertTrue(calls.isEmpty());
    }

    @Test
    void lowStockObserverIncludesThresholdBoundary() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        LowStockObserver observer = new LowStockObserver(3, new PrintStream(bytes));

        observer.onProductChanged("A", BigDecimal.ZERO, 4);
        assertEquals("", bytes.toString());
        observer.onProductChanged("A", BigDecimal.ZERO, 3);
        assertTrue(bytes.toString().contains("3 units left"));
    }
}
