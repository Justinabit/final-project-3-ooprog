// [Member D] Pay with cash
public class CashPayment implements PaymentStrategy {
    public String getDisplayName() { return "Cash"; }

    public void pay(double amount) throws PaymentException {
        // TODO (D): ask for the cash given, throw PaymentException if it's less than amount
        System.out.println("Paid " + amount + " in cash.");
    }
}
