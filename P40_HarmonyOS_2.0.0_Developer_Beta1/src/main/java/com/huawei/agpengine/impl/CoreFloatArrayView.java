package com.huawei.agpengine.impl;

import java.nio.Buffer;

class CoreFloatArrayView {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreFloatArrayView(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreFloatArrayView obj) {
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
                CoreJni.deleteCoreFloatArrayView(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreFloatArrayView(Buffer begin) {
        this(CoreJni.newCoreFloatArrayView(begin), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreFloatArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public float get(long index) {
        return CoreJni.getInCoreFloatArrayView(this.agpCptr, this, index);
    }
}
