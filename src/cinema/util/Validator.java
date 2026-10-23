package cinema.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import cinema.exception.ValidationException;

// [Member A] All input validation lives here so everyone can reuse it.
// Each method throws ValidationException (with a message for the user) if the input is bad.
public class Validator {

    public static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    // ---------- the rules (change them here, and every screen follows) ----------
    public static final int MIN_ACCOUNT_AGE = 13;      // younger children are booked by a parent or guardian
    public static final int MAX_AGE = 100;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 64;
    public static final int MAX_TITLE_LENGTH = 60;
    public static final int MAX_DURATION_MINUTES = 600;
    public static final double MIN_TICKET_PRICE = 100;
    public static final double MAX_TICKET_PRICE = 2000;
    public static final LocalTime FIRST_SHOW = LocalTime.of(9, 0);     // cinema opening time
    public static final LocalTime LAST_SHOW = LocalTime.of(23, 30);    // last screening starts before midnight
    public static final int MAX_DAYS_AHEAD = 365;                      // showtimes and promos: up to 1 year ahead
    public static final String[] GENRES = { "Action", "Adventure", "Animation", "Comedy", "Documentary", "Drama",
            "Family", "Fantasy", "Horror", "Musical", "Romance", "Sci-Fi", "Superhero", "Thriller" };

    // ---------- general ----------

