package java.util;

public class UnknownFormatFlagsException extends IllegalFormatException {
    private static final long serialVersionUID = 19370506;
    private String flags;

    public UnknownFormatFlagsException(String f) {
        if (f == null) {
            throw new NullPointerException();
        }
        this.flags = f;
    }

    public String getFlags() {
        return this.flags;
    }

    public String getMessage() {
        return "Flags = " + this.flags;
    }
}
