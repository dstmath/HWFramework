package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreSystemGraphLoaderPtr {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreSystemGraphLoaderPtr(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreSystemGraphLoaderPtr obj) {
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
                CoreJni.deleteCoreSystemGraphLoaderPtr(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreSystemGraphLoaderPtr(CoreSystemGraphLoader Ptr) {
        this(CoreJni.newCoreSystemGraphLoaderPtr0(CoreSystemGraphLoader.getCptr(Ptr), Ptr), true);
    }

    CoreSystemGraphLoaderPtr(CoreSystemGraphLoaderPtr Right) {
        this(CoreJni.newCoreSystemGraphLoaderPtr1(getCptr(Right), Right), true);
    }

    /* access modifiers changed from: package-private */
    public CoreSystemGraphLoader get() {
        long cptr = CoreJni.getInCoreSystemGraphLoaderPtr(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreSystemGraphLoader(cptr, false);
    }
}
