package com.huawei.agpengine.impl;

import java.nio.Buffer;

/* access modifiers changed from: package-private */
public class CoreVec3ArrayView {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreVec3ArrayView(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreVec3ArrayView obj) {
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
                CoreJni.deleteCoreVec3ArrayView(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreVec3ArrayView(Buffer begin) {
        this(CoreJni.newCoreVec3ArrayView(begin), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreVec3ArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 get(long index) {
        return new CoreVec3(CoreJni.getInCoreVec3ArrayView(this.agpCptr, this, index), true);
    }
}
