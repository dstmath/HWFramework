package com.huawei.agpengine.impl;

class CoreBufferImageCopy {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreBufferImageCopy(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreBufferImageCopy obj) {
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
