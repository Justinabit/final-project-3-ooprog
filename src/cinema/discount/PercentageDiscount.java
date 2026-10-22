package cinema.discount;

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

    @Override
    public double applyDiscount(double price) {
        return price * (100 - percentOff) / 100.0;
    }
}