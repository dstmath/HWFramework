package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreVec2 {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreVec2(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreVec2 obj) {
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
                CoreJni.deleteCoreVec2(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreVec2() {
        this(CoreJni.newCoreVec20(), true);
    }

    CoreVec2(float xi, float yi) {
        this(CoreJni.newCoreVec21(xi, yi), true);
    }

    /* access modifiers changed from: package-private */
    public void setX(float value) {
        CoreJni.setVarxCoreVec2(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getX() {
        return CoreJni.getVarxCoreVec2(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setY(float value) {
        CoreJni.setVaryCoreVec2(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getY() {
        return CoreJni.getVaryCoreVec2(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setData(float[] value) {
        CoreJni.setVardataCoreVec2(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float[] getData() {
        return CoreJni.getVardataCoreVec2(this.agpCptr, this);
    }
}
