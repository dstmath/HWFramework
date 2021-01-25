package com.huawei.agpengine.impl;

class CoreMat3X3 {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreMat3X3(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMat3X3 obj) {
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
                CoreJni.deleteCoreMat3X3(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setX(CoreVec3 value) {
        CoreJni.setVarxCoreMat3X3(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getX() {
        long cptr = CoreJni.getVarxCoreMat3X3(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setY(CoreVec3 value) {
        CoreJni.setVaryCoreMat3X3(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getY() {
        long cptr = CoreJni.getVaryCoreMat3X3(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setZ(CoreVec3 value) {
        CoreJni.setVarzCoreMat3X3(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getZ() {
        long cptr = CoreJni.getVarzCoreMat3X3(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setBase(CoreVec3 value) {
        CoreJni.setVarbaseCoreMat3X3(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getBase() {
        long cptr = CoreJni.getVarbaseCoreMat3X3(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setData(float[] value) {
        CoreJni.setVardataCoreMat3X3(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float[] getData() {
        return CoreJni.getVardataCoreMat3X3(this.agpCptr, this);
    }

    CoreMat3X3() {
        this(CoreJni.newCoreMat3X3(), true);
    }
}
