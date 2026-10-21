package cinema.model;

// [Member A] VIP seat, costs 1.5x the base price
public class VipSeat extends Seat {
    public VipSeat(String seatId) {
        super(seatId);
    }

    @Override
    public String getSeatType() {
        return "VIP";
    }

    @Override
    public double getPriceMultiplier() {
        return VIP_MULTIPLIER;
    }
}
