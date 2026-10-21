package cinema.payment;

import cinema.exception.PaymentException;

// [Member D] STRATEGY PATTERN #1: different ways to pay
public interface PaymentStrategy {
    String getDisplayName();
    void pay(double amount) throws PaymentException;

    // Money goes back the same way it came in. Subclasses can override the message.
    default void refund(double amount) {
        System.out.println(String.format("  Refunded $%.2f via %s.", amount, getDisplayName()));
    }
}
