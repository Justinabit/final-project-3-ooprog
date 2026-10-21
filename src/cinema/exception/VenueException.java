package cinema.exception;

// Thrown for venue problems (branch or room not found, room still has booked showtimes)
public class VenueException extends CinemaException {
    public VenueException(String message) { super(message); }
}
