package cinema.ui;

import cinema.exception.CinemaException;
import cinema.exception.GoBackException;
import cinema.exception.ValidationException;
import cinema.exception.VenueException;
import cinema.layout.EconomyLayout;
import cinema.layout.PremiumLayout;
import cinema.layout.SeatLayoutStrategy;
import cinema.layout.StandardLayout;
import cinema.model.CinemaBranch;
import cinema.model.CinemaRoom;
import cinema.model.Seat;
import cinema.model.Showtime;
import cinema.model.VipSeat;
import cinema.service.MovieManager;
import cinema.service.VenueManager;
import cinema.util.ConsoleReader;
import cinema.util.Format;
import cinema.util.Validator;

// [Member A] Admin menu for branches, rooms and seats
public class VenueMenu {
    private VenueManager venueManager;
    private MovieManager movieManager;   // needed to check showtimes before removing a room or branch

    public VenueMenu(VenueManager venueManager, MovieManager movieManager) {
        this.venueManager = venueManager;
        this.movieManager = movieManager;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            Screen.title("MANAGE VENUES (ADMIN)");
            Screen.option(1, "View all venues");
            Screen.option(2, "Add a branch");
            Screen.option(3, "Edit a branch");
            Screen.option(4, "Remove a branch");
            Screen.option(5, "Add a room to a branch");
            Screen.option(6, "Rename a room");
            Screen.option(7, "Remove a room");
            Screen.option(8, "View a room's seat map");
            Screen.option(9, "Change a seat type (Standard / VIP)");
            Screen.option(0, "Back");
            int choice = Screen.readMenuChoice(9);
            try {
                switch (choice) {
                    case 1: viewAll(); break;
                    case 2: addBranch(); break;
                    case 3: editBranch(); break;
                    case 4: removeBranch(); break;
                    case 5: addRoom(); break;
                    case 6: renameRoom(); break;
                    case 7: removeRoom(); break;
                    case 8: viewSeatMap(); break;
                    case 9: changeSeatType(); break;
                    case 0: back = true; break;
                    default: Screen.error("Invalid choice. Try again.");
                }
            } catch (GoBackException e) {
                Screen.backTo("Manage Venues");
            }
        }
    }

    // ---------- branches ----------

    private void viewAll() {
        if (venueManager.getBranches().isEmpty()) {
            Screen.info("No branches yet.");
            return;
        }
        Screen.section("ALL VENUES");
        int roomCount = 0;
        int seatCount = 0;
        for (CinemaBranch b : venueManager.getBranches()) {
            // long names and locations are cut with "..." so every line fits the screen
            System.out.println("  " + b.getBranchId() + "  "
                    + Format.shorten(b.getBranchName() + "  (" + b.getLocation() + ")", 64));
            if (b.getRooms().isEmpty()) {
                System.out.println("    (no rooms yet)");
            }
            for (CinemaRoom r : b.getRooms()) {
                System.out.println("    " + Format.fit(r.getRoomId(), 6) + " " + Format.fit(r.getRoomName(), 14)
                        + String.format(" %2d x %-2d  %3d seats (%d VIP)  %d upcoming show(s)", r.getRows(),
                        r.getSeatsPerRow(), r.getSeats().size(), r.getVipSeatCount(), countUpcomingShows(r)));
                roomCount++;
            }
            seatCount += b.getTotalSeats();
        }
        Screen.line();
        System.out.println("  Total: " + venueManager.getBranches().size() + " branch(es), "
                + roomCount + " room(s), " + seatCount + " seats");
    }

    private void addBranch() {
        Screen.section("ADD A BRANCH");
        String name = askBranchName("Branch name (blank to cancel): ", null);
        if (name == null) return;
        String location = askLocation("Location (blank to cancel): ");
        if (location == null) return;
        try {
            CinemaBranch branch = venueManager.createBranch(name, location);
            Screen.success("Branch added with ID " + branch.getBranchId() + ".");
        } catch (ValidationException e) {
            Screen.error(e.getMessage());
        }
    }

    private void editBranch() {
        CinemaBranch branch = chooseBranch();
        if (branch == null) return;
        Screen.section("EDIT " + branch.getBranchName());
        System.out.println("  Location: " + branch.getLocation());
        Screen.option(1, "Change name");
        Screen.option(2, "Change location");
        Screen.option(0, "Cancel");
        int choice = Screen.readChoice(2);
        try {
            if (choice == 1) {
                String name = askBranchName("New branch name (blank to cancel): ", branch);
                if (name == null) return;
                venueManager.renameBranch(branch, name);
                Screen.success("Branch renamed to " + branch.getBranchName() + ".");
            } else if (choice == 2) {
                String location = askLocation("New location (blank to cancel): ");
                if (location == null) return;
                branch.setLocation(location);
                Screen.success("Location updated.");
            }
        } catch (ValidationException e) {
            Screen.error(e.getMessage());
        }
    }

    private void removeBranch() {
        CinemaBranch branch = chooseBranch();
        if (branch == null) return;
        if (!ConsoleReader.readYesNo("Remove '" + branch.getBranchName() + "', its " + branch.getRooms().size()
                + " room(s) and their showtimes? (y/n): ")) {
            Screen.info("Cancelled.");
            return;
        }
        try {
            int removed = venueManager.removeBranch(branch.getBranchId(), movieManager.getShowtimes());
            Screen.success("Branch removed. " + removed + " showtime(s) were removed with it.");
        } catch (VenueException e) {
            Screen.error(e.getMessage());
        }
    }

    // ---------- rooms ----------

    private void addRoom() {
        if (venueManager.getBranches().isEmpty()) {
            Screen.error("Add a branch first.");
            return;
        }
        CinemaBranch branch = chooseBranch();
        if (branch == null) return;
        // each answer is checked right after it is typed, and asked again if it is wrong
        String name = askRoomName("Room name (blank to cancel): ", branch, null);
        if (name == null) return;
        int rows = ConsoleReader.readNumber("Number of rows (" + VenueManager.MIN_ROWS + "-" + VenueManager.MAX_ROWS
                + ", lettered A-Z): ", "Rows", VenueManager.MIN_ROWS, VenueManager.MAX_ROWS);
        int perRow = ConsoleReader.readNumber("Seats per row (" + VenueManager.MIN_SEATS_PER_ROW + "-"
                + VenueManager.MAX_SEATS_PER_ROW + "): ", "Seats per row",
                VenueManager.MIN_SEATS_PER_ROW, VenueManager.MAX_SEATS_PER_ROW);
        SeatLayoutStrategy layout = chooseLayout();
        if (layout == null) {
            Screen.info("Cancelled. No room was added.");
            return;
        }
        try {
            CinemaRoom room = venueManager.addRoomToBranch(branch.getBranchId(), name, rows, perRow, layout);
            Screen.success("Room added with ID " + room.getRoomId() + " (" + room.getSeats().size()
                    + " seats, " + room.getVipSeatCount() + " VIP).");
            printSeatMap(room);
        } catch (CinemaException e) {   // ValidationException or VenueException
            Screen.error(e.getMessage());
        }
    }

    // STRATEGY PATTERN: the admin picks how the VIP seats are arranged. Returns null if cancelled.
    private SeatLayoutStrategy chooseLayout() {
        SeatLayoutStrategy[] layouts = { new StandardLayout(), new PremiumLayout(), new EconomyLayout() };
        Screen.section("SEAT LAYOUT");
        for (int i = 0; i < layouts.length; i++) {
            Screen.option(i + 1, layouts[i].getName());
        }
        Screen.option(0, "Cancel");
        int choice = Screen.readChoice(layouts.length);
        if (choice == 0) {
            return null;
        }
        return layouts[choice - 1];
    }

    private void renameRoom() {
        CinemaRoom room = chooseRoom();
        if (room == null) return;
        String name = askRoomName("New room name (blank to cancel): ", venueManager.findBranchOfRoom(room), room);
        if (name == null) return;
        try {
            venueManager.renameRoom(room, name);
            Screen.success("Room renamed to " + room.getRoomName() + ".");
        } catch (ValidationException e) {
            Screen.error(e.getMessage());
        }
    }

    private void removeRoom() {
        CinemaRoom room = chooseRoom();
        if (room == null) return;
        if (!ConsoleReader.readYesNo("Remove '" + room.getRoomName() + "' and its showtimes? (y/n): ")) {
            Screen.info("Cancelled.");
            return;
        }
        try {
            int removed = venueManager.removeRoom(room.getRoomId(), movieManager.getShowtimes());
            Screen.success("Room removed. " + removed + " showtime(s) were removed with it.");
        } catch (VenueException e) {
            Screen.error(e.getMessage());
        }
    }

    // ---------- seats ----------

    private void viewSeatMap() {
        CinemaRoom room = chooseRoom();
        if (room == null) return;
        printSeatMap(room);
    }

    private void changeSeatType() {
        CinemaRoom room = chooseRoom();
        if (room == null) return;
        printSeatMap(room);
        String seatId = askSeatToChange(room);
        if (seatId == null) return;
        Seat seat = room.findSeat(seatId);
        Screen.info("Seat " + seat.getSeatNumber() + " is currently " + seat.getSeatType() + ".");
        Screen.option(1, "Standard seat");
        Screen.option(2, "VIP seat (+" + Format.peso(VipSeat.EXTRA_CHARGE) + ")");
        Screen.option(0, "Cancel");
        int choice = Screen.readChoice(2);
        if (choice == 0) {
            Screen.info("Cancelled. The seat was not changed.");
            return;
        }
        try {
            venueManager.changeSeatType(room, seatId, choice == 2, movieManager.getShowtimes());
            seat = room.findSeat(seatId);   // changing the type replaces the seat object
            Screen.success("Seat " + seat.getSeatNumber() + " is now " + seat.getSeatType() + ".");
            Screen.info("Bookings that were already made keep the price they paid.");
        } catch (ValidationException e) {
            Screen.error(e.getMessage());
        }
    }

    private void printSeatMap(CinemaRoom room) {
        Screen.section("SEAT MAP: " + room.getRoomName() + " (" + room.getRoomId() + ")");
        System.out.println("  Layout: " + room.getLayoutName() + "  |  " + room.getSeats().size()
                + " seats, " + room.getVipSeatCount() + " VIP");
        SeatMap.print(room, null);
    }

    // ---------- helpers: each answer is checked as soon as it is typed and asked again if wrong ----------
    // (a blank answer cancels and returns null)

    // ignore = the branch being renamed (it may keep its own name), or null for a new branch
    private String askBranchName(String prompt, CinemaBranch ignore) {
        while (true) {
            String name = ConsoleReader.readLine(prompt);
            if (name.isEmpty()) {
                Screen.info("Cancelled.");
                return null;
            }
            try {
                venueManager.checkBranchName(name, ignore);
                return name;
            } catch (ValidationException e) {
                Screen.error(e.getMessage());
            }
        }
    }

    private String askLocation(String prompt) {
        while (true) {
            String location = ConsoleReader.readLine(prompt);
            if (location.isEmpty()) {
                Screen.info("Cancelled.");
                return null;
            }
            try {
                Validator.validateLocation(location);
                return location;
            } catch (ValidationException e) {
                Screen.error(e.getMessage());
            }
        }
    }

    // ignore = the room being renamed, or null for a new room
    private String askRoomName(String prompt, CinemaBranch branch, CinemaRoom ignore) {
        while (true) {
            String name = ConsoleReader.readLine(prompt);
            if (name.isEmpty()) {
                Screen.info("Cancelled.");
                return null;
            }
            try {
                venueManager.checkRoomName(branch, name, ignore);
                return name;
            } catch (ValidationException e) {
                Screen.error(e.getMessage());
            }
        }
    }

    // The seat must exist and must not be booked for a show that hasn't ended
    private String askSeatToChange(CinemaRoom room) {
        while (true) {
            String seatId = ConsoleReader.readLine("Seat to change (e.g. A1, blank to cancel): ");
            if (seatId.isEmpty()) {
                Screen.info("Cancelled.");
                return null;
            }
            try {
                venueManager.checkSeatCanChange(room, seatId, movieManager.getShowtimes());
                return seatId;
            } catch (ValidationException e) {
                Screen.error(e.getMessage());
            }
        }
    }

    private CinemaBranch chooseBranch() {
        if (venueManager.getBranches().isEmpty()) {
            Screen.info("No branches yet.");
            return null;
        }
        for (CinemaBranch b : venueManager.getBranches()) {
            System.out.println("  " + b.getBranchId() + "  " + b.getBranchName());
        }
        while (true) {
            String id = ConsoleReader.readLine("Branch ID (blank to cancel): ");
            if (id.isEmpty()) {
                Screen.info("Cancelled.");
                return null;
            }
            CinemaBranch branch = venueManager.findBranch(id);
            if (branch != null) {
                return branch;
            }
            Screen.error("No branch with the ID " + id.toUpperCase() + ". Please try again.");
        }
    }

    private CinemaRoom chooseRoom() {
        boolean anyRoom = false;
        for (CinemaBranch b : venueManager.getBranches()) {
            for (CinemaRoom r : b.getRooms()) {
                System.out.println("  " + Format.fit(r.getRoomId(), 6) + "  "
                        + Format.shorten(b.getBranchName() + " - " + r.getRoomName(), 65));
                anyRoom = true;
            }
        }
        if (!anyRoom) {
            Screen.info("No rooms yet. Add a room first.");
            return null;
        }
        while (true) {
            String id = ConsoleReader.readLine("Room ID (blank to cancel): ");
            if (id.isEmpty()) {
                Screen.info("Cancelled.");
                return null;
            }
            CinemaRoom room = venueManager.findRoom(id);
            if (room != null) {
                return room;
            }
            Screen.error("No room with the ID " + id.toUpperCase() + ". Please try again.");
        }
    }

    private int countUpcomingShows(CinemaRoom room) {
        int count = 0;
        for (Showtime s : movieManager.getUpcomingShowtimes(null)) {
            if (s.getRoom() == room) count++;
        }
        return count;
    }
}
