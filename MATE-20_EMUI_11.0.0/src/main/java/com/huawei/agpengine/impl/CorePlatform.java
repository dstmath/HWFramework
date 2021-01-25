package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CorePlatform {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CorePlatform(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CorePlatform obj) {
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
