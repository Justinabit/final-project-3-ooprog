package cinema.ui;

import cinema.service.MovieManager;
import cinema.service.VenueManager;

// [Member B] Browse catalog (customer) + manage movies and showtimes (admin)
public class MovieMenu {
    private MovieManager movieManager;
    private VenueManager venueManager;   // needed to pick a room when creating a showtime

    public MovieMenu(MovieManager movieManager, VenueManager venueManager) {
        this.movieManager = movieManager;
        this.venueManager = venueManager;
    }

    // Customer: browse movies
    public void browseMovies() {
        System.out.println("\n--- BROWSE MOVIES ---");
        // TODO (B): list movies, let the user filter by genre or status
    }

    // Admin: add / edit / remove movies, create showtimes
    public void manageMovies() {
        System.out.println("\n--- MANAGE MOVIES (ADMIN) ---");
        // TODO (B): menu -> 1. add movie  2. edit movie  3. remove movie  0. back
    }

    public void manageShowtimes() {
        System.out.println("\n--- MANAGE SHOWTIMES (ADMIN) ---");
        // TODO (B): menu -> 1. add showtime  2. view showtimes  0. back
    }
}
