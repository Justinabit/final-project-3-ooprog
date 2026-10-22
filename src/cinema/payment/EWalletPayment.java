package cinema.payment;

import java.util.Random;

import cinema.exception.PaymentException;
import cinema.util.ConsoleReader;
import cinema.util.Format;

// [Member D] Pay with an e-wallet like GCash or Maya.
// One class serves both wallets; only the name is different.
public class EWalletPayment implements PaymentStrategy {
    private String walletName;   // "GCash" or "Maya"

    public EWalletPayment(String walletName) {
        this.walletName = walletName;
    }

    public String getDisplayName() { return walletName; }

    public void pay(double amount) throws PaymentException {
        String number = ConsoleReader.readLine(walletName + " mobile number (09XXXXXXXXX): ").replaceAll("[ -]", "");
        if (!number.matches("09\\d{9}")) {
            throw new PaymentException("Invalid mobile number. It must have 11 digits and start with 09.");
        }
        String hiddenNumber = number.substring(0, 4) + "***" + number.substring(7);

        // A real app texts the one-time PIN; this demo just shows it on screen.
        int otp = 100000 + new Random().nextInt(900000);
        System.out.println("  (Demo) " + walletName + " sent a one-time PIN to " + hiddenNumber + ": " + otp);
        String typed = ConsoleReader.readLine("Enter the 6-digit OTP: ");
        if (!typed.equals(String.valueOf(otp))) {
            throw new PaymentException("Wrong OTP. The payment was not completed.");
        }
        System.out.println("  Paid " + Format.peso(amount) + " via " + walletName + " (" + hiddenNumber + ").");
    }

    @Override
    public void refund(double amount) {
        System.out.println("  Refunded " + Format.peso(amount) + " to your " + walletName + " wallet.");
    }
}
