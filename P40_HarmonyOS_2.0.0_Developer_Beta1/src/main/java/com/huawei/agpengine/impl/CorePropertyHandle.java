package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CorePropertyHandle {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CorePropertyHandle(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CorePropertyHandle obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: package-private */
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public CorePropertyApi owner() {
        long cptr = CoreJni.ownerInCorePropertyHandle(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CorePropertyApi(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCorePropertyHandle(this.agpCptr, this);
    }
}
