package com.huawei.agpengine.impl;

import java.nio.Buffer;

/* access modifiers changed from: package-private */
public class CoreByteArrayView {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreByteArrayView(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreByteArrayView obj) {
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
                CoreJni.deleteCoreByteArrayView(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreByteArrayView(Buffer begin) {
        this(CoreJni.newCoreByteArrayView(begin), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreByteArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public short get(long index) {
        return CoreJni.getInCoreByteArrayView(this.agpCptr, this, index);
    }
}
