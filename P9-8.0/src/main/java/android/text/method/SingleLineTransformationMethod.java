package android.text.method;

public class SingleLineTransformationMethod extends ReplacementTransformationMethod {
    private static char[] ORIGINAL = new char[]{10, 13};
    private static char[] REPLACEMENT = new char[]{' ', 65279};
    private static SingleLineTransformationMethod sInstance;

    protected char[] getOriginal() {
        return ORIGINAL;
    }

    protected char[] getReplacement() {
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