    public static void validateRequired(String label, String value) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(label + " cannot be empty.");
        }
        if (value.contains("|")) {   // "|" separates the values in the data files
            throw new ValidationException(label + " cannot contain the | character.");
        }
    }

    // "  Juan   Dela  Cruz " -> "Juan Dela Cruz"
    public static String cleanSpaces(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

    public static void validateRange(String label, int value, int min, int max) throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(label + " must be between " + min + " and " + max + ".");
        }
    }

    // ---------- accounts ----------

    // Letters (including ñ, é, ...), spaces, and . ' - right after a letter: "Ma. Niña O'Neil-Cruz"
    public static void validateName(String name) throws ValidationException {
        validateRequired("Name", name);
        String n = cleanSpaces(name);
        if (n.length() < 2 || n.length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Name must be 2-" + MAX_NAME_LENGTH + " characters.");
        }
        if (!Character.isLetter(n.charAt(0))) {
            throw new ValidationException("Name must start with a letter.");
        }
        int letters = 1;
        for (int i = 1; i < n.length(); i++) {
            char c = n.charAt(i);
            char before = n.charAt(i - 1);
            if (Character.isLetter(c)) {
                letters++;
            } else if (c == ' ') {
                // spaces are fine (double spaces were already removed)
            } else if ((c == '.' || c == '\'' || c == '-') && Character.isLetter(before)) {
                // a dot, apostrophe or hyphen right after a letter is fine
            } else {
                throw new ValidationException("Name may only contain letters, spaces, and . ' - after a letter.");
            }
        }
        if (letters < 2) {
            throw new ValidationException("Name must have at least 2 letters.");
        }
    }

    // name@example.com: no spaces, no ".." and the ending (com, ph, ...) has 2 or more letters
    public static void validateEmail(String email) throws ValidationException {
        validateRequired("Email", email);
        String e = email.trim();
        if (e.length() > MAX_EMAIL_LENGTH) {
            throw new ValidationException("Email must be " + MAX_EMAIL_LENGTH + " characters or less.");
        }
        boolean goodShape = e.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}");
        if (!goodShape || e.contains("..") || e.startsWith(".") || e.contains(".@")) {
            throw new ValidationException("Invalid email. Use a format like name@example.com.");
        }
    }

    public static void validatePassword(String password) throws ValidationException {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new ValidationException("Password must be " + MIN_PASSWORD_LENGTH + "-" + MAX_PASSWORD_LENGTH + " characters.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new ValidationException("Password must contain at least one number.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new ValidationException("Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new ValidationException("Password must contain at least one lowercase letter.");
        }
        if (password.contains(" ") || password.contains("|")) {
            throw new ValidationException("Password cannot contain spaces or the | character.");
        }
    }

    public static void validateUsername(String username) throws ValidationException {
        validateRequired("Username", username);
        if (!username.trim().matches("[A-Za-z0-9]{4,20}")) {
            throw new ValidationException("Username must be 4-20 letters or digits, with no spaces.");
        }
    }

    // Reads a birthdate typed as yyyy-MM-dd and checks the age rules
    public static LocalDate validateBirthDate(String text) throws ValidationException {
        validateRequired("Birthdate", text);
        LocalDate date;
        try {
            date = LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid birthdate. Use the format yyyy-MM-dd (e.g. 2004-08-15).");
        }
        validateBirthDate(date);
        return date;
    }

    // Customers must be 13-100 years old to have an account
    public static void validateBirthDate(LocalDate date) throws ValidationException {
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new ValidationException("Birthdate cannot be in the future.");
        }
        int age = Period.between(date, LocalDate.now()).getYears();
        if (age < MIN_ACCOUNT_AGE) {
            throw new ValidationException("You must be at least " + MIN_ACCOUNT_AGE
                    + " years old to create an account. A parent or guardian can book for younger children.");
        }
        if (age > MAX_AGE) {
            throw new ValidationException("Please check the birthdate: the age must be " + MAX_AGE + " or below.");
        }
    }

    // ---------- venues ----------

    public static void validateBranchName(String name) throws ValidationException {
        validateRequired("Branch name", name);
        if (name.trim().length() > 40) {
            throw new ValidationException("Branch name must be 40 characters or less.");
        }
    }

    public static void validateLocation(String location) throws ValidationException {
        validateRequired("Location", location);
        if (location.trim().length() > 60) {
            throw new ValidationException("Location must be 60 characters or less.");
        }
    }

    public static void validateRoomName(String name) throws ValidationException {
        validateRequired("Room name", name);
        if (name.trim().length() > 30) {
            throw new ValidationException("Room name must be 30 characters or less.");
        }
    }

    // ---------- movies and showtimes ----------

    public static void validateTitle(String title) throws ValidationException {
        validateRequired("Title", title);
        if (title.trim().length() > MAX_TITLE_LENGTH) {
            throw new ValidationException("Title must be " + MAX_TITLE_LENGTH + " characters or less.");
        }
    }

    // Returns the genre spelled the standard way ("sci-fi" -> "Sci-Fi")
    public static String validateGenre(String genre) throws ValidationException {
        validateRequired("Genre", genre);
        for (String g : GENRES) {
            if (g.equalsIgnoreCase(genre.trim())) return g;
        }
        throw new ValidationException("Genre must be one of: " + String.join(", ", GENRES) + ".");
    }

    public static void validateAgeRating(String rating) throws ValidationException {
        if (rating == null || !rating.matches("G|PG|R-13|R-16|R-18")) {
            throw new ValidationException("Age rating must be one of: G, PG, R-13, R-16, R-18.");
        }
    }

    public static void validateMovieStatus(String status) throws ValidationException {
        if (status == null || !(status.equals("NOW SHOWING") || status.equals("COMING SOON"))) {
            throw new ValidationException("Status must be NOW SHOWING or COMING SOON.");
        }
    }

    // Regular seat price: PHP 100.00 - 2,000.00 with at most 2 decimal places
    public static void validatePrice(double price) throws ValidationException {
        if (Double.isNaN(price) || price < MIN_TICKET_PRICE || price > MAX_TICKET_PRICE) {
            throw new ValidationException("Regular seat price must be between " + Format.peso(MIN_TICKET_PRICE)
                    + " and " + Format.peso(MAX_TICKET_PRICE) + ".");
        }
        if (BigDecimal.valueOf(price).stripTrailingZeros().scale() > 2) {
            throw new ValidationException("Price can have at most 2 decimal places.");
        }
    }

    // Expects "yyyy-MM-dd HH:mm" (e.g. 2026-11-20 18:30). Returns the parsed value.
    public static LocalDateTime validateDateTime(String text) throws ValidationException {
        validateRequired("Date/time", text);
        try {
            return LocalDateTime.parse(text.trim(), DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date/time. Use the format yyyy-MM-dd HH:mm (e.g. 2026-11-20 18:30).");
        }
    }

    // A new showtime: not in the past, at most 1 year ahead, and during cinema hours
    public static void validateShowtimeStart(LocalDateTime start) throws ValidationException {
        LocalDateTime now = LocalDateTime.now();
        if (start.isBefore(now)) {
            throw new ValidationException("A showtime cannot be in the past.");
        }
        if (start.isAfter(now.plusDays(MAX_DAYS_AHEAD))) {
            throw new ValidationException("A showtime can be scheduled at most " + MAX_DAYS_AHEAD + " days ahead.");
        }
        LocalTime time = start.toLocalTime();
        if (time.isBefore(FIRST_SHOW) || time.isAfter(LAST_SHOW)) {
            throw new ValidationException("Showtimes must start between 9:00 AM and 11:30 PM (cinema hours).");
        }
    }

    // ---------- discounts ----------

    public static void validatePromoCode(String code) throws ValidationException {
        validateRequired("Promo code", code);
        if (!code.matches("[A-Za-z0-9]{3,15}")) {
            throw new ValidationException("Promo code must be 3-15 letters/digits with no spaces.");
        }
    }

    // The last day a new promo code can be used: from today up to 1 year ahead
    public static LocalDate validatePromoEndDate(String text) throws ValidationException {
        validateRequired("Valid until", text);
        LocalDate date;
        try {
            date = LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date. Use the format yyyy-MM-dd (e.g. 2026-12-31).");
        }
        if (date.isBefore(LocalDate.now()) || date.isAfter(LocalDate.now().plusDays(MAX_DAYS_AHEAD))) {
            throw new ValidationException("The end date must be between today and " + MAX_DAYS_AHEAD + " days from now.");
        }
        return date;
    }

    // PWD ID: digits and dashes, like 04-1014-000-0001234
    public static void validatePwdId(String id) throws ValidationException {
        validateRequired("PWD ID number", id);
        String trimmed = id.trim();
        int digits = trimmed.replace("-", "").length();
        if (!trimmed.matches("[0-9-]{10,25}") || digits < 8) {
            throw new ValidationException("Invalid PWD ID number. Use the digits and dashes on the ID (e.g. 04-1014-000-0001234).");
        }
    }
}
