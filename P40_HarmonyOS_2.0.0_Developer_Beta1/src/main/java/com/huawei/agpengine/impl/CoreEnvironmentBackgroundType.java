package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public enum CoreEnvironmentBackgroundType {
    CORE_ENV_BG_NONE(0),
    CORE_ENV_BG_IMAGE(1),
    CORE_ENV_BG_CUBEMAP(2),
    CORE_ENV_BG_EQUIRECTANGULAR(3);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreEnvironmentBackgroundType swigToEnum(int swigValue2) {
        CoreEnvironmentBackgroundType[] swigValues = (CoreEnvironmentBackgroundType[]) CoreEnvironmentBackgroundType.class.getEnumConstants();
        if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
            return swigValues[swigValue2];
        }
        for (CoreEnvironmentBackgroundType swigEnum : swigValues) {
            if (swigEnum.swigValue == swigValue2) {
                return swigEnum;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreEnvironmentBackgroundType.class + " with value " + swigValue2);
    }

    private CoreEnvironmentBackgroundType() {
        this.swigValue = SwigNext.next;
        SwigNext.access$008();
    }

    private CoreEnvironmentBackgroundType(int swigValue2) {
        this.swigValue = swigValue2;
        int unused = SwigNext.next = swigValue2 + 1;
    }

    private CoreEnvironmentBackgroundType(CoreEnvironmentBackgroundType swigEnum) {
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
