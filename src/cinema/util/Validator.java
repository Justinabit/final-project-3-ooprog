package cinema.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import cinema.exception.ValidationException;
import cinema.model.Promo;

// [Member A] All input validation lives here so everyone can reuse it.
// Each method throws ValidationException if the input is bad.
public class Validator {

    public static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    public static void validateRequired(String label, String value) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(label + " cannot be empty.");
        }
    }

    public static void validateName(String name) throws ValidationException {
        validateRequired("Name", name);
        String n = name.trim();
        if (n.length() < 2) {
            throw new ValidationException("Name must be at least 2 characters.");
        }
        if (!n.matches("[A-Za-z][A-Za-z .'-]*")) {
            throw new ValidationException("Name may only contain letters and spaces.");
        }
    }

    public static void validateEmail(String email) throws ValidationException {
        validateRequired("Email", email);
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new ValidationException("Invalid email. Use a format like name@example.com (no spaces).");
        }
    }

    public static void validatePassword(String password) throws ValidationException {
        if (password == null || password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new ValidationException("Password must contain at least one number.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new ValidationException("Password must contain at least one uppercase letter.");
        }
    }

    public static void validateAge(int age) throws ValidationException {
        if (age < 1 || age > 120) {
            throw new ValidationException("Age must be between 1 and 120.");
        }
    }

    public static void validateRange(String label, int value, int min, int max) throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(label + " must be between " + min + " and " + max + ".");
        }
    }

    public static void validateRoomName(String name) throws ValidationException {
        validateRequired("Room name", name);
        if (name.trim().length() > 30) {
            throw new ValidationException("Room name must be 30 characters or less.");
        }
    }

    public static void validatePrice(double price) throws ValidationException {
        if (Double.isNaN(price) || price <= 0 || price > 10000) {
            throw new ValidationException("Price must be greater than 0 and at most 10000.");
        }
    }

    public static void validateAgeRating(String rating) throws ValidationException {
        if (rating == null || !rating.matches("G|PG|R-16|R-18")) {
            throw new ValidationException("Age rating must be one of: G, PG, R-16, R-18.");
        }
    }

    public static void validateMovieStatus(String status) throws ValidationException {
        if (status == null || !(status.equals("NOW SHOWING") || status.equals("COMING SOON"))) {
            throw new ValidationException("Status must be NOW SHOWING or COMING SOON.");
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

    public static void validatePromoCode(String code) throws ValidationException {
        validateRequired("Promo code", code);
        if (!code.matches("[A-Za-z0-9]{3,15}")) {
            throw new ValidationException("Promo code must be 3-15 letters/digits with no spaces.");
        }
    }
}
