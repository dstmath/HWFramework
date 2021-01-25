package ohos.agp.render.opengl.adapter;

import android.opengl.GLES30;
import ohos.annotation.SystemApi;

@SystemApi
public class GLES30Adapter {
    private static final int INITIAL_OFFSET_VALUE = 0;

    public static void glBindFramebuffer(int i, int i2) {
        GLES30.glBindFramebuffer(i, i2);
    }

    public static void glBindRenderbuffer(int i, int i2) {
        GLES30.glBindRenderbuffer(i, i2);
    }

    public static void glRenderbufferStorageMultisample(int i, int i2, int i3, int i4, int i5) {
        GLES30.glRenderbufferStorageMultisample(i, i2, i3, i4, i5);
    }

    public static void glBlitFramebuffer(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10) {
        GLES30.glBlitFramebuffer(i, i2, i3, i4, i5, i6, i7, i8, i9, i10);
    }

    public static void glDrawBuffers(int i, int[] iArr) {
        GLES30.glDrawBuffers(i, iArr, 0);
    }

    public static void glGenVertexArrays(int i, int[] iArr) {
        GLES30.glGenVertexArrays(i, iArr, 0);
    }

    public static void glBindVertexArray(int i) {
        GLES30.glBindVertexArray(i);
    }
}
