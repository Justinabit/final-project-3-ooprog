package cinema.model;

import java.util.ArrayList;

// [Member A] A cinema branch (e.g. "SM Mall Branch") that contains rooms
public class CinemaBranch {
    private String branchId;
    private String branchName;
    private String location;
    private ArrayList<CinemaRoom> rooms = new ArrayList<>();

    public CinemaBranch(String branchId, String branchName, String location) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.location = location;
    }

    public void addRoom(CinemaRoom room) {
        // TODO (A): add the room (don't allow two rooms with the same name)
    }

    // TODO (A): getters (getBranchId, getBranchName, getLocation, getRooms)
}
