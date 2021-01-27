package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreNativeWindow {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreNativeWindow(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreNativeWindow obj) {
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
                CoreJni.deleteCoreNativeWindow(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isValid() {
        return CoreJni.isValidInCoreNativeWindow(this.agpCptr, this);
    }

    CoreNativeWindow() {
        this(CoreJni.newCoreNativeWindow(), true);
    }
}
