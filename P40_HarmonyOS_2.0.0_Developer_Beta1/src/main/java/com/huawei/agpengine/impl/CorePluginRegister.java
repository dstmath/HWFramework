package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CorePluginRegister {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CorePluginRegister(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CorePluginRegister obj) {
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
