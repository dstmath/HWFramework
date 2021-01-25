package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreRenderMeshComponentManager extends CoreComponentManager {
    private transient long agpCptrCoreRenderMeshComponentMgr;
    private final Object delLock = new Object();

    CoreRenderMeshComponentManager(long j, boolean z) {
        super(CoreJni.classUpcastCoreRenderMeshComponentManager(j), z);
        this.agpCptrCoreRenderMeshComponentMgr = j;
    }

    static long getCptr(CoreRenderMeshComponentManager coreRenderMeshComponentManager) {
        if (coreRenderMeshComponentManager == null) {
            return 0;
        }
        return coreRenderMeshComponentManager.agpCptrCoreRenderMeshComponentMgr;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.agp.render.render3d.impl.CoreComponentManager
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreRenderMeshComponentMgr != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrCoreRenderMeshComponentMgr = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
            super.delete();
        }
    }

    static long getCptrAndSetMemOwn(CoreRenderMeshComponentManager coreRenderMeshComponentManager, boolean z) {
        if (coreRenderMeshComponentManager != null) {
            coreRenderMeshComponentManager.isAgpCmemOwn = z;
        }
        return getCptr(coreRenderMeshComponentManager);
    }

    /* access modifiers changed from: package-private */
    public void set(long j, CoreRenderMeshComponent coreRenderMeshComponent) {
        CoreJni.setInCoreRenderMeshComponentManager0(this.agpCptrCoreRenderMeshComponentMgr, this, j, CoreRenderMeshComponent.getCptr(coreRenderMeshComponent), coreRenderMeshComponent);
    }

    /* access modifiers changed from: package-private */
    public void set(CoreEntity coreEntity, CoreRenderMeshComponent coreRenderMeshComponent) {
        CoreJni.setInCoreRenderMeshComponentManager1(this.agpCptrCoreRenderMeshComponentMgr, this, CoreEntity.getCptr(coreEntity), coreEntity, CoreRenderMeshComponent.getCptr(coreRenderMeshComponent), coreRenderMeshComponent);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderMeshComponent get(long j) {
        return new CoreRenderMeshComponent(CoreJni.getInCoreRenderMeshComponentManager0(this.agpCptrCoreRenderMeshComponentMgr, this, j), true);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderMeshComponent get(CoreEntity coreEntity) {
        return new CoreRenderMeshComponent(CoreJni.getInCoreRenderMeshComponentManager1(this.agpCptrCoreRenderMeshComponentMgr, this, CoreEntity.getCptr(coreEntity), coreEntity), true);
    }
}
