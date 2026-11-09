package kz.aitu.sdp.shop.app;

import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import kz.aitu.sdp.shop.cart.CartQuote;
import kz.aitu.sdp.shop.cart.OrderLine;
import kz.aitu.sdp.shop.cart.ShoppingCart;
import kz.aitu.sdp.shop.catalog.ConsoleNotificationObserver;
import kz.aitu.sdp.shop.catalog.LowStockObserver;
import kz.aitu.sdp.shop.catalog.ProductCatalog;
import kz.aitu.sdp.shop.checkout.CheckoutFacade;
import kz.aitu.sdp.shop.checkout.OrderResult;
import kz.aitu.sdp.shop.contracts.PurchasableItem;
import kz.aitu.sdp.shop.discount.NoDiscountStrategy;
import kz.aitu.sdp.shop.discount.PercentageDiscountStrategy;
import kz.aitu.sdp.shop.discount.ThresholdDiscountStrategy;
import kz.aitu.sdp.shop.payment.LegacyPaymentService;
import kz.aitu.sdp.shop.payment.PaymentAdapter;
import kz.aitu.sdp.shop.product.ExtendedWarrantyDecorator;
import kz.aitu.sdp.shop.product.GiftWrapDecorator;
import kz.aitu.sdp.shop.product.Laptop;

public final class ShopApplication {
    private final Scanner input;
    private final PrintStream output;
    private final ProductCatalog catalog = new ProductCatalog();
    private final LegacyPaymentService paymentService = new LegacyPaymentService();
    private final ShoppingCart cart = new ShoppingCart(new NoDiscountStrategy());
    private final CheckoutFacade checkout = new CheckoutFacade(catalog, new PaymentAdapter(paymentService));

    public ShopApplication(InputStream input, PrintStream output) {
        if (input == null || output == null) {
            throw new IllegalArgumentException("Input and output are required.");
        }
        this.input = new Scanner(input);
        this.output = output;
        catalog.addProduct("CAMPUS", new BigDecimal("300000.00"), 5);
        catalog.addProduct("PRO", new BigDecimal("500000.00"), 2);
        ConsoleNotificationObserver notifications = new ConsoleNotificationObserver(output);
        LowStockObserver lowStock = new LowStockObserver(3, output);
        for (String productId : List.of("CAMPUS", "PRO")) {
            catalog.subscribe(productId, notifications);
            catalog.subscribe(productId, lowStock);
        }
    }

    public static void main(String[] args) {
        ShopApplication application = new ShopApplication(System.in, System.out);
        if (args.length == 1 && "--demo".equals(args[0])) {
            application.runDemo();
        } else {
            application.run();
        }
    }

    public void run() {
        output.println("LAPTOP SHOP | KZT");
        while (true) {
            printMenu();
            try {
                int choice = readInt("Choice: ");
                if (choice == 0) {
                    output.println("Goodbye.");
                    return;
                }
                handleChoice(choice);
            } catch (NoSuchElementException exception) {
                output.println("Input ended.");
                return;
            } catch (IllegalArgumentException | IllegalStateException exception) {
                output.println("Cannot complete action: " + exception.getMessage());
            }
        }
    }

