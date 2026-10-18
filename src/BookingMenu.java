import java.util.ArrayList;

// [Member C] Customer booking flow + admin promo/booking handling
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

    // Customer: full booking process
    public void bookTicket(Customer customer) {
        System.out.println("\n--- BOOK A TICKET ---");

        // 1. choose a showtime
        ArrayList<Showtime> options = new ArrayList<>();
        for (Showtime s : movieManager.getUpcomingShowtimes(null)) {
            if ("NOW SHOWING".equals(s.getMovie().getStatus())) options.add(s);
        }
        if (options.isEmpty()) {
            System.out.println("There are no upcoming showtimes right now.");
            return;
        }
        for (Showtime s : options) {
            CinemaBranch b = venueManager.findBranchOfRoom(s.getRoom());
            System.out.println(s.describe(b == null ? null : b.getBranchName()) + " | " + s.getMovie().getAgeRating()
                    + " | " + s.getAvailableSeats().size() + " seats free");
        }
        Showtime showtime = movieManager.findShowtime(ConsoleReader.readLine("Showtime ID (or blank to cancel): "));
        if (showtime == null || !options.contains(showtime)) {
            System.out.println("Booking cancelled (no valid showtime chosen).");
            return;
        }
        if (customer.getAge() < showtime.getMovie().getMinimumAge()) {
            System.out.println("Sorry, " + showtime.getMovie().getAgeRating() + " movies require age "
                    + showtime.getMovie().getMinimumAge() + "+.");
            return;
        }

        // 2. show seat map, choose seats
        showSeatMap(showtime);
        ArrayList<Seat> seats = chooseSeats(showtime);
        if (seats == null) return;

        // 3. choose ticket type
        System.out.println("\nTicket type:  1. Regular   2. Student (20% off)");
        boolean student = ConsoleReader.readInt("Choose: ") == 2;
        if (student) System.out.println("Note: " + new StudentTicket(showtime, seats.get(0)).getEligibilityNote());
        ArrayList<Ticket> tickets = new ArrayList<>();
        for (Seat seat : seats) {
            tickets.add(student ? new StudentTicket(showtime, seat) : new RegularTicket(showtime, seat));
        }

        // 4. apply discount
        DiscountStrategy discount = chooseDiscount(customer);

        // 5. choose payment method
        double total = BookingManager.calculateTotal(tickets, discount);
        System.out.println("\n--- SUMMARY ---");
        System.out.println(showtime.getMovie().getTitle() + " | " + showtime.getDateTime() + " | " + showtime.getRoom().getRoomName());
        for (Ticket t : tickets) {
            System.out.println("  " + t.getSeat() + "  " + t.getTicketType() + "  " + String.format("$%.2f", t.calculatePrice()));
        }
        System.out.println("Discount: " + discount.getName());
        System.out.println(String.format("TOTAL: $%.2f", total));

        System.out.println("\nPayment method:  1. Cash   2. Credit/Debit Card");
        PaymentStrategy payment;
        int pm = ConsoleReader.readInt("Choose: ");
        if (pm == 1) payment = new CashPayment();
        else if (pm == 2) payment = new CardPayment();
        else {
            System.out.println("Invalid payment method. Booking cancelled.");
            return;
        }
        if (!ConsoleReader.readYesNo("Confirm and pay? (y/n): ")) {
            System.out.println("Booking cancelled.");
            return;
        }

        // 6. confirm: hold seats first, then take payment; release seats if payment fails
        Booking booking = null;
        try {
            booking = BookingManager.getInstance().createBooking(customer, showtime, tickets, discount);
            boolean paid = false;
            for (int attempt = 1; attempt <= MAX_PAYMENT_ATTEMPTS && !paid; attempt++) {
                try {
                    payment.pay(booking.getTotalPrice());
                    paid = true;
                } catch (PaymentException e) {
                    System.out.println("Payment failed: " + e.getMessage());
                    if (attempt < MAX_PAYMENT_ATTEMPTS) System.out.println("Try again (" + attempt + "/" + MAX_PAYMENT_ATTEMPTS + ").");
                }
            }
            if (!paid) {
                BookingManager.getInstance().discardBooking(booking);
                System.out.println("Booking cancelled; your seats were released.");
                return;
            }
            booking.setPayment(payment);
            System.out.println("\nBooking confirmed!");
            System.out.println(booking);
        } catch (BookingException e) {
            System.out.println("Booking failed: " + e.getMessage());
        }
    }

    private void showSeatMap(Showtime showtime) {
        System.out.println("\nSEAT MAP  ([ ] free, [XX] taken, * = VIP)    SCREEN THIS WAY");
        String currentRow = "";
        StringBuilder line = new StringBuilder();
        for (Seat seat : showtime.getRoom().getSeats()) {
            String row = seat.getSeatNumber().substring(0, 1);
            if (!row.equals(currentRow)) {
                if (line.length() > 0) System.out.println(line);
                line = new StringBuilder();
                currentRow = row;
            }
            String cell = showtime.isSeatTaken(seat.getSeatNumber()) ? "XX" : seat.getSeatNumber();
            line.append(String.format("[%-3s]", cell)).append(seat.getSeatType().equals("VIP") ? "*" : " ");
        }
        if (line.length() > 0) System.out.println(line);
        System.out.println(String.format("Standard: $%.2f   VIP: $%.2f", showtime.getBasePrice(),
                showtime.getBasePrice() * Seat.VIP_MULTIPLIER));
    }

    // Returns the chosen seats, or null if the customer gave up / input was invalid
    private ArrayList<Seat> chooseSeats(Showtime showtime) {
        String input = ConsoleReader.readLine("Seats, comma-separated (e.g. A1,A2) or blank to cancel: ");
        if (input.isEmpty()) {
            System.out.println("Booking cancelled.");
            return null;
        }
        ArrayList<Seat> chosen = new ArrayList<>();
        for (String part : input.split(",")) {
            String id = part.trim();
            if (id.isEmpty()) continue;
            Seat seat = showtime.findSeat(id);
            if (seat == null) {
                System.out.println("Seat " + id.toUpperCase() + " does not exist.");
                return null;
            }
            if (showtime.isSeatTaken(seat.getSeatNumber())) {
                System.out.println("Seat " + seat.getSeatNumber() + " is already taken.");
                return null;
            }
            if (chosen.contains(seat)) {
                System.out.println("Seat " + seat.getSeatNumber() + " was entered twice.");
                return null;
            }
            chosen.add(seat);
        }
        if (chosen.isEmpty() || chosen.size() > MAX_SEATS_PER_BOOKING) {
            System.out.println("Please choose between 1 and " + MAX_SEATS_PER_BOOKING + " seats.");
            return null;
        }
        return chosen;
    }

    private DiscountStrategy chooseDiscount(Customer customer) {
        boolean senior = customer.getAge() >= SeniorDiscount.MIN_AGE;
        System.out.println("\nDiscount:  1. None   2. Promo code" + (senior ? "   3. Senior (20% off)" : ""));
        int choice = ConsoleReader.readInt("Choose: ");
        if (choice == 2) {
            Promo promo = promoManager.findPromo(ConsoleReader.readLine("Promo code: "));
            if (promo == null) {
                System.out.println("Invalid promo code, continuing without a discount.");
                return new NoDiscount();
            }
            return new PromoDiscount(promo);
        }
        if (choice == 3 && senior) return new SeniorDiscount();
        return new NoDiscount();
    }

    // Customer: see / cancel bookings
    public void viewMyBookings(Customer customer) {
        System.out.println("\n--- MY BOOKINGS ---");
        ArrayList<Booking> mine = BookingManager.getInstance().getBookingsForCustomer(customer.getUserId());
        if (mine.isEmpty()) {
            System.out.println("You have no bookings yet.");
            return;
        }
        for (Booking b : mine) System.out.println(b);

        String id = ConsoleReader.readLine("\nEnter a Booking ID to cancel it (or blank to go back): ");
        if (id.isEmpty()) return;
        try {
            Booking cancelled = BookingManager.getInstance().cancelBooking(id, customer);
            System.out.println("Booking " + cancelled.getBookingId() + " cancelled.");
            if (cancelled.getPayment() != null) {
                cancelled.getPayment().refund(cancelled.getTotalPrice());
            }
        } catch (BookingException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Admin: promo codes
    public void managePromos() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- MANAGE PROMO CODES (ADMIN) ---");
            System.out.println("1. Add promo code");
            System.out.println("2. List promo codes");
            System.out.println("3. Remove promo code");
            System.out.println("0. Back");
            int choice = ConsoleReader.readInt("Choose: ");
            switch (choice) {
                case 1: {
                    String code = ConsoleReader.readLine("New code (3-15 letters/digits): ");
                    int percent = ConsoleReader.readInt("Percent off (1-90): ");
                    try {
                        promoManager.addPromo(code, percent);
                        System.out.println("Promo code added.");
                    } catch (ValidationException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                }
                case 2:
                    if (promoManager.getPromos().isEmpty()) System.out.println("No promo codes.");
                    for (Promo p : promoManager.getPromos()) {
                        System.out.println(p.getCode() + " - " + p.getPercentOff() + "% off");
                    }
                    break;
                case 3:
                    System.out.println(promoManager.removePromo(ConsoleReader.readLine("Code to remove: "))
                            ? "Promo code removed." : "No such promo code.");
                    break;
                case 0: back = true; break;
                default: System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
