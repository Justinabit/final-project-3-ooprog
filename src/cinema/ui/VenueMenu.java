package cinema.ui;

import cinema.exception.ValidationException;
import cinema.model.CinemaBranch;
import cinema.model.CinemaRoom;
import cinema.service.VenueManager;
import cinema.util.ConsoleReader;

// [Member A] Admin menu for branches, rooms and seats
public class VenueMenu {
    private VenueManager venueManager;

    public VenueMenu(VenueManager venueManager) {
        this.venueManager = venueManager;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- MANAGE VENUES (ADMIN) ---");
            System.out.println("1. Add branch");
            System.out.println("2. Add room to branch");
            System.out.println("3. View all venues");
            System.out.println("0. Back");
            int choice = ConsoleReader.readInt("Choose: ");
            switch (choice) {
                case 1: addBranch(); break;
                case 2: addRoom(); break;
                case 3: viewAll(); break;
                case 0: back = true; break;
                default: System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void addBranch() {
        String name = ConsoleReader.readLine("Branch name: ");
        String location = ConsoleReader.readLine("Location: ");
        try {
            CinemaBranch branch = new CinemaBranch(venueManager.nextBranchId(), name, location);
            venueManager.addBranch(branch);
            System.out.println("Branch added with ID " + branch.getBranchId() + ".");
        } catch (ValidationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addRoom() {
        if (venueManager.getBranches().isEmpty()) {
            System.out.println("Add a branch first.");
            return;
        }
        CinemaBranch branch = chooseBranch();
        if (branch == null) return;
        String name = ConsoleReader.readLine("Room name: ");
        int rows = ConsoleReader.readInt("Number of rows (1-26): ");
        int perRow = ConsoleReader.readInt("Seats per row (1-30): ");
        try {
            CinemaRoom room = venueManager.addRoomToBranch(branch.getBranchId(), name, rows, perRow);
            System.out.println("Room added with ID " + room.getRoomId() + " (" + room.getSeats().size()
                    + " seats; last row is VIP when there is more than one row).");
        } catch (ValidationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private CinemaBranch chooseBranch() {
        for (CinemaBranch b : venueManager.getBranches()) {
            System.out.println("  " + b.getBranchId() + " - " + b.getBranchName());
        }
        String id = ConsoleReader.readLine("Branch ID: ");
        CinemaBranch branch = venueManager.findBranch(id);
        if (branch == null) System.out.println("Branch not found.");
        return branch;
    }

    private void viewAll() {
        if (venueManager.getBranches().isEmpty()) {
            System.out.println("No branches yet.");
            return;
        }
        for (CinemaBranch b : venueManager.getBranches()) {
            System.out.println(b.getBranchId() + " | " + b.getBranchName() + " (" + b.getLocation() + ")");
            if (b.getRooms().isEmpty()) System.out.println("    (no rooms)");
            for (CinemaRoom r : b.getRooms()) {
                System.out.println("    " + r.getRoomId() + " | " + r.getRoomName() + " | " + r.getSeats().size() + " seats");
            }
        }
    }
}
