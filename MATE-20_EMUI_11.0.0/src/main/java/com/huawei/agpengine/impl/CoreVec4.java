package com.huawei.agpengine.impl;

class CoreVec4 {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreVec4(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreVec4 obj) {
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
                CoreJni.deleteCoreVec4(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreVec4() {
        this(CoreJni.newCoreVec40(), true);
    }

    CoreVec4(float xi, float yi, float zi, float wi) {
        this(CoreJni.newCoreVec41(xi, yi, zi, wi), true);
    }

    /* access modifiers changed from: package-private */
    public void setX(float value) {
        CoreJni.setVarxCoreVec4(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getX() {
        return CoreJni.getVarxCoreVec4(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setY(float value) {
        CoreJni.setVaryCoreVec4(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getY() {
        return CoreJni.getVaryCoreVec4(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setZ(float value) {
        CoreJni.setVarzCoreVec4(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getZ() {
        return CoreJni.getVarzCoreVec4(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setW(float value) {
        CoreJni.setVarwCoreVec4(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getW() {
        return CoreJni.getVarwCoreVec4(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setData(float[] value) {
        CoreJni.setVardataCoreVec4(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float[] getData() {
        return CoreJni.getVardataCoreVec4(this.agpCptr, this);
    }
}
