package com.huawei.agpengine.impl;

import java.nio.Buffer;

class CoreRenderNodeDescArrayView {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreRenderNodeDescArrayView(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreRenderNodeDescArrayView obj) {
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
                CoreJni.deleteCoreRenderNodeDescArrayView(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreRenderNodeDescArrayView(Buffer begin) {
        this(CoreJni.newCoreRenderNodeDescArrayView(begin), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreRenderNodeDescArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderNodeDesc get(long index) {
        return new CoreRenderNodeDesc(CoreJni.getInCoreRenderNodeDescArrayView(this.agpCptr, this, index), true);
    }
}
