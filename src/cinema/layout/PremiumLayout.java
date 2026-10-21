package cinema.layout;

// [Member A] The back half of the room is VIP (5 rows -> D and E are VIP)
public class PremiumLayout implements SeatLayoutStrategy {
    public String getName() { return "Premium (back half is VIP)"; }

    public boolean isVipRow(int rowIndex, int totalRows) {
        int firstVipRow = totalRows - totalRows / 2;
        return rowIndex >= firstVipRow;
    }
}
