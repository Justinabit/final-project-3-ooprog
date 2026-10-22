package cinema.discount;

// [Member C] 20% off for students (valid school ID checked at the entrance)
public class StudentDiscount extends PercentageDiscount {
    public static final int PERCENT = 20;

    public StudentDiscount() { super(PERCENT); }

    public String getName() { return "Student Discount (" + PERCENT + "%)"; }
}