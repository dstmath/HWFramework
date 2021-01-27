package com.huawei.agpengine.impl;

class CoreContextInfo {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreContextInfo(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreContextInfo obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (this.isAgpCmemOwn) {
                this.isAgpCmemOwn = false;
                CoreJni.deleteCoreContextInfo(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreContextInfo() {
        this(CoreJni.newCoreContextInfo(), true);
    }
}
