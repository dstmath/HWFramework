package ohos.agp.render.render3d.impl;

class CoreVec4 {
    private transient long agpCptrCoreVec4;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreVec4(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreVec4 = j;
    }

    static long getCptr(CoreVec4 coreVec4) {
        if (coreVec4 == null) {
            return 0;
        }
        return coreVec4.agpCptrCoreVec4;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreVec4 != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreVec4(this.agpCptrCoreVec4);
                }
                this.agpCptrCoreVec4 = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreVec4 coreVec4, boolean z) {
        if (coreVec4 != null) {
            synchronized (coreVec4.delLock) {
                coreVec4.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreVec4);
    }

    CoreVec4() {
        this(CoreJni.newCoreVec40(), true);
    }

    CoreVec4(float f, float f2, float f3, float f4) {
        this(CoreJni.newCoreVec41(f, f2, f3, f4), true);
    }

    /* access modifiers changed from: package-private */
    public void setX(float f) {
        CoreJni.setVarxCoreVec4(this.agpCptrCoreVec4, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getX() {
        return CoreJni.getVarxCoreVec4(this.agpCptrCoreVec4, this);
    }

    /* access modifiers changed from: package-private */
    public void setY(float f) {
        CoreJni.setVaryCoreVec4(this.agpCptrCoreVec4, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getY() {
        return CoreJni.getVaryCoreVec4(this.agpCptrCoreVec4, this);
    }

    /* access modifiers changed from: package-private */
    public void setZ(float f) {
        CoreJni.setVarzCoreVec4(this.agpCptrCoreVec4, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getZ() {
        return CoreJni.getVarzCoreVec4(this.agpCptrCoreVec4, this);
    }

    /* access modifiers changed from: package-private */
    public void setW(float f) {
        CoreJni.setVarwCoreVec4(this.agpCptrCoreVec4, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getW() {
        return CoreJni.getVarwCoreVec4(this.agpCptrCoreVec4, this);
    }

    /* access modifiers changed from: package-private */
    public void setData(float[] fArr) {
        CoreJni.setVardataCoreVec4(this.agpCptrCoreVec4, this, fArr);
    }

    /* access modifiers changed from: package-private */
    public float[] getData() {
        return CoreJni.getVardataCoreVec4(this.agpCptrCoreVec4, this);
    }
}
