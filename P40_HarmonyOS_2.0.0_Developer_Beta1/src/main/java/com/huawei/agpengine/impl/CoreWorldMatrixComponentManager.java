package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreWorldMatrixComponentManager extends CoreComponentManager {
    private transient long agpCptr;

    CoreWorldMatrixComponentManager(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreWorldMatrixComponentManager(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreWorldMatrixComponentManager obj) {
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
    public void set(long index, CoreWorldMatrixComponent data) {
        CoreJni.setInCoreWorldMatrixComponentManager0(this.agpCptr, this, index, CoreWorldMatrixComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public void set(int entity, CoreWorldMatrixComponent data) {
        CoreJni.setInCoreWorldMatrixComponentManager1(this.agpCptr, this, entity, CoreWorldMatrixComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public CoreWorldMatrixComponent get(long index) {
        return new CoreWorldMatrixComponent(CoreJni.getInCoreWorldMatrixComponentManager0(this.agpCptr, this, index), true);
    }

    /* access modifiers changed from: package-private */
    public CoreWorldMatrixComponent get(int entity) {
        return new CoreWorldMatrixComponent(CoreJni.getInCoreWorldMatrixComponentManager1(this.agpCptr, this, entity), true);
    }
}
