package android.view.textclassifier;

final class LangId {
    private final long mModelPtr;

    static final class ClassificationResult {
        final String mLanguage;
        final float mScore;

        ClassificationResult(String language, float score) {
            this.mLanguage = language;
            this.mScore = score;
        }
    }

    private static native void nativeClose(long j);

    private static native ClassificationResult[] nativeFindLanguages(long j, String str);

    private static native long nativeNew(int i);

    static {
        System.loadLibrary("textclassifier");
    }

    LangId(int fd) {
        this.mModelPtr = nativeNew(fd);
    }

    public ClassificationResult[] findLanguages(String text) {
        return nativeFindLanguages(this.mModelPtr, text);
    }

    public void close() {
        nativeClose(this.mModelPtr);
    }
}
