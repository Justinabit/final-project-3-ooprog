// [Member D] Pay with cash
public class CashPayment implements PaymentStrategy {
    public String getDisplayName() { return "Cash"; }

    public void pay(double amount) throws PaymentException {
        double tendered = ConsoleReader.readDouble("Enter cash amount tendered: ");
        if (tendered < amount) {
            throw new PaymentException(String.format("Insufficient cash. Required: $%.2f, provided: $%.2f", amount, tendered));
        }
        System.out.println(String.format("Paid $%.2f in cash.", amount));
        double change = tendered - amount;
        if (change > 0.004) {
            System.out.println(String.format("Change returned: $%.2f", change));
        }
    }

    @Override
    public void refund(double amount) {
        System.out.println(String.format("Refund of $%.2f in cash is available at the counter.", amount));
    }
}
