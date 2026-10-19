package cinema.service;

import java.util.ArrayList;

import cinema.model.CinemaBranch;
import cinema.model.CinemaRoom;

// [Member A] Stores all branches. Others (Member B) use this to find rooms for showtimes.
public class VenueManager {
    private ArrayList<CinemaBranch> branches = new ArrayList<>();

    public void addBranch(CinemaBranch branch) {
        // TODO (A): validate and add (no duplicate branch names)
    }

    public CinemaBranch findBranch(String branchId) {
        // TODO (A): search the list and return the branch (or null)
        return null;
    }

    public CinemaRoom findRoom(String roomId) {
        // TODO (A): search every branch's rooms and return the matching room (or null)
        return null;
    }

    public ArrayList<CinemaBranch> getBranches() {
        return branches;
    }
}
