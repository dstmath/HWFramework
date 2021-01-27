package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreLocalMatrixComponentManager extends CoreComponentManager {
    private transient long agpCptrCoreLocalMatrixComponentMgr;

    CoreLocalMatrixComponentManager(long j, boolean z) {
        super(CoreJni.classUpcastCoreLocalMatrixComponentManager(j), z);
        this.agpCptrCoreLocalMatrixComponentMgr = j;
    }

    static long getCptr(CoreLocalMatrixComponentManager coreLocalMatrixComponentManager) {
        if (coreLocalMatrixComponentManager == null) {
            return 0;
        }
        return coreLocalMatrixComponentManager.agpCptrCoreLocalMatrixComponentMgr;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.agp.render.render3d.impl.CoreComponentManager
    public synchronized void delete() {
        if (this.agpCptrCoreLocalMatrixComponentMgr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptrCoreLocalMatrixComponentMgr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
        super.delete();
    }

    static long getCptrAndSetMemOwn(CoreLocalMatrixComponentManager coreLocalMatrixComponentManager, boolean z) {
        if (coreLocalMatrixComponentManager != null) {
            coreLocalMatrixComponentManager.isAgpCmemOwn = z;
        }
        return getCptr(coreLocalMatrixComponentManager);
    }

    /* access modifiers changed from: package-private */
    public void set(long j, CoreLocalMatrixComponent coreLocalMatrixComponent) {
        CoreJni.setInCoreLocalMatrixComponentManager0(this.agpCptrCoreLocalMatrixComponentMgr, this, j, CoreLocalMatrixComponent.getCptr(coreLocalMatrixComponent), coreLocalMatrixComponent);
    }

    /* access modifiers changed from: package-private */
    public void set(CoreEntity coreEntity, CoreLocalMatrixComponent coreLocalMatrixComponent) {
        CoreJni.setInCoreLocalMatrixComponentManager1(this.agpCptrCoreLocalMatrixComponentMgr, this, CoreEntity.getCptr(coreEntity), coreEntity, CoreLocalMatrixComponent.getCptr(coreLocalMatrixComponent), coreLocalMatrixComponent);
    }

    /* access modifiers changed from: package-private */
    public CoreLocalMatrixComponent get(long j) {
        return new CoreLocalMatrixComponent(CoreJni.getInCoreLocalMatrixComponentManager0(this.agpCptrCoreLocalMatrixComponentMgr, this, j), true);
    }

    /* access modifiers changed from: package-private */
    public CoreLocalMatrixComponent get(CoreEntity coreEntity) {
        return new CoreLocalMatrixComponent(CoreJni.getInCoreLocalMatrixComponentManager1(this.agpCptrCoreLocalMatrixComponentMgr, this, CoreEntity.getCptr(coreEntity), coreEntity), true);
    }
}
