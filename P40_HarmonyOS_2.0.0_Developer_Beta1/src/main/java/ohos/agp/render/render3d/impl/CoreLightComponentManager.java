package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreLightComponentManager extends CoreComponentManager {
    private transient long agpCptrCoreLightComponentManager;

    CoreLightComponentManager(long j, boolean z) {
        super(CoreJni.classUpcastCoreLightComponentManager(j), z);
        this.agpCptrCoreLightComponentManager = j;
    }

    static long getCptr(CoreLightComponentManager coreLightComponentManager) {
        if (coreLightComponentManager == null) {
            return 0;
        }
        return coreLightComponentManager.agpCptrCoreLightComponentManager;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.agp.render.render3d.impl.CoreComponentManager
    public synchronized void delete() {
        if (this.agpCptrCoreLightComponentManager != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptrCoreLightComponentManager = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
        super.delete();
    }

    static long getCptrAndSetMemOwn(CoreLightComponentManager coreLightComponentManager, boolean z) {
        if (coreLightComponentManager != null) {
            coreLightComponentManager.isAgpCmemOwn = z;
        }
        return getCptr(coreLightComponentManager);
    }

    /* access modifiers changed from: package-private */
    public void set(long j, CoreLightComponent coreLightComponent) {
        CoreJni.setInCoreLightComponentManager0(this.agpCptrCoreLightComponentManager, this, j, CoreLightComponent.getCptr(coreLightComponent), coreLightComponent);
    }

    /* access modifiers changed from: package-private */
    public void set(CoreEntity coreEntity, CoreLightComponent coreLightComponent) {
        CoreJni.setInCoreLightComponentManager1(this.agpCptrCoreLightComponentManager, this, CoreEntity.getCptr(coreEntity), coreEntity, CoreLightComponent.getCptr(coreLightComponent), coreLightComponent);
    }

    /* access modifiers changed from: package-private */
    public CoreLightComponent get(long j) {
        return new CoreLightComponent(CoreJni.getInCoreLightComponentManager0(this.agpCptrCoreLightComponentManager, this, j), true);
    }

    /* access modifiers changed from: package-private */
    public CoreLightComponent get(CoreEntity coreEntity) {
        return new CoreLightComponent(CoreJni.getInCoreLightComponentManager1(this.agpCptrCoreLightComponentManager, this, CoreEntity.getCptr(coreEntity), coreEntity), true);
    }
}
