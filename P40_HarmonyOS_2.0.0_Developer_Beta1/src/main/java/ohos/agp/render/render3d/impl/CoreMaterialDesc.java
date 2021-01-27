package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreMaterialDesc {
    private transient long agpCptr;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreMaterialDesc(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreMaterialDesc coreMaterialDesc) {
        if (coreMaterialDesc == null) {
            return 0;
        }
        return coreMaterialDesc.agpCptr;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptr != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreMaterialDesc(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreMaterialDesc coreMaterialDesc, boolean z) {
        if (coreMaterialDesc != null) {
            synchronized (coreMaterialDesc.delLock) {
                coreMaterialDesc.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreMaterialDesc);
    }

    /* access modifiers changed from: package-private */
    public void setType(CoreMaterialType coreMaterialType) {
        CoreJni.setVartypeCoreMaterialDesc(this.agpCptr, this, coreMaterialType.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreMaterialType getType() {
        return CoreMaterialType.swigToEnum(CoreJni.getVartypeCoreMaterialDesc(this.agpCptr, this));
    }

    /* access modifiers changed from: package-private */
    public void setBaseColor(CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.setVarbaseColorCoreMaterialDesc(this.agpCptr, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle getBaseColor() {
        long varbaseColorCoreMaterialDesc = CoreJni.getVarbaseColorCoreMaterialDesc(this.agpCptr, this);
        if (varbaseColorCoreMaterialDesc == 0) {
            return null;
        }
        return new CoreGpuResourceHandle(varbaseColorCoreMaterialDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setNormal(CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.setVarnormalCoreMaterialDesc(this.agpCptr, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle getNormal() {
        long varnormalCoreMaterialDesc = CoreJni.getVarnormalCoreMaterialDesc(this.agpCptr, this);
        if (varnormalCoreMaterialDesc == 0) {
            return null;
        }
        return new CoreGpuResourceHandle(varnormalCoreMaterialDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setEmissive(CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.setVaremissiveCoreMaterialDesc(this.agpCptr, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle getEmissive() {
        long varemissiveCoreMaterialDesc = CoreJni.getVaremissiveCoreMaterialDesc(this.agpCptr, this);
        if (varemissiveCoreMaterialDesc == 0) {
            return null;
        }
        return new CoreGpuResourceHandle(varemissiveCoreMaterialDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setMaterial(CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.setVarmaterialCoreMaterialDesc(this.agpCptr, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle getMaterial() {
        long varmaterialCoreMaterialDesc = CoreJni.getVarmaterialCoreMaterialDesc(this.agpCptr, this);
        if (varmaterialCoreMaterialDesc == 0) {
            return null;
        }
        return new CoreGpuResourceHandle(varmaterialCoreMaterialDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setAo(CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.setVaraoCoreMaterialDesc(this.agpCptr, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle getAo() {
        long varaoCoreMaterialDesc = CoreJni.getVaraoCoreMaterialDesc(this.agpCptr, this);
        if (varaoCoreMaterialDesc == 0) {
            return null;
        }
        return new CoreGpuResourceHandle(varaoCoreMaterialDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setSampler(CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.setVarsamplerCoreMaterialDesc(this.agpCptr, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle getSampler() {
        long varsamplerCoreMaterialDesc = CoreJni.getVarsamplerCoreMaterialDesc(this.agpCptr, this);
        if (varsamplerCoreMaterialDesc == 0) {
            return null;
        }
        return new CoreGpuResourceHandle(varsamplerCoreMaterialDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setBaseColorFactor(CoreVec4 coreVec4) {
        CoreJni.setVarbaseColorFactorCoreMaterialDesc(this.agpCptr, this, CoreVec4.getCptr(coreVec4), coreVec4);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4 getBaseColorFactor() {
        long varbaseColorFactorCoreMaterialDesc = CoreJni.getVarbaseColorFactorCoreMaterialDesc(this.agpCptr, this);
        if (varbaseColorFactorCoreMaterialDesc == 0) {
            return null;
        }
        return new CoreVec4(varbaseColorFactorCoreMaterialDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setEmissiveFactor(CoreVec3 coreVec3) {
        CoreJni.setVaremissiveFactorCoreMaterialDesc(this.agpCptr, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getEmissiveFactor() {
        long varemissiveFactorCoreMaterialDesc = CoreJni.getVaremissiveFactorCoreMaterialDesc(this.agpCptr, this);
        if (varemissiveFactorCoreMaterialDesc == 0) {
            return null;
        }
        return new CoreVec3(varemissiveFactorCoreMaterialDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setAmbientOcclusionFactor(float f) {
        CoreJni.setVarambientOcclusionFactorCoreMaterialDesc(this.agpCptr, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getAmbientOcclusionFactor() {
        return CoreJni.getVarambientOcclusionFactorCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setRoughnessFactor(float f) {
        CoreJni.setVarroughnessFactorCoreMaterialDesc(this.agpCptr, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getRoughnessFactor() {
        return CoreJni.getVarroughnessFactorCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setMetallicFactor(float f) {
        CoreJni.setVarmetallicFactorCoreMaterialDesc(this.agpCptr, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getMetallicFactor() {
        return CoreJni.getVarmetallicFactorCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setReflectance(float f) {
        CoreJni.setVarreflectanceCoreMaterialDesc(this.agpCptr, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getReflectance() {
        return CoreJni.getVarreflectanceCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setNormalScale(float f) {
        CoreJni.setVarnormalScaleCoreMaterialDesc(this.agpCptr, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getNormalScale() {
        return CoreJni.getVarnormalScaleCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setSpecularFactor(CoreVec3 coreVec3) {
        CoreJni.setVarspecularFactorCoreMaterialDesc(this.agpCptr, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getSpecularFactor() {
        long varspecularFactorCoreMaterialDesc = CoreJni.getVarspecularFactorCoreMaterialDesc(this.agpCptr, this);
        if (varspecularFactorCoreMaterialDesc == 0) {
            return null;
        }
        return new CoreVec3(varspecularFactorCoreMaterialDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setGlossinessFactor(float f) {
        CoreJni.setVarglossinessFactorCoreMaterialDesc(this.agpCptr, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getGlossinessFactor() {
        return CoreJni.getVarglossinessFactorCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setClearCoatFactor(float f) {
        CoreJni.setVarclearCoatFactorCoreMaterialDesc(this.agpCptr, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getClearCoatFactor() {
        return CoreJni.getVarclearCoatFactorCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setClearCoatRoughness(float f) {
        CoreJni.setVarclearCoatRoughnessCoreMaterialDesc(this.agpCptr, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getClearCoatRoughness() {
        return CoreJni.getVarclearCoatRoughnessCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setAlphaMode(CoreMaterialAlphaMode coreMaterialAlphaMode) {
        CoreJni.setVaralphaModeCoreMaterialDesc(this.agpCptr, this, coreMaterialAlphaMode.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreMaterialAlphaMode getAlphaMode() {
        return CoreMaterialAlphaMode.swigToEnum(CoreJni.getVaralphaModeCoreMaterialDesc(this.agpCptr, this));
    }

    /* access modifiers changed from: package-private */
    public void setAlphaCutoff(float f) {
        CoreJni.setVaralphaCutoffCoreMaterialDesc(this.agpCptr, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getAlphaCutoff() {
        return CoreJni.getVaralphaCutoffCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setMaterialFlags(long j) {
        CoreJni.setVarmaterialFlagsCoreMaterialDesc(this.agpCptr, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getMaterialFlags() {
        return CoreJni.getVarmaterialFlagsCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setExtraMaterialRenderingFlags(long j) {
        CoreJni.setVarextraMaterialRenderingFlagsCoreMaterialDesc(this.agpCptr, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getExtraMaterialRenderingFlags() {
        return CoreJni.getVarextraMaterialRenderingFlagsCoreMaterialDesc(this.agpCptr, this);
    }

    CoreMaterialDesc() {
        this(CoreJni.newCoreMaterialDesc(), true);
    }
}
