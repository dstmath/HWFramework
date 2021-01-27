package com.huawei.agpengine.impl;

class CorePickingPtr {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CorePickingPtr(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CorePickingPtr obj) {
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
                CoreJni.deleteCorePickingPtr(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CorePickingPtr(CorePicking ptr) {
        this(CoreJni.newCorePickingPtr0(CorePicking.getCptr(ptr), ptr), true);
    }

    CorePickingPtr(CorePickingPtr ptr) {
        this(CoreJni.newCorePickingPtr1(getCptr(ptr), ptr), true);
    }

    /* access modifiers changed from: package-private */
    public CorePicking get() {
        long cptr = CoreJni.getInCorePickingPtr(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CorePicking(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void reset(CorePicking ptr) {
        CoreJni.resetInCorePickingPtr0(this.agpCptr, this, CorePicking.getCptr(ptr), ptr);
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        CoreJni.resetInCorePickingPtr1(this.agpCptr, this);
    }
}
