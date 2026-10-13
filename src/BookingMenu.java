// [Member C] Customer booking flow + admin promo/booking handling
public class BookingMenu {
    private MovieManager movieManager;

    public BookingMenu(MovieManager movieManager) {
        this.movieManager = movieManager;
    }

    // Customer: full booking process
    public void bookTicket(Customer customer) {
        System.out.println("\n--- BOOK A TICKET ---");
        // TODO (C): steps ->
        //   1. choose a showtime            (from movieManager)
        //   2. show seat map, choose seats  (showtime.getAvailableSeats())
        //   3. choose ticket type           (Member D's Ticket classes)
        //   4. apply discount               (DiscountStrategy)
        //   5. choose payment method        (Member D's PaymentStrategy) and pay
        //   6. confirm -> BookingManager.getInstance().createBooking(...)
        //   Wrap in try/catch for BookingException and PaymentException
    }

    // Customer: see / cancel bookings
    public void viewMyBookings(Customer customer) {
        System.out.println("\n--- MY BOOKINGS ---");
        // TODO (C): list bookings, allow cancelling one (and trigger a refund via Member D)
    }

    // Admin: promo codes
    public void managePromos() {
        System.out.println("\n--- MANAGE PROMO CODES (ADMIN) ---");
        // TODO (C): add / list / remove promo codes
    }
}
