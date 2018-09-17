package android.icu.impl.locale;

public class Extension {
    private char _key;
    protected String _value;

    protected Extension(char key) {
        this._key = key;
    }

    Extension(char key, String value) {
        this._key = key;
        this._value = value;
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
