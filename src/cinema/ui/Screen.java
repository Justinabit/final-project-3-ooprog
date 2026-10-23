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
        System.out.println(" --- " + text + " " + repeat("-", WIDTH - 5 - text.length()));
    }

    // One menu option, for example "   [1]  Register" (two-digit numbers stay lined up)
    public static void option(int number, String text) {
        String label = "[" + number + "]";
        if (number < 10) {
            label = " " + label;
        }
        System.out.println("  " + label + "  " + text);
    }

    // Prints the bottom line of a menu and reads the user's choice (0 to max, exactly as shown)
    public static int readChoice(int max) {
        line();
        return ConsoleReader.readChoice("Enter your choice: ", max);
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

    // ---------- boxes (used for the welcome screen and the e-ticket) ----------

    public static void border() {
        System.out.println(" +" + repeat("=", WIDTH - 2) + "+");
    }

    public static void boxDivider() {
        System.out.println(" |" + repeat("-", WIDTH - 2) + "|");
    }

    // Prints text on the left side between two "|" walls (long text is cut off)
    public static void boxLeft(String text) {
        int inside = WIDTH - 4;
        if (text.length() > inside) {
            text = text.substring(0, inside);
        }
        System.out.println(" |  " + text + spaces(inside - text.length()) + "|");
    }

    // ---------- small helpers ----------

    private static void filmStrip() {
        System.out.println(" |" + repeat(" [] ", (WIDTH - 2) / 4) + "|");
    }

    // Prints text centered between two "|" walls
    public static void boxLine(String text) {
        System.out.println(" |" + center(text, WIDTH - 2) + "|");
    }

    // Pads text with spaces on both sides so it is centered in the given width
    public static String center(String text, int width) {
        int left = (width - text.length()) / 2;
        int right = width - text.length() - left;
        return spaces(left) + text + spaces(right);
    }

    public static String spaces(int count) {
        return repeat(" ", count);
    }

    public static String repeat(String text, int count) {
        String result = "";
        for (int i = 0; i < count; i++) {
            result += text;
        }
        return result;
    }
}
