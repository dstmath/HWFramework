package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreLightComponentManager extends CoreComponentManager {
    private transient long agpCptr;

    CoreLightComponentManager(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreLightComponentManager(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreLightComponentManager obj) {
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
    @Override // com.huawei.agpengine.impl.CoreComponentManager
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
    public void set(long index, CoreLightComponent data) {
        CoreJni.setInCoreLightComponentManager0(this.agpCptr, this, index, CoreLightComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public void set(int entity, CoreLightComponent data) {
        CoreJni.setInCoreLightComponentManager1(this.agpCptr, this, entity, CoreLightComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public CoreLightComponent get(long index) {
        return new CoreLightComponent(CoreJni.getInCoreLightComponentManager0(this.agpCptr, this, index), true);
    }

    /* access modifiers changed from: package-private */
    public CoreLightComponent get(int entity) {
        return new CoreLightComponent(CoreJni.getInCoreLightComponentManager1(this.agpCptr, this, entity), true);
    }
}
