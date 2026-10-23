package cinema.discount;

// [Member C] 20% off for persons with disability (a valid PWD ID is checked at the entrance)
public class PwdDiscount extends PercentageDiscount {
    public static final int PERCENT = 20;
    private String pwdIdNumber;

    // The ID number is required: a PwdDiscount can never be created without one,
    // so BookingManager can trust it no matter which screen created it.
    public PwdDiscount(String pwdIdNumber) {
        super(PERCENT);
        if (pwdIdNumber == null || pwdIdNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("A PWD ID number is required.");
        }
        this.pwdIdNumber = pwdIdNumber.trim();
    }

    public String getName() { return "PWD Discount (" + PERCENT + "%) - ID " + pwdIdNumber; }
}