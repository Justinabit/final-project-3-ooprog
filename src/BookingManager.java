import java.util.ArrayList;

// [Member C] SINGLETON PATTERN: only ONE BookingManager exists in the whole program.
// Get it anywhere with:  BookingManager.getInstance()
public class BookingManager {
    private static BookingManager instance;       // the one and only copy
    private ArrayList<Booking> bookings = new ArrayList<>();

    private BookingManager() { }                  // private so nobody can use "new"

    public static BookingManager getInstance() {
        if (instance == null) {
            instance = new BookingManager();
        }
        return instance;
    }

    public Booking createBooking(Customer customer, Showtime showtime, ArrayList<Seat> seats, Ticket ticket)
            throws BookingException {
        // TODO (C): 1. hold each seat using showtime.holdSeat(...)
        //           2. calculate total using ticket.calculatePrice()
        //           3. create the Booking, add to list, return it
        return null;
    }

    public void cancelBooking(String bookingId) throws BookingException {
        // TODO (C): find the booking, cancel it, release its seats (showtime.releaseSeat)
    }

    public ArrayList<Booking> getBookingsForCustomer(String customerId) {
        // TODO (C): return only this customer's bookings
        return new ArrayList<>();
    }

    public ArrayList<Booking> getAllBookings() {
        return bookings;
    }
}
