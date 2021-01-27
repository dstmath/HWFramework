package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreLocalMatrixComponentManager extends CoreComponentManager {
    private transient long agpCptr;

    CoreLocalMatrixComponentManager(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreLocalMatrixComponentManager(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreLocalMatrixComponentManager obj) {
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
    public void set(long index, CoreLocalMatrixComponent data) {
        CoreJni.setInCoreLocalMatrixComponentManager0(this.agpCptr, this, index, CoreLocalMatrixComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public void set(int entity, CoreLocalMatrixComponent data) {
        CoreJni.setInCoreLocalMatrixComponentManager1(this.agpCptr, this, entity, CoreLocalMatrixComponent.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public CoreLocalMatrixComponent get(long index) {
        return new CoreLocalMatrixComponent(CoreJni.getInCoreLocalMatrixComponentManager0(this.agpCptr, this, index), true);
    }

    /* access modifiers changed from: package-private */
    public CoreLocalMatrixComponent get(int entity) {
        return new CoreLocalMatrixComponent(CoreJni.getInCoreLocalMatrixComponentManager1(this.agpCptr, this, entity), true);
    }
}
