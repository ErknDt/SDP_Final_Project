package kz.aitu.sdp.shop.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShopApplicationTest {
    @Test
    void deterministicDemoExercisesCompletePurchaseAndDecline() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ShopApplication app = application("", bytes);

        app.runDemo();

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Total 617400.00 KZT"));
        assertTrue(output.contains("Stock after declined payment: 5"));
        assertTrue(output.contains("Stock after successful payment: 3"));
        assertTrue(output.contains("Cart lines after checkout: 0"));
        assertTrue(output.contains("61740000 tiyn"));
    }

    @Test
    void interactiveMenuSupportsConfigurationDiscountAndCheckout() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        String commands = "2\n1\n16\n512\nBlack\ny\ny\n2\n4\n2\n7\n0\n";

        application(commands, bytes).run();

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Order confirmed."));
        assertTrue(output.contains("Total 653400.00 KZT"));
        assertTrue(output.contains("Goodbye."));
    }

    @Test
    void invalidInputAndEndOfInputAreHandled() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        application("bad\n7\n", bytes).run();

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Cannot complete action:"));
        assertTrue(output.contains("Input ended."));
        assertFalse(output.contains("Exception in thread"));
    }

    private ShopApplication application(String commands, ByteArrayOutputStream output) {
        return new ShopApplication(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8));
    }
}
