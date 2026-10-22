package cinema.layout;

// [Member A] Only the last row is VIP (when the room has more than one row)
public class StandardLayout implements SeatLayoutStrategy {
    public String getName() { return "Standard (last row is VIP)"; }

    public boolean isVipRow(int rowIndex, int totalRows) {
        return totalRows > 1 && rowIndex == totalRows - 1;
    }
}
