package cinema.payment;

import cinema.exception.PaymentException;

// [Member D] Pay with credit/debit card
public class CardPayment implements PaymentStrategy {
    public String getDisplayName() { return "Credit/Debit Card"; }

    public void pay(double amount) throws PaymentException {
        // TODO (D): ask for card number and validate it (16 digits), throw PaymentException if invalid
        System.out.println("Paid " + amount + " by card.");
    }
}
