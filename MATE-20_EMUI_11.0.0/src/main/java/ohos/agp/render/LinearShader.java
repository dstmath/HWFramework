package ohos.agp.render;

import ohos.agp.render.Shader;
import ohos.agp.utils.Color;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;
import ohos.agp.utils.Point;

public class LinearShader extends Shader {
    private static final Point[] DEFAULT_POINTS = {new Point(0.0f, 0.0f), new Point(1.0f, 1.0f)};
    public Point[] points;
    public float[] stops;

    private native long nativeGetLinearShaderHandle(long j, Point[] pointArr, float[] fArr, int[] iArr, int i);

    public LinearShader() {
        this(DEFAULT_POINTS, null, DEFAULT_COLORS, Shader.TileMode.CLAMP);
    }

    public LinearShader(Point[] pointArr, float[] fArr, Color[] colorArr, Shader.TileMode tileMode) {
        super(colorArr, tileMode);
        if (pointArr != null && pointArr.length == 2) {
            if (fArr != null) {
                this.stops = (float[]) fArr.clone();
            }
            long nativeHandle = this.mMatrix != null ? this.mMatrix.getNativeHandle() : 0;
            this.points = (Point[]) pointArr.clone();
            if (this.mNativeShaderHandle == 0) {
                this.mNativeShaderHandle = nativeGetLinearShaderHandle(nativeHandle, this.points, this.stops, this.intColors, this.mTileMode);
            }
            MemoryCleanerRegistry.getInstance().register(this, new LinearShaderCleaner(this.mNativeShaderHandle));
        }
    }

    protected static class LinearShaderCleaner extends NativeMemoryCleanerHelper {
        private native void nativeLinearShaderRelease(long j);

        public LinearShaderCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativeLinearShaderRelease(j);
            }
        }
    }

    /* access modifiers changed from: protected */
    public Point[] getShaderPoints() {
        return (Point[]) this.points.clone();
    }
}
