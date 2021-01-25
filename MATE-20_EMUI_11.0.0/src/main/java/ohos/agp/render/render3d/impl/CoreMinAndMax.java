package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreMinAndMax {
    private transient long agpCptrCoreMinAndMax;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreMinAndMax(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreMinAndMax = j;
    }

    static long getCptr(CoreMinAndMax coreMinAndMax) {
        if (coreMinAndMax == null) {
            return 0;
        }
        return coreMinAndMax.agpCptrCoreMinAndMax;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreMinAndMax != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreMinAndMax(this.agpCptrCoreMinAndMax);
                }
                this.agpCptrCoreMinAndMax = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreMinAndMax coreMinAndMax, boolean z) {
        if (coreMinAndMax != null) {
            synchronized (coreMinAndMax.delLock) {
                coreMinAndMax.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreMinAndMax);
    }

    /* access modifiers changed from: package-private */
    public void setMinAabb(CoreVec3 coreVec3) {
        CoreJni.setVarminAabbCoreMinAndMax(this.agpCptrCoreMinAndMax, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getMinAabb() {
        long varminAabbCoreMinAndMax = CoreJni.getVarminAabbCoreMinAndMax(this.agpCptrCoreMinAndMax, this);
        if (varminAabbCoreMinAndMax == 0) {
            return null;
        }
        return new CoreVec3(varminAabbCoreMinAndMax, false);
    }

    /* access modifiers changed from: package-private */
    public void setMaxAabb(CoreVec3 coreVec3) {
        CoreJni.setVarmaxAabbCoreMinAndMax(this.agpCptrCoreMinAndMax, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getMaxAabb() {
        long varmaxAabbCoreMinAndMax = CoreJni.getVarmaxAabbCoreMinAndMax(this.agpCptrCoreMinAndMax, this);
        if (varmaxAabbCoreMinAndMax == 0) {
            return null;
        }
        return new CoreVec3(varmaxAabbCoreMinAndMax, false);
    }

    CoreMinAndMax() {
        this(CoreJni.newCoreMinAndMax(), true);
    }
}
