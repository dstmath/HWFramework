package ohos.agp.render.render3d.impl;

import java.nio.Buffer;

class CoreRenderNodeDescArrayView {
    private transient long agpCptrRenderNodeDescArrayView;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreRenderNodeDescArrayView(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrRenderNodeDescArrayView = j;
    }

    static long getCptr(CoreRenderNodeDescArrayView coreRenderNodeDescArrayView) {
        if (coreRenderNodeDescArrayView == null) {
            return 0;
        }
        return coreRenderNodeDescArrayView.agpCptrRenderNodeDescArrayView;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrRenderNodeDescArrayView != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreRenderNodeDescArrayView(this.agpCptrRenderNodeDescArrayView);
                }
                this.agpCptrRenderNodeDescArrayView = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreRenderNodeDescArrayView coreRenderNodeDescArrayView, boolean z) {
        if (coreRenderNodeDescArrayView != null) {
            synchronized (coreRenderNodeDescArrayView.delLock) {
                coreRenderNodeDescArrayView.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreRenderNodeDescArrayView);
    }

    CoreRenderNodeDescArrayView(Buffer buffer) {
        this(CoreJni.newCoreRenderNodeDescArrayView(buffer), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreRenderNodeDescArrayView(this.agpCptrRenderNodeDescArrayView, this);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderNodeDesc get(long j) {
        return new CoreRenderNodeDesc(CoreJni.getInCoreRenderNodeDescArrayView(this.agpCptrRenderNodeDescArrayView, this, j), true);
    }
}
