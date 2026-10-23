package cinema.service;

import java.time.LocalDate;
import java.util.ArrayList;

import cinema.exception.ValidationException;
import cinema.model.Promo;
import cinema.util.Validator;

// [Member C] Stores promo codes (logic only, no Scanner)
public class PromoManager {
    private ArrayList<Promo> promos = new ArrayList<>();

    // A new promo code from the admin (or the sample data)
    public void addPromo(String code, int percentOff, LocalDate validUntil) throws ValidationException {
        Validator.validatePromoCode(code);
        Validator.validateRange("Discount percent", percentOff, 1, 90);
        if (validUntil == null || validUntil.isBefore(LocalDate.now())) {
            throw new ValidationException("The promo end date cannot be in the past.");
        }
        restorePromo(new Promo(code, percentOff, validUntil));
    }

    // Adds a promo loaded from the data file (it may already be expired, which is fine for history)
    public void restorePromo(Promo promo) throws ValidationException {
        if (findPromo(promo.getCode()) != null) {
            throw new ValidationException("Promo code " + promo.getCode() + " already exists.");
        }
        promos.add(promo);
    }

    public boolean removePromo(String code) {
        Promo p = findPromo(code);
        return p != null && promos.remove(p);
    }

    public Promo findPromo(String code) {
        for (Promo p : promos) {
            if (p.getCode().equalsIgnoreCase(code.trim())) return p;
        }
        return null;
    }

    public ArrayList<Promo> getPromos() { return promos; }
}
