package ohos.agp.render.opengl;

import java.nio.Buffer;
import java.nio.IntBuffer;
import ohos.agp.render.opengl.adapter.GLES1XAdapter;

public class GLES1X {
    public static final int GL_ACTIVE_TEXTURE = 34016;
    public static final int GL_ALWAYS = 519;
    public static final int GL_AMBIENT = 4608;
    public static final int GL_AMBIENT_AND_DIFFUSE = 5634;
    public static final int GL_ARRAY_BUFFER = 34962;
    public static final int GL_BACK = 1029;
    public static final int GL_BLEND = 3042;
    public static final int GL_CCW = 2305;
    public static final int GL_CLAMP_TO_EDGE = 33071;
    public static final int GL_COLOR_BUFFER_BIT = 16384;
    public static final int GL_CONSTANT_ATTENUATION = 4615;
    public static final int GL_CULL_FACE = 2884;
    public static final int GL_CW = 2304;
    public static final int GL_DEPTH_BUFFER_BIT = 256;
    public static final int GL_DEPTH_TEST = 2929;
    public static final int GL_DIFFUSE = 4609;
    public static final int GL_EMISSION = 5632;
    public static final int GL_FLOAT = 5126;
    public static final int GL_FOG_COLOR = 2918;
    public static final int GL_FOG_DENSITY = 2914;
    public static final int GL_FOG_END = 2916;
    public static final int GL_FOG_MODE = 2917;
    public static final int GL_FOG_START = 2915;
    public static final int GL_FRONT = 1028;
    public static final int GL_INVALID_VALUE = 1281;
    public static final int GL_LEQUAL = 515;
    public static final int GL_LESS = 513;
    public static final int GL_LIGHT_MODEL_AMBIENT = 2899;
    public static final int GL_LIGHT_MODEL_TWO_SIDE = 2898;
    public static final int GL_LINEAR = 9729;
    public static final int GL_LINEAR_ATTENUATION = 4616;
    public static final int GL_LINEAR_MIPMAP_LINEAR = 9987;
    public static final int GL_LINES = 1;
    public static final int GL_LINE_LOOP = 2;
    public static final int GL_LINE_STRIP = 3;
    public static final int GL_MAX_TEXTURE_SIZE = 3379;
    public static final int GL_NEAREST = 9728;
    public static final int GL_NICEST = 4354;
    public static final int GL_NO_ERROR = 0;
    public static final int GL_ONE = 1;
    public static final int GL_ONE_MINUS_SRC_ALPHA = 771;
    public static final int GL_POINTS = 0;
    public static final int GL_POSITION = 4611;
    public static final int GL_QUADRATIC_ATTENUATION = 4617;
    public static final int GL_RENDERER = 7937;
    public static final int GL_REPEAT = 10497;
    public static final int GL_REPLACE = 7681;
    public static final int GL_RGB = 6407;
    public static final int GL_RGBA = 6408;
    public static final int GL_SHININESS = 5633;
    public static final int GL_SPECULAR = 4610;
    public static final int GL_SPOT_CUTOFF = 4614;
    public static final int GL_SPOT_DIRECTION = 4612;
    public static final int GL_SPOT_EXPONENT = 4613;
    public static final int GL_SRC_ALPHA = 770;
    public static final int GL_STATIC_DRAW = 35044;
    public static final int GL_TEXTURE0 = 33984;
    public static final int GL_TEXTURE1 = 33985;
    public static final int GL_TEXTURE3 = 33987;
    public static final int GL_TEXTURE_2D = 3553;
    public static final int GL_TEXTURE_MAG_FILTER = 10240;
    public static final int GL_TEXTURE_MIN_FILTER = 10241;
    public static final int GL_TEXTURE_WRAP_S = 10242;
    public static final int GL_TEXTURE_WRAP_T = 10243;
    public static final int GL_TRIANGLES = 4;
    public static final int GL_TRIANGLE_FAN = 6;
    public static final int GL_TRIANGLE_STRIP = 5;
    public static final int GL_TRUE = 1;
    public static final int GL_UNSIGNED_BYTE = 5121;
    public static final int GL_UNSIGNED_SHORT = 5123;
    public static final int GL_VENDOR = 7936;
    public static final int GL_VERSION = 7938;
    public static final int GL_ZERO = 0;

    public static void glClear(int i) {
        GLES1XAdapter.glClear(i);
    }

    public static void glClearColorx(int i, int i2, int i3, int i4) {
        GLES1XAdapter.glClearColorx(i, i2, i3, i4);
    }

    public static void glClearDepthx(int i) {
        GLES1XAdapter.glClearDepthx(i);
    }

    public static void glClearStencil(int i) {
        GLES1XAdapter.glClearStencil(i);
    }

