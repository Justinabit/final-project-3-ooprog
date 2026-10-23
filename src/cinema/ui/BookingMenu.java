package cinema.ui;

import java.util.ArrayList;

import cinema.discount.DiscountStrategy;
import cinema.discount.NoDiscount;
import cinema.discount.PromoDiscount;
import cinema.discount.PwdDiscount;
import cinema.discount.SeniorDiscount;
import cinema.discount.StudentDiscount;
import cinema.exception.BookingException;
import cinema.exception.PaymentException;
import cinema.exception.ValidationException;
import cinema.model.Booking;
import cinema.model.CinemaBranch;
import cinema.model.Customer;
import cinema.model.Movie;
import cinema.model.Promo;
import cinema.model.RegularTicket;
import cinema.model.Seat;
import cinema.model.Showtime;
import cinema.model.StudentTicket;
import cinema.model.Ticket;
import cinema.payment.CardPayment;
import cinema.payment.CashPayment;
import cinema.payment.EWalletPayment;
import cinema.payment.PaymentStrategy;
import cinema.service.BookingManager;
import cinema.service.MovieManager;
import cinema.service.PromoManager;
import cinema.service.VenueManager;
import cinema.util.ConsoleReader;
import cinema.util.Format;

// [Member C] Customer booking flow + admin promo handling.
// Booking works like a real cinema app: movie -> schedule -> seats -> tickets -> pay -> e-ticket.
public class BookingMenu {
    private static final int MAX_SEATS_PER_BOOKING = 8;
    private static final int MAX_PAYMENT_ATTEMPTS = 3;

    private MovieManager movieManager;
    private VenueManager venueManager;
    private PromoManager promoManager;

    public BookingMenu(MovieManager movieManager, VenueManager venueManager, PromoManager promoManager) {
        this.movieManager = movieManager;
        this.venueManager = venueManager;
        this.promoManager = promoManager;
    }

    // ---------- customer: book a ticket ----------

    public void bookTicket(Customer customer) {
        Screen.title("BOOK A TICKET");

        // STEP 1: movie
        Movie movie = chooseMovie();
        if (movie == null) return;
        if (customer.getAge() < movie.getMinimumAge()) {
            Screen.error("Sorry, " + movie.getTitle() + " is rated " + movie.getAgeRating()
                    + ". You must be at least " + movie.getMinimumAge() + " years old to watch it.");
            return;
        }

        // STEP 2: schedule
        Showtime showtime = chooseShowtime(movie);
        if (showtime == null) return;

        // STEP 3: seats
        Screen.section("STEP 3 OF 5: CHOOSE YOUR SEATS");
        System.out.println("  " + movie.getTitle() + "  |  " + cinemaName(showtime) + "  |  "
                + Format.dateTime(showtime.getStart()));
        SeatMap.print(showtime.getRoom(), showtime);
        ArrayList<Seat> seats = chooseSeats(showtime);
        if (seats == null) return;

        // STEP 4: ticket type and discount
        Screen.section("STEP 4 OF 5: TICKET TYPE AND DISCOUNT");
        boolean student = chooseStudentTicket();
        ArrayList<Ticket> tickets = new ArrayList<>();
        for (Seat seat : seats) {
            if (student) {
                tickets.add(new StudentTicket(showtime, seat));
            } else {
                tickets.add(new RegularTicket(showtime, seat));
            }
        }
        DiscountStrategy discount;
        if (student) {
            // One discount per booking: student tickets get the student discount and nothing else.
            Screen.info("Note: " + tickets.get(0).getEligibilityNote());
            Screen.info("The student discount (" + StudentDiscount.PERCENT + "% off) cannot be combined with other discounts.");
            discount = new StudentDiscount();
        } else {
            discount = chooseDiscount(customer);
        }

        // STEP 5: review and pay
        Screen.section("STEP 5 OF 5: REVIEW AND PAY");
        double total = printOrderSummary(showtime, tickets, discount);
        PaymentStrategy payment = choosePayment();
        if (payment == null) return;
        if (!ConsoleReader.readYesNo("Confirm and pay " + Format.peso(total) + " with " + payment.getDisplayName() + "? (y/n): ")) {
            Screen.info("Booking cancelled. Nothing was charged.");
            return;
        }

        // Hold the seats first, then take the payment. If ANYTHING stops us before the payment is saved
        // (a failed payment, a price mismatch, an unexpected error), the finally block releases the seats
        // and forgets the booking, so an unpaid booking can never stay in the system.
        Booking booking = null;
        boolean paid = false;
        try {
            booking = BookingManager.getInstance().createBooking(customer, showtime, tickets, discount);
            double charged = booking.getTotalPrice();
            // The customer approved `total` above. We only charge that exact amount.
            if (Double.compare(charged, total) != 0) {
                Screen.error("The price changed while booking (" + Format.peso(total) + " -> "
                        + Format.peso(charged) + "). Nothing was charged. Please try again.");
                return;
            }
            if (!takePayment(payment, charged)) {
                Screen.info("Booking cancelled; your seats were released.");
                return;
            }
            booking.recordPayment(payment, charged);   // saved only AFTER the payment succeeded
            paid = true;
            Screen.success("Payment successful! Your booking is confirmed.");
            printETicket(booking);
        } catch (BookingException e) {
            Screen.error("Booking failed: " + e.getMessage());
        } finally {
            if (booking != null && !paid) {
                BookingManager.getInstance().discardBooking(booking);
            }
        }
    }

    // STEP 1: only NOW SHOWING movies that still have upcoming schedules
    private Movie chooseMovie() {
        ArrayList<Movie> movies = new ArrayList<>();
        for (Movie m : movieManager.getMovies()) {
            if ("NOW SHOWING".equals(m.getStatus()) && !movieManager.getUpcomingShowtimes(m).isEmpty()) {
                movies.add(m);
            }
        }
        if (movies.isEmpty()) {
            Screen.info("There are no upcoming showtimes right now.");
            return null;
        }
        Screen.section("STEP 1 OF 5: CHOOSE A MOVIE");
        for (int i = 0; i < movies.size(); i++) {
            Movie m = movies.get(i);
            Screen.option(i + 1, String.format("%-28s %-9s %-5s %s", m.getTitle(), m.getGenre(),
                    m.getAgeRating(), m.getRuntime()));
        }
        Screen.option(0, "Cancel");
        int choice = Screen.readChoice();
        if (choice < 1 || choice > movies.size()) {
            Screen.info("Booking cancelled.");
            return null;
        }
        return movies.get(choice - 1);
    }

    // STEP 2: schedules grouped by branch, numbered in the order they are shown
    private Showtime chooseShowtime(Movie movie) {
        Screen.section("STEP 2 OF 5: CHOOSE A SCHEDULE");
        System.out.println("  " + movie.getTitle() + "  (" + movie.getAgeRating() + ", " + movie.getRuntime() + ")");
        ArrayList<Showtime> numbered = new ArrayList<>();
        for (CinemaBranch branch : venueManager.getBranches()) {
            boolean branchPrinted = false;
            for (Showtime s : movieManager.getUpcomingShowtimes(movie)) {
                if (venueManager.findBranchOfRoom(s.getRoom()) != branch) continue;
                if (!branchPrinted) {
                    System.out.println();
                    System.out.println("  " + branch.getBranchName() + "  -  " + branch.getLocation());
                    branchPrinted = true;
                }
                numbered.add(s);
                String seatsLeft = s.getAvailableSeats().size() + " seats left";
                if (s.getAvailableSeats().isEmpty()) {
                    seatsLeft = "SOLD OUT";
                }
                Screen.option(numbered.size(), Format.shortDateTime(s.getStart()) + "   "
                        + String.format("%-10s", s.getRoom().getRoomName()) + "   "
                        + Format.peso(s.getBasePrice()) + "   " + seatsLeft);
            }
        }
        Screen.option(0, "Cancel");
        int choice = Screen.readChoice();
        if (choice < 1 || choice > numbered.size()) {
            Screen.info("Booking cancelled.");
            return null;
        }
        Showtime chosen = numbered.get(choice - 1);
        if (chosen.getAvailableSeats().isEmpty()) {
            Screen.error("Sorry, that schedule is sold out. Please choose another one.");
            return null;
        }
        return chosen;
    }

