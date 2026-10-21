package cinema.layout;

// [Member A] No VIP seats at all
public class EconomyLayout implements SeatLayoutStrategy {
    public String getName() { return "Economy (no VIP seats)"; }

    public boolean isVipRow(int rowIndex, int totalRows) {
        return false;
    }
}
