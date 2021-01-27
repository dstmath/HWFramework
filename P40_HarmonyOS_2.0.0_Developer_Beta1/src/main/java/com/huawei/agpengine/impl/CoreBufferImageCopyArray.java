package com.huawei.agpengine.impl;

import java.nio.Buffer;

class CoreBufferImageCopyArray {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreBufferImageCopyArray(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreBufferImageCopyArray obj) {
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
                CoreJni.deleteCoreBufferImageCopyArray(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreBufferImageCopyArray(Buffer begin) {
        this(CoreJni.newCoreBufferImageCopyArray(begin), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreBufferImageCopyArray(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreBufferImageCopy get(long index) {
        return new CoreBufferImageCopy(CoreJni.getInCoreBufferImageCopyArray(this.agpCptr, this, index), true);
    }
}
