package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreRenderMeshComponentManager extends CoreComponentManager {
    private transient long agpCptr;

    CoreRenderMeshComponentManager(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreRenderMeshComponentManager(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreRenderMeshComponentManager obj) {
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
    public void set(long index, CoreRenderMeshComponent data) {
        CoreJni.setInCoreRenderMeshComponentManager0(this.agpCptr, this, index, CoreRenderMeshComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public void set(int entity, CoreRenderMeshComponent data) {
        CoreJni.setInCoreRenderMeshComponentManager1(this.agpCptr, this, entity, CoreRenderMeshComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderMeshComponent get(long index) {
        return new CoreRenderMeshComponent(CoreJni.getInCoreRenderMeshComponentManager0(this.agpCptr, this, index), true);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderMeshComponent get(int entity) {
        return new CoreRenderMeshComponent(CoreJni.getInCoreRenderMeshComponentManager1(this.agpCptr, this, entity), true);
    }
}
