package com.huawei.agpengine.impl;

class CoreMinAndMax {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreMinAndMax(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMinAndMax obj) {
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
                CoreJni.deleteCoreMinAndMax(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setMinAabb(CoreVec3 value) {
        CoreJni.setVarminAabbCoreMinAndMax(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getMinAabb() {
        long cptr = CoreJni.getVarminAabbCoreMinAndMax(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setMaxAabb(CoreVec3 value) {
        CoreJni.setVarmaxAabbCoreMinAndMax(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getMaxAabb() {
        long cptr = CoreJni.getVarmaxAabbCoreMinAndMax(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    CoreMinAndMax() {
        this(CoreJni.newCoreMinAndMax(), true);
    }
}
