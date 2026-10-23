package cinema.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

import cinema.exception.CinemaException;
import cinema.exception.DataException;
import cinema.model.Admin;
import cinema.model.Booking;
import cinema.model.CinemaBranch;
import cinema.model.CinemaRoom;
import cinema.model.Customer;
import cinema.model.Movie;
import cinema.model.Promo;
import cinema.model.Seat;
import cinema.model.Showtime;
import cinema.model.User;
import cinema.payment.CardPayment;
import cinema.payment.CashPayment;
import cinema.payment.EWalletPayment;
import cinema.payment.PaymentStrategy;
import cinema.service.BookingManager;
import cinema.service.MovieManager;
import cinema.service.PromoManager;
import cinema.service.UserManager;
import cinema.service.VenueManager;
import cinema.util.Validator;

// FILE HANDLING: saves everything to text files in the "data" folder and loads it back when the
// program starts, so accounts, bookings and admin changes are kept after closing CURATOR.
// One record per line, values separated by "|". The first line of each file is a "#" comment
// that names the columns, so the files are easy to read and check by hand.
public class FileManager {
    public static final String FOLDER = "data";
    private static final String SEPARATOR = "|";
    private static final String[] FILE_NAMES = {
        "users.txt", "bookings.txt", "branches.txt", "rooms.txt", "movies.txt", "showtimes.txt", "promos.txt"
    };

    private File folder = new File(FOLDER);
    private UserManager userManager;
    private VenueManager venueManager;
    private MovieManager movieManager;
    private PromoManager promoManager;
    private ArrayList<String> warnings = new ArrayList<>();   // damaged lines found while loading
    private ArrayList<String> backedUp = new ArrayList<>();   // files already copied to .bak this time

    public FileManager(UserManager userManager, VenueManager venueManager, MovieManager movieManager,
            PromoManager promoManager) {
        this.userManager = userManager;
        this.venueManager = venueManager;
        this.movieManager = movieManager;
        this.promoManager = promoManager;
    }

    public boolean hasSavedData() {
        return new File(folder, "users.txt").exists();
    }

    public String getFolderPath() {
        return folder.getAbsolutePath();
    }

    // ======================= SAVING =======================

    public void saveAll() throws DataException {
        if (!folder.exists() && !folder.mkdirs()) {
            throw new DataException("Could not create the data folder: " + getFolderPath());
        }
        saveBranches();
        saveRooms();
        saveMovies();
        saveShowtimes();
        saveUsers();
        saveBookings();
        savePromos();
    }

    private void saveUsers() throws DataException {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("# ROLE|ID|NAME|EMAIL|PASSWORD|BIRTHDATE|STATUS|FAILED LOGINS|LOCKED UNTIL|REGISTERED|USERNAME");
        for (User u : userManager.getAllUsers()) {
            String birthDate = "-";
            String username = "-";
            if (u instanceof Customer) {
                birthDate = ((Customer) u).getBirthDate().format(Validator.DATE_FORMAT);
            }
            if (u instanceof Admin) {
                username = ((Admin) u).getUsername();
            }
            String status = "ACTIVE";
            if (!u.isActive()) {
                status = "DEACTIVATED";
            }
            lines.add(toLine(new String[] { u.getRole(), u.getUserId(), u.getName(), u.getEmail(), u.getPassword(),
                    birthDate, status, String.valueOf(u.getFailedLogins()), dateTimeText(u.getLockedUntil()),
                    dateTimeText(u.getRegisteredAt()), username }));
        }
        writeFile("users.txt", lines);
    }

    private void saveBookings() throws DataException {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("# REFERENCE|CUSTOMER ID|SHOWTIME ID|SEATS|TICKET TYPE|DISCOUNT|SUBTOTAL|TOTAL|STATUS|PAYMENT|PAID AT|REFUNDED");
        for (Booking b : BookingManager.getInstance().getAllBookings()) {
            String seatIds = "";
            for (Seat seat : b.getSeats()) {
                if (!seatIds.isEmpty()) {
                    seatIds += ",";
                }
                seatIds += seat.getSeatNumber();
            }
            String payment = "-";
            if (b.getPayment() != null) {
                payment = b.getPayment().getDisplayName();
            }
            lines.add(toLine(new String[] { b.getBookingId(), b.getCustomer().getUserId(),
                    b.getShowtime().getShowtimeId(), seatIds, b.getTicketType(), b.getDiscountName(),
                    String.valueOf(b.getSubtotal()), String.valueOf(b.getTotalPrice()), b.getStatus(), payment,
                    dateTimeText(b.getPaidAt()), String.valueOf(b.getRefundedAmount()) }));
        }
        writeFile("bookings.txt", lines);
    }

