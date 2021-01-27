package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreTransformComponentManager extends CoreComponentManager {
    private transient long agpCptr;

    CoreTransformComponentManager(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreTransformComponentManager(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreTransformComponentManager obj) {
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
    public void set(long index, CoreTransformComponent data) {
        CoreJni.setInCoreTransformComponentManager0(this.agpCptr, this, index, CoreTransformComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public void set(int entity, CoreTransformComponent data) {
        CoreJni.setInCoreTransformComponentManager1(this.agpCptr, this, entity, CoreTransformComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public CoreTransformComponent get(long index) {
        return new CoreTransformComponent(CoreJni.getInCoreTransformComponentManager0(this.agpCptr, this, index), true);
    }

    /* access modifiers changed from: package-private */
    public CoreTransformComponent get(int entity) {
        return new CoreTransformComponent(CoreJni.getInCoreTransformComponentManager1(this.agpCptr, this, entity), true);
    }
}
