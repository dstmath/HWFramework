package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreRenderNodeGraphInput {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreRenderNodeGraphInput(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreRenderNodeGraphInput obj) {
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
                CoreJni.deleteCoreRenderNodeGraphInput(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setRenderNodeGraphHandle(long value) {
        CoreJni.setVarrenderNodeGraphHandleCoreRenderNodeGraphInput(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getRenderNodeGraphHandle() {
        return CoreJni.getVarrenderNodeGraphHandleCoreRenderNodeGraphInput(this.agpCptr, this);
    }

    CoreRenderNodeGraphInput() {
        this(CoreJni.newCoreRenderNodeGraphInput(), true);
    }
}
