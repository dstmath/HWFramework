package ohos.agp.render.render3d.impl;

class CoreGpuSamplerDesc {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGpuSamplerDesc(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreGpuSamplerDesc coreGpuSamplerDesc) {
        if (coreGpuSamplerDesc == null) {
            return 0;
        }
        return coreGpuSamplerDesc.agpCptr;
    }

    static long getCptrAndSetMemOwn(CoreGpuSamplerDesc coreGpuSamplerDesc, boolean z) {
        if (coreGpuSamplerDesc != null) {
            coreGpuSamplerDesc.isAgpCmemOwn = z;
        }
        return getCptr(coreGpuSamplerDesc);
    }
}
