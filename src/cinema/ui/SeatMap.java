package cinema.ui;

import cinema.model.CinemaRoom;
import cinema.model.Seat;
import cinema.model.Showtime;
import cinema.model.VipSeat;
import cinema.util.Format;

// Draws a room's seats with the screen at the front, like a real cinema booking page.
//   [A1 ] = regular seat     {E1 } = VIP seat (can be booked, PHP 200 more)
//   [XX ] / {XX } = seat already taken
// Used by the booking screen (with a showtime) and the admin venue screen (showtime = null).
public class SeatMap {
    private static final int CELL_WIDTH = 6;     // "[A1 ] " is 6 characters
    private static final String AISLE = "   ";   // walkway in the middle of each row
    private static final String MARGIN = "      ";

    public static void print(CinemaRoom room, Showtime showtime) {
        if (room.getSeats().isEmpty()) {
            Screen.info("This room has no seats.");
            return;
        }
        int seatsPerRow = room.getSeatsPerRow();
        int aisleAfter = 0;                 // no aisle in very small rooms
        if (seatsPerRow >= 4) {
            aisleAfter = seatsPerRow / 2;
        }
        int width = seatsPerRow * CELL_WIDTH - 1;
        if (aisleAfter > 0) {
            width += AISLE.length();
        }

        // The screen, drawn as a box across the front of the room
        System.out.println();
        System.out.println(MARGIN + " " + Screen.repeat("_", width));
        System.out.println(MARGIN + "|" + Screen.spaces(width) + "|");
        System.out.println(MARGIN + "|" + Screen.center("S C R E E N", width) + "|");
        System.out.println(MARGIN + "|" + Screen.repeat("_", width) + "|");
        System.out.println();

        // The seats, one row at a time, with the row letter on both sides
        String line = "";
        int seatInRow = 0;
        for (Seat seat : room.getSeats()) {
            String rowLetter = seat.getSeatNumber().substring(0, 1);
            if (seatInRow == 0) {
                line = "    " + rowLetter + "  ";
            } else {
                line += " ";
            }
            line += cell(seat, showtime);
            seatInRow++;
            if (seatInRow == aisleAfter) {
                line += AISLE;
            }
            if (seatInRow == seatsPerRow) {
                System.out.println(line + "  " + rowLetter);
                seatInRow = 0;
            }
        }

        // Legend and prices
        System.out.println();
        if (showtime != null) {
            double base = showtime.getBasePrice();
            System.out.println("    [A1 ] Regular  " + Format.peso(base)
                    + "     {A1 } VIP  " + Format.peso(base + VipSeat.EXTRA_CHARGE)
                    + "     [XX ] Taken");
        } else {
            System.out.println("    [A1 ] Regular seat     {A1 } VIP seat (+" + Format.peso(VipSeat.EXTRA_CHARGE) + ")");
        }
        System.out.println("    VIP seats can be booked too, at " + Format.peso(VipSeat.EXTRA_CHARGE) + " more per seat.");
    }

    // One seat: [A1 ] for regular, {E1 } for VIP, XX when already taken
    private static String cell(Seat seat, Showtime showtime) {
        String label = seat.getSeatNumber();
        if (showtime != null && showtime.isSeatTaken(label)) {
            label = "XX";
        }
        label = String.format("%-3s", label);
        if (seat.isVip()) {
            return "{" + label + "}";
        }
        return "[" + label + "]";
    }
}
