package com.huawei.agpengine.impl;

class CoreGpuImageDesc {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGpuImageDesc(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreGpuImageDesc obj) {
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
