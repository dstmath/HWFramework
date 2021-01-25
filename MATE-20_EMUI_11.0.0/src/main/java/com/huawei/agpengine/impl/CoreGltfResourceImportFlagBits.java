package com.huawei.agpengine.impl;

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

    static CoreGltfResourceImportFlagBits swigToEnum(int swigValue2) {
        CoreGltfResourceImportFlagBits[] swigValues = (CoreGltfResourceImportFlagBits[]) CoreGltfResourceImportFlagBits.class.getEnumConstants();
        if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
            return swigValues[swigValue2];
        }
        for (CoreGltfResourceImportFlagBits swigEnum : swigValues) {
            if (swigEnum.swigValue == swigValue2) {
                return swigEnum;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreGltfResourceImportFlagBits.class + " with value " + swigValue2);
    }

    private CoreGltfResourceImportFlagBits() {
        this.swigValue = SwigNext.next;
        SwigNext.access$008();
    }

    private CoreGltfResourceImportFlagBits(int swigValue2) {
        this.swigValue = swigValue2;
        int unused = SwigNext.next = swigValue2 + 1;
    }

    private CoreGltfResourceImportFlagBits(CoreGltfResourceImportFlagBits swigEnum) {
        this.swigValue = swigEnum.swigValue;
        int unused = SwigNext.next = this.swigValue + 1;
    }

    private static class SwigNext {
        private static int next = 0;

        private SwigNext() {
        }

        static /* synthetic */ int access$008() {
            int i = next;
            next = i + 1;
            return i;
        }
    }
}
