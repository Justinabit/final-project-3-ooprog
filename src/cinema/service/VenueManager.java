package cinema.service;

import java.util.ArrayList;

import cinema.exception.ValidationException;
import cinema.exception.VenueException;
import cinema.layout.SeatLayoutStrategy;
import cinema.model.Booking;
import cinema.model.CinemaBranch;
import cinema.model.CinemaRoom;
import cinema.model.Showtime;
import cinema.util.Validator;

// [Member A] Stores all branches. Others (Member B) use this to find rooms for showtimes.
public class VenueManager {
    public static final int MIN_ROWS = 2, MAX_ROWS = 26;                    // rows are letters A-Z
    public static final int MIN_SEATS_PER_ROW = 4, MAX_SEATS_PER_ROW = 30;  // smaller rooms are not realistic

    private ArrayList<CinemaBranch> branches = new ArrayList<>();
    private int branchCounter = 0;
    private int roomCounter = 0;

    private String nextBranchId() { return "BR-" + (++branchCounter); }
    private String nextRoomId() { return "RM-" + (++roomCounter); }

    // ---------- branches ----------

    // Used when loading the data file (new branches use createBranch)
    public void addBranch(CinemaBranch branch) throws ValidationException {
        if (isBranchNameTaken(branch.getBranchName(), null)) {
            throw new ValidationException("A branch named '" + branch.getBranchName() + "' already exists.");
        }
        int number = Integer.parseInt(branch.getBranchId().substring(3));   // "BR-12" -> 12
        branches.add(branch);
        if (number > branchCounter) {
            branchCounter = number;
        }
    }

    // Convenience for the menu: checks everything first, so an ID is only used when the branch is really added
    public CinemaBranch createBranch(String name, String location) throws ValidationException {
        checkBranchName(name, null);
        Validator.validateLocation(location);
        CinemaBranch branch = new CinemaBranch(nextBranchId(), name, location);
        branches.add(branch);
        return branch;
    }

    // Branch names must stay unique when renaming too
    public void renameBranch(CinemaBranch branch, String newName) throws ValidationException {
        checkBranchName(newName, branch);
        branch.setBranchName(newName);
    }

    // A branch name must be valid and not used by another branch. The menu calls this right after the
    // name is typed. ignore = the branch being renamed (it may keep its own name), or null for a new one.
    public void checkBranchName(String name, CinemaBranch ignore) throws ValidationException {
        Validator.validateBranchName(name);
        if (isBranchNameTaken(name, ignore)) {
            throw new ValidationException("A branch named '" + name.trim() + "' already exists.");
        }
    }

    // ignore = the branch being renamed (it may keep its own name), or null
    public boolean isBranchNameTaken(String name, CinemaBranch ignore) {
        for (CinemaBranch b : branches) {
            if (b != ignore && b.getBranchName().equalsIgnoreCase(name.trim())) return true;
        }
        return false;
    }

    // Removes the branch, its rooms and their showtimes. Returns how many showtimes were removed.
    public int removeBranch(String branchId, ArrayList<Showtime> showtimes) throws VenueException {
        CinemaBranch branch = findBranch(branchId);
        if (branch == null) {
            throw new VenueException("Branch " + branchId + " not found.");
        }
        // Check every room first, so nothing is removed if one room is still in use
        for (CinemaRoom room : branch.getRooms()) {
            checkRoomCanBeRemoved(room);
        }
        int removed = 0;
        for (CinemaRoom room : branch.getRooms()) {
            removed += removeShowtimesOfRoom(room, showtimes);
        }
        branches.remove(branch);
        return removed;
    }

    // ---------- rooms ----------

    // Validates, creates the room, generates seats with the chosen layout and adds it.
    public CinemaRoom addRoomToBranch(String branchId, String roomName, int rows, int seatsPerRow,
            SeatLayoutStrategy layout) throws ValidationException, VenueException {
        CinemaBranch branch = findBranch(branchId);
        if (branch == null) {
            throw new VenueException("Branch " + branchId + " not found.");
        }
        checkRoomName(branch, roomName, null);
        Validator.validateRange("Rows", rows, MIN_ROWS, MAX_ROWS);
        Validator.validateRange("Seats per row", seatsPerRow, MIN_SEATS_PER_ROW, MAX_SEATS_PER_ROW);
        CinemaRoom room = new CinemaRoom(nextRoomId(), roomName);
        room.generateSeats(rows, seatsPerRow, layout);
        branch.addRoom(room);
        return room;
    }

