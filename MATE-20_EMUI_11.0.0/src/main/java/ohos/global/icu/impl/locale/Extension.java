package ohos.global.icu.impl.locale;

public class Extension {
    private char _key;
    protected String _value;

    protected Extension(char c) {
        this._key = c;
    }

    Extension(char c, String str) {
        this._key = c;
        this._value = str;
    }

    public char getKey() {
        return this._key;
    }

    public String getValue() {
        return this._value;
    }

    public String getID() {
        return this._key + LanguageTag.SEP + this._value;
    }

    public String toString() {
        return getID();
    }
}
