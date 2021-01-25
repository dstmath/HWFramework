package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreMat4X4 {
    private transient long agpCptrCoreMat4X4;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreMat4X4(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreMat4X4 = j;
    }

    static long getCptr(CoreMat4X4 coreMat4X4) {
        if (coreMat4X4 == null) {
            return 0;
        }
        return coreMat4X4.agpCptrCoreMat4X4;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreMat4X4 != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreMat4X4(this.agpCptrCoreMat4X4);
                }
                this.agpCptrCoreMat4X4 = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreMat4X4 coreMat4X4, boolean z) {
        if (coreMat4X4 != null) {
            synchronized (coreMat4X4.delLock) {
                coreMat4X4.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreMat4X4);
    }

    /* access modifiers changed from: package-private */
    public void setX(CoreVec4 coreVec4) {
        CoreJni.setVarxCoreMat4X4(this.agpCptrCoreMat4X4, this, CoreVec4.getCptr(coreVec4), coreVec4);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4 getX() {
        long varxCoreMat4X4 = CoreJni.getVarxCoreMat4X4(this.agpCptrCoreMat4X4, this);
        if (varxCoreMat4X4 == 0) {
            return null;
        }
        return new CoreVec4(varxCoreMat4X4, false);
    }

    /* access modifiers changed from: package-private */
    public void setY(CoreVec4 coreVec4) {
        CoreJni.setVaryCoreMat4X4(this.agpCptrCoreMat4X4, this, CoreVec4.getCptr(coreVec4), coreVec4);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4 getY() {
        long varyCoreMat4X4 = CoreJni.getVaryCoreMat4X4(this.agpCptrCoreMat4X4, this);
        if (varyCoreMat4X4 == 0) {
            return null;
        }
        return new CoreVec4(varyCoreMat4X4, false);
    }

    /* access modifiers changed from: package-private */
    public void setZ(CoreVec4 coreVec4) {
        CoreJni.setVarzCoreMat4X4(this.agpCptrCoreMat4X4, this, CoreVec4.getCptr(coreVec4), coreVec4);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4 getZ() {
        long varzCoreMat4X4 = CoreJni.getVarzCoreMat4X4(this.agpCptrCoreMat4X4, this);
        if (varzCoreMat4X4 == 0) {
            return null;
        }
        return new CoreVec4(varzCoreMat4X4, false);
    }

    /* access modifiers changed from: package-private */
    public void setW(CoreVec4 coreVec4) {
        CoreJni.setVarwCoreMat4X4(this.agpCptrCoreMat4X4, this, CoreVec4.getCptr(coreVec4), coreVec4);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4 getW() {
        long varwCoreMat4X4 = CoreJni.getVarwCoreMat4X4(this.agpCptrCoreMat4X4, this);
        if (varwCoreMat4X4 == 0) {
            return null;
        }
        return new CoreVec4(varwCoreMat4X4, false);
    }

    /* access modifiers changed from: package-private */
    public void setBase(CoreVec4 coreVec4) {
        CoreJni.setVarbaseCoreMat4X4(this.agpCptrCoreMat4X4, this, CoreVec4.getCptr(coreVec4), coreVec4);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4 getBase() {
        long varbaseCoreMat4X4 = CoreJni.getVarbaseCoreMat4X4(this.agpCptrCoreMat4X4, this);
        if (varbaseCoreMat4X4 == 0) {
            return null;
        }
        return new CoreVec4(varbaseCoreMat4X4, false);
    }

    /* access modifiers changed from: package-private */
    public void setData(float[] fArr) {
        CoreJni.setVardataCoreMat4X4(this.agpCptrCoreMat4X4, this, fArr);
    }

    /* access modifiers changed from: package-private */
    public float[] getData() {
        return CoreJni.getVardataCoreMat4X4(this.agpCptrCoreMat4X4, this);
    }

    CoreMat4X4() {
        this(CoreJni.newCoreMat4X4(), true);
    }
}
