package cinema.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import cinema.exception.PaymentException;
import cinema.util.ConsoleReader;
import cinema.util.Format;

// [Member D] Pay with cash at the cinema counter
public class CashPayment implements PaymentStrategy {
    private static final double MAX_TENDERED = 1000000;   // far above any possible booking total

    public String getDisplayName() { return "Cash"; }

    public void pay(double amount) throws PaymentException {
        PaymentStrategy.validateAmount(amount);
        double tendered = ConsoleReader.readDouble("Enter cash amount tendered (PHP): ");

        // ConsoleReader accepts "NaN", "Infinity" and "1e300", so check what was typed before using it.
        if (Double.isNaN(tendered) || Double.isInfinite(tendered)) {
            throw new PaymentException("Invalid cash amount.");
        }
        if (tendered <= 0) {
            throw new PaymentException("The cash amount must be more than " + Format.peso(0) + ".");
        }
        if (tendered > MAX_TENDERED) {
            throw new PaymentException("That cash amount is too large. The maximum is " + Format.peso(MAX_TENDERED) + ".");
        }
        if (BigDecimal.valueOf(tendered).stripTrailingZeros().scale() > 2) {
            throw new PaymentException("Please enter the cash amount with at most 2 decimal places.");
        }

        // BigDecimal compares exact centavos, so floating-point error can't reject exact cash.
        BigDecimal due = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal given = BigDecimal.valueOf(tendered).setScale(2, RoundingMode.HALF_UP);
        if (given.compareTo(due) < 0) {
            throw new PaymentException("Insufficient cash. Required: " + Format.peso(amount)
                    + ", provided: " + Format.peso(tendered));
        }
        System.out.println("  Paid " + Format.peso(amount) + " in cash.");
        BigDecimal change = given.subtract(due);
        if (change.signum() > 0) {
            System.out.println("  Change: " + Format.peso(change.doubleValue()));
        }
    }

    @Override
    public void refund(double amount) {
        System.out.println("  A cash refund of " + Format.peso(amount) + " is available at the cinema counter.");
    }
}