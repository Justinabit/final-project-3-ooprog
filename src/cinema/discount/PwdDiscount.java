package cinema.discount;

// [Member C] 20% off for persons with disability (a valid PWD ID is checked at the entrance)
public class PwdDiscount implements DiscountStrategy {
    private String pwdIdNumber;

    public PwdDiscount(String pwdIdNumber) {
        this.pwdIdNumber = pwdIdNumber;
    }

    public String getName() { return "PWD Discount (20%) - ID " + pwdIdNumber; }

    public double applyDiscount(double price) { return price * 0.80; }
}
