package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreQuat {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreQuat(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreQuat obj) {
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
                CoreJni.deleteCoreQuat(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreQuat() {
        this(CoreJni.newCoreQuat0(), true);
    }

    CoreQuat(float xi, float yi, float zi, float wi) {
        this(CoreJni.newCoreQuat1(xi, yi, zi, wi), true);
    }

    /* access modifiers changed from: package-private */
    public void setX(float value) {
        CoreJni.setVarxCoreQuat(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getX() {
        return CoreJni.getVarxCoreQuat(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setY(float value) {
        CoreJni.setVaryCoreQuat(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getY() {
        return CoreJni.getVaryCoreQuat(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setZ(float value) {
        CoreJni.setVarzCoreQuat(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getZ() {
        return CoreJni.getVarzCoreQuat(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setW(float value) {
        CoreJni.setVarwCoreQuat(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getW() {
        return CoreJni.getVarwCoreQuat(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setData(float[] value) {
        CoreJni.setVardataCoreQuat(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float[] getData() {
        return CoreJni.getVardataCoreQuat(this.agpCptr, this);
    }
}
