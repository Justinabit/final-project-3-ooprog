package cinema.service;

import java.util.ArrayList;

import cinema.exception.BookingException;
import cinema.exception.ValidationException;
import cinema.model.Movie;
import cinema.model.Showtime;

// [Member B] Stores movies and showtimes
public class MovieManager {
    private ArrayList<Movie> movies = new ArrayList<>();
    private ArrayList<Showtime> showtimes = new ArrayList<>();

    public void addMovie(Movie movie) throws ValidationException {
        // TODO (B): validate, then add (no duplicate titles)
    }

    public void removeMovie(String movieId) {
        // TODO (B): remove the movie (what about its showtimes? decide as a team)
    }

    public void addShowtime(Showtime showtime) throws BookingException {
        // TODO (B): prevent double-booking: same room can't have two showtimes at the same time
        showtimes.add(showtime);
    }

    public Showtime findShowtime(String showtimeId) {
        // TODO (B): search the list
        return null;
    }

    public ArrayList<Movie> getMovies() { return movies; }
    public ArrayList<Showtime> getShowtimes() { return showtimes; }
}
