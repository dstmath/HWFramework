package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreSceneComponent {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreSceneComponent(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreSceneComponent obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (this.isAgpCmemOwn) {
                this.isAgpCmemOwn = false;
                CoreJni.deleteCoreSceneComponent(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setEnvironmentDiffuseColor(CoreVec3 value) {
        CoreJni.setVarenvironmentDiffuseColorCoreSceneComponent(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getEnvironmentDiffuseColor() {
        long cptr = CoreJni.getVarenvironmentDiffuseColorCoreSceneComponent(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setEnvironmentSpecularColor(CoreVec3 value) {
        CoreJni.setVarenvironmentSpecularColorCoreSceneComponent(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getEnvironmentSpecularColor() {
        long cptr = CoreJni.getVarenvironmentSpecularColorCoreSceneComponent(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setEnvironmentDiffuseIntensity(float value) {
        CoreJni.setVarenvironmentDiffuseIntensityCoreSceneComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getEnvironmentDiffuseIntensity() {
        return CoreJni.getVarenvironmentDiffuseIntensityCoreSceneComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setEnvironmentSpecularIntensity(float value) {
        CoreJni.setVarenvironmentSpecularIntensityCoreSceneComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getEnvironmentSpecularIntensity() {
        return CoreJni.getVarenvironmentSpecularIntensityCoreSceneComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setCamera(int value) {
        CoreJni.setVarcameraCoreSceneComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public int getCamera() {
        return CoreJni.getVarcameraCoreSceneComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setRadianceCubemap(long value) {
        CoreJni.setVarradianceCubemapCoreSceneComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getRadianceCubemap() {
        return CoreJni.getVarradianceCubemapCoreSceneComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setRadianceCubemapMipCount(long value) {
        CoreJni.setVarradianceCubemapMipCountCoreSceneComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getRadianceCubemapMipCount() {
        return CoreJni.getVarradianceCubemapMipCountCoreSceneComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setEnvMap(long value) {
        CoreJni.setVarenvMapCoreSceneComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getEnvMap() {
        return CoreJni.getVarenvMapCoreSceneComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setEnvMapLodLevel(float value) {
        CoreJni.setVarenvMapLodLevelCoreSceneComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getEnvMapLodLevel() {
        return CoreJni.getVarenvMapLodLevelCoreSceneComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setBackgroundType(short value) {
        CoreJni.setVarbackgroundTypeCoreSceneComponent(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public short getBackgroundType() {
        return CoreJni.getVarbackgroundTypeCoreSceneComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setIrradianceCoefficients(CoreVec3 value) {
        CoreJni.setVarirradianceCoefficientsCoreSceneComponent(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getIrradianceCoefficients() {
        long cptr = CoreJni.getVarirradianceCoefficientsCoreSceneComponent(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setEnvironmentRotation(CoreQuat value) {
        CoreJni.setVarenvironmentRotationCoreSceneComponent(this.agpCptr, this, CoreQuat.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreQuat getEnvironmentRotation() {
        long cptr = CoreJni.getVarenvironmentRotationCoreSceneComponent(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreQuat(cptr, false);
    }

    CoreSceneComponent() {
        this(CoreJni.newCoreSceneComponent(), true);
    }
}
