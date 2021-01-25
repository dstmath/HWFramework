package ohos.agp.render.opengl.adapter;

import android.opengl.GLES20;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import ohos.annotation.SystemApi;

@SystemApi
public class GLES20Adapter {
    private static final int INITIAL_OFFSET_VALUE = 0;

    public static void glClearColor(float f, float f2, float f3, float f4) {
        GLES20.glClearColor(f, f2, f3, f4);
    }

    public static void glAttachShader(int i, int i2) {
        GLES20.glAttachShader(i, i2);
    }

    public static int glCreateProgram() {
        return GLES20.glCreateProgram();
    }

    public static void glLinkProgram(int i) {
        GLES20.glLinkProgram(i);
    }

    public static void glViewport(int i, int i2, int i3, int i4) {
        GLES20.glViewport(i, i2, i3, i4);
    }

    public static void glUseProgram(int i) {
        GLES20.glUseProgram(i);
    }

    public static int glGetAttribLocation(int i, String str) {
        return GLES20.glGetAttribLocation(i, str);
    }

    public static void glEnableVertexAttribArray(int i) {
        GLES20.glEnableVertexAttribArray(i);
    }

    public static void glVertexAttribPointer(int i, int i2, int i3, boolean z, int i4, int i5) {
        GLES20.glVertexAttribPointer(i, i2, i3, z, i4, i5);
    }

    public static void glVertexAttribPointer(int i, int i2, int i3, boolean z, int i4, Buffer buffer) {
        GLES20.glVertexAttribPointer(i, i2, i3, z, i4, buffer);
    }

    public static int glGetUniformLocation(int i, String str) {
        return GLES20.glGetUniformLocation(i, str);
    }

    public static void glUniform4fv(int i, int i2, float[] fArr) {
        GLES20.glUniform4fv(i, i2, fArr, 0);
    }

    public static void glDrawArrays(int i, int i2, int i3) {
        GLES20.glDrawArrays(i, i2, i3);
    }

    public static void glDisableVertexAttribArray(int i) {
        GLES20.glDisableVertexAttribArray(i);
    }

    public static int glCreateShader(int i) {
        return GLES20.glCreateShader(i);
    }

    public static void glShaderSource(int i, String[] strArr) {
        StringBuilder sb = new StringBuilder();
        for (String str : strArr) {
            sb.append(str);
        }
        GLES20.glShaderSource(i, sb.toString());
    }

    public static void glCompileShader(int i) {
        GLES20.glCompileShader(i);
    }

    public static void glEnable(int i) {
        GLES20.glEnable(i);
    }

    public static void glActiveTexture(int i) {
        GLES20.glActiveTexture(i);
    }

    public static void glClear(int i) {
        GLES20.glClear(i);
    }

    public static void glTexParameteri(int i, int i2, int i3) {
        GLES20.glTexParameteri(i, i2, i3);
    }

    public static void glGenTextures(int i, int[] iArr) {
        GLES20.glGenTextures(i, iArr, 0);
    }

    public static void glBindTexture(int i, int i2) {
        GLES20.glBindTexture(i, i2);
    }

    public static void glDrawElements(int i, int i2, int i3, Buffer buffer) {
        GLES20.glDrawElements(i, i2, i3, buffer);
    }

    public static void glUniform1i(int i, int i2) {
        GLES20.glUniform1i(i, i2);
    }

    public static void glBindFramebuffer(int i, int i2) {
        GLES20.glBindFramebuffer(i, i2);
    }

    public static void glBindRenderbuffer(int i, int i2) {
        GLES20.glBindRenderbuffer(i, i2);
    }

    public static int glCheckFramebufferStatus(int i) {
        return GLES20.glCheckFramebufferStatus(i);
    }

    public static void glCullFace(int i) {
        GLES20.glCullFace(i);
    }

    public static void glDeleteProgram(int i) {
        GLES20.glDeleteProgram(i);
    }

    public static void glDepthFunc(int i) {
        GLES20.glDepthFunc(i);
    }

    public static void glDisable(int i) {
        GLES20.glDisable(i);
    }

    public static void glFramebufferRenderbuffer(int i, int i2, int i3, int i4) {
        GLES20.glFramebufferRenderbuffer(i, i2, i3, i4);
    }

    public static void glFramebufferTexture2D(int i, int i2, int i3, int i4, int i5) {
        GLES20.glFramebufferTexture2D(i, i2, i3, i4, i5);
    }

    public static void glFrontFace(int i) {
        GLES20.glFrontFace(i);
    }

    public static void glRenderbufferStorage(int i, int i2, int i3, int i4) {
        GLES20.glRenderbufferStorage(i, i2, i3, i4);
    }

    public static void glTexParameterf(int i, int i2, float f) {
        GLES20.glTexParameterf(i, i2, f);
    }

    public static void glUniform1f(int i, float f) {
        GLES20.glUniform1f(i, f);
    }

    public static void glUniform2f(int i, float f, float f2) {
        GLES20.glUniform2f(i, f, f2);
    }

    public static void glUniform3f(int i, float f, float f2, float f3) {
        GLES20.glUniform3f(i, f, f2, f3);
    }

    public static void glUniform4f(int i, float f, float f2, float f3, float f4) {
        GLES20.glUniform4f(i, f, f2, f3, f4);
    }

    public static void glBlendFunc(int i, int i2) {
        GLES20.glBlendFunc(i, i2);
    }

    public static void glBlendFuncSeparate(int i, int i2, int i3, int i4) {
        GLES20.glBlendFuncSeparate(i, i2, i3, i4);
    }

    public static int glGetError() {
        return GLES20.glGetError();
    }

    public static void glDeleteFramebuffers(int i, IntBuffer intBuffer) {
        GLES20.glDeleteFramebuffers(i, intBuffer);
    }

    public static void glDeleteRenderbuffers(int i, IntBuffer intBuffer) {
        GLES20.glDeleteRenderbuffers(i, intBuffer);
    }

    public static void glDeleteTextures(int i, IntBuffer intBuffer) {
        GLES20.glDeleteTextures(i, intBuffer);
    }

    public static void glGenFramebuffers(int i, IntBuffer intBuffer) {
        GLES20.glGenFramebuffers(i, intBuffer);
    }

    public static void glGenRenderbuffers(int i, IntBuffer intBuffer) {
        GLES20.glGenRenderbuffers(i, intBuffer);
    }

    public static void glGenTextures(int i, IntBuffer intBuffer) {
        GLES20.glGenTextures(i, intBuffer);
    }

    public static void glGetShaderiv(int i, int i2, IntBuffer intBuffer) {
        GLES20.glGetShaderiv(i, i2, intBuffer);
    }

    public static void glReadPixels(int i, int i2, int i3, int i4, int i5, int i6, Buffer buffer) {
        GLES20.glReadPixels(i, i2, i3, i4, i5, i6, buffer);
    }

    public static void glTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        GLES20.glTexImage2D(i, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    public static void glDeleteShader(int i) {
        GLES20.glDeleteShader(i);
    }

    public static void glGetProgramiv(int i, int i2, int[] iArr) {
        GLES20.glGetProgramiv(i, i2, iArr, 0);
    }

    public static void glUniform3fv(int i, int i2, float[] fArr) {
        GLES20.glUniform3fv(i, i2, fArr, 0);
    }

    public static void glUniformMatrix4fv(int i, int i2, boolean z, float[] fArr) {
        GLES20.glUniformMatrix4fv(i, i2, z, fArr, 0);
    }

    public static void glDeleteFramebuffers(int i, int[] iArr) {
        GLES20.glDeleteFramebuffers(i, iArr, 0);
    }

    public static void glDeleteRenderbuffers(int i, int[] iArr) {
        GLES20.glDeleteRenderbuffers(i, iArr, 0);
    }

    public static void glDeleteTextures(int i, int[] iArr) {
        GLES20.glDeleteTextures(i, iArr, 0);
    }

    public static void glGenFramebuffers(int i, int[] iArr) {
        GLES20.glGenFramebuffers(i, iArr, 0);
    }

    public static void glGenRenderbuffers(int i, int[] iArr) {
        GLES20.glGenRenderbuffers(i, iArr, 0);
    }

    public static String glGetProgramInfoLog(int i, int i2) {
        String glGetProgramInfoLog = GLES20.glGetProgramInfoLog(i);
        return glGetProgramInfoLog.length() > i2 ? glGetProgramInfoLog.substring(0, i2) : glGetProgramInfoLog;
    }

    public static String glGetShaderInfoLog(int i, int i2) {
        String glGetShaderInfoLog = GLES20.glGetShaderInfoLog(i);
        return glGetShaderInfoLog.length() > i2 ? glGetShaderInfoLog.substring(0, i2) : glGetShaderInfoLog;
    }

    public static void glGenBuffers(int i, int[] iArr) {
        GLES20.glGenBuffers(i, iArr, 0);
    }

    public static void glBindBuffer(int i, int i2) {
        GLES20.glBindBuffer(i, i2);
    }

    public static void glBufferData(int i, int i2, Buffer buffer, int i3) {
        GLES20.glBufferData(i, i2, buffer, i3);
    }

    public static void glGetShaderiv(int i, int i2, int[] iArr) {
        GLES20.glGetShaderiv(i, i2, iArr, 0);
    }

    public static void glGenerateMipmap(int i) {
        GLES20.glGenerateMipmap(i);
    }

    public static void glGetIntegerv(int i, int[] iArr) {
        GLES20.glGetIntegerv(i, iArr, 0);
    }

    public static void glCompressedTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, Buffer buffer) {
        GLES20.glCompressedTexImage2D(i, i2, i3, i4, i5, i6, i7, buffer);
    }

    public static void glCompressedTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        GLES20.glCompressedTexSubImage2D(i, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    public static void glDepthRangef(float f, float f2) {
        GLES20.glDepthRangef(f, f2);
    }

    public static void glGetAttachedShaders(int i, int i2, IntBuffer intBuffer, IntBuffer intBuffer2) {
        GLES20.glGetAttachedShaders(i, i2, intBuffer, intBuffer2);
    }

    public static void glGetBufferParameteriv(int i, int i2, IntBuffer intBuffer) {
        GLES20.glGetBufferParameteriv(i, i2, intBuffer);
    }

    public static void glGetFramebufferAttachmentParameteriv(int i, int i2, int i3, IntBuffer intBuffer) {
        GLES20.glGetFramebufferAttachmentParameteriv(i, i2, i3, intBuffer);
    }

    public static void glGetRenderbufferParameteriv(int i, int i2, IntBuffer intBuffer) {
        GLES20.glGetBufferParameteriv(i, i2, intBuffer);
    }

    public static void glGetShaderPrecisionFormat(int i, int i2, IntBuffer intBuffer, IntBuffer intBuffer2) {
        GLES20.glGetShaderPrecisionFormat(i, i2, intBuffer, intBuffer2);
    }

    public static void glGetShaderSource(int i, int i2, IntBuffer intBuffer, CharBuffer charBuffer) {
        int[] array = intBuffer.array();
        ByteBuffer allocate = ByteBuffer.allocate(charBuffer.capacity());
        while (charBuffer.hasRemaining()) {
            allocate.putChar(charBuffer.get());
        }
        GLES20.glGetShaderSource(i, i2, array, 0, allocate.array(), 0);
    }

    public static void glGetTexParameterfv(int i, int i2, FloatBuffer floatBuffer) {
        GLES20.glGetTexParameterfv(i, i2, floatBuffer);
    }

    public static void glGetTexParameteriv(int i, int i2, IntBuffer intBuffer) {
        GLES20.glGetTexParameteriv(i, i2, intBuffer);
    }

    public static void glGetUniformfv(int i, int i2, FloatBuffer floatBuffer) {
        GLES20.glGetUniformfv(i, i2, floatBuffer);
    }

    public static void glGetUniformiv(int i, int i2, IntBuffer intBuffer) {
        GLES20.glGetUniformiv(i, i2, intBuffer);
    }

    public static void glGetVertexAttribfv(int i, int i2, FloatBuffer floatBuffer) {
        GLES20.glGetVertexAttribfv(i, i2, floatBuffer);
    }

    public static void glGetVertexAttribiv(int i, int i2, IntBuffer intBuffer) {
        GLES20.glGetVertexAttribiv(i, i2, intBuffer);
    }
}
