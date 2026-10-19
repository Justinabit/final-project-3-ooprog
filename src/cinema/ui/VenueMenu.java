package cinema.ui;

import cinema.service.VenueManager;

// [Member A] Admin menu for branches, rooms and seats
public class VenueMenu {
    private VenueManager venueManager;

    public VenueMenu(VenueManager venueManager) {
        this.venueManager = venueManager;
    }

    public void show() {
        System.out.println("\n--- MANAGE VENUES (ADMIN) ---");
        // TODO (A): menu -> 1. add branch  2. add room to branch  3. view all venues  0. back
        //           for "add room" ask for rows + seats per row, then call room.generateSeats(...)
    }
}
