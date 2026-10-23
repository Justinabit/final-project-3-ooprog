package cinema.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Shared formats for money and dates, so every screen shows them the same way.
public class Format {
    // Philippine peso. "PHP" is used instead of the peso sign because many consoles can't print it.
    public static final String CURRENCY = "PHP ";

    private static final DateTimeFormatter LONG_DATE_TIME =
            DateTimeFormatter.ofPattern("EEE, MMM d, yyyy  h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_ONLY =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT_DATE_TIME =
            DateTimeFormatter.ofPattern("EEE, MMM d  hh:mm a", Locale.ENGLISH);

    // 1250.5 -> "PHP 1,250.50"
    public static String peso(double amount) {
        return CURRENCY + String.format(Locale.ENGLISH, "%,.2f", amount);
    }

    // -> "Thu, Oct 22, 2026  2:00 PM"
    public static String dateTime(LocalDateTime time) {
        return time.format(LONG_DATE_TIME);
    }

    // -> "Aug 15, 2004"
    public static String date(LocalDate day) {
        return day.format(DATE_ONLY);
    }

    // -> "Thu, Oct 22  02:00 PM" (same length every time, so lists line up)
    public static String shortDateTime(LocalDateTime time) {
        return time.format(SHORT_DATE_TIME);
    }

    // ---------- fitting text into table columns ----------

    // Makes text exactly `width` characters: short text gets spaces after it, long text is cut and ends
    // with "..." so it never pushes the next column. fit("Ma. Cristina Dela Cruz", 16) -> "Ma. Cristina... "
    public static String fit(String text, int width) {
        String cut = shorten(text, width);
        return cut + spaces(width - cut.length());
    }

    // Same as fit, but the spaces go first (for numbers and money, so they line up on the right)
    public static String fitRight(String text, int width) {
        String cut = shorten(text, width);
        return spaces(width - cut.length()) + cut;
    }

    // Cuts text to at most `width` characters, ending with "..." when something was cut (no padding)
    public static String shorten(String text, int width) {
        if (text.length() <= width) {
            return text;
        }
        if (width <= 3) {
            return text.substring(0, width);
        }
        return text.substring(0, width - 3).trim() + "...";   // "Ma. Cristina ..." -> "Ma. Cristina..."
    }

    private static String spaces(int count) {
        String result = "";
        for (int i = 0; i < count; i++) {
            result += " ";
        }
        return result;
    }
}
