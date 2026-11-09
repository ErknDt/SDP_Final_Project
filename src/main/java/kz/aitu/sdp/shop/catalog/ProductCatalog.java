package kz.aitu.sdp.shop.catalog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import kz.aitu.sdp.shop.common.Money;
import kz.aitu.sdp.shop.contracts.Catalog;
import kz.aitu.sdp.shop.contracts.ProductObserver;

public final class ProductCatalog implements Catalog {
    private static final Logger LOGGER = Logger.getLogger(ProductCatalog.class.getName());
    private final Map<String, ProductEntry> products = new LinkedHashMap<>();

    @Override
    public void addProduct(String productId, BigDecimal unitPrice, int stock) {
        requireId(productId);
        BigDecimal price = Money.requireValid(unitPrice);
        requireStock(stock);
        if (products.containsKey(productId)) {
            throw new IllegalArgumentException("Product already exists: " + productId);
        }
        products.put(productId, new ProductEntry(price, stock));
    }

    @Override
    public BigDecimal getUnitPrice(String productId) {
        return entry(productId).price;
    }

    @Override
    public int getStock(String productId) {
        return entry(productId).stock;
    }

    @Override
    public void updatePrice(String productId, BigDecimal unitPrice) {
        ProductEntry product = entry(productId);
        product.price = Money.requireValid(unitPrice);
        notifyObservers(productId);
    }

    @Override
    public void updateStock(String productId, int stock) {
        ProductEntry product = entry(productId);
        requireStock(stock);
        product.stock = stock;
        notifyObservers(productId);
    }

    @Override
    public void decreaseStock(Map<String, Integer> quantities) {
        if (quantities == null || quantities.isEmpty()) {
            throw new IllegalArgumentException("Order quantities are required.");
        }
        Map<String, Integer> requested = new LinkedHashMap<>(quantities);
        for (Map.Entry<String, Integer> item : requested.entrySet()) {
            ProductEntry product = entry(item.getKey());
            if (item.getValue() == null || item.getValue() <= 0) {
                throw new IllegalArgumentException("Order quantities must be positive.");
            }
            if (product.stock < item.getValue()) {
                throw new IllegalStateException("Insufficient stock for " + item.getKey());
            }
        }
        for (Map.Entry<String, Integer> item : requested.entrySet()) {
            entry(item.getKey()).stock -= item.getValue();
        }
        for (String productId : requested.keySet()) {
            notifyObservers(productId);
        }
    }

    @Override
    public void subscribe(String productId, ProductObserver observer) {
        ProductEntry product = entry(productId);
        requireObserver(observer);
        for (ProductObserver existing : product.observers) {
            if (existing == observer) {
                return;
            }
        }
        product.observers.add(observer);
    }

    @Override
    public void unsubscribe(String productId, ProductObserver observer) {
        ProductEntry product = entry(productId);
        requireObserver(observer);
        product.observers.removeIf(existing -> existing == observer);
    }

    private void notifyObservers(String productId) {
        ProductEntry product = entry(productId);
        BigDecimal price = product.price;
        int stock = product.stock;
        for (ProductObserver observer : List.copyOf(product.observers)) {
            try {
                observer.onProductChanged(productId, price, stock);
            } catch (RuntimeException exception) {
                LOGGER.log(Level.WARNING, "Observer failed for " + productId, exception);
            }
        }
    }

    private ProductEntry entry(String productId) {
        requireId(productId);
        ProductEntry product = products.get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Unknown product: " + productId);
        }
        return product;
    }

    private static void requireId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product identifier is required.");
        }
    }

    private static void requireStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stock must be nonnegative.");
        }
    }

    private static void requireObserver(ProductObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer is required.");
        }
    }

    private static final class ProductEntry {
        private BigDecimal price;
        private int stock;
        private final List<ProductObserver> observers = new ArrayList<>();

        private ProductEntry(BigDecimal price, int stock) {
            this.price = price;
            this.stock = stock;
        }
    }
}
