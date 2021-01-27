package ohos.agp.render.opengl.adapter;

import android.opengl.EGL14;
import android.opengl.EGLContext;
import ohos.annotation.SystemApi;

@SystemApi
public class EGLContextAdapter {
    private EGLContext mEglContext;

    public EGLContextAdapter() {
        this.mEglContext = null;
        this.mEglContext = EGL14.eglGetCurrentContext();
    }

    /* access modifiers changed from: package-private */
    public void setEGLContext(EGLContext eGLContext) {
        this.mEglContext = eGLContext;
    }

    public EGLContext getEGLContext() {
        return this.mEglContext;
    }
}
