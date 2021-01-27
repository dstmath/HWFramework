package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreNodeComponentManager extends CoreComponentManager {
    private transient long agpCptrNodeComponentMgr;
    private final Object delLock = new Object();

    CoreNodeComponentManager(long j, boolean z) {
        super(CoreJni.classUpcastCoreNodeComponentManager(j), z);
        this.agpCptrNodeComponentMgr = j;
    }

    static long getCptr(CoreNodeComponentManager coreNodeComponentManager) {
        if (coreNodeComponentManager == null) {
            return 0;
        }
        return coreNodeComponentManager.agpCptrNodeComponentMgr;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.agp.render.render3d.impl.CoreComponentManager
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrNodeComponentMgr != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrNodeComponentMgr = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
            super.delete();
        }
    }

    static long getCptrAndSetMemOwn(CoreNodeComponentManager coreNodeComponentManager, boolean z) {
        if (coreNodeComponentManager != null) {
            coreNodeComponentManager.isAgpCmemOwn = z;
        }
        return getCptr(coreNodeComponentManager);
    }

    /* access modifiers changed from: package-private */
    public void set(long j, CoreNodeComponent coreNodeComponent) {
        CoreJni.setInCoreNodeComponentManager0(this.agpCptrNodeComponentMgr, this, j, CoreNodeComponent.getCptr(coreNodeComponent), coreNodeComponent);
    }

    /* access modifiers changed from: package-private */
    public void set(CoreEntity coreEntity, CoreNodeComponent coreNodeComponent) {
        CoreJni.setInCoreNodeComponentManager1(this.agpCptrNodeComponentMgr, this, CoreEntity.getCptr(coreEntity), coreEntity, CoreNodeComponent.getCptr(coreNodeComponent), coreNodeComponent);
    }

    /* access modifiers changed from: package-private */
    public CoreNodeComponent get(long j) {
        return new CoreNodeComponent(CoreJni.getInCoreNodeComponentManager0(this.agpCptrNodeComponentMgr, this, j), true);
    }

    /* access modifiers changed from: package-private */
    public CoreNodeComponent get(CoreEntity coreEntity) {
        return new CoreNodeComponent(CoreJni.getInCoreNodeComponentManager1(this.agpCptrNodeComponentMgr, this, CoreEntity.getCptr(coreEntity), coreEntity), true);
    }
}
