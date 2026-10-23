package cinema.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

import cinema.model.Booking;
import cinema.util.Validator;

// Keeps a running record of every payment and refund in data/transactions.txt.
// Lines are only ever added (never changed or deleted), like a receipt book.
public class TransactionLog {
    public static final String FILE_NAME = "transactions.txt";

    // type is "PAYMENT" or "REFUND"
    public static void record(String type, Booking booking, double amount) {
        String method = "-";
        if (booking.getPayment() != null) {
            method = booking.getPayment().getDisplayName();
        }
        String line = LocalDateTime.now().format(Validator.DATE_TIME_FORMAT) + "|" + type + "|"
                + booking.getBookingId() + "|" + booking.getCustomer().getUserId() + "|" + method + "|"
                + String.format(Locale.ENGLISH, "%.2f", amount);
        File file = new File(FileManager.FOLDER, FILE_NAME);
        try {
            file.getParentFile().mkdirs();
            boolean newFile = !file.exists();
            try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {   // true = add to the end
                if (newFile) {
                    out.println("# DATE AND TIME|TYPE|BOOKING|CUSTOMER ID|METHOD|AMOUNT");
                }
                out.println(line);
            }
        } catch (IOException e) {
            ErrorLog.write(e);
        }
    }

    // Every recorded transaction, oldest first (for the admin screen)
    public static ArrayList<String[]> readAll() {
        ArrayList<String[]> rows = new ArrayList<>();
        File file = new File(FileManager.FOLDER, FILE_NAME);
        if (!file.exists()) {
            return rows;
        }
        try (Scanner reader = new Scanner(file)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                if (!line.trim().isEmpty() && !line.startsWith("#")) {
                    rows.add(line.split("\\|", -1));
                }
            }
        } catch (FileNotFoundException e) {
            ErrorLog.write(e);
        }
        return rows;
    }
}
