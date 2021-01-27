package com.huawei.agpengine.impl;

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

    static CoreGltfSceneImportFlagBits swigToEnum(int swigValue2) {
        CoreGltfSceneImportFlagBits[] swigValues = (CoreGltfSceneImportFlagBits[]) CoreGltfSceneImportFlagBits.class.getEnumConstants();
        if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
            return swigValues[swigValue2];
        }
        for (CoreGltfSceneImportFlagBits swigEnum : swigValues) {
            if (swigEnum.swigValue == swigValue2) {
                return swigEnum;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreGltfSceneImportFlagBits.class + " with value " + swigValue2);
    }

    private CoreGltfSceneImportFlagBits() {
        this.swigValue = SwigNext.next;
        SwigNext.access$008();
    }

    private CoreGltfSceneImportFlagBits(int swigValue2) {
        this.swigValue = swigValue2;
        int unused = SwigNext.next = swigValue2 + 1;
    }

    private CoreGltfSceneImportFlagBits(CoreGltfSceneImportFlagBits swigEnum) {
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
