package java.lang;

public class NumberFormatException extends IllegalArgumentException {
    static final long serialVersionUID = -2848938806368998894L;

    public NumberFormatException(String s) {
        super(s);
    }

    static NumberFormatException forInputString(String s) {
        return new NumberFormatException("For input string: \"" + s + "\"");
    }
}
