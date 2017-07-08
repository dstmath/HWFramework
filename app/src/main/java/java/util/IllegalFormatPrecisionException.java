package java.util;

public class IllegalFormatPrecisionException extends IllegalFormatException {
    private static final long serialVersionUID = 18711008;
    private int p;

    public IllegalFormatPrecisionException(int p) {
        this.p = p;
    }

    public int getPrecision() {
        return this.p;
    }

    public String getMessage() {
        return Integer.toString(this.p);
    }
}
