// [Member D] Student ticket: 20% off the regular price (reuses StudentDiscount)
public class StudentTicket extends Ticket {
    private final DiscountStrategy discount = new StudentDiscount();

    public StudentTicket(Showtime showtime, Seat seat) {
        super(showtime, seat);
    }

    @Override
    public double calculatePrice() {
        return discount.applyDiscount(showtime.getBasePrice() * seat.getPriceMultiplier());
    }

    @Override
    public String getTicketType() {
        return "Student";
    }

    @Override
    public String getEligibilityNote() {
        return "Valid student ID required at the entrance.";
    }
}
