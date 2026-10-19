package cinema.exception;

// Thrown when user input is invalid (bad email, weak password, empty name...)
public class ValidationException extends CinemaException {
    public ValidationException(String message) { super(message); }
}
