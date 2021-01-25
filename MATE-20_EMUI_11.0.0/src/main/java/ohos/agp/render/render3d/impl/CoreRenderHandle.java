package ohos.agp.render.render3d.impl;

import java.math.BigInteger;

/* access modifiers changed from: package-private */
public class CoreRenderHandle {
    private transient long agpCptrCoreRenderHandle;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreRenderHandle(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreRenderHandle = j;
    }

    static long getCptr(CoreRenderHandle coreRenderHandle) {
        if (coreRenderHandle == null) {
            return 0;
        }
        return coreRenderHandle.agpCptrCoreRenderHandle;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreRenderHandle != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreRenderHandle(this.agpCptrCoreRenderHandle);
                }
                this.agpCptrCoreRenderHandle = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreRenderHandle coreRenderHandle, boolean z) {
        if (coreRenderHandle != null) {
            synchronized (coreRenderHandle.delLock) {
                coreRenderHandle.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreRenderHandle);
    }

    /* access modifiers changed from: package-private */
    public void setId(BigInteger bigInteger) {
        CoreJni.setVaridCoreRenderHandle(this.agpCptrCoreRenderHandle, this, bigInteger);
    }

    /* access modifiers changed from: package-private */
    public BigInteger getId() {
        return CoreJni.getVaridCoreRenderHandle(this.agpCptrCoreRenderHandle, this);
    }

    CoreRenderHandle() {
        this(CoreJni.newCoreRenderHandle(), true);
    }
}
