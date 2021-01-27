package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreMaterialCreateInfo {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreMaterialCreateInfo(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMaterialCreateInfo obj) {
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
                CoreJni.deleteCoreMaterialCreateInfo(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setDesc(CoreMaterialDesc value) {
        CoreJni.setVardescCoreMaterialCreateInfo(this.agpCptr, this, CoreMaterialDesc.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreMaterialDesc getDesc() {
        long cptr = CoreJni.getVardescCoreMaterialCreateInfo(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreMaterialDesc(cptr, false);
    }

    CoreMaterialCreateInfo() {
        this(CoreJni.newCoreMaterialCreateInfo(), true);
    }
}
