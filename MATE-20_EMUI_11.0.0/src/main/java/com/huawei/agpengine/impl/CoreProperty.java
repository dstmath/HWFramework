package com.huawei.agpengine.impl;

import java.math.BigInteger;

class CoreProperty {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreProperty(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreProperty obj) {
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
                CoreJni.deleteCoreProperty(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return CoreJni.getVarnameCoreProperty(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public BigInteger getHash() {
        return CoreJni.getVarhashCoreProperty(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CorePropertyTypeDecl getType() {
        long cptr = CoreJni.getVartypeCoreProperty(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CorePropertyTypeDecl(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public long getCount() {
        return CoreJni.getVarcountCoreProperty(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public long getSize() {
        return CoreJni.getVarsizeCoreProperty(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public BigInteger getOffset() {
        return CoreJni.getVaroffsetCoreProperty(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public String getDisplayName() {
        return CoreJni.getVardisplayNameCoreProperty(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public long getFlags() {
        return CoreJni.getVarflagsCoreProperty(this.agpCptr, this);
    }
}
