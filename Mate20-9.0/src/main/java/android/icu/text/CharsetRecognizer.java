package android.icu.text;

abstract class CharsetRecognizer {
    /* access modifiers changed from: package-private */
    public abstract String getName();

    /* access modifiers changed from: package-private */
    public abstract CharsetMatch match(CharsetDetector charsetDetector);

    CharsetRecognizer() {
    }

    public String getLanguage() {
        return null;
    }
}
