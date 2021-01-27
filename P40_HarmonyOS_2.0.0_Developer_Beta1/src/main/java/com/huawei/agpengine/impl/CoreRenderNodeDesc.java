package com.huawei.agpengine.impl;

class CoreRenderNodeDesc {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreRenderNodeDesc(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreRenderNodeDesc obj) {
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
                CoreJni.deleteCoreRenderNodeDesc(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreRenderNodeDesc() {
        this(CoreJni.newCoreRenderNodeDesc(), true);
    }
}
