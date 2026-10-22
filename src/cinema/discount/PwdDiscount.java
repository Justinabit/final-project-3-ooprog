package cinema.discount;

// [Member C] 20% off for persons with disability (a valid PWD ID is checked at the entrance)
public class PwdDiscount extends PercentageDiscount {
    public static final int PERCENT = 20;
    private String pwdIdNumber;

    public PwdDiscount(String pwdIdNumber) {
        super(PERCENT);
        this.pwdIdNumber = pwdIdNumber;
    }

    public String getName() { return "PWD Discount (" + PERCENT + "%) - ID " + pwdIdNumber; }
}