package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreSceneComponentManager extends CoreComponentManager {
    private transient long agpCptr;

    CoreSceneComponentManager(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreSceneComponentManager(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreSceneComponentManager obj) {
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
    public void set(long index, CoreSceneComponent data) {
        CoreJni.setInCoreSceneComponentManager0(this.agpCptr, this, index, CoreSceneComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public void set(int entity, CoreSceneComponent data) {
        CoreJni.setInCoreSceneComponentManager1(this.agpCptr, this, entity, CoreSceneComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneComponent get(long index) {
        return new CoreSceneComponent(CoreJni.getInCoreSceneComponentManager0(this.agpCptr, this, index), true);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneComponent get(int entity) {
        return new CoreSceneComponent(CoreJni.getInCoreSceneComponentManager1(this.agpCptr, this, entity), true);
    }
}
