package com.huawei.agpengine.impl;

class CoreSkin {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreSkin(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreSkin obj) {
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
