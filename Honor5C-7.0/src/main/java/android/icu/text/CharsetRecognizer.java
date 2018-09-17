package android.icu.text;

abstract class CharsetRecognizer {
    abstract String getName();

    abstract CharsetMatch match(CharsetDetector charsetDetector);

    CharsetRecognizer() {
    }

    public String getLanguage() {
        return null;
    }
}
