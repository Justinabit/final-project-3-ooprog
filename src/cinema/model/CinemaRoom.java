package cinema.model;

import java.util.ArrayList;

import cinema.exception.ValidationException;
import cinema.layout.SeatLayoutStrategy;
import cinema.layout.StandardLayout;
import cinema.util.Validator;

// [Member A] A room (hall) inside a branch. It owns a list of seats.
public class CinemaRoom {
    private String roomId;
    private String roomName;
    private int rows;
    private int seatsPerRow;
    private String layoutName = "No seats yet";
    private ArrayList<Seat> seats = new ArrayList<>();

    public CinemaRoom(String roomId, String roomName) throws ValidationException {
        this.roomId = roomId;
        setRoomName(roomName);
    }

    // Default layout: the last row is VIP (only when there is more than one row)
    public void generateSeats(int rows, int seatsPerRow) {
        generateSeats(rows, seatsPerRow, new StandardLayout());
    }

    // Creates seats A1, A2... B1, B2... The layout strategy decides which rows are VIP.
    public void generateSeats(int rows, int seatsPerRow, SeatLayoutStrategy layout) {
        seats.clear();
        for (int r = 0; r < rows; r++) {
            char rowLetter = (char) ('A' + r);
            for (int s = 1; s <= seatsPerRow; s++) {
                String seatId = rowLetter + String.valueOf(s);
                if (layout.isVipRow(r, rows)) {
                    seats.add(new VipSeat(seatId));
                } else {
                    seats.add(new StandardSeat(seatId));
                }
            }
        }
        this.rows = rows;
        this.seatsPerRow = seatsPerRow;
        this.layoutName = layout.getName();
    }

    public Seat findSeat(String seatId) {
        for (Seat seat : seats) {
            if (seat.getSeatNumber().equalsIgnoreCase(seatId.trim())) return seat;
        }
        return null;
    }

    // Replaces one seat with a Standard or VIP seat. Bookings already made keep the price they paid.
    public void changeSeatType(String seatId, boolean makeVip) throws ValidationException {
        Seat oldSeat = findSeat(seatId);
        if (oldSeat == null) {
            throw new ValidationException("Seat " + seatId.trim().toUpperCase() + " does not exist in this room.");
        }
        if (oldSeat.isVip() == makeVip) {
            throw new ValidationException("Seat " + oldSeat.getSeatNumber() + " is already " + oldSeat.getSeatType() + ".");
        }
        Seat newSeat;
        if (makeVip) {
            newSeat = new VipSeat(oldSeat.getSeatNumber());
        } else {
            newSeat = new StandardSeat(oldSeat.getSeatNumber());
        }
        seats.set(seats.indexOf(oldSeat), newSeat);
        layoutName = "Custom";
    }

    public int getVipSeatCount() {
        int count = 0;
        for (Seat seat : seats) {
            if (seat.isVip()) count++;
        }
        return count;
    }

    public String getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public int getRows() { return rows; }
    public int getSeatsPerRow() { return seatsPerRow; }
    public String getLayoutName() { return layoutName; }
    public ArrayList<Seat> getSeats() { return seats; }

    public void setRoomName(String roomName) throws ValidationException {
        Validator.validateRoomName(roomName);
        this.roomName = roomName.trim();
    }
}
