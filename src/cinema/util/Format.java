package cinema.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Shared formats for money and dates, so every screen shows them the same way.
public class Format {
    // Philippine peso. "PHP" is used instead of the peso sign because many consoles can't print it.
    public static final String CURRENCY = "PHP ";

    private static final DateTimeFormatter LONG_DATE_TIME =
            DateTimeFormatter.ofPattern("EEE, MMM d, yyyy  h:mm a", Locale.ENGLISH);
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

    // -> "Thu, Oct 22  02:00 PM" (same length every time, so lists line up)
    public static String shortDateTime(LocalDateTime time) {
        return time.format(SHORT_DATE_TIME);
    }
}
