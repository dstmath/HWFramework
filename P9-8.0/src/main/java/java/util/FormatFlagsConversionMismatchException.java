package java.util;

public class FormatFlagsConversionMismatchException extends IllegalFormatException {
    private static final long serialVersionUID = 19120414;
    private char c;
    private String f;

    public FormatFlagsConversionMismatchException(String f, char c) {
        if (f == null) {
            throw new NullPointerException();
        }
        this.f = f;
        this.c = c;
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
