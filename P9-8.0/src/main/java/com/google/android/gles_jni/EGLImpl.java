package com.google.android.gles_jni;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class EGLImpl implements EGL10 {
    private EGLContextImpl mContext = new EGLContextImpl(-1);
    private EGLDisplayImpl mDisplay = new EGLDisplayImpl(-1);
    private EGLSurfaceImpl mSurface = new EGLSurfaceImpl(-1);

    private native long _eglCreateContext(EGLDisplay eGLDisplay, EGLConfig eGLConfig, EGLContext eGLContext, int[] iArr);

    private native long _eglCreatePbufferSurface(EGLDisplay eGLDisplay, EGLConfig eGLConfig, int[] iArr);

    private native void _eglCreatePixmapSurface(EGLSurface eGLSurface, EGLDisplay eGLDisplay, EGLConfig eGLConfig, Object obj, int[] iArr);

    private native long _eglCreateWindowSurface(EGLDisplay eGLDisplay, EGLConfig eGLConfig, Object obj, int[] iArr);

    private native long _eglCreateWindowSurfaceTexture(EGLDisplay eGLDisplay, EGLConfig eGLConfig, Object obj, int[] iArr);

    private native long _eglGetCurrentContext();

    private native long _eglGetCurrentDisplay();

    private native long _eglGetCurrentSurface(int i);

    private native long _eglGetDisplay(Object obj);

    private static native void _nativeClassInit();

    public static native int getInitCount(EGLDisplay eGLDisplay);

    public native boolean eglChooseConfig(EGLDisplay eGLDisplay, int[] iArr, EGLConfig[] eGLConfigArr, int i, int[] iArr2);

    public native boolean eglCopyBuffers(EGLDisplay eGLDisplay, EGLSurface eGLSurface, Object obj);

    public native boolean eglDestroyContext(EGLDisplay eGLDisplay, EGLContext eGLContext);

    public native boolean eglDestroySurface(EGLDisplay eGLDisplay, EGLSurface eGLSurface);

    public native boolean eglGetConfigAttrib(EGLDisplay eGLDisplay, EGLConfig eGLConfig, int i, int[] iArr);

    public native boolean eglGetConfigs(EGLDisplay eGLDisplay, EGLConfig[] eGLConfigArr, int i, int[] iArr);

    public native int eglGetError();

    public native boolean eglInitialize(EGLDisplay eGLDisplay, int[] iArr);

    public native boolean eglMakeCurrent(EGLDisplay eGLDisplay, EGLSurface eGLSurface, EGLSurface eGLSurface2, EGLContext eGLContext);

    public native boolean eglQueryContext(EGLDisplay eGLDisplay, EGLContext eGLContext, int i, int[] iArr);

    public native String eglQueryString(EGLDisplay eGLDisplay, int i);

    public native boolean eglQuerySurface(EGLDisplay eGLDisplay, EGLSurface eGLSurface, int i, int[] iArr);

    public native boolean eglReleaseThread();

    public native boolean eglSwapBuffers(EGLDisplay eGLDisplay, EGLSurface eGLSurface);

    public native boolean eglTerminate(EGLDisplay eGLDisplay);

    public native boolean eglWaitGL();

    public native boolean eglWaitNative(int i, Object obj);

    public EGLContext eglCreateContext(EGLDisplay display, EGLConfig config, EGLContext share_context, int[] attrib_list) {
        long eglContextId = _eglCreateContext(display, config, share_context, attrib_list);
        if (eglContextId == 0) {
            return EGL10.EGL_NO_CONTEXT;
        }
        return new EGLContextImpl(eglContextId);
    }

    public EGLSurface eglCreatePbufferSurface(EGLDisplay display, EGLConfig config, int[] attrib_list) {
        long eglSurfaceId = _eglCreatePbufferSurface(display, config, attrib_list);
        if (eglSurfaceId == 0) {
            return EGL10.EGL_NO_SURFACE;
        }
        return new EGLSurfaceImpl(eglSurfaceId);
    }

    public EGLSurface eglCreatePixmapSurface(EGLDisplay display, EGLConfig config, Object native_pixmap, int[] attrib_list) {
        EGLSurfaceImpl sur = new EGLSurfaceImpl();
        _eglCreatePixmapSurface(sur, display, config, native_pixmap, attrib_list);
        if (sur.mEGLSurface == 0) {
            return EGL10.EGL_NO_SURFACE;
        }
        return sur;
    }

    public EGLSurface eglCreateWindowSurface(EGLDisplay display, EGLConfig config, Object native_window, int[] attrib_list) {
        long eglSurfaceId;
        Object sur = null;
        if (native_window instanceof SurfaceView) {
            sur = ((SurfaceView) native_window).getHolder().getSurface();
        } else if (native_window instanceof SurfaceHolder) {
            sur = ((SurfaceHolder) native_window).getSurface();
        } else if (native_window instanceof Surface) {
            Surface sur2 = (Surface) native_window;
        }
        if (sur2 != null) {
            eglSurfaceId = _eglCreateWindowSurface(display, config, sur2, attrib_list);
        } else if (native_window instanceof SurfaceTexture) {
            eglSurfaceId = _eglCreateWindowSurfaceTexture(display, config, native_window, attrib_list);
        } else {
            throw new UnsupportedOperationException("eglCreateWindowSurface() can only be called with an instance of Surface, SurfaceView, SurfaceHolder or SurfaceTexture at the moment.");
        }
        if (eglSurfaceId == 0) {
            return EGL10.EGL_NO_SURFACE;
        }
        return new EGLSurfaceImpl(eglSurfaceId);
    }

    public synchronized EGLDisplay eglGetDisplay(Object native_display) {
        long value = _eglGetDisplay(native_display);
        if (value == 0) {
            return EGL10.EGL_NO_DISPLAY;
        }
        if (this.mDisplay.mEGLDisplay != value) {
            this.mDisplay = new EGLDisplayImpl(value);
        }
        return this.mDisplay;
    }

    public synchronized EGLContext eglGetCurrentContext() {
        long value = _eglGetCurrentContext();
        if (value == 0) {
            return EGL10.EGL_NO_CONTEXT;
        }
        if (this.mContext.mEGLContext != value) {
            this.mContext = new EGLContextImpl(value);
        }
        return this.mContext;
    }

    public synchronized EGLDisplay eglGetCurrentDisplay() {
        long value = _eglGetCurrentDisplay();
        if (value == 0) {
            return EGL10.EGL_NO_DISPLAY;
        }
        if (this.mDisplay.mEGLDisplay != value) {
            this.mDisplay = new EGLDisplayImpl(value);
        }
        return this.mDisplay;
    }

    public synchronized EGLSurface eglGetCurrentSurface(int readdraw) {
        long value = _eglGetCurrentSurface(readdraw);
        if (value == 0) {
            return EGL10.EGL_NO_SURFACE;
        }
        if (this.mSurface.mEGLSurface != value) {
            this.mSurface = new EGLSurfaceImpl(value);
        }
        return this.mSurface;
    }

    static {
        _nativeClassInit();
    }
}
