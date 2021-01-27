package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreTransformComponentManager extends CoreComponentManager {
    private transient long agpCptrTransformComponentMgr;
    private final Object delLock = new Object();

    CoreTransformComponentManager(long j, boolean z) {
        super(CoreJni.classUpcastCoreTransformComponentManager(j), z);
        this.agpCptrTransformComponentMgr = j;
    }

    static long getCptr(CoreTransformComponentManager coreTransformComponentManager) {
        if (coreTransformComponentManager == null) {
            return 0;
        }
        return coreTransformComponentManager.agpCptrTransformComponentMgr;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.agp.render.render3d.impl.CoreComponentManager
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrTransformComponentMgr != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrTransformComponentMgr = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
            super.delete();
        }
    }

    static long getCptrAndSetMemOwn(CoreTransformComponentManager coreTransformComponentManager, boolean z) {
        if (coreTransformComponentManager != null) {
            coreTransformComponentManager.isAgpCmemOwn = z;
        }
        return getCptr(coreTransformComponentManager);
    }

    /* access modifiers changed from: package-private */
    public void set(long j, CoreTransformComponent coreTransformComponent) {
        CoreJni.setInCoreTransformComponentManager0(this.agpCptrTransformComponentMgr, this, j, CoreTransformComponent.getCptr(coreTransformComponent), coreTransformComponent);
    }

    /* access modifiers changed from: package-private */
    public void set(CoreEntity coreEntity, CoreTransformComponent coreTransformComponent) {
        CoreJni.setInCoreTransformComponentManager1(this.agpCptrTransformComponentMgr, this, CoreEntity.getCptr(coreEntity), coreEntity, CoreTransformComponent.getCptr(coreTransformComponent), coreTransformComponent);
    }

    /* access modifiers changed from: package-private */
    public CoreTransformComponent get(long j) {
        return new CoreTransformComponent(CoreJni.getInCoreTransformComponentManager0(this.agpCptrTransformComponentMgr, this, j), true);
    }

    /* access modifiers changed from: package-private */
    public CoreTransformComponent get(CoreEntity coreEntity) {
        return new CoreTransformComponent(CoreJni.getInCoreTransformComponentManager1(this.agpCptrTransformComponentMgr, this, CoreEntity.getCptr(coreEntity), coreEntity), true);
    }
}
