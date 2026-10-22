package cinema.model;

import java.util.ArrayList;

import cinema.exception.ValidationException;
import cinema.util.Validator;

// [Member A] A cinema branch (e.g. "SM Mall Branch") that contains rooms
public class CinemaBranch {
    private String branchId;
    private String branchName;
    private String location;
    private ArrayList<CinemaRoom> rooms = new ArrayList<>();

    // The setters validate, so an invalid branch can never be created (ENCAPSULATION)
    public CinemaBranch(String branchId, String branchName, String location) throws ValidationException {
        this.branchId = branchId;
        setBranchName(branchName);
        setLocation(location);
    }

    public void addRoom(CinemaRoom room) throws ValidationException {
        if (hasRoomNamed(room.getRoomName(), null)) {
            throw new ValidationException("A room named '" + room.getRoomName() + "' already exists in this branch.");
        }
        rooms.add(room);
    }

    public void removeRoom(CinemaRoom room) {
        rooms.remove(room);
    }

    // ignore = the room being renamed (it may keep its own name), or null
    public boolean hasRoomNamed(String name, CinemaRoom ignore) {
        for (CinemaRoom existing : rooms) {
            if (existing != ignore && existing.getRoomName().equalsIgnoreCase(name.trim())) return true;
        }
        return false;
    }

    public int getTotalSeats() {
        int total = 0;
        for (CinemaRoom room : rooms) {
            total += room.getSeats().size();
        }
        return total;
    }

    public String getBranchId() { return branchId; }
    public String getBranchName() { return branchName; }
    public String getLocation() { return location; }
    public ArrayList<CinemaRoom> getRooms() { return rooms; }

    public void setBranchName(String branchName) throws ValidationException {
        Validator.validateBranchName(branchName);
        this.branchName = branchName.trim();
    }

    public void setLocation(String location) throws ValidationException {
        Validator.validateLocation(location);
        this.location = location.trim();
    }
}
