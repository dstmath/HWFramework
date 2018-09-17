package java.util;

public class DuplicateFormatFlagsException extends IllegalFormatException {
    private static final long serialVersionUID = 18890531;
    private String flags;

    public DuplicateFormatFlagsException(String f) {
        if (f == null) {
            throw new NullPointerException();
        }
        this.flags = f;
    }

    public String getFlags() {
        return this.flags;
    }

    public String getMessage() {
        return String.format("Flags = '%s'", this.flags);
    }
}