    // STEP 3: returns the chosen seats, or null if the customer gave up / input was invalid
    private ArrayList<Seat> chooseSeats(Showtime showtime) {
        System.out.println();
        String input = ConsoleReader.readLine("Enter seat codes separated by commas (e.g. D5,D6), or blank to cancel: ");
        if (input.isEmpty()) {
            Screen.info("Booking cancelled.");
            return null;
        }
        ArrayList<Seat> chosen = new ArrayList<>();
        for (String part : input.split(",")) {
            String id = part.trim();
            if (id.isEmpty()) continue;
            Seat seat = showtime.findSeat(id);
            if (seat == null) {
                Screen.error("Seat " + id.toUpperCase() + " does not exist.");
                return null;
            }
            if (showtime.isSeatTaken(seat.getSeatNumber())) {
                Screen.error("Seat " + seat.getSeatNumber() + " is already taken.");
                return null;
            }
            if (chosen.contains(seat)) {
                Screen.error("Seat " + seat.getSeatNumber() + " was entered twice.");
                return null;
            }
            chosen.add(seat);
        }
        if (chosen.isEmpty() || chosen.size() > MAX_SEATS_PER_BOOKING) {
            Screen.error("Please choose between 1 and " + MAX_SEATS_PER_BOOKING + " seats.");
            return null;
        }
        Screen.success("Seats selected: " + seatListWithTypes(chosen));
        return chosen;
    }

    private boolean chooseStudentTicket() {
        System.out.println("  Ticket type:");
        Screen.option(1, "Regular");
        Screen.option(2, "Student (20% off, valid school ID required)");
        return Screen.readChoice() == 2;
    }

    // STRATEGY PATTERN: each choice gives a different DiscountStrategy
    private DiscountStrategy chooseDiscount(Customer customer) {
        System.out.println();
        System.out.println("  Discount:");
        Screen.option(1, "No discount");
        Screen.option(2, "Promo code");
        Screen.option(3, "Senior citizen (20% off, age " + SeniorDiscount.MIN_AGE + "+)");
        Screen.option(4, "PWD (20% off, valid PWD ID required)");
        int choice = Screen.readChoice();
        if (choice == 2) {
            Promo promo = promoManager.findPromo(ConsoleReader.readLine("Promo code: "));
            if (promo == null) {
                Screen.error("Invalid promo code, continuing without a discount.");
                return new NoDiscount();
            }
            Screen.success("Promo code applied: " + promo.getPercentOff() + "% off.");
            return new PromoDiscount(promo);
        }
        if (choice == 3) {
            if (customer.getAge() >= SeniorDiscount.MIN_AGE) {
                return new SeniorDiscount();
            }
            Screen.error("The senior citizen discount is only for customers aged " + SeniorDiscount.MIN_AGE
                    + " and above. Continuing without a discount.");
            return new NoDiscount();
        }
        if (choice == 4) {
            String idNumber = ConsoleReader.readLine("PWD ID number: ");
            if (idNumber.isEmpty()) {
                Screen.error("A PWD ID number is required. Continuing without a discount.");
                return new NoDiscount();
            }
            return new PwdDiscount(idNumber);
        }
        return new NoDiscount();
    }

    // STEP 5: receipt-style summary. Returns the total to pay.
    private double printOrderSummary(Showtime showtime, ArrayList<Ticket> tickets, DiscountStrategy discount) {
        Movie movie = showtime.getMovie();
        System.out.println("  Movie    : " + movie.getTitle() + " (" + movie.getAgeRating() + ", " + movie.getRuntime() + ")");
        System.out.println("  Cinema   : " + cinemaName(showtime));
        System.out.println("  Schedule : " + Format.dateTime(showtime.getStart()));
        Screen.line();
        for (Ticket t : tickets) {
            System.out.println(String.format("    %-6s %-10s %-22s %15s", t.getSeat().getSeatNumber(),
                    t.getSeat().getSeatType(), t.getTicketType(),
                    Format.peso(BookingManager.round2(t.calculatePrice()))));
        }
        double subtotal = BookingManager.calculateSubtotal(tickets);
        double total = BookingManager.calculateTotal(tickets, discount);
        Screen.line();
        System.out.println(String.format("  %-42s %15s", "Subtotal (" + tickets.size() + " ticket/s)", Format.peso(subtotal)));
        if (total < subtotal) {
            System.out.println(String.format("  %-42s %15s", "Less: " + discount.getName(),
                    "- " + Format.peso(BookingManager.round2(subtotal - total))));
        }
        System.out.println(String.format("  %-42s %15s", "TOTAL", Format.peso(total)));
        System.out.println("  All prices are VAT-inclusive.");
        return total;
    }

    // STRATEGY PATTERN: each choice gives a different PaymentStrategy
    private PaymentStrategy choosePayment() {
        System.out.println();
        System.out.println("  Payment method:");
        Screen.option(1, "GCash");
        Screen.option(2, "Maya");
        Screen.option(3, "Credit/Debit Card");
        Screen.option(4, "Cash (over the counter)");
        int choice = Screen.readChoice();
        if (choice == 1) return new EWalletPayment("GCash");
        if (choice == 2) return new EWalletPayment("Maya");
        if (choice == 3) return new CardPayment();
        if (choice == 4) return new CashPayment();
        Screen.error("Invalid payment method. Booking cancelled.");
        return null;
    }

    // The same pay() call works for every payment method (POLYMORPHISM). Allows 3 attempts.
    private boolean takePayment(PaymentStrategy payment, double amount) {
        for (int attempt = 1; attempt <= MAX_PAYMENT_ATTEMPTS; attempt++) {
            try {
                payment.pay(amount);
                return true;
            } catch (PaymentException e) {
                Screen.error("Payment failed: " + e.getMessage());
                if (attempt < MAX_PAYMENT_ATTEMPTS) {
                    Screen.info("Try again (" + attempt + "/" + MAX_PAYMENT_ATTEMPTS + ").");
                }
            }
        }
        return false;
    }

    private void printETicket(Booking booking) {
        Showtime s = booking.getShowtime();
        String paidWith = "-";
        if (booking.getPayment() != null) {
            paidWith = booking.getPayment().getDisplayName();
        }
        System.out.println();
        Screen.border();
        Screen.boxLine("C U R A T O R    E - T I C K E T");
        Screen.boxDivider();
        Screen.boxLeft("Booking Ref : " + booking.getBookingId());
        Screen.boxLeft("Movie       : " + s.getMovie().getTitle() + " (" + s.getMovie().getAgeRating() + ")");
        Screen.boxLeft("Cinema      : " + cinemaName(s));
        Screen.boxLeft("Schedule    : " + Format.dateTime(s.getStart()));
        Screen.boxLeft("Seats       : " + seatListWithTypes(booking.getSeats()));
        Screen.boxLeft("Tickets     : " + booking.getSeats().size() + " x " + booking.getTicketType());
        Screen.boxLeft("Subtotal    : " + Format.peso(booking.getSubtotal()));
        Screen.boxLeft("Discount    : " + booking.getDiscountName());
        if (booking.getDiscountAmount() > 0) {
            Screen.boxLeft("You saved   : " + Format.peso(booking.getDiscountAmount()));
        }
        Screen.boxLeft("Amount Paid : " + Format.peso(booking.getAmountPaid()) + " (" + paidWith + ")");
        if (booking.isRefunded()) {
            Screen.boxLeft("Refunded    : " + Format.peso(booking.getRefundedAmount()));
        }
        Screen.boxLeft("Status      : " + booking.getStatus());
        Screen.boxDivider();
        Screen.boxLine("Please arrive 15 minutes before the show starts.");
        Screen.boxLine("Show this e-ticket at the cinema entrance. Enjoy the movie!");
        Screen.border();
    }

    // ---------- customer: my bookings ----------

