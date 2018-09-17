package java.util;

public class UnknownFormatConversionException extends IllegalFormatException {
    private static final long serialVersionUID = 19060418;
    private String s;

    public UnknownFormatConversionException(String s) {
        if (s == null) {
            throw new NullPointerException();
        }
        this.s = s;
    }

    public String getConversion() {
        return this.s;
    }

    public String getMessage() {
        return String.format("Conversion = '%s'", this.s);
    }
}
