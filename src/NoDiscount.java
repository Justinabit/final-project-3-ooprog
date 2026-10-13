// [Member C] Default: no discount
public class NoDiscount implements DiscountStrategy {
    public String getName() { return "No Discount"; }
    public double applyDiscount(double price) { return price; }
}
