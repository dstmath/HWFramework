package android.graphics;

public class EmbossMaskFilter extends MaskFilter {
    private static native long nativeConstructor(float[] fArr, float f, float f2, float f3);

    @Deprecated
    public EmbossMaskFilter(float[] direction, float ambient, float specular, float blurRadius) {
        if (direction.length >= 3) {
            this.native_instance = nativeConstructor(direction, ambient, specular, blurRadius);
            return;
        }
        throw new ArrayIndexOutOfBoundsException();
    }
}
