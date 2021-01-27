package com.huawei.agpengine.impl;

import java.nio.Buffer;

class CoreRenderNodeGraphInputArrayView {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreRenderNodeGraphInputArrayView(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreRenderNodeGraphInputArrayView obj) {
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
                CoreJni.deleteCoreRenderNodeGraphInputArrayView(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreRenderNodeGraphInputArrayView(Buffer begin) {
        this(CoreJni.newCoreRenderNodeGraphInputArrayView(begin), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreRenderNodeGraphInputArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderNodeGraphInput get(long index) {
        return new CoreRenderNodeGraphInput(CoreJni.getInCoreRenderNodeGraphInputArrayView(this.agpCptr, this, index), true);
    }
}
