package com.huawei.iimagekit.common.agp.graphics.porting.gl;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import com.huawei.iimagekit.blur.util.DebugUtil;
import com.huawei.iimagekit.common.agp.graphics.engine.GraphicsContext;

public class OffscreenGraphicsContext {
    private static EGLConfig eglConf;
    private static EGLContext eglCtx;
    private static EGLDisplay eglDisp;
    private static EGLSurface eglSurface;

    public static void init(Context appContext) {
        setAppContext(appContext);
        init();
    }

    public static void init() {
        int[] eglMajVers = {0, 0};
        int[] eglMinVers = {0, 0};
        eglDisp = EGL14.eglGetDisplay(0);
        EGL14.eglInitialize(eglDisp, eglMajVers, 0, eglMinVers, 0);
        DebugUtil.log("EGL init with version %d.%d", Integer.valueOf(eglMajVers[0]), Integer.valueOf(eglMinVers[0]));
        EGLConfig[] conf = new EGLConfig[2];
        EGL14.eglChooseConfig(eglDisp, new int[]{12352, 64, 12339, 1, 12324, 8, 12323, 8, 12321, 8, 12322, 8, 12344}, 0, conf, 0, 1, new int[]{0, 0}, 0);
        eglConf = conf[0];
        eglCtx = EGL14.eglCreateContext(eglDisp, eglConf, EGL14.EGL_NO_CONTEXT, new int[]{12440, 3, 12344}, 0);
        eglSurface = EGL14.eglCreatePbufferSurface(eglDisp, eglConf, new int[]{12375, 1, 12374, 1, 12344}, 0);
        EGL14.eglMakeCurrent(eglDisp, eglSurface, eglSurface, eglCtx);
        GraphicsContext.init();
    }

    public static void setAppContext(Context appContext) {
        GraphicsContext.setAppContext(appContext);
    }

    public static void destroy() {
        GraphicsContext.destroy();
        EGL14.eglDestroyContext(eglDisp, eglCtx);
        EGL14.eglDestroySurface(eglDisp, eglSurface);
    }
}
