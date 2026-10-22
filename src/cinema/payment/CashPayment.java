package cinema.payment;

import cinema.exception.PaymentException;
import cinema.util.ConsoleReader;
import cinema.util.Format;

// [Member D] Pay with cash at the cinema counter
public class CashPayment implements PaymentStrategy {
    public String getDisplayName() { return "Cash"; }

    public void pay(double amount) throws PaymentException {
        double tendered = ConsoleReader.readDouble("Enter cash amount tendered (PHP): ");
        // Compare in whole centavos so floating-point error can't reject exact cash.
        long dueCents = Math.round(amount * 100);
        long tenderedCents = Math.round(tendered * 100);
        if (tenderedCents < dueCents) {
            throw new PaymentException("Insufficient cash. Required: " + Format.peso(amount)
                    + ", provided: " + Format.peso(tendered));
        }
        System.out.println("  Paid " + Format.peso(amount) + " in cash.");
        if (tenderedCents > dueCents) {
            System.out.println("  Change: " + Format.peso((tenderedCents - dueCents) / 100.0));
        }
    }

    @Override
    public void refund(double amount) {
        System.out.println("  A cash refund of " + Format.peso(amount) + " is available at the cinema counter.");
    }
}