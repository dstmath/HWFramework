package ohos.agp.render.render3d.impl;

class CoreCameraComponentManager extends CoreComponentManager {
    private transient long agpCptrCoreCameraComponentManager;

    CoreCameraComponentManager(long j, boolean z) {
        super(CoreJni.classUpcastCoreCameraComponentManager(j), z);
        this.agpCptrCoreCameraComponentManager = j;
    }

    static long getCptr(CoreCameraComponentManager coreCameraComponentManager) {
        if (coreCameraComponentManager == null) {
            return 0;
        }
        return coreCameraComponentManager.agpCptrCoreCameraComponentManager;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.agp.render.render3d.impl.CoreComponentManager
    public synchronized void delete() {
        if (this.agpCptrCoreCameraComponentManager != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptrCoreCameraComponentManager = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
        super.delete();
    }

    static long getCptrAndSetMemOwn(CoreCameraComponentManager coreCameraComponentManager, boolean z) {
        if (coreCameraComponentManager != null) {
            coreCameraComponentManager.isAgpCmemOwn = z;
        }
        return getCptr(coreCameraComponentManager);
    }

    /* access modifiers changed from: package-private */
    public void set(long j, CoreCameraComponent coreCameraComponent) {
        CoreJni.setInCoreCameraComponentManager0(this.agpCptrCoreCameraComponentManager, this, j, CoreCameraComponent.getCptr(coreCameraComponent), coreCameraComponent);
    }

    /* access modifiers changed from: package-private */
    public void set(CoreEntity coreEntity, CoreCameraComponent coreCameraComponent) {
        CoreJni.setInCoreCameraComponentManager1(this.agpCptrCoreCameraComponentManager, this, CoreEntity.getCptr(coreEntity), coreEntity, CoreCameraComponent.getCptr(coreCameraComponent), coreCameraComponent);
    }

    /* access modifiers changed from: package-private */
    public CoreCameraComponent get(long j) {
        return new CoreCameraComponent(CoreJni.getInCoreCameraComponentManager0(this.agpCptrCoreCameraComponentManager, this, j), true);
    }

    /* access modifiers changed from: package-private */
    public CoreCameraComponent get(CoreEntity coreEntity) {
        return new CoreCameraComponent(CoreJni.getInCoreCameraComponentManager1(this.agpCptrCoreCameraComponentManager, this, CoreEntity.getCptr(coreEntity), coreEntity), true);
    }
}
