package android.graphics;

import android.graphics.PorterDuff.Mode;

public class ComposeShader extends Shader {
    private long mNativeInstanceShaderA;
    private long mNativeInstanceShaderB;
    private int mPorterDuffMode;
    Shader mShaderA;
    Shader mShaderB;

    private static native long nativeCreate(long j, long j2, long j3, int i);

    public ComposeShader(Shader shaderA, Shader shaderB, Xfermode mode) {
        this(shaderA, shaderB, mode.porterDuffMode);
    }

    public ComposeShader(Shader shaderA, Shader shaderB, Mode mode) {
        this(shaderA, shaderB, mode.nativeInt);
    }

    private ComposeShader(Shader shaderA, Shader shaderB, int nativeMode) {
        if (shaderA == null || shaderB == null) {
            throw new IllegalArgumentException("Shader parameters must not be null");
        }
        this.mShaderA = shaderA;
        this.mShaderB = shaderB;
        this.mPorterDuffMode = nativeMode;
    }

    long createNativeInstance(long nativeMatrix) {
        this.mNativeInstanceShaderA = this.mShaderA.getNativeInstance();
        this.mNativeInstanceShaderB = this.mShaderB.getNativeInstance();
        return nativeCreate(nativeMatrix, this.mShaderA.getNativeInstance(), this.mShaderB.getNativeInstance(), this.mPorterDuffMode);
    }

    protected void verifyNativeInstance() {
        if (this.mShaderA.getNativeInstance() != this.mNativeInstanceShaderA || this.mShaderB.getNativeInstance() != this.mNativeInstanceShaderB) {
            discardNativeInstance();
        }
    }

    protected Shader copy() {
        ComposeShader copy = new ComposeShader(this.mShaderA.copy(), this.mShaderB.copy(), this.mPorterDuffMode);
        copyLocalMatrix(copy);
        return copy;
    }
}
