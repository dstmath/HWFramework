package android.icu.impl.locale;

public class LocaleSyntaxException extends Exception {
    private static final long serialVersionUID = 1;
    private int _index;

    public LocaleSyntaxException(String msg) {
        this(msg, 0);
    }

    public LocaleSyntaxException(String msg, int errorIndex) {
        super(msg);
        this._index = -1;
        this._index = errorIndex;
    }

    public int getErrorIndex() {
        return this._index;
    }
}
