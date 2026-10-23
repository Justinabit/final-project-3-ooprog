package cinema.util;

import java.util.NoSuchElementException;
import java.util.Scanner;

// [Member D] Helper for reading input safely. Everyone uses this instead of making their own Scanner.
public class ConsoleReader {
    private static Scanner scanner = new Scanner(System.in);

    // Reads a line of text. Prompts are indented so they line up with the menus.
    public static String readLine(String prompt) {
        System.out.print("  " + prompt);
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {   // input stream closed (Ctrl+D / end of piped input)
            System.out.println("\nInput ended. Goodbye!");
            System.exit(0);
            return "";
        }
    }

    // Keeps asking until the user types a valid whole number
    public static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("  [ !! ] Please enter a number.");
            }
        }
    }

    // Keeps asking until the user types a money amount like 450, 450.50 or 1,250
    // (digits only, at most 2 decimal places; no letters, minus signs or "1e3").
    public static double readAmount(String prompt) {
        while (true) {
            String text = readLine(prompt).replace(",", "");
            if (text.matches("\\d{1,9}(\\.\\d{1,2})?")) {
                return Double.parseDouble(text);
            }
            System.out.println("  [ !! ] Please enter an amount like 450 or 450.50.");
        }
    }

    // Keeps asking until the user answers y / yes or n / no
    public static boolean readYesNo(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")) return true;
            if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no")) return false;
            System.out.println("  [ !! ] Please answer y or n.");
        }
    }
}
