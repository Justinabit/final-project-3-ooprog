package cinema.model;

// [Member D] Normal ticket
public class RegularTicket extends Ticket {
    public RegularTicket(Showtime showtime, Seat seat) {
        super(showtime, seat);
    }

    @Override
    public double calculatePrice() {
        // TODO (D): showtime.getBasePrice() * seat's price multiplier
        return 0;
    }

    @Override
    public String getTicketType() {
        return "Regular";
    }

    // TODO (D): add more subclasses: StudentTicket, SeniorTicket, ChildTicket...
}
