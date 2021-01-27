package ohos.agp.render.render3d.impl;

enum CoreGltfSceneImportFlagBits {
    CORE_GLTF_IMPORT_COMPONENT_SCENE(1),
    CORE_GLTF_IMPORT_COMPONENT_MESH(2),
    CORE_GLTF_IMPORT_COMPONENT_CAMERA(4),
    CORE_GLTF_IMPORT_COMPONENT_SKIN(8),
    CORE_GLTF_IMPORT_COMPONENT_LIGHT(16),
    CORE_GLTF_IMPORT_COMPONENT_MORPH(32),
    CORE_GLTF_IMPORT_COMPONENT_FLAG_BITS_ALL(Integer.MAX_VALUE);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreGltfSceneImportFlagBits swigToEnum(int i) {
        CoreGltfSceneImportFlagBits[] coreGltfSceneImportFlagBitsArr = (CoreGltfSceneImportFlagBits[]) CoreGltfSceneImportFlagBits.class.getEnumConstants();
        if (i < coreGltfSceneImportFlagBitsArr.length && i >= 0 && coreGltfSceneImportFlagBitsArr[i].swigValue == i) {
            return coreGltfSceneImportFlagBitsArr[i];
        }
        for (CoreGltfSceneImportFlagBits coreGltfSceneImportFlagBits : coreGltfSceneImportFlagBitsArr) {
            if (coreGltfSceneImportFlagBits.swigValue == i) {
                return coreGltfSceneImportFlagBits;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreGltfSceneImportFlagBits.class + " with value " + i);
    }

    private CoreGltfSceneImportFlagBits() {
        this(SwigNext.next);
    }

    private CoreGltfSceneImportFlagBits(int i) {
        this.swigValue = i;
        int unused = SwigNext.next = i + 1;
    }

    private CoreGltfSceneImportFlagBits(CoreGltfSceneImportFlagBits coreGltfSceneImportFlagBits) {
        this(coreGltfSceneImportFlagBits.swigValue);
    }

    private static class SwigNext {
        private static int next;

        private SwigNext() {
        }
    }
}
