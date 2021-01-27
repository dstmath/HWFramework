package com.huawei.agpengine.impl;

class CoreUVec2 {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreUVec2(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreUVec2 obj) {
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
                CoreJni.deleteCoreUVec2(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreUVec2() {
        this(CoreJni.newCoreUVec20(), true);
    }

    CoreUVec2(int xi, int yi) {
        this(CoreJni.newCoreUVec21(xi, yi), true);
    }

    /* access modifiers changed from: package-private */
    public void setX(int value) {
        CoreJni.setVarxCoreUVec2(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public int getX() {
        return CoreJni.getVarxCoreUVec2(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setY(int value) {
        CoreJni.setVaryCoreUVec2(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public int getY() {
        return CoreJni.getVaryCoreUVec2(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setData(int[] value) {
        CoreJni.setVardataCoreUVec2(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public int[] getData() {
        return CoreJni.getVardataCoreUVec2(this.agpCptr, this);
    }
}
