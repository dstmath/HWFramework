package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreMaterial extends CoreResource {
    private transient long agpCptrCoreMaterial;
    private final Object delLock = new Object();

    CoreMaterial(long j, boolean z) {
        super(CoreJni.classUpcastCoreMaterial(j), z);
        this.agpCptrCoreMaterial = j;
    }

    static long getCptr(CoreMaterial coreMaterial) {
        if (coreMaterial == null) {
            return 0;
        }
        return coreMaterial.agpCptrCoreMaterial;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.agp.render.render3d.impl.CoreResource
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreMaterial != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrCoreMaterial = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
            super.delete();
        }
    }

    static long getCptrAndSetMemOwn(CoreMaterial coreMaterial, boolean z) {
        if (coreMaterial != null) {
            coreMaterial.isAgpCmemOwn = z;
        }
        return getCptr(coreMaterial);
    }

    /* access modifiers changed from: package-private */
    public void setDesc(CoreMaterialDesc coreMaterialDesc) {
        CoreJni.setDescInCoreMaterial(this.agpCptrCoreMaterial, this, CoreMaterialDesc.getCptr(coreMaterialDesc), coreMaterialDesc);
    }

    /* access modifiers changed from: package-private */
    public CoreMaterialDesc getDesc() {
        return new CoreMaterialDesc(CoreJni.getDescInCoreMaterial(this.agpCptrCoreMaterial, this), false);
    }
}
