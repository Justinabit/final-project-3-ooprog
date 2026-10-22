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

    // Keeps asking until the user types a valid decimal number
    public static double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("  [ !! ] Please enter a valid number.");
            }
        }
    }

    // Returns true for "y" or "yes"
    public static boolean readYesNo(String prompt) {
        String input = readLine(prompt);
        return input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes");
    }
}
