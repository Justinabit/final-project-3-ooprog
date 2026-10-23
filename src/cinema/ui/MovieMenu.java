package cinema.ui;

import java.util.ArrayList;

import cinema.exception.BookingException;
import cinema.exception.GoBackException;
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
import cinema.util.Validator;

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
            int choice = Screen.readMenuChoice(4);
            try {
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
            } catch (GoBackException e) {
                Screen.backTo("Browse Movies");
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
            int choice = Screen.readMenuChoice(4);
            try {
                switch (choice) {
                    case 1: addMovie(); break;
                    case 2: editMovie(); break;
                    case 3: removeMovie(); break;
                    case 4: printMovies(movieManager.getMovies()); break;
                    case 0: back = true; break;
                    default: Screen.error("Invalid choice. Try again.");
                }
            } catch (GoBackException e) {
                Screen.backTo("Manage Movies");
            }
        }
    }

    private void addMovie() {
        String title = ConsoleReader.readLine("Title: ");
        String genre = readGenre();
        String rating = ConsoleReader.readLine("MTRCB rating (G, PG, R-13, R-16, R-18): ");
        int duration = ConsoleReader.readInt("Duration in minutes (1-600): ");
        String status = readStatus();
        try {
            Movie movie = movieManager.createMovie(title, genre, rating, duration, status);
            Screen.success("Movie added with ID " + movie.getMovieId() + ".");
        } catch (ValidationException e) {
            Screen.error(e.getMessage());
        }
    }

    // Genres come from a fixed list (Validator.GENRES), so they are always spelled the same way
    private String readGenre() {
        System.out.println("  Genres:");
        for (int i = 0; i < Validator.GENRES.length; i++) {
            System.out.print(String.format("   %2d %-12s", i + 1, Validator.GENRES[i]));
            if ((i + 1) % 4 == 0) System.out.println();
        }
        System.out.println();
        while (true) {
            int choice = ConsoleReader.readChoice("Genre number: ", Validator.GENRES.length);
            if (choice >= 1) {
                return Validator.GENRES[choice - 1];
            }
            Screen.error("Please choose a genre from 1 to " + Validator.GENRES.length + ".");
        }
    }

    private String readStatus() {
        while (true) {
            int choice = ConsoleReader.readChoice("Status (1 = NOW SHOWING, 2 = COMING SOON): ", 2);
            if (choice == 1) return "NOW SHOWING";
            if (choice == 2) return "COMING SOON";
            Screen.error("Please type 1 or 2.");
        }
    }

    private void editMovie() {
        Movie m = movieManager.findMovie(ConsoleReader.readLine("Movie ID to edit: "));
        if (m == null) {
            Screen.error("Movie not found.");
            return;
        }
        System.out.println("  " + m);
        System.out.println("   [1] Title  [2] Genre  [3] Age rating  [4] Duration  [5] Status  [0] Cancel");
        int field = ConsoleReader.readChoice("Edit which field? ", 5);
        try {
            switch (field) {
                case 1: movieManager.renameMovie(m, ConsoleReader.readLine("New title: ")); break;
                case 2: m.setGenre(readGenre()); break;
                case 3: m.setAgeRating(ConsoleReader.readLine("New MTRCB rating (G, PG, R-13, R-16, R-18): ")); break;
                case 4: movieManager.changeDuration(m, ConsoleReader.readInt("New duration in minutes (1-600): ")); break;
                case 5: m.setStatus(readStatus()); break;
                case 0: return;
                default: Screen.error("Invalid choice."); return;
            }
            Screen.success("Updated: " + m);
        } catch (CinemaException e) {   // ValidationException, or BookingException for an overlapping duration
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
            Screen.option(3, "Change a showtime's price");
            Screen.option(4, "Cancel a showtime (refunds every booking)");
            Screen.option(0, "Back");
            int choice = Screen.readMenuChoice(4);
            try {
                switch (choice) {
                    case 1: addShowtime(); break;
                    case 2: printShowtimes(movieManager.getUpcomingShowtimes(null)); break;
                    case 3: changePrice(); break;
                    case 4: cancelShowtime(); break;
                    case 0: back = true; break;
                    default: Screen.error("Invalid choice. Try again.");
                }
            } catch (GoBackException e) {
                Screen.backTo("Manage Showtimes");
            }
        }
    }

    private void addShowtime() {
        ArrayList<Movie> showing = new ArrayList<>();
        for (Movie m : movieManager.getMovies()) {
            if (m.isNowShowing()) showing.add(m);
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
                System.out.println("  " + Format.fit(r.getRoomId(), 6) + " | "
                        + Format.shorten(b.getBranchName() + " - " + r.getRoomName(), 50)
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
        Screen.info("Cinema hours: shows start between 9:00 AM and 11:30 PM, up to " + Validator.MAX_DAYS_AHEAD + " days ahead.");
        String dateTime = ConsoleReader.readLine("Date and time (yyyy-MM-dd HH:mm): ");
        double price = ConsoleReader.readAmount("Regular seat price in PHP (" + Format.peso(Validator.MIN_TICKET_PRICE)
                + " - " + Format.peso(Validator.MAX_TICKET_PRICE) + "; VIP seats add " + Format.peso(VipSeat.EXTRA_CHARGE) + "): ");
        try {
            Showtime s = movieManager.createShowtime(movie, room, dateTime, price);
            Screen.success("Showtime added with ID " + s.getShowtimeId() + ".");
        } catch (CinemaException e) {   // ValidationException or BookingException
            Screen.error(e.getMessage());
        }
    }

    // Picks one of the upcoming showtimes by its ID (null if not found)
    private Showtime chooseUpcomingShowtime() {
        ArrayList<Showtime> upcoming = movieManager.getUpcomingShowtimes(null);
        if (upcoming.isEmpty()) {
            Screen.info("No upcoming showtimes.");
            return null;
        }
        printShowtimes(upcoming);
        Showtime s = movieManager.findShowtime(ConsoleReader.readLine("Showtime ID (e.g. S-1): "));
        if (s == null || !upcoming.contains(s)) {
            Screen.error("Please choose one of the upcoming showtimes listed.");
            return null;
        }
        return s;
    }

    // New bookings pay the new price; bookings already made keep what they paid
    private void changePrice() {
        Showtime s = chooseUpcomingShowtime();
        if (s == null) return;
        double price = ConsoleReader.readAmount("New regular seat price in PHP (" + Format.peso(Validator.MIN_TICKET_PRICE)
                + " - " + Format.peso(Validator.MAX_TICKET_PRICE) + "): ");
        try {
            s.setBasePrice(price);
            Screen.success("Price of " + s.getShowtimeId() + " changed to " + Format.peso(price) + ".");
            Screen.info("Bookings that were already made keep the price they paid.");
        } catch (ValidationException e) {
            Screen.error(e.getMessage());
        }
    }

    private void cancelShowtime() {
        Showtime s = chooseUpcomingShowtime();
        if (s == null) return;
        if (!ConsoleReader.readYesNo("Cancel " + s.getMovie().getTitle() + " on " + Format.dateTime(s.getStart())
                + " and refund every booking? (y/n): ")) {
            Screen.info("The showtime was kept.");
            return;
        }
        try {
            int refunded = movieManager.cancelShowtime(s);
            Screen.success("Showtime " + s.getShowtimeId() + " cancelled. " + refunded + " booking(s) were refunded.");
        } catch (BookingException e) {
            Screen.error(e.getMessage());
        }
    }

    private void printMovies(ArrayList<Movie> list) {
        if (list.isEmpty()) {
            Screen.info("No movies found.");
            return;
        }
        Table table = new Table(5, 25, 11, 5, 6, 11);
        table.printHeader("ID", "TITLE", "GENRE", "RATED", "LENGTH", "STATUS");
        for (Movie m : list) {
            table.printRow(m.getMovieId(), m.getTitle(), m.getGenre(), m.getAgeRating(), m.getRuntime(), m.getStatus());
        }
    }

    // Two lines per showtime: when and what, then where, price and seats left. Long titles end with "...".
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
            System.out.println("  " + Format.fit(s.getShowtimeId(), 6) + " " + Format.shortDateTime(s.getStart())
                    + "   " + Format.shorten(s.getMovie().getTitle(), 35) + " (" + s.getMovie().getAgeRating() + ")");
            String seatsInfo = s.isFull() ? "SOLD OUT"
                    : s.getAvailableSeatCount() + "/" + s.getCapacity() + " seats left";
            System.out.println("        " + Format.shorten(cinema, 30) + "   " + Format.peso(s.getBasePrice())
                    + "   " + seatsInfo);
        }
    }
}