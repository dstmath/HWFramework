package android.text.method;

import android.annotation.UnsupportedAppUsage;

public class HideReturnsTransformationMethod extends ReplacementTransformationMethod {
    private static char[] ORIGINAL = {'\r'};
    private static char[] REPLACEMENT = {65279};
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private static HideReturnsTransformationMethod sInstance;

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

    public static HideReturnsTransformationMethod getInstance() {
        HideReturnsTransformationMethod hideReturnsTransformationMethod = sInstance;
        if (hideReturnsTransformationMethod != null) {
            return hideReturnsTransformationMethod;
        }
        sInstance = new HideReturnsTransformationMethod();
        return sInstance;
    }
}
