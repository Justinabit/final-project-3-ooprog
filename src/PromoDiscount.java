// [Member C] Discount strategy created from a promo code
public class PromoDiscount implements DiscountStrategy {
    private Promo promo;

    public PromoDiscount(Promo promo) {
        this.promo = promo;
    }

    public String getName() { return "Promo " + promo.getCode() + " (" + promo.getPercentOff() + "% off)"; }

    public double applyDiscount(double price) { return price * (100 - promo.getPercentOff()) / 100.0; }
}
