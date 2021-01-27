package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreRenderer {
    private transient long agpCptrCoreRenderer;
    private final Object delLock = new Object();
    transient boolean isAgpCmemOwn;

    CoreRenderer(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreRenderer = j;
    }

    static long getCptr(CoreRenderer coreRenderer) {
        if (coreRenderer == null) {
            return 0;
        }
        return coreRenderer.agpCptrCoreRenderer;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreRenderer != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreRenderer(this.agpCptrCoreRenderer);
                }
                this.agpCptrCoreRenderer = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreRenderer coreRenderer, boolean z) {
        if (coreRenderer != null) {
            synchronized (coreRenderer.delLock) {
                coreRenderer.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreRenderer);
    }

    /* access modifiers changed from: package-private */
    public void renderFrame(CoreRenderNodeGraphInput coreRenderNodeGraphInput) {
        CoreJni.renderFrameInCoreRenderer0(this.agpCptrCoreRenderer, this, CoreRenderNodeGraphInput.getCptr(coreRenderNodeGraphInput), coreRenderNodeGraphInput);
    }

    /* access modifiers changed from: package-private */
    public void renderFrame(CoreRenderNodeGraphType coreRenderNodeGraphType) {
        CoreJni.renderFrameInCoreRenderer1(this.agpCptrCoreRenderer, this, coreRenderNodeGraphType.swigValue());
    }

    /* access modifiers changed from: package-private */
    public void renderFrame(CoreRenderNodeGraphInputArrayView coreRenderNodeGraphInputArrayView) {
        CoreJni.renderFrameInCoreRenderer2(this.agpCptrCoreRenderer, this, CoreRenderNodeGraphInputArrayView.getCptr(coreRenderNodeGraphInputArrayView), coreRenderNodeGraphInputArrayView);
    }
}
