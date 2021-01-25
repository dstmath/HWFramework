package com.huawei.agpengine.impl;

class CoreImageCreateInfo {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreImageCreateInfo(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreImageCreateInfo obj) {
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
