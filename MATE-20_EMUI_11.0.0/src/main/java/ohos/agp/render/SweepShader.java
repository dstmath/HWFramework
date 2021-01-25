package ohos.agp.render;

import ohos.agp.render.Shader;
import ohos.agp.utils.Color;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;

public class SweepShader extends Shader {
    private static final Shader.TileMode DEFAULT_MODE = Shader.TileMode.CLAMP;
    private static final float DEFAULT_XCOOR = 200.0f;
    private static final float DEFAULT_YCOOR = 200.0f;
    public float[] positions;
    public float xCoordinate;
    public float yCoordinate;

    private native long nativeGetSweepShaderHandle(long j, float f, float f2, int[] iArr, float[] fArr);

    public SweepShader() {
        this(200.0f, 200.0f, DEFAULT_COLORS, null);
    }

    public SweepShader(float f, float f2, Color[] colorArr, float[] fArr) {
        super(colorArr, DEFAULT_MODE);
        this.xCoordinate = f;
        this.yCoordinate = f2;
        if (fArr != null) {
            this.positions = (float[]) fArr.clone();
        }
        long nativeHandle = this.mMatrix != null ? this.mMatrix.getNativeHandle() : 0;
        if (this.mNativeShaderHandle == 0) {
            this.mNativeShaderHandle = nativeGetSweepShaderHandle(nativeHandle, this.xCoordinate, this.yCoordinate, this.intColors, this.positions);
        }
        MemoryCleanerRegistry.getInstance().register(this, new SweepShaderCleaner(this.mNativeShaderHandle));
    }

    protected static class SweepShaderCleaner extends NativeMemoryCleanerHelper {
        private native void nativeSweepShaderRelease(long j);

        public SweepShaderCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativeSweepShaderRelease(j);
            }
        }
    }

    /* access modifiers changed from: protected */
    public float getShaderXCoordinate() {
        return this.xCoordinate;
    }

    /* access modifiers changed from: protected */
    public float getShaderYCoordinate() {
        return this.yCoordinate;
    }

    /* access modifiers changed from: protected */
    public float[] getShaderPositions() {
        return (float[]) this.positions.clone();
    }
}
