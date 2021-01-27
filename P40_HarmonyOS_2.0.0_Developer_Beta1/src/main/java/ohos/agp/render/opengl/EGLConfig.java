package ohos.agp.render.opengl;

import ohos.agp.render.opengl.adapter.EGLConfigAdapter;

public class EGLConfig {
    private EGLConfigAdapter mEGLConfigAdapter = new EGLConfigAdapter();

    EGLConfig() {
    }

    /* access modifiers changed from: package-private */
    public EGLConfigAdapter getEGLConfigAdapter() {
        return this.mEGLConfigAdapter;
    }

    /* access modifiers changed from: package-private */
    public void setEGLConfigAdapter(EGLConfigAdapter eGLConfigAdapter) {
        this.mEGLConfigAdapter = eGLConfigAdapter;
    }
}
