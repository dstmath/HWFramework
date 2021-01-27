package ohos.agp.render.opengl.adapter;

import android.opengl.GLES10;
import android.opengl.GLES11;
import java.nio.Buffer;
import java.nio.IntBuffer;
import ohos.annotation.SystemApi;

@SystemApi
public class GLES1XAdapter {
    private static final int INITIAL_OFFSET_VALUE = 0;

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

    public static void glGetIntegerv(int i, int[] iArr) {
        GLES10.glGetIntegerv(i, iArr, 0);
    }

    public static void glGenTextures(int i, int[] iArr) {
        GLES10.glGenTextures(i, iArr, 0);
    }

    public static void glEnableClientState(int i) {
        GLES10.glEnableClientState(i);
    }

    public static void glDrawArrays(int i, int i2, int i3) {
        GLES10.glDrawArrays(i, i2, i3);
    }

    public static void glDeleteTextures(int i, int[] iArr) {
        GLES10.glDeleteTextures(i, iArr, 0);
    }

    public static void glOrthof(float f, float f2, float f3, float f4, float f5, float f6) {
        GLES10.glOrthof(f, f2, f3, f4, f5, f6);
    }

    public static void glLightxv(int i, int i2, IntBuffer intBuffer) {
        GLES10.glLightxv(i, i2, intBuffer);
    }

    public static void glLineWidthx(int i) {
        GLES10.glLineWidthx(i);
    }

    public static void glLoadIdentity() {
        GLES10.glLoadIdentity();
    }

    public static void glLoadMatrixx(IntBuffer intBuffer) {
        GLES10.glLoadMatrixx(intBuffer);
    }

    public static void glLogicOp(int i) {
        GLES10.glLogicOp(i);
    }

    public static void glMaterialx(int i, int i2, int i3) {
        GLES10.glMaterialx(i, i2, i3);
    }

    public static void glMaterialxv(int i, int i2, IntBuffer intBuffer) {
        GLES10.glMaterialxv(i, i2, intBuffer);
    }

    public static void glMatrixMode(int i) {
        GLES10.glMatrixMode(i);
    }

    public static void glMultMatrixx(IntBuffer intBuffer) {
        GLES10.glMultMatrixx(intBuffer);
    }

    public static void glMultiTexCoord4x(int i, int i2, int i3, int i4, int i5) {
        GLES10.glMultiTexCoord4x(i, i2, i3, i4, i5);
    }

    public static void glNormal3x(int i, int i2, int i3) {
        GLES10.glNormal3x(i, i2, i3);
    }

    public static void glNormalPointer(int i, int i2, Buffer buffer) {
        GLES10.glNormalPointer(i, i2, buffer);
    }

    public static void glOrthox(int i, int i2, int i3, int i4, int i5, int i6) {
        GLES10.glOrthox(i, i2, i3, i4, i5, i6);
    }

    public static void glPixelStorei(int i, int i2) {
        GLES10.glPixelStorei(i, i2);
    }

    public static void glPointSizex(int i) {
        GLES10.glPointSizex(i);
    }

    public static void glPolygonOffsetx(int i, int i2) {
        GLES10.glPolygonOffsetx(i, i2);
    }

    public static void glPopMatrix() {
        GLES10.glPopMatrix();
    }

    public static void glPushMatrix() {
        GLES10.glPushMatrix();
    }

    public static void glReadPixels(int i, int i2, int i3, int i4, int i5, int i6, Buffer buffer) {
        GLES10.glReadPixels(i, i2, i3, i4, i5, i6, buffer);
    }

    public static void glRotatex(int i, int i2, int i3, int i4) {
        GLES10.glRotatex(i, i2, i3, i4);
    }

    public static void glSampleCoverage(float f, boolean z) {
        GLES10.glSampleCoverage(f, z);
    }

    public static void glSampleCoveragex(int i, boolean z) {
        GLES10.glSampleCoveragex(i, z);
    }

    public static void glScalex(int i, int i2, int i3) {
        GLES10.glScalex(i, i2, i3);
    }

    public static void glScissor(int i, int i2, int i3, int i4) {
        GLES10.glScissor(i, i2, i3, i4);
    }

    public static void glShadeModel(int i) {
        GLES10.glShadeModel(i);
    }

    public static void glStencilFunc(int i, int i2, int i3) {
        GLES10.glStencilFunc(i, i2, i3);
    }

    public static void glStencilMask(int i) {
        GLES10.glStencilMask(i);
    }

    public static void glStencilOp(int i, int i2, int i3) {
        GLES10.glStencilOp(i, i2, i3);
    }

    public static void glTexCoordPointer(int i, int i2, int i3, Buffer buffer) {
        GLES10.glTexCoordPointer(i, i2, i3, buffer);
    }

    public static void glTexEnvx(int i, int i2, int i3) {
        GLES10.glTexEnvx(i, i2, i3);
    }

    public static void glTexEnvxv(int i, int i2, IntBuffer intBuffer) {
        GLES10.glTexEnvxv(i, i2, intBuffer);
    }

