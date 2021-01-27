package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreMorphComponentManager extends CoreComponentManager {
    private transient long agpCptrMorphComponentMgr;
    private final Object delLock = new Object();

    CoreMorphComponentManager(long j, boolean z) {
        super(CoreJni.classUpcastCoreMorphComponentManager(j), z);
        this.agpCptrMorphComponentMgr = j;
    }

    static long getCptr(CoreMorphComponentManager coreMorphComponentManager) {
        if (coreMorphComponentManager == null) {
            return 0;
        }
        return coreMorphComponentManager.agpCptrMorphComponentMgr;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.agp.render.render3d.impl.CoreComponentManager
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrMorphComponentMgr != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrMorphComponentMgr = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
            super.delete();
        }
    }

    static long getCptrAndSetMemOwn(CoreMorphComponentManager coreMorphComponentManager, boolean z) {
        if (coreMorphComponentManager != null) {
            coreMorphComponentManager.isAgpCmemOwn = z;
        }
        return getCptr(coreMorphComponentManager);
    }

    /* access modifiers changed from: package-private */
    public void set(long j, CoreMorphComponent coreMorphComponent) {
        CoreJni.setInCoreMorphComponentManager0(this.agpCptrMorphComponentMgr, this, j, CoreMorphComponent.getCptr(coreMorphComponent), coreMorphComponent);
    }

    /* access modifiers changed from: package-private */
    public void set(CoreEntity coreEntity, CoreMorphComponent coreMorphComponent) {
        CoreJni.setInCoreMorphComponentManager1(this.agpCptrMorphComponentMgr, this, CoreEntity.getCptr(coreEntity), coreEntity, CoreMorphComponent.getCptr(coreMorphComponent), coreMorphComponent);
    }

    /* access modifiers changed from: package-private */
    public CoreMorphComponent get(long j) {
        return new CoreMorphComponent(CoreJni.getInCoreMorphComponentManager0(this.agpCptrMorphComponentMgr, this, j), true);
    }

    /* access modifiers changed from: package-private */
    public CoreMorphComponent get(CoreEntity coreEntity) {
        return new CoreMorphComponent(CoreJni.getInCoreMorphComponentManager1(this.agpCptrMorphComponentMgr, this, CoreEntity.getCptr(coreEntity), coreEntity), true);
    }
}
