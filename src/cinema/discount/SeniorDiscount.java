package cinema.discount;

// [Member C] 20% off for senior citizens (aged 60 and above)
public class SeniorDiscount extends PercentageDiscount {
    public static final int MIN_AGE = 60;
    public static final int PERCENT = 20;

    public SeniorDiscount() { super(PERCENT); }

    public String getName() { return "Senior Discount (" + PERCENT + "%)"; }
}