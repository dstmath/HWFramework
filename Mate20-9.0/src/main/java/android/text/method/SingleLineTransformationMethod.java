package android.text.method;

public class SingleLineTransformationMethod extends ReplacementTransformationMethod {
    private static char[] ORIGINAL = {10, 13};
    private static char[] REPLACEMENT = {' ', 65279};
    private static SingleLineTransformationMethod sInstance;

    /* access modifiers changed from: protected */
    public char[] getOriginal() {
        return ORIGINAL;
    }

    /* access modifiers changed from: protected */
    public char[] getReplacement() {
        return REPLACEMENT;
    }

    public static SingleLineTransformationMethod getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        sInstance = new SingleLineTransformationMethod();
        return sInstance;
    }
}
