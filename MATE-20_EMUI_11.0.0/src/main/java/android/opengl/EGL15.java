package android.opengl;

import java.nio.Buffer;

public final class EGL15 {
    public static final int EGL_CL_EVENT_HANDLE = 12444;
    public static final int EGL_CONDITION_SATISFIED = 12534;
    public static final int EGL_CONTEXT_MAJOR_VERSION = 12440;
    public static final int EGL_CONTEXT_MINOR_VERSION = 12539;
    public static final int EGL_CONTEXT_OPENGL_COMPATIBILITY_PROFILE_BIT = 2;
    public static final int EGL_CONTEXT_OPENGL_CORE_PROFILE_BIT = 1;
    public static final int EGL_CONTEXT_OPENGL_DEBUG = 12720;
    public static final int EGL_CONTEXT_OPENGL_FORWARD_COMPATIBLE = 12721;
    public static final int EGL_CONTEXT_OPENGL_PROFILE_MASK = 12541;
    public static final int EGL_CONTEXT_OPENGL_RESET_NOTIFICATION_STRATEGY = 12733;
    public static final int EGL_CONTEXT_OPENGL_ROBUST_ACCESS = 12722;
    public static final long EGL_FOREVER = -1;
    public static final int EGL_GL_COLORSPACE = 12445;
    public static final int EGL_GL_COLORSPACE_LINEAR = 12426;
    public static final int EGL_GL_COLORSPACE_SRGB = 12425;
    public static final int EGL_GL_RENDERBUFFER = 12473;
    public static final int EGL_GL_TEXTURE_2D = 12465;
    public static final int EGL_GL_TEXTURE_3D = 12466;
    public static final int EGL_GL_TEXTURE_CUBE_MAP_NEGATIVE_X = 12468;
    public static final int EGL_GL_TEXTURE_CUBE_MAP_NEGATIVE_Y = 12470;
    public static final int EGL_GL_TEXTURE_CUBE_MAP_NEGATIVE_Z = 12472;
    public static final int EGL_GL_TEXTURE_CUBE_MAP_POSITIVE_X = 12467;
    public static final int EGL_GL_TEXTURE_CUBE_MAP_POSITIVE_Y = 12469;
    public static final int EGL_GL_TEXTURE_CUBE_MAP_POSITIVE_Z = 12471;
    public static final int EGL_GL_TEXTURE_LEVEL = 12476;
    public static final int EGL_GL_TEXTURE_ZOFFSET = 12477;
    public static final int EGL_IMAGE_PRESERVED = 12498;
    public static final int EGL_LOSE_CONTEXT_ON_RESET = 12735;
    public static final EGLContext EGL_NO_CONTEXT = null;
    public static final EGLDisplay EGL_NO_DISPLAY = null;
    public static final EGLImage EGL_NO_IMAGE = null;
    public static final int EGL_NO_RESET_NOTIFICATION = 12734;
    public static final EGLSurface EGL_NO_SURFACE = null;
    public static final EGLSync EGL_NO_SYNC = null;
    public static final int EGL_OPENGL_ES3_BIT = 64;
    public static final int EGL_PLATFORM_ANDROID_KHR = 12609;
    public static final int EGL_SIGNALED = 12530;
    public static final int EGL_SYNC_CL_EVENT = 12542;
    public static final int EGL_SYNC_CL_EVENT_COMPLETE = 12543;
    public static final int EGL_SYNC_CONDITION = 12536;
    public static final int EGL_SYNC_FENCE = 12537;
    public static final int EGL_SYNC_FLUSH_COMMANDS_BIT = 1;
    public static final int EGL_SYNC_PRIOR_COMMANDS_COMPLETE = 12528;
    public static final int EGL_SYNC_STATUS = 12529;
    public static final int EGL_SYNC_TYPE = 12535;
    public static final int EGL_TIMEOUT_EXPIRED = 12533;
    public static final int EGL_UNSIGNALED = 12531;

    private static native void _nativeClassInit();

    public static native int eglClientWaitSync(EGLDisplay eGLDisplay, EGLSync eGLSync, int i, long j);

    public static native EGLImage eglCreateImage(EGLDisplay eGLDisplay, EGLContext eGLContext, int i, long j, long[] jArr, int i2);

    public static native EGLSurface eglCreatePlatformPixmapSurface(EGLDisplay eGLDisplay, EGLConfig eGLConfig, Buffer buffer, long[] jArr, int i);

    public static native EGLSurface eglCreatePlatformWindowSurface(EGLDisplay eGLDisplay, EGLConfig eGLConfig, Buffer buffer, long[] jArr, int i);

    public static native EGLSync eglCreateSync(EGLDisplay eGLDisplay, int i, long[] jArr, int i2);

    public static native boolean eglDestroyImage(EGLDisplay eGLDisplay, EGLImage eGLImage);

    public static native boolean eglDestroySync(EGLDisplay eGLDisplay, EGLSync eGLSync);

    public static native EGLDisplay eglGetPlatformDisplay(int i, long j, long[] jArr, int i2);

    public static native boolean eglGetSyncAttrib(EGLDisplay eGLDisplay, EGLSync eGLSync, int i, long[] jArr, int i2);

    public static native boolean eglWaitSync(EGLDisplay eGLDisplay, EGLSync eGLSync, int i);

    private EGL15() {
    }

    static {
        _nativeClassInit();
    }
}
