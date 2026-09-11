public class DiscountCode {
    private final String code;
    private final double percentOff;

    public DiscountCode(String code, double percentOff) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Discount code cannot be empty.");
        }

        if (percentOff < 0 || percentOff > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100.");
        }

        this.code = code;
        this.percentOff = percentOff;
    }

    public String getCode() {
        return code;
    }

    public double getPercentOff() {
        return percentOff;
    }

    @Override
    public String toString() {
        return code + " (" + percentOff + "% off)";
    }
}