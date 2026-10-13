import java.util.ArrayList;

// [Member B] A movie playing in a room at a certain time.
// Member C and D call these methods, so don't rename them without telling the team!
public class Showtime {
    private String showtimeId;
    private Movie movie;
    private CinemaRoom room;       // from Member A
    private String dateTime;       // simple text like "2025-01-20 18:30" (or use LocalDateTime)
    private double basePrice;
    private ArrayList<String> heldOrBookedSeatIds = new ArrayList<>();

    public Showtime(String showtimeId, Movie movie, CinemaRoom room, String dateTime, double basePrice) {
        this.showtimeId = showtimeId;
        this.movie = movie;
        this.room = room;
        this.dateTime = dateTime;
        this.basePrice = basePrice;
    }

    public String getShowtimeId() { return showtimeId; }
    public Movie getMovie() { return movie; }
    public CinemaRoom getRoom() { return room; }
    public double getBasePrice() { return basePrice; }

    public ArrayList<Seat> getAvailableSeats() {
        // TODO (B): return the room's seats that are NOT in heldOrBookedSeatIds
        return new ArrayList<>();
    }

    public void holdSeat(String seatId) throws BookingException {
        // TODO (B): throw BookingException if the seat doesn't exist or is already taken
        //           otherwise add it to heldOrBookedSeatIds
    }

    public void releaseSeat(String seatId) {
        // TODO (B): remove the seat from heldOrBookedSeatIds (used when booking is cancelled)
    }
}
