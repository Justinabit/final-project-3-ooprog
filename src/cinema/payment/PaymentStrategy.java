package cinema.payment;

import cinema.exception.PaymentException;

// [Member D] STRATEGY PATTERN #1: different ways to pay
public interface PaymentStrategy {
    String getDisplayName();
    void pay(double amount) throws PaymentException;
}
