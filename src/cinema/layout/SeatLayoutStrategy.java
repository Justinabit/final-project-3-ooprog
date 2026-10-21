package cinema.layout;

// [Member A] STRATEGY PATTERN #3: different ways to decide which rows of a room are VIP.
// The admin picks one when adding a room, and CinemaRoom uses it to create the seats.
public interface SeatLayoutStrategy {
    String getName();
    boolean isVipRow(int rowIndex, int totalRows);   // rowIndex 0 = row A (front, nearest the screen)
}
