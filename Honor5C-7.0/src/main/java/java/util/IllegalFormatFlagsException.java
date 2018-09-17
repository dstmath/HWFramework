package java.util;

public class IllegalFormatFlagsException extends IllegalFormatException {
    private static final long serialVersionUID = 790824;
    private String flags;

    public IllegalFormatFlagsException(String f) {
        if (f == null) {
            throw new NullPointerException();
        }
        this.flags = f;
    }

    public String getFlags() {
        return this.flags;
    }

    public String getMessage() {
        return "Flags = '" + this.flags + "'";
    }
}
