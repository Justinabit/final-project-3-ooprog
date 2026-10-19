package cinema.model;

import java.util.ArrayList;

// [Member C] A confirmed (or cancelled) booking
public class Booking {
    private String bookingId;
    private Customer customer;
    private Showtime showtime;
    private ArrayList<Seat> seats;
    private double totalPrice;
    private String status = "CONFIRMED";   // "CONFIRMED" or "CANCELLED"

    public Booking(String bookingId, Customer customer, Showtime showtime, ArrayList<Seat> seats, double totalPrice) {
        this.bookingId = bookingId;
        this.customer = customer;
        this.showtime = showtime;
        this.seats = seats;
        this.totalPrice = totalPrice;
    }

    // TODO (C): getters, plus cancel() which sets status to "CANCELLED"
}