    public void viewMyBookings(Customer customer) {
        boolean back = false;
        while (!back) {
            Screen.title("MY BOOKINGS");
            ArrayList<Booking> mine = BookingManager.getInstance().getBookingsForCustomer(customer.getUserId());
            if (mine.isEmpty()) {
                Screen.info("You have no bookings yet. Choose 'Book a Ticket' to get started.");
                return;
            }
            for (int i = 0; i < mine.size(); i++) {
                Booking b = mine.get(i);
                Screen.option(i + 1, String.format("%-10s %-25s %-21s %s", b.getBookingId(),
                        b.getShowtime().getMovie().getTitle(), Format.shortDateTime(b.getShowtime().getStart()),
                        b.getStatus()));
            }
            Screen.option(0, "Back");
            int choice = Screen.readChoice();
            if (choice == 0) {
                back = true;
            } else if (choice < 1 || choice > mine.size()) {
                Screen.error("Invalid choice. Try again.");
            } else {
                showBooking(mine.get(choice - 1), customer);
            }
        }
    }

    // Shows the e-ticket, and lets the customer cancel if the show hasn't started yet
    private void showBooking(Booking booking, Customer customer) {
        printETicket(booking);
        boolean canCancel = "CONFIRMED".equals(booking.getStatus()) && !booking.getShowtime().hasStarted();
        if (canCancel) {
            Screen.option(1, "Cancel this booking");
        }
        Screen.option(0, "Back");
        int choice = Screen.readChoice();
        if (choice != 1 || !canCancel) return;
        if (!ConsoleReader.readYesNo("Cancel booking " + booking.getBookingId() + "? (y/n): ")) {
            Screen.info("Your booking was kept.");
            return;
        }
        try {
            Booking cancelled = BookingManager.getInstance().cancelBooking(booking.getBookingId(), customer);
            // BookingManager already refunded the payment as part of cancelling.
            Screen.success("Booking " + cancelled.getBookingId() + " cancelled.");
        } catch (BookingException e) {
            Screen.error(e.getMessage());
        }
    }

    // ---------- helpers ----------

    private String cinemaName(Showtime showtime) {
        CinemaBranch branch = venueManager.findBranchOfRoom(showtime.getRoom());
        if (branch == null) {
            return showtime.getRoom().getRoomName();
        }
        return branch.getBranchName() + " - " + showtime.getRoom().getRoomName();
    }

    // "D5, D6, F1 (VIP)"
    private String seatListWithTypes(ArrayList<Seat> seats) {
        String result = "";
        for (Seat seat : seats) {
            if (!result.isEmpty()) {
                result += ", ";
            }
            result += seat.toString();
        }
        return result;
    }

    // ---------- admin: promo codes ----------

    public void managePromos() {
        boolean back = false;
        while (!back) {
            Screen.title("MANAGE PROMO CODES (ADMIN)");
            Screen.option(1, "Add promo code");
            Screen.option(2, "List promo codes");
            Screen.option(3, "Remove promo code");
            Screen.option(0, "Back");
            int choice = Screen.readChoice();
            switch (choice) {
                case 1: {
                    String code = ConsoleReader.readLine("New code (3-15 letters/digits): ");
                    int percent = ConsoleReader.readInt("Percent off (1-90): ");
                    try {
                        promoManager.addPromo(code, percent);
                        Screen.success("Promo code added.");
                    } catch (ValidationException e) {
                        Screen.error(e.getMessage());
                    }
                    break;
                }
                case 2:
                    if (promoManager.getPromos().isEmpty()) Screen.info("No promo codes.");
                    for (Promo p : promoManager.getPromos()) {
                        System.out.println("  " + p.getCode() + " - " + p.getPercentOff() + "% off");
                    }
                    break;
                case 3:
                    if (promoManager.removePromo(ConsoleReader.readLine("Code to remove: "))) {
                        Screen.success("Promo code removed.");
                    } else {
                        Screen.error("No such promo code.");
                    }
                    break;
                case 0: back = true; break;
                default: Screen.error("Invalid choice. Try again.");
            }
        }
    }
}