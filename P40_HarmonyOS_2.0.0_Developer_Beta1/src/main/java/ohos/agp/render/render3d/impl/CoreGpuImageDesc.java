package ohos.agp.render.render3d.impl;

class CoreGpuImageDesc {
    private final transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGpuImageDesc(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreGpuImageDesc coreGpuImageDesc) {
        if (coreGpuImageDesc == null) {
            return 0;
        }
        return coreGpuImageDesc.agpCptr;
    }

    static long getCptrAndSetMemOwn(CoreGpuImageDesc coreGpuImageDesc, boolean z) {
        if (coreGpuImageDesc != null) {
            coreGpuImageDesc.isAgpCmemOwn = z;
        }
        return getCptr(coreGpuImageDesc);
    }
}
