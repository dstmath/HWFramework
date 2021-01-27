package ohos.agp.render.render3d.impl;

class CoreStringArrayView {
    private transient long agpCptrCoreStringArrayView;
    private final Object delLock = new Object();
    transient boolean isAgpCmemOwn;

    CoreStringArrayView(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreStringArrayView = j;
    }

    static long getCptr(CoreStringArrayView coreStringArrayView) {
        if (coreStringArrayView == null) {
            return 0;
        }
        return coreStringArrayView.agpCptrCoreStringArrayView;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreStringArrayView != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreStringArrayView(this.agpCptrCoreStringArrayView);
                }
                this.agpCptrCoreStringArrayView = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreStringArrayView coreStringArrayView, boolean z) {
        if (coreStringArrayView != null) {
            synchronized (coreStringArrayView.delLock) {
                coreStringArrayView.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreStringArrayView);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreStringArrayView(this.agpCptrCoreStringArrayView, this);
    }

    /* access modifiers changed from: package-private */
    public String get(long j) {
        return CoreJni.getInCoreStringArrayView(this.agpCptrCoreStringArrayView, this, j);
    }
}
