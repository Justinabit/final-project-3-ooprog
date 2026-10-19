package cinema.util;

import java.util.Scanner;

// [Member D] Helper for reading input safely. Everyone uses this instead of making their own Scanner.
public class ConsoleReader {
    private static Scanner scanner = new Scanner(System.in);

    // Reads a line of text
    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    // Keeps asking until the user types a valid whole number
    public static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    // TODO (D): add readDouble(prompt)
    // TODO (D): add readYesNo(prompt) that returns true for "y"
}
