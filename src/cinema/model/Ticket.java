package cinema.model;

// [Member D] Abstract ticket (ABSTRACTION). Subclasses calculate price differently (POLYMORPHISM).
public abstract class Ticket {
    protected Showtime showtime;
    protected Seat seat;

    public Ticket(Showtime showtime, Seat seat) {
        this.showtime = showtime;
        this.seat = seat;
    }

    public abstract double calculatePrice();     // seat price only (base + VIP extra). Discounts are applied once per booking by BookingManager.
    public abstract String getTicketType();      // "Regular", "Student", ...
    public String getEligibilityNote() {
        return "No requirements.";               // subclasses can override (e.g. "Valid student ID required")
    }

    public Showtime getShowtime() { return showtime; }
    public Seat getSeat() { return seat; }
}