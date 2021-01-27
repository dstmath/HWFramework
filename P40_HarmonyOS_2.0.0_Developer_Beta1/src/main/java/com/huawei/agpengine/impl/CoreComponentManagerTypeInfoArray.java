package com.huawei.agpengine.impl;

class CoreComponentManagerTypeInfoArray {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreComponentManagerTypeInfoArray(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreComponentManagerTypeInfoArray obj) {
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
