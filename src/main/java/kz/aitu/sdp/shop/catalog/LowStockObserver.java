package kz.aitu.sdp.shop.catalog;

import java.io.PrintStream;
import java.math.BigDecimal;
import kz.aitu.sdp.shop.contracts.ProductObserver;

public final class LowStockObserver implements ProductObserver {
    private final int threshold;
    private final PrintStream output;

    public LowStockObserver(int threshold, PrintStream output) {
        if (threshold < 0 || output == null) {
            throw new IllegalArgumentException("A nonnegative threshold and output are required.");
        }
        this.threshold = threshold;
        this.output = output;
    }

    @Override
    public void onProductChanged(String productId, BigDecimal unitPrice, int stock) {
        if (stock <= threshold) {
            output.println("[LOW STOCK] " + productId + " has " + stock + " units left.");
        }
    }
}
