package cinema.service;

import java.util.ArrayList;

import cinema.exception.ValidationException;
import cinema.exception.VenueException;
import cinema.layout.SeatLayoutStrategy;
import cinema.layout.StandardLayout;
import cinema.model.Booking;
import cinema.model.CinemaBranch;
import cinema.model.CinemaRoom;
import cinema.model.Showtime;
import cinema.util.Validator;

// [Member A] Stores all branches. Others (Member B) use this to find rooms for showtimes.
public class VenueManager {
    private ArrayList<CinemaBranch> branches = new ArrayList<>();
    private int branchCounter = 0;
    private int roomCounter = 0;

    public String nextBranchId() { return "BR-" + (++branchCounter); }
    public String nextRoomId() { return "RM-" + (++roomCounter); }

    // ---------- branches ----------

    public void addBranch(CinemaBranch branch) throws ValidationException {
        if (isBranchNameTaken(branch.getBranchName(), null)) {
            throw new ValidationException("A branch named '" + branch.getBranchName() + "' already exists.");
        }
        branches.add(branch);
    }

    // Convenience for the menu: checks everything first, so an ID is only used when the branch is really added
    public CinemaBranch createBranch(String name, String location) throws ValidationException {
        Validator.validateBranchName(name);
        Validator.validateLocation(location);
        if (isBranchNameTaken(name, null)) {
            throw new ValidationException("A branch named '" + name.trim() + "' already exists.");
        }
        CinemaBranch branch = new CinemaBranch(nextBranchId(), name, location);
        branches.add(branch);
        return branch;
    }

    // Branch names must stay unique when renaming too
    public void renameBranch(CinemaBranch branch, String newName) throws ValidationException {
        Validator.validateBranchName(newName);
        if (isBranchNameTaken(newName, branch)) {
            throw new ValidationException("A branch named '" + newName.trim() + "' already exists.");
        }
        branch.setBranchName(newName);
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

    // Default layout (last row VIP). Used by the sample data in Main.
    public CinemaRoom addRoomToBranch(String branchId, String roomName, int rows, int seatsPerRow)
            throws ValidationException, VenueException {
        return addRoomToBranch(branchId, roomName, rows, seatsPerRow, new StandardLayout());
    }

    // Validates, creates the room, generates seats with the chosen layout and adds it.
    public CinemaRoom addRoomToBranch(String branchId, String roomName, int rows, int seatsPerRow,
            SeatLayoutStrategy layout) throws ValidationException, VenueException {
        CinemaBranch branch = findBranch(branchId);
        if (branch == null) {
            throw new VenueException("Branch " + branchId + " not found.");
        }
        Validator.validateRoomName(roomName);
        if (branch.hasRoomNamed(roomName, null)) {
            throw new ValidationException("A room named '" + roomName.trim() + "' already exists in this branch.");
        }
        Validator.validateRange("Rows", rows, 1, 26);          // rows are letters A-Z
        Validator.validateRange("Seats per row", seatsPerRow, 1, 30);
        CinemaRoom room = new CinemaRoom(nextRoomId(), roomName);
        room.generateSeats(rows, seatsPerRow, layout);
        branch.addRoom(room);
        return room;
    }

    // Room names must stay unique inside a branch
    public void renameRoom(CinemaRoom room, String newName) throws ValidationException {
        Validator.validateRoomName(newName);
        CinemaBranch branch = findBranchOfRoom(room);
        if (branch != null && branch.hasRoomNamed(newName, room)) {
            throw new ValidationException("A room named '" + newName.trim() + "' already exists in this branch.");
        }
        room.setRoomName(newName);
    }

    // Same rule as removing a movie: not allowed while a showtime in this room that hasn't ended
    // has confirmed bookings. Otherwise the room's showtimes are removed with it.
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
            if (b.getShowtime().getRoom() == room && "CONFIRMED".equals(b.getStatus())
                    && !b.getShowtime().hasEnded()) {
                throw new VenueException("Cannot remove '" + room.getRoomName()
                        + "': it has upcoming showtimes with confirmed bookings. Cancel those bookings first.");
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

    public ArrayList<CinemaBranch> getBranches() { return branches; }
}
