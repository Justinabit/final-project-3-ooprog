package cinema.model;

import java.util.ArrayList;

import cinema.payment.PaymentStrategy;
import cinema.util.Format;

// [Member C] A confirmed (or cancelled) booking
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

    public Booking(String bookingId, Customer customer, Showtime showtime, ArrayList<Seat> seats,
                   double subtotal, double totalPrice, String discountName, String ticketType) {
        this.bookingId = bookingId;
        this.customer = customer;
        this.showtime = showtime;
        this.seats = seats;
        this.subtotal = subtotal;
        this.totalPrice = totalPrice;
        this.discountName = discountName;
        this.ticketType = ticketType;
    }

    public String getBookingId() { return bookingId; }
    public Customer getCustomer() { return customer; }
    public Showtime getShowtime() { return showtime; }
    public ArrayList<Seat> getSeats() { return seats; }
    public double getSubtotal() { return subtotal; }
    public double getTotalPrice() { return totalPrice; }
    public double getDiscountAmount() { return Math.round((subtotal - totalPrice) * 100.0) / 100.0; }
    public String getStatus() { return status; }
    public String getDiscountName() { return discountName; }
    public String getTicketType() { return ticketType; }
    public PaymentStrategy getPayment() { return payment; }
    public void setPayment(PaymentStrategy payment) { this.payment = payment; }
    public double getRefundedAmount() { return refundedAmount; }
    public boolean isRefunded() { return refundedAmount > 0; }

    public void cancel() { this.status = "CANCELLED"; }
    public void markRefunded(double amount) { this.refundedAmount = amount; }

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