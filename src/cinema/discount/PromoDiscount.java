package cinema.discount;

import cinema.model.Promo;

// [Member C] Discount strategy created from a promo code
public class PromoDiscount extends PercentageDiscount {
    private Promo promo;

    public PromoDiscount(Promo promo) {
        super(promo.getPercentOff());
        this.promo = promo;
    }

    public String getName() { return "Promo " + promo.getCode() + " (" + getPercentOff() + "% off)"; }
}