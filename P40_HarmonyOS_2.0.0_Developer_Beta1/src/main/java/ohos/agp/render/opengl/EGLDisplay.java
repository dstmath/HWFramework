package ohos.agp.render.opengl;

import ohos.agp.render.opengl.adapter.EGLDisplayAdapter;

public class EGLDisplay {
    private EGLDisplayAdapter mEGLDisplayAdapter;

    EGLDisplay(long j) {
        this.mEGLDisplayAdapter = new EGLDisplayAdapter(j);
    }

    /* access modifiers changed from: package-private */
    public EGLDisplayAdapter getEGLDisplayAdapter() {
        return this.mEGLDisplayAdapter;
    }
}
