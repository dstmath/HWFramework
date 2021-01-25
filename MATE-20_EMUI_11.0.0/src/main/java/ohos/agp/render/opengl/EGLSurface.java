package ohos.agp.render.opengl;

import ohos.agp.render.opengl.adapter.EGLSurfaceAdapter;

public class EGLSurface {
    private EGLSurfaceAdapter mEGLSurfaceAdapter;

    EGLSurface(int i) {
        this.mEGLSurfaceAdapter = new EGLSurfaceAdapter(i);
    }

    /* access modifiers changed from: package-private */
    public EGLSurfaceAdapter getEGLSurfaceAdapter() {
        return this.mEGLSurfaceAdapter;
    }
}
