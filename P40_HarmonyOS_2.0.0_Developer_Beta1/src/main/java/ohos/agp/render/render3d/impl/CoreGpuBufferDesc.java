package ohos.agp.render.render3d.impl;

class CoreGpuBufferDesc {
    private final transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGpuBufferDesc(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreGpuBufferDesc coreGpuBufferDesc) {
        if (coreGpuBufferDesc == null) {
            return 0;
        }
        return coreGpuBufferDesc.agpCptr;
    }

    static long getCptrAndSetMemOwn(CoreGpuBufferDesc coreGpuBufferDesc, boolean z) {
        if (coreGpuBufferDesc != null) {
            coreGpuBufferDesc.isAgpCmemOwn = z;
        }
        return getCptr(coreGpuBufferDesc);
    }
}
