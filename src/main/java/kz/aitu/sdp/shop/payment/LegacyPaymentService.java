package kz.aitu.sdp.shop.payment;

public final class LegacyPaymentService {
    private boolean declineNext;
    private long lastAmountInTiyn;
    private int attempts;

    public String charge(long amountInTiyn, String currencyCode) {
        if (amountInTiyn < 0 || !"KZT".equals(currencyCode)) {
            throw new IllegalArgumentException("A nonnegative KZT payment is required.");
        }
        lastAmountInTiyn = amountInTiyn;
        attempts++;
        if (declineNext) {
            declineNext = false;
            return "DECLINED";
        }
        return "APPROVED";
    }

    public void declineNextPayment() {
        declineNext = true;
    }

    public long getLastAmountInTiyn() {
        return lastAmountInTiyn;
    }

    public int getAttempts() {
        return attempts;
    }
}
