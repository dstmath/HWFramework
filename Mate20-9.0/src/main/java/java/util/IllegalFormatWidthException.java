package java.util;

public class IllegalFormatWidthException extends IllegalFormatException {
    private static final long serialVersionUID = 16660902;
    private int w;

    public IllegalFormatWidthException(int w2) {
        this.w = w2;
    }

    public int getWidth() {
        return this.w;
    }

    public String getMessage() {
        return Integer.toString(this.w);
    }
}
