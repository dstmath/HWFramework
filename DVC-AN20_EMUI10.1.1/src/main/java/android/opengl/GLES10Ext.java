package android.opengl;

import java.nio.IntBuffer;

public class GLES10Ext {
    private static native void _nativeClassInit();

    public static native int glQueryMatrixxOES(IntBuffer intBuffer, IntBuffer intBuffer2);

    public static native int glQueryMatrixxOES(int[] iArr, int i, int[] iArr2, int i2);

    static {
        _nativeClassInit();
    }
}
