package cinema.ui;

import cinema.util.ConsoleReader;

// Shared look for every screen: the welcome banner, titles, menu options and messages.
// All menus call these methods so the whole program has the same design.
public class Screen {
    private static final int WIDTH = 74;   // width of every box, in characters

    // Big "CURATOR" logo for the welcome screen (every line has the same length)
    private static final String[] LOGO = {
        " ######  ##     ## ########     ###    ########  #######  ######## ",
        "##    ## ##     ## ##     ##   ## ##      ##    ##     ## ##     ##",
        "##       ##     ## ##     ##  ##   ##     ##    ##     ## ##     ##",
        "##       ##     ## ########  ##     ##    ##    ##     ## ######## ",
        "##       ##     ## ##   ##   #########    ##    ##     ## ##   ##  ",
        "##    ## ##     ## ##    ##  ##     ##    ##    ##     ## ##    ## ",
        " ######   #######  ##     ## ##     ##    ##     #######  ##     ##"
    };

    // ---------- welcome / goodbye ----------

    public static void showWelcome() {
        System.out.println();
        border();
        filmStrip();
        border();
        boxLine("");
        boxLine("W E L C O M E   T O");
        boxLine("");
        for (String line : LOGO) {
            boxLine(line);
        }
        boxLine("");
        boxLine("-=-=-=-   C I N E M A   M A N A G E M E N T   S Y S T E M   -=-=-=-");
        boxLine("");
        boxLine("Movies  *  Showtimes  *  Branches  *  Bookings");
        boxLine("");
        border();
        filmStrip();
        border();
        System.out.println();
        ConsoleReader.readLine(spaces(23) + "Press Enter to continue...");
    }

    public static void showGoodbye() {
        System.out.println();
        border();
        boxLine("Thank you for using CURATOR. Enjoy the show!");
        border();
    }

    // ---------- titles and menus ----------

    // Box with the screen name, for example "ADMIN MENU"
    public static void title(String text) {
        System.out.println();
        String brand = "=[ CURATOR ]";
        System.out.println(" +" + brand + repeat("=", WIDTH - 2 - brand.length()) + "+");
        boxLine(text);
        border();
    }

    // Smaller heading inside a screen, for example "SUMMARY"
    public static void section(String text) {
        System.out.println();
        System.out.println(" --- " + text + " " + repeat("-", WIDTH - 6 - text.length()));
    }

    // One menu option, for example "   [1]  Register"
    public static void option(int number, String text) {
        System.out.println("   [" + number + "]  " + text);
    }

    // Prints the bottom line of a menu and reads the user's choice
    public static int readChoice() {
        line();
        return ConsoleReader.readInt("Enter your choice: ");
    }

    public static void line() {
        System.out.println(" " + repeat("-", WIDTH));
    }

    // ---------- messages ----------

    public static void success(String message) {
        System.out.println("  [ OK ] " + message);
    }

    public static void error(String message) {
        System.out.println("  [ !! ] " + message);
    }

    public static void info(String message) {
        System.out.println("  [INFO] " + message);
    }

    // ---------- small helpers ----------

    private static void border() {
        System.out.println(" +" + repeat("=", WIDTH - 2) + "+");
    }

    private static void filmStrip() {
        System.out.println(" |" + repeat(" [] ", (WIDTH - 2) / 4) + "|");
    }

    // Prints text centered between two "|" walls
    private static void boxLine(String text) {
        int inside = WIDTH - 2;
        int left = (inside - text.length()) / 2;
        int right = inside - text.length() - left;
        System.out.println(" |" + spaces(left) + text + spaces(right) + "|");
    }

    private static String spaces(int count) {
        return repeat(" ", count);
    }

    private static String repeat(String text, int count) {
        String result = "";
        for (int i = 0; i < count; i++) {
            result += text;
        }
        return result;
    }
}
