package android.graphics;

public class BlurMaskFilter extends MaskFilter {
    private static native long nativeConstructor(float f, int i);

    public enum Blur {
        NORMAL(0),
        SOLID(1),
        OUTER(2),
        INNER(3);
        
        final int native_int;

        private Blur(int value) {
            this.native_int = value;
        }
    }

    public BlurMaskFilter(float radius, Blur style) {
        this.native_instance = nativeConstructor(radius, style.native_int);
    }
}
