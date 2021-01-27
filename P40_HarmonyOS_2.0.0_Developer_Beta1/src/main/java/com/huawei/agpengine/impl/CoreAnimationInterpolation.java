package com.huawei.agpengine.impl;

enum CoreAnimationInterpolation {
    CORE_INTERPOLATION_TYPE_STEP,
    CORE_INTERPOLATION_TYPE_LINEAR,
    CORE_INTERPOLATION_TYPE_SPLINE;
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreAnimationInterpolation swigToEnum(int swigValue2) {
        CoreAnimationInterpolation[] swigValues = (CoreAnimationInterpolation[]) CoreAnimationInterpolation.class.getEnumConstants();
        if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
            return swigValues[swigValue2];
        }
        for (CoreAnimationInterpolation swigEnum : swigValues) {
            if (swigEnum.swigValue == swigValue2) {
                return swigEnum;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreAnimationInterpolation.class + " with value " + swigValue2);
    }

    private CoreAnimationInterpolation() {
        this.swigValue = SwigNext.next;
        SwigNext.access$008();
    }

    private CoreAnimationInterpolation(int swigValue2) {
        this.swigValue = swigValue2;
        int unused = SwigNext.next = swigValue2 + 1;
    }

    private CoreAnimationInterpolation(CoreAnimationInterpolation swigEnum) {
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
