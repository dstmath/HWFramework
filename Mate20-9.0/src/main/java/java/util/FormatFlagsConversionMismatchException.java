package java.util;

public class FormatFlagsConversionMismatchException extends IllegalFormatException {
    private static final long serialVersionUID = 19120414;
    private char c;
    private String f;

    public FormatFlagsConversionMismatchException(String f2, char c2) {
        if (f2 != null) {
            this.f = f2;
            this.c = c2;
            return;
        }
        throw new NullPointerException();
    }

    public String getFlags() {
        return this.f;
    }

    public char getConversion() {
        return this.c;
    }

    public String getMessage() {
        return "Conversion = " + this.c + ", Flags = " + this.f;
    }
}
