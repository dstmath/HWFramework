package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreResourceHandle {
    private transient long agpCptrCoreResourceHandle;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreResourceHandle(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreResourceHandle = j;
    }

    static long getCptr(CoreResourceHandle coreResourceHandle) {
        if (coreResourceHandle == null) {
            return 0;
        }
        return coreResourceHandle.agpCptrCoreResourceHandle;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreResourceHandle != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreResourceHandle(this.agpCptrCoreResourceHandle);
                }
                this.agpCptrCoreResourceHandle = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreResourceHandle coreResourceHandle, boolean z) {
        if (coreResourceHandle != null) {
            synchronized (coreResourceHandle.delLock) {
                coreResourceHandle.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public void setId(long j) {
        CoreJni.setVaridCoreResourceHandle(this.agpCptrCoreResourceHandle, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getId() {
        return CoreJni.getVaridCoreResourceHandle(this.agpCptrCoreResourceHandle, this);
    }

    /* access modifiers changed from: package-private */
    public void setType(long j) {
        CoreJni.setVartypeCoreResourceHandle(this.agpCptrCoreResourceHandle, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getType() {
        return CoreJni.getVartypeCoreResourceHandle(this.agpCptrCoreResourceHandle, this);
    }

    CoreResourceHandle() {
        this(CoreJni.newCoreResourceHandle(), true);
    }
}
