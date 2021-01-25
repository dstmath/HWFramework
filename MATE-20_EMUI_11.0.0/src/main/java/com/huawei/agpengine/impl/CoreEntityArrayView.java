package com.huawei.agpengine.impl;

import java.nio.Buffer;

class CoreEntityArrayView {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreEntityArrayView(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreEntityArrayView obj) {
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
                CoreJni.deleteCoreEntityArrayView(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreEntityArrayView(Buffer begin) {
        this(CoreJni.newCoreEntityArrayView(begin), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreEntityArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public int get(long index) {
        return CoreJni.getInCoreEntityArrayView(this.agpCptr, this, index);
    }
}
