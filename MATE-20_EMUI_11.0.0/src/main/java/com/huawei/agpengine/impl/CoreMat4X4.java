package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreMat4X4 {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreMat4X4(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMat4X4 obj) {
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
                CoreJni.deleteCoreMat4X4(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setX(CoreVec4 value) {
        CoreJni.setVarxCoreMat4X4(this.agpCptr, this, CoreVec4.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4 getX() {
        long cptr = CoreJni.getVarxCoreMat4X4(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec4(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setY(CoreVec4 value) {
        CoreJni.setVaryCoreMat4X4(this.agpCptr, this, CoreVec4.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4 getY() {
        long cptr = CoreJni.getVaryCoreMat4X4(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec4(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setZ(CoreVec4 value) {
        CoreJni.setVarzCoreMat4X4(this.agpCptr, this, CoreVec4.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4 getZ() {
        long cptr = CoreJni.getVarzCoreMat4X4(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec4(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setW(CoreVec4 value) {
        CoreJni.setVarwCoreMat4X4(this.agpCptr, this, CoreVec4.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4 getW() {
        long cptr = CoreJni.getVarwCoreMat4X4(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec4(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setBase(CoreVec4 value) {
        CoreJni.setVarbaseCoreMat4X4(this.agpCptr, this, CoreVec4.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4 getBase() {
        long cptr = CoreJni.getVarbaseCoreMat4X4(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec4(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setData(float[] value) {
        CoreJni.setVardataCoreMat4X4(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float[] getData() {
        return CoreJni.getVardataCoreMat4X4(this.agpCptr, this);
    }

    CoreMat4X4() {
        this(CoreJni.newCoreMat4X4(), true);
    }
}
