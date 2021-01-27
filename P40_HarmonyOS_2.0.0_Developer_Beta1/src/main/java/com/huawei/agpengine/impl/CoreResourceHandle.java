package com.huawei.agpengine.impl;

import java.math.BigInteger;

/* access modifiers changed from: package-private */
public class CoreResourceHandle {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreResourceHandle(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreResourceHandle obj) {
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
                CoreJni.deleteCoreResourceHandle(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setId(long value) {
        CoreJni.setVaridCoreResourceHandle(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getId() {
        return CoreJni.getVaridCoreResourceHandle(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setType(BigInteger value) {
        CoreJni.setVartypeCoreResourceHandle(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public BigInteger getType() {
        return CoreJni.getVartypeCoreResourceHandle(this.agpCptr, this);
    }

    CoreResourceHandle() {
        this(CoreJni.newCoreResourceHandle(), true);
    }
}
