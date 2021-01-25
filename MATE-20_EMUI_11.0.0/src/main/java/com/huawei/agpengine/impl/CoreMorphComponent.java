package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreMorphComponent {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreMorphComponent(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMorphComponent obj) {
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
                CoreJni.deleteCoreMorphComponent(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreMorphComponent() {
        this(CoreJni.newCoreMorphComponent(), true);
    }
}
