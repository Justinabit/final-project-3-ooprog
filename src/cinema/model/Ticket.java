package cinema.model;

// [Member D] Abstract ticket (ABSTRACTION). Subclasses calculate price differently (POLYMORPHISM).
public abstract class Ticket {
    protected Showtime showtime;
    protected Seat seat;

    public Ticket(Showtime showtime, Seat seat) {
        this.showtime = showtime;
        this.seat = seat;
    }

    public abstract double calculatePrice();     // base price x seat multiplier (x discount later)
    public abstract String getTicketType();      // "Regular", "Student", ...
    public String getEligibilityNote() {
        return "No requirements.";               // subclasses can override (e.g. "Valid student ID required")
    }
}
