package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public enum CoreSwapchainFlagBits {
    CORE_SWAPCHAIN_COLOR_BUFFER_BIT(1),
    CORE_SWAPCHAIN_DEPTH_BUFFER_BIT(2);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreSwapchainFlagBits swigToEnum(int swigValue2) {
        CoreSwapchainFlagBits[] swigValues = (CoreSwapchainFlagBits[]) CoreSwapchainFlagBits.class.getEnumConstants();
        if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
            return swigValues[swigValue2];
        }
        for (CoreSwapchainFlagBits swigEnum : swigValues) {
            if (swigEnum.swigValue == swigValue2) {
                return swigEnum;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreSwapchainFlagBits.class + " with value " + swigValue2);
    }

    private CoreSwapchainFlagBits() {
        this.swigValue = SwigNext.next;
        SwigNext.access$008();
    }

    private CoreSwapchainFlagBits(int swigValue2) {
        this.swigValue = swigValue2;
        int unused = SwigNext.next = swigValue2 + 1;
    }

    private CoreSwapchainFlagBits(CoreSwapchainFlagBits swigEnum) {
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
