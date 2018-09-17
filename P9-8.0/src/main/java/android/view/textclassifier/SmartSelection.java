package android.view.textclassifier;

final class SmartSelection {
    static final int HINT_FLAG_EMAIL = 2;
    static final int HINT_FLAG_URL = 1;
    private final long mCtx;

    static final class ClassificationResult {
        final String mCollection;
        final float mScore;

        ClassificationResult(String collection, float score) {
            this.mCollection = collection;
            this.mScore = score;
        }
    }

    private static native ClassificationResult[] nativeClassifyText(long j, String str, int i, int i2, int i3);

    private static native void nativeClose(long j);

    private static native String nativeGetLanguage(int i);

    private static native int nativeGetVersion(int i);

    private static native long nativeNew(int i);

    private static native int[] nativeSuggest(long j, String str, int i, int i2);

    static {
        System.loadLibrary("textclassifier");
    }

    SmartSelection(int fd) {
        this.mCtx = nativeNew(fd);
    }

    public int[] suggest(String context, int selectionBegin, int selectionEnd) {
        return nativeSuggest(this.mCtx, context, selectionBegin, selectionEnd);
    }

    public ClassificationResult[] classifyText(String context, int selectionBegin, int selectionEnd, int hintFlags) {
        return nativeClassifyText(this.mCtx, context, selectionBegin, selectionEnd, hintFlags);
    }

    public void close() {
        nativeClose(this.mCtx);
    }

    public static String getLanguage(int fd) {
        return nativeGetLanguage(fd);
    }

    public static int getVersion(int fd) {
        return nativeGetVersion(fd);
    }
}
