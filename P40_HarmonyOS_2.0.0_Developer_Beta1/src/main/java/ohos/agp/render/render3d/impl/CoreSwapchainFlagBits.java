package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public enum CoreSwapchainFlagBits {
    CORE_SWAPCHAIN_COLOR_BUFFER_BIT(1),
    CORE_SWAPCHAIN_DEPTH_BUFFER_BIT(2);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreSwapchainFlagBits swigToEnum(int i) {
        CoreSwapchainFlagBits[] coreSwapchainFlagBitsArr = (CoreSwapchainFlagBits[]) CoreSwapchainFlagBits.class.getEnumConstants();
        if (i < coreSwapchainFlagBitsArr.length && i >= 0 && coreSwapchainFlagBitsArr[i].swigValue == i) {
            return coreSwapchainFlagBitsArr[i];
        }
        for (CoreSwapchainFlagBits coreSwapchainFlagBits : coreSwapchainFlagBitsArr) {
            if (coreSwapchainFlagBits.swigValue == i) {
                return coreSwapchainFlagBits;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreSwapchainFlagBits.class + " with value " + i);
    }

    private CoreSwapchainFlagBits() {
        this(SwigNext.next);
    }

    private CoreSwapchainFlagBits(int i) {
        this.swigValue = i;
        int unused = SwigNext.next = i + 1;
    }

    private CoreSwapchainFlagBits(CoreSwapchainFlagBits coreSwapchainFlagBits) {
        this(coreSwapchainFlagBits.swigValue);
    }

    private static class SwigNext {
        private static int next;

        private SwigNext() {
        }
    }
}
