package com.huawei.agpengine.impl;

class CoreResourceDataHandle {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreResourceDataHandle(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreResourceDataHandle obj) {
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
                CoreJni.deleteCoreResourceDataHandle(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setId(long value) {
        CoreJni.setVaridCoreResourceDataHandle(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getId() {
        return CoreJni.getVaridCoreResourceDataHandle(this.agpCptr, this);
    }

    CoreResourceDataHandle() {
        this(CoreJni.newCoreResourceDataHandle(), true);
    }
}
