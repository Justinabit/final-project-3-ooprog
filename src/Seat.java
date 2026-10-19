// [Member A] One seat in a room
public class Seat {
    public static final double VIP_MULTIPLIER = 1.5;

    private String seatId;      // example: "A1"
    private String seatType;    // "STANDARD" or "VIP"
    private double priceMultiplier = 1.0;   // VIP is 1.5 (used by Member D for pricing)

    public Seat(String seatId, String seatType) {
        this.seatId = seatId;
        this.seatType = "VIP".equalsIgnoreCase(seatType) ? "VIP" : "STANDARD";
        this.priceMultiplier = this.seatType.equals("VIP") ? VIP_MULTIPLIER : 1.0;
    }

    public Seat(String seatId, boolean isVip) {
        this(seatId, isVip ? "VIP" : "STANDARD");
    }

    public String getSeatId() { return seatId; }
    public String getSeatNumber() { return seatId; }
    public String getSeatType() { return seatType; }
    public double getPriceMultiplier() { return priceMultiplier; }

    @Override
    public String toString() {
        return seatId + (seatType.equals("VIP") ? " (VIP)" : "");
    }
}
