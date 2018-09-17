package java.util;

public class MissingFormatArgumentException extends IllegalFormatException {
    private static final long serialVersionUID = 19190115;
    private String s;

    public MissingFormatArgumentException(String s) {
        if (s == null) {
            throw new NullPointerException();
        }
        this.s = s;
    }

    public String getFormatSpecifier() {
        return this.s;
    }

    public String getMessage() {
        return "Format specifier '" + this.s + "'";
    }
}
