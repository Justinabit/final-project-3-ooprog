package cinema.model;

// [Member A] Normal seat, regular price
public class StandardSeat extends Seat {
    public StandardSeat(String seatId) {
        super(seatId);
    }

    @Override
    public String getSeatType() {
        return "STANDARD";
    }

    @Override
    public double getExtraCharge() {
        return 0;
    }
}
