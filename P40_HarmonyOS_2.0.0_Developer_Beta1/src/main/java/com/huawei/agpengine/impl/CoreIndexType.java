package com.huawei.agpengine.impl;

enum CoreIndexType {
    CORE_INDEX_TYPE_UINT16(0),
    CORE_INDEX_TYPE_UINT32(1),
    CORE_INDEX_TYPE_MAX_ENUM(Integer.MAX_VALUE);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreIndexType swigToEnum(int swigValue2) {
        CoreIndexType[] swigValues = (CoreIndexType[]) CoreIndexType.class.getEnumConstants();
        if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
            return swigValues[swigValue2];
        }
        for (CoreIndexType swigEnum : swigValues) {
            if (swigEnum.swigValue == swigValue2) {
                return swigEnum;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreIndexType.class + " with value " + swigValue2);
    }

    private CoreIndexType() {
        this.swigValue = SwigNext.next;
        SwigNext.access$008();
    }

    private CoreIndexType(int swigValue2) {
        this.swigValue = swigValue2;
        int unused = SwigNext.next = swigValue2 + 1;
    }

    private CoreIndexType(CoreIndexType swigEnum) {
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
