import java.time.LocalDateTime;
import java.util.ArrayList;

// [Member B] A movie playing in a room at a certain time.
// Member C and D call these methods, so don't rename them without telling the team!
public class Showtime {
    private String showtimeId;
    private Movie movie;
    private CinemaRoom room;       // from Member A
    private LocalDateTime start;   // parsed from text like "2026-11-20 18:30"
    private double basePrice;
    private ArrayList<String> heldOrBookedSeatIds = new ArrayList<>();

    public Showtime(String showtimeId, Movie movie, CinemaRoom room, String dateTime, double basePrice)
            throws ValidationException {
        this.start = Validator.validateDateTime(dateTime);
        Validator.validatePrice(basePrice);
        this.showtimeId = showtimeId;
        this.movie = movie;
        this.room = room;
        this.basePrice = basePrice;
    }

    public String getShowtimeId() { return showtimeId; }
    public Movie getMovie() { return movie; }
    public CinemaRoom getRoom() { return room; }
    public double getBasePrice() { return basePrice; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return start.plusMinutes(movie.getDurationMinutes()); }
    public String getDateTime() { return start.format(Validator.DATE_TIME_FORMAT); }
    public boolean hasStarted() { return !start.isAfter(LocalDateTime.now()); }
    public boolean hasEnded() { return !getEnd().isAfter(LocalDateTime.now()); }

    public boolean isSeatTaken(String seatId) {
        for (String id : heldOrBookedSeatIds) {
            if (id.equalsIgnoreCase(seatId)) return true;
        }
        return false;
    }

    public ArrayList<Seat> getAvailableSeats() {
        ArrayList<Seat> available = new ArrayList<>();
        for (Seat seat : room.getSeats()) {
            if (!isSeatTaken(seat.getSeatNumber())) available.add(seat);
        }
        return available;
    }

    public int getSeatsTakenCount() { return heldOrBookedSeatIds.size(); }
    public int getCapacity() { return room.getSeats().size(); }

    public Seat findSeat(String seatId) {
        for (Seat seat : room.getSeats()) {
            if (seat.getSeatNumber().equalsIgnoreCase(seatId.trim())) return seat;
        }
        return null;
    }

    public void holdSeat(String seatId) throws BookingException {
        Seat seat = findSeat(seatId);
        if (seat == null) {
            throw new BookingException("Seat " + seatId + " does not exist in this room.");
        }
        if (isSeatTaken(seat.getSeatNumber())) {
            throw new BookingException("Seat " + seat.getSeatNumber() + " is already taken.");
        }
        heldOrBookedSeatIds.add(seat.getSeatNumber());
    }

    public void releaseSeat(String seatId) {
        for (int i = 0; i < heldOrBookedSeatIds.size(); i++) {
            if (heldOrBookedSeatIds.get(i).equalsIgnoreCase(seatId)) {
                heldOrBookedSeatIds.remove(i);
                return;
            }
        }
    }

    public String describe(String branchName) {
        return showtimeId + " | " + movie.getTitle() + " | " + (branchName == null ? "" : branchName + " - ")
                + room.getRoomName() + " | " + getDateTime() + " | " + String.format("$%.2f", basePrice);
    }
}
