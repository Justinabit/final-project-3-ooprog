package cinema.payment;

import cinema.exception.PaymentException;
import cinema.util.ConsoleReader;
import cinema.util.Format;

// [Member D] Pay with credit/debit card
public class CardPayment implements PaymentStrategy {
    public String getDisplayName() { return "Credit/Debit Card"; }

    public void pay(double amount) throws PaymentException {
        String input = ConsoleReader.readLine("Enter 16-digit card number: ");
        String cardNumber = input.replaceAll("[ -]", "");   // allow "1234 5678 ..." or "1234-5678-..."
        if (!cardNumber.matches("\\d{16}")) {
            throw new PaymentException("Invalid card number. It must contain exactly 16 digits.");
        }
        System.out.println("  Paid " + Format.peso(amount) + " by card ending in " + cardNumber.substring(12) + ".");
    }

    @Override
    public void refund(double amount) {
        System.out.println("  Refunded " + Format.peso(amount) + " to the original card (3-5 banking days).");
    }
}
