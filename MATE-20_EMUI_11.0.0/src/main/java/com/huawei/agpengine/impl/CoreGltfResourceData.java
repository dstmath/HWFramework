package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreGltfResourceData {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGltfResourceData(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreGltfResourceData obj) {
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
                CoreJni.deleteCoreGltfResourceData(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setSamplers(CoreGpuResourceArray value) {
        CoreJni.setVarsamplersCoreGltfResourceData(this.agpCptr, this, CoreGpuResourceArray.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceArray getSamplers() {
        long cptr = CoreJni.getVarsamplersCoreGltfResourceData(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreGpuResourceArray(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setImages(CoreResourceArray value) {
        CoreJni.setVarimagesCoreGltfResourceData(this.agpCptr, this, CoreResourceArray.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceArray getImages() {
        long cptr = CoreJni.getVarimagesCoreGltfResourceData(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreResourceArray(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setTextures(CoreGpuResourceArray value) {
        CoreJni.setVartexturesCoreGltfResourceData(this.agpCptr, this, CoreGpuResourceArray.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceArray getTextures() {
        long cptr = CoreJni.getVartexturesCoreGltfResourceData(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreGpuResourceArray(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setMaterials(CoreResourceArray value) {
        CoreJni.setVarmaterialsCoreGltfResourceData(this.agpCptr, this, CoreResourceArray.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceArray getMaterials() {
        long cptr = CoreJni.getVarmaterialsCoreGltfResourceData(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreResourceArray(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setMeshes(CoreResourceArray value) {
        CoreJni.setVarmeshesCoreGltfResourceData(this.agpCptr, this, CoreResourceArray.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceArray getMeshes() {
        long cptr = CoreJni.getVarmeshesCoreGltfResourceData(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreResourceArray(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setSkins(CoreResourceArray value) {
        CoreJni.setVarskinsCoreGltfResourceData(this.agpCptr, this, CoreResourceArray.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceArray getSkins() {
        long cptr = CoreJni.getVarskinsCoreGltfResourceData(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreResourceArray(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setAnimations(CoreResourceArray value) {
        CoreJni.setVaranimationsCoreGltfResourceData(this.agpCptr, this, CoreResourceArray.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceArray getAnimations() {
        long cptr = CoreJni.getVaranimationsCoreGltfResourceData(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreResourceArray(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setSpecularRadianceCubemaps(CoreGpuResourceArray value) {
        CoreJni.setVarspecularRadianceCubemapsCoreGltfResourceData(this.agpCptr, this, CoreGpuResourceArray.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceArray getSpecularRadianceCubemaps() {
        long cptr = CoreJni.getVarspecularRadianceCubemapsCoreGltfResourceData(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreGpuResourceArray(cptr, false);
    }

    CoreGltfResourceData() {
        this(CoreJni.newCoreGltfResourceData(), true);
    }
}
