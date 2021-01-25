package ohos.agp.render;

import ohos.agp.render.Shader;
import ohos.agp.utils.Color;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;
import ohos.agp.utils.Point;

public class RadialShader extends Shader {
    private static final Point DEFAULT_POINT = new Point(200.0f, 200.0f);
    private static final float DEFAULT_RADIUS = 100.0f;
    private Point point;
    private float radius;
    public float[] stops;

    private native long nativeGetRadialShaderHandle(long j, Point point2, float f, float[] fArr, int[] iArr, int i);

    public RadialShader() {
        this(DEFAULT_POINT, DEFAULT_RADIUS, null, DEFAULT_COLORS, Shader.TileMode.CLAMP);
    }

    public RadialShader(Point point2, float f, float[] fArr, Color[] colorArr, Shader.TileMode tileMode) {
        super(colorArr, tileMode);
        this.point = point2;
        this.radius = f;
        if (fArr != null) {
            this.stops = (float[]) fArr.clone();
        }
        long nativeHandle = this.mMatrix != null ? this.mMatrix.getNativeHandle() : 0;
        if (this.mNativeShaderHandle == 0) {
            this.mNativeShaderHandle = nativeGetRadialShaderHandle(nativeHandle, this.point, this.radius, this.stops, this.intColors, this.mTileMode);
        }
        MemoryCleanerRegistry.getInstance().register(this, new RadialShaderCleaner(this.mNativeShaderHandle));
    }

    protected static class RadialShaderCleaner extends NativeMemoryCleanerHelper {
        private native void nativeRadialShaderRelease(long j);

        public RadialShaderCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativeRadialShaderRelease(j);
            }
        }
    }

    /* access modifiers changed from: protected */
    public Point getShaderPoint() {
        return this.point;
    }

    /* access modifiers changed from: protected */
    public float getShaderRadius() {
        return this.radius;
    }

    /* access modifiers changed from: protected */
    public float[] getShaderStops() {
        return (float[]) this.stops.clone();
    }
}
