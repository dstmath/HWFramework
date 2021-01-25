package ohos.agp.render.render3d.impl;

class CoreBackendExtra {
    private transient long agpCptrCoreBackendExtra;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreBackendExtra(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreBackendExtra = j;
    }

    static long getCptr(CoreBackendExtra coreBackendExtra) {
        if (coreBackendExtra == null) {
            return 0;
        }
        return coreBackendExtra.agpCptrCoreBackendExtra;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreBackendExtra != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreBackendExtra(this.agpCptrCoreBackendExtra);
                }
                this.agpCptrCoreBackendExtra = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreBackendExtra coreBackendExtra, boolean z) {
        if (coreBackendExtra != null) {
            synchronized (coreBackendExtra.lock) {
                coreBackendExtra.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreBackendExtra);
    }

    CoreBackendExtra() {
        this(CoreJni.newCoreBackendExtra(), true);
    }
}
