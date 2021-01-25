package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreSceneComponent {
    private transient long agpCptrCoreSceneComponent;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreSceneComponent(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreSceneComponent = j;
    }

    static long getCptr(CoreSceneComponent coreSceneComponent) {
        if (coreSceneComponent == null) {
            return 0;
        }
        return coreSceneComponent.agpCptrCoreSceneComponent;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreSceneComponent != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreSceneComponent(this.agpCptrCoreSceneComponent);
                }
                this.agpCptrCoreSceneComponent = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreSceneComponent coreSceneComponent, boolean z) {
        if (coreSceneComponent != null) {
            synchronized (coreSceneComponent.delLock) {
                coreSceneComponent.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreSceneComponent);
    }

    /* access modifiers changed from: package-private */
    public void setEnvironmentDiffuseColor(CoreVec3 coreVec3) {
        CoreJni.setVarenvironmentDiffuseColorCoreSceneComponent(this.agpCptrCoreSceneComponent, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getEnvironmentDiffuseColor() {
        long varenvironmentDiffuseColorCoreSceneComponent = CoreJni.getVarenvironmentDiffuseColorCoreSceneComponent(this.agpCptrCoreSceneComponent, this);
        if (varenvironmentDiffuseColorCoreSceneComponent == 0) {
            return null;
        }
        return new CoreVec3(varenvironmentDiffuseColorCoreSceneComponent, false);
    }

    /* access modifiers changed from: package-private */
    public void setEnvironmentSpecularColor(CoreVec3 coreVec3) {
        CoreJni.setVarenvironmentSpecularColorCoreSceneComponent(this.agpCptrCoreSceneComponent, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getEnvironmentSpecularColor() {
        long varenvironmentSpecularColorCoreSceneComponent = CoreJni.getVarenvironmentSpecularColorCoreSceneComponent(this.agpCptrCoreSceneComponent, this);
        if (varenvironmentSpecularColorCoreSceneComponent == 0) {
            return null;
        }
        return new CoreVec3(varenvironmentSpecularColorCoreSceneComponent, false);
    }

    /* access modifiers changed from: package-private */
    public void setEnvironmentDiffuseIntensity(float f) {
        CoreJni.setVarenvironmentDiffuseIntensityCoreSceneComponent(this.agpCptrCoreSceneComponent, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getEnvironmentDiffuseIntensity() {
        return CoreJni.getVarenvironmentDiffuseIntensityCoreSceneComponent(this.agpCptrCoreSceneComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setEnvironmentSpecularIntensity(float f) {
        CoreJni.setVarenvironmentSpecularIntensityCoreSceneComponent(this.agpCptrCoreSceneComponent, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getEnvironmentSpecularIntensity() {
        return CoreJni.getVarenvironmentSpecularIntensityCoreSceneComponent(this.agpCptrCoreSceneComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setCamera(CoreEntity coreEntity) {
        CoreJni.setVarcameraCoreSceneComponent(this.agpCptrCoreSceneComponent, this, CoreEntity.getCptr(coreEntity), coreEntity);
    }

    /* access modifiers changed from: package-private */
    public CoreEntity getCamera() {
        long varcameraCoreSceneComponent = CoreJni.getVarcameraCoreSceneComponent(this.agpCptrCoreSceneComponent, this);
        if (varcameraCoreSceneComponent == 0) {
            return null;
        }
        return new CoreEntity(varcameraCoreSceneComponent, false);
    }

    /* access modifiers changed from: package-private */
    public void setRadianceCubemap(CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.setVarradianceCubemapCoreSceneComponent(this.agpCptrCoreSceneComponent, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle getRadianceCubemap() {
        long varradianceCubemapCoreSceneComponent = CoreJni.getVarradianceCubemapCoreSceneComponent(this.agpCptrCoreSceneComponent, this);
        if (varradianceCubemapCoreSceneComponent == 0) {
            return null;
        }
        return new CoreGpuResourceHandle(varradianceCubemapCoreSceneComponent, false);
    }

    /* access modifiers changed from: package-private */
    public void setRadianceCubemapMipCount(long j) {
        CoreJni.setVarradianceCubemapMipCountCoreSceneComponent(this.agpCptrCoreSceneComponent, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getRadianceCubemapMipCount() {
        return CoreJni.getVarradianceCubemapMipCountCoreSceneComponent(this.agpCptrCoreSceneComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setEnvMap(CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.setVarenvMapCoreSceneComponent(this.agpCptrCoreSceneComponent, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle getEnvMap() {
        long varenvMapCoreSceneComponent = CoreJni.getVarenvMapCoreSceneComponent(this.agpCptrCoreSceneComponent, this);
        if (varenvMapCoreSceneComponent == 0) {
            return null;
        }
        return new CoreGpuResourceHandle(varenvMapCoreSceneComponent, false);
    }

    /* access modifiers changed from: package-private */
    public void setEnvMapLodLevel(float f) {
        CoreJni.setVarenvMapLodLevelCoreSceneComponent(this.agpCptrCoreSceneComponent, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getEnvMapLodLevel() {
        return CoreJni.getVarenvMapLodLevelCoreSceneComponent(this.agpCptrCoreSceneComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setBackgroundType(short s) {
        CoreJni.setVarbackgroundTypeCoreSceneComponent(this.agpCptrCoreSceneComponent, this, s);
    }

    /* access modifiers changed from: package-private */
    public short getBackgroundType() {
        return CoreJni.getVarbackgroundTypeCoreSceneComponent(this.agpCptrCoreSceneComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setIrradianceCoefficients(CoreVec3 coreVec3) {
        CoreJni.setVarirradianceCoefficientsCoreSceneComponent(this.agpCptrCoreSceneComponent, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getIrradianceCoefficients() {
        long varirradianceCoefficientsCoreSceneComponent = CoreJni.getVarirradianceCoefficientsCoreSceneComponent(this.agpCptrCoreSceneComponent, this);
        if (varirradianceCoefficientsCoreSceneComponent == 0) {
            return null;
        }
        return new CoreVec3(varirradianceCoefficientsCoreSceneComponent, false);
    }

    /* access modifiers changed from: package-private */
    public void setEnvironmentRotation(CoreQuat coreQuat) {
        CoreJni.setVarenvironmentRotationCoreSceneComponent(this.agpCptrCoreSceneComponent, this, CoreQuat.getCptr(coreQuat), coreQuat);
    }

    /* access modifiers changed from: package-private */
    public CoreQuat getEnvironmentRotation() {
        long varenvironmentRotationCoreSceneComponent = CoreJni.getVarenvironmentRotationCoreSceneComponent(this.agpCptrCoreSceneComponent, this);
        if (varenvironmentRotationCoreSceneComponent == 0) {
            return null;
        }
        return new CoreQuat(varenvironmentRotationCoreSceneComponent, false);
    }

    CoreSceneComponent() {
        this(CoreJni.newCoreSceneComponent(), true);
    }
}
