package cinema.ui;

import java.time.LocalDate;
import java.util.ArrayList;

import cinema.discount.DiscountStrategy;
import cinema.discount.NoDiscount;
import cinema.discount.PromoDiscount;
import cinema.discount.PwdDiscount;
import cinema.discount.SeniorDiscount;
import cinema.discount.StudentDiscount;
import cinema.exception.BookingException;
import cinema.exception.GoBackException;
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
import cinema.util.Validator;

// [Member C] Customer booking flow + admin promo handling.
// Booking works like a real cinema app: movie -> schedule -> seats -> tickets -> pay -> e-ticket.
// A wrong entry at any step asks again; 0 (or a blank line) cancels the booking.
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

    // The booking goes through numbered steps. Typing /return at any prompt goes back ONE step
    // (and from step 1 back to the customer menu). 0 or a blank line cancels the booking.
    public void bookTicket(Customer customer) {
        Screen.title("BOOK A TICKET");
        Screen.info("Type " + ConsoleReader.RETURN_COMMAND + " at any prompt to go back one step.");
        Movie movie = null;
        Showtime showtime = null;
        ArrayList<Seat> seats = null;
        ArrayList<Ticket> tickets = null;
        DiscountStrategy discount = null;
        PaymentStrategy payment = null;
        double total = 0;

        int step = 1;
        while (true) {
            try {
                if (step == 1) {
                    movie = chooseMovie(customer);
                    if (movie == null) return;
                } else if (step == 2) {
                    showtime = chooseShowtime(movie);
                    if (showtime == null) return;
                } else if (step == 3) {
                    Screen.section("STEP 3 OF 5: CHOOSE YOUR SEATS");
                    System.out.println("  " + Format.shorten(movie.getTitle(), 73));
                    System.out.println("  " + Format.shorten(cinemaName(showtime), 42) + "  |  "
                            + Format.dateTime(showtime.getStart()));
                    SeatMap.print(showtime.getRoom(), showtime);
                    seats = chooseSeats(showtime);
                    if (seats == null) return;
                } else if (step == 4) {
                    Screen.section("STEP 4 OF 5: TICKET TYPE AND DISCOUNT");
                    tickets = chooseTickets(showtime, seats);
                    if (tickets == null) return;
                    discount = chooseDiscountFor(customer, tickets);
                    if (discount == null) return;
                } else if (step == 5) {
                    Screen.section("STEP 5 OF 5: REVIEW AND PAY");
                    total = printOrderSummary(showtime, tickets, discount);
                    payment = choosePayment();
                    if (payment == null) return;
                } else {
                    if (!ConsoleReader.readYesNo("Confirm and pay " + Format.peso(total) + " with "
                            + payment.getDisplayName() + "? (y/n): ")) {
                        Screen.info("Booking cancelled. Nothing was charged.");
                        return;
                    }
                    payAndConfirm(customer, showtime, tickets, discount, payment, total);
                    return;
                }
                step++;
            } catch (GoBackException e) {
                step--;
                if (step < 1) {
                    Screen.backTo("the customer menu");
                    return;
                }
                Screen.info("Going back one step.");
            }
        }
    }

    // STEP 4a: Regular or Student tickets for every seat. Returns null if the booking is cancelled.
    private ArrayList<Ticket> chooseTickets(Showtime showtime, ArrayList<Seat> seats) {
        System.out.println("  Ticket type:");
        Screen.option(1, "Regular");
        Screen.option(2, "Student (20% off, valid school ID required)");
        Screen.option(0, "Cancel booking");
        int ticketType = Screen.readChoice(2);
        if (ticketType == 0) {
            Screen.info("Booking cancelled.");
            return null;
        }
        ArrayList<Ticket> tickets = new ArrayList<>();
        for (Seat seat : seats) {
            if (ticketType == 2) {
                tickets.add(new StudentTicket(showtime, seat));
            } else {
                tickets.add(new RegularTicket(showtime, seat));
            }
        }
        return tickets;
    }

    // STEP 4b: one discount per booking. Student tickets get the student discount and nothing else.
    private DiscountStrategy chooseDiscountFor(Customer customer, ArrayList<Ticket> tickets) {
        if (tickets.get(0) instanceof StudentTicket) {
            Screen.info("Note: " + tickets.get(0).getEligibilityNote());
            Screen.info("The student discount (" + StudentDiscount.PERCENT + "% off) cannot be combined with other discounts.");
            return new StudentDiscount();
        }
        DiscountStrategy discount = chooseDiscount(customer);
        if (discount == null) {
            Screen.info("Booking cancelled.");
        }
        return discount;
    }

    // STEP 6: hold the seats first, then take the payment. If ANYTHING stops us before the payment is saved
    // (a failed payment, a price mismatch, /return, an unexpected error), the finally block releases the seats
    // and forgets the booking, so an unpaid booking can never stay in the system.
    private void payAndConfirm(Customer customer, Showtime showtime, ArrayList<Ticket> tickets,
            DiscountStrategy discount, PaymentStrategy payment, double total) {
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
            // Saved only AFTER the payment succeeded (also marks a promo code as used and logs the payment)
            BookingManager.getInstance().confirmPayment(booking, payment, charged, discount);
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
    private Movie chooseMovie(Customer customer) {
        ArrayList<Movie> movies = new ArrayList<>();
        for (Movie m : movieManager.getMovies()) {
            if (m.isNowShowing() && !movieManager.getUpcomingShowtimes(m).isEmpty()) {
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
            Screen.option(i + 1, Format.fit(m.getTitle(), 32) + "  " + Format.fit(m.getGenre(), 11) + "  "
                    + Format.fit(m.getAgeRating(), 4) + "  " + m.getRuntime());
        }
        Screen.option(0, "Cancel");
        while (true) {
            int choice = Screen.readChoice(movies.size());
            if (choice == 0) {
                Screen.info("Booking cancelled.");
                return null;
            }
            Movie movie = movies.get(choice - 1);
            if (movie.isAllowedForAge(customer.getAge())) {
                return movie;
            }
            Screen.error("Sorry, " + movie.getTitle() + " is rated " + movie.getAgeRating() + ". You must be at least "
                    + movie.getMinimumAge() + " years old to watch it. Please choose another movie.");
        }
    }

    // STEP 2: schedules grouped by branch, numbered in the order they are shown
    private Showtime chooseShowtime(Movie movie) {
        Screen.section("STEP 2 OF 5: CHOOSE A SCHEDULE");
        System.out.println("  " + Format.shorten(movie.getTitle(), 56) + "  (" + movie.getAgeRating() + ", "
                + movie.getRuntime() + ")");
        ArrayList<Showtime> numbered = new ArrayList<>();
        for (CinemaBranch branch : venueManager.getBranches()) {
            boolean branchPrinted = false;
            for (Showtime s : movieManager.getUpcomingShowtimes(movie)) {
                if (venueManager.findBranchOfRoom(s.getRoom()) != branch) continue;
                if (!branchPrinted) {
                    System.out.println();
                    System.out.println("  " + Format.shorten(branch.getBranchName() + "  -  " + branch.getLocation(), 73));
                    branchPrinted = true;
                }
                numbered.add(s);
                String seatsLeft = s.getAvailableSeatCount() + " seats left";
                if (s.isFull()) {
                    seatsLeft = "SOLD OUT";
                }
                Screen.option(numbered.size(), Format.shortDateTime(s.getStart()) + "   "
                        + Format.fit(s.getRoom().getRoomName(), 12) + "  "
                        + Format.fitRight(Format.peso(s.getBasePrice()), 12) + "  " + seatsLeft);
            }
        }
        Screen.option(0, "Cancel");
        while (true) {
            int choice = Screen.readChoice(numbered.size());
            if (choice == 0) {
                Screen.info("Booking cancelled.");
                return null;
            }
            Showtime chosen = numbered.get(choice - 1);
            if (!chosen.isFull()) {
                return chosen;
            }
            Screen.error("Sorry, that schedule is sold out. Please choose another one.");
        }
    }

    // STEP 3: keeps asking until the seats are valid. Returns null if the customer leaves it blank (cancel).
    private ArrayList<Seat> chooseSeats(Showtime showtime) {
        while (true) {
            System.out.println();
            String input = ConsoleReader.readLine("Enter seat codes separated by commas or spaces (e.g. D5,D6), or blank to cancel: ");
            if (input.isEmpty()) {
                Screen.info("Booking cancelled.");
                return null;
            }
            ArrayList<Seat> chosen = readSeats(showtime, input);
            if (chosen != null) {
                Screen.success("Seats selected: " + seatListWithTypes(chosen));
                return chosen;
            }
        }
    }

    // Turns "D5, d6 E1" into seats. Shows the problem and returns null if any seat can't be booked.
    private ArrayList<Seat> readSeats(Showtime showtime, String input) {
        ArrayList<Seat> chosen = new ArrayList<>();
        for (String id : input.trim().split("[,\\s]+")) {   // split on commas and/or spaces
            if (id.isEmpty()) continue;
            Seat seat = showtime.findSeat(id);
            if (seat == null) {
                Screen.error("Seat " + id.toUpperCase() + " does not exist. Please check the seat map.");
                return null;
            }
            if (showtime.isSeatTaken(seat.getSeatNumber())) {
                Screen.error("Seat " + seat.getSeatNumber() + " is already taken. Please choose another seat.");
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
        return chosen;
    }

    // STRATEGY PATTERN: each choice gives a different DiscountStrategy. Returns null if the booking is cancelled.
    private DiscountStrategy chooseDiscount(Customer customer) {
        while (true) {
            System.out.println();
            System.out.println("  Discount:");
            Screen.option(1, "No discount");
            Screen.option(2, "Promo code");
            Screen.option(3, "Senior citizen (20% off, age " + SeniorDiscount.MIN_AGE + "+)");
            Screen.option(4, "PWD (20% off, valid PWD ID required)");
            Screen.option(0, "Cancel booking");
            int choice = Screen.readChoice(4);
            if (choice == 0) {
                return null;
            }
            if (choice == 1) {
                return new NoDiscount();
            }
            if (choice == 2) {
                Promo promo = askPromoCode(customer);
                if (promo != null) {
                    Screen.success("Promo code applied: " + promo.getPercentOff() + "% off.");
                    return new PromoDiscount(promo);
                }
            }
            if (choice == 3) {
                if (customer.getAge() >= SeniorDiscount.MIN_AGE) {
                    return new SeniorDiscount();
                }
                Screen.error("The senior citizen discount is only for customers aged " + SeniorDiscount.MIN_AGE + " and above.");
            }
            if (choice == 4) {
                String idNumber = askPwdId();
                if (idNumber != null) {
                    return new PwdDiscount(idNumber);
                }
            }
            // anything that was not accepted goes back to the discount list
        }
    }

    // Keeps asking until the code can be used. Blank or /return goes back to the discount list (returns null).
    private Promo askPromoCode(Customer customer) {
        while (true) {
            String code;
            try {
                code = ConsoleReader.readLine("Promo code (blank or /return to go back): ");
            } catch (GoBackException e) {
                return null;
            }
            if (code.isEmpty()) {
                return null;
            }
            Promo promo = promoManager.findPromo(code);
            if (promo == null) {
                Screen.error("Promo code " + code.toUpperCase() + " does not exist. Please check the spelling.");
            } else if (promo.isExpired()) {
                Screen.error("Promo code " + promo.getCode() + " expired on " + Format.date(promo.getValidUntil()) + ".");
            } else if (promo.isUsedBy(customer.getUserId())) {
                Screen.error("You have already used promo code " + promo.getCode() + ". Each code can be used once.");
            } else {
                return promo;
            }
        }
    }

    // Keeps asking until the PWD ID looks right. Blank or /return goes back to the discount list (returns null).
    private String askPwdId() {
        while (true) {
            String id;
            try {
                id = ConsoleReader.readLine("PWD ID number (blank or /return to go back): ");
            } catch (GoBackException e) {
                return null;
            }
            if (id.isEmpty()) {
                return null;
            }
            try {
                Validator.validatePwdId(id);
                return id;
            } catch (ValidationException e) {
                Screen.error(e.getMessage());
            }
        }
    }

    // STEP 5: receipt-style summary. Returns the total to pay.
    private double printOrderSummary(Showtime showtime, ArrayList<Ticket> tickets, DiscountStrategy discount) {
        Movie movie = showtime.getMovie();
        System.out.println("  Movie    : " + Format.shorten(movie.getTitle(), 45) + " (" + movie.getAgeRating() + ", "
                + movie.getRuntime() + ")");
        System.out.println("  Cinema   : " + Format.shorten(cinemaName(showtime), 62));
        System.out.println("  Schedule : " + Format.dateTime(showtime.getStart()));
        Screen.line();
        for (Ticket t : tickets) {
            System.out.println("    " + Format.fit(t.getSeat().getSeatNumber(), 6) + " "
                    + Format.fit(t.getSeat().getSeatType(), 10) + " " + Format.fit(t.getTicketType(), 22) + " "
                    + Format.fitRight(Format.peso(BookingManager.round2(t.calculatePrice())), 15));
        }
        double subtotal = BookingManager.calculateSubtotal(tickets);
        double total = BookingManager.calculateTotal(tickets, discount);
        Screen.line();
        printSummaryLine("Subtotal (" + tickets.size() + " ticket/s)", Format.peso(subtotal));
        if (total < subtotal) {
            // a PWD discount shows the ID number, so this label can be long
            printSummaryLine("Less: " + discount.getName(), "- " + Format.peso(BookingManager.round2(subtotal - total)));
        }
        printSummaryLine("TOTAL", Format.peso(total));
        System.out.println("  All prices are VAT-inclusive.");
        return total;
    }

    // One "label ...... amount" line of the summary; the amounts line up on the right
    private void printSummaryLine(String label, String amount) {
        System.out.println("  " + Format.fit(label, 42) + " " + Format.fitRight(amount, 15));
    }

    // STRATEGY PATTERN: each choice gives a different PaymentStrategy. Returns null if cancelled.
    private PaymentStrategy choosePayment() {
        System.out.println();
        System.out.println("  Payment method:");
        Screen.option(1, "GCash");
        Screen.option(2, "Maya");
        Screen.option(3, "Credit/Debit Card (Visa / Mastercard)");
        Screen.option(4, "Cash (over the counter)");
        Screen.option(0, "Cancel booking");
        int choice = Screen.readChoice(4);
        if (choice == 1) return new EWalletPayment("GCash");
        if (choice == 2) return new EWalletPayment("Maya");
        if (choice == 3) return new CardPayment();
        if (choice == 4) return new CashPayment();
        Screen.info("Booking cancelled. Nothing was charged.");
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

    // Public so the admin screens can show the same e-ticket
    public void printETicket(Booking booking) {
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
        Screen.boxLeft("Movie       : " + Format.shorten(s.getMovie().getTitle(), 48) + " (" + s.getMovie().getAgeRating() + ")");
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
                Screen.option(i + 1, Format.fit(b.getBookingId(), 10) + "  "
                        + Format.fit(b.getShowtime().getMovie().getTitle(), 21) + "  "
                        + Format.shortDateTime(b.getShowtime().getStart()) + "  " + b.getStatus());
            }
            Screen.option(0, "Back");
            int choice = Screen.readMenuChoice(mine.size());
            if (choice == 0) {
                back = true;
            } else {
                try {
                    showBooking(mine.get(choice - 1), customer);
                } catch (GoBackException e) {
                    Screen.backTo("My Bookings");
                }
            }
        }
    }

    // Shows the e-ticket, and lets the customer cancel until CANCEL_CUTOFF_HOURS before the show
    private void showBooking(Booking booking, Customer customer) {
        printETicket(booking);
        boolean canCancel = BookingManager.canCustomerCancel(booking);
        if (canCancel) {
            Screen.option(1, "Cancel this booking (full refund)");
        } else if ("CONFIRMED".equals(booking.getStatus()) && !booking.getShowtime().hasStarted()) {
            Screen.info("Online cancellation closes " + BookingManager.CANCEL_CUTOFF_HOURS
                    + " hours before the show. Please go to the cinema counter.");
        }
        Screen.option(0, "Back");
        int lastOption = 0;
        if (canCancel) {
            lastOption = 1;
        }
        int choice = Screen.readChoice(lastOption);
        if (choice != 1) return;
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
        return venueManager.getCinemaName(showtime.getRoom());
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
            int choice = Screen.readMenuChoice(3);
            try {
                switch (choice) {
                    case 1:
                        addPromo();
                        break;
                    case 2:
                        printPromos();
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
            } catch (GoBackException e) {
                Screen.backTo("Manage Promo Codes");
            }
        }
    }

    private void printPromos() {
        if (promoManager.getPromos().isEmpty()) {
            Screen.info("No promo codes.");
            return;
        }
        Table table = new Table(15, 4, 24, 16);
        table.alignRight(1);
        table.printHeader("CODE", "OFF", "STATUS", "USED BY");
        for (Promo p : promoManager.getPromos()) {
            String state = "valid until " + Format.date(p.getValidUntil());
            if (p.isExpired()) {
                state = "EXPIRED on " + Format.date(p.getValidUntil());
            }
            table.printRow(p.getCode(), p.getPercentOff() + "%", state, p.getUsedBy().size() + " customer(s)");
        }
    }

    private void addPromo() {
        String code = ConsoleReader.readLine("New code (3-15 letters/digits): ");
        int percent = ConsoleReader.readInt("Percent off (1-90): ");
        String endDate = ConsoleReader.readLine("Valid until (yyyy-MM-dd, up to " + Validator.MAX_DAYS_AHEAD + " days ahead): ");
        try {
            LocalDate validUntil = Validator.validatePromoEndDate(endDate);
            promoManager.addPromo(code, percent, validUntil);
            Screen.success("Promo code " + code.trim().toUpperCase() + " added (" + percent + "% off until "
                    + Format.date(validUntil) + ", once per customer).");
        } catch (ValidationException e) {
            Screen.error(e.getMessage());
        }
    }
}
