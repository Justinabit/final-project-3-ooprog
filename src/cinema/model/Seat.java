package cinema.model;

// [Member A] One seat in a room (ABSTRACTION: you can't create a plain "Seat").
// StandardSeat and VipSeat extend it (INHERITANCE) and each one gives its own
// type and extra charge (POLYMORPHISM).
public abstract class Seat {
    private String seatId;      // example: "A1"

    public Seat(String seatId) {
        this.seatId = seatId;
    }

    public abstract String getSeatType();       // "STANDARD" or "VIP"
    public abstract double getExtraCharge();    // added on top of the showtime's base price

    // Price of this seat for a showtime. Used by Member D's tickets.
    public double getPrice(double basePrice) {
        return basePrice + getExtraCharge();
    }

    public String getSeatNumber() { return seatId; }

    public boolean isVip() {
        return getSeatType().equals("VIP");
    }

    @Override
    public String toString() {
        if (isVip()) {
            return seatId + " (VIP)";
        }
        return seatId;
    }
}
