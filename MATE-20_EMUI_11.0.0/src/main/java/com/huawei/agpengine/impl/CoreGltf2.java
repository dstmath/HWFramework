package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreGltf2 {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGltf2(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreGltf2 obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: package-private */
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public CoreGltfLoadResult loadGltf(String uri) {
        return new CoreGltfLoadResult(CoreJni.loadGltfInCoreGltf20(this.agpCptr, this, uri), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGltfLoadResult loadGltf(CoreByteArrayView data) {
        return new CoreGltfLoadResult(CoreJni.loadGltfInCoreGltf21(this.agpCptr, this, CoreByteArrayView.getCptr(data), data), true);
    }

    /* access modifiers changed from: package-private */
    public boolean saveGltf(CoreEcs ecs, String uri) {
        return CoreJni.saveGltfInCoreGltf2(this.agpCptr, this, CoreEcs.getCptr(ecs), ecs, uri);
    }

    /* access modifiers changed from: package-private */
    public CoreGltf2ImporterPtr createGltf2Importer() {
        return new CoreGltf2ImporterPtr(CoreJni.createGltf2ImporterInCoreGltf2(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public int importGltfScene(long sceneIndex, CoreGltfData gltfData, CoreGltfResourceData gltfImportData, CoreEcs ecs, int rootEntity, long flags) {
        return CoreJni.importGltfSceneInCoreGltf20(this.agpCptr, this, sceneIndex, CoreGltfData.getCptr(gltfData), gltfData, CoreGltfResourceData.getCptr(gltfImportData), gltfImportData, CoreEcs.getCptr(ecs), ecs, rootEntity, flags);
    }

    /* access modifiers changed from: package-private */
    public int importGltfScene(long sceneIndex, CoreGltfData gltfData, CoreGltfResourceData gltfImportData, CoreEcs ecs, int rootEntity) {
        return CoreJni.importGltfSceneInCoreGltf21(this.agpCptr, this, sceneIndex, CoreGltfData.getCptr(gltfData), gltfData, CoreGltfResourceData.getCptr(gltfImportData), gltfImportData, CoreEcs.getCptr(ecs), ecs, rootEntity);
    }

    /* access modifiers changed from: package-private */
    public int importGltfScene(long sceneIndex, CoreGltfData gltfData, CoreGltfResourceData gltfImportData, CoreEcs ecs) {
        return CoreJni.importGltfSceneInCoreGltf22(this.agpCptr, this, sceneIndex, CoreGltfData.getCptr(gltfData), gltfData, CoreGltfResourceData.getCptr(gltfImportData), gltfImportData, CoreEcs.getCptr(ecs), ecs);
    }

    /* access modifiers changed from: package-private */
    public void releaseGltfResources(CoreGltfResourceData resourceData) {
        CoreJni.releaseGltfResourcesInCoreGltf2(this.agpCptr, this, CoreGltfResourceData.getCptr(resourceData), resourceData);
    }
}
