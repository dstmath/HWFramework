package ohos.agp.render.opengl.adapter;

import android.opengl.GLES10;
import java.nio.Buffer;
import java.nio.IntBuffer;
import ohos.annotation.SystemApi;

@SystemApi
public class GLES10Adapter {
    public static void glClear(int i) {
        GLES10.glClear(i);
    }

    public static void glClearColorx(int i, int i2, int i3, int i4) {
        GLES10.glClearColorx(i, i2, i3, i4);
    }

    public static void glClearDepthx(int i) {
        GLES10.glClearDepthx(i);
    }

    public static void glClearStencil(int i) {
        GLES10.glClearStencil(i);
    }

    public static void glClientActiveTexture(int i) {
        GLES10.glClientActiveTexture(i);
    }

    public static void glColor4x(int i, int i2, int i3, int i4) {
        GLES10.glColor4x(i, i2, i3, i4);
    }

    public static void glColorMask(boolean z, boolean z2, boolean z3, boolean z4) {
        GLES10.glColorMask(z, z2, z3, z4);
    }

    public static void glColorPointer(int i, int i2, int i3, Buffer buffer) {
        GLES10.glColorPointer(i, i2, i3, buffer);
    }

    public static void glCompressedTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, Buffer buffer) {
        GLES10.glCompressedTexImage2D(i, i2, i3, i4, i5, i6, i7, buffer);
    }

    public static void glCompressedTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        GLES10.glCompressedTexSubImage2D(i, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    public static void glCopyTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        GLES10.glCopyTexImage2D(i, i2, i3, i4, i5, i6, i7, i8);
    }

    public static void glCopyTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        GLES10.glCopyTexSubImage2D(i, i2, i3, i4, i5, i6, i7, i8);
    }

    public static void glCullFace(int i) {
        GLES10.glCullFace(i);
    }

    public static void glDepthFunc(int i) {
        GLES10.glDepthFunc(i);
    }

    public static void glDepthMask(boolean z) {
        GLES10.glDepthMask(z);
    }

    public static void glDepthRangex(int i, int i2) {
        GLES10.glDepthRangex(i, i2);
    }

    public static void glDisable(int i) {
        GLES10.glDisable(i);
    }

    public static void glDisableClientState(int i) {
        GLES10.glDisableClientState(i);
    }

    public static void glDrawElements(int i, int i2, int i3, Buffer buffer) {
        GLES10.glDrawElements(i, i2, i3, buffer);
    }

    public static void glEnable(int i) {
        GLES10.glEnable(i);
    }

    public static void glFinish() {
        GLES10.glFinish();
    }

    public static void glFlush() {
        GLES10.glFlush();
    }

    public static void glFogx(int i, int i2) {
        GLES10.glFogx(i, i2);
    }

    public static void glFogxv(int i, IntBuffer intBuffer) {
        GLES10.glFogxv(i, intBuffer);
    }

    public static void glFrontFace(int i) {
        GLES10.glFrontFace(i);
    }

    public static void glFrustumx(int i, int i2, int i3, int i4, int i5, int i6) {
        GLES10.glFrustumx(i, i2, i3, i4, i5, i6);
    }

    public static int glGetError() {
        return GLES10.glGetError();
    }
}
