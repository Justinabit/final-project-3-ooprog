package cinema.model;

// [Member D] Student ticket. It is priced at the regular seat price; the 20% student discount is
// applied ONCE at booking level by StudentDiscount, so it can never stack with another discount.
public class StudentTicket extends Ticket {
    public StudentTicket(Showtime showtime, Seat seat) {
        super(showtime, seat);
    }

    @Override
    public double calculatePrice() {
        return seat.getPrice(showtime.getBasePrice());
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