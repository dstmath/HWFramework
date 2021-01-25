package ohos.agp.render.render3d.impl;

import java.math.BigInteger;

/* access modifiers changed from: package-private */
public class CoreGpuResourceHandle {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreGpuResourceHandle(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreGpuResourceHandle coreGpuResourceHandle) {
        if (coreGpuResourceHandle == null) {
            return 0;
        }
        return coreGpuResourceHandle.agpCptr;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptr != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreGpuResourceHandle(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreGpuResourceHandle coreGpuResourceHandle, boolean z) {
        if (coreGpuResourceHandle != null) {
            synchronized (coreGpuResourceHandle.lock) {
                coreGpuResourceHandle.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreGpuResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public void setId(BigInteger bigInteger) {
        CoreJni.setVaridCoreGpuResourceHandle(this.agpCptr, this, bigInteger);
    }

    /* access modifiers changed from: package-private */
    public BigInteger getId() {
        return CoreJni.getVaridCoreGpuResourceHandle(this.agpCptr, this);
    }

    CoreGpuResourceHandle() {
        this(CoreJni.newCoreGpuResourceHandle(), true);
    }
}
