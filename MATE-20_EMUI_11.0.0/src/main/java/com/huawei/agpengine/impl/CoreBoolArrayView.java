package com.huawei.agpengine.impl;

import java.nio.Buffer;

class CoreBoolArrayView {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreBoolArrayView(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreBoolArrayView obj) {
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
                CoreJni.deleteCoreBoolArrayView(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreBoolArrayView(Buffer begin) {
        this(CoreJni.newCoreBoolArrayView(begin), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreBoolArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public boolean get(long index) {
        return CoreJni.getInCoreBoolArrayView(this.agpCptr, this, index);
    }
}
