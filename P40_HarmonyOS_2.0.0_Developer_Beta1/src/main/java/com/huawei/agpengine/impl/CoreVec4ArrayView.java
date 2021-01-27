package com.huawei.agpengine.impl;

import java.nio.Buffer;

class CoreVec4ArrayView {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreVec4ArrayView(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreVec4ArrayView obj) {
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
                CoreJni.deleteCoreVec4ArrayView(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreVec4ArrayView(Buffer begin) {
        this(CoreJni.newCoreVec4ArrayView(begin), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreVec4ArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4 get(long index) {
        return new CoreVec4(CoreJni.getInCoreVec4ArrayView(this.agpCptr, this, index), true);
    }
}
