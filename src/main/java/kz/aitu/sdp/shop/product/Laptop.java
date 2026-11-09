package kz.aitu.sdp.shop.product;

import java.math.BigDecimal;
import java.util.List;
import kz.aitu.sdp.shop.contracts.PurchasableItem;

public final class Laptop implements PurchasableItem {
    private final String productId;
    private final String model;
    private final int ramGb;
    private final int storageGb;
    private final String color;

    private Laptop(Builder builder) {
        productId = requireText(builder.productId);
        model = requireText(builder.model);
        color = requireText(builder.color);
        if (!List.of(8, 16, 32).contains(builder.ramGb)) {
            throw new IllegalArgumentException("RAM must be 8, 16 or 32 GB.");
        }
        if (!List.of(256, 512, 1024).contains(builder.storageGb)) {
            throw new IllegalArgumentException("Storage must be 256, 512 or 1024 GB.");
        }
        ramGb = builder.ramGb;
        storageGb = builder.storageGb;
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Product identifier, model and color must be provided.");
        }
        return value.strip();
    }

    @Override
    public String getProductId() {
        return productId;
    }

    @Override
    public String getDescription() {
        return model + ", " + ramGb + " GB RAM, " + storageGb + " GB SSD, " + color;
    }

    @Override
    public BigDecimal getAdditionalPrice() {
        int ramPrice = switch (ramGb) {
            case 16 -> 20000;
            case 32 -> 50000;
            default -> 0;
        };
        int storagePrice = switch (storageGb) {
            case 512 -> 15000;
            case 1024 -> 35000;
            default -> 0;
        };
        return BigDecimal.valueOf(ramPrice + storagePrice).setScale(2);
    }

    public static final class Builder {
        private final String productId;
        private final String model;
        private int ramGb = 8;
        private int storageGb = 256;
        private String color = "Silver";

        public Builder(String productId, String model) {
            this.productId = productId;
            this.model = model;
        }

        public Builder ramGb(int ramGb) {
            this.ramGb = ramGb;
            return this;
        }

        public Builder storageGb(int storageGb) {
            this.storageGb = storageGb;
            return this;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public Laptop build() {
            return new Laptop(this);
        }
    }
}
