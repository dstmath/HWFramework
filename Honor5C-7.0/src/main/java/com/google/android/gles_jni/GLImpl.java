package com.google.android.gles_jni;

import android.app.AppGlobals;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL10Ext;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

public class GLImpl implements GL10, GL10Ext, GL11, GL11Ext, GL11ExtensionPack {
    Buffer _colorPointer;
    Buffer _matrixIndexPointerOES;
    Buffer _normalPointer;
    Buffer _pointSizePointerOES;
    Buffer _texCoordPointer;
    Buffer _vertexPointer;
    Buffer _weightPointerOES;
    private boolean haveCheckedExtensions;
    private boolean have_OES_blend_equation_separate;
    private boolean have_OES_blend_subtract;
    private boolean have_OES_framebuffer_object;
    private boolean have_OES_texture_cube_map;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.gles_jni.GLImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.gles_jni.GLImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gles_jni.GLImpl.<clinit>():void");
    }

    private static native void _nativeClassInit();

    private native void glColorPointerBounds(int i, int i2, int i3, Buffer buffer, int i4);

    private native void glMatrixIndexPointerOESBounds(int i, int i2, int i3, Buffer buffer, int i4);

    private native void glNormalPointerBounds(int i, int i2, Buffer buffer, int i3);

    private native void glPointSizePointerOESBounds(int i, int i2, Buffer buffer, int i3);

    private native void glTexCoordPointerBounds(int i, int i2, int i3, Buffer buffer, int i4);

    private native void glVertexPointerBounds(int i, int i2, int i3, Buffer buffer, int i4);

    private native void glWeightPointerOESBounds(int i, int i2, int i3, Buffer buffer, int i4);

    public native String _glGetString(int i);

    public native void glActiveTexture(int i);

    public native void glAlphaFunc(int i, float f);

    public native void glAlphaFuncx(int i, int i2);

    public native void glBindBuffer(int i, int i2);

    public native void glBindFramebufferOES(int i, int i2);

    public native void glBindRenderbufferOES(int i, int i2);

    public native void glBindTexture(int i, int i2);

    public native void glBlendEquation(int i);

    public native void glBlendEquationSeparate(int i, int i2);

    public native void glBlendFunc(int i, int i2);

    public native void glBlendFuncSeparate(int i, int i2, int i3, int i4);

    public native void glBufferData(int i, int i2, Buffer buffer, int i3);

    public native void glBufferSubData(int i, int i2, int i3, Buffer buffer);

    public native int glCheckFramebufferStatusOES(int i);

    public native void glClear(int i);

    public native void glClearColor(float f, float f2, float f3, float f4);

    public native void glClearColorx(int i, int i2, int i3, int i4);

    public native void glClearDepthf(float f);

    public native void glClearDepthx(int i);

    public native void glClearStencil(int i);

    public native void glClientActiveTexture(int i);

    public native void glClipPlanef(int i, FloatBuffer floatBuffer);

    public native void glClipPlanef(int i, float[] fArr, int i2);

    public native void glClipPlanex(int i, IntBuffer intBuffer);

    public native void glClipPlanex(int i, int[] iArr, int i2);

    public native void glColor4f(float f, float f2, float f3, float f4);

    public native void glColor4ub(byte b, byte b2, byte b3, byte b4);

    public native void glColor4x(int i, int i2, int i3, int i4);

    public native void glColorMask(boolean z, boolean z2, boolean z3, boolean z4);

    public native void glColorPointer(int i, int i2, int i3, int i4);

    public native void glCompressedTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, Buffer buffer);

    public native void glCompressedTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer);

    public native void glCopyTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8);

    public native void glCopyTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8);

    public native void glCullFace(int i);

    public native void glCurrentPaletteMatrixOES(int i);

    public native void glDeleteBuffers(int i, IntBuffer intBuffer);

    public native void glDeleteBuffers(int i, int[] iArr, int i2);

    public native void glDeleteFramebuffersOES(int i, IntBuffer intBuffer);

    public native void glDeleteFramebuffersOES(int i, int[] iArr, int i2);

    public native void glDeleteRenderbuffersOES(int i, IntBuffer intBuffer);

    public native void glDeleteRenderbuffersOES(int i, int[] iArr, int i2);

    public native void glDeleteTextures(int i, IntBuffer intBuffer);

    public native void glDeleteTextures(int i, int[] iArr, int i2);

    public native void glDepthFunc(int i);

    public native void glDepthMask(boolean z);

    public native void glDepthRangef(float f, float f2);

    public native void glDepthRangex(int i, int i2);

    public native void glDisable(int i);

    public native void glDisableClientState(int i);

    public native void glDrawArrays(int i, int i2, int i3);

    public native void glDrawElements(int i, int i2, int i3, int i4);

    public native void glDrawElements(int i, int i2, int i3, Buffer buffer);

    public native void glDrawTexfOES(float f, float f2, float f3, float f4, float f5);

    public native void glDrawTexfvOES(FloatBuffer floatBuffer);

    public native void glDrawTexfvOES(float[] fArr, int i);

    public native void glDrawTexiOES(int i, int i2, int i3, int i4, int i5);

    public native void glDrawTexivOES(IntBuffer intBuffer);

    public native void glDrawTexivOES(int[] iArr, int i);

    public native void glDrawTexsOES(short s, short s2, short s3, short s4, short s5);

    public native void glDrawTexsvOES(ShortBuffer shortBuffer);

    public native void glDrawTexsvOES(short[] sArr, int i);

    public native void glDrawTexxOES(int i, int i2, int i3, int i4, int i5);

    public native void glDrawTexxvOES(IntBuffer intBuffer);

    public native void glDrawTexxvOES(int[] iArr, int i);

    public native void glEnable(int i);

    public native void glEnableClientState(int i);

    public native void glFinish();

    public native void glFlush();

    public native void glFogf(int i, float f);

    public native void glFogfv(int i, FloatBuffer floatBuffer);

    public native void glFogfv(int i, float[] fArr, int i2);

    public native void glFogx(int i, int i2);

    public native void glFogxv(int i, IntBuffer intBuffer);

    public native void glFogxv(int i, int[] iArr, int i2);

    public native void glFramebufferRenderbufferOES(int i, int i2, int i3, int i4);

    public native void glFramebufferTexture2DOES(int i, int i2, int i3, int i4, int i5);

    public native void glFrontFace(int i);

    public native void glFrustumf(float f, float f2, float f3, float f4, float f5, float f6);

    public native void glFrustumx(int i, int i2, int i3, int i4, int i5, int i6);

    public native void glGenBuffers(int i, IntBuffer intBuffer);

    public native void glGenBuffers(int i, int[] iArr, int i2);

    public native void glGenFramebuffersOES(int i, IntBuffer intBuffer);

    public native void glGenFramebuffersOES(int i, int[] iArr, int i2);

    public native void glGenRenderbuffersOES(int i, IntBuffer intBuffer);

    public native void glGenRenderbuffersOES(int i, int[] iArr, int i2);

    public native void glGenTextures(int i, IntBuffer intBuffer);

    public native void glGenTextures(int i, int[] iArr, int i2);

    public native void glGenerateMipmapOES(int i);

    public native void glGetBooleanv(int i, IntBuffer intBuffer);

    public native void glGetBooleanv(int i, boolean[] zArr, int i2);

    public native void glGetBufferParameteriv(int i, int i2, IntBuffer intBuffer);

    public native void glGetBufferParameteriv(int i, int i2, int[] iArr, int i3);

    public native void glGetClipPlanef(int i, FloatBuffer floatBuffer);

    public native void glGetClipPlanef(int i, float[] fArr, int i2);

    public native void glGetClipPlanex(int i, IntBuffer intBuffer);

    public native void glGetClipPlanex(int i, int[] iArr, int i2);

    public native int glGetError();

    public native void glGetFixedv(int i, IntBuffer intBuffer);

    public native void glGetFixedv(int i, int[] iArr, int i2);

    public native void glGetFloatv(int i, FloatBuffer floatBuffer);

    public native void glGetFloatv(int i, float[] fArr, int i2);

    public native void glGetFramebufferAttachmentParameterivOES(int i, int i2, int i3, IntBuffer intBuffer);

    public native void glGetFramebufferAttachmentParameterivOES(int i, int i2, int i3, int[] iArr, int i4);

    public native void glGetIntegerv(int i, IntBuffer intBuffer);

    public native void glGetIntegerv(int i, int[] iArr, int i2);

    public native void glGetLightfv(int i, int i2, FloatBuffer floatBuffer);

    public native void glGetLightfv(int i, int i2, float[] fArr, int i3);

    public native void glGetLightxv(int i, int i2, IntBuffer intBuffer);

    public native void glGetLightxv(int i, int i2, int[] iArr, int i3);

    public native void glGetMaterialfv(int i, int i2, FloatBuffer floatBuffer);

    public native void glGetMaterialfv(int i, int i2, float[] fArr, int i3);

    public native void glGetMaterialxv(int i, int i2, IntBuffer intBuffer);

    public native void glGetMaterialxv(int i, int i2, int[] iArr, int i3);

    public native void glGetRenderbufferParameterivOES(int i, int i2, IntBuffer intBuffer);

    public native void glGetRenderbufferParameterivOES(int i, int i2, int[] iArr, int i3);

    public native void glGetTexEnviv(int i, int i2, IntBuffer intBuffer);

    public native void glGetTexEnviv(int i, int i2, int[] iArr, int i3);

    public native void glGetTexEnvxv(int i, int i2, IntBuffer intBuffer);

    public native void glGetTexEnvxv(int i, int i2, int[] iArr, int i3);

    public native void glGetTexGenfv(int i, int i2, FloatBuffer floatBuffer);

    public native void glGetTexGenfv(int i, int i2, float[] fArr, int i3);

    public native void glGetTexGeniv(int i, int i2, IntBuffer intBuffer);

    public native void glGetTexGeniv(int i, int i2, int[] iArr, int i3);

    public native void glGetTexGenxv(int i, int i2, IntBuffer intBuffer);

    public native void glGetTexGenxv(int i, int i2, int[] iArr, int i3);

    public native void glGetTexParameterfv(int i, int i2, FloatBuffer floatBuffer);

    public native void glGetTexParameterfv(int i, int i2, float[] fArr, int i3);

    public native void glGetTexParameteriv(int i, int i2, IntBuffer intBuffer);

    public native void glGetTexParameteriv(int i, int i2, int[] iArr, int i3);

    public native void glGetTexParameterxv(int i, int i2, IntBuffer intBuffer);

    public native void glGetTexParameterxv(int i, int i2, int[] iArr, int i3);

    public native void glHint(int i, int i2);

    public native boolean glIsBuffer(int i);

    public native boolean glIsEnabled(int i);

    public native boolean glIsFramebufferOES(int i);

    public native boolean glIsRenderbufferOES(int i);

    public native boolean glIsTexture(int i);

    public native void glLightModelf(int i, float f);

    public native void glLightModelfv(int i, FloatBuffer floatBuffer);

    public native void glLightModelfv(int i, float[] fArr, int i2);

    public native void glLightModelx(int i, int i2);

    public native void glLightModelxv(int i, IntBuffer intBuffer);

    public native void glLightModelxv(int i, int[] iArr, int i2);

    public native void glLightf(int i, int i2, float f);

    public native void glLightfv(int i, int i2, FloatBuffer floatBuffer);

    public native void glLightfv(int i, int i2, float[] fArr, int i3);

    public native void glLightx(int i, int i2, int i3);

    public native void glLightxv(int i, int i2, IntBuffer intBuffer);

    public native void glLightxv(int i, int i2, int[] iArr, int i3);

    public native void glLineWidth(float f);

    public native void glLineWidthx(int i);

    public native void glLoadIdentity();

    public native void glLoadMatrixf(FloatBuffer floatBuffer);

    public native void glLoadMatrixf(float[] fArr, int i);

    public native void glLoadMatrixx(IntBuffer intBuffer);

    public native void glLoadMatrixx(int[] iArr, int i);

    public native void glLoadPaletteFromModelViewMatrixOES();

    public native void glLogicOp(int i);

    public native void glMaterialf(int i, int i2, float f);

    public native void glMaterialfv(int i, int i2, FloatBuffer floatBuffer);

    public native void glMaterialfv(int i, int i2, float[] fArr, int i3);

    public native void glMaterialx(int i, int i2, int i3);

    public native void glMaterialxv(int i, int i2, IntBuffer intBuffer);

    public native void glMaterialxv(int i, int i2, int[] iArr, int i3);

    public native void glMatrixIndexPointerOES(int i, int i2, int i3, int i4);

    public native void glMatrixMode(int i);

    public native void glMultMatrixf(FloatBuffer floatBuffer);

    public native void glMultMatrixf(float[] fArr, int i);

    public native void glMultMatrixx(IntBuffer intBuffer);

    public native void glMultMatrixx(int[] iArr, int i);

    public native void glMultiTexCoord4f(int i, float f, float f2, float f3, float f4);

    public native void glMultiTexCoord4x(int i, int i2, int i3, int i4, int i5);

    public native void glNormal3f(float f, float f2, float f3);

    public native void glNormal3x(int i, int i2, int i3);

    public native void glNormalPointer(int i, int i2, int i3);

    public native void glOrthof(float f, float f2, float f3, float f4, float f5, float f6);

    public native void glOrthox(int i, int i2, int i3, int i4, int i5, int i6);

    public native void glPixelStorei(int i, int i2);

    public native void glPointParameterf(int i, float f);

    public native void glPointParameterfv(int i, FloatBuffer floatBuffer);

    public native void glPointParameterfv(int i, float[] fArr, int i2);

    public native void glPointParameterx(int i, int i2);

    public native void glPointParameterxv(int i, IntBuffer intBuffer);

    public native void glPointParameterxv(int i, int[] iArr, int i2);

    public native void glPointSize(float f);

    public native void glPointSizex(int i);

    public native void glPolygonOffset(float f, float f2);

    public native void glPolygonOffsetx(int i, int i2);

    public native void glPopMatrix();

    public native void glPushMatrix();

    public native int glQueryMatrixxOES(IntBuffer intBuffer, IntBuffer intBuffer2);

    public native int glQueryMatrixxOES(int[] iArr, int i, int[] iArr2, int i2);

    public native void glReadPixels(int i, int i2, int i3, int i4, int i5, int i6, Buffer buffer);

    public native void glRenderbufferStorageOES(int i, int i2, int i3, int i4);

    public native void glRotatef(float f, float f2, float f3, float f4);

    public native void glRotatex(int i, int i2, int i3, int i4);

    public native void glSampleCoverage(float f, boolean z);

    public native void glSampleCoveragex(int i, boolean z);

    public native void glScalef(float f, float f2, float f3);

    public native void glScalex(int i, int i2, int i3);

    public native void glScissor(int i, int i2, int i3, int i4);

    public native void glShadeModel(int i);

    public native void glStencilFunc(int i, int i2, int i3);

    public native void glStencilMask(int i);

    public native void glStencilOp(int i, int i2, int i3);

    public native void glTexCoordPointer(int i, int i2, int i3, int i4);

    public native void glTexEnvf(int i, int i2, float f);

    public native void glTexEnvfv(int i, int i2, FloatBuffer floatBuffer);

    public native void glTexEnvfv(int i, int i2, float[] fArr, int i3);

    public native void glTexEnvi(int i, int i2, int i3);

    public native void glTexEnviv(int i, int i2, IntBuffer intBuffer);

    public native void glTexEnviv(int i, int i2, int[] iArr, int i3);

    public native void glTexEnvx(int i, int i2, int i3);

    public native void glTexEnvxv(int i, int i2, IntBuffer intBuffer);

    public native void glTexEnvxv(int i, int i2, int[] iArr, int i3);

    public native void glTexGenf(int i, int i2, float f);

    public native void glTexGenfv(int i, int i2, FloatBuffer floatBuffer);

    public native void glTexGenfv(int i, int i2, float[] fArr, int i3);

    public native void glTexGeni(int i, int i2, int i3);

    public native void glTexGeniv(int i, int i2, IntBuffer intBuffer);

    public native void glTexGeniv(int i, int i2, int[] iArr, int i3);

    public native void glTexGenx(int i, int i2, int i3);

    public native void glTexGenxv(int i, int i2, IntBuffer intBuffer);

    public native void glTexGenxv(int i, int i2, int[] iArr, int i3);

    public native void glTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer);

    public native void glTexParameterf(int i, int i2, float f);

    public native void glTexParameterfv(int i, int i2, FloatBuffer floatBuffer);

    public native void glTexParameterfv(int i, int i2, float[] fArr, int i3);

    public native void glTexParameteri(int i, int i2, int i3);

    public native void glTexParameteriv(int i, int i2, IntBuffer intBuffer);

    public native void glTexParameteriv(int i, int i2, int[] iArr, int i3);

    public native void glTexParameterx(int i, int i2, int i3);

    public native void glTexParameterxv(int i, int i2, IntBuffer intBuffer);

    public native void glTexParameterxv(int i, int i2, int[] iArr, int i3);

    public native void glTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer);

    public native void glTranslatef(float f, float f2, float f3);

    public native void glTranslatex(int i, int i2, int i3);

    public native void glVertexPointer(int i, int i2, int i3, int i4);

    public native void glViewport(int i, int i2, int i3, int i4);

    public native void glWeightPointerOES(int i, int i2, int i3, int i4);

    public GLImpl() {
        this._colorPointer = null;
        this._normalPointer = null;
        this._texCoordPointer = null;
        this._vertexPointer = null;
        this._pointSizePointerOES = null;
        this._matrixIndexPointerOES = null;
        this._weightPointerOES = null;
    }

    public void glGetPointerv(int pname, Buffer[] params) {
        throw new UnsupportedOperationException("glGetPointerv");
    }

    private static boolean allowIndirectBuffers(String appName) {
        int version = 0;
        try {
            ApplicationInfo applicationInfo = AppGlobals.getPackageManager().getApplicationInfo(appName, 0, UserHandle.myUserId());
            if (applicationInfo != null) {
                version = applicationInfo.targetSdkVersion;
            }
        } catch (RemoteException e) {
        }
        Log.e("OpenGLES", String.format("Application %s (SDK target %d) called a GL11 Pointer method with an indirect Buffer.", new Object[]{appName, Integer.valueOf(version)}));
        if (version <= 3) {
            return true;
        }
        return false;
    }

    public void glColorPointer(int size, int type, int stride, Buffer pointer) {
        glColorPointerBounds(size, type, stride, pointer, pointer.remaining());
        if (size == 4) {
            if (!(type == GL10.GL_FLOAT || type == GL10.GL_UNSIGNED_BYTE)) {
                if (type != GL10.GL_FIXED) {
                    return;
                }
            }
            if (stride >= 0) {
                this._colorPointer = pointer;
            }
        }
    }

    public String glGetString(int name) {
        String returnValue = _glGetString(name);
        if (Build.HIDE_PRODUCT_INFO && (name == GL10.GL_RENDERER || name == GL10.GL_VENDOR)) {
            return Build.hide_build_info("gpu", returnValue);
        }
        return returnValue;
    }

    public void glNormalPointer(int type, int stride, Buffer pointer) {
        glNormalPointerBounds(type, stride, pointer, pointer.remaining());
        if (!(type == GL10.GL_FLOAT || type == GL10.GL_BYTE || type == GL10.GL_SHORT)) {
            if (type != GL10.GL_FIXED) {
                return;
            }
        }
        if (stride >= 0) {
            this._normalPointer = pointer;
        }
    }

    public void glTexCoordPointer(int size, int type, int stride, Buffer pointer) {
        glTexCoordPointerBounds(size, type, stride, pointer, pointer.remaining());
        if (!(size == 2 || size == 3)) {
            if (size != 4) {
                return;
            }
        }
        if (!(type == GL10.GL_FLOAT || type == GL10.GL_BYTE || type == GL10.GL_SHORT)) {
            if (type != GL10.GL_FIXED) {
                return;
            }
        }
        if (stride >= 0) {
            this._texCoordPointer = pointer;
        }
    }

    public void glVertexPointer(int size, int type, int stride, Buffer pointer) {
        glVertexPointerBounds(size, type, stride, pointer, pointer.remaining());
        if (!(size == 2 || size == 3)) {
            if (size != 4) {
                return;
            }
        }
        if (!(type == GL10.GL_FLOAT || type == GL10.GL_BYTE || type == GL10.GL_SHORT)) {
            if (type != GL10.GL_FIXED) {
                return;
            }
        }
        if (stride >= 0) {
            this._vertexPointer = pointer;
        }
    }

    public void glPointSizePointerOES(int type, int stride, Buffer pointer) {
        glPointSizePointerOESBounds(type, stride, pointer, pointer.remaining());
        if ((type == GL10.GL_FLOAT || type == GL10.GL_FIXED) && stride >= 0) {
            this._pointSizePointerOES = pointer;
        }
    }

    public void glMatrixIndexPointerOES(int size, int type, int stride, Buffer pointer) {
        glMatrixIndexPointerOESBounds(size, type, stride, pointer, pointer.remaining());
        if (!(size == 2 || size == 3)) {
            if (size != 4) {
                return;
            }
        }
        if (!(type == GL10.GL_FLOAT || type == GL10.GL_BYTE || type == GL10.GL_SHORT)) {
            if (type != GL10.GL_FIXED) {
                return;
            }
        }
        if (stride >= 0) {
            this._matrixIndexPointerOES = pointer;
        }
    }

    public void glWeightPointerOES(int size, int type, int stride, Buffer pointer) {
        glWeightPointerOESBounds(size, type, stride, pointer, pointer.remaining());
    }
}
