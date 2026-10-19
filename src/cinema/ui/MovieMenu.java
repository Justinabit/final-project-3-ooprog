package cinema.ui;

import java.util.ArrayList;

import cinema.exception.BookingException;
import cinema.exception.CinemaException;
import cinema.exception.ValidationException;
import cinema.model.CinemaBranch;
import cinema.model.CinemaRoom;
import cinema.model.Movie;
import cinema.model.Showtime;
import cinema.service.MovieManager;
import cinema.service.VenueManager;
import cinema.util.ConsoleReader;

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
        boolean back = false;
        while (!back) {
            System.out.println("\n--- BROWSE MOVIES ---");
            System.out.println("1. All movies");
            System.out.println("2. Filter by genre");
            System.out.println("3. Filter by status (NOW SHOWING / COMING SOON)");
            System.out.println("4. View showtimes for a movie");
            System.out.println("0. Back");
            int choice = ConsoleReader.readInt("Choose: ");
            switch (choice) {
                case 1:
                    printMovies(movieManager.getMovies());
                    break;
                case 2: {
                    String genre = ConsoleReader.readLine("Genre: ").toLowerCase();
                    ArrayList<Movie> result = new ArrayList<>();
                    for (Movie m : movieManager.getMovies()) {
                        if (m.getGenre().toLowerCase().contains(genre)) result.add(m);
                    }
                    printMovies(result);
                    break;
                }
                case 3: {
                    String status = ConsoleReader.readLine("Status (1 = NOW SHOWING, 2 = COMING SOON): ");
                    String wanted = status.equals("1") ? "NOW SHOWING" : status.equals("2") ? "COMING SOON" : null;
                    if (wanted == null) {
                        System.out.println("Please enter 1 or 2.");
                        break;
                    }
                    ArrayList<Movie> result = new ArrayList<>();
                    for (Movie m : movieManager.getMovies()) {
                        if (m.getStatus().equals(wanted)) result.add(m);
                    }
                    printMovies(result);
                    break;
                }
                case 4: {
                    Movie m = movieManager.findMovie(ConsoleReader.readLine("Movie ID: "));
                    if (m == null) {
                        System.out.println("Movie not found.");
                    } else {
                        printShowtimes(movieManager.getUpcomingShowtimes(m));
                    }
                    break;
                }
                case 0: back = true; break;
                default: System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // Admin: add / edit / remove movies
    public void manageMovies() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- MANAGE MOVIES (ADMIN) ---");
            System.out.println("1. Add movie");
            System.out.println("2. Edit movie");
            System.out.println("3. Remove movie");
            System.out.println("4. List movies");
            System.out.println("0. Back");
            int choice = ConsoleReader.readInt("Choose: ");
            switch (choice) {
                case 1: addMovie(); break;
                case 2: editMovie(); break;
                case 3: removeMovie(); break;
                case 4: printMovies(movieManager.getMovies()); break;
                case 0: back = true; break;
                default: System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void addMovie() {
        String title = ConsoleReader.readLine("Title: ");
        String genre = ConsoleReader.readLine("Genre: ");
        String rating = ConsoleReader.readLine("Age rating (G, PG, R-16, R-18): ");
        int duration = ConsoleReader.readInt("Duration in minutes: ");
        String status = readStatus();
        try {
            Movie movie = new Movie(movieManager.nextMovieId(), title, genre, rating, duration, status);
            movieManager.addMovie(movie);
            System.out.println("Movie added with ID " + movie.getMovieId() + ".");
        } catch (ValidationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private String readStatus() {
        String s = ConsoleReader.readLine("Status (1 = NOW SHOWING, 2 = COMING SOON): ");
        return s.equals("1") ? "NOW SHOWING" : s.equals("2") ? "COMING SOON" : s;
    }

    private void editMovie() {
        Movie m = movieManager.findMovie(ConsoleReader.readLine("Movie ID to edit: "));
        if (m == null) {
            System.out.println("Movie not found.");
            return;
        }
        System.out.println(m);
        System.out.println("1. Title  2. Genre  3. Age rating  4. Duration  5. Status  0. Cancel");
        int field = ConsoleReader.readInt("Edit which field? ");
        try {
            switch (field) {
                case 1: movieManager.renameMovie(m, ConsoleReader.readLine("New title: ")); break;
                case 2: m.setGenre(ConsoleReader.readLine("New genre: ")); break;
                case 3: m.setAgeRating(ConsoleReader.readLine("New age rating (G, PG, R-16, R-18): ")); break;
                case 4: m.setDurationMinutes(ConsoleReader.readInt("New duration in minutes: ")); break;
                case 5: m.setStatus(readStatus()); break;
                case 0: return;
                default: System.out.println("Invalid choice."); return;
            }
            System.out.println("Updated: " + m);
        } catch (ValidationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void removeMovie() {
        String id = ConsoleReader.readLine("Movie ID to remove: ");
        Movie m = movieManager.findMovie(id);
        if (m == null) {
            System.out.println("Movie not found.");
            return;
        }
        if (!ConsoleReader.readYesNo("Remove '" + m.getTitle() + "' and its showtimes? (y/n): ")) {
            System.out.println("Cancelled.");
            return;
        }
        try {
            movieManager.removeMovie(id);
            System.out.println("Movie removed.");
        } catch (BookingException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Admin: showtimes
    public void manageShowtimes() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- MANAGE SHOWTIMES (ADMIN) ---");
            System.out.println("1. Add showtime");
            System.out.println("2. View upcoming showtimes");
            System.out.println("0. Back");
            int choice = ConsoleReader.readInt("Choose: ");
            switch (choice) {
                case 1: addShowtime(); break;
                case 2: printShowtimes(movieManager.getUpcomingShowtimes(null)); break;
                case 0: back = true; break;
                default: System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void addShowtime() {
        ArrayList<Movie> showing = new ArrayList<>();
        for (Movie m : movieManager.getMovies()) {
            if ("NOW SHOWING".equals(m.getStatus())) showing.add(m);
        }
        if (showing.isEmpty()) {
            System.out.println("No NOW SHOWING movies. Add one first.");
            return;
        }
        printMovies(showing);
        Movie movie = movieManager.findMovie(ConsoleReader.readLine("Movie ID: "));
        if (movie == null || !showing.contains(movie)) {
            System.out.println("Please choose one of the NOW SHOWING movies listed.");
            return;
        }

        boolean anyRoom = false;
        for (CinemaBranch b : venueManager.getBranches()) {
            for (CinemaRoom r : b.getRooms()) {
                System.out.println("  " + r.getRoomId() + " | " + b.getBranchName() + " - " + r.getRoomName()
                        + " (" + r.getSeats().size() + " seats)");
                anyRoom = true;
            }
        }
        if (!anyRoom) {
            System.out.println("No rooms exist yet. Add a branch and room first.");
            return;
        }
        CinemaRoom room = venueManager.findRoom(ConsoleReader.readLine("Room ID: "));
        if (room == null) {
            System.out.println("Room not found.");
            return;
        }
        if (room.getSeats().isEmpty()) {
            System.out.println("That room has no seats.");
            return;
        }
        String dateTime = ConsoleReader.readLine("Date and time (yyyy-MM-dd HH:mm): ");
        double price = ConsoleReader.readDouble("Base ticket price: ");
        try {
            Showtime s = new Showtime(movieManager.nextShowtimeId(), movie, room, dateTime, price);
            movieManager.addShowtime(s);
            System.out.println("Showtime added with ID " + s.getShowtimeId() + ".");
        } catch (CinemaException e) {   // ValidationException or BookingException
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void printMovies(ArrayList<Movie> list) {
        if (list.isEmpty()) {
            System.out.println("No movies found.");
            return;
        }
        for (Movie m : list) System.out.println(m);
    }

    private void printShowtimes(ArrayList<Showtime> list) {
        if (list.isEmpty()) {
            System.out.println("No upcoming showtimes.");
            return;
        }
        for (Showtime s : list) {
            CinemaBranch b = venueManager.findBranchOfRoom(s.getRoom());
            System.out.println(s.describe(b == null ? null : b.getBranchName())
                    + " | " + s.getAvailableSeats().size() + "/" + s.getCapacity() + " seats free");
        }
    }
}
