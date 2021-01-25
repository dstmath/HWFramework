package ohos.agp.render;

import ohos.agp.render.Shader;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;

public class PixelMapShader extends Shader {
    private PixelMapHolder mPixelMapHolder;
    private int mTileModeX;
    private int mTileModeY;

    private native long nativeGetPixelMapShaderHandle(long j, PixelMapHolder pixelMapHolder, int i, int i2);

    public PixelMapShader() {
        this(null, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    }

    public PixelMapShader(PixelMapHolder pixelMapHolder, Shader.TileMode tileMode, Shader.TileMode tileMode2) {
        super(DEFAULT_COLORS, tileMode);
        this.mTileModeX = -1;
        this.mTileModeY = -1;
        if (pixelMapHolder != null) {
            this.mPixelMapHolder = pixelMapHolder;
            this.mTileModeX = tileMode.value();
            this.mTileModeY = tileMode2.value();
            long nativeHandle = this.mMatrix != null ? this.mMatrix.getNativeHandle() : 0;
            if (this.mNativeShaderHandle == 0) {
                this.mNativeShaderHandle = nativeGetPixelMapShaderHandle(nativeHandle, this.mPixelMapHolder, this.mTileModeX, this.mTileModeY);
            }
            MemoryCleanerRegistry.getInstance().register(this, new PixelMapShaderCleaner(this.mNativeShaderHandle));
        }
    }

    protected static class PixelMapShaderCleaner extends NativeMemoryCleanerHelper {
        private native void nativePixelMapShaderRelease(long j);

        public PixelMapShaderCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativePixelMapShaderRelease(j);
            }
        }
    }
}
