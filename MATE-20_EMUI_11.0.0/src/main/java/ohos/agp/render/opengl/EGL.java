package ohos.agp.render.opengl;

import ohos.agp.render.opengl.adapter.EGLAdapter;
import ohos.agp.render.opengl.adapter.EGLConfigAdapter;

public class EGL {
    public static final int EGL_ALPHA_SIZE = 12321;
    public static final int EGL_BAD_NATIVE_WINDOW = 12299;
    public static final int EGL_BLUE_SIZE = 12322;
    public static final int EGL_BUFFER_SIZE = 12320;
    public static final int EGL_COLOR_BUFFER_TYPE = 12351;
    public static final int EGL_DEFAULT_DISPLAY = 0;
    public static final int EGL_DEPTH_SIZE = 12325;
    public static final int EGL_GREEN_SIZE = 12323;
    public static final int EGL_HEIGHT = 12374;
    public static final int EGL_LEVEL = 12329;
    public static final int EGL_NONE = 12344;
    public static final EGLContext EGL_NO_CONTEXT = null;
    public static final EGLDisplay EGL_NO_DISPLAY = null;
    public static final EGLSurface EGL_NO_SURFACE = null;
    public static final int EGL_PBUFFER_BIT = 1;
    public static final int EGL_RED_SIZE = 12324;
    public static final int EGL_RENDERABLE_TYPE = 12352;
    public static final int EGL_RGB_BUFFER = 12430;
    public static final int EGL_STENCIL_SIZE = 12326;
    public static final int EGL_SUCCESS = 12288;
    public static final int EGL_SURFACE_TYPE = 12339;
    public static final int EGL_WIDTH = 12375;
    private static final String TAG = "EGL";

    public static boolean eglChooseConfig(EGLDisplay eGLDisplay, int[] iArr, EGLConfig[] eGLConfigArr, int i, int[] iArr2) {
        EGLConfigAdapter[] eGLConfigAdapterArr = eGLConfigArr != null ? new EGLConfigAdapter[i] : null;
        boolean eglChooseConfig = eGLDisplay instanceof EGLDisplay ? EGLAdapter.eglChooseConfig(eGLDisplay.getEGLDisplayAdapter(), iArr, eGLConfigAdapterArr, i, iArr2) : false;
        if (eglChooseConfig && eGLConfigArr != null) {
            for (int i2 = 0; i2 < i; i2++) {
                EGLConfig eGLConfig = new EGLConfig();
                eGLConfig.setEGLConfigAdapter(eGLConfigAdapterArr[i2]);
                eGLConfigArr[i2] = eGLConfig;
            }
        }
        return eglChooseConfig;
    }

    public static EGLContext eglCreateContext(EGLDisplay eGLDisplay, EGLConfig eGLConfig, EGLContext eGLContext, int[] iArr) {
        EGLContext eGLContext2 = new EGLContext();
        if ((eGLDisplay instanceof EGLDisplay) && (eGLConfig instanceof EGLConfig)) {
            if (eGLContext == EGL_NO_CONTEXT) {
                EGLAdapter.eglCreateContext(eGLDisplay.getEGLDisplayAdapter(), eGLConfig.getEGLConfigAdapter(), null, iArr, eGLContext2.getEGLContextAdapter());
            } else {
                EGLAdapter.eglCreateContext(eGLDisplay.getEGLDisplayAdapter(), eGLConfig.getEGLConfigAdapter(), eGLContext.getEGLContextAdapter(), iArr, eGLContext2.getEGLContextAdapter());
            }
        }
        return eGLContext2;
    }

    public static EGLSurface eglCreateWindowSurface(EGLDisplay eGLDisplay, EGLConfig eGLConfig, Object obj, int[] iArr) {
        EGLSurface eGLSurface = new EGLSurface(0);
        if ((eGLDisplay instanceof EGLDisplay) && (eGLConfig instanceof EGLConfig)) {
            EGLAdapter.eglCreateWindowSurface(eGLDisplay.getEGLDisplayAdapter(), eGLConfig.getEGLConfigAdapter(), obj, iArr, eGLSurface.getEGLSurfaceAdapter());
        }
        return eGLSurface;
    }

    public static boolean eglDestroyContext(EGLDisplay eGLDisplay, EGLContext eGLContext) {
        if (!(eGLDisplay instanceof EGLDisplay) || !(eGLContext instanceof EGLContext)) {
            return false;
        }
        return EGLAdapter.eglDestroyContext(eGLDisplay.getEGLDisplayAdapter(), eGLContext.getEGLContextAdapter());
    }

