package android.graphics;

import android.graphics.PorterDuff.Mode;

public class ComposeShader extends Shader {
    private static final int TYPE_PORTERDUFFMODE = 2;
    private static final int TYPE_XFERMODE = 1;
    private Mode mPorterDuffMode;
    private final Shader mShaderA;
    private final Shader mShaderB;
    private int mType;
    private Xfermode mXferMode;

    private static native long nativeCreate1(long j, long j2, long j3);

    private static native long nativeCreate2(long j, long j2, int i);

    public ComposeShader(Shader shaderA, Shader shaderB, Xfermode mode) {
        this.mType = TYPE_XFERMODE;
        this.mShaderA = shaderA;
        this.mShaderB = shaderB;
        this.mXferMode = mode;
        init(nativeCreate1(shaderA.getNativeInstance(), shaderB.getNativeInstance(), mode != null ? mode.native_instance : 0));
    }

    public ComposeShader(Shader shaderA, Shader shaderB, Mode mode) {
        this.mType = TYPE_PORTERDUFFMODE;
        this.mShaderA = shaderA;
        this.mShaderB = shaderB;
        this.mPorterDuffMode = mode;
        init(nativeCreate2(shaderA.getNativeInstance(), shaderB.getNativeInstance(), mode.nativeInt));
    }

    protected Shader copy() {
        ComposeShader copy;
        switch (this.mType) {
            case TYPE_XFERMODE /*1*/:
                copy = new ComposeShader(this.mShaderA.copy(), this.mShaderB.copy(), this.mXferMode);
                break;
            case TYPE_PORTERDUFFMODE /*2*/:
                copy = new ComposeShader(this.mShaderA.copy(), this.mShaderB.copy(), this.mPorterDuffMode);
                break;
            default:
                throw new IllegalArgumentException("ComposeShader should be created with either Xfermode or PorterDuffMode");
        }
        copyLocalMatrix(copy);
        return copy;
    }
}
