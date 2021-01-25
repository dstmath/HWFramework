package com.huawei.agpengine.impl;

class CoreGpuSamplerDesc {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGpuSamplerDesc(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreGpuSamplerDesc obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }
}
