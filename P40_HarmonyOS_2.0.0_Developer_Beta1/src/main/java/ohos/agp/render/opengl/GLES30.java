package ohos.agp.render.opengl;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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

    public static void glWaitSync(long j, int i, long j2) {
        GLES30Adapter.glWaitSync(j, i, j2);
    }

    public static void glTransformFeedbackVaryings(int i, int i2, String[] strArr, int i3) {
        GLES30Adapter.glTransformFeedbackVaryings(i, i2, strArr, i3);
    }

    public static void glReadPixels(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        GLES30Adapter.glReadPixels(i, i2, i3, i4, i5, i6, i7);
    }

    public static void glReadBuffer(int i) {
        GLES30Adapter.glReadBuffer(i);
    }

    public static Buffer glMapBufferRange(int i, int i2, int i3, int i4) {
        return GLES30Adapter.glMapBufferRange(i, i2, i3, i4);
    }

    public static long glFenceSync(int i, int i2) {
        return GLES30Adapter.glFenceSync(i, i2);
    }

    public static void glBeginTransformFeedback(int i) {
        GLES30Adapter.glBeginTransformFeedback(i);
    }

    public static void glEndTransformFeedback() {
        GLES30Adapter.glEndTransformFeedback();
    }

    public static void glDeleteVertexArrays(int i, int[] iArr) {
        GLES30Adapter.glDeleteVertexArrays(i, iArr);
    }

    public static void glBindBufferBase(int i, int i2, int i3) {
        GLES30Adapter.glBindBufferBase(i, i2, i3);
    }

    public static boolean glUnmapBuffer(int i) {
        return GLES30Adapter.glUnmapBuffer(i);
    }

    public static void glDrawRangeElements(int i, int i2, int i3, int i4, int i5, Buffer buffer) {
        GLES30Adapter.glDrawRangeElements(i, i2, i3, i4, i5, buffer);
    }

    public static void glTexImage3D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, Buffer buffer) {
        GLES30Adapter.glTexImage3D(i, i2, i3, i4, i5, i6, i7, i8, i9, buffer);
    }

    public static void glTexSubImage3D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, Buffer buffer) {
        GLES30Adapter.glTexSubImage3D(i, i2, i3, i4, i5, i6, i7, i8, i9, i10, buffer);
    }

    public static void glCopyTexSubImage3D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
        GLES30Adapter.glCopyTexSubImage3D(i, i2, i3, i4, i5, i6, i7, i8, i9);
    }

    public static void glCompressedTexImage3D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        GLES30Adapter.glCompressedTexImage3D(i, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    public static void glCompressedTexSubImage3D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, Buffer buffer) {
        GLES30Adapter.glCompressedTexSubImage3D(i, i2, i3, i4, i5, i6, i7, i8, i9, i10, buffer);
    }

    public static void glGenQueries(int i, IntBuffer intBuffer) {
        GLES30Adapter.glGenQueries(i, intBuffer);
    }

    public static void glDeleteQueries(int i, IntBuffer intBuffer) {
        GLES30Adapter.glDeleteQueries(i, intBuffer);
    }

    public static boolean glIsQuery(int i) {
        return GLES30Adapter.glIsQuery(i);
    }

    public static void glBeginQuery(int i, int i2) {
        GLES30Adapter.glBeginQuery(i, i2);
    }

    public static void glEndQuery(int i) {
        GLES30Adapter.glEndQuery(i);
    }

    public static void glGetQueryiv(int i, int i2, IntBuffer intBuffer) {
        GLES30Adapter.glGetQueryiv(i, i2, intBuffer);
    }

    public static void glGetQueryObjectuiv(int i, int i2, IntBuffer intBuffer) {
        GLES30Adapter.glGetQueryObjectuiv(i, i2, intBuffer);
    }

    public static void glGetBufferPointerv(int i, int i2, Buffer buffer) {
        GLES30Adapter.glGetBufferPointerv(i, i2, buffer);
    }

    public static void glBindBufferRange(int i, int i2, int i3, int i4, int i5) {
        GLES30Adapter.glBindBufferRange(i, i2, i3, i4, i5);
    }

    public static void glGetTransformFeedbackVarying(int i, int i2, int i3, int[] iArr, int[] iArr2, int[] iArr3, byte[] bArr) {
        GLES30Adapter.glGetTransformFeedbackVarying(i, i2, i3, iArr, iArr2, iArr3, bArr);
    }

    public static void glVertexAttribIPointer(int i, int i2, int i3, int i4, int i5) {
        GLES30Adapter.glVertexAttribIPointer(i, i2, i3, i4, i5);
    }

    public static void glGetVertexAttribIiv(int i, int i2, int[] iArr) {
        GLES30Adapter.glGetVertexAttribIiv(i, i2, iArr);
    }

    public static void glGetVertexAttribIuiv(int i, int i2, int[] iArr) {
        GLES30Adapter.glGetVertexAttribIuiv(i, i2, iArr);
    }

    public static void glVertexAttribI4i(int i, int i2, int i3, int i4, int i5) {
        GLES30Adapter.glVertexAttribI4i(i, i2, i3, i4, i5);
    }

    public static void glVertexAttribI4ui(int i, int i2, int i3, int i4, int i5) {
        GLES30Adapter.glVertexAttribI4ui(i, i2, i3, i4, i5);
    }

    public static void glVertexAttribI4iv(int i, int[] iArr) {
        GLES30Adapter.glVertexAttribI4iv(i, iArr);
    }

    public static void glVertexAttribI4uiv(int i, int[] iArr) {
        GLES30Adapter.glVertexAttribI4uiv(i, iArr);
    }

    public static void glGetUniformuiv(int i, int i2, int[] iArr) {
        GLES30Adapter.glGetUniformuiv(i, i2, iArr);
    }

    public static int glGetFragDataLocation(int i, String str) {
        return GLES30Adapter.glGetFragDataLocation(i, str);
    }

    public static void glUniform1ui(int i, int i2) {
        GLES30Adapter.glUniform1ui(i, i2);
    }

    public static void glUniform2ui(int i, int i2, int i3) {
        GLES30Adapter.glUniform2ui(i, i2, i3);
    }

    public static void glUniformMatrix2x3fv(int i, int i2, boolean z, FloatBuffer floatBuffer) {
        GLES30Adapter.glUniformMatrix2x3fv(i, i2, z, floatBuffer);
    }

    public static void glUniform3ui(int i, int i2, int i3, int i4) {
        GLES30Adapter.glUniform3ui(i, i2, i3, i4);
    }

    public static void glUniformMatrix3x2fv(int i, int i2, boolean z, FloatBuffer floatBuffer) {
        GLES30Adapter.glUniformMatrix3x2fv(i, i2, z, floatBuffer);
    }

    public static void glUniformMatrix2x4fv(int i, int i2, boolean z, FloatBuffer floatBuffer) {
        GLES30Adapter.glUniformMatrix2x4fv(i, i2, z, floatBuffer);
    }

    public static void glUniform4ui(int i, int i2, int i3, int i4, int i5) {
        GLES30Adapter.glUniform4ui(i, i2, i3, i4, i5);
    }

    public static void glUniform1uiv(int i, int i2, int[] iArr) {
        GLES30Adapter.glUniform1uiv(i, i2, iArr);
    }

    public static void glUniformMatrix4x2fv(int i, int i2, boolean z, FloatBuffer floatBuffer) {
        GLES30Adapter.glUniformMatrix4x2fv(i, i2, z, floatBuffer);
    }

    public static void glUniform2uiv(int i, int i2, int[] iArr) {
        GLES30Adapter.glUniform2uiv(i, i2, iArr);
    }

    public static void glUniformMatrix3x4fv(int i, int i2, boolean z, FloatBuffer floatBuffer) {
        GLES30Adapter.glUniformMatrix3x4fv(i, i2, z, floatBuffer);
    }

    public static void glUniform3uiv(int i, int i2, int[] iArr) {
        GLES30Adapter.glUniform3uiv(i, i2, iArr);
    }

    public static void glUniformMatrix4x3fv(int i, int i2, boolean z, FloatBuffer floatBuffer) {
        GLES30Adapter.glUniformMatrix4x3fv(i, i2, z, floatBuffer);
    }

    public static void glFramebufferTextureLayer(int i, int i2, int i3, int i4, int i5) {
        GLES30Adapter.glFramebufferTextureLayer(i, i2, i3, i4, i5);
    }

    public static void glFlushMappedBufferRange(int i, int i2, int i3) {
        GLES30Adapter.glFlushMappedBufferRange(i, i2, i3);
    }

    public static boolean glIsVertexArray(int i) {
        return GLES30Adapter.glIsVertexArray(i);
    }

    public static void glGetIntegerIndexv(int i, int i2, IntBuffer intBuffer) {
        GLES30Adapter.glGetIntegerIndexv(i, i2, intBuffer);
    }

    public static void glSamplerParameteri(int i, int i2, int i3) {
        GLES30Adapter.glSamplerParameteri(i, i2, i3);
    }

    public static void glSamplerParameteriv(int i, int i2, int[] iArr) {
        GLES30Adapter.glSamplerParameteriv(i, i2, iArr);
    }

    public static void glSamplerParameterf(int i, int i2, float f) {
        GLES30Adapter.glSamplerParameterf(i, i2, f);
    }

    public static void glSamplerParameterfv(int i, int i2, float[] fArr) {
        GLES30Adapter.glSamplerParameterfv(i, i2, fArr);
    }

    public static void glGetSamplerParameteriv(int i, int i2, int[] iArr) {
        GLES30Adapter.glGetSamplerParameteriv(i, i2, iArr);
    }

    public static void glGetSamplerParameterfv(int i, int i2, float[] fArr) {
        GLES30Adapter.glGetSamplerParameterfv(i, i2, fArr);
    }

    public static void glVertexAttribDivisor(int i, int i2) {
        GLES30Adapter.glVertexAttribDivisor(i, i2);
    }

    public static void glBindTransformFeedback(int i, int i2) {
        GLES30Adapter.glBindTransformFeedback(i, i2);
    }

    public static void glDeleteTransformFeedbacks(int i, int[] iArr) {
        GLES30Adapter.glDeleteTransformFeedbacks(i, iArr);
    }

    public static void glGenTransformFeedbacks(int i, int[] iArr) {
        GLES30Adapter.glGenTransformFeedbacks(i, iArr);
    }

    public static boolean glIsTransformFeedback(int i) {
        return GLES30Adapter.glIsTransformFeedback(i);
    }

    public static void glPauseTransformFeedback() {
        GLES30Adapter.glPauseTransformFeedback();
    }

    public static void glResumeTransformFeedback() {
        GLES30Adapter.glResumeTransformFeedback();
    }

    public static void glGetProgramBinary(int i, int i2, int[] iArr, int[] iArr2, Buffer buffer) {
        GLES30Adapter.glGetProgramBinary(i, i2, iArr, iArr2, buffer);
    }

    public static void glProgramBinary(int i, int i2, Buffer buffer, int i3) {
        GLES30Adapter.glProgramBinary(i, i2, buffer, i3);
    }

    public static void glProgramParameteri(int i, int i2, int i3) {
        GLES30Adapter.glProgramParameteri(i, i2, i3);
    }

    public static void glInvalidateFramebuffer(int i, int i2, int[] iArr) {
        GLES30Adapter.glInvalidateFramebuffer(i, i2, iArr);
    }

    public static void glInvalidateSubFramebuffer(int i, int i2, int[] iArr, int i3, int i4, int i5, int i6) {
        GLES30Adapter.glInvalidateSubFramebuffer(i, i2, iArr, i3, i4, i5, i6);
    }

    public static void glTexStorage2D(int i, int i2, int i3, int i4, int i5) {
        GLES30Adapter.glTexStorage2D(i, i2, i3, i4, i5);
    }

    public static void glTexStorage3D(int i, int i2, int i3, int i4, int i5, int i6) {
        GLES30Adapter.glTexStorage3D(i, i2, i3, i4, i5, i6);
    }

    public static void glGetInternalformativ(int i, int i2, int i3, int i4, int[] iArr) {
        GLES30Adapter.glGetInternalformativ(i, i2, i3, i4, iArr);
    }

    public static void glUniform4uiv(int i, int i2, int[] iArr) {
        GLES30Adapter.glUniform4uiv(i, i2, iArr);
    }

    public static void glClearBufferiv(int i, int i2, int[] iArr) {
        GLES30Adapter.glClearBufferiv(i, i2, iArr);
    }

    public static void glClearBufferuiv(int i, int i2, int[] iArr) {
        GLES30Adapter.glClearBufferuiv(i, i2, iArr);
    }

    public static void glClearBufferfv(int i, int i2, float[] fArr) {
        GLES30Adapter.glClearBufferfv(i, i2, fArr);
    }

    public static void glClearBufferfi(int i, int i2, float f, int i3) {
        GLES30Adapter.glClearBufferfi(i, i2, f, i3);
    }

    public static String glGetStringi(int i, int i2) {
        return GLES30Adapter.glGetStringi(i, i2);
    }

    public static void glCopyBufferSubData(int i, int i2, int i3, int i4, int i5) {
        GLES30Adapter.glCopyBufferSubData(i, i2, i3, i4, i5);
    }

    public static void glGetUniformIndices(int i, String[] strArr, int[] iArr) {
        GLES30Adapter.glGetUniformIndices(i, strArr, iArr);
    }

    public static void glGetActiveUniformsiv(int i, int i2, int[] iArr, int i3, int[] iArr2) {
        GLES30Adapter.glGetActiveUniformsiv(i, i2, iArr, i3, iArr2);
    }

    public static int glGetUniformBlockIndex(int i, String str) {
        return GLES30Adapter.glGetUniformBlockIndex(i, str);
    }

    public static void glGetActiveUniformBlockiv(int i, int i2, int i3, int[] iArr) {
        GLES30Adapter.glGetActiveUniformBlockiv(i, i2, i3, iArr);
    }

    public static void glGetActiveUniformBlockName(int i, int i2, int i3, int[] iArr, byte[] bArr) {
        GLES30Adapter.glGetActiveUniformBlockName(i, i2, i3, iArr, bArr);
    }

    public static void glUniformBlockBinding(int i, int i2, int i3) {
        GLES30Adapter.glUniformBlockBinding(i, i2, i3);
    }

    public static void glDrawArraysInstanced(int i, int i2, int i3, int i4) {
        GLES30Adapter.glDrawArraysInstanced(i, i2, i3, i4);
    }

    public static void glDrawElementsInstanced(int i, int i2, int i3, Buffer buffer, int i4) {
        GLES30Adapter.glDrawElementsInstanced(i, i2, i3, buffer, i4);
    }

    public static boolean glIsSync(long j) {
        return GLES30Adapter.glIsSync(j);
    }

    public static void glDeleteSync(long j) {
        GLES30Adapter.glDeleteSync(j);
    }

    public static int glClientWaitSync(long j, int i, long j2) {
        return GLES30Adapter.glClientWaitSync(j, i, j2);
    }

    public static void glGetInteger64v(int i, long[] jArr) {
        GLES30Adapter.glGetInteger64v(i, jArr);
    }

    public static void glGetSynciv(long j, int i, int i2, int[] iArr, int[] iArr2) {
        GLES30Adapter.glGetSynciv(j, i, i2, iArr, iArr2);
    }

    public static void glGetInteger64iV(int i, int i2, long[] jArr) {
        GLES30Adapter.glGetInteger64iV(i, i2, jArr);
    }

    public static void glGetBufferParameteri64v(int i, int i2, long[] jArr) {
        GLES30Adapter.glGetBufferParameteri64v(i, i2, jArr);
    }

    public static void glGenSamplers(int i, int[] iArr) {
        GLES30Adapter.glGenSamplers(i, iArr);
    }

    public static void glDeleteSamplers(int i, int[] iArr) {
        GLES30Adapter.glDeleteSamplers(i, iArr);
    }

    public static boolean glIsSampler(int i) {
        return GLES30Adapter.glIsSampler(i);
    }

    public static void glBindSampler(int i, int i2) {
        GLES30Adapter.glBindSampler(i, i2);
    }
}
