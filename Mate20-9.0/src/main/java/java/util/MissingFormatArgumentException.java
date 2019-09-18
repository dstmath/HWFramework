package java.util;

public class MissingFormatArgumentException extends IllegalFormatException {
    private static final long serialVersionUID = 19190115;
    private String s;

    public MissingFormatArgumentException(String s2) {
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
        return "Format specifier '" + this.s + "'";
    }
}
