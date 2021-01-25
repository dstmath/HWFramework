package ohos.agp.render.opengl.adapter;

import android.opengl.EGLConfig;
import ohos.annotation.SystemApi;

@SystemApi
public class EGLConfigAdapter {
    private EGLConfig mEglConfig = null;

    /* access modifiers changed from: package-private */
    public void setEGLConfig(EGLConfig eGLConfig) {
        this.mEglConfig = eGLConfig;
    }

    public EGLConfig getEGLConfig() {
        return this.mEglConfig;
    }
}
