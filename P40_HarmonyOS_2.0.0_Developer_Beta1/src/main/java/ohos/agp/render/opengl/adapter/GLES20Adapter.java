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

    public static void glShaderSource(int i, int i2, String[] strArr, IntBuffer intBuffer) {
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

    public static void glGetProgramInfoLog(int i, int i2, IntBuffer intBuffer, StringBuffer stringBuffer) {
        String glGetProgramInfoLog = GLES20.glGetProgramInfoLog(i);
        if (glGetProgramInfoLog.length() > i2) {
            stringBuffer.append(glGetProgramInfoLog.substring(0, i2));
        } else {
            stringBuffer.append(glGetProgramInfoLog);
        }
        if (intBuffer != null) {
            intBuffer.put(stringBuffer.length());
        }
    }

    public static void glGetShaderInfoLog(int i, int i2, IntBuffer intBuffer, StringBuffer stringBuffer) {
        String glGetShaderInfoLog = GLES20.glGetShaderInfoLog(i);
        if (glGetShaderInfoLog.length() > i2) {
            stringBuffer.append(glGetShaderInfoLog.substring(0, i2));
        } else {
            stringBuffer.append(glGetShaderInfoLog);
        }
        if (intBuffer != null) {
            intBuffer.put(stringBuffer.length());
        }
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

    public static void glUniformMatrix2fv(int i, int i2, boolean z, float[] fArr) {
        GLES20.glUniformMatrix2fv(i, i2, z, fArr, 0);
    }

    public static void glUniform1iv(int i, int i2, int[] iArr) {
        GLES20.glUniform1iv(i, i2, iArr, 0);
    }

    public static boolean glIsTexture(int i) {
        return GLES20.glIsTexture(i);
    }

    public static boolean glIsProgram(int i) {
        return GLES20.glIsProgram(i);
    }

    public static boolean glIsEnabled(int i) {
        return GLES20.glIsEnabled(i);
    }

    public static void glGetFloatv(int i, float[] fArr) {
        GLES20.glGetFloatv(i, fArr, 0);
    }

    public static void glGetBooleanv(int i, boolean[] zArr) {
        GLES20.glGetBooleanv(i, zArr, 0);
    }

    public static void glGetActiveUniform(int i, int i2, int i3, int[] iArr, int[] iArr2, int[] iArr3, byte[] bArr) {
        GLES20.glGetActiveUniform(i, i2, i3, iArr, 0, iArr2, 0, iArr3, 0, bArr, 0);
    }

    public static void glGetActiveAttrib(int i, int i2, int i3, int[] iArr, int[] iArr2, int[] iArr3, byte[] bArr) {
        GLES20.glGetActiveAttrib(i, i2, i3, iArr, 0, iArr2, 0, iArr3, 0, bArr, 0);
    }

    public static void glDetachShader(int i, int i2) {
        GLES20.glDetachShader(i, i2);
    }

    public static void glCopyTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        GLES20.glCopyTexSubImage2D(i, i2, i3, i4, i5, i6, i7, i8);
    }

    public static void glCopyTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        GLES20.glCopyTexImage2D(i, i2, i3, i4, i5, i6, i7, i8);
    }

    public static void glBlendEquationSeparate(int i, int i2) {
        GLES20.glBlendEquationSeparate(i, i2);
    }

    public static void glBlendEquation(int i) {
        GLES20.glBlendEquation(i);
    }

    public static void glHint(int i, int i2) {
        GLES20.glHint(i, i2);
    }

    public static boolean glIsBuffer(int i) {
        return GLES20.glIsBuffer(i);
    }

    public static boolean glIsFramebuffer(int i) {
        return GLES20.glIsFramebuffer(i);
    }

    public static boolean glIsRenderbuffer(int i) {
        return GLES20.glIsRenderbuffer(i);
    }

    public static void glPolygonOffset(float f, float f2) {
        GLES20.glPolygonOffset(f, f2);
    }

    public static void glReleaseShaderCompiler() {
        GLES20.glReleaseShaderCompiler();
    }

    public static void glSampleCoverage(float f, boolean z) {
        GLES20.glSampleCoverage(f, z);
    }

    public static void glShaderBinary(int i, IntBuffer intBuffer, int i2, Buffer buffer, int i3) {
        GLES20.glShaderBinary(i, intBuffer, i2, buffer, i3);
    }

    public static void glStencilFuncSeparate(int i, int i2, int i3, int i4) {
        GLES20.glStencilFuncSeparate(i, i2, i3, i4);
    }

    public static void glStencilMaskSeparate(int i, int i2) {
        GLES20.glStencilMaskSeparate(i, i2);
    }

    public static void glStencilOpSeparate(int i, int i2, int i3, int i4) {
        GLES20.glStencilOpSeparate(i, i2, i3, i4);
    }

    public static void glTexParameterfv(int i, int i2, FloatBuffer floatBuffer) {
        GLES20.glTexParameterfv(i, i2, floatBuffer);
    }

    public static void glTexParameteriv(int i, int i2, IntBuffer intBuffer) {
        GLES20.glTexParameteriv(i, i2, intBuffer);
    }

    public static void glValidateProgram(int i) {
        GLES20.glValidateProgram(i);
    }

    public static void glUniformMatrix3fv(int i, int i2, boolean z, float[] fArr) {
        GLES20.glUniformMatrix3fv(i, i2, z, fArr, 0);
    }

    public static void glTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        GLES20.glTexSubImage2D(i, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    public static void glStencilOp(int i, int i2, int i3) {
        GLES20.glStencilOp(i, i2, i3);
    }

    public static void glStencilMask(int i) {
        GLES20.glStencilMask(i);
    }

    public static void glStencilFunc(int i, int i2, int i3) {
        GLES20.glStencilFunc(i, i2, i3);
    }

    public static void glPixelStorei(int i, int i2) {
        GLES20.glPixelStorei(i, i2);
    }

    public static void glLineWidth(float f) {
        GLES20.glLineWidth(f);
    }

    public static String glGetString(int i) {
        return GLES20.glGetString(i);
    }

    public static void glGenBuffers(int i, IntBuffer intBuffer) {
        GLES20.glGenBuffers(i, intBuffer);
    }

    public static void glFlush() {
        GLES20.glFlush();
    }

    public static void glFinish() {
        GLES20.glFinish();
    }

    public static void glDepthMask(boolean z) {
        GLES20.glDepthMask(z);
    }

    public static void glDeleteBuffers(int i, IntBuffer intBuffer) {
        GLES20.glDeleteBuffers(i, intBuffer);
    }

    public static void glColorMask(boolean z, boolean z2, boolean z3, boolean z4) {
        GLES20.glColorMask(z, z2, z3, z4);
    }

    public static void glClearStencil(int i) {
        GLES20.glClearStencil(i);
    }

    public static void glClearDepthf(float f) {
        GLES20.glClearDepthf(f);
    }

    public static void glBufferSubData(int i, int i2, int i3, Buffer buffer) {
        GLES20.glBufferSubData(i, i2, i3, buffer);
    }

    public static void glBlendColor(float f, float f2, float f3, float f4) {
        GLES20.glBlendColor(f, f2, f3, f4);
    }

    public static void glScissor(int i, int i2, int i3, int i4) {
        GLES20.glScissor(i, i2, i3, i4);
    }

    public static void glBindAttribLocation(int i, int i2, String str) {
        GLES20.glBindAttribLocation(i, i2, str);
    }

    public static void glUniform1fv(int i, int i2, FloatBuffer floatBuffer) {
        GLES20.glUniform1fv(i, i2, floatBuffer);
    }

    public static void glUniform2fv(int i, int i2, FloatBuffer floatBuffer) {
        GLES20.glUniform2fv(i, i2, floatBuffer);
    }

    public static void glUniform2i(int i, int i2, int i3) {
        GLES20.glUniform2i(i, i2, i3);
    }

    public static void glUniform2iv(int i, int i2, IntBuffer intBuffer) {
        GLES20.glUniform2iv(i, i2, intBuffer);
    }

    public static void glUniform3i(int i, int i2, int i3, int i4) {
        GLES20.glUniform3i(i, i2, i3, i4);
    }

    public static void glUniform3iv(int i, int i2, IntBuffer intBuffer) {
        GLES20.glUniform3iv(i, i2, intBuffer);
    }

    public static void glUniform4i(int i, int i2, int i3, int i4, int i5) {
        GLES20.glUniform4i(i, i2, i3, i4, i5);
    }

    public static void glUniform4iv(int i, int i2, IntBuffer intBuffer) {
        GLES20.glUniform4iv(i, i2, intBuffer);
    }

    public static void glVertexAttrib1f(int i, float f) {
        GLES20.glVertexAttrib1f(i, f);
    }

    public static void glVertexAttrib1fv(int i, FloatBuffer floatBuffer) {
        GLES20.glVertexAttrib1fv(i, floatBuffer);
    }

    public static void glVertexAttrib2f(int i, float f, float f2) {
        GLES20.glVertexAttrib2f(i, f, f2);
    }

    public static void glVertexAttrib2fv(int i, FloatBuffer floatBuffer) {
        GLES20.glVertexAttrib2fv(i, floatBuffer);
    }

    public static void glVertexAttrib3f(int i, float f, float f2, float f3) {
        GLES20.glVertexAttrib3f(i, f, f2, f3);
    }

    public static void glVertexAttrib3fv(int i, FloatBuffer floatBuffer) {
        GLES20.glVertexAttrib3fv(i, floatBuffer);
    }

    public static void glVertexAttrib4f(int i, float f, float f2, float f3, float f4) {
        GLES20.glVertexAttrib4f(i, f, f2, f3, f4);
    }

    public static void glVertexAttrib4fv(int i, FloatBuffer floatBuffer) {
        GLES20.glVertexAttrib4fv(i, floatBuffer);
    }

    public static boolean glIsShader(int i) {
        return GLES20.glIsShader(i);
    }
}
