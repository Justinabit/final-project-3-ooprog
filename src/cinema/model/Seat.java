package cinema.model;

// [Member A] One seat in a room
public class Seat {
    private String seatId;      // example: "A1"
    private String seatType;    // "STANDARD" or "VIP"
    private double priceMultiplier = 1.0;   // VIP could be 1.5 (used by Member D for pricing)

    public Seat(String seatId, String seatType) {
        this.seatId = seatId;
        this.seatType = seatType;
    }

    // TODO (A): getters for all fields
    // TODO (A): set priceMultiplier based on seatType
}
