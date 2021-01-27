package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreWorldMatrixComponentManager extends CoreComponentManager {
    private transient long agpCptrWorldMatrixComponentMgr;
    private final Object delLock = new Object();

    CoreWorldMatrixComponentManager(long j, boolean z) {
        super(CoreJni.classUpcastCoreWorldMatrixComponentManager(j), z);
        this.agpCptrWorldMatrixComponentMgr = j;
    }

    static long getCptr(CoreWorldMatrixComponentManager coreWorldMatrixComponentManager) {
        if (coreWorldMatrixComponentManager == null) {
            return 0;
        }
        return coreWorldMatrixComponentManager.agpCptrWorldMatrixComponentMgr;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.agp.render.render3d.impl.CoreComponentManager
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrWorldMatrixComponentMgr != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrWorldMatrixComponentMgr = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
            super.delete();
        }
    }

    static long getCptrAndSetMemOwn(CoreWorldMatrixComponentManager coreWorldMatrixComponentManager, boolean z) {
        if (coreWorldMatrixComponentManager != null) {
            coreWorldMatrixComponentManager.isAgpCmemOwn = z;
        }
        return getCptr(coreWorldMatrixComponentManager);
    }

    /* access modifiers changed from: package-private */
    public void set(long j, CoreWorldMatrixComponent coreWorldMatrixComponent) {
        CoreJni.setInCoreWorldMatrixComponentManager0(this.agpCptrWorldMatrixComponentMgr, this, j, CoreWorldMatrixComponent.getCptr(coreWorldMatrixComponent), coreWorldMatrixComponent);
    }

    /* access modifiers changed from: package-private */
    public void set(CoreEntity coreEntity, CoreWorldMatrixComponent coreWorldMatrixComponent) {
        CoreJni.setInCoreWorldMatrixComponentManager1(this.agpCptrWorldMatrixComponentMgr, this, CoreEntity.getCptr(coreEntity), coreEntity, CoreWorldMatrixComponent.getCptr(coreWorldMatrixComponent), coreWorldMatrixComponent);
    }

    /* access modifiers changed from: package-private */
    public CoreWorldMatrixComponent get(long j) {
        return new CoreWorldMatrixComponent(CoreJni.getInCoreWorldMatrixComponentManager0(this.agpCptrWorldMatrixComponentMgr, this, j), true);
    }

    /* access modifiers changed from: package-private */
    public CoreWorldMatrixComponent get(CoreEntity coreEntity) {
        return new CoreWorldMatrixComponent(CoreJni.getInCoreWorldMatrixComponentManager1(this.agpCptrWorldMatrixComponentMgr, this, CoreEntity.getCptr(coreEntity), coreEntity), true);
    }
}
