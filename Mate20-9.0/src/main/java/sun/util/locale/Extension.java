package sun.util.locale;

class Extension {
    private String id;
    private final char key;
    private String value;

    protected Extension(char key2) {
        this.key = key2;
    }

    Extension(char key2, String value2) {
        this.key = key2;
        setValue(value2);
    }

    /* access modifiers changed from: protected */
    public void setValue(String value2) {
        this.value = value2;
        this.id = this.key + LanguageTag.SEP + value2;
    }

    public char getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public String getID() {
        return this.id;
    }

    public String toString() {
        return getID();
    }
}
