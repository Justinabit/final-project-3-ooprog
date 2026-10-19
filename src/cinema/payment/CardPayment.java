package cinema.payment;

import cinema.exception.PaymentException;
import cinema.util.ConsoleReader;

// [Member D] Pay with credit/debit card
public class CardPayment implements PaymentStrategy {
    public String getDisplayName() { return "Credit/Debit Card"; }

    public void pay(double amount) throws PaymentException {
        String input = ConsoleReader.readLine("Enter 16-digit card number: ");
        String cardNumber = input.replaceAll("[ -]", "");   // allow "1234 5678 ..." or "1234-5678-..."
        if (!cardNumber.matches("\\d{16}")) {
            throw new PaymentException("Invalid card number. It must contain exactly 16 digits.");
        }
        System.out.println(String.format("Paid $%.2f by card ending in %s.", amount, cardNumber.substring(12)));
    }

    @Override
    public void refund(double amount) {
        System.out.println(String.format("Refunded $%.2f to the original card.", amount));
    }
}
