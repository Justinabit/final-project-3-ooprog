package cinema.util;

import cinema.exception.ValidationException;

// [Member A] All input validation lives here so everyone can reuse it.
// Each method should throw ValidationException if the input is bad.
public class Validator {

    public static void validateName(String name) throws ValidationException {
        // TODO (A): not empty, letters and spaces only, at least 2 characters
    }

    public static void validateEmail(String email) throws ValidationException {
        // TODO (A): not empty, contains "@" and a "." after it, no spaces
    }

    public static void validatePassword(String password) throws ValidationException {
        // TODO (A): at least 8 characters, has a number, has an uppercase letter
    }

    public static void validateAge(int age) throws ValidationException {
        // TODO (A): between 1 and 120
    }

    // TODO (A): add more as needed (phone number, room name, seat count, etc.)
    // TIP for everyone: use these before saving any user input!
}
