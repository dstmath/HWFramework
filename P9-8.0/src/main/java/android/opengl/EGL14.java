package android.opengl;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class EGL14 {
    public static final int EGL_ALPHA_MASK_SIZE = 12350;
    public static final int EGL_ALPHA_SIZE = 12321;
    public static final int EGL_BACK_BUFFER = 12420;
    public static final int EGL_BAD_ACCESS = 12290;
    public static final int EGL_BAD_ALLOC = 12291;
    public static final int EGL_BAD_ATTRIBUTE = 12292;
    public static final int EGL_BAD_CONFIG = 12293;
    public static final int EGL_BAD_CONTEXT = 12294;
    public static final int EGL_BAD_CURRENT_SURFACE = 12295;
    public static final int EGL_BAD_DISPLAY = 12296;
    public static final int EGL_BAD_MATCH = 12297;
    public static final int EGL_BAD_NATIVE_PIXMAP = 12298;
    public static final int EGL_BAD_NATIVE_WINDOW = 12299;
    public static final int EGL_BAD_PARAMETER = 12300;
    public static final int EGL_BAD_SURFACE = 12301;
    public static final int EGL_BIND_TO_TEXTURE_RGB = 12345;
    public static final int EGL_BIND_TO_TEXTURE_RGBA = 12346;
    public static final int EGL_BLUE_SIZE = 12322;
    public static final int EGL_BUFFER_DESTROYED = 12437;
    public static final int EGL_BUFFER_PRESERVED = 12436;
    public static final int EGL_BUFFER_SIZE = 12320;
    public static final int EGL_CLIENT_APIS = 12429;
    public static final int EGL_COLOR_BUFFER_TYPE = 12351;
    public static final int EGL_CONFIG_CAVEAT = 12327;
    public static final int EGL_CONFIG_ID = 12328;
    public static final int EGL_CONFORMANT = 12354;
    public static final int EGL_CONTEXT_CLIENT_TYPE = 12439;
    public static final int EGL_CONTEXT_CLIENT_VERSION = 12440;
    public static final int EGL_CONTEXT_LOST = 12302;
    public static final int EGL_CORE_NATIVE_ENGINE = 12379;
    public static final int EGL_DEFAULT_DISPLAY = 0;
    public static final int EGL_DEPTH_SIZE = 12325;
    public static final int EGL_DISPLAY_SCALING = 10000;
    public static final int EGL_DRAW = 12377;
    public static final int EGL_EXTENSIONS = 12373;
    public static final int EGL_FALSE = 0;
    public static final int EGL_GREEN_SIZE = 12323;
    public static final int EGL_HEIGHT = 12374;
    public static final int EGL_HORIZONTAL_RESOLUTION = 12432;
    public static final int EGL_LARGEST_PBUFFER = 12376;
    public static final int EGL_LEVEL = 12329;
    public static final int EGL_LUMINANCE_BUFFER = 12431;
    public static final int EGL_LUMINANCE_SIZE = 12349;
    public static final int EGL_MATCH_NATIVE_PIXMAP = 12353;
    public static final int EGL_MAX_PBUFFER_HEIGHT = 12330;
    public static final int EGL_MAX_PBUFFER_PIXELS = 12331;
    public static final int EGL_MAX_PBUFFER_WIDTH = 12332;
    public static final int EGL_MAX_SWAP_INTERVAL = 12348;
    public static final int EGL_MIN_SWAP_INTERVAL = 12347;
    public static final int EGL_MIPMAP_LEVEL = 12419;
    public static final int EGL_MIPMAP_TEXTURE = 12418;
    public static final int EGL_MULTISAMPLE_RESOLVE = 12441;
    public static final int EGL_MULTISAMPLE_RESOLVE_BOX = 12443;
    public static final int EGL_MULTISAMPLE_RESOLVE_BOX_BIT = 512;
    public static final int EGL_MULTISAMPLE_RESOLVE_DEFAULT = 12442;
    public static final int EGL_NATIVE_RENDERABLE = 12333;
    public static final int EGL_NATIVE_VISUAL_ID = 12334;
    public static final int EGL_NATIVE_VISUAL_TYPE = 12335;
    public static final int EGL_NONE = 12344;
    public static final int EGL_NON_CONFORMANT_CONFIG = 12369;
    public static final int EGL_NOT_INITIALIZED = 12289;
    public static EGLContext EGL_NO_CONTEXT = null;
    public static EGLDisplay EGL_NO_DISPLAY = null;
    public static EGLSurface EGL_NO_SURFACE = null;
    public static final int EGL_NO_TEXTURE = 12380;
    public static final int EGL_OPENGL_API = 12450;
    public static final int EGL_OPENGL_BIT = 8;
    public static final int EGL_OPENGL_ES2_BIT = 4;
    public static final int EGL_OPENGL_ES_API = 12448;
    public static final int EGL_OPENGL_ES_BIT = 1;
    public static final int EGL_OPENVG_API = 12449;
    public static final int EGL_OPENVG_BIT = 2;
    public static final int EGL_OPENVG_IMAGE = 12438;
    public static final int EGL_PBUFFER_BIT = 1;
    public static final int EGL_PIXEL_ASPECT_RATIO = 12434;
    public static final int EGL_PIXMAP_BIT = 2;
    public static final int EGL_READ = 12378;
    public static final int EGL_RED_SIZE = 12324;
    public static final int EGL_RENDERABLE_TYPE = 12352;
    public static final int EGL_RENDER_BUFFER = 12422;
    public static final int EGL_RGB_BUFFER = 12430;
    public static final int EGL_SAMPLES = 12337;
    public static final int EGL_SAMPLE_BUFFERS = 12338;
    public static final int EGL_SINGLE_BUFFER = 12421;
    public static final int EGL_SLOW_CONFIG = 12368;
    public static final int EGL_STENCIL_SIZE = 12326;
    public static final int EGL_SUCCESS = 12288;
    public static final int EGL_SURFACE_TYPE = 12339;
    public static final int EGL_SWAP_BEHAVIOR = 12435;
    public static final int EGL_SWAP_BEHAVIOR_PRESERVED_BIT = 1024;
    public static final int EGL_TEXTURE_2D = 12383;
    public static final int EGL_TEXTURE_FORMAT = 12416;
    public static final int EGL_TEXTURE_RGB = 12381;
    public static final int EGL_TEXTURE_RGBA = 12382;
    public static final int EGL_TEXTURE_TARGET = 12417;
    public static final int EGL_TRANSPARENT_BLUE_VALUE = 12341;
    public static final int EGL_TRANSPARENT_GREEN_VALUE = 12342;
    public static final int EGL_TRANSPARENT_RED_VALUE = 12343;
    public static final int EGL_TRANSPARENT_RGB = 12370;
    public static final int EGL_TRANSPARENT_TYPE = 12340;
    public static final int EGL_TRUE = 1;
    public static final int EGL_VENDOR = 12371;
    public static final int EGL_VERSION = 12372;
    public static final int EGL_VERTICAL_RESOLUTION = 12433;
    public static final int EGL_VG_ALPHA_FORMAT = 12424;
    public static final int EGL_VG_ALPHA_FORMAT_NONPRE = 12427;
    public static final int EGL_VG_ALPHA_FORMAT_PRE = 12428;
    public static final int EGL_VG_ALPHA_FORMAT_PRE_BIT = 64;
    public static final int EGL_VG_COLORSPACE = 12423;
    public static final int EGL_VG_COLORSPACE_LINEAR = 12426;
    public static final int EGL_VG_COLORSPACE_LINEAR_BIT = 32;
    public static final int EGL_VG_COLORSPACE_sRGB = 12425;
    public static final int EGL_WIDTH = 12375;
    public static final int EGL_WINDOW_BIT = 4;

    private static native EGLSurface _eglCreateWindowSurface(EGLDisplay eGLDisplay, EGLConfig eGLConfig, Object obj, int[] iArr, int i);

    private static native EGLSurface _eglCreateWindowSurfaceTexture(EGLDisplay eGLDisplay, EGLConfig eGLConfig, Object obj, int[] iArr, int i);

    private static native void _nativeClassInit();

    public static native boolean eglBindAPI(int i);

    public static native boolean eglBindTexImage(EGLDisplay eGLDisplay, EGLSurface eGLSurface, int i);

    public static native boolean eglChooseConfig(EGLDisplay eGLDisplay, int[] iArr, int i, EGLConfig[] eGLConfigArr, int i2, int i3, int[] iArr2, int i4);

    public static native boolean eglCopyBuffers(EGLDisplay eGLDisplay, EGLSurface eGLSurface, int i);

    public static native EGLContext eglCreateContext(EGLDisplay eGLDisplay, EGLConfig eGLConfig, EGLContext eGLContext, int[] iArr, int i);

    public static native EGLSurface eglCreatePbufferFromClientBuffer(EGLDisplay eGLDisplay, int i, int i2, EGLConfig eGLConfig, int[] iArr, int i3);

    public static native EGLSurface eglCreatePbufferFromClientBuffer(EGLDisplay eGLDisplay, int i, long j, EGLConfig eGLConfig, int[] iArr, int i2);

    public static native EGLSurface eglCreatePbufferSurface(EGLDisplay eGLDisplay, EGLConfig eGLConfig, int[] iArr, int i);

    @Deprecated
    public static native EGLSurface eglCreatePixmapSurface(EGLDisplay eGLDisplay, EGLConfig eGLConfig, int i, int[] iArr, int i2);

    public static native boolean eglDestroyContext(EGLDisplay eGLDisplay, EGLContext eGLContext);

    public static native boolean eglDestroySurface(EGLDisplay eGLDisplay, EGLSurface eGLSurface);

    public static native boolean eglGetConfigAttrib(EGLDisplay eGLDisplay, EGLConfig eGLConfig, int i, int[] iArr, int i2);

    public static native boolean eglGetConfigs(EGLDisplay eGLDisplay, EGLConfig[] eGLConfigArr, int i, int i2, int[] iArr, int i3);

    public static native EGLContext eglGetCurrentContext();

    public static native EGLDisplay eglGetCurrentDisplay();

    public static native EGLSurface eglGetCurrentSurface(int i);

    public static native EGLDisplay eglGetDisplay(int i);

    public static native EGLDisplay eglGetDisplay(long j);

    public static native int eglGetError();

    public static native boolean eglInitialize(EGLDisplay eGLDisplay, int[] iArr, int i, int[] iArr2, int i2);

    public static native boolean eglMakeCurrent(EGLDisplay eGLDisplay, EGLSurface eGLSurface, EGLSurface eGLSurface2, EGLContext eGLContext);

    public static native int eglQueryAPI();

    public static native boolean eglQueryContext(EGLDisplay eGLDisplay, EGLContext eGLContext, int i, int[] iArr, int i2);

    public static native String eglQueryString(EGLDisplay eGLDisplay, int i);

    public static native boolean eglQuerySurface(EGLDisplay eGLDisplay, EGLSurface eGLSurface, int i, int[] iArr, int i2);

    public static native boolean eglReleaseTexImage(EGLDisplay eGLDisplay, EGLSurface eGLSurface, int i);

    public static native boolean eglReleaseThread();

    public static native boolean eglSurfaceAttrib(EGLDisplay eGLDisplay, EGLSurface eGLSurface, int i, int i2);

    public static native boolean eglSwapBuffers(EGLDisplay eGLDisplay, EGLSurface eGLSurface);

    public static native boolean eglSwapInterval(EGLDisplay eGLDisplay, int i);

    public static native boolean eglTerminate(EGLDisplay eGLDisplay);

    public static native boolean eglWaitClient();

    public static native boolean eglWaitGL();

    public static native boolean eglWaitNative(int i);

    static {
        _nativeClassInit();
    }

    public static EGLSurface eglCreateWindowSurface(EGLDisplay dpy, EGLConfig config, Object win, int[] attrib_list, int offset) {
        Object sur = null;
        if (win instanceof SurfaceView) {
            sur = ((SurfaceView) win).getHolder().getSurface();
        } else if (win instanceof SurfaceHolder) {
            sur = ((SurfaceHolder) win).getSurface();
        } else if (win instanceof Surface) {
            Surface sur2 = (Surface) win;
        }
        if (sur2 != null) {
            return _eglCreateWindowSurface(dpy, config, sur2, attrib_list, offset);
        }
        if (win instanceof SurfaceTexture) {
            return _eglCreateWindowSurfaceTexture(dpy, config, win, attrib_list, offset);
        }
        throw new UnsupportedOperationException("eglCreateWindowSurface() can only be called with an instance of Surface, SurfaceView, SurfaceTexture or SurfaceHolder at the moment, this will be fixed later.");
    }
}
