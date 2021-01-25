package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public enum CoreCameraType {
    CORE_CAMERA_TYPE_ORTHOGRAPHIC(0),
    CORE_CAMERA_TYPE_PERSPECTIVE(1),
    CORE_CAMERA_TYPE_CUSTOM(2);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreCameraType swigToEnum(int swigValue2) {
        CoreCameraType[] swigValues = (CoreCameraType[]) CoreCameraType.class.getEnumConstants();
        if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
            return swigValues[swigValue2];
        }
        for (CoreCameraType swigEnum : swigValues) {
            if (swigEnum.swigValue == swigValue2) {
                return swigEnum;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreCameraType.class + " with value " + swigValue2);
    }

    private CoreCameraType() {
        this.swigValue = SwigNext.next;
        SwigNext.access$008();
    }

    private CoreCameraType(int swigValue2) {
        this.swigValue = swigValue2;
        int unused = SwigNext.next = swigValue2 + 1;
    }

    private CoreCameraType(CoreCameraType swigEnum) {
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
