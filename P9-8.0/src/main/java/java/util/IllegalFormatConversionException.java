package java.util;

public class IllegalFormatConversionException extends IllegalFormatException {
    private static final long serialVersionUID = 17000126;
    private Class<?> arg;
    private char c;

    public IllegalFormatConversionException(char c, Class<?> arg) {
        if (arg == null) {
            throw new NullPointerException();
        }
        this.c = c;
        this.arg = arg;
    }

    public char getConversion() {
        return this.c;
    }

    public Class<?> getArgumentClass() {
        return this.arg;
    }

    public String getMessage() {
        return String.format("%c != %s", Character.valueOf(this.c), this.arg.getName());
    }
}
