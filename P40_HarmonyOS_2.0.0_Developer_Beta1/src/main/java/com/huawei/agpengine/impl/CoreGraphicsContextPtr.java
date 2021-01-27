package com.huawei.agpengine.impl;

class CoreGraphicsContextPtr {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGraphicsContextPtr(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreGraphicsContextPtr obj) {
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
                CoreJni.deleteCoreGraphicsContextPtr(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreGraphicsContextPtr(CoreGraphicsContext ptr) {
        this(CoreJni.newCoreGraphicsContextPtr0(CoreGraphicsContext.getCptr(ptr), ptr), true);
    }

    CoreGraphicsContextPtr(CoreGraphicsContextPtr ptr) {
        this(CoreJni.newCoreGraphicsContextPtr1(getCptr(ptr), ptr), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGraphicsContext get() {
        long cptr = CoreJni.getInCoreGraphicsContextPtr(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreGraphicsContext(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void reset(CoreGraphicsContext ptr) {
        CoreJni.resetInCoreGraphicsContextPtr0(this.agpCptr, this, CoreGraphicsContext.getCptr(ptr), ptr);
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        CoreJni.resetInCoreGraphicsContextPtr1(this.agpCptr, this);
    }

    static CoreGraphicsContextPtr dynamicCast(CoreInterfacePtr ptr) {
        return new CoreGraphicsContextPtr(CoreJni.dynamicCastInCoreGraphicsContextPtr(CoreInterfacePtr.getCptr(ptr), ptr), true);
    }

    static CoreGraphicsContextPtr create(CorePluginRegister ptr) {
        return new CoreGraphicsContextPtr(CoreJni.createInCoreGraphicsContextPtr(CorePluginRegister.getCptr(ptr), ptr), true);
    }
}
