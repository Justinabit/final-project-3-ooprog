// [Member C] Example discount: 20% off for students
public class StudentDiscount implements DiscountStrategy {
    public String getName() { return "Student Discount (20%)"; }

    public double applyDiscount(double price) {
        // TODO (C): return price minus 20%
        return price;
    }

    // TODO (C): add more classes like SeniorDiscount, PromoCodeDiscount, etc.
}
