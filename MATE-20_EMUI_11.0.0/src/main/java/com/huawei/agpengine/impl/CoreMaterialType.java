package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public enum CoreMaterialType {
    CORE_MATERIAL_METALLIC_ROUGHNESS(0),
    CORE_MATERIAL_SPECULAR_GLOSSINESS(1),
    CORE_MATERIAL_UNLIT(2),
    CORE_MATERIAL_UNLIT_SHADOW_ALPHA(3);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreMaterialType swigToEnum(int swigValue2) {
        CoreMaterialType[] swigValues = (CoreMaterialType[]) CoreMaterialType.class.getEnumConstants();
        if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
            return swigValues[swigValue2];
        }
        for (CoreMaterialType swigEnum : swigValues) {
            if (swigEnum.swigValue == swigValue2) {
                return swigEnum;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreMaterialType.class + " with value " + swigValue2);
    }

    private CoreMaterialType() {
        this.swigValue = SwigNext.next;
        SwigNext.access$008();
    }

    private CoreMaterialType(int swigValue2) {
        this.swigValue = swigValue2;
        int unused = SwigNext.next = swigValue2 + 1;
    }

    private CoreMaterialType(CoreMaterialType swigEnum) {
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
