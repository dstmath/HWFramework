package android.graphics;

public class PaintFlagsDrawFilter extends DrawFilter {
    private static native long nativeConstructor(int i, int i2);

    public PaintFlagsDrawFilter(int clearBits, int setBits) {
        this.mNativeInt = nativeConstructor(clearBits, setBits);
    }
}
