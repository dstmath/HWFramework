package ohos.agp.render.render3d.impl;

import java.nio.Buffer;

class CoreRenderNodeGraphInputArrayView {
    private transient long agpCptrRenderNodeGraphInputArrayView;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreRenderNodeGraphInputArrayView(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrRenderNodeGraphInputArrayView = j;
    }

    static long getCptr(CoreRenderNodeGraphInputArrayView coreRenderNodeGraphInputArrayView) {
        if (coreRenderNodeGraphInputArrayView == null) {
            return 0;
        }
        return coreRenderNodeGraphInputArrayView.agpCptrRenderNodeGraphInputArrayView;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrRenderNodeGraphInputArrayView != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreRenderNodeGraphInputArrayView(this.agpCptrRenderNodeGraphInputArrayView);
                }
                this.agpCptrRenderNodeGraphInputArrayView = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreRenderNodeGraphInputArrayView coreRenderNodeGraphInputArrayView, boolean z) {
        if (coreRenderNodeGraphInputArrayView != null) {
            synchronized (coreRenderNodeGraphInputArrayView.delLock) {
                coreRenderNodeGraphInputArrayView.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreRenderNodeGraphInputArrayView);
    }

    CoreRenderNodeGraphInputArrayView(Buffer buffer) {
        this(CoreJni.newCoreRenderNodeGraphInputArrayView(buffer), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreRenderNodeGraphInputArrayView(this.agpCptrRenderNodeGraphInputArrayView, this);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderNodeGraphInput get(long j) {
        return new CoreRenderNodeGraphInput(CoreJni.getInCoreRenderNodeGraphInputArrayView(this.agpCptrRenderNodeGraphInputArrayView, this, j), true);
    }
}
