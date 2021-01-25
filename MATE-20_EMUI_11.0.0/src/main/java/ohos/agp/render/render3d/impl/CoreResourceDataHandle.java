package ohos.agp.render.render3d.impl;

class CoreResourceDataHandle {
    private transient long agpCptrCoreResourceDataHandle;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreResourceDataHandle(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreResourceDataHandle = j;
    }

    static long getCptr(CoreResourceDataHandle coreResourceDataHandle) {
        if (coreResourceDataHandle == null) {
            return 0;
        }
        return coreResourceDataHandle.agpCptrCoreResourceDataHandle;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreResourceDataHandle != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreResourceDataHandle(this.agpCptrCoreResourceDataHandle);
                }
                this.agpCptrCoreResourceDataHandle = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreResourceDataHandle coreResourceDataHandle, boolean z) {
        if (coreResourceDataHandle != null) {
            synchronized (coreResourceDataHandle.delLock) {
                coreResourceDataHandle.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreResourceDataHandle);
    }

    /* access modifiers changed from: package-private */
    public void setId(long j) {
        CoreJni.setVaridCoreResourceDataHandle(this.agpCptrCoreResourceDataHandle, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getId() {
        return CoreJni.getVaridCoreResourceDataHandle(this.agpCptrCoreResourceDataHandle, this);
    }

    CoreResourceDataHandle() {
        this(CoreJni.newCoreResourceDataHandle(), true);
    }
}
