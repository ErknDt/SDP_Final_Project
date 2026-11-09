package kz.aitu.sdp.shop.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import kz.aitu.sdp.shop.common.Money;
import kz.aitu.sdp.shop.contracts.Catalog;
import kz.aitu.sdp.shop.contracts.DiscountStrategy;
import kz.aitu.sdp.shop.contracts.PurchasableItem;

public final class ShoppingCart {
    private final List<CartItem> items = new ArrayList<>();
    private DiscountStrategy strategy;

    public ShoppingCart(DiscountStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(DiscountStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Discount strategy is required.");
        }
        this.strategy = strategy;
    }

    public void addItem(PurchasableItem item, int quantity) {
        items.add(new CartItem(item, quantity));
    }

    public void removeItem(int index) {
        requireIndex(index);
        items.remove(index);
    }

    public void updateQuantity(int index, int quantity) {
        requireIndex(index);
        items.set(index, new CartItem(items.get(index).item(), quantity));
    }

    public List<CartItem> getItems() {
        return List.copyOf(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }

    public CartQuote quote(Catalog catalog) {
        if (catalog == null) {
            throw new IllegalArgumentException("Catalog is required.");
        }
        List<OrderLine> lines = new ArrayList<>();
        BigDecimal subtotal = new BigDecimal("0.00");
        for (CartItem entry : items) {
            PurchasableItem item = entry.item();
            BigDecimal basePrice = Money.requireValid(catalog.getUnitPrice(item.getProductId()));
            BigDecimal additionalPrice = Money.requireValid(item.getAdditionalPrice());
            OrderLine line = new OrderLine(item.getProductId(), item.getDescription(),
                    entry.quantity(), basePrice.add(additionalPrice));
            lines.add(line);
            subtotal = subtotal.add(line.lineTotal());
        }
        BigDecimal discount = Money.requireValid(strategy.calculateDiscount(subtotal));
        if (discount.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("Discount cannot exceed subtotal.");
        }
        return new CartQuote(lines, subtotal, discount, subtotal.subtract(discount));
    }

    private void requireIndex(int index) {
        if (index < 0 || index >= items.size()) {
            throw new IllegalArgumentException("Choose an existing cart line.");
        }
    }
}
