package cinema.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

import cinema.exception.BookingException;
import cinema.exception.CinemaException;
import cinema.exception.ValidationException;
import cinema.model.Booking;
import cinema.model.CinemaRoom;
import cinema.model.Movie;
import cinema.model.Showtime;
import cinema.util.Validator;

// [Member B] Stores movies and showtimes
public class MovieManager {
    private static final int CLEANING_BUFFER_MINUTES = 15;   // gap needed between shows in one room

    private ArrayList<Movie> movies = new ArrayList<>();
    private ArrayList<Showtime> showtimes = new ArrayList<>();
    private int movieCounter = 0;      // number of the last movie ID given out (M-1, M-2, ...)
    private int showtimeCounter = 0;   // number of the last showtime ID given out (S-1, S-2, ...)

    // ---------- movies ----------

    // Creates and adds a movie. The ID is only used up when the movie is really added.
    public Movie createMovie(String title, String genre, String rating, int minutes, String status)
            throws ValidationException {
        Movie movie = new Movie("M-" + (movieCounter + 1), title, genre, rating, minutes, status);   // validates
        addMovie(movie);
        return movie;
    }

    // Used by createMovie and when loading the data file
    public void addMovie(Movie movie) throws ValidationException {
        for (Movie m : movies) {
            if (m.getTitle().equalsIgnoreCase(movie.getTitle())) {
                throw new ValidationException("A movie titled '" + movie.getTitle() + "' already exists.");
            }
        }
        int number = Integer.parseInt(movie.getMovieId().substring(2));   // "M-12" -> 12
        movies.add(movie);
        if (number > movieCounter) {
            movieCounter = number;
        }
    }

    // Renaming must also keep titles unique
    public void renameMovie(Movie movie, String newTitle) throws ValidationException {
        Validator.validateTitle(newTitle);
        for (Movie m : movies) {
            if (m != movie && m.getTitle().equalsIgnoreCase(newTitle.trim())) {
                throw new ValidationException("A movie titled '" + newTitle.trim() + "' already exists.");
            }
        }
        movie.setTitle(newTitle);
    }

    // A longer movie must still fit between the other shows in each room, so every upcoming
    // showtime of this movie is checked with the new length before anything is changed.
    public void changeDuration(Movie movie, int minutes) throws CinemaException {
        Validator.validateRange("Duration (minutes)", minutes, 1, 600);
        for (Showtime s : showtimes) {
            if (s.getMovie() != movie || s.isCancelled() || s.hasEnded()) continue;
            LocalDateTime newEnd = s.getStart().plusMinutes(minutes);
            for (Showtime other : showtimes) {
                if (other == s || other.getRoom() != s.getRoom() || other.isCancelled()) continue;
                LocalDateTime otherEnd = other.getEnd();
                if (other.getMovie() == movie) {
                    otherEnd = other.getStart().plusMinutes(minutes);   // this one gets longer too
                }
                if (overlaps(s.getStart(), newEnd, other.getStart(), otherEnd)) {
                    throw new BookingException("Can't change the duration: the " + s.getDateTime() + " show in "
                            + s.getRoom().getRoomName() + " would overlap " + other.getMovie().getTitle()
                            + " at " + other.getDateTime() + ".");
                }
            }
        }
        movie.setDurationMinutes(minutes);
    }

    // A movie can't be removed once it has bookings (confirmed or cancelled), so the booking history
    // and the reports stay complete. Otherwise its showtimes are removed with it.
    public void removeMovie(String movieId) throws BookingException {
        Movie movie = findMovie(movieId);
        if (movie == null) {
            throw new BookingException("Movie " + movieId + " not found.");
        }
        for (Booking b : BookingManager.getInstance().getAllBookings()) {
            if (b.getShowtime().getMovie() == movie) {
                throw new BookingException("Cannot remove '" + movie.getTitle()
                        + "': it has bookings on record. Set it to COMING SOON instead so it can't be booked.");
            }
        }
        ArrayList<Showtime> toRemove = new ArrayList<>();
        for (Showtime s : showtimes) {
            if (s.getMovie() == movie) toRemove.add(s);
        }
        showtimes.removeAll(toRemove);
        movies.remove(movie);
    }

