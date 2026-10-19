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

    public void generateSeats(int rows, int seatsPerRow) {
        // TODO (A): create seats named A1, A2... B1, B2... and add them to the list
        //           (make the last row VIP if you want)
    }

    // TODO (A): getters (getRoomId, getRoomName, getSeats)
}
