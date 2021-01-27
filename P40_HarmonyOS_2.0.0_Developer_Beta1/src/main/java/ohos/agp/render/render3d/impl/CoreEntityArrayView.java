package ohos.agp.render.render3d.impl;

import java.nio.Buffer;

class CoreEntityArrayView {
    private transient long agpCptrCoreEntityArrayView;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreEntityArrayView(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreEntityArrayView = j;
    }

    static long getCptr(CoreEntityArrayView coreEntityArrayView) {
        if (coreEntityArrayView == null) {
            return 0;
        }
        return coreEntityArrayView.agpCptrCoreEntityArrayView;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreEntityArrayView != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreEntityArrayView(this.agpCptrCoreEntityArrayView);
                }
                this.agpCptrCoreEntityArrayView = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreEntityArrayView coreEntityArrayView, boolean z) {
        if (coreEntityArrayView != null) {
            synchronized (coreEntityArrayView.lock) {
                coreEntityArrayView.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreEntityArrayView);
    }

    CoreEntityArrayView(Buffer buffer) {
        this(CoreJni.newCoreEntityArrayView(buffer), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreEntityArrayView(this.agpCptrCoreEntityArrayView, this);
    }

    /* access modifiers changed from: package-private */
    public CoreEntity get(long j) {
        return new CoreEntity(CoreJni.getInCoreEntityArrayView(this.agpCptrCoreEntityArrayView, this, j), true);
    }
}
