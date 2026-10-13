// Base class for all our custom (checked) exceptions.
// Catch this to handle any cinema-related error.
public class CinemaException extends Exception {
    public CinemaException(String message) {
        super(message);
    }
}
