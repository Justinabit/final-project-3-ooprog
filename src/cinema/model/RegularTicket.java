package cinema.model;

// [Member D] Normal ticket
public class RegularTicket extends Ticket {
    public RegularTicket(Showtime showtime, Seat seat) {
        super(showtime, seat);
    }

    @Override
    public double calculatePrice() {
        return showtime.getBasePrice() * seat.getPriceMultiplier();
    }

    @Override
    public String getTicketType() {
        return "Regular";
    }
}
