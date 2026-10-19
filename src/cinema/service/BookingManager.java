package cinema.service;

import java.util.ArrayList;

import cinema.discount.DiscountStrategy;
import cinema.exception.BookingException;
import cinema.model.Booking;
import cinema.model.Customer;
import cinema.model.Seat;
import cinema.model.Showtime;
import cinema.model.Ticket;

// [Member C] SINGLETON PATTERN: only ONE BookingManager exists in the whole program.
// Get it anywhere with:  BookingManager.getInstance()
public class BookingManager {
    private static BookingManager instance;       // the one and only copy
    private ArrayList<Booking> bookings = new ArrayList<>();
    private int bookingCounter = 0;               // never reused, so IDs stay unique

    private BookingManager() { }                  // private so nobody can use "new"

    public static BookingManager getInstance() {
        if (instance == null) {
            instance = new BookingManager();
        }
        return instance;
    }

    // Total = sum of each ticket's own price (seat type + ticket type), then the discount on top.
    public static double calculateTotal(ArrayList<Ticket> tickets, DiscountStrategy discount) {
        double subtotal = 0;
        for (Ticket t : tickets) subtotal += t.calculatePrice();
        return Math.round(discount.applyDiscount(subtotal) * 100.0) / 100.0;
    }

    // One ticket per seat. Seats are held first; if any fails, the ones already held are released.
    public Booking createBooking(Customer customer, Showtime showtime, ArrayList<Ticket> tickets, DiscountStrategy discount)
            throws BookingException {
        if (tickets == null || tickets.isEmpty()) {
            throw new BookingException("No seats selected for booking.");
        }
        if (showtime.hasStarted()) {
            throw new BookingException("This showtime has already started.");
        }
        if (customer.getAge() < showtime.getMovie().getMinimumAge()) {
            throw new BookingException("You must be at least " + showtime.getMovie().getMinimumAge()
                    + " to watch a " + showtime.getMovie().getAgeRating() + " movie.");
        }

        ArrayList<Seat> held = new ArrayList<>();
        try {
            for (Ticket t : tickets) {
                showtime.holdSeat(t.getSeat().getSeatNumber());
                held.add(t.getSeat());
            }
        } catch (BookingException e) {
            for (Seat s : held) showtime.releaseSeat(s.getSeatNumber());   // roll back partial hold
            throw e;
        }

        double total = calculateTotal(tickets, discount);
        Booking booking = new Booking("BK-" + (++bookingCounter), customer, showtime, held, total);
        booking.setDiscountName(discount.getName());
        bookings.add(booking);
        return booking;
    }

    // Undo a booking that was never paid for: free the seats and forget it
    public void discardBooking(Booking booking) {
        for (Seat seat : booking.getSeats()) {
            booking.getShowtime().releaseSeat(seat.getSeatNumber());
        }
        bookings.remove(booking);
    }

    // requester may be null (admin use); a customer can only cancel their own booking
    public Booking cancelBooking(String bookingId, Customer requester) throws BookingException {
        Booking found = null;
        for (Booking b : bookings) {
            if (b.getBookingId().equalsIgnoreCase(bookingId.trim())) {
                found = b;
                break;
            }
        }
        if (found == null || (requester != null && found.getCustomer() != requester)) {
            throw new BookingException("Booking ID " + bookingId + " not found.");
        }
        if ("CANCELLED".equals(found.getStatus())) {
            throw new BookingException("Booking " + found.getBookingId() + " is already cancelled.");
        }
        if (found.getShowtime().hasStarted()) {
            throw new BookingException("This showtime has already started, so it can no longer be cancelled.");
        }

        found.cancel();
        for (Seat seat : found.getSeats()) {
            found.getShowtime().releaseSeat(seat.getSeatNumber());
        }
        return found;
    }

    public void cancelBooking(String bookingId) throws BookingException {
        cancelBooking(bookingId, null);
    }

    public ArrayList<Booking> getBookingsForCustomer(String customerId) {
        ArrayList<Booking> result = new ArrayList<>();
        for (Booking b : bookings) {
            if (b.getCustomer().getUserId().equalsIgnoreCase(customerId)) result.add(b);
        }
        return result;
    }

    public ArrayList<Booking> getAllBookings() {
        return bookings;
    }
}
