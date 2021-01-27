package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreQuat {
    private transient long agpCptrCoreQuat;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreQuat(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreQuat = j;
    }

    static long getCptr(CoreQuat coreQuat) {
        if (coreQuat == null) {
            return 0;
        }
        return coreQuat.agpCptrCoreQuat;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreQuat != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreQuat(this.agpCptrCoreQuat);
                }
                this.agpCptrCoreQuat = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreQuat coreQuat, boolean z) {
        if (coreQuat != null) {
            synchronized (coreQuat.delLock) {
                coreQuat.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreQuat);
    }

    CoreQuat() {
        this(CoreJni.newCoreQuat0(), true);
    }

    CoreQuat(float f, float f2, float f3, float f4) {
        this(CoreJni.newCoreQuat1(f, f2, f3, f4), true);
    }

    /* access modifiers changed from: package-private */
    public void setX(float f) {
        CoreJni.setVarxCoreQuat(this.agpCptrCoreQuat, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getX() {
        return CoreJni.getVarxCoreQuat(this.agpCptrCoreQuat, this);
    }

    /* access modifiers changed from: package-private */
    public void setY(float f) {
        CoreJni.setVaryCoreQuat(this.agpCptrCoreQuat, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getY() {
        return CoreJni.getVaryCoreQuat(this.agpCptrCoreQuat, this);
    }

    /* access modifiers changed from: package-private */
    public void setZ(float f) {
        CoreJni.setVarzCoreQuat(this.agpCptrCoreQuat, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getZ() {
        return CoreJni.getVarzCoreQuat(this.agpCptrCoreQuat, this);
    }

    /* access modifiers changed from: package-private */
    public void setW(float f) {
        CoreJni.setVarwCoreQuat(this.agpCptrCoreQuat, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getW() {
        return CoreJni.getVarwCoreQuat(this.agpCptrCoreQuat, this);
    }

    /* access modifiers changed from: package-private */
    public void setData(float[] fArr) {
        CoreJni.setVardataCoreQuat(this.agpCptrCoreQuat, this, fArr);
    }

    /* access modifiers changed from: package-private */
    public float[] getData() {
        return CoreJni.getVardataCoreQuat(this.agpCptrCoreQuat, this);
    }
}
