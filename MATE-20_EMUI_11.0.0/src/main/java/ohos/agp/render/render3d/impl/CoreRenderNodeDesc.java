package ohos.agp.render.render3d.impl;

class CoreRenderNodeDesc {
    private transient long agpCptrCoreRenderNodeDesc;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreRenderNodeDesc(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreRenderNodeDesc = j;
    }

    static long getCptr(CoreRenderNodeDesc coreRenderNodeDesc) {
        if (coreRenderNodeDesc == null) {
            return 0;
        }
        return coreRenderNodeDesc.agpCptrCoreRenderNodeDesc;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreRenderNodeDesc != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreRenderNodeDesc(this.agpCptrCoreRenderNodeDesc);
                }
                this.agpCptrCoreRenderNodeDesc = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreRenderNodeDesc coreRenderNodeDesc, boolean z) {
        if (coreRenderNodeDesc != null) {
            synchronized (coreRenderNodeDesc.delLock) {
                coreRenderNodeDesc.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreRenderNodeDesc);
    }

    CoreRenderNodeDesc() {
        this(CoreJni.newCoreRenderNodeDesc(), true);
    }
}
