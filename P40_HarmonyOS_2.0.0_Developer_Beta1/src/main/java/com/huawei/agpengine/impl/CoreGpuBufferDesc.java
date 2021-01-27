package com.huawei.agpengine.impl;

class CoreGpuBufferDesc {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGpuBufferDesc(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreGpuBufferDesc obj) {
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
