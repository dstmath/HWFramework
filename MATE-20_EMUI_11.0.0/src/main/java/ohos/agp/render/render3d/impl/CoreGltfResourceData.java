package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreGltfResourceData {
    private transient long agpCptrCoreGltfResourceData;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreGltfResourceData(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreGltfResourceData = j;
    }

    static long getCptr(CoreGltfResourceData coreGltfResourceData) {
        if (coreGltfResourceData == null) {
            return 0;
        }
        return coreGltfResourceData.agpCptrCoreGltfResourceData;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreGltfResourceData != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreGltfResourceData(this.agpCptrCoreGltfResourceData);
                }
                this.agpCptrCoreGltfResourceData = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreGltfResourceData coreGltfResourceData, boolean z) {
        if (coreGltfResourceData != null) {
            synchronized (coreGltfResourceData.lock) {
                coreGltfResourceData.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreGltfResourceData);
    }

    /* access modifiers changed from: package-private */
    public void setSamplers(CoreGpuResourceArray coreGpuResourceArray) {
        CoreJni.setVarsamplersCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this, CoreGpuResourceArray.getCptr(coreGpuResourceArray), coreGpuResourceArray);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceArray getSamplers() {
        long varsamplersCoreGltfResourceData = CoreJni.getVarsamplersCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this);
        if (varsamplersCoreGltfResourceData == 0) {
            return null;
        }
        return new CoreGpuResourceArray(varsamplersCoreGltfResourceData, false);
    }

    /* access modifiers changed from: package-private */
    public void setImages(CoreResourceArray coreResourceArray) {
        CoreJni.setVarimagesCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this, CoreResourceArray.getCptr(coreResourceArray), coreResourceArray);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceArray getImages() {
        long varimagesCoreGltfResourceData = CoreJni.getVarimagesCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this);
        if (varimagesCoreGltfResourceData == 0) {
            return null;
        }
        return new CoreResourceArray(varimagesCoreGltfResourceData, false);
    }

    /* access modifiers changed from: package-private */
    public void setTextures(CoreGpuResourceArray coreGpuResourceArray) {
        CoreJni.setVartexturesCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this, CoreGpuResourceArray.getCptr(coreGpuResourceArray), coreGpuResourceArray);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceArray getTextures() {
        long vartexturesCoreGltfResourceData = CoreJni.getVartexturesCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this);
        if (vartexturesCoreGltfResourceData == 0) {
            return null;
        }
        return new CoreGpuResourceArray(vartexturesCoreGltfResourceData, false);
    }

    /* access modifiers changed from: package-private */
    public void setMaterials(CoreResourceArray coreResourceArray) {
        CoreJni.setVarmaterialsCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this, CoreResourceArray.getCptr(coreResourceArray), coreResourceArray);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceArray getMaterials() {
        long varmaterialsCoreGltfResourceData = CoreJni.getVarmaterialsCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this);
        if (varmaterialsCoreGltfResourceData == 0) {
            return null;
        }
        return new CoreResourceArray(varmaterialsCoreGltfResourceData, false);
    }

    /* access modifiers changed from: package-private */
    public void setMeshes(CoreResourceArray coreResourceArray) {
        CoreJni.setVarmeshesCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this, CoreResourceArray.getCptr(coreResourceArray), coreResourceArray);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceArray getMeshes() {
        long varmeshesCoreGltfResourceData = CoreJni.getVarmeshesCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this);
        if (varmeshesCoreGltfResourceData == 0) {
            return null;
        }
        return new CoreResourceArray(varmeshesCoreGltfResourceData, false);
    }

    /* access modifiers changed from: package-private */
    public void setSkins(CoreResourceArray coreResourceArray) {
        CoreJni.setVarskinsCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this, CoreResourceArray.getCptr(coreResourceArray), coreResourceArray);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceArray getSkins() {
        long varskinsCoreGltfResourceData = CoreJni.getVarskinsCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this);
        if (varskinsCoreGltfResourceData == 0) {
            return null;
        }
        return new CoreResourceArray(varskinsCoreGltfResourceData, false);
    }

    /* access modifiers changed from: package-private */
    public void setAnimations(CoreResourceArray coreResourceArray) {
        CoreJni.setVaranimationsCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this, CoreResourceArray.getCptr(coreResourceArray), coreResourceArray);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceArray getAnimations() {
        long varanimationsCoreGltfResourceData = CoreJni.getVaranimationsCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this);
        if (varanimationsCoreGltfResourceData == 0) {
            return null;
        }
        return new CoreResourceArray(varanimationsCoreGltfResourceData, false);
    }

    /* access modifiers changed from: package-private */
    public void setSpecularRadianceCubemaps(CoreGpuResourceArray coreGpuResourceArray) {
        CoreJni.setVarspecularRadianceCubemapsCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this, CoreGpuResourceArray.getCptr(coreGpuResourceArray), coreGpuResourceArray);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceArray getSpecularRadianceCubemaps() {
        long varspecularRadianceCubemapsCoreGltfResourceData = CoreJni.getVarspecularRadianceCubemapsCoreGltfResourceData(this.agpCptrCoreGltfResourceData, this);
        if (varspecularRadianceCubemapsCoreGltfResourceData == 0) {
            return null;
        }
        return new CoreGpuResourceArray(varspecularRadianceCubemapsCoreGltfResourceData, false);
    }

    CoreGltfResourceData() {
        this(CoreJni.newCoreGltfResourceData(), true);
    }
}
