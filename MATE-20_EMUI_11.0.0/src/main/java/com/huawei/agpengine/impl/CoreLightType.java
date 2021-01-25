package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public enum CoreLightType {
    CORE_LIGHT_TYPE_INVALID(0),
    CORE_LIGHT_TYPE_DIRECTIONAL(1),
    CORE_LIGHT_TYPE_POINT(2),
    CORE_LIGHT_TYPE_SPOT(3);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreLightType swigToEnum(int swigValue2) {
        CoreLightType[] swigValues = (CoreLightType[]) CoreLightType.class.getEnumConstants();
        if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
            return swigValues[swigValue2];
        }
        for (CoreLightType swigEnum : swigValues) {
            if (swigEnum.swigValue == swigValue2) {
                return swigEnum;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreLightType.class + " with value " + swigValue2);
    }

    private CoreLightType() {
        this.swigValue = SwigNext.next;
        SwigNext.access$008();
    }

    private CoreLightType(int swigValue2) {
        this.swigValue = swigValue2;
        int unused = SwigNext.next = swigValue2 + 1;
    }

    private CoreLightType(CoreLightType swigEnum) {
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
