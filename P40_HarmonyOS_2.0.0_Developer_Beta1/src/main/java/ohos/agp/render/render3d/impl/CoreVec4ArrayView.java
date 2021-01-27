package ohos.agp.render.render3d.impl;

import java.nio.Buffer;

class CoreVec4ArrayView {
    private transient long agpCptrCoreVec4ArrayView;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreVec4ArrayView(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreVec4ArrayView = j;
    }

    static long getCptr(CoreVec4ArrayView coreVec4ArrayView) {
        if (coreVec4ArrayView == null) {
            return 0;
        }
        return coreVec4ArrayView.agpCptrCoreVec4ArrayView;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreVec4ArrayView != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreVec4ArrayView(this.agpCptrCoreVec4ArrayView);
                }
                this.agpCptrCoreVec4ArrayView = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreVec4ArrayView coreVec4ArrayView, boolean z) {
        if (coreVec4ArrayView != null) {
            synchronized (coreVec4ArrayView.delLock) {
                coreVec4ArrayView.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreVec4ArrayView);
    }

    CoreVec4ArrayView(Buffer buffer) {
        this(CoreJni.newCoreVec4ArrayView(buffer), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreVec4ArrayView(this.agpCptrCoreVec4ArrayView, this);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4 get(long j) {
        return new CoreVec4(CoreJni.getInCoreVec4ArrayView(this.agpCptrCoreVec4ArrayView, this, j), true);
    }
}
