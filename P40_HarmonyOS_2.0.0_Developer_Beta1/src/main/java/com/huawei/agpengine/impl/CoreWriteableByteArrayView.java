package com.huawei.agpengine.impl;

import java.nio.Buffer;

/* access modifiers changed from: package-private */
public class CoreWriteableByteArrayView {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreWriteableByteArrayView(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreWriteableByteArrayView obj) {
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
                CoreJni.deleteCoreWriteableByteArrayView(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreWriteableByteArrayView(Buffer begin) {
        this(CoreJni.newCoreWriteableByteArrayView(begin), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreWriteableByteArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public short get(long index) {
        return CoreJni.getInCoreWriteableByteArrayView(this.agpCptr, this, index);
    }
}
