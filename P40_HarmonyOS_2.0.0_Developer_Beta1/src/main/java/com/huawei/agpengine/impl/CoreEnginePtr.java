package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreEnginePtr {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreEnginePtr(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreEnginePtr obj) {
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
                CoreJni.deleteCoreEnginePtr(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreEnginePtr(CoreEngine ptr) {
        this(CoreJni.newCoreEnginePtr0(CoreEngine.getCptr(ptr), ptr), true);
    }

    CoreEnginePtr(CoreEnginePtr ptr) {
        this(CoreJni.newCoreEnginePtr1(getCptr(ptr), ptr), true);
    }

    /* access modifiers changed from: package-private */
    public CoreEngine get() {
        long cptr = CoreJni.getInCoreEnginePtr(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreEngine(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void reset(CoreEngine ptr) {
        CoreJni.resetInCoreEnginePtr0(this.agpCptr, this, CoreEngine.getCptr(ptr), ptr);
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        CoreJni.resetInCoreEnginePtr1(this.agpCptr, this);
    }
}
