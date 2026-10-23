package cinema.model;

import java.time.LocalDate;
import java.util.ArrayList;

// [Member C] A promo code created by the admin (percent off the total).
// It has a last valid day, and each customer can use it only once.
public class Promo {
    private String code;
    private int percentOff;
    private LocalDate validUntil;                            // last day the code can be used
    private ArrayList<String> usedBy = new ArrayList<>();    // IDs of customers who already used it

    public Promo(String code, int percentOff, LocalDate validUntil) {
        this.code = code.trim().toUpperCase();
        this.percentOff = percentOff;
        this.validUntil = validUntil;
    }

    public String getCode() { return code; }
    public int getPercentOff() { return percentOff; }
    public LocalDate getValidUntil() { return validUntil; }
    public ArrayList<String> getUsedBy() { return usedBy; }

    public boolean isExpired() {
        return LocalDate.now().isAfter(validUntil);
    }

    public boolean isUsedBy(String customerId) {
        return usedBy.contains(customerId);
    }

    // Called after a paid booking used this code
    public void markUsedBy(String customerId) {
        if (!isUsedBy(customerId)) {
            usedBy.add(customerId);
        }
    }
}
