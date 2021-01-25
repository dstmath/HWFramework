package com.huawei.agpengine.impl;

enum CoreMaterialAlphaMode {
    CORE_ALPHA_MODE_OPAQUE(0),
    CORE_ALPHA_MODE_MASK(1),
    CORE_ALPHA_MODE_BLEND(2);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreMaterialAlphaMode swigToEnum(int swigValue2) {
        CoreMaterialAlphaMode[] swigValues = (CoreMaterialAlphaMode[]) CoreMaterialAlphaMode.class.getEnumConstants();
        if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
            return swigValues[swigValue2];
        }
        for (CoreMaterialAlphaMode swigEnum : swigValues) {
            if (swigEnum.swigValue == swigValue2) {
                return swigEnum;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreMaterialAlphaMode.class + " with value " + swigValue2);
    }

    private CoreMaterialAlphaMode() {
        this.swigValue = SwigNext.next;
        SwigNext.access$008();
    }

    private CoreMaterialAlphaMode(int swigValue2) {
        this.swigValue = swigValue2;
        int unused = SwigNext.next = swigValue2 + 1;
    }

    private CoreMaterialAlphaMode(CoreMaterialAlphaMode swigEnum) {
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
