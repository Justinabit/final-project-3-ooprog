package cinema.model;

// [Member D] Normal ticket
public class RegularTicket extends Ticket {
    public RegularTicket(Showtime showtime, Seat seat) {
        super(showtime, seat);
    }

    @Override
    public double calculatePrice() {
        return seat.getPrice(showtime.getBasePrice());
    }

    @Override
    public String getTicketType() {
        return "Regular";
    }
}
