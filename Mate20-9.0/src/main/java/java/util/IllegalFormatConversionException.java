package java.util;

public class IllegalFormatConversionException extends IllegalFormatException {
    private static final long serialVersionUID = 17000126;
    private Class<?> arg;
    private char c;

    public IllegalFormatConversionException(char c2, Class<?> arg2) {
        if (arg2 != null) {
            this.c = c2;
            this.arg = arg2;
            return;
        }
        throw new NullPointerException();
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
