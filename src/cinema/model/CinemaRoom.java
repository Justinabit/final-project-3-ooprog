package cinema.model;

import java.util.ArrayList;

// [Member A] A room (hall) inside a branch. It owns a list of seats.
public class CinemaRoom {
    private String roomId;
    private String roomName;
    private ArrayList<Seat> seats = new ArrayList<>();

    public CinemaRoom(String roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }

    // Creates seats A1, A2... B1, B2... The last row is VIP (only when there is more than one row).
    public void generateSeats(int rows, int seatsPerRow) {
        seats.clear();
        for (int r = 0; r < rows; r++) {
            char rowLetter = (char) ('A' + r);
            boolean isVip = rows > 1 && r == rows - 1;
            for (int s = 1; s <= seatsPerRow; s++) {
                seats.add(new Seat(rowLetter + String.valueOf(s), isVip));
            }
        }
    }

    public String getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public ArrayList<Seat> getSeats() { return seats; }
}