    // ---------- showtimes ----------

    // Creates and adds a showtime. The ID is only used up when the showtime is really added.
    public Showtime createShowtime(Movie movie, CinemaRoom room, String dateTime, double price) throws CinemaException {
        if (!movie.isNowShowing()) {
            throw new BookingException("Only NOW SHOWING movies can be scheduled.");
        }
        Showtime showtime = new Showtime("S-" + (showtimeCounter + 1), movie, room, dateTime, price);   // validates
        Validator.validateShowtimeStart(showtime.getStart());
        addShowtime(showtime);
        return showtime;
    }

    // Checks the schedule rules. createShowtime uses it; the data file uses it without the time rules,
    // because old showtimes are history and may be in the past.
    public void addShowtime(Showtime showtime) throws BookingException {
        if (!movies.contains(showtime.getMovie())) {
            throw new BookingException("That movie is not in the catalog.");
        }
        if (!showtime.isCancelled()) {
            for (Showtime other : showtimes) {
                if (other.getRoom() != showtime.getRoom() || other.isCancelled()) continue;
                if (overlaps(showtime.getStart(), showtime.getEnd(), other.getStart(), other.getEnd())) {
                    throw new BookingException("Room '" + showtime.getRoom().getRoomName() + "' is already in use from "
                            + other.getDateTime() + " (" + other.getMovie().getTitle() + ", ends "
                            + other.getEnd().format(Validator.DATE_TIME_FORMAT) + ").");
                }
            }
        }
        int number = Integer.parseInt(showtime.getShowtimeId().substring(2));   // "S-12" -> 12
        showtimes.add(showtime);
        if (number > showtimeCounter) {
            showtimeCounter = number;
        }
    }

    // Two shows in one room overlap if one starts before the other has ended plus the cleaning gap
    private boolean overlaps(LocalDateTime startA, LocalDateTime endA, LocalDateTime startB, LocalDateTime endB) {
        return startA.isBefore(endB.plusMinutes(CLEANING_BUFFER_MINUTES))
                && startB.isBefore(endA.plusMinutes(CLEANING_BUFFER_MINUTES));
    }

    // Cancels a screening: every confirmed booking is cancelled and refunded first.
    // The showtime stays in the list (marked cancelled) so its bookings keep their history.
    // Returns how many bookings were refunded.
    public int cancelShowtime(Showtime showtime) throws BookingException {
        if (showtime.isCancelled()) {
            throw new BookingException("This showtime is already cancelled.");
        }
        if (showtime.hasStarted()) {
            throw new BookingException("This showtime has already started, so it can't be cancelled.");
        }
        int refunded = 0;
        for (Booking b : BookingManager.getInstance().getAllBookings()) {
            if (b.getShowtime() == showtime && "CONFIRMED".equals(b.getStatus())) {
                BookingManager.getInstance().cancelBooking(b.getBookingId(), null);   // admin cancel + refund
                refunded++;
            }
        }
        showtime.cancel();
        return refunded;
    }

    public Showtime findShowtime(String showtimeId) {
        if (showtimeId == null) return null;
        for (Showtime s : showtimes) {
            if (s.getShowtimeId().equalsIgnoreCase(showtimeId.trim())) return s;
        }
        return null;
    }

    public Movie findMovie(String movieId) {
        if (movieId == null) return null;
        for (Movie m : movies) {
            if (m.getMovieId().equalsIgnoreCase(movieId.trim())) return m;
        }
        return null;
    }

    // Showtimes that haven't started and aren't cancelled, earliest first. movie == null means all movies.
    public ArrayList<Showtime> getUpcomingShowtimes(Movie movie) {
        ArrayList<Showtime> result = new ArrayList<>();
        for (Showtime s : showtimes) {
            if (!s.hasStarted() && !s.isCancelled() && (movie == null || s.getMovie() == movie)) result.add(s);
        }
        result.sort(Comparator.comparing(Showtime::getStart));
        return result;
    }

    public ArrayList<Movie> getMovies() { return movies; }
    public ArrayList<Showtime> getShowtimes() { return showtimes; }
}
