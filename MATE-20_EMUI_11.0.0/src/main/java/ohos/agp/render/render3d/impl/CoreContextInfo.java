package ohos.agp.render.render3d.impl;

class CoreContextInfo {
    private transient long agpCptrCoreContextInfo;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreContextInfo(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreContextInfo = j;
    }

    static long getCptr(CoreContextInfo coreContextInfo) {
        if (coreContextInfo == null) {
            return 0;
        }
        return coreContextInfo.agpCptrCoreContextInfo;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreContextInfo != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreContextInfo(this.agpCptrCoreContextInfo);
                }
                this.agpCptrCoreContextInfo = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreContextInfo coreContextInfo, boolean z) {
        if (coreContextInfo != null) {
            synchronized (coreContextInfo.lock) {
                coreContextInfo.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreContextInfo);
    }

    CoreContextInfo() {
        this(CoreJni.newCoreContextInfo(), true);
    }
}
