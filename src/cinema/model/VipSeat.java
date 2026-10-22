package cinema.model;

// [Member A] VIP seat (wider recliner), costs PHP 200 more than a regular seat
public class VipSeat extends Seat {
    public static final double EXTRA_CHARGE = 200.0;

    public VipSeat(String seatId) {
        super(seatId);
    }

    @Override
    public String getSeatType() {
        return "VIP";
    }

    @Override
    public double getExtraCharge() {
        return EXTRA_CHARGE;
    }
}