    // Used when loading the data file
    public void addLoadedRoom(CinemaBranch branch, CinemaRoom room) throws ValidationException {
        int number = Integer.parseInt(room.getRoomId().substring(3));   // "RM-12" -> 12
        branch.addRoom(room);
        if (number > roomCounter) {
            roomCounter = number;
        }
    }

    public void changeSeatType(CinemaRoom room, String seatId, boolean makeVip, ArrayList<Showtime> showtimes)
            throws ValidationException {
        checkSeatCanChange(room, seatId, showtimes);
        room.changeSeatType(seatId, makeVip);
    }

    // A seat that is booked for a show that hasn't ended can't change type (the ticket says what it is).
    // The menu calls this right after the seat is typed.
    public void checkSeatCanChange(CinemaRoom room, String seatId, ArrayList<Showtime> showtimes)
            throws ValidationException {
        if (room.findSeat(seatId) == null) {
            throw new ValidationException("Seat " + seatId.trim().toUpperCase() + " does not exist in this room.");
        }
        for (Showtime s : showtimes) {
            if (s.getRoom() == room && !s.isCancelled() && !s.hasEnded() && s.isSeatTaken(seatId.trim())) {
                throw new ValidationException("Seat " + seatId.trim().toUpperCase() + " is booked for the "
                        + s.getDateTime() + " show. Change it after that show has ended.");
            }
        }
    }

    // Room names must stay unique inside a branch
    public void renameRoom(CinemaRoom room, String newName) throws ValidationException {
        checkRoomName(findBranchOfRoom(room), newName, room);
        room.setRoomName(newName);
    }

    // A room name must be valid and not used by another room in the same branch. The menu calls this
    // right after the name is typed. ignore = the room being renamed, or null for a new room.
    public void checkRoomName(CinemaBranch branch, String name, CinemaRoom ignore) throws ValidationException {
        Validator.validateRoomName(name);
        if (branch != null && branch.hasRoomNamed(name, ignore)) {
            throw new ValidationException("A room named '" + name.trim() + "' already exists in this branch.");
        }
    }

    // Same rule as removing a movie: not allowed once a showtime in this room has bookings
    // (so the saved booking history stays complete). Otherwise the room's showtimes are removed with it.
    // Returns how many showtimes were removed.
    public int removeRoom(String roomId, ArrayList<Showtime> showtimes) throws VenueException {
        CinemaRoom room = findRoom(roomId);
        if (room == null) {
            throw new VenueException("Room " + roomId + " not found.");
        }
        checkRoomCanBeRemoved(room);
        int removed = removeShowtimesOfRoom(room, showtimes);
        findBranchOfRoom(room).removeRoom(room);
        return removed;
    }

    private void checkRoomCanBeRemoved(CinemaRoom room) throws VenueException {
        for (Booking b : BookingManager.getInstance().getAllBookings()) {
            if (b.getShowtime().getRoom() == room) {
                throw new VenueException("Cannot remove '" + room.getRoomName()
                        + "': it has bookings on record, which must be kept.");
            }
        }
    }

    private int removeShowtimesOfRoom(CinemaRoom room, ArrayList<Showtime> showtimes) {
        ArrayList<Showtime> toRemove = new ArrayList<>();
        for (Showtime s : showtimes) {
            if (s.getRoom() == room) toRemove.add(s);
        }
        showtimes.removeAll(toRemove);
        return toRemove.size();
    }

    // ---------- finding ----------

    public CinemaBranch findBranch(String branchId) {
        for (CinemaBranch b : branches) {
            if (b.getBranchId().equalsIgnoreCase(branchId.trim())) return b;
        }
        return null;
    }

    public CinemaRoom findRoom(String roomId) {
        for (CinemaBranch b : branches) {
            for (CinemaRoom r : b.getRooms()) {
                if (r.getRoomId().equalsIgnoreCase(roomId.trim())) return r;
            }
        }
        return null;
    }

    public CinemaBranch findBranchOfRoom(CinemaRoom room) {
        for (CinemaBranch b : branches) {
            if (b.getRooms().contains(room)) return b;
        }
        return null;
    }

    // "SM City Lipa - Cinema 1"
    public String getCinemaName(CinemaRoom room) {
        CinemaBranch branch = findBranchOfRoom(room);
        if (branch == null) {
            return room.getRoomName();
        }
        return branch.getBranchName() + " - " + room.getRoomName();
    }

    public ArrayList<CinemaBranch> getBranches() { return branches; }
}
