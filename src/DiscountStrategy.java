// [Member C] STRATEGY PATTERN #2: different ways to compute a discount
public interface DiscountStrategy {
    String getName();
    double applyDiscount(double price);   // returns the new (lower) price
}
