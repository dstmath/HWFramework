package ohos.agp.render.opengl.adapter;

import android.opengl.GLES30;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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

    public static void glWaitSync(long j, int i, long j2) {
        GLES30.glWaitSync(j, i, j2);
    }

    public static void glTransformFeedbackVaryings(int i, int i2, String[] strArr, int i3) {
        GLES30.glTransformFeedbackVaryings(i, strArr, i3);
    }

    public static void glReadPixels(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        GLES30.glReadPixels(i, i2, i3, i4, i5, i6, i7);
    }

    public static void glReadBuffer(int i) {
        GLES30.glReadBuffer(i);
    }

    public static Buffer glMapBufferRange(int i, int i2, int i3, int i4) {
        return GLES30.glMapBufferRange(i, i2, i3, i4);
    }

    public static long glFenceSync(int i, int i2) {
        return GLES30.glFenceSync(i, i2);
    }

    public static void glBeginTransformFeedback(int i) {
        GLES30.glBeginTransformFeedback(i);
    }

    public static void glEndTransformFeedback() {
        GLES30.glEndTransformFeedback();
    }

    public static void glDeleteVertexArrays(int i, int[] iArr) {
        GLES30.glDeleteVertexArrays(i, iArr, 0);
    }

    public static void glBindBufferBase(int i, int i2, int i3) {
        GLES30.glBindBufferBase(i, i2, i3);
    }

    public static boolean glUnmapBuffer(int i) {
        return GLES30.glUnmapBuffer(i);
    }

    public static void glDrawRangeElements(int i, int i2, int i3, int i4, int i5, Buffer buffer) {
        GLES30.glDrawRangeElements(i, i2, i3, i4, i5, buffer);
    }

    public static void glTexImage3D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, Buffer buffer) {
        GLES30.glTexImage3D(i, i2, i3, i4, i5, i6, i7, i8, i9, buffer);
    }

    public static void glTexSubImage3D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, Buffer buffer) {
        GLES30.glTexSubImage3D(i, i2, i3, i4, i5, i6, i7, i8, i9, i10, buffer);
    }

    public static void glCopyTexSubImage3D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
        GLES30.glCopyTexSubImage3D(i, i2, i3, i4, i5, i6, i7, i8, i9);
    }

    public static void glCompressedTexImage3D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        GLES30.glCompressedTexImage3D(i, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    public static void glCompressedTexSubImage3D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, Buffer buffer) {
        GLES30.glCompressedTexSubImage3D(i, i2, i3, i4, i5, i6, i7, i8, i9, i10, buffer);
    }

    public static void glGenQueries(int i, IntBuffer intBuffer) {
        GLES30.glGenQueries(i, intBuffer);
    }

    public static void glDeleteQueries(int i, IntBuffer intBuffer) {
        GLES30.glDeleteQueries(i, intBuffer);
    }

    public static boolean glIsQuery(int i) {
        return GLES30.glIsQuery(i);
    }

    public static void glBeginQuery(int i, int i2) {
        GLES30.glBeginQuery(i, i2);
    }

    public static void glEndQuery(int i) {
        GLES30.glEndQuery(i);
    }

    public static void glGetQueryiv(int i, int i2, IntBuffer intBuffer) {
        GLES30.glGetQueryiv(i, i2, intBuffer);
    }

    public static void glGetQueryObjectuiv(int i, int i2, IntBuffer intBuffer) {
        GLES30.glGetQueryObjectuiv(i, i2, intBuffer);
    }

    public static void glGetBufferPointerv(int i, int i2, Buffer buffer) {
        GLES30.glGetBufferPointerv(i, i2);
    }

    public static void glBindBufferRange(int i, int i2, int i3, int i4, int i5) {
        GLES30.glBindBufferRange(i, i2, i3, i4, i5);
    }

    public static void glGetTransformFeedbackVarying(int i, int i2, int i3, int[] iArr, int[] iArr2, int[] iArr3, byte[] bArr) {
        GLES30.glGetTransformFeedbackVarying(i, i2, i3, iArr, 0, iArr2, 0, iArr3, 0, bArr, 0);
    }

    public static void glVertexAttribIPointer(int i, int i2, int i3, int i4, int i5) {
        GLES30.glVertexAttribIPointer(i, i2, i3, i4, i5);
    }

    public static void glGetVertexAttribIiv(int i, int i2, int[] iArr) {
        GLES30.glGetVertexAttribIiv(i, i2, iArr, 0);
    }

    public static void glGetVertexAttribIuiv(int i, int i2, int[] iArr) {
        GLES30.glGetVertexAttribIuiv(i, i2, iArr, 0);
    }

    public static void glVertexAttribI4i(int i, int i2, int i3, int i4, int i5) {
        GLES30.glVertexAttribI4i(i, i2, i3, i4, i5);
    }

    public static void glVertexAttribI4ui(int i, int i2, int i3, int i4, int i5) {
        GLES30.glVertexAttribI4ui(i, i2, i3, i4, i5);
    }

    public static void glVertexAttribI4iv(int i, int[] iArr) {
        GLES30.glVertexAttribI4iv(i, iArr, 0);
    }

    public static void glVertexAttribI4uiv(int i, int[] iArr) {
        GLES30.glVertexAttribI4uiv(i, iArr, 0);
    }

    public static void glGetUniformuiv(int i, int i2, int[] iArr) {
        GLES30.glGetUniformuiv(i, i2, iArr, 0);
    }

    public static int glGetFragDataLocation(int i, String str) {
        return GLES30.glGetFragDataLocation(i, str);
    }

    public static void glUniform1ui(int i, int i2) {
        GLES30.glUniform1ui(i, i2);
    }

    public static void glUniform2ui(int i, int i2, int i3) {
        GLES30.glUniform2ui(i, i2, i3);
    }

    public static void glUniformMatrix2x3fv(int i, int i2, boolean z, FloatBuffer floatBuffer) {
        GLES30.glUniformMatrix2x3fv(i, i2, z, floatBuffer);
    }

    public static void glUniform3ui(int i, int i2, int i3, int i4) {
        GLES30.glUniform3ui(i, i2, i3, i4);
    }

    public static void glUniformMatrix3x2fv(int i, int i2, boolean z, FloatBuffer floatBuffer) {
        GLES30.glUniformMatrix3x2fv(i, i2, z, floatBuffer);
    }

    public static void glUniformMatrix2x4fv(int i, int i2, boolean z, FloatBuffer floatBuffer) {
        GLES30.glUniformMatrix2x4fv(i, i2, z, floatBuffer);
    }

    public static void glUniform4ui(int i, int i2, int i3, int i4, int i5) {
        GLES30.glUniform4ui(i, i2, i3, i4, i5);
    }

    public static void glUniform1uiv(int i, int i2, int[] iArr) {
        GLES30.glUniform1uiv(i, i2, iArr, 0);
    }

    public static void glUniformMatrix4x2fv(int i, int i2, boolean z, FloatBuffer floatBuffer) {
        GLES30.glUniformMatrix4x2fv(i, i2, z, floatBuffer);
    }

    public static void glUniform2uiv(int i, int i2, int[] iArr) {
        GLES30.glUniform2uiv(i, i2, iArr, 0);
    }

    public static void glUniformMatrix3x4fv(int i, int i2, boolean z, FloatBuffer floatBuffer) {
        GLES30.glUniformMatrix3x4fv(i, i2, z, floatBuffer);
    }

    public static void glUniform3uiv(int i, int i2, int[] iArr) {
        GLES30.glUniform3uiv(i, i2, iArr, 0);
    }

    public static void glUniformMatrix4x3fv(int i, int i2, boolean z, FloatBuffer floatBuffer) {
        GLES30.glUniformMatrix4x3fv(i, i2, z, floatBuffer);
    }

    public static void glFramebufferTextureLayer(int i, int i2, int i3, int i4, int i5) {
        GLES30.glFramebufferTextureLayer(i, i2, i3, i4, i5);
    }

    public static void glFlushMappedBufferRange(int i, int i2, int i3) {
        GLES30.glFlushMappedBufferRange(i, i2, i3);
    }

    public static boolean glIsVertexArray(int i) {
        return GLES30.glIsVertexArray(i);
    }

    public static void glGetIntegerIndexv(int i, int i2, IntBuffer intBuffer) {
        GLES30.glGetIntegeri_v(i, i2, intBuffer);
    }

    public static void glSamplerParameteri(int i, int i2, int i3) {
        GLES30.glSamplerParameteri(i, i2, i3);
    }

    public static void glSamplerParameteriv(int i, int i2, int[] iArr) {
        GLES30.glSamplerParameteriv(i, i2, iArr, 0);
    }

    public static void glSamplerParameterf(int i, int i2, float f) {
        GLES30.glSamplerParameterf(i, i2, f);
    }

    public static void glSamplerParameterfv(int i, int i2, float[] fArr) {
        GLES30.glSamplerParameterfv(i, i2, fArr, 0);
    }

    public static void glGetSamplerParameteriv(int i, int i2, int[] iArr) {
        GLES30.glGetSamplerParameteriv(i, i2, iArr, 0);
    }

    public static void glGetSamplerParameterfv(int i, int i2, float[] fArr) {
        GLES30.glGetSamplerParameterfv(i, i2, fArr, 0);
    }

    public static void glVertexAttribDivisor(int i, int i2) {
        GLES30.glVertexAttribDivisor(i, i2);
    }

    public static void glBindTransformFeedback(int i, int i2) {
        GLES30.glBindTransformFeedback(i, i2);
    }

    public static void glDeleteTransformFeedbacks(int i, int[] iArr) {
        GLES30.glDeleteTransformFeedbacks(i, iArr, 0);
    }

    public static void glGenTransformFeedbacks(int i, int[] iArr) {
        GLES30.glGenTransformFeedbacks(i, iArr, 0);
    }

    public static boolean glIsTransformFeedback(int i) {
        return GLES30.glIsTransformFeedback(i);
    }

    public static void glPauseTransformFeedback() {
        GLES30.glPauseTransformFeedback();
    }

    public static void glResumeTransformFeedback() {
        GLES30.glResumeTransformFeedback();
    }

    public static void glGetProgramBinary(int i, int i2, int[] iArr, int[] iArr2, Buffer buffer) {
        GLES30.glGetProgramBinary(i, i2, iArr, 0, iArr2, 0, buffer);
    }

    public static void glProgramBinary(int i, int i2, Buffer buffer, int i3) {
        GLES30.glProgramBinary(i, i2, buffer, i3);
    }

    public static void glProgramParameteri(int i, int i2, int i3) {
        GLES30.glProgramParameteri(i, i2, i3);
    }

    public static void glInvalidateFramebuffer(int i, int i2, int[] iArr) {
        GLES30.glInvalidateFramebuffer(i, i2, iArr, 0);
    }

    public static void glInvalidateSubFramebuffer(int i, int i2, int[] iArr, int i3, int i4, int i5, int i6) {
        GLES30.glInvalidateSubFramebuffer(i, i2, iArr, 0, i3, i4, i5, i6);
    }

    public static void glTexStorage2D(int i, int i2, int i3, int i4, int i5) {
        GLES30.glTexStorage2D(i, i2, i3, i4, i5);
    }

    public static void glTexStorage3D(int i, int i2, int i3, int i4, int i5, int i6) {
        GLES30.glTexStorage3D(i, i2, i3, i4, i5, i6);
    }

    public static void glGetInternalformativ(int i, int i2, int i3, int i4, int[] iArr) {
        GLES30.glGetInternalformativ(i, i2, i3, i4, iArr, 0);
    }

    public static void glUniform4uiv(int i, int i2, int[] iArr) {
        GLES30.glUniform4uiv(i, i2, iArr, 0);
    }

    public static void glClearBufferiv(int i, int i2, int[] iArr) {
        GLES30.glClearBufferiv(i, i2, iArr, 0);
    }

    public static void glClearBufferuiv(int i, int i2, int[] iArr) {
        GLES30.glClearBufferuiv(i, i2, iArr, 0);
    }

    public static void glClearBufferfv(int i, int i2, float[] fArr) {
        GLES30.glClearBufferfv(i, i2, fArr, 0);
    }

    public static void glClearBufferfi(int i, int i2, float f, int i3) {
        GLES30.glClearBufferfi(i, i2, f, i3);
    }

    public static String glGetStringi(int i, int i2) {
        return GLES30.glGetStringi(i, i2);
    }

    public static void glCopyBufferSubData(int i, int i2, int i3, int i4, int i5) {
        GLES30.glCopyBufferSubData(i, i2, i3, i4, i5);
    }

    public static void glGetUniformIndices(int i, String[] strArr, int[] iArr) {
        GLES30.glGetUniformIndices(i, strArr, iArr, 0);
    }

    public static void glGetActiveUniformsiv(int i, int i2, int[] iArr, int i3, int[] iArr2) {
        GLES30.glGetActiveUniformsiv(i, i2, iArr, 0, i3, iArr2, 0);
    }

    public static int glGetUniformBlockIndex(int i, String str) {
        return GLES30.glGetUniformBlockIndex(i, str);
    }

    public static void glGetActiveUniformBlockiv(int i, int i2, int i3, int[] iArr) {
        GLES30.glGetActiveUniformBlockiv(i, i2, i3, iArr, 0);
    }

    public static void glGetActiveUniformBlockName(int i, int i2, int i3, int[] iArr, byte[] bArr) {
        GLES30.glGetActiveUniformBlockName(i, i2, i3, iArr, 0, bArr, 0);
    }

    public static void glUniformBlockBinding(int i, int i2, int i3) {
        GLES30.glUniformBlockBinding(i, i2, i3);
    }

    public static void glDrawArraysInstanced(int i, int i2, int i3, int i4) {
        GLES30.glDrawArraysInstanced(i, i2, i3, i4);
    }

    public static void glDrawElementsInstanced(int i, int i2, int i3, Buffer buffer, int i4) {
        GLES30.glDrawElementsInstanced(i, i2, i3, buffer, i4);
    }

    public static boolean glIsSync(long j) {
        return GLES30.glIsSync(j);
    }

    public static void glDeleteSync(long j) {
        GLES30.glDeleteSync(j);
    }

    public static int glClientWaitSync(long j, int i, long j2) {
        return GLES30.glClientWaitSync(j, i, j2);
    }

    public static void glGetInteger64v(int i, long[] jArr) {
        GLES30.glGetInteger64v(i, jArr, 0);
    }

    public static void glGetSynciv(long j, int i, int i2, int[] iArr, int[] iArr2) {
        GLES30.glGetSynciv(j, i, i2, iArr, 0, iArr2, 0);
    }

    public static void glGetInteger64iV(int i, int i2, long[] jArr) {
        GLES30.glGetInteger64i_v(i, i2, jArr, 0);
    }

    public static void glGetBufferParameteri64v(int i, int i2, long[] jArr) {
        GLES30.glGetBufferParameteri64v(i, i2, jArr, 0);
    }

    public static void glGenSamplers(int i, int[] iArr) {
        GLES30.glGenSamplers(i, iArr, 0);
    }

    public static void glDeleteSamplers(int i, int[] iArr) {
        GLES30.glDeleteSamplers(i, iArr, 0);
    }

    public static boolean glIsSampler(int i) {
        return GLES30.glIsSampler(i);
    }

    public static void glBindSampler(int i, int i2) {
        GLES30.glBindSampler(i, i2);
    }
}
