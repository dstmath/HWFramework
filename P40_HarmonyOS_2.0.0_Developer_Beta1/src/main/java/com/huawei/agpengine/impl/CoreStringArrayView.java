package com.huawei.agpengine.impl;

class CoreStringArrayView {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreStringArrayView(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreStringArrayView obj) {
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
                CoreJni.deleteCoreStringArrayView(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreStringArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public String get(long index) {
        return CoreJni.getInCoreStringArrayView(this.agpCptr, this, index);
    }
}
