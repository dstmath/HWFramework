package com.huawei.agpengine.impl;

class CoreEcsPtr {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreEcsPtr(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreEcsPtr obj) {
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
                CoreJni.deleteCoreEcsPtr(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreEcsPtr(CoreEcs Ptr) {
        this(CoreJni.newCoreEcsPtr0(CoreEcs.getCptr(Ptr), Ptr), true);
    }

    CoreEcsPtr(CoreEcsPtr Right) {
        this(CoreJni.newCoreEcsPtr1(getCptr(Right), Right), true);
    }

    /* access modifiers changed from: package-private */
    public CoreEcs get() {
        long cptr = CoreJni.getInCoreEcsPtr(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreEcs(cptr, false);
    }
}
