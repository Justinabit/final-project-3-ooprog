package cinema.exception;

// Thrown for booking problems (seat already taken, showtime full, invalid promo)
public class BookingException extends CinemaException {
    public BookingException(String message) { super(message); }
}
