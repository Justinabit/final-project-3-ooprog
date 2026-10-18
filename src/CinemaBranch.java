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

    public void addRoom(CinemaRoom room) throws ValidationException {
        for (CinemaRoom existing : rooms) {
            if (existing.getRoomName().equalsIgnoreCase(room.getRoomName())) {
                throw new ValidationException("A room named '" + room.getRoomName() + "' already exists in this branch.");
            }
        }
        rooms.add(room);
    }

    public String getBranchId() { return branchId; }
    public String getBranchName() { return branchName; }
    public String getLocation() { return location; }
    public ArrayList<CinemaRoom> getRooms() { return rooms; }
}
