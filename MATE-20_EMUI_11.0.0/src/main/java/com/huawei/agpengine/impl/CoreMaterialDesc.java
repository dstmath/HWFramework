package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreMaterialDesc {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreMaterialDesc(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMaterialDesc obj) {
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
                CoreJni.deleteCoreMaterialDesc(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setType(CoreMaterialType value) {
        CoreJni.setVartypeCoreMaterialDesc(this.agpCptr, this, value.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreMaterialType getType() {
        return CoreMaterialType.swigToEnum(CoreJni.getVartypeCoreMaterialDesc(this.agpCptr, this));
    }

    /* access modifiers changed from: package-private */
    public void setBaseColor(long value) {
        CoreJni.setVarbaseColorCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getBaseColor() {
        return CoreJni.getVarbaseColorCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setNormal(long value) {
        CoreJni.setVarnormalCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getNormal() {
        return CoreJni.getVarnormalCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setEmissive(long value) {
        CoreJni.setVaremissiveCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getEmissive() {
        return CoreJni.getVaremissiveCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setMaterial(long value) {
        CoreJni.setVarmaterialCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getMaterial() {
        return CoreJni.getVarmaterialCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setAo(long value) {
        CoreJni.setVaraoCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getAo() {
        return CoreJni.getVaraoCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setSampler(long value) {
        CoreJni.setVarsamplerCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getSampler() {
        return CoreJni.getVarsamplerCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setBaseColorFactor(CoreVec4 value) {
        CoreJni.setVarbaseColorFactorCoreMaterialDesc(this.agpCptr, this, CoreVec4.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4 getBaseColorFactor() {
        long cptr = CoreJni.getVarbaseColorFactorCoreMaterialDesc(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec4(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setEmissiveFactor(CoreVec3 value) {
        CoreJni.setVaremissiveFactorCoreMaterialDesc(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getEmissiveFactor() {
        long cptr = CoreJni.getVaremissiveFactorCoreMaterialDesc(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setAmbientOcclusionFactor(float value) {
        CoreJni.setVarambientOcclusionFactorCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getAmbientOcclusionFactor() {
        return CoreJni.getVarambientOcclusionFactorCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setRoughnessFactor(float value) {
        CoreJni.setVarroughnessFactorCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getRoughnessFactor() {
        return CoreJni.getVarroughnessFactorCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setMetallicFactor(float value) {
        CoreJni.setVarmetallicFactorCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getMetallicFactor() {
        return CoreJni.getVarmetallicFactorCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setReflectance(float value) {
        CoreJni.setVarreflectanceCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getReflectance() {
        return CoreJni.getVarreflectanceCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setNormalScale(float value) {
        CoreJni.setVarnormalScaleCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getNormalScale() {
        return CoreJni.getVarnormalScaleCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setSpecularFactor(CoreVec3 value) {
        CoreJni.setVarspecularFactorCoreMaterialDesc(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getSpecularFactor() {
        long cptr = CoreJni.getVarspecularFactorCoreMaterialDesc(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setGlossinessFactor(float value) {
        CoreJni.setVarglossinessFactorCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getGlossinessFactor() {
        return CoreJni.getVarglossinessFactorCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setClearCoatFactor(float value) {
        CoreJni.setVarclearCoatFactorCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getClearCoatFactor() {
        return CoreJni.getVarclearCoatFactorCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setClearCoatRoughness(float value) {
        CoreJni.setVarclearCoatRoughnessCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getClearCoatRoughness() {
        return CoreJni.getVarclearCoatRoughnessCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setAlphaMode(CoreMaterialAlphaMode value) {
        CoreJni.setVaralphaModeCoreMaterialDesc(this.agpCptr, this, value.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreMaterialAlphaMode getAlphaMode() {
        return CoreMaterialAlphaMode.swigToEnum(CoreJni.getVaralphaModeCoreMaterialDesc(this.agpCptr, this));
    }

    /* access modifiers changed from: package-private */
    public void setAlphaCutoff(float value) {
        CoreJni.setVaralphaCutoffCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getAlphaCutoff() {
        return CoreJni.getVaralphaCutoffCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setMaterialFlags(long value) {
        CoreJni.setVarmaterialFlagsCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getMaterialFlags() {
        return CoreJni.getVarmaterialFlagsCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setCustomMaterialShader(long value) {
        CoreJni.setVarcustomMaterialShaderCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getCustomMaterialShader() {
        return CoreJni.getVarcustomMaterialShaderCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setExtraMaterialRenderingFlags(long value) {
        CoreJni.setVarextraMaterialRenderingFlagsCoreMaterialDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getExtraMaterialRenderingFlags() {
        return CoreJni.getVarextraMaterialRenderingFlagsCoreMaterialDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setTransform(CoreMat3X3 value) {
        CoreJni.setVartransformCoreMaterialDesc(this.agpCptr, this, CoreMat3X3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreMat3X3 getTransform() {
        long cptr = CoreJni.getVartransformCoreMaterialDesc(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreMat3X3(cptr, false);
    }

    CoreMaterialDesc() {
        this(CoreJni.newCoreMaterialDesc(), true);
    }
}