    public static boolean eglDestroySurface(EGLDisplay eGLDisplay, EGLSurface eGLSurface) {
        if (!(eGLDisplay instanceof EGLDisplay) || !(eGLSurface instanceof EGLSurface)) {
            return false;
        }
        return EGLAdapter.eglDestroySurface(eGLDisplay.getEGLDisplayAdapter(), eGLSurface.getEGLSurfaceAdapter());
    }

    public static EGLDisplay eglGetDisplay(long j) {
        EGLDisplay eGLDisplay = new EGLDisplay(0);
        EGLAdapter.eglGetDisplay(j, eGLDisplay.getEGLDisplayAdapter());
        return eGLDisplay;
    }

    public static boolean eglInitialize(EGLDisplay eGLDisplay, int[] iArr, int[] iArr2) {
        if (eGLDisplay instanceof EGLDisplay) {
            return EGLAdapter.eglInitialize(eGLDisplay.getEGLDisplayAdapter(), iArr, iArr2);
        }
        return false;
    }

    public static boolean eglMakeCurrent(EGLDisplay eGLDisplay, EGLSurface eGLSurface, EGLSurface eGLSurface2, EGLContext eGLContext) {
        if (!(eGLDisplay instanceof EGLDisplay) || !(eGLSurface instanceof EGLSurface) || !(eGLSurface2 instanceof EGLSurface) || !(eGLContext instanceof EGLContext)) {
            return false;
        }
        return EGLAdapter.eglMakeCurrent(eGLDisplay.getEGLDisplayAdapter(), eGLSurface.getEGLSurfaceAdapter(), eGLSurface2.getEGLSurfaceAdapter(), eGLContext.getEGLContextAdapter());
    }

    public static boolean eglSwapBuffers(EGLDisplay eGLDisplay, EGLSurface eGLSurface) {
        if (!(eGLDisplay instanceof EGLDisplay) || !(eGLSurface instanceof EGLSurface)) {
            return false;
        }
        return EGLAdapter.eglSwapBuffers(eGLDisplay.getEGLDisplayAdapter(), eGLSurface.getEGLSurfaceAdapter());
    }

    public static boolean eglTerminate(EGLDisplay eGLDisplay) {
        if (eGLDisplay instanceof EGLDisplay) {
            return EGLAdapter.eglTerminate(eGLDisplay.getEGLDisplayAdapter());
        }
        return false;
    }

    public static int eglGetError() {
        return EGLAdapter.eglGetError();
    }

    public static EGLContext eglGetCurrentContext() {
        EGLContext eGLContext = new EGLContext();
        EGLAdapter.eglGetCurrentContext(eGLContext.getEGLContextAdapter());
        return eGLContext;
    }

    public static EGLDisplay eglGetCurrentDisplay() {
        EGLDisplay eGLDisplay = new EGLDisplay(0);
        EGLAdapter.eglGetCurrentDisplay(eGLDisplay.getEGLDisplayAdapter());
        return eGLDisplay;
    }

    public static EGLSurface eglGetCurrentSurface(int i) {
        EGLSurface eGLSurface = new EGLSurface(0);
        EGLAdapter.eglGetCurrentSurface(i, eGLSurface.getEGLSurfaceAdapter());
        return eGLSurface;
    }

    public static EGLSurface eglCreatePbufferSurface(EGLDisplay eGLDisplay, EGLConfig eGLConfig, int[] iArr) {
        EGLSurface eGLSurface = new EGLSurface(0);
        if (!(eGLDisplay instanceof EGLDisplay) || !(eGLConfig instanceof EGLConfig) || !EGLAdapter.eglCreatePbufferSurface(eGLDisplay.getEGLDisplayAdapter(), eGLConfig.getEGLConfigAdapter(), iArr, eGLSurface.getEGLSurfaceAdapter())) {
            return EGL_NO_SURFACE;
        }
        return eGLSurface;
    }

    public static boolean eglQuerySurface(EGLDisplay eGLDisplay, EGLSurface eGLSurface, int i, int[] iArr) {
        if (!(eGLDisplay instanceof EGLDisplay) || !(eGLSurface instanceof EGLSurface)) {
            return false;
        }
        return EGLAdapter.eglQuerySurface(eGLDisplay.getEGLDisplayAdapter(), eGLSurface.getEGLSurfaceAdapter(), i, iArr);
    }
}
