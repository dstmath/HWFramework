package com.huawei.agpengine.impl;

import java.math.BigInteger;

/* access modifiers changed from: package-private */
public class CoreEngineTime {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreEngineTime(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreEngineTime obj) {
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
                CoreJni.deleteCoreEngineTime(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setTotalTimeUs(BigInteger value) {
        CoreJni.setVartotalTimeUsCoreEngineTime(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public BigInteger getTotalTimeUs() {
        return CoreJni.getVartotalTimeUsCoreEngineTime(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setDeltaTimeUs(BigInteger value) {
        CoreJni.setVardeltaTimeUsCoreEngineTime(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public BigInteger getDeltaTimeUs() {
        return CoreJni.getVardeltaTimeUsCoreEngineTime(this.agpCptr, this);
    }

    CoreEngineTime() {
        this(CoreJni.newCoreEngineTime(), true);
    }
}
