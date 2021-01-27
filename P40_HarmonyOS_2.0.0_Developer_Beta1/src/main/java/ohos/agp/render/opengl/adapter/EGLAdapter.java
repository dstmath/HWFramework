package ohos.agp.render.opengl.adapter;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.surfaceview.adapter.SurfaceUtils;
import ohos.agp.graphics.Surface;
import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

@SystemApi
public class EGLAdapter {
    private static final int INITIAL_OFFSET = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, TAG1);
    private static final String TAG1 = "EGLAdapter";

    public static boolean eglChooseConfig(EGLDisplayAdapter eGLDisplayAdapter, int[] iArr, EGLConfigAdapter[] eGLConfigAdapterArr, int i, int[] iArr2) {
        EGLDisplay eGLDisplay;
        EGLConfig[] eGLConfigArr;
        if (eGLDisplayAdapter == null || (eGLDisplay = eGLDisplayAdapter.getEGLDisplay()) == null) {
            return false;
        }
        if (eGLConfigAdapterArr == null) {
            eGLConfigArr = null;
        } else {
            eGLConfigArr = new EGLConfig[i];
        }
        boolean eglChooseConfig = EGL14.eglChooseConfig(eGLDisplay, iArr, 0, eGLConfigArr, 0, i, iArr2, 0);
        if (eGLConfigArr != null && eglChooseConfig) {
            for (int i2 = 0; i2 < i; i2++) {
                eGLConfigAdapterArr[i2] = new EGLConfigAdapter();
                eGLConfigAdapterArr[i2].setEGLConfig(eGLConfigArr[i2]);
            }
        }
        return eglChooseConfig;
    }

    public static void eglCreateContext(EGLDisplayAdapter eGLDisplayAdapter, EGLConfigAdapter eGLConfigAdapter, EGLContextAdapter eGLContextAdapter, int[] iArr, EGLContextAdapter eGLContextAdapter2) {
        EGLContext eGLContext;
        if (eGLDisplayAdapter != null && eGLConfigAdapter != null && eGLContextAdapter2 != null && eGLDisplayAdapter.getEGLDisplay() != null && eGLConfigAdapter.getEGLConfig() != null) {
            if (eGLContextAdapter == null || eGLContextAdapter.getEGLContext() != EGL14.EGL_NO_CONTEXT) {
                if (eGLContextAdapter == null) {
                    eGLContext = EGL14.eglCreateContext(eGLDisplayAdapter.getEGLDisplay(), eGLConfigAdapter.getEGLConfig(), EGL14.EGL_NO_CONTEXT, iArr, 0);
                } else {
                    eGLContext = EGL14.eglCreateContext(eGLDisplayAdapter.getEGLDisplay(), eGLConfigAdapter.getEGLConfig(), eGLContextAdapter.getEGLContext(), iArr, 0);
                }
                eGLContextAdapter2.setEGLContext(eGLContext);
            }
        }
    }

    public static void eglCreateWindowSurface(EGLDisplayAdapter eGLDisplayAdapter, EGLConfigAdapter eGLConfigAdapter, Object obj, int[] iArr, EGLSurfaceAdapter eGLSurfaceAdapter) {
        if (eGLDisplayAdapter == null || eGLConfigAdapter == null || eGLSurfaceAdapter == null) {
            HiLog.error(TAG, "eglCreateWindowSurface: eglDisplayAdapt or eglConfigAdapt or eglSurfaceAdapt is null.", new Object[0]);
        } else if (!(obj instanceof Surface) || eGLDisplayAdapter.getEGLDisplay() == null || eGLConfigAdapter.getEGLConfig() == null) {
            HiLog.error(TAG, "eglCreateWindowSurface: surface is not instanceof Surface or eglInstance is null.", new Object[0]);
        } else {
            android.view.Surface surfaceImpl = SurfaceUtils.getSurfaceImpl((Surface) obj);
            if (surfaceImpl == null) {
                HiLog.error(TAG, "eglCreateWindowSurface: surface is null.", new Object[0]);
            } else {
                eGLSurfaceAdapter.setEGLSurface(EGL14.eglCreateWindowSurface(eGLDisplayAdapter.getEGLDisplay(), eGLConfigAdapter.getEGLConfig(), surfaceImpl, iArr, 0));
            }
        }
    }

    public static boolean eglDestroyContext(EGLDisplayAdapter eGLDisplayAdapter, EGLContextAdapter eGLContextAdapter) {
        if (eGLDisplayAdapter == null || eGLContextAdapter == null) {
            HiLog.error(TAG, "eglDestroyContext: eglDisplayAdapter or eglContextAdapter is null.", new Object[0]);
            return false;
        } else if (eGLDisplayAdapter.getEGLDisplay() != null && eGLContextAdapter.getEGLContext() != null) {
            return EGL14.eglDestroyContext(eGLDisplayAdapter.getEGLDisplay(), eGLContextAdapter.getEGLContext());
        } else {
            HiLog.error(TAG, "eglDestroyContext: eglDisplay or eglContext is null.", new Object[0]);
            return false;
        }
    }

    public static boolean eglDestroySurface(EGLDisplayAdapter eGLDisplayAdapter, EGLSurfaceAdapter eGLSurfaceAdapter) {
        if (eGLDisplayAdapter == null || eGLSurfaceAdapter == null) {
            HiLog.error(TAG, "eglDestroySurface: eglDisplayAdapter or eglSurfaceAdapter is null.", new Object[0]);
            return false;
        } else if (eGLDisplayAdapter.getEGLDisplay() != null && eGLSurfaceAdapter.getEGLSurface() != null) {
            return EGL14.eglDestroySurface(eGLDisplayAdapter.getEGLDisplay(), eGLSurfaceAdapter.getEGLSurface());
        } else {
            HiLog.error(TAG, "eglDestroySurface: eglDisplay or eglSurface is null.", new Object[0]);
            return false;
        }
    }

    public static void eglGetDisplay(long j, EGLDisplayAdapter eGLDisplayAdapter) {
        if (eGLDisplayAdapter != null) {
            eGLDisplayAdapter.setEGLDisplay(EGL14.eglGetDisplay((int) j));
        }
    }

    public static boolean eglInitialize(EGLDisplayAdapter eGLDisplayAdapter, int[] iArr, int[] iArr2) {
        if (eGLDisplayAdapter == null || eGLDisplayAdapter.getEGLDisplay() == null || eGLDisplayAdapter.getEGLDisplay() == EGL14.EGL_NO_DISPLAY) {
            return false;
        }
        return EGL14.eglInitialize(eGLDisplayAdapter.getEGLDisplay(), iArr, 0, iArr2, 0);
    }

    public static boolean eglMakeCurrent(EGLDisplayAdapter eGLDisplayAdapter, EGLSurfaceAdapter eGLSurfaceAdapter, EGLSurfaceAdapter eGLSurfaceAdapter2, EGLContextAdapter eGLContextAdapter) {
        if (eGLDisplayAdapter == null || eGLSurfaceAdapter == null || eGLSurfaceAdapter2 == null || eGLContextAdapter == null || eGLDisplayAdapter.getEGLDisplay() == null || eGLSurfaceAdapter.getEGLSurface() == null || eGLSurfaceAdapter2.getEGLSurface() == null || eGLContextAdapter.getEGLContext() == null) {
            return false;
        }
        return EGL14.eglMakeCurrent(eGLDisplayAdapter.getEGLDisplay(), eGLSurfaceAdapter.getEGLSurface(), eGLSurfaceAdapter2.getEGLSurface(), eGLContextAdapter.getEGLContext());
    }

    public static boolean eglSwapBuffers(EGLDisplayAdapter eGLDisplayAdapter, EGLSurfaceAdapter eGLSurfaceAdapter) {
        if (eGLDisplayAdapter == null || eGLSurfaceAdapter == null) {
            HiLog.error(TAG, "eglSwapBuffers: eglDisplayAdapter or eglSurfaceAdapter is null.", new Object[0]);
            return false;
        } else if (eGLDisplayAdapter.getEGLDisplay() != null && eGLSurfaceAdapter.getEGLSurface() != null) {
            return EGL14.eglSwapBuffers(eGLDisplayAdapter.getEGLDisplay(), eGLSurfaceAdapter.getEGLSurface());
        } else {
            HiLog.error(TAG, "eglSwapBuffers: eglDisplay or eglSurface is null.", new Object[0]);
            return false;
        }
    }

    public static boolean eglTerminate(EGLDisplayAdapter eGLDisplayAdapter) {
        if (eGLDisplayAdapter == null) {
            HiLog.error(TAG, "eglTerminate: eglDisplayAdapter is null.", new Object[0]);
            return false;
        }
        EGLDisplay eGLDisplay = eGLDisplayAdapter.getEGLDisplay();
        if (eGLDisplay != null) {
            return EGL14.eglTerminate(eGLDisplay);
        }
        HiLog.error(TAG, "eglTerminate: eglDisplay or eglInstance is null.", new Object[0]);
        return false;
    }

    public static int eglGetError() {
        return EGL14.eglGetError();
    }

    public static void eglGetCurrentContext(EGLContextAdapter eGLContextAdapter) {
        if (eGLContextAdapter != null) {
            eGLContextAdapter.setEGLContext(EGL14.eglGetCurrentContext());
        }
    }

    public static void eglGetCurrentDisplay(EGLDisplayAdapter eGLDisplayAdapter) {
        if (eGLDisplayAdapter != null) {
            eGLDisplayAdapter.setEGLDisplay(EGL14.eglGetCurrentDisplay());
        }
    }

    public static void eglGetCurrentSurface(int i, EGLSurfaceAdapter eGLSurfaceAdapter) {
        if (eGLSurfaceAdapter != null) {
            eGLSurfaceAdapter.setEGLSurface(EGL14.eglGetCurrentSurface(i));
        }
    }

    public static boolean eglCreatePbufferSurface(EGLDisplayAdapter eGLDisplayAdapter, EGLConfigAdapter eGLConfigAdapter, int[] iArr, EGLSurfaceAdapter eGLSurfaceAdapter) {
        EGLSurface eglCreatePbufferSurface;
        if (eGLSurfaceAdapter == null || (eglCreatePbufferSurface = EGL14.eglCreatePbufferSurface(eGLDisplayAdapter.getEGLDisplay(), eGLConfigAdapter.getEGLConfig(), iArr, 0)) == EGL14.EGL_NO_SURFACE) {
            return false;
        }
        eGLSurfaceAdapter.setEGLSurface(eglCreatePbufferSurface);
        return true;
    }

    public static boolean eglQuerySurface(EGLDisplayAdapter eGLDisplayAdapter, EGLSurfaceAdapter eGLSurfaceAdapter, int i, int[] iArr) {
        if (eGLDisplayAdapter == null || eGLSurfaceAdapter == null) {
            HiLog.error(TAG, "eglQuerySurface: eglDisplayAdapter or eglSurfaceAdapter is null.", new Object[0]);
            return false;
        } else if (eGLDisplayAdapter.getEGLDisplay() != null && eGLSurfaceAdapter.getEGLSurface() != null) {
            return EGL14.eglQuerySurface(eGLDisplayAdapter.getEGLDisplay(), eGLSurfaceAdapter.getEGLSurface(), i, iArr, 0);
        } else {
            HiLog.error(TAG, "eglQuerySurface: eglDisplay or eglSurface is null.", new Object[0]);
            return false;
        }
    }

    public static boolean eglGetConfigs(EGLDisplayAdapter eGLDisplayAdapter, EGLConfigAdapter[] eGLConfigAdapterArr, int i, int[] iArr) {
        return EGL14.eglGetConfigs(eGLDisplayAdapter.getEGLDisplay(), eGLConfigAdapterArr == null ? null : new EGLConfig[i], 0, i, iArr, 0);
    }

    public static boolean eglReleaseThread() {
        return EGL14.eglReleaseThread();
    }

    public static String eglQueryString(EGLDisplayAdapter eGLDisplayAdapter, int i) {
        if (eGLDisplayAdapter == null) {
            HiLog.error(TAG, "eglQueryString:EGLDisplayAdapter display is null.", new Object[0]);
            return "";
        } else if (eGLDisplayAdapter.getEGLDisplay() != null) {
            return EGL14.eglQueryString(eGLDisplayAdapter.getEGLDisplay(), i);
        } else {
            HiLog.error(TAG, "eglDestroyContext: eglDisplay is null.", new Object[0]);
            return "";
        }
    }

    public static boolean eglQueryContext(EGLDisplayAdapter eGLDisplayAdapter, EGLContextAdapter eGLContextAdapter, int i, int[] iArr) {
        if (eGLDisplayAdapter == null || eGLContextAdapter == null) {
            HiLog.error(TAG, "eglQueryContext: display or context is null.", new Object[0]);
            return false;
        } else if (eGLDisplayAdapter.getEGLDisplay() != null && eGLContextAdapter.getEGLContext() != null) {
            return EGL14.eglQueryContext(eGLDisplayAdapter.getEGLDisplay(), eGLContextAdapter.getEGLContext(), i, iArr, 0);
        } else {
            HiLog.error(TAG, "eglDestroyContext: eglDisplay or eglContext is null.", new Object[0]);
            return false;
        }
    }

    public static boolean eglGetConfigAttrib(EGLDisplayAdapter eGLDisplayAdapter, EGLConfigAdapter eGLConfigAdapter, int i, int[] iArr) {
        if (eGLDisplayAdapter == null || eGLConfigAdapter == null) {
            HiLog.error(TAG, "eglGetConfigAttrib: dpy or config is null.", new Object[0]);
            return false;
        } else if (eGLDisplayAdapter.getEGLDisplay() != null && eGLConfigAdapter.getEGLConfig() != null) {
            return EGL14.eglGetConfigAttrib(eGLDisplayAdapter.getEGLDisplay(), eGLConfigAdapter.getEGLConfig(), i, iArr, 0);
        } else {
            HiLog.error(TAG, "eglDestroySurface: eglDisplay or eglSurface is null.", new Object[0]);
            return false;
        }
    }

    public static boolean eglBindAPI(int i) {
        return EGL14.eglBindAPI(i);
    }
}