    private void printMenu() {
        output.println();
        output.println("1 Catalog   2 Configure and add   3 View cart   4 Choose discount");
        output.println("5 Change price   6 Change stock   7 Checkout   8 Decline next payment");
        output.println("9 Remove line   10 Change quantity   0 Exit");
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1 -> printCatalog();
            case 2 -> configureAndAdd();
            case 3 -> printQuote(cart.quote(catalog));
            case 4 -> chooseDiscount();
            case 5 -> catalog.updatePrice(readProductId(), readMoney("New base price: "));
            case 6 -> catalog.updateStock(readProductId(), readInt("New stock: "));
            case 7 -> printOrder(checkout.placeOrder(cart));
            case 8 -> {
                paymentService.declineNextPayment();
                output.println("The next payment will be declined for demonstration.");
            }
            case 9 -> cart.removeItem(readInt("Line number: ") - 1);
            case 10 -> cart.updateQuantity(readInt("Line number: ") - 1, readInt("Quantity: "));
            default -> throw new IllegalArgumentException("Choose a menu option from 0 to 10.");
        }
    }

    private void configureAndAdd() {
        String productId = readProductId();
        String model = "CAMPUS".equals(productId) ? "Campus Laptop" : "Pro Laptop";
        PurchasableItem laptop = new Laptop.Builder(productId, model)
                .ramGb(readInt("RAM GB (8/16/32): "))
                .storageGb(readInt("SSD GB (256/512/1024): "))
                .color(readText("Color: "))
                .build();
        if (readYes("Gift wrap for 3000.00 KZT? (y/n): ")) {
            laptop = new GiftWrapDecorator(laptop);
        }
        if (readYes("Extended warranty for 25000.00 KZT? (y/n): ")) {
            laptop = new ExtendedWarrantyDecorator(laptop);
        }
        cart.addItem(laptop, readInt("Quantity: "));
        output.println("Added " + laptop.getDescription());
    }

    private void chooseDiscount() {
        output.println("1 None | 2 Ten percent | 3 Save 50000 on orders from 700000 KZT");
        switch (readInt("Discount: ")) {
            case 1 -> cart.setStrategy(new NoDiscountStrategy());
            case 2 -> cart.setStrategy(new PercentageDiscountStrategy(10));
            case 3 -> cart.setStrategy(new ThresholdDiscountStrategy(
                    new BigDecimal("700000.00"), new BigDecimal("50000.00")));
            default -> throw new IllegalArgumentException("Choose discount 1, 2 or 3.");
        }
        printQuote(cart.quote(catalog));
    }

    private void printCatalog() {
        for (String productId : List.of("CAMPUS", "PRO")) {
            output.println(productId + " | base price " + catalog.getUnitPrice(productId)
                    + " KZT | stock " + catalog.getStock(productId));
        }
        output.println("Upgrades: RAM 16 +20000, RAM 32 +50000, SSD 512 +15000, SSD 1024 +35000 KZT.");
    }

    private void printQuote(CartQuote quote) {
        int lineNumber = 1;
        for (OrderLine line : quote.lines()) {
            output.println(lineNumber++ + ". " + line.description());
            output.println("   " + line.quantity() + " x " + line.unitPrice()
                    + " = " + line.lineTotal() + " KZT");
        }
        output.println("Subtotal " + quote.subtotal() + " | Discount " + quote.discount()
                + " | Total " + quote.total() + " KZT");
    }

    private void printOrder(OrderResult result) {
        output.println(result.message());
        printQuote(result.quote());
        output.println("Cart lines after checkout: " + cart.getItems().size());
    }

    private String readProductId() {
        int choice = readInt("Model (1 Campus / 2 Pro): ");
        return switch (choice) {
            case 1 -> "CAMPUS";
            case 2 -> "PRO";
            default -> throw new IllegalArgumentException("Choose model 1 or 2.");
        };
    }

    private String readText(String prompt) {
        output.print(prompt);
        return input.nextLine().strip();
    }

    private int readInt(String prompt) {
        return Integer.parseInt(readText(prompt));
    }

    private BigDecimal readMoney(String prompt) {
        return new BigDecimal(readText(prompt));
    }

    private boolean readYes(String prompt) {
        String answer = readText(prompt);
        if (!"y".equalsIgnoreCase(answer) && !"n".equalsIgnoreCase(answer)) {
            throw new IllegalArgumentException("Answer y or n.");
        }
        return "y".equalsIgnoreCase(answer);
    }

    public void runDemo() {
        output.println("LAPTOP SHOP DEMO | KZT");
        output.println("\n1. Configure a laptop and add services");
        PurchasableItem laptop = new Laptop.Builder("CAMPUS", "Campus Laptop")
                .ramGb(16).storageGb(512).color("Black").build();
        laptop = new ExtendedWarrantyDecorator(new GiftWrapDecorator(laptop));
        cart.addItem(laptop, 2);
        printQuote(cart.quote(catalog));

        output.println("\n2. Compare discounts on the same cart");
        cart.setStrategy(new ThresholdDiscountStrategy(
                new BigDecimal("700000.00"), new BigDecimal("50000.00")));
        printQuote(cart.quote(catalog));
        cart.setStrategy(new PercentageDiscountStrategy(10));
        printQuote(cart.quote(catalog));

        output.println("\n3. Update the catalog price");
        catalog.updatePrice("CAMPUS", new BigDecimal("280000.00"));
        printQuote(cart.quote(catalog));

        output.println("\n4. Decline a payment, then retry checkout");
        paymentService.declineNextPayment();
        printOrder(checkout.placeOrder(cart));
        output.println("Stock after declined payment: " + catalog.getStock("CAMPUS"));
        printOrder(checkout.placeOrder(cart));
        output.println("Stock after successful payment: " + catalog.getStock("CAMPUS"));
        output.println("Payment adapter sent " + paymentService.getLastAmountInTiyn() + " tiyn.");
    }
}
