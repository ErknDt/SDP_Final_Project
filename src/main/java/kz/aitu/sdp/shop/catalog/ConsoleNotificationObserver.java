package kz.aitu.sdp.shop.catalog;

import java.io.PrintStream;
import java.math.BigDecimal;
import kz.aitu.sdp.shop.contracts.ProductObserver;

public final class ConsoleNotificationObserver implements ProductObserver {
    private final PrintStream output;

    public ConsoleNotificationObserver(PrintStream output) {
        if (output == null) {
            throw new IllegalArgumentException("Output is required.");
        }
        this.output = output;
    }

    @Override
    public void onProductChanged(String productId, BigDecimal unitPrice, int stock) {
        output.println("[UPDATE] " + productId + " | base price " + unitPrice
                + " KZT | stock " + stock);
    }
}
