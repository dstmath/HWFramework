package ohos.agp.render.opengl.adapter;

import android.opengl.EGL14;
import android.opengl.EGLSurface;
import ohos.annotation.SystemApi;

@SystemApi
public class EGLSurfaceAdapter {
    private EGLSurface mEglSurface = null;

    public EGLSurfaceAdapter(int i) {
        this.mEglSurface = EGL14.eglGetCurrentSurface(i);
    }

    /* access modifiers changed from: package-private */
    public void setEGLSurface(EGLSurface eGLSurface) {
        this.mEglSurface = eGLSurface;
    }

    public EGLSurface getEGLSurface() {
        return this.mEglSurface;
    }
}
