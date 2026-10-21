package cinema.model;

// [Member A] One seat in a room (ABSTRACTION: you can't create a plain "Seat").
// StandardSeat and VipSeat extend it (INHERITANCE) and each one gives its own
// type and price multiplier (POLYMORPHISM).
public abstract class Seat {
    public static final double VIP_MULTIPLIER = 1.5;

    private String seatId;      // example: "A1"

    public Seat(String seatId) {
        this.seatId = seatId;
    }

    public abstract String getSeatType();          // "STANDARD" or "VIP"
    public abstract double getPriceMultiplier();   // used by Member D for pricing

    public String getSeatId() { return seatId; }
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
