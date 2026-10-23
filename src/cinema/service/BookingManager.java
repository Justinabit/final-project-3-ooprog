package cinema.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import cinema.data.TransactionLog;
import cinema.discount.DiscountStrategy;
import cinema.discount.PromoDiscount;
import cinema.discount.SeniorDiscount;
import cinema.discount.StudentDiscount;
import cinema.exception.BookingException;
import cinema.model.Booking;
import cinema.model.Customer;
import cinema.model.Promo;
import cinema.model.Seat;
import cinema.model.Showtime;
import cinema.model.Ticket;
import cinema.payment.PaymentStrategy;
import cinema.util.Format;

// [Member C] SINGLETON PATTERN: only ONE BookingManager exists in the whole program.
// Get it anywhere with:  BookingManager.getInstance()
//
// MONEY RULE (one place for the whole program): all money math is done with BigDecimal and rounded to
// whole centavos (HALF_UP). The public methods still take and return double so other classes need no changes.
// calculateSubtotal() and calculateTotal() are the ONLY calculators: the order summary, the booking,
// the payment and the reports all use their results.
public class BookingManager {
    public static final int FIRST_REFERENCE = 100000;   // booking references start at CUR-100001
    public static final int CANCEL_CUTOFF_HOURS = 2;    // customers can cancel until 2 hours before the show

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

    // ---------- money helpers ----------

