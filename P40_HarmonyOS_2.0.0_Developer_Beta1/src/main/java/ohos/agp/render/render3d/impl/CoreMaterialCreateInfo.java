package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreMaterialCreateInfo {
    private transient long agpCptrMaterialCreateInfo;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreMaterialCreateInfo(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrMaterialCreateInfo = j;
    }

    static long getCptr(CoreMaterialCreateInfo coreMaterialCreateInfo) {
        if (coreMaterialCreateInfo == null) {
            return 0;
        }
        return coreMaterialCreateInfo.agpCptrMaterialCreateInfo;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrMaterialCreateInfo != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreMaterialCreateInfo(this.agpCptrMaterialCreateInfo);
                }
                this.agpCptrMaterialCreateInfo = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreMaterialCreateInfo coreMaterialCreateInfo, boolean z) {
        if (coreMaterialCreateInfo != null) {
            synchronized (coreMaterialCreateInfo.delLock) {
                coreMaterialCreateInfo.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreMaterialCreateInfo);
    }

    /* access modifiers changed from: package-private */
    public void setDesc(CoreMaterialDesc coreMaterialDesc) {
        CoreJni.setVardescCoreMaterialCreateInfo(this.agpCptrMaterialCreateInfo, this, CoreMaterialDesc.getCptr(coreMaterialDesc), coreMaterialDesc);
    }

    /* access modifiers changed from: package-private */
    public CoreMaterialDesc getDesc() {
        long vardescCoreMaterialCreateInfo = CoreJni.getVardescCoreMaterialCreateInfo(this.agpCptrMaterialCreateInfo, this);
        if (vardescCoreMaterialCreateInfo == 0) {
            return null;
        }
        return new CoreMaterialDesc(vardescCoreMaterialCreateInfo, false);
    }

    CoreMaterialCreateInfo() {
        this(CoreJni.newCoreMaterialCreateInfo(), true);
    }
}
