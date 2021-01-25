package com.huawei.agpengine.impl;

class CoreSkinCreateInfo {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreSkinCreateInfo(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreSkinCreateInfo obj) {
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
