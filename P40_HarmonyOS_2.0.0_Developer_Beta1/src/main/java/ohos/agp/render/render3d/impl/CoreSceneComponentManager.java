package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreSceneComponentManager extends CoreComponentManager {
    private transient long agpCptrCoreSceneComponentManager;
    private final Object delLock = new Object();

    CoreSceneComponentManager(long j, boolean z) {
        super(CoreJni.classUpcastCoreSceneComponentManager(j), z);
        this.agpCptrCoreSceneComponentManager = j;
    }

    static long getCptr(CoreSceneComponentManager coreSceneComponentManager) {
        if (coreSceneComponentManager == null) {
            return 0;
        }
        return coreSceneComponentManager.agpCptrCoreSceneComponentManager;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.agp.render.render3d.impl.CoreComponentManager
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreSceneComponentManager != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrCoreSceneComponentManager = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
            super.delete();
        }
    }

    static long getCptrAndSetMemOwn(CoreSceneComponentManager coreSceneComponentManager, boolean z) {
        if (coreSceneComponentManager != null) {
            coreSceneComponentManager.isAgpCmemOwn = z;
        }
        return getCptr(coreSceneComponentManager);
    }

    /* access modifiers changed from: package-private */
    public void set(long j, CoreSceneComponent coreSceneComponent) {
        CoreJni.setInCoreSceneComponentManager0(this.agpCptrCoreSceneComponentManager, this, j, CoreSceneComponent.getCptr(coreSceneComponent), coreSceneComponent);
    }

    /* access modifiers changed from: package-private */
    public void set(CoreEntity coreEntity, CoreSceneComponent coreSceneComponent) {
        CoreJni.setInCoreSceneComponentManager1(this.agpCptrCoreSceneComponentManager, this, CoreEntity.getCptr(coreEntity), coreEntity, CoreSceneComponent.getCptr(coreSceneComponent), coreSceneComponent);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneComponent get(long j) {
        return new CoreSceneComponent(CoreJni.getInCoreSceneComponentManager0(this.agpCptrCoreSceneComponentManager, this, j), true);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneComponent get(CoreEntity coreEntity) {
        return new CoreSceneComponent(CoreJni.getInCoreSceneComponentManager1(this.agpCptrCoreSceneComponentManager, this, CoreEntity.getCptr(coreEntity), coreEntity), true);
    }
}
