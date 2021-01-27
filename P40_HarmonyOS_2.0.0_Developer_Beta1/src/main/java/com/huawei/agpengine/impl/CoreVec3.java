package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreVec3 {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreVec3(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreVec3 obj) {
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
                CoreJni.deleteCoreVec3(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreVec3() {
        this(CoreJni.newCoreVec30(), true);
    }

    CoreVec3(float xi, float yi, float zi) {
        this(CoreJni.newCoreVec31(xi, yi, zi), true);
    }

    /* access modifiers changed from: package-private */
    public void setX(float value) {
        CoreJni.setVarxCoreVec3(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getX() {
        return CoreJni.getVarxCoreVec3(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setY(float value) {
        CoreJni.setVaryCoreVec3(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getY() {
        return CoreJni.getVaryCoreVec3(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setZ(float value) {
        CoreJni.setVarzCoreVec3(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getZ() {
        return CoreJni.getVarzCoreVec3(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setData(float[] value) {
        CoreJni.setVardataCoreVec3(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float[] getData() {
        return CoreJni.getVardataCoreVec3(this.agpCptr, this);
    }
}
