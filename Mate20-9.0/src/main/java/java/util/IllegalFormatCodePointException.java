package java.util;

public class IllegalFormatCodePointException extends IllegalFormatException {
    private static final long serialVersionUID = 19080630;
    private int c;

    public IllegalFormatCodePointException(int c2) {
        this.c = c2;
    }

    public int getCodePoint() {
        return this.c;
    }

    public String getMessage() {
        return String.format("Code point = %#x", Integer.valueOf(this.c));
    }
}
