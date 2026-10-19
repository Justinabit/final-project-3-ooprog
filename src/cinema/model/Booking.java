package cinema.model;

import java.util.ArrayList;

import cinema.payment.PaymentStrategy;

// [Member C] A confirmed (or cancelled) booking
public class Booking {
    private String bookingId;
    private Customer customer;
    private Showtime showtime;
    private ArrayList<Seat> seats;
    private double totalPrice;
    private String status = "CONFIRMED";   // "CONFIRMED" or "CANCELLED"
    private String discountName = "No Discount";
    private PaymentStrategy payment;       // remembered so a cancellation can refund the same way

    public Booking(String bookingId, Customer customer, Showtime showtime, ArrayList<Seat> seats, double totalPrice) {
        this.bookingId = bookingId;
        this.customer = customer;
        this.showtime = showtime;
        this.seats = seats;
        this.totalPrice = totalPrice;
    }

    public String getBookingId() { return bookingId; }
    public Customer getCustomer() { return customer; }
    public Showtime getShowtime() { return showtime; }
    public ArrayList<Seat> getSeats() { return seats; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public String getDiscountName() { return discountName; }
    public void setDiscountName(String discountName) { this.discountName = discountName; }
    public PaymentStrategy getPayment() { return payment; }
    public void setPayment(PaymentStrategy payment) { this.payment = payment; }

    public void cancel() { this.status = "CANCELLED"; }

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
        return bookingId + " | " + showtime.getMovie().getTitle() + " | " + showtime.getDateTime()
                + " | " + showtime.getRoom().getRoomName() + " | Seats: " + getSeatList()
                + " | " + String.format("$%.2f", totalPrice) + " | " + status;
    }
}
