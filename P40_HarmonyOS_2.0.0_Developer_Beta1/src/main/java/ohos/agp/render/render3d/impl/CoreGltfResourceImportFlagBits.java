package ohos.agp.render.render3d.impl;

enum CoreGltfResourceImportFlagBits {
    CORE_GLTF_IMPORT_RESOURCE_SAMPLER(1),
    CORE_GLTF_IMPORT_RESOURCE_IMAGE(2),
    CORE_GLTF_IMPORT_RESOURCE_TEXTURE(4),
    CORE_GLTF_IMPORT_RESOURCE_MATERIAL(8),
    CORE_GLTF_IMPORT_RESOURCE_MESH(16),
    CORE_GLTF_IMPORT_RESOURCE_SKIN(32),
    CORE_GLTF_IMPORT_RESOURCE_ANIMATION(64),
    CORE_GLTF_IMPORT_RESOURCE_SKIP_UNUSED(128),
    CORE_GLTF_IMPORT_RESOURCE_FLAG_BITS_ALL(Integer.MAX_VALUE);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreGltfResourceImportFlagBits swigToEnum(int i) {
        CoreGltfResourceImportFlagBits[] coreGltfResourceImportFlagBitsArr = (CoreGltfResourceImportFlagBits[]) CoreGltfResourceImportFlagBits.class.getEnumConstants();
        if (i < coreGltfResourceImportFlagBitsArr.length && i >= 0 && coreGltfResourceImportFlagBitsArr[i].swigValue == i) {
            return coreGltfResourceImportFlagBitsArr[i];
        }
        for (CoreGltfResourceImportFlagBits coreGltfResourceImportFlagBits : coreGltfResourceImportFlagBitsArr) {
            if (coreGltfResourceImportFlagBits.swigValue == i) {
                return coreGltfResourceImportFlagBits;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreGltfResourceImportFlagBits.class + " with value " + i);
    }

    private CoreGltfResourceImportFlagBits() {
        this(SwigNext.next);
    }

    private CoreGltfResourceImportFlagBits(int i) {
        this.swigValue = i;
        int unused = SwigNext.next = i + 1;
    }

    private CoreGltfResourceImportFlagBits(CoreGltfResourceImportFlagBits coreGltfResourceImportFlagBits) {
        this(coreGltfResourceImportFlagBits.swigValue);
    }

    private static class SwigNext {
        private static int next;

        private SwigNext() {
        }
    }
}
