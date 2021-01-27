package com.huawei.agpengine.impl;

import java.nio.Buffer;

class CoreStringViewArrayView {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreStringViewArrayView(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreStringViewArrayView obj) {
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
                CoreJni.deleteCoreStringViewArrayView(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreStringViewArrayView(Buffer begin) {
        this(CoreJni.newCoreStringViewArrayView(begin), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreStringViewArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public String get(long index) {
        return CoreJni.getInCoreStringViewArrayView(this.agpCptr, this, index);
    }
}