    // The ONE rounding rule: whole centavos, HALF_UP (0.005 -> 0.01). BigDecimal.valueOf uses the number as
    // it is written (1.005 stays 1.005), so there is no floating-point surprise.
    public static BigDecimal toMoney(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException("Amount must be a valid number.");
        }
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }

    // Rounds to whole centavos, so every printed amount adds up exactly to the total.
    public static double round2(double amount) {
        return toMoney(amount).doubleValue();
    }

    // Sum of each ticket's own price (seat type + showtime base price), BEFORE any discount.
    private static BigDecimal subtotalOf(ArrayList<Ticket> tickets) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Ticket t : tickets) {
            subtotal = subtotal.add(toMoney(t.calculatePrice()));
        }
        return subtotal;
    }

    // Subtotal minus the ONE discount of the booking. Never below 0 and never above the subtotal.
    private static BigDecimal totalOf(BigDecimal subtotal, DiscountStrategy discount) {
        BigDecimal discounted = toMoney(discount.applyDiscount(subtotal.doubleValue()));
        return discounted.max(BigDecimal.ZERO).min(subtotal);
    }

    public static double calculateSubtotal(ArrayList<Ticket> tickets) {
        return subtotalOf(tickets).doubleValue();
    }

    public static double calculateTotal(ArrayList<Ticket> tickets, DiscountStrategy discount) {
        return totalOf(subtotalOf(tickets), discount).doubleValue();
    }

    // ---------- booking rules ----------

    // One discount per booking, and only for customers who are eligible. Checked HERE (not only in the menu),
    // so no screen can sneak in a discount the customer is not entitled to.
    private static void checkDiscountRule(Customer customer, ArrayList<Ticket> tickets, DiscountStrategy discount)
            throws BookingException {
        HashSet<String> ticketTypes = new HashSet<>();
        for (Ticket t : tickets) ticketTypes.add(t.getTicketType());
        if (ticketTypes.size() > 1) {
            throw new BookingException("Regular and Student tickets cannot be mixed in one booking.");
        }
        // Student tickets get exactly the student discount, and the student discount is only for
        // student tickets. Anything else would stack discounts.
        boolean studentTickets = ticketTypes.contains("Student");
        boolean studentDiscount = discount instanceof StudentDiscount;
        if (studentTickets && !studentDiscount) {
            throw new BookingException("Student tickets cannot be combined with another discount.");
        }
        if (!studentTickets && studentDiscount) {
            throw new BookingException("The student discount needs student tickets.");
        }
        if (discount instanceof SeniorDiscount && customer.getAge() < SeniorDiscount.MIN_AGE) {
            throw new BookingException("The senior citizen discount is only for customers aged "
                    + SeniorDiscount.MIN_AGE + " and above.");
        }
        if (discount instanceof PromoDiscount) {
            Promo promo = ((PromoDiscount) discount).getPromo();
            if (promo.isExpired()) {
                throw new BookingException("Promo code " + promo.getCode() + " has expired.");
            }
            if (promo.isUsedBy(customer.getUserId())) {
                throw new BookingException("You have already used promo code " + promo.getCode() + ".");
            }
        }
    }

    // One ticket per seat. Seats are held first; if any fails, the ones already held are released.
    public Booking createBooking(Customer customer, Showtime showtime, ArrayList<Ticket> tickets, DiscountStrategy discount)
            throws BookingException {
        if (tickets == null || tickets.isEmpty()) {
            throw new BookingException("No seats selected for booking.");
        }
        if (discount == null) {
            throw new BookingException("No discount option was given.");
        }
        if (showtime.isCancelled()) {
            throw new BookingException("This showtime was cancelled.");
        }
        if (showtime.hasStarted()) {
            throw new BookingException("This showtime has already started.");
        }
        if (!showtime.getMovie().isAllowedForAge(customer.getAge())) {
            throw new BookingException("You must be at least " + showtime.getMovie().getMinimumAge()
                    + " to watch a " + showtime.getMovie().getAgeRating() + " movie.");
        }
        for (Ticket t : tickets) {
            if (t.getSeat() == null || t.getShowtime() != showtime) {
                throw new BookingException("Every ticket must be for a seat of this showtime.");
            }
            if (toMoney(t.calculatePrice()).signum() <= 0) {
                throw new BookingException("Ticket prices must be greater than " + Format.peso(0) + ".");
            }
        }
        checkDiscountRule(customer, tickets, discount);

        // Price it BEFORE holding any seat, so a pricing problem never leaves seats held.
        BigDecimal subtotal = subtotalOf(tickets);
        BigDecimal total = totalOf(subtotal, discount);
        if (total.compareTo(new BigDecimal("0.01")) < 0) {
            throw new BookingException("The total must be at least " + Format.peso(0.01) + ".");
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

        // Booking reference like a real e-ticket: CUR-100001, CUR-100002, ...
        Booking booking = new Booking("CUR-" + (FIRST_REFERENCE + (++bookingCounter)), customer, showtime, held,
                subtotal.doubleValue(), total.doubleValue(), discount.getName(), tickets.get(0).getTicketType());
        bookings.add(booking);
        return booking;
    }

    // Call ONLY after payment.pay(...) succeeded: saves the payment on the booking, marks a promo code
    // as used by this customer, and writes the payment to the transaction log.
    public void confirmPayment(Booking booking, PaymentStrategy payment, double amount, DiscountStrategy discount) {
        booking.recordPayment(payment, amount);
        if (discount instanceof PromoDiscount) {
            ((PromoDiscount) discount).getPromo().markUsedBy(booking.getCustomer().getUserId());
        }
        TransactionLog.record("PAYMENT", booking, booking.getAmountPaid());
    }

    // Customers can cancel a confirmed booking until CANCEL_CUTOFF_HOURS before the show
    public static boolean canCustomerCancel(Booking booking) {
        return "CONFIRMED".equals(booking.getStatus())
                && LocalDateTime.now().plusHours(CANCEL_CUTOFF_HOURS).isBefore(booking.getShowtime().getStart());
    }

    // Undo a booking that was never paid for: free the seats and forget it
    public void discardBooking(Booking booking) {
        for (Seat seat : booking.getSeats()) {
            booking.getShowtime().releaseSeat(seat.getSeatNumber());
        }
        bookings.remove(booking);
    }

    // requester may be null (admin use); a customer can only cancel their own booking.
    // Cancelling refunds the payment the same way it was made AND frees the seats.
    public Booking cancelBooking(String bookingId, Customer requester) throws BookingException {
        Booking found = findBooking(bookingId);
        if (found == null || (requester != null && !found.getCustomer().getUserId().equalsIgnoreCase(requester.getUserId()))) {
            throw new BookingException("Booking ID " + bookingId + " not found.");
        }
        if ("CANCELLED".equals(found.getStatus())) {
            throw new BookingException("Booking " + found.getBookingId() + " is already cancelled.");
        }
        if (found.getShowtime().hasStarted()) {
            throw new BookingException("This showtime has already started, so it can no longer be cancelled.");
        }
        if (requester != null && !canCustomerCancel(found)) {
            throw new BookingException("Online cancellation closes " + CANCEL_CUTOFF_HOURS
                    + " hours before the show. Please go to the cinema counter.");
        }

        // The refund is part of cancelling, so it happens on every cancel path (customer or admin).
        // It goes first: the booking is only marked CANCELLED after the money was sent back.
        if (found.isPaid()) {
            double amount = found.getAmountPaid();          // refund exactly what was really paid
            found.getPayment().refund(amount);
            found.markRefunded(amount);
            TransactionLog.record("REFUND", found, amount);
        }
        found.cancel();
        for (Seat seat : found.getSeats()) {
            found.getShowtime().releaseSeat(seat.getSeatNumber());
        }
        return found;
    }

    // Adds a booking loaded from the data file (it was already checked and paid when it was made)
    public void restoreBooking(Booking booking) {
        int number = Integer.parseInt(booking.getBookingId().substring(4)) - FIRST_REFERENCE;   // "CUR-100006" -> 6
        bookings.add(booking);
        if (number > bookingCounter) {
            bookingCounter = number;
        }
    }

    public Booking findBooking(String bookingId) {
        for (Booking b : bookings) {
            if (b.getBookingId().equalsIgnoreCase(bookingId.trim())) return b;
        }
        return null;
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