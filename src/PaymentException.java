// Thrown when a payment fails (not enough balance, invalid card number)
public class PaymentException extends CinemaException {
    public PaymentException(String message) { super(message); }
}
