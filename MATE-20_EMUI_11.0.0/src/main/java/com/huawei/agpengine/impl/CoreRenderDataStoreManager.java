package com.huawei.agpengine.impl;

class CoreRenderDataStoreManager {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreRenderDataStoreManager(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreRenderDataStoreManager obj) {
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
