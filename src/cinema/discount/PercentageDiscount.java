package cinema.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

// [Member C] Base class for every "X% off" discount, so the formula lives in ONE place.
// Subclasses only supply their percentage and their display name.
public abstract class PercentageDiscount implements DiscountStrategy {
    private final int percentOff;

    protected PercentageDiscount(int percentOff) {
        if (percentOff < 0 || percentOff > 100) {
            throw new IllegalArgumentException("Percent off must be between 0 and 100.");
        }
        this.percentOff = percentOff;
    }

    public int getPercentOff() { return percentOff; }

    // New price = price x (100 - percentOff) / 100, rounded to whole centavos (HALF_UP).
    // BigDecimal is used so there is no floating-point error, e.g. 1110.00 with 15% off is exactly 943.50.
    @Override
    public double applyDiscount(double price) {
        if (Double.isNaN(price) || Double.isInfinite(price) || price < 0) {
            throw new IllegalArgumentException("Price must be a number that is zero or more.");
        }
        BigDecimal original = BigDecimal.valueOf(price);
        BigDecimal kept = BigDecimal.valueOf(100 - percentOff);   // the part of the price the customer still pays
        return original.multiply(kept)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}