package java.util;

public class MissingFormatWidthException extends IllegalFormatException {
    private static final long serialVersionUID = 15560123;
    private String s;

    public MissingFormatWidthException(String s2) {
        if (s2 != null) {
            this.s = s2;
            return;
        }
        throw new NullPointerException();
    }

    public String getFormatSpecifier() {
        return this.s;
    }

    public String getMessage() {
        return this.s;
    }
}
