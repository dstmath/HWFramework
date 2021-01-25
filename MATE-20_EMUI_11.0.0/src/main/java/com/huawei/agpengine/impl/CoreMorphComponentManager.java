package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreMorphComponentManager extends CoreComponentManager {
    private transient long agpCptr;

    CoreMorphComponentManager(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreMorphComponentManager(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMorphComponentManager obj) {
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
    public void set(long index, CoreMorphComponent data) {
        CoreJni.setInCoreMorphComponentManager0(this.agpCptr, this, index, CoreMorphComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public void set(int entity, CoreMorphComponent data) {
        CoreJni.setInCoreMorphComponentManager1(this.agpCptr, this, entity, CoreMorphComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public CoreMorphComponent get(long index) {
        return new CoreMorphComponent(CoreJni.getInCoreMorphComponentManager0(this.agpCptr, this, index), true);
    }

    /* access modifiers changed from: package-private */
    public CoreMorphComponent get(int entity) {
        return new CoreMorphComponent(CoreJni.getInCoreMorphComponentManager1(this.agpCptr, this, entity), true);
    }
}