    private void saveBranches() throws DataException {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("# ID|NAME|LOCATION");
        for (CinemaBranch b : venueManager.getBranches()) {
            lines.add(toLine(new String[] { b.getBranchId(), b.getBranchName(), b.getLocation() }));
        }
        writeFile("branches.txt", lines);
    }

    private void saveRooms() throws DataException {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("# ID|BRANCH ID|NAME|ROWS|SEATS PER ROW|LAYOUT|SEAT CODES (S = standard, V = VIP)");
        for (CinemaBranch b : venueManager.getBranches()) {
            for (CinemaRoom r : b.getRooms()) {
                lines.add(toLine(new String[] { r.getRoomId(), b.getBranchId(), r.getRoomName(),
                        String.valueOf(r.getRows()), String.valueOf(r.getSeatsPerRow()), r.getLayoutName(),
                        r.getSeatCodes() }));
            }
        }
        writeFile("rooms.txt", lines);
    }

    private void saveMovies() throws DataException {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("# ID|TITLE|GENRE|RATING|MINUTES|STATUS");
        for (Movie m : movieManager.getMovies()) {
            lines.add(toLine(new String[] { m.getMovieId(), m.getTitle(), m.getGenre(), m.getAgeRating(),
                    String.valueOf(m.getDurationMinutes()), m.getStatus() }));
        }
        writeFile("movies.txt", lines);
    }

    private void saveShowtimes() throws DataException {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("# ID|MOVIE ID|ROOM ID|DATE AND TIME|REGULAR SEAT PRICE|STATUS");
        for (Showtime s : movieManager.getShowtimes()) {
            String status = "SCHEDULED";
            if (s.isCancelled()) {
                status = "CANCELLED";
            }
            lines.add(toLine(new String[] { s.getShowtimeId(), s.getMovie().getMovieId(), s.getRoom().getRoomId(),
                    s.getDateTime(), String.valueOf(s.getBasePrice()), status }));
        }
        writeFile("showtimes.txt", lines);
    }

    private void savePromos() throws DataException {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("# CODE|PERCENT OFF|VALID UNTIL|USED BY (customer IDs)");
        for (Promo p : promoManager.getPromos()) {
            String usedBy = String.join(",", p.getUsedBy());
            if (usedBy.isEmpty()) {
                usedBy = "-";
            }
            lines.add(toLine(new String[] { p.getCode(), String.valueOf(p.getPercentOff()),
                    p.getValidUntil().format(Validator.DATE_FORMAT), usedBy }));
        }
        writeFile("promos.txt", lines);
    }

