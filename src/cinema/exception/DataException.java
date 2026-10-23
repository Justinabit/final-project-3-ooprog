package cinema.exception;

// Thrown when a data file can't be read or written, or a saved line is damaged
public class DataException extends CinemaException {
    public DataException(String message) { super(message); }
}
