package cinema.service;

import java.util.ArrayList;

import cinema.discount.DiscountStrategy;
import cinema.discount.StudentDiscount;
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
    private int bookingCounter = 0;               // never reused, so booking references stay unique

    private BookingManager() { }                  // private so nobody can use "new"

    public static BookingManager getInstance() {
        if (instance == null) {
            instance = new BookingManager();
        }
        return instance;
    }

    // Rounds to whole centavos, so every printed amount adds up exactly to the total.
    public static double round2(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }

    // Sum of each ticket's own price (seat type + showtime base price), BEFORE any discount.
    public static double calculateSubtotal(ArrayList<Ticket> tickets) {
        double subtotal = 0;
        for (Ticket t : tickets) subtotal += round2(t.calculatePrice());
        return round2(subtotal);
    }

    // Subtotal minus the ONE discount of the booking. Never below 0 and never above the subtotal.
    public static double calculateTotal(ArrayList<Ticket> tickets, DiscountStrategy discount) {
        double subtotal = calculateSubtotal(tickets);
        double total = round2(discount.applyDiscount(subtotal));
        return Math.max(0, Math.min(subtotal, total));
    }

    // One discount per booking: student tickets get exactly the student discount, and the student
    // discount is only for student tickets. Anything else would stack discounts.
    private static void checkDiscountRule(ArrayList<Ticket> tickets, DiscountStrategy discount) throws BookingException {
        boolean studentTickets = false;
        for (Ticket t : tickets) {
            if ("Student".equals(t.getTicketType())) studentTickets = true;
        }
        boolean studentDiscount = discount instanceof StudentDiscount;
        if (studentTickets && !studentDiscount) {
            throw new BookingException("Student tickets cannot be combined with another discount.");
        }
        if (!studentTickets && studentDiscount) {
            throw new BookingException("The student discount needs student tickets.");
        }
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
        checkDiscountRule(tickets, discount);

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

        double subtotal = calculateSubtotal(tickets);
        double total = calculateTotal(tickets, discount);
        // Booking reference like a real e-ticket: CUR-100001, CUR-100002, ...
        Booking booking = new Booking("CUR-" + (100000 + (++bookingCounter)), customer, showtime, held,
                subtotal, total, discount.getName(), tickets.get(0).getTicketType());
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

    // requester may be null (admin use); a customer can only cancel their own booking.
    // Cancelling frees the seats AND refunds the payment the same way it was made.
    public Booking cancelBooking(String bookingId, Customer requester) throws BookingException {
        Booking found = null;
        for (Booking b : bookings) {
            if (b.getBookingId().equalsIgnoreCase(bookingId.trim())) {
                found = b;
                break;
            }
        }
        if (found == null || (requester != null && !found.getCustomer().getUserId().equalsIgnoreCase(requester.getUserId()))) {
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
        // The refund is part of cancelling, so it happens on every cancel path (customer or admin).
        if (found.getPayment() != null) {
            found.getPayment().refund(found.getTotalPrice());
            found.markRefunded(found.getTotalPrice());
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