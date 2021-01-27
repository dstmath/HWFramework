package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreVec3 {
    private transient long agpCptrCoreVec3;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreVec3(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreVec3 = j;
    }

    static long getCptr(CoreVec3 coreVec3) {
        if (coreVec3 == null) {
            return 0;
        }
        return coreVec3.agpCptrCoreVec3;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreVec3 != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreVec3(this.agpCptrCoreVec3);
                }
                this.agpCptrCoreVec3 = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreVec3 coreVec3, boolean z) {
        if (coreVec3 != null) {
            synchronized (coreVec3.delLock) {
                coreVec3.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreVec3);
    }

    CoreVec3() {
        this(CoreJni.newCoreVec30(), true);
    }

    CoreVec3(float f, float f2, float f3) {
        this(CoreJni.newCoreVec31(f, f2, f3), true);
    }

    /* access modifiers changed from: package-private */
    public void setX(float f) {
        CoreJni.setVarxCoreVec3(this.agpCptrCoreVec3, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getX() {
        return CoreJni.getVarxCoreVec3(this.agpCptrCoreVec3, this);
    }

    /* access modifiers changed from: package-private */
    public void setY(float f) {
        CoreJni.setVaryCoreVec3(this.agpCptrCoreVec3, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getY() {
        return CoreJni.getVaryCoreVec3(this.agpCptrCoreVec3, this);
    }

    /* access modifiers changed from: package-private */
    public void setZ(float f) {
        CoreJni.setVarzCoreVec3(this.agpCptrCoreVec3, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getZ() {
        return CoreJni.getVarzCoreVec3(this.agpCptrCoreVec3, this);
    }

    /* access modifiers changed from: package-private */
    public void setData(float[] fArr) {
        CoreJni.setVardataCoreVec3(this.agpCptrCoreVec3, this, fArr);
    }

    /* access modifiers changed from: package-private */
    public float[] getData() {
        return CoreJni.getVardataCoreVec3(this.agpCptrCoreVec3, this);
    }
}
