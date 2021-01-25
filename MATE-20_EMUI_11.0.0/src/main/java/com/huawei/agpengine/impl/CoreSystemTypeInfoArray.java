package com.huawei.agpengine.impl;

class CoreSystemTypeInfoArray {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreSystemTypeInfoArray(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreSystemTypeInfoArray obj) {
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
