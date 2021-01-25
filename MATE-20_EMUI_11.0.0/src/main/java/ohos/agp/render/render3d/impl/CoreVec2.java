package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreVec2 {
    private transient long agpCptrCoreVec2;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreVec2(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreVec2 = j;
    }

    static long getCptr(CoreVec2 coreVec2) {
        if (coreVec2 == null) {
            return 0;
        }
        return coreVec2.agpCptrCoreVec2;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreVec2 != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreVec2(this.agpCptrCoreVec2);
                }
                this.agpCptrCoreVec2 = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreVec2 coreVec2, boolean z) {
        if (coreVec2 != null) {
            synchronized (coreVec2.delLock) {
                coreVec2.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreVec2);
    }

    CoreVec2() {
        this(CoreJni.newCoreVec20(), true);
    }

    CoreVec2(float f, float f2) {
        this(CoreJni.newCoreVec21(f, f2), true);
    }

    /* access modifiers changed from: package-private */
    public void setX(float f) {
        CoreJni.setVarxCoreVec2(this.agpCptrCoreVec2, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getX() {
        return CoreJni.getVarxCoreVec2(this.agpCptrCoreVec2, this);
    }

    /* access modifiers changed from: package-private */
    public void setY(float f) {
        CoreJni.setVaryCoreVec2(this.agpCptrCoreVec2, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getY() {
        return CoreJni.getVaryCoreVec2(this.agpCptrCoreVec2, this);
    }

    /* access modifiers changed from: package-private */
    public void setData(float[] fArr) {
        CoreJni.setVardataCoreVec2(this.agpCptrCoreVec2, this, fArr);
    }

    /* access modifiers changed from: package-private */
    public float[] getData() {
        return CoreJni.getVardataCoreVec2(this.agpCptrCoreVec2, this);
    }
}
