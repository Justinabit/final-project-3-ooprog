package cinema.discount;

// [Member C] 20% off for students
public class StudentDiscount implements DiscountStrategy {
    public String getName() { return "Student Discount (20%)"; }

    public double applyDiscount(double price) { return price * 0.80; }
}
