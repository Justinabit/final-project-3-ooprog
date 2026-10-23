package cinema.util;

import java.util.NoSuchElementException;
import java.util.Scanner;

import cinema.exception.GoBackException;

// [Member D] Helper for reading input safely. Everyone uses this instead of making their own Scanner.
public class ConsoleReader {
    public static final String RETURN_COMMAND = "/return";   // typed at any prompt to go back one screen

    private static Scanner scanner = new Scanner(System.in);

    // Reads a line of text with the spaces around it removed. Prompts are indented so they line up with the menus.
    public static String readLine(String prompt) {
        return readExactLine(prompt).trim();
    }

    // Reads a line exactly as typed (spaces kept), so readChoice can reject " 1".
    // Every input goes through here, so typing /return anywhere throws GoBackException.
    private static String readExactLine(String prompt) {
        System.out.print("  " + prompt);
        String line;
        try {
            line = scanner.nextLine();
        } catch (NoSuchElementException e) {   // input stream closed (Ctrl+D / end of piped input)
            System.out.println("\nInput ended. Goodbye!");
            System.exit(0);
            return "";
        }
        if (line.trim().equalsIgnoreCase(RETURN_COMMAND)) {
            throw new GoBackException();
        }
        return line;
    }

    // Menu choices: only the number exactly as it is shown, from 0 to max.
    // "1" is accepted; " 1", "1 ", "01", "+1", "1.0" and numbers above max are not.
    // Menus with options 0-9 therefore take exactly one digit. Long lists (10 or more items)
    // also take the two-digit number shown, like "10", but never with a leading zero.
    public static int readChoice(String prompt, int max) {
        while (true) {
            String text = readExactLine(prompt);
            if (text.matches("[0-9]|[1-9][0-9]{1,2}") && Integer.parseInt(text) <= max) {
                return Integer.parseInt(text);
            }
            if (max <= 9) {
                System.out.println("  [ !! ] Invalid choice. Please type one digit from 0 to " + max + " (no spaces).");
            } else {
                System.out.println("  [ !! ] Invalid choice. Please type a number from 0 to " + max
                        + " (no spaces, no leading zero).");
            }
        }
    }

    // Keeps asking until the user types a whole number from min to max. The number is checked as soon as
    // it is typed, so a wrong value is asked again right away (not after the whole form is filled in).
    // label is used in the message, e.g. "Rows must be between 2 and 26."
    public static int readNumber(String prompt, String label, int min, int max) {
        while (true) {
            String text = readLine(prompt);
            if (!text.matches("[0-9]{1,9}")) {
                System.out.println("  [ !! ] Please enter a whole number from " + min + " to " + max + ".");
            } else if (Integer.parseInt(text) < min || Integer.parseInt(text) > max) {
                System.out.println("  [ !! ] " + label + " must be between " + min + " and " + max + ". Please try again.");
            } else {
                return Integer.parseInt(text);
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
