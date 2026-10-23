package cinema.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

import cinema.payment.PaymentStrategy;
import cinema.service.BookingManager;
import cinema.util.Format;

// [Member C] A confirmed (or cancelled) booking.
// It also keeps the PAYMENT RECORD (method, amount actually paid, time) and the refund, so the reports
// read real saved data instead of guessing. All amounts are whole centavos.
public class Booking {
    private final String bookingId;
    private final Customer customer;
    private final Showtime showtime;
    private final ArrayList<Seat> seats;
    private final double subtotal;          // price of all tickets BEFORE the discount
    private final double totalPrice;        // what the customer pays, AFTER the discount
    private final String discountName;
    private final String ticketType;        // "Regular" or "Student"
    private String status = "CONFIRMED";    // "CONFIRMED" or "CANCELLED"
    private double refundedAmount = 0;      // set when a cancellation is refunded
    private PaymentStrategy payment;        // remembered so a cancellation can refund the same way
    private double amountPaid = 0;          // 0 until a payment really succeeded
    private LocalDateTime paidAt;           // when the payment succeeded (null = not paid yet)

    // The constructor checks the numbers, so a booking with a negative price, or a total that is bigger
    // than the subtotal, can never exist (ENCAPSULATION).
    public Booking(String bookingId, Customer customer, Showtime showtime, ArrayList<Seat> seats,
                   double subtotal, double totalPrice, String discountName, String ticketType) {
        if (bookingId == null || customer == null || showtime == null || seats == null
                || discountName == null || ticketType == null) {
            throw new IllegalArgumentException("A booking needs an ID, customer, showtime, seats, discount and ticket type.");
        }
        double roundedSubtotal = BookingManager.round2(subtotal);   // also rejects NaN / infinity
        double roundedTotal = BookingManager.round2(totalPrice);
        if (roundedSubtotal < 0 || roundedTotal < 0 || roundedTotal > roundedSubtotal) {
            throw new IllegalArgumentException("Invalid booking amounts: the total must be between 0 and the subtotal.");
        }
        this.bookingId = bookingId;
        this.customer = customer;
        this.showtime = showtime;
        this.seats = seats;
        this.subtotal = roundedSubtotal;
        this.totalPrice = roundedTotal;
        this.discountName = discountName;
        this.ticketType = ticketType;
    }

    public String getBookingId() { return bookingId; }
    public Customer getCustomer() { return customer; }
    public Showtime getShowtime() { return showtime; }
    public ArrayList<Seat> getSeats() { return seats; }
    public double getSubtotal() { return subtotal; }
    public double getTotalPrice() { return totalPrice; }
    public double getDiscountAmount() {
        return BookingManager.toMoney(subtotal).subtract(BookingManager.toMoney(totalPrice)).doubleValue();
    }
    public String getStatus() { return status; }
    public String getDiscountName() { return discountName; }
    public String getTicketType() { return ticketType; }
    public PaymentStrategy getPayment() { return payment; }
    public double getRefundedAmount() { return refundedAmount; }
    public boolean isRefunded() { return refundedAmount > 0; }
    public double getAmountPaid() { return amountPaid; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public boolean isPaid() { return paidAt != null; }

    // Saves the payment record. Call it ONLY after payment.pay(...) succeeded.
    // The amount must equal the booking total, and a booking can be paid only once.
    public void recordPayment(PaymentStrategy payment, double amount) {
        if (payment == null) {
            throw new IllegalArgumentException("A payment method is required.");
        }
        if (isPaid()) {
            throw new IllegalStateException("Booking " + bookingId + " is already paid.");
        }
        if ("CANCELLED".equals(status)) {
            throw new IllegalStateException("Booking " + bookingId + " is cancelled, so it cannot be paid.");
        }
        if (BookingManager.toMoney(amount).compareTo(BookingManager.toMoney(totalPrice)) != 0) {
            throw new IllegalArgumentException("The amount paid (" + Format.peso(amount)
                    + ") must equal the booking total (" + Format.peso(totalPrice) + ").");
        }
        this.payment = payment;
        this.amountPaid = totalPrice;
        this.paidAt = LocalDateTime.now();
    }

    // Used only when loading a saved booking from the data file: the payment already happened at paidAt.
    public void loadPayment(PaymentStrategy payment, LocalDateTime paidAt) {
        if (payment == null || paidAt == null) {
            throw new IllegalArgumentException("A saved payment needs a method and a date.");
        }
        this.payment = payment;
        this.amountPaid = totalPrice;
        this.paidAt = paidAt;
    }

    public void cancel() { this.status = "CANCELLED"; }

    // One refund only, and never more than was paid.
    public void markRefunded(double amount) {
        if (!isPaid()) {
            throw new IllegalStateException("Booking " + bookingId + " was never paid, so there is nothing to refund.");
        }
        if (isRefunded()) {
            throw new IllegalStateException("Booking " + bookingId + " was already refunded.");
        }
        double rounded = BookingManager.round2(amount);
        if (rounded <= 0 || rounded > amountPaid) {
            throw new IllegalArgumentException("The refund must be more than 0 and at most " + Format.peso(amountPaid) + ".");
        }
        this.refundedAmount = rounded;
    }

    public String getSeatList() {
        StringBuilder sb = new StringBuilder();
        for (Seat s : seats) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(s.getSeatNumber());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return bookingId + " | " + showtime.getMovie().getTitle() + " | " + Format.shortDateTime(showtime.getStart())
                + " | " + showtime.getRoom().getRoomName() + " | Seats: " + getSeatList()
                + " | " + Format.peso(totalPrice) + " | " + status;
    }
}