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
            System.out.println("  " + b.getBranchId() + "  " + b.getBranchName() + "  (" + b.getLocation() + ")");
            if (b.getRooms().isEmpty()) {
                System.out.println("      (no rooms yet)");
            }
            for (CinemaRoom r : b.getRooms()) {
                System.out.println(String.format("      %-6s %-14s %2d x %-2d  %3d seats (%d VIP)   %d upcoming show(s)",
                        r.getRoomId(), r.getRoomName(), r.getRows(), r.getSeatsPerRow(),
                        r.getSeats().size(), r.getVipSeatCount(), countUpcomingShows(r)));
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
        String name = ConsoleReader.readLine("Branch name: ");
        String location = ConsoleReader.readLine("Location: ");
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
                venueManager.renameBranch(branch, ConsoleReader.readLine("New branch name: "));
                Screen.success("Branch renamed to " + branch.getBranchName() + ".");
            } else if (choice == 2) {
                branch.setLocation(ConsoleReader.readLine("New location: "));
                Screen.success("Location updated.");
            } else if (choice != 0) {
                Screen.error("Invalid choice.");
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
        String name = ConsoleReader.readLine("Room name: ");
        int rows = ConsoleReader.readInt("Number of rows (" + VenueManager.MIN_ROWS + "-" + VenueManager.MAX_ROWS + "): ");
        int perRow = ConsoleReader.readInt("Seats per row (" + VenueManager.MIN_SEATS_PER_ROW + "-"
                + VenueManager.MAX_SEATS_PER_ROW + "): ");
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
        try {
            venueManager.renameRoom(room, ConsoleReader.readLine("New room name: "));
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
        String seatId = ConsoleReader.readLine("Seat to change (e.g. A1): ");
        if (room.findSeat(seatId) == null) {
            Screen.error("Seat " + seatId.toUpperCase() + " does not exist in this room.");
            return;
        }
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
            Seat seat = room.findSeat(seatId);
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

    // ---------- helpers ----------

    private CinemaBranch chooseBranch() {
        if (venueManager.getBranches().isEmpty()) {
            Screen.info("No branches yet.");
            return null;
        }
        for (CinemaBranch b : venueManager.getBranches()) {
            System.out.println("  " + b.getBranchId() + "  " + b.getBranchName());
        }
        CinemaBranch branch = venueManager.findBranch(ConsoleReader.readLine("Branch ID: "));
        if (branch == null) {
            Screen.error("Branch not found.");
        }
        return branch;
    }

    private CinemaRoom chooseRoom() {
        boolean anyRoom = false;
        for (CinemaBranch b : venueManager.getBranches()) {
            for (CinemaRoom r : b.getRooms()) {
                System.out.println("  " + r.getRoomId() + "  " + b.getBranchName() + " - " + r.getRoomName());
                anyRoom = true;
            }
        }
        if (!anyRoom) {
            Screen.info("No rooms yet. Add a room first.");
            return null;
        }
        CinemaRoom room = venueManager.findRoom(ConsoleReader.readLine("Room ID: "));
        if (room == null) {
            Screen.error("Room not found.");
        }
        return room;
    }

    private int countUpcomingShows(CinemaRoom room) {
        int count = 0;
        for (Showtime s : movieManager.getUpcomingShowtimes(null)) {
            if (s.getRoom() == room) count++;
        }
        return count;
    }
}
