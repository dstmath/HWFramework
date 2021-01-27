package ohos.agp.render;

import ohos.agp.render.Shader;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;

public class GroupShader extends Shader {
    private Shader mDstShader;
    private int mPorterDuffMode;
    private Shader mSrcShader;

    private native long nativeGetGroupShaderHandle(long j, long j2, long j3, int i);

    public GroupShader(Shader shader, Shader shader2, BlendMode blendMode) {
        this(shader, shader2, blendMode.value());
    }

    private GroupShader(Shader shader, Shader shader2, int i) {
        super(DEFAULT_COLORS, Shader.TileMode.CLAMP_TILEMODE);
        if (shader == null || shader2 == null) {
            throw new IllegalArgumentException("Shader cannot be null");
        }
        this.mDstShader = shader;
        this.mSrcShader = shader2;
        this.mPorterDuffMode = i;
        long nativeHandle = this.mMatrix != null ? this.mMatrix.getNativeHandle() : 0;
        if (this.mNativeShaderHandle == 0) {
            this.mNativeShaderHandle = nativeGetGroupShaderHandle(nativeHandle, this.mDstShader.getNativeHandle(), this.mSrcShader.getNativeHandle(), this.mPorterDuffMode);
        }
        MemoryCleanerRegistry.getInstance().register(this, new GroupShaderCleaner(this.mNativeShaderHandle));
    }

    protected static class GroupShaderCleaner extends NativeMemoryCleanerHelper {
        private native void nativeGroupShaderRelease(long j);

        public GroupShaderCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativeGroupShaderRelease(j);
            }
        }
    }
}
