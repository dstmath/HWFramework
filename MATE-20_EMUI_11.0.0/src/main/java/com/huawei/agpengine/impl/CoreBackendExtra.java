package com.huawei.agpengine.impl;

class CoreBackendExtra {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreBackendExtra(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreBackendExtra obj) {
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
                CoreJni.deleteCoreBackendExtra(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreBackendExtra() {
        this(CoreJni.newCoreBackendExtra(), true);
    }
}
