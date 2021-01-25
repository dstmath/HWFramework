package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreMaterial extends CoreResource {
    private transient long agpCptr;

    CoreMaterial(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreMaterial(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMaterial obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.agpengine.impl.CoreResource
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
        super.delete();
    }

    /* access modifiers changed from: package-private */
    public void setDesc(CoreMaterialDesc desc) {
        CoreJni.setDescInCoreMaterial(this.agpCptr, this, CoreMaterialDesc.getCptr(desc), desc);
    }

    /* access modifiers changed from: package-private */
    public CoreMaterialDesc getDesc() {
        return new CoreMaterialDesc(CoreJni.getDescInCoreMaterial(this.agpCptr, this), false);
    }
}
