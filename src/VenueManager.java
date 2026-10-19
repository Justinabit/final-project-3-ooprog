import java.util.ArrayList;

// [Member A] Stores all branches. Others (Member B) use this to find rooms for showtimes.
public class VenueManager {
    private ArrayList<CinemaBranch> branches = new ArrayList<>();
    private int branchCounter = 0;
    private int roomCounter = 0;

    public String nextBranchId() { return "BR-" + (++branchCounter); }
    public String nextRoomId() { return "RM-" + (++roomCounter); }

    public void addBranch(CinemaBranch branch) throws ValidationException {
        Validator.validateRequired("Branch name", branch.getBranchName());
        Validator.validateRequired("Branch location", branch.getLocation());
        for (CinemaBranch b : branches) {
            if (b.getBranchName().equalsIgnoreCase(branch.getBranchName())) {
                throw new ValidationException("A branch named '" + branch.getBranchName() + "' already exists.");
            }
        }
        branches.add(branch);
    }

    // Convenience for the menu: validates, creates the room, generates seats and adds it.
    public CinemaRoom addRoomToBranch(String branchId, String roomName, int rows, int seatsPerRow)
            throws ValidationException {
        CinemaBranch branch = findBranch(branchId);
        if (branch == null) {
            throw new ValidationException("Branch not found.");
        }
        Validator.validateRoomName(roomName);
        Validator.validateRange("Rows", rows, 1, 26);          // rows are letters A-Z
        Validator.validateRange("Seats per row", seatsPerRow, 1, 30);
        CinemaRoom room = new CinemaRoom(nextRoomId(), roomName.trim());
        room.generateSeats(rows, seatsPerRow);
        branch.addRoom(room);
        return room;
    }

    public CinemaBranch findBranch(String branchId) {
        for (CinemaBranch b : branches) {
            if (b.getBranchId().equalsIgnoreCase(branchId)) return b;
        }
        return null;
    }

    public CinemaRoom findRoom(String roomId) {
        for (CinemaBranch b : branches) {
            for (CinemaRoom r : b.getRooms()) {
                if (r.getRoomId().equalsIgnoreCase(roomId)) return r;
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
