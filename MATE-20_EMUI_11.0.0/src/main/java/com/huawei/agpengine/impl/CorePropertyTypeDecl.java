package com.huawei.agpengine.impl;

import java.math.BigInteger;

class CorePropertyTypeDecl {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CorePropertyTypeDecl(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CorePropertyTypeDecl obj) {
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
                CoreJni.deleteCorePropertyTypeDecl(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setIsArray(boolean isEnabled) {
        CoreJni.setVarisArrayCorePropertyTypeDecl(this.agpCptr, this, isEnabled);
    }

    /* access modifiers changed from: package-private */
    public boolean getIsArray() {
        return CoreJni.getVarisArrayCorePropertyTypeDecl(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setTypeHash(BigInteger value) {
        CoreJni.setVartypeHashCorePropertyTypeDecl(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public BigInteger getTypeHash() {
        return CoreJni.getVartypeHashCorePropertyTypeDecl(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setCompareHash(BigInteger value) {
        CoreJni.setVarcompareHashCorePropertyTypeDecl(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public BigInteger getCompareHash() {
        return CoreJni.getVarcompareHashCorePropertyTypeDecl(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setName(String value) {
        CoreJni.setVarnameCorePropertyTypeDecl(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return CoreJni.getVarnameCorePropertyTypeDecl(this.agpCptr, this);
    }

    CorePropertyTypeDecl() {
        this(CoreJni.newCorePropertyTypeDecl(), true);
    }
}
