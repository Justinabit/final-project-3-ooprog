package cinema.payment;

import cinema.exception.PaymentException;
import cinema.util.Format;

// [Member D] STRATEGY PATTERN #1: different ways to pay
public interface PaymentStrategy {
    String getDisplayName();
    void pay(double amount) throws PaymentException;

    // Every payment method calls this FIRST, so no method can "pay" a nonsense amount (NaN, 0, negative).
    // Called as PaymentStrategy.validateAmount(amount) because static interface methods are not inherited.
    static void validateAmount(double amount) throws PaymentException {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new PaymentException("Invalid payment amount.");
        }
        if (amount <= 0) {
            throw new PaymentException("The payment amount must be more than " + Format.peso(0) + ".");
        }
    }

    // Money goes back the same way it came in. Subclasses can override the message.
    default void refund(double amount) {
        System.out.println("  Refunded " + Format.peso(amount) + " via " + getDisplayName() + ".");
    }
}