package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreNodeComponentManager extends CoreComponentManager {
    private transient long agpCptr;

    CoreNodeComponentManager(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreNodeComponentManager(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreNodeComponentManager obj) {
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
    public void set(long index, CoreNodeComponent data) {
        CoreJni.setInCoreNodeComponentManager0(this.agpCptr, this, index, CoreNodeComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public void set(int entity, CoreNodeComponent data) {
        CoreJni.setInCoreNodeComponentManager1(this.agpCptr, this, entity, CoreNodeComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public CoreNodeComponent get(long index) {
        return new CoreNodeComponent(CoreJni.getInCoreNodeComponentManager0(this.agpCptr, this, index), true);
    }

    /* access modifiers changed from: package-private */
    public CoreNodeComponent get(int entity) {
        return new CoreNodeComponent(CoreJni.getInCoreNodeComponentManager1(this.agpCptr, this, entity), true);
    }
}
