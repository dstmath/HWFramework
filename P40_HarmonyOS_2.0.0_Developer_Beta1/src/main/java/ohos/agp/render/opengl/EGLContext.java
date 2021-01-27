package ohos.agp.render.opengl;

import ohos.agp.render.opengl.adapter.EGLContextAdapter;

public class EGLContext {
    private EGLContextAdapter mEGLContextAdapter = new EGLContextAdapter();

    EGLContext() {
    }

    /* access modifiers changed from: package-private */
    public EGLContextAdapter getEGLContextAdapter() {
        return this.mEGLContextAdapter;
    }
}
