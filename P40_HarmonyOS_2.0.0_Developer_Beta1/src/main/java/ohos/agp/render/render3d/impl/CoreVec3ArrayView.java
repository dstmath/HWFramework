package ohos.agp.render.render3d.impl;

import java.nio.Buffer;

/* access modifiers changed from: package-private */
public class CoreVec3ArrayView {
    private transient long agpCptrCoreVec3ArrayView;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreVec3ArrayView(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreVec3ArrayView = j;
    }

    static long getCptr(CoreVec3ArrayView coreVec3ArrayView) {
        if (coreVec3ArrayView == null) {
            return 0;
        }
        return coreVec3ArrayView.agpCptrCoreVec3ArrayView;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreVec3ArrayView != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreVec3ArrayView(this.agpCptrCoreVec3ArrayView);
                }
                this.agpCptrCoreVec3ArrayView = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreVec3ArrayView coreVec3ArrayView, boolean z) {
        if (coreVec3ArrayView != null) {
            synchronized (coreVec3ArrayView.delLock) {
                coreVec3ArrayView.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreVec3ArrayView);
    }

    CoreVec3ArrayView(Buffer buffer) {
        this(CoreJni.newCoreVec3ArrayView(buffer), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreVec3ArrayView(this.agpCptrCoreVec3ArrayView, this);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 get(long j) {
        return new CoreVec3(CoreJni.getInCoreVec3ArrayView(this.agpCptrCoreVec3ArrayView, this, j), true);
    }
}