    public static void glClientActiveTexture(int i) {
        GLES1XAdapter.glClientActiveTexture(i);
    }

    public static void glColor4x(int i, int i2, int i3, int i4) {
        GLES1XAdapter.glColor4x(i, i2, i3, i4);
    }

    public static void glColorMask(boolean z, boolean z2, boolean z3, boolean z4) {
        GLES1XAdapter.glColorMask(z, z2, z3, z4);
    }

    public static void glColorPointer(int i, int i2, int i3, Buffer buffer) {
        GLES1XAdapter.glColorPointer(i, i2, i3, buffer);
    }

    public static void glCompressedTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, Buffer buffer) {
        GLES1XAdapter.glCompressedTexImage2D(i, i2, i3, i4, i5, i6, i7, buffer);
    }

    public static void glCompressedTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        GLES1XAdapter.glCompressedTexSubImage2D(i, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    public static void glCopyTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        GLES1XAdapter.glCopyTexImage2D(i, i2, i3, i4, i5, i6, i7, i8);
    }

    public static void glCopyTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        GLES1XAdapter.glCopyTexSubImage2D(i, i2, i3, i4, i5, i6, i7, i8);
    }

    public static void glCullFace(int i) {
        GLES1XAdapter.glCullFace(i);
    }

    public static void glDepthFunc(int i) {
        GLES1XAdapter.glDepthFunc(i);
    }

    public static void glDepthMask(boolean z) {
        GLES1XAdapter.glDepthMask(z);
    }

    public static void glDepthRangex(int i, int i2) {
        GLES1XAdapter.glDepthRangex(i, i2);
    }

    public static void glDisable(int i) {
        GLES1XAdapter.glDisable(i);
    }

    public static void glDisableClientState(int i) {
        GLES1XAdapter.glDisableClientState(i);
    }

    public static void glDrawElements(int i, int i2, int i3, Buffer buffer) {
        GLES1XAdapter.glDrawElements(i, i2, i3, buffer);
    }

    public static void glEnable(int i) {
        GLES1XAdapter.glEnable(i);
    }

    public static void glFinish() {
        GLES1XAdapter.glFinish();
    }

    public static void glFlush() {
        GLES1XAdapter.glFlush();
    }

    public static void glFogx(int i, int i2) {
        GLES1XAdapter.glFogx(i, i2);
    }

    public static void glFogxv(int i, IntBuffer intBuffer) {
        GLES1XAdapter.glFogxv(i, intBuffer);
    }

    public static void glFrontFace(int i) {
        GLES1XAdapter.glFrontFace(i);
    }

    public static void glFrustumx(int i, int i2, int i3, int i4, int i5, int i6) {
        GLES1XAdapter.glFrustumx(i, i2, i3, i4, i5, i6);
    }

    public static int glGetError() {
        return GLES1XAdapter.glGetError();
    }

    public static void glGetIntegerv(int i, int[] iArr) {
        GLES1XAdapter.glGetIntegerv(i, iArr);
    }

    public static void glGenTextures(int i, int[] iArr) {
        GLES1XAdapter.glGenTextures(i, iArr);
    }

    public static void glEnableClientState(int i) {
        GLES1XAdapter.glEnableClientState(i);
    }

    public static void glDrawArrays(int i, int i2, int i3) {
        GLES1XAdapter.glDrawArrays(i, i2, i3);
    }

    public static void glDeleteTextures(int i, int[] iArr) {
        GLES1XAdapter.glDeleteTextures(i, iArr);
    }

    public static void glOrthof(float f, float f2, float f3, float f4, float f5, float f6) {
        GLES1XAdapter.glOrthof(f, f2, f3, f4, f5, f6);
    }

    public static void glLightxv(int i, int i2, IntBuffer intBuffer) {
        GLES1XAdapter.glLightxv(i, i2, intBuffer);
    }

    public static void glLineWidthx(int i) {
        GLES1XAdapter.glLineWidthx(i);
    }

    public static void glLoadIdentity() {
        GLES1XAdapter.glLoadIdentity();
    }

    public static void glLoadMatrixx(IntBuffer intBuffer) {
        GLES1XAdapter.glLoadMatrixx(intBuffer);
    }

    public static void glLogicOp(int i) {
        GLES1XAdapter.glLogicOp(i);
    }

    public static void glMaterialx(int i, int i2, int i3) {
        GLES1XAdapter.glMaterialx(i, i2, i3);
    }

    public static void glMaterialxv(int i, int i2, IntBuffer intBuffer) {
        GLES1XAdapter.glMaterialxv(i, i2, intBuffer);
    }

    public static void glMatrixMode(int i) {
        GLES1XAdapter.glMatrixMode(i);
    }

    public static void glMultMatrixx(IntBuffer intBuffer) {
        GLES1XAdapter.glMultMatrixx(intBuffer);
    }

    public static void glMultiTexCoord4x(int i, int i2, int i3, int i4, int i5) {
        GLES1XAdapter.glMultiTexCoord4x(i, i2, i3, i4, i5);
    }

    public static void glNormal3x(int i, int i2, int i3) {
        GLES1XAdapter.glNormal3x(i, i2, i3);
    }

    public static void glNormalPointer(int i, int i2, Buffer buffer) {
        GLES1XAdapter.glNormalPointer(i, i2, buffer);
    }

    public static void glOrthox(int i, int i2, int i3, int i4, int i5, int i6) {
        GLES1XAdapter.glOrthox(i, i2, i3, i4, i5, i6);
    }

    public static void glPixelStorei(int i, int i2) {
        GLES1XAdapter.glPixelStorei(i, i2);
    }

    public static void glPointSizex(int i) {
        GLES1XAdapter.glPointSizex(i);
    }

    public static void glPolygonOffsetx(int i, int i2) {
        GLES1XAdapter.glPolygonOffsetx(i, i2);
    }

    public static void glPopMatrix() {
        GLES1XAdapter.glPopMatrix();
    }

    public static void glPushMatrix() {
        GLES1XAdapter.glPushMatrix();
    }

    public static void glReadPixels(int i, int i2, int i3, int i4, int i5, int i6, Buffer buffer) {
        GLES1XAdapter.glReadPixels(i, i2, i3, i4, i5, i6, buffer);
    }

    public static void glRotatex(int i, int i2, int i3, int i4) {
        GLES1XAdapter.glRotatex(i, i2, i3, i4);
    }

    public static void glSampleCoverage(float f, boolean z) {
        GLES1XAdapter.glSampleCoverage(f, z);
    }

    public static void glSampleCoveragex(int i, boolean z) {
        GLES1XAdapter.glSampleCoveragex(i, z);
    }

    public static void glScalex(int i, int i2, int i3) {
        GLES1XAdapter.glScalex(i, i2, i3);
    }

    public static void glScissor(int i, int i2, int i3, int i4) {
        GLES1XAdapter.glScissor(i, i2, i3, i4);
    }

    public static void glShadeModel(int i) {
        GLES1XAdapter.glShadeModel(i);
    }

    public static void glStencilFunc(int i, int i2, int i3) {
        GLES1XAdapter.glStencilFunc(i, i2, i3);
    }

    public static void glStencilMask(int i) {
        GLES1XAdapter.glStencilMask(i);
    }

    public static void glStencilOp(int i, int i2, int i3) {
        GLES1XAdapter.glStencilOp(i, i2, i3);
    }

    public static void glTexCoordPointer(int i, int i2, int i3, Buffer buffer) {
        GLES1XAdapter.glTexCoordPointer(i, i2, i3, buffer);
    }

    public static void glTexEnvx(int i, int i2, int i3) {
        GLES1XAdapter.glTexEnvx(i, i2, i3);
    }

    public static void glTexEnvxv(int i, int i2, IntBuffer intBuffer) {
        GLES1XAdapter.glTexEnvxv(i, i2, intBuffer);
    }

    public static void glTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        GLES1XAdapter.glTexImage2D(i, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    public static void glTexParameterx(int i, int i2, int i3) {
        GLES1XAdapter.glTexParameterx(i, i2, i3);
    }

    public static void glTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        GLES1XAdapter.glTexSubImage2D(i, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    public static void glTranslatex(int i, int i2, int i3) {
        GLES1XAdapter.glTranslatex(i, i2, i3);
    }

    public static void glVertexPointer(int i, int i2, int i3, Buffer buffer) {
        GLES1XAdapter.glVertexPointer(i, i2, i3, buffer);
    }

    public static void glViewport(int i, int i2, int i3, int i4) {
        GLES1XAdapter.glViewport(i, i2, i3, i4);
    }

    public static String glGetString(int i) {
        return GLES1XAdapter.glGetString(i);
    }

    public static void glGetTexEnvxv(int i, int i2, IntBuffer intBuffer) {
        GLES1XAdapter.glGetTexEnvxv(i, i2, intBuffer);
    }

    public static void glGetTexParameteriv(int i, int i2, IntBuffer intBuffer) {
        GLES1XAdapter.glGetTexParameteriv(i, i2, intBuffer);
    }

    public static void glGetTexParameterxv(int i, int i2, IntBuffer intBuffer) {
        GLES1XAdapter.glGetTexParameterxv(i, i2, intBuffer);
    }

    public static void glHint(int i, int i2) {
        GLES1XAdapter.glHint(i, i2);
    }

    public static boolean glIsBuffer(int i) {
        return GLES1XAdapter.glIsBuffer(i);
    }

    public static boolean glIsEnabled(int i) {
        return GLES1XAdapter.glIsEnabled(i);
    }

    public static boolean glIsTexture(int i) {
        return GLES1XAdapter.glIsTexture(i);
    }

    public static void glLightModelx(int i, int i2) {
        GLES1XAdapter.glLightModelx(i, i2);
    }

    public static void glLightModelxv(int i, IntBuffer intBuffer) {
        GLES1XAdapter.glLightModelxv(i, intBuffer);
    }

    public static void glLightx(int i, int i2, int i3) {
        GLES1XAdapter.glLightx(i, i2, i3);
    }

    public static void glDepthRangef(float f, float f2) {
        GLES1XAdapter.glDepthRangef(f, f2);
    }

    public static void glFogf(int i, float f) {
        GLES1XAdapter.glFogf(i, f);
    }

    public static void glFogfv(int i, float[] fArr) {
        GLES1XAdapter.glFogfv(i, fArr);
    }

    public static void glFrustumf(float f, float f2, float f3, float f4, float f5, float f6) {
        GLES1XAdapter.glFrustumf(f, f2, f3, f4, f5, f6);
    }

    public static void glLightModelf(int i, float f) {
        GLES1XAdapter.glLightModelf(i, f);
    }

    public static void glLightModelfv(int i, float[] fArr) {
        GLES1XAdapter.glLightModelfv(i, fArr);
    }

    public static void glLightf(int i, int i2, float f) {
        GLES1XAdapter.glLightf(i, i2, f);
    }

    public static void glLightfv(int i, int i2, float[] fArr) {
        GLES1XAdapter.glLightfv(i, i2, fArr);
    }

    public static void glLineWidth(float f) {
        GLES1XAdapter.glLineWidth(f);
    }

    public static void glLoadMatrixf(float[] fArr) {
        GLES1XAdapter.glLoadMatrixf(fArr);
    }

    public static void glMaterialf(int i, int i2, float f) {
        GLES1XAdapter.glMaterialf(i, i2, f);
    }

    public static void glMaterialfv(int i, int i2, float[] fArr) {
        GLES1XAdapter.glMaterialfv(i, i2, fArr);
    }

    public static void glMultMatrixf(float[] fArr) {
        GLES1XAdapter.glMultMatrixf(fArr);
    }

    public static void glMultiTexCoord4f(int i, float f, float f2, float f3, float f4) {
        GLES1XAdapter.glMultiTexCoord4f(i, f, f2, f3, f4);
    }

    public static void glNormal3f(float f, float f2, float f3) {
        GLES1XAdapter.glNormal3f(f, f2, f3);
    }

    public static void glPointSize(float f) {
        GLES1XAdapter.glPointSize(f);
    }

    public static void glPolygonOffset(float f, float f2) {
        GLES1XAdapter.glPolygonOffset(f, f2);
    }

    public static void glRotatef(float f, float f2, float f3, float f4) {
        GLES1XAdapter.glRotatef(f, f2, f3, f4);
    }

    public static void glScalef(float f, float f2, float f3) {
        GLES1XAdapter.glScalef(f, f2, f3);
    }

    public static void glTexEnvf(int i, int i2, float f) {
        GLES1XAdapter.glTexEnvf(i, i2, f);
    }

    public static void glTexEnvfv(int i, int i2, float[] fArr) {
        GLES1XAdapter.glTexEnvfv(i, i2, fArr);
    }

    public static void glTexParameterf(int i, int i2, float f) {
        GLES1XAdapter.glTexParameterf(i, i2, f);
    }

    public static void glTranslatef(float f, float f2, float f3) {
        GLES1XAdapter.glTranslatef(f, f2, f3);
    }

    public static void glActiveTexture(int i) {
        GLES1XAdapter.glActiveTexture(i);
    }

    public static void glAlphaFuncx(int i, int i2) {
        GLES1XAdapter.glAlphaFuncx(i, i2);
    }

    public static void glBindTexture(int i, int i2) {
        GLES1XAdapter.glBindTexture(i, i2);
    }

    public static void glBlendFunc(int i, int i2) {
        GLES1XAdapter.glBlendFunc(i, i2);
    }

    public static void glAlphaFunc(int i, float f) {
        GLES1XAdapter.glAlphaFunc(i, f);
    }

    public static void glClearColor(float f, float f2, float f3, float f4) {
        GLES1XAdapter.glClearColor(f, f2, f3, f4);
    }

    public static void glClearDepthf(float f) {
        GLES1XAdapter.glClearDepthf(f);
    }

    public static void glClipPlanef(int i, float[] fArr) {
        GLES1XAdapter.glClipPlanef(i, fArr);
    }

    public static void glColor4f(float f, float f2, float f3, float f4) {
        GLES1XAdapter.glClearColor(f, f2, f3, f4);
    }
}