    // Writes all lines to one file, replacing the old file
    private void writeFile(String fileName, ArrayList<String> lines) throws DataException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(new File(folder, fileName)))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            throw new DataException("Could not save " + fileName + ": " + e.getMessage());
        }
    }

    // Joins the values with "|". A "|" inside a value is changed to "/" so the line can be read back.
    private String toLine(String[] values) {
        String line = "";
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line += SEPARATOR;
            }
            line += values[i].replace(SEPARATOR, "/");
        }
        return line;
    }

    // ======================= LOADING =======================

    // Loads every file. Venues and movies come first, because showtimes and bookings point to them.
    // Damaged lines are skipped; the returned list describes each one.
    public ArrayList<String> loadAll() throws DataException {
        warnings.clear();
        backedUp.clear();
        loadBranches();
        loadRooms();
        loadMovies();
        loadShowtimes();
        loadUsers();
        loadPromos();
        loadBookings();
        return warnings;
    }

    private void loadUsers() throws DataException {
        for (String line : readFile("users.txt")) {
            try {
                String[] f = fields(line, 11);
                User user;
                if (f[0].equals("ADMIN")) {
                    user = new Admin(f[1], f[2], f[3], f[4], f[10]);
                } else if (f[0].equals("CUSTOMER")) {
                    user = new Customer(f[1], f[2], f[3], f[4], parseDate(f[5]));
                } else {
                    throw new DataException("unknown role " + f[0]);
                }
                user.loadSavedState(f[6].equals("ACTIVE"), parseDateTime(f[9]), Integer.parseInt(f[7]),
                        parseDateTime(f[8]));
                userManager.addUser(user);
            } catch (CinemaException e) {
                skip("users.txt", line, e.getMessage());
            } catch (IllegalArgumentException e) {
                skip("users.txt", line, "a value is not valid");
            }
        }
    }

    private void loadBookings() throws DataException {
        for (String line : readFile("bookings.txt")) {
            try {
                String[] f = fields(line, 12);
                Customer customer = userManager.findCustomer(f[1]);
                if (customer == null) {
                    throw new DataException("unknown customer " + f[1]);
                }
                Showtime showtime = movieManager.findShowtime(f[2]);
                if (showtime == null) {
                    throw new DataException("unknown showtime " + f[2]);
                }
                boolean confirmed = f[8].equals("CONFIRMED");
                ArrayList<Seat> seats = new ArrayList<>();
                for (String seatId : f[3].split(",")) {
                    Seat seat = showtime.findSeat(seatId);
                    if (seat == null) {
                        throw new DataException("seat " + seatId + " is not in room " + showtime.getRoom().getRoomId());
                    }
                    if (confirmed && showtime.isSeatTaken(seat.getSeatNumber())) {
                        throw new DataException("seat " + seat.getSeatNumber() + " is already taken by another booking");
                    }
                    seats.add(seat);
                }

                Booking booking = new Booking(f[0], customer, showtime, seats, Double.parseDouble(f[6]),
                        Double.parseDouble(f[7]), f[5], f[4]);
                if (!f[10].equals("-")) {
                    booking.loadPayment(paymentFromName(f[9]), parseDateTime(f[10]));
                }
                if (confirmed) {
                    for (Seat seat : seats) {
                        showtime.holdSeat(seat.getSeatNumber());   // the seat shows as taken again
                    }
                } else {
                    booking.cancel();
                    double refunded = Double.parseDouble(f[11]);
                    if (refunded > 0) {
                        booking.markRefunded(refunded);
                    }
                }
                BookingManager.getInstance().restoreBooking(booking);
            } catch (CinemaException e) {
                skip("bookings.txt", line, e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                skip("bookings.txt", line, "a value is not valid");
            }
        }
    }

    private void loadBranches() throws DataException {
        for (String line : readFile("branches.txt")) {
            try {
                String[] f = fields(line, 3);
                venueManager.addBranch(new CinemaBranch(f[0], f[1], f[2]));
            } catch (CinemaException e) {
                skip("branches.txt", line, e.getMessage());
            } catch (IllegalArgumentException e) {
                skip("branches.txt", line, "a value is not valid");
            }
        }
    }

    private void loadRooms() throws DataException {
        for (String line : readFile("rooms.txt")) {
            try {
                String[] f = fields(line, 7);
                CinemaBranch branch = venueManager.findBranch(f[1]);
                if (branch == null) {
                    throw new DataException("unknown branch " + f[1]);
                }
                CinemaRoom room = new CinemaRoom(f[0], f[2]);
                room.loadSeats(Integer.parseInt(f[3]), Integer.parseInt(f[4]), f[6], f[5]);
                venueManager.addLoadedRoom(branch, room);
            } catch (CinemaException e) {
                skip("rooms.txt", line, e.getMessage());
            } catch (IllegalArgumentException e) {
                skip("rooms.txt", line, "a value is not valid");
            }
        }
    }

    private void loadMovies() throws DataException {
        for (String line : readFile("movies.txt")) {
            try {
                String[] f = fields(line, 6);
                movieManager.addMovie(new Movie(f[0], f[1], f[2], f[3], Integer.parseInt(f[4]), f[5]));
            } catch (CinemaException e) {
                skip("movies.txt", line, e.getMessage());
            } catch (IllegalArgumentException e) {
                skip("movies.txt", line, "a value is not valid");
            }
        }
    }

    private void loadShowtimes() throws DataException {
        for (String line : readFile("showtimes.txt")) {
            try {
                String[] f = fields(line, 6);
                Movie movie = movieManager.findMovie(f[1]);
                if (movie == null) {
                    throw new DataException("unknown movie " + f[1]);
                }
                CinemaRoom room = venueManager.findRoom(f[2]);
                if (room == null) {
                    throw new DataException("unknown room " + f[2]);
                }
                Showtime showtime = new Showtime(f[0], movie, room, f[3], Double.parseDouble(f[4]));
                if (f[5].equals("CANCELLED")) {
                    showtime.cancel();
                }
                movieManager.addShowtime(showtime);   // no "past" check: old showtimes are history
            } catch (CinemaException e) {
                skip("showtimes.txt", line, e.getMessage());
            } catch (IllegalArgumentException e) {
                skip("showtimes.txt", line, "a value is not valid");
            }
        }
    }

    private void loadPromos() throws DataException {
        for (String line : readFile("promos.txt")) {
            try {
                String[] f = fields(line, 4);
                Validator.validatePromoCode(f[0]);
                Validator.validateRange("Discount percent", Integer.parseInt(f[1]), 1, 90);
                Promo promo = new Promo(f[0], Integer.parseInt(f[1]), parseDate(f[2]));
                if (!f[3].equals("-")) {
                    for (String customerId : f[3].split(",")) {
                        promo.markUsedBy(customerId);
                    }
                }
                promoManager.restorePromo(promo);
            } catch (CinemaException e) {
                skip("promos.txt", line, e.getMessage());
            } catch (IllegalArgumentException e) {
                skip("promos.txt", line, "a value is not valid");
            }
        }
    }

    // Returns the record lines of a file (comments and blank lines left out).
    // A missing file just means nothing was saved yet.
    private ArrayList<String> readFile(String fileName) throws DataException {
        ArrayList<String> lines = new ArrayList<>();
        File file = new File(folder, fileName);
        if (!file.exists()) {
            return lines;
        }
        try (Scanner reader = new Scanner(file)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                if (!line.trim().isEmpty() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            throw new DataException("Could not read " + fileName + ": " + e.getMessage());
        }
        return lines;
    }

    // Splits a line into its values and checks that the number of values is right
    private String[] fields(String line, int expected) throws DataException {
        String[] values = line.split("\\|", -1);   // "\\|" means a plain "|"; -1 keeps empty values
        if (values.length != expected) {
            throw new DataException("expected " + expected + " values but found " + values.length);
        }
        return values;
    }

    // null is saved as "-"
    private String dateTimeText(LocalDateTime time) {
        if (time == null) {
            return "-";
        }
        return time.format(Validator.DATE_TIME_FORMAT);
    }

    // "-" is read back as null
    private LocalDateTime parseDateTime(String text) throws DataException {
        if (text.equals("-")) {
            return null;
        }
        try {
            return LocalDateTime.parse(text, Validator.DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new DataException("invalid date and time '" + text + "'");
        }
    }

    private LocalDate parseDate(String text) throws DataException {
        try {
            return LocalDate.parse(text, Validator.DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new DataException("invalid date '" + text + "'");
        }
    }

    // Rebuilds the payment method from its saved name, so refunds still go back the same way
    private PaymentStrategy paymentFromName(String name) throws DataException {
        if (name.equals("GCash") || name.equals("Maya")) {
            return new EWalletPayment(name);
        }
        if (name.equals("Credit/Debit Card")) {
            return new CardPayment();
        }
        if (name.equals("Cash")) {
            return new CashPayment();
        }
        throw new DataException("unknown payment method " + name);
    }

    private void skip(String fileName, String line, String reason) {
        String start = line;
        if (start.length() > 30) {
            start = start.substring(0, 30) + "...";
        }
        warnings.add(fileName + ": skipped damaged line \"" + start + "\" (" + reason + ")");
        backup(fileName);
    }

    // The next save rewrites the file without the damaged line, so first keep a copy of the original
    private void backup(String fileName) {
        if (backedUp.contains(fileName)) return;
        backedUp.add(fileName);
        File original = new File(folder, fileName);
        File copy = new File(folder, fileName + ".bak");
        try {
            Files.copy(original.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
            warnings.add(fileName + ": the original file was copied to " + copy.getName());
        } catch (IOException e) {
            warnings.add(fileName + ": could not make a backup copy (" + e.getMessage() + ")");
        }
    }

    // ======================= SUMMARY =======================

    // For the admin "Data Files" screen: each file and how many records it holds
    public ArrayList<String> getFileSummary() throws DataException {
        ArrayList<String> summary = new ArrayList<>();
        for (String name : FILE_NAMES) {
            String count = "not saved yet";
            if (new File(folder, name).exists()) {
                count = readFile(name).size() + " record(s)";
            }
            summary.add(String.format("%-17s %s", name, count));
        }
        summary.add(String.format("%-17s %s", TransactionLog.FILE_NAME, TransactionLog.readAll().size()
                + " payment/refund line(s)"));
        String errors = "no errors";
        if (new File(folder, ErrorLog.FILE_NAME).exists()) {
            errors = "has entries - open it to see the details";
        }
        summary.add(String.format("%-17s %s", ErrorLog.FILE_NAME, errors));
        return summary;
    }
}
