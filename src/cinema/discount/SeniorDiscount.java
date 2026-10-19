package cinema.discount;

// [Member C] 20% off for customers aged 60 and above
public class SeniorDiscount implements DiscountStrategy {
    public static final int MIN_AGE = 60;

    public String getName() { return "Senior Discount (20%)"; }

    public double applyDiscount(double price) { return price * 0.80; }
}
