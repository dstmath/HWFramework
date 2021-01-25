package ohos.global.icu.text;

/* access modifiers changed from: package-private */
public abstract class CharsetRecognizer {
    public String getLanguage() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public abstract String getName();

    /* access modifiers changed from: package-private */
    public abstract CharsetMatch match(CharsetDetector charsetDetector);

    CharsetRecognizer() {
    }
}
