package ohos.global.icu.impl.locale;

public class LocaleSyntaxException extends Exception {
    private static final long serialVersionUID = 1;
    private int _index;

    public LocaleSyntaxException(String str) {
        this(str, 0);
    }

    public LocaleSyntaxException(String str, int i) {
        super(str);
        this._index = -1;
        this._index = i;
    }

    public int getErrorIndex() {
        return this._index;
    }
}