    public static void glTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        GLES10.glTexImage2D(i, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    public static void glTexParameterx(int i, int i2, int i3) {
        GLES10.glTexParameterx(i, i2, i3);
    }

    public static void glTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        GLES10.glTexSubImage2D(i, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    public static void glTranslatex(int i, int i2, int i3) {
        GLES10.glTranslatex(i, i2, i3);
    }

    public static void glVertexPointer(int i, int i2, int i3, Buffer buffer) {
        GLES10.glVertexPointer(i, i2, i3, buffer);
    }

    public static void glViewport(int i, int i2, int i3, int i4) {
        GLES10.glViewport(i, i2, i3, i4);
    }

    public static String glGetString(int i) {
        return GLES10.glGetString(i);
    }

    public static void glGetTexEnvxv(int i, int i2, IntBuffer intBuffer) {
        GLES11.glGetTexEnvxv(i, i2, intBuffer);
    }

    public static void glGetTexParameteriv(int i, int i2, IntBuffer intBuffer) {
        GLES11.glGetTexParameteriv(i, i2, intBuffer);
    }

    public static void glGetTexParameterxv(int i, int i2, IntBuffer intBuffer) {
        GLES11.glGetTexParameterxv(i, i2, intBuffer);
    }

    public static void glHint(int i, int i2) {
        GLES10.glHint(i, i2);
    }

    public static boolean glIsBuffer(int i) {
        return GLES11.glIsBuffer(i);
    }

    public static boolean glIsEnabled(int i) {
        return GLES11.glIsEnabled(i);
    }

    public static boolean glIsTexture(int i) {
        return GLES11.glIsTexture(i);
    }

    public static void glLightModelx(int i, int i2) {
        GLES10.glLightModelx(i, i2);
    }

    public static void glLightModelxv(int i, IntBuffer intBuffer) {
        GLES10.glLightModelxv(i, intBuffer);
    }

    public static void glLightx(int i, int i2, int i3) {
        GLES10.glLightx(i, i2, i3);
    }

    public static void glDepthRangef(float f, float f2) {
        GLES10.glDepthRangef(f, f2);
    }

    public static void glFogf(int i, float f) {
        GLES10.glFogf(i, f);
    }

    public static void glFogfv(int i, float[] fArr) {
        GLES10.glFogfv(i, fArr, 0);
    }

    public static void glFrustumf(float f, float f2, float f3, float f4, float f5, float f6) {
        GLES10.glFrustumf(f, f2, f3, f4, f5, f6);
    }

    public static void glLightModelf(int i, float f) {
        GLES10.glLightModelf(i, f);
    }

    public static void glLightModelfv(int i, float[] fArr) {
        GLES10.glLightModelfv(i, fArr, 0);
    }

    public static void glLightf(int i, int i2, float f) {
        GLES10.glLightf(i, i2, f);
    }

    public static void glLightfv(int i, int i2, float[] fArr) {
        GLES10.glLightfv(i, i2, fArr, 0);
    }

    public static void glLineWidth(float f) {
        GLES10.glLineWidth(f);
    }

    public static void glLoadMatrixf(float[] fArr) {
        GLES10.glLoadMatrixf(fArr, 0);
    }

    public static void glMaterialf(int i, int i2, float f) {
        GLES10.glMaterialf(i, i2, f);
    }

    public static void glMaterialfv(int i, int i2, float[] fArr) {
        GLES10.glMaterialfv(i, i2, fArr, 0);
    }

    public static void glMultMatrixf(float[] fArr) {
        GLES10.glMultMatrixf(fArr, 0);
    }

    public static void glMultiTexCoord4f(int i, float f, float f2, float f3, float f4) {
        GLES10.glMultiTexCoord4f(i, f, f2, f3, f4);
    }

    public static void glNormal3f(float f, float f2, float f3) {
        GLES10.glNormal3f(f, f2, f3);
    }

    public static void glPointSize(float f) {
        GLES10.glPointSize(f);
    }

    public static void glPolygonOffset(float f, float f2) {
        GLES10.glPolygonOffset(f, f2);
    }

    public static void glRotatef(float f, float f2, float f3, float f4) {
        GLES10.glRotatef(f, f2, f3, f4);
    }

    public static void glScalef(float f, float f2, float f3) {
        GLES10.glScalef(f, f2, f3);
    }

    public static void glTexEnvf(int i, int i2, float f) {
        GLES10.glTexEnvf(i, i2, f);
    }

    public static void glTexEnvfv(int i, int i2, float[] fArr) {
        GLES10.glTexEnvfv(i, i2, fArr, 0);
    }

    public static void glTexParameterf(int i, int i2, float f) {
        GLES10.glTexParameterf(i, i2, f);
    }

    public static void glTranslatef(float f, float f2, float f3) {
        GLES10.glTranslatef(f, f2, f3);
    }

    public static void glActiveTexture(int i) {
        GLES10.glActiveTexture(i);
    }

    public static void glAlphaFuncx(int i, int i2) {
        GLES10.glAlphaFuncx(i, i2);
    }

    public static void glBindTexture(int i, int i2) {
        GLES10.glBindTexture(i, i2);
    }

    public static void glBlendFunc(int i, int i2) {
        GLES10.glBlendFunc(i, i2);
    }

    public static void glAlphaFunc(int i, float f) {
        GLES10.glAlphaFunc(i, f);
    }

    public static void glClearColor(float f, float f2, float f3, float f4) {
        GLES10.glClearColor(f, f2, f3, f4);
    }

    public static void glClearDepthf(float f) {
        GLES10.glClearDepthf(f);
    }

    public static void glClipPlanef(int i, float[] fArr) {
        GLES11.glClipPlanef(i, fArr, 0);
    }

    public static void glColor4f(float f, float f2, float f3, float f4) {
        GLES10.glClearColor(f, f2, f3, f4);
    }
}
