package com.huawei.agpengine.impl;

enum CorePropertyFlags {
    HAS_MIN(1),
    HAS_MAX(2),
    IS_SLIDER(4),
    IS_HIDDEN(8),
    IS_READONLY(16);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CorePropertyFlags swigToEnum(int swigValue2) {
        CorePropertyFlags[] swigValues = (CorePropertyFlags[]) CorePropertyFlags.class.getEnumConstants();
        if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
            return swigValues[swigValue2];
        }
        for (CorePropertyFlags swigEnum : swigValues) {
            if (swigEnum.swigValue == swigValue2) {
                return swigEnum;
            }
        }
        throw new IllegalArgumentException("No enum " + CorePropertyFlags.class + " with value " + swigValue2);
    }

    private CorePropertyFlags() {
        this.swigValue = SwigNext.next;
        SwigNext.access$008();
    }

    private CorePropertyFlags(int swigValue2) {
        this.swigValue = swigValue2;
        int unused = SwigNext.next = swigValue2 + 1;
    }

    private CorePropertyFlags(CorePropertyFlags swigEnum) {
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
