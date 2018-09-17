package java.util;

public class IllegalFormatWidthException extends IllegalFormatException {
    private static final long serialVersionUID = 16660902;
    private int w;

    public IllegalFormatWidthException(int w) {
        this.w = w;
    }

    public int getWidth() {
        return this.w;
    }

    public String getMessage() {
        return Integer.toString(this.w);
    }
}
