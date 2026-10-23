package cinema.payment;

import java.time.YearMonth;

import cinema.exception.PaymentException;
import cinema.util.ConsoleReader;
import cinema.util.Format;

// [Member D] Pay with a Visa or Mastercard credit/debit card.
// Checks the card number (Luhn checksum), the expiry date and the CVV like a real payment page:
// each one as soon as it is typed, asking again until it is right.
public class CardPayment implements PaymentStrategy {
    public String getDisplayName() { return "Credit/Debit Card"; }

    public void pay(double amount) throws PaymentException {
        PaymentStrategy.validateAmount(amount);
        System.out.println("  (Demo test card: 4111 1111 1111 1111, any future expiry, any 3-digit CVV)");
        String cardNumber;
        while (true) {
            cardNumber = ConsoleReader.readLine("Card number (16 digits): ").replaceAll("[ -]", "");
            try {
                checkCardNumber(cardNumber);
                break;
            } catch (PaymentException e) {
                System.out.println("  [ !! ] " + e.getMessage());
            }
        }
        while (true) {
            try {
                checkExpiry(ConsoleReader.readLine("Expiry date (MM/YY): "));
                break;
            } catch (PaymentException e) {
                System.out.println("  [ !! ] " + e.getMessage());
            }
        }
        // The CVV is only checked here; it is never printed or saved.
        while (!ConsoleReader.readLine("CVV (3 digits on the back): ").matches("\\d{3}")) {
            System.out.println("  [ !! ] Invalid CVV. It must be 3 digits.");
        }
        System.out.println("  Paid " + Format.peso(amount) + " by card ending in " + cardNumber.substring(12) + ".");
    }

    private void checkCardNumber(String cardNumber) throws PaymentException {
        if (!cardNumber.matches("\\d{16}")) {
            throw new PaymentException("Invalid card number. It must contain exactly 16 digits.");
        }
        if (!(cardNumber.startsWith("4") || cardNumber.startsWith("5") || cardNumber.startsWith("2"))) {
            throw new PaymentException("Only Visa (starts with 4) and Mastercard (starts with 5 or 2) are accepted.");
        }
        if (!passesLuhnCheck(cardNumber)) {
            throw new PaymentException("Invalid card number. Please check the digits.");
        }
    }

    // Luhn check: the checksum every real card number passes. Going from the right, double every second
    // digit (minus 9 if it becomes more than 9) and add all the digits up: the total must end in 0.
    private boolean passesLuhnCheck(String number) {
        int sum = 0;
        boolean doubleIt = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (doubleIt) {
                digit = digit * 2;
                if (digit > 9) {
                    digit = digit - 9;
                }
            }
            sum += digit;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }

    // "MM/YY", a real month, and not before this month
    private void checkExpiry(String text) throws PaymentException {
        if (!text.matches("\\d{2}/\\d{2}")) {
            throw new PaymentException("Invalid expiry date. Use MM/YY (e.g. 08/28).");
        }
        int month = Integer.parseInt(text.substring(0, 2));
        int year = 2000 + Integer.parseInt(text.substring(3));
        if (month < 1 || month > 12) {
            throw new PaymentException("Invalid expiry month. It must be 01 to 12.");
        }
        if (YearMonth.of(year, month).isBefore(YearMonth.now())) {
            throw new PaymentException("This card has expired.");
        }
    }

    @Override
    public void refund(double amount) {
        System.out.println("  Refunded " + Format.peso(amount) + " to the original card (3-5 banking days).");
    }
}
