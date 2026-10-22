package cinema.model;

// [Member C] A promo code created by the admin (percent off the total)
public class Promo {
    private String code;
    private int percentOff;

    public Promo(String code, int percentOff) {
        this.code = code.toUpperCase();
        this.percentOff = percentOff;
    }

    public String getCode() { return code; }
    public int getPercentOff() { return percentOff; }
}
