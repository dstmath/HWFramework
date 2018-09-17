package android.graphics;

@Deprecated
public class LayerRasterizer extends Rasterizer {
    private static native void nativeAddLayer(long j, long j2, float f, float f2);

    private static native long nativeConstructor();

    public LayerRasterizer() {
        this.native_instance = nativeConstructor();
    }

    public void addLayer(Paint paint, float dx, float dy) {
        nativeAddLayer(this.native_instance, paint.getNativeInstance(), dx, dy);
    }

    public void addLayer(Paint paint) {
        nativeAddLayer(this.native_instance, paint.getNativeInstance(), 0.0f, 0.0f);
    }
}
