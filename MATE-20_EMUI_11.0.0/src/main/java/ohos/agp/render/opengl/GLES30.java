package ohos.agp.render.opengl;

import ohos.agp.render.opengl.adapter.GLES30Adapter;

public class GLES30 extends GLES20 {
    public static final int GL_DEPTH24_STENCIL8 = 35056;
    public static final int GL_DEPTH_STENCIL_ATTACHMENT = 33306;
    public static final int GL_DRAW_FRAMEBUFFER = 36009;
    public static final int GL_READ_FRAMEBUFFER = 36008;
    public static final int GL_RGBA8 = 32856;

    public static void glBindFramebuffer(int i, int i2) {
        GLES30Adapter.glBindFramebuffer(i, i2);
    }

    public static void glBindRenderbuffer(int i, int i2) {
        GLES30Adapter.glBindRenderbuffer(i, i2);
    }

    public static void glRenderbufferStorageMultisample(int i, int i2, int i3, int i4, int i5) {
        GLES30Adapter.glRenderbufferStorageMultisample(i, i2, i3, i4, i5);
    }

    public static void glBlitFramebuffer(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10) {
        GLES30Adapter.glBlitFramebuffer(i, i2, i3, i4, i5, i6, i7, i8, i9, i10);
    }

    public static void glDrawBuffers(int i, int[] iArr) {
        GLES30Adapter.glDrawBuffers(i, iArr);
    }

    public static void glGenVertexArrays(int i, int[] iArr) {
        GLES30Adapter.glGenVertexArrays(i, iArr);
    }

    public static void glBindVertexArray(int i) {
        GLES30Adapter.glBindVertexArray(i);
    }
}
