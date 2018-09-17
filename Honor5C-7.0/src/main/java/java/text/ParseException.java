package java.text;

public class ParseException extends Exception {
    private int errorOffset;

    public ParseException(String s, int errorOffset) {
        super(s);
        this.errorOffset = errorOffset;
    }

    public int getErrorOffset() {
        return this.errorOffset;
    }
}
