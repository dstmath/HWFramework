package ohos.agp.render.render3d.impl;

import java.nio.Buffer;

class CoreVec2ArrayView {
    private transient long agpCptrCoreVec2ArrayView;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreVec2ArrayView(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreVec2ArrayView = j;
    }

    static long getCptr(CoreVec2ArrayView coreVec2ArrayView) {
        if (coreVec2ArrayView == null) {
            return 0;
        }
        return coreVec2ArrayView.agpCptrCoreVec2ArrayView;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreVec2ArrayView != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreVec2ArrayView(this.agpCptrCoreVec2ArrayView);
                }
                this.agpCptrCoreVec2ArrayView = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreVec2ArrayView coreVec2ArrayView, boolean z) {
        if (coreVec2ArrayView != null) {
            synchronized (coreVec2ArrayView.delLock) {
                coreVec2ArrayView.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreVec2ArrayView);
    }

    CoreVec2ArrayView(Buffer buffer) {
        this(CoreJni.newCoreVec2ArrayView(buffer), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreVec2ArrayView(this.agpCptrCoreVec2ArrayView, this);
    }

    /* access modifiers changed from: package-private */
    public CoreVec2 get(long j) {
        return new CoreVec2(CoreJni.getInCoreVec2ArrayView(this.agpCptrCoreVec2ArrayView, this, j), true);
    }
}
