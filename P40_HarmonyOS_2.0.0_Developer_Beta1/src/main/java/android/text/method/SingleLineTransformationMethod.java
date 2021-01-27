package android.text.method;

public class SingleLineTransformationMethod extends ReplacementTransformationMethod {
    private static char[] ORIGINAL = {'\n', '\r'};
    private static char[] REPLACEMENT = {' ', 65279};
    private static SingleLineTransformationMethod sInstance;

    /* access modifiers changed from: protected */
    @Override // android.text.method.ReplacementTransformationMethod
    public char[] getOriginal() {
        return ORIGINAL;
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.ReplacementTransformationMethod
    public char[] getReplacement() {
        return REPLACEMENT;
    }

    public static SingleLineTransformationMethod getInstance() {
        SingleLineTransformationMethod singleLineTransformationMethod = sInstance;
        if (singleLineTransformationMethod != null) {
            return singleLineTransformationMethod;
        }
        sInstance = new SingleLineTransformationMethod();
        return sInstance;
    }
}
