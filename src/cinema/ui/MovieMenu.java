package cinema.ui;

import java.util.ArrayList;

import cinema.exception.BookingException;
import cinema.exception.CinemaException;
import cinema.exception.ValidationException;
import cinema.model.CinemaBranch;
import cinema.model.CinemaRoom;
import cinema.model.Movie;
import cinema.model.Showtime;
import cinema.model.VipSeat;
import cinema.service.MovieManager;
import cinema.service.VenueManager;
import cinema.util.ConsoleReader;
import cinema.util.Format;

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
            Screen.title("BROWSE MOVIES");
            Screen.option(1, "Now Showing");
            Screen.option(2, "Coming Soon");
            Screen.option(3, "Search by title or genre");
            Screen.option(4, "View schedules for a movie");
            Screen.option(0, "Back");
            int choice = Screen.readChoice();
            switch (choice) {
                case 1:
                    Screen.section("NOW SHOWING");
                    printMovies(moviesWithStatus("NOW SHOWING"));
                    break;
                case 2:
                    Screen.section("COMING SOON");
                    printMovies(moviesWithStatus("COMING SOON"));
                    break;
                case 3: {
                    String keyword = ConsoleReader.readLine("Title or genre: ").toLowerCase();
                    ArrayList<Movie> result = new ArrayList<>();
                    for (Movie m : movieManager.getMovies()) {
                        if (m.getTitle().toLowerCase().contains(keyword) || m.getGenre().toLowerCase().contains(keyword)) {
                            result.add(m);
                        }
                    }
                    Screen.section("SEARCH RESULTS");
                    printMovies(result);
                    break;
                }
                case 4: {
                    printMovies(movieManager.getMovies());
                    Movie m = movieManager.findMovie(ConsoleReader.readLine("Movie ID (e.g. M-1): "));
                    if (m == null) {
                        Screen.error("Movie not found.");
                    } else {
                        Screen.section("SCHEDULES: " + m.getTitle());
                        printShowtimes(movieManager.getUpcomingShowtimes(m));
                    }
                    break;
                }
                case 0: back = true; break;
                default: Screen.error("Invalid choice. Try again.");
            }
        }
    }

    private ArrayList<Movie> moviesWithStatus(String status) {
        ArrayList<Movie> result = new ArrayList<>();
        for (Movie m : movieManager.getMovies()) {
            if (m.getStatus().equals(status)) result.add(m);
        }
        return result;
    }

    // Admin: add / edit / remove movies
    public void manageMovies() {
        boolean back = false;
        while (!back) {
            Screen.title("MANAGE MOVIES (ADMIN)");
            Screen.option(1, "Add movie");
            Screen.option(2, "Edit movie");
            Screen.option(3, "Remove movie");
            Screen.option(4, "List movies");
            Screen.option(0, "Back");
            int choice = Screen.readChoice();
            switch (choice) {
                case 1: addMovie(); break;
                case 2: editMovie(); break;
                case 3: removeMovie(); break;
                case 4: printMovies(movieManager.getMovies()); break;
                case 0: back = true; break;
                default: Screen.error("Invalid choice. Try again.");
            }
        }
    }

    private void addMovie() {
        String title = ConsoleReader.readLine("Title: ");
        String genre = ConsoleReader.readLine("Genre: ");
        String rating = ConsoleReader.readLine("MTRCB rating (G, PG, R-13, R-16, R-18): ");
        int duration = ConsoleReader.readInt("Duration in minutes: ");
        String status = readStatus();
        try {
            Movie movie = new Movie(movieManager.nextMovieId(), title, genre, rating, duration, status);
            movieManager.addMovie(movie);
            Screen.success("Movie added with ID " + movie.getMovieId() + ".");
        } catch (ValidationException e) {
            Screen.error(e.getMessage());
        }
    }

    private String readStatus() {
        String s = ConsoleReader.readLine("Status (1 = NOW SHOWING, 2 = COMING SOON): ");
        return s.equals("1") ? "NOW SHOWING" : s.equals("2") ? "COMING SOON" : s;
    }

    private void editMovie() {
        Movie m = movieManager.findMovie(ConsoleReader.readLine("Movie ID to edit: "));
        if (m == null) {
            Screen.error("Movie not found.");
            return;
        }
        System.out.println("  " + m);
        System.out.println("   [1] Title  [2] Genre  [3] Age rating  [4] Duration  [5] Status  [0] Cancel");
        int field = ConsoleReader.readInt("Edit which field? ");
        try {
            switch (field) {
                case 1: movieManager.renameMovie(m, ConsoleReader.readLine("New title: ")); break;
                case 2: m.setGenre(ConsoleReader.readLine("New genre: ")); break;
                case 3: m.setAgeRating(ConsoleReader.readLine("New MTRCB rating (G, PG, R-13, R-16, R-18): ")); break;
                case 4: m.setDurationMinutes(ConsoleReader.readInt("New duration in minutes: ")); break;
                case 5: m.setStatus(readStatus()); break;
                case 0: return;
                default: Screen.error("Invalid choice."); return;
            }
            Screen.success("Updated: " + m);
        } catch (ValidationException e) {
            Screen.error(e.getMessage());
        }
    }

    private void removeMovie() {
        String id = ConsoleReader.readLine("Movie ID to remove: ");
        Movie m = movieManager.findMovie(id);
        if (m == null) {
            Screen.error("Movie not found.");
            return;
        }
        if (!ConsoleReader.readYesNo("Remove '" + m.getTitle() + "' and its showtimes? (y/n): ")) {
            Screen.info("Cancelled.");
            return;
        }
        try {
            movieManager.removeMovie(id);
            Screen.success("Movie removed.");
        } catch (BookingException e) {
            Screen.error(e.getMessage());
        }
    }

    // Admin: showtimes
    public void manageShowtimes() {
        boolean back = false;
        while (!back) {
            Screen.title("MANAGE SHOWTIMES (ADMIN)");
            Screen.option(1, "Add showtime");
            Screen.option(2, "View upcoming showtimes");
            Screen.option(0, "Back");
            int choice = Screen.readChoice();
            switch (choice) {
                case 1: addShowtime(); break;
                case 2: printShowtimes(movieManager.getUpcomingShowtimes(null)); break;
                case 0: back = true; break;
                default: Screen.error("Invalid choice. Try again.");
            }
        }
    }

    private void addShowtime() {
        ArrayList<Movie> showing = new ArrayList<>();
        for (Movie m : movieManager.getMovies()) {
            if ("NOW SHOWING".equals(m.getStatus())) showing.add(m);
        }
        if (showing.isEmpty()) {
            Screen.error("No NOW SHOWING movies. Add one first.");
            return;
        }
        printMovies(showing);
        Movie movie = movieManager.findMovie(ConsoleReader.readLine("Movie ID: "));
        if (movie == null || !showing.contains(movie)) {
            Screen.error("Please choose one of the NOW SHOWING movies listed.");
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
            Screen.error("No rooms exist yet. Add a branch and room first.");
            return;
        }
        CinemaRoom room = venueManager.findRoom(ConsoleReader.readLine("Room ID: "));
        if (room == null) {
            Screen.error("Room not found.");
            return;
        }
        if (room.getSeats().isEmpty()) {
            Screen.error("That room has no seats.");
            return;
        }
        String dateTime = ConsoleReader.readLine("Date and time (yyyy-MM-dd HH:mm): ");
        double price = ConsoleReader.readDouble("Regular seat price in PHP (VIP seats add " + Format.peso(VipSeat.EXTRA_CHARGE) + "): ");
        try {
            Showtime s = new Showtime(movieManager.nextShowtimeId(), movie, room, dateTime, price);
            movieManager.addShowtime(s);
            Screen.success("Showtime added with ID " + s.getShowtimeId() + ".");
        } catch (CinemaException e) {   // ValidationException or BookingException
            Screen.error(e.getMessage());
        }
    }

    private void printMovies(ArrayList<Movie> list) {
        if (list.isEmpty()) {
            Screen.info("No movies found.");
            return;
        }
        System.out.println("  " + String.format(Movie.TABLE_FORMAT, "ID", "TITLE", "GENRE", "RATED", "RUNTIME", "STATUS"));
        Screen.line();
        for (Movie m : list) System.out.println("  " + m);
    }

    // Two lines per showtime: when and what, then where, price and seats left
    private void printShowtimes(ArrayList<Showtime> list) {
        if (list.isEmpty()) {
            Screen.info("No upcoming showtimes.");
            return;
        }
        for (Showtime s : list) {
            CinemaBranch b = venueManager.findBranchOfRoom(s.getRoom());
            String cinema = s.getRoom().getRoomName();
            if (b != null) {
                cinema = b.getBranchName() + " - " + cinema;
            }
            System.out.println("  " + String.format("%-5s", s.getShowtimeId()) + " " + Format.shortDateTime(s.getStart())
                    + "   " + s.getMovie().getTitle() + " (" + s.getMovie().getAgeRating() + ")");
            System.out.println("        " + cinema + "   " + Format.peso(s.getBasePrice()) + "   "
                    + s.getAvailableSeats().size() + "/" + s.getCapacity() + " seats left");
        }
    }
}
