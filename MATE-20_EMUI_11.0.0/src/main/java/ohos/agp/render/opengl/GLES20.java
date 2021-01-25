package ohos.agp.render.opengl;

import java.nio.Buffer;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import ohos.agp.render.opengl.adapter.GLES20Adapter;

public class GLES20 {
    public static final int GL_ACTIVE_TEXTURE = 34016;
    public static final int GL_ALWAYS = 519;
    public static final int GL_ARRAY_BUFFER = 34962;
    public static final int GL_BACK = 1029;
    public static final int GL_BLEND = 3042;
    public static final int GL_CCW = 2305;
    public static final int GL_CLAMP_TO_EDGE = 33071;
    public static final int GL_COLOR_ATTACHMENT0 = 36064;
    public static final int GL_COLOR_BUFFER_BIT = 16384;
    public static final int GL_COMPILE_STATUS = 35713;
    public static final int GL_CULL_FACE = 2884;
    public static final int GL_CW = 2304;
    public static final int GL_DELETE_STATUS = 35712;
    public static final int GL_DEPTH_ATTACHMENT = 36096;
    public static final int GL_DEPTH_BUFFER_BIT = 256;
    public static final int GL_DEPTH_COMPONENT16 = 33189;
    public static final int GL_DEPTH_TEST = 2929;
    public static final int GL_FLOAT = 5126;
    public static final int GL_FRAGMENT_SHADER = 35632;
    public static final int GL_FRAMEBUFFER = 36160;
    public static final int GL_FRAMEBUFFER_COMPLETE = 36053;
    public static final int GL_FRONT = 1028;
    public static final int GL_INVALID_VALUE = 1281;
    public static final int GL_LEQUAL = 515;
    public static final int GL_LESS = 513;
    public static final int GL_LINEAR = 9729;
    public static final int GL_LINEAR_MIPMAP_LINEAR = 9987;
    public static final int GL_LINES = 1;
    public static final int GL_LINE_LOOP = 2;
    public static final int GL_LINE_STRIP = 3;
    public static final int GL_LINK_STATUS = 35714;
    public static final int GL_MAX_TEXTURE_SIZE = 3379;
    public static final int GL_NEAREST = 9728;
    public static final int GL_NICEST = 4354;
    public static final int GL_NONE = 0;
    public static final int GL_NO_ERROR = 0;
    public static final int GL_ONE = 1;
    public static final int GL_ONE_MINUS_SRC_ALPHA = 771;
    public static final int GL_POINTS = 0;
    public static final int GL_RENDERBUFFER = 36161;
    public static final int GL_RENDERER = 7937;
    public static final int GL_REPEAT = 10497;
    public static final int GL_REPLACE = 7681;
    public static final int GL_RGB = 6407;
    public static final int GL_RGBA = 6408;
    public static final int GL_SAMPLER_2D = 35678;
    public static final int GL_SHADER_COMPILER = 36346;
    public static final int GL_SRC_ALPHA = 770;
    public static final int GL_STATIC_DRAW = 35044;
    public static final int GL_STENCIL_ATTACHMENT = 36128;
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
    public static final int GL_VALIDATE_STATUS = 35715;
    public static final int GL_VENDOR = 7936;
    public static final int GL_VERSION = 7938;
    public static final int GL_VERTEX_SHADER = 35633;
    public static final int GL_ZERO = 0;

    public static void glClearColor(float f, float f2, float f3, float f4) {
        GLES20Adapter.glClearColor(f, f2, f3, f4);
    }

    public static void glAttachShader(int i, int i2) {
        GLES20Adapter.glAttachShader(i, i2);
    }

    public static int glCreateProgram() {
        return GLES20Adapter.glCreateProgram();
    }

    public static void glLinkProgram(int i) {
        GLES20Adapter.glLinkProgram(i);
    }

    public static void glViewport(int i, int i2, int i3, int i4) {
        GLES20Adapter.glViewport(i, i2, i3, i4);
    }

    public static void glUseProgram(int i) {
        GLES20Adapter.glUseProgram(i);
    }

    public static int glGetAttribLocation(int i, String str) {
        return GLES20Adapter.glGetAttribLocation(i, str);
    }

    public static void glEnableVertexAttribArray(int i) {
        GLES20Adapter.glEnableVertexAttribArray(i);
    }

    public static void glVertexAttribPointer(int i, int i2, int i3, boolean z, int i4, int i5) {
        GLES20Adapter.glVertexAttribPointer(i, i2, i3, z, i4, i5);
    }

    public static void glVertexAttribPointer(int i, int i2, int i3, boolean z, int i4, Buffer buffer) {
        GLES20Adapter.glVertexAttribPointer(i, i2, i3, z, i4, buffer);
    }

    public static int glGetUniformLocation(int i, String str) {
        return GLES20Adapter.glGetUniformLocation(i, str);
    }

    public static void glUniform4fv(int i, int i2, float[] fArr) {
        GLES20Adapter.glUniform4fv(i, i2, fArr);
    }

    public static void glDrawArrays(int i, int i2, int i3) {
        GLES20Adapter.glDrawArrays(i, i2, i3);
    }

    public static void glDisableVertexAttribArray(int i) {
        GLES20Adapter.glDisableVertexAttribArray(i);
    }

    public static int glCreateShader(int i) {
        return GLES20Adapter.glCreateShader(i);
    }

    public static void glShaderSource(int i, String[] strArr) {
        GLES20Adapter.glShaderSource(i, strArr);
    }

    public static void glCompileShader(int i) {
        GLES20Adapter.glCompileShader(i);
    }

    public static void glEnable(int i) {
        GLES20Adapter.glEnable(i);
    }

    public static void glActiveTexture(int i) {
        GLES20Adapter.glActiveTexture(i);
    }

    public static void glClear(int i) {
        GLES20Adapter.glClear(i);
    }

    public static void glTexParameteri(int i, int i2, int i3) {
        GLES20Adapter.glTexParameteri(i, i2, i3);
    }

    public static void glGenTextures(int i, int[] iArr) {
        GLES20Adapter.glGenTextures(i, iArr);
    }

    public static void glBindTexture(int i, int i2) {
        GLES20Adapter.glBindTexture(i, i2);
    }

    public static void glDrawElements(int i, int i2, int i3, Buffer buffer) {
        GLES20Adapter.glDrawElements(i, i2, i3, buffer);
    }

    public static void glUniform1i(int i, int i2) {
        GLES20Adapter.glUniform1i(i, i2);
    }

    public static void glBindFramebuffer(int i, int i2) {
        GLES20Adapter.glBindFramebuffer(i, i2);
    }

    public static void glBindRenderbuffer(int i, int i2) {
        GLES20Adapter.glBindRenderbuffer(i, i2);
    }

    public static int glCheckFramebufferStatus(int i) {
        return GLES20Adapter.glCheckFramebufferStatus(i);
    }

    public static void glCullFace(int i) {
        GLES20Adapter.glCullFace(i);
    }

    public static void glDeleteProgram(int i) {
        GLES20Adapter.glDeleteProgram(i);
    }

    public static void glDepthFunc(int i) {
        GLES20Adapter.glDepthFunc(i);
    }

    public static void glDisable(int i) {
        GLES20Adapter.glDisable(i);
    }

    public static void glFramebufferRenderbuffer(int i, int i2, int i3, int i4) {
        GLES20Adapter.glFramebufferRenderbuffer(i, i2, i3, i4);
    }

    public static void glFramebufferTexture2D(int i, int i2, int i3, int i4, int i5) {
        GLES20Adapter.glFramebufferTexture2D(i, i2, i3, i4, i5);
    }

    public static void glFrontFace(int i) {
        GLES20Adapter.glFrontFace(i);
    }

    public static void glRenderbufferStorage(int i, int i2, int i3, int i4) {
        GLES20Adapter.glRenderbufferStorage(i, i2, i3, i4);
    }

    public static void glTexParameterf(int i, int i2, float f) {
        GLES20Adapter.glTexParameterf(i, i2, f);
    }

    public static void glUniform1f(int i, float f) {
        GLES20Adapter.glUniform1f(i, f);
    }

    public static void glUniform2f(int i, float f, float f2) {
        GLES20Adapter.glUniform2f(i, f, f2);
    }

    public static void glUniform3f(int i, float f, float f2, float f3) {
        GLES20Adapter.glUniform3f(i, f, f2, f3);
    }

    public static void glUniform4f(int i, float f, float f2, float f3, float f4) {
        GLES20Adapter.glUniform4f(i, f, f2, f3, f4);
    }

    public static void glBlendFunc(int i, int i2) {
        GLES20Adapter.glBlendFunc(i, i2);
    }

    public static void glBlendFuncSeparate(int i, int i2, int i3, int i4) {
        GLES20Adapter.glBlendFuncSeparate(i, i2, i3, i4);
    }

    public static int glGetError() {
        return GLES20Adapter.glGetError();
    }

    public static void glDeleteFramebuffers(int i, IntBuffer intBuffer) {
        GLES20Adapter.glDeleteFramebuffers(i, intBuffer);
    }

    public static void glDeleteRenderbuffers(int i, IntBuffer intBuffer) {
        GLES20Adapter.glDeleteRenderbuffers(i, intBuffer);
    }

    public static void glDeleteTextures(int i, IntBuffer intBuffer) {
        GLES20Adapter.glDeleteTextures(i, intBuffer);
    }

    public static void glGenFramebuffers(int i, IntBuffer intBuffer) {
        GLES20Adapter.glGenFramebuffers(i, intBuffer);
    }

    public static void glGenRenderbuffers(int i, IntBuffer intBuffer) {
        GLES20Adapter.glGenRenderbuffers(i, intBuffer);
    }

    public static void glGenTextures(int i, IntBuffer intBuffer) {
        GLES20Adapter.glGenTextures(i, intBuffer);
    }

    public static void glGetShaderiv(int i, int i2, IntBuffer intBuffer) {
        GLES20Adapter.glGetShaderiv(i, i2, intBuffer);
    }

    public static void glReadPixels(int i, int i2, int i3, int i4, int i5, int i6, Buffer buffer) {
        GLES20Adapter.glReadPixels(i, i2, i3, i4, i5, i6, buffer);
    }

    public static void glTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        GLES20Adapter.glTexImage2D(i, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    public static void glDeleteShader(int i) {
        GLES20Adapter.glDeleteShader(i);
    }

    public static void glGetProgramiv(int i, int i2, int[] iArr) {
        GLES20Adapter.glGetProgramiv(i, i2, iArr);
    }

    public static void glUniform3fv(int i, int i2, float[] fArr) {
        GLES20Adapter.glUniform3fv(i, i2, fArr);
    }

    public static void glUniformMatrix4fv(int i, int i2, boolean z, float[] fArr) {
        GLES20Adapter.glUniformMatrix4fv(i, i2, z, fArr);
    }

    public static void glDeleteFramebuffers(int i, int[] iArr) {
        GLES20Adapter.glDeleteFramebuffers(i, iArr);
    }

    public static void glDeleteRenderbuffers(int i, int[] iArr) {
        GLES20Adapter.glDeleteRenderbuffers(i, iArr);
    }

    public static void glDeleteTextures(int i, int[] iArr) {
        GLES20Adapter.glDeleteTextures(i, iArr);
    }

    public static void glGenFramebuffers(int i, int[] iArr) {
        GLES20Adapter.glGenFramebuffers(i, iArr);
    }

    public static void glGenRenderbuffers(int i, int[] iArr) {
        GLES20Adapter.glGenRenderbuffers(i, iArr);
    }

    public static String glGetProgramInfoLog(int i, int i2) {
        return GLES20Adapter.glGetProgramInfoLog(i, i2);
    }

    public static String glGetShaderInfoLog(int i, int i2) {
        return GLES20Adapter.glGetShaderInfoLog(i, i2);
    }

    public static void glGenBuffers(int i, int[] iArr) {
        GLES20Adapter.glGenBuffers(i, iArr);
    }

    public static void glBindBuffer(int i, int i2) {
        GLES20Adapter.glBindBuffer(i, i2);
    }

    public static void glBufferData(int i, int i2, Buffer buffer, int i3) {
        GLES20Adapter.glBufferData(i, i2, buffer, i3);
    }

    public static void glGetShaderiv(int i, int i2, int[] iArr) {
        GLES20Adapter.glGetShaderiv(i, i2, iArr);
    }

    public static void glGenerateMipmap(int i) {
        GLES20Adapter.glGenerateMipmap(i);
    }

    public static void glGetIntegerv(int i, int[] iArr) {
        GLES20Adapter.glGetIntegerv(i, iArr);
    }

    public static void glCompressedTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, Buffer buffer) {
        GLES20Adapter.glCompressedTexImage2D(i, i2, i3, i4, i5, i6, i7, buffer);
    }

    public static void glCompressedTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        GLES20Adapter.glCompressedTexSubImage2D(i, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    public static void glDepthRangef(float f, float f2) {
        GLES20Adapter.glDepthRangef(f, f2);
    }

    public static void glGetAttachedShaders(int i, int i2, IntBuffer intBuffer, IntBuffer intBuffer2) {
        GLES20Adapter.glGetAttachedShaders(i, i2, intBuffer, intBuffer2);
    }

    public static void glGetBufferParameteriv(int i, int i2, IntBuffer intBuffer) {
        GLES20Adapter.glGetBufferParameteriv(i, i2, intBuffer);
    }

    public static void glGetFramebufferAttachmentParameteriv(int i, int i2, int i3, IntBuffer intBuffer) {
        GLES20Adapter.glGetFramebufferAttachmentParameteriv(i, i2, i3, intBuffer);
    }

    public static void glGetRenderbufferParameteriv(int i, int i2, IntBuffer intBuffer) {
        GLES20Adapter.glGetBufferParameteriv(i, i2, intBuffer);
    }

    public static void glGetShaderPrecisionFormat(int i, int i2, IntBuffer intBuffer, IntBuffer intBuffer2) {
        GLES20Adapter.glGetShaderPrecisionFormat(i, i2, intBuffer, intBuffer2);
    }

    public static void glGetShaderSource(int i, int i2, IntBuffer intBuffer, CharBuffer charBuffer) {
        GLES20Adapter.glGetShaderSource(i, i2, intBuffer, charBuffer);
    }

    public static void glGetTexParameterfv(int i, int i2, FloatBuffer floatBuffer) {
        GLES20Adapter.glGetTexParameterfv(i, i2, floatBuffer);
    }

    public static void glGetTexParameteriv(int i, int i2, IntBuffer intBuffer) {
        GLES20Adapter.glGetTexParameteriv(i, i2, intBuffer);
    }

    public static void glGetUniformfv(int i, int i2, FloatBuffer floatBuffer) {
        GLES20Adapter.glGetUniformfv(i, i2, floatBuffer);
    }

    public static void glGetUniformiv(int i, int i2, IntBuffer intBuffer) {
        GLES20Adapter.glGetUniformiv(i, i2, intBuffer);
    }

    public static void glGetVertexAttribfv(int i, int i2, FloatBuffer floatBuffer) {
        GLES20Adapter.glGetVertexAttribfv(i, i2, floatBuffer);
    }

    public static void glGetVertexAttribiv(int i, int i2, IntBuffer intBuffer) {
        GLES20Adapter.glGetVertexAttribiv(i, i2, intBuffer);
    }
}
