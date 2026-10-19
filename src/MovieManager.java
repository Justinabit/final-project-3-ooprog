import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

// [Member B] Stores movies and showtimes
public class MovieManager {
    private static final int CLEANING_BUFFER_MINUTES = 15;   // gap needed between shows in one room

    private ArrayList<Movie> movies = new ArrayList<>();
    private ArrayList<Showtime> showtimes = new ArrayList<>();
    private int movieCounter = 0;
    private int showtimeCounter = 0;

    public String nextMovieId() { return "M-" + (++movieCounter); }
    public String nextShowtimeId() { return "S-" + (++showtimeCounter); }

    public void addMovie(Movie movie) throws ValidationException {
        for (Movie m : movies) {
            if (m.getTitle().equalsIgnoreCase(movie.getTitle())) {
                throw new ValidationException("A movie titled '" + movie.getTitle() + "' already exists.");
            }
        }
        movies.add(movie);
    }

    // Renaming must also keep titles unique
    public void renameMovie(Movie movie, String newTitle) throws ValidationException {
        Validator.validateRequired("Title", newTitle);
        for (Movie m : movies) {
            if (m != movie && m.getTitle().equalsIgnoreCase(newTitle.trim())) {
                throw new ValidationException("A movie titled '" + newTitle.trim() + "' already exists.");
            }
        }
        movie.setTitle(newTitle);
    }

    // Team decision: a movie can't be removed while it has upcoming showtimes with confirmed bookings.
    // Otherwise its showtimes (and any past/cancelled bookings' showtimes list) are removed with it.
    public void removeMovie(String movieId) throws BookingException {
        Movie movie = findMovie(movieId);
        if (movie == null) {
            throw new BookingException("Movie " + movieId + " not found.");
        }
        for (Booking b : BookingManager.getInstance().getAllBookings()) {
            if (b.getShowtime().getMovie() == movie && "CONFIRMED".equals(b.getStatus())
                    && !b.getShowtime().hasEnded()) {
                throw new BookingException("Cannot remove '" + movie.getTitle()
                        + "': it has upcoming showtimes with confirmed bookings. "
                        + "Cancel those bookings first, or set the movie to COMING SOON.");
            }
        }
        showtimes.removeIf(s -> s.getMovie() == movie);
        movies.remove(movie);
    }

    public void addShowtime(Showtime showtime) throws BookingException {
        if (!movies.contains(showtime.getMovie())) {
            throw new BookingException("That movie is not in the catalog.");
        }
        if (!"NOW SHOWING".equals(showtime.getMovie().getStatus())) {
            throw new BookingException("Only NOW SHOWING movies can be scheduled.");
        }
        if (showtime.getStart().isBefore(LocalDateTime.now())) {
            throw new BookingException("A showtime cannot be in the past.");
        }
        // Prevent double-booking the room (with a short cleaning gap)
        for (Showtime other : showtimes) {
            if (other.getRoom() != showtime.getRoom()) continue;
            boolean overlaps = showtime.getStart().isBefore(other.getEnd().plusMinutes(CLEANING_BUFFER_MINUTES))
                    && other.getStart().isBefore(showtime.getEnd().plusMinutes(CLEANING_BUFFER_MINUTES));
            if (overlaps) {
                throw new BookingException("Room '" + showtime.getRoom().getRoomName() + "' is already in use from "
                        + other.getDateTime() + " (" + other.getMovie().getTitle() + ", ends "
                        + other.getEnd().format(Validator.DATE_TIME_FORMAT) + ").");
            }
        }
        showtimes.add(showtime);
    }

    public Showtime findShowtime(String showtimeId) {
        for (Showtime s : showtimes) {
            if (s.getShowtimeId().equalsIgnoreCase(showtimeId.trim())) return s;
        }
        return null;
    }

    public Movie findMovie(String movieId) {
        for (Movie m : movies) {
            if (m.getMovieId().equalsIgnoreCase(movieId.trim())) return m;
        }
        return null;
    }

    // Showtimes that haven't started yet, earliest first. movie == null means all movies.
    public ArrayList<Showtime> getUpcomingShowtimes(Movie movie) {
        ArrayList<Showtime> result = new ArrayList<>();
        for (Showtime s : showtimes) {
            if (!s.hasStarted() && (movie == null || s.getMovie() == movie)) result.add(s);
        }
        result.sort(Comparator.comparing(Showtime::getStart));
        return result;
    }

    public ArrayList<Movie> getMovies() { return movies; }
    public ArrayList<Showtime> getShowtimes() { return showtimes; }
}
