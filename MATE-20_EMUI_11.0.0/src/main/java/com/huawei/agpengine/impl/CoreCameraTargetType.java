package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public enum CoreCameraTargetType {
    CORE_CAMERA_TARGET_TYPE_DEFAULT(0),
    CORE_CAMERA_TARGET_TYPE_CUSTOM(1);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreCameraTargetType swigToEnum(int swigValue2) {
        CoreCameraTargetType[] swigValues = (CoreCameraTargetType[]) CoreCameraTargetType.class.getEnumConstants();
        if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
            return swigValues[swigValue2];
        }
        for (CoreCameraTargetType swigEnum : swigValues) {
            if (swigEnum.swigValue == swigValue2) {
                return swigEnum;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreCameraTargetType.class + " with value " + swigValue2);
    }

    private CoreCameraTargetType() {
        this.swigValue = SwigNext.next;
        SwigNext.access$008();
    }

    private CoreCameraTargetType(int swigValue2) {
        this.swigValue = swigValue2;
        int unused = SwigNext.next = swigValue2 + 1;
    }

    private CoreCameraTargetType(CoreCameraTargetType swigEnum) {
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
