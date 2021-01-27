package com.huawei.agpengine.impl;

class CoreInterfacePtr {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreInterfacePtr(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreInterfacePtr obj) {
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
                CoreJni.deleteCoreInterfacePtr(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreInterfacePtr(CoreInterface ptr) {
        this(CoreJni.newCoreInterfacePtr0(CoreInterface.getCptr(ptr), ptr), true);
    }

    CoreInterfacePtr(CoreInterfacePtr ptr) {
        this(CoreJni.newCoreInterfacePtr1(getCptr(ptr), ptr), true);
    }

    /* access modifiers changed from: package-private */
    public CoreInterface get() {
        long cptr = CoreJni.getInCoreInterfacePtr(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreInterface(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void reset(CoreInterface ptr) {
        CoreJni.resetInCoreInterfacePtr0(this.agpCptr, this, CoreInterface.getCptr(ptr), ptr);
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        CoreJni.resetInCoreInterfacePtr1(this.agpCptr, this);
    }
}
