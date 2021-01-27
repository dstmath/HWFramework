package com.huawei.agpengine.impl;

import java.nio.Buffer;

class CoreVec2ArrayView {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreVec2ArrayView(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreVec2ArrayView obj) {
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
                CoreJni.deleteCoreVec2ArrayView(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreVec2ArrayView(Buffer begin) {
        this(CoreJni.newCoreVec2ArrayView(begin), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreVec2ArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreVec2 get(long index) {
        return new CoreVec2(CoreJni.getInCoreVec2ArrayView(this.agpCptr, this, index), true);
    }
}
