package ohos.agp.render.opengl.adapter;

import android.opengl.EGL14;
import android.opengl.EGLDisplay;
import ohos.annotation.SystemApi;

@SystemApi
public class EGLDisplayAdapter {
    private EGLDisplay mEglDisplay = null;

    public EGLDisplayAdapter(long j) {
        this.mEglDisplay = EGL14.eglGetDisplay(j);
    }

    /* access modifiers changed from: package-private */
    public void setEGLDisplay(EGLDisplay eGLDisplay) {
        this.mEglDisplay = eGLDisplay;
    }

    public EGLDisplay getEGLDisplay() {
        return this.mEglDisplay;
    }
}
