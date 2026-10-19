package cinema.service;

import java.util.ArrayList;

import cinema.exception.ValidationException;
import cinema.model.Promo;
import cinema.util.Validator;

// [Member C] Stores promo codes (logic only, no Scanner)
public class PromoManager {
    private ArrayList<Promo> promos = new ArrayList<>();

    public void addPromo(String code, int percentOff) throws ValidationException {
        Validator.validatePromoCode(code);
        Validator.validateRange("Discount percent", percentOff, 1, 90);
        if (findPromo(code) != null) {
            throw new ValidationException("Promo code " + code.toUpperCase() + " already exists.");
        }
        promos.add(new Promo(code, percentOff));
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
