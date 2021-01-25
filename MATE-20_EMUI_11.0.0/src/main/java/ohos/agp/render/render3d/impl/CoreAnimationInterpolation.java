package ohos.agp.render.render3d.impl;

enum CoreAnimationInterpolation {
    CORE_INTERPOLATION_TYPE_STEP,
    CORE_INTERPOLATION_TYPE_LINEAR,
    CORE_INTERPOLATION_TYPE_SPLINE;
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreAnimationInterpolation swigToEnum(int i) {
        CoreAnimationInterpolation[] coreAnimationInterpolationArr = (CoreAnimationInterpolation[]) CoreAnimationInterpolation.class.getEnumConstants();
        if (i < coreAnimationInterpolationArr.length && i >= 0 && coreAnimationInterpolationArr[i].swigValue == i) {
            return coreAnimationInterpolationArr[i];
        }
        for (CoreAnimationInterpolation coreAnimationInterpolation : coreAnimationInterpolationArr) {
            if (coreAnimationInterpolation.swigValue == i) {
                return coreAnimationInterpolation;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreAnimationInterpolation.class + " with value " + i);
    }

    private CoreAnimationInterpolation() {
        this(SwigNext.next);
    }

    private CoreAnimationInterpolation(int i) {
        this.swigValue = i;
        int unused = SwigNext.next = i + 1;
    }

    private CoreAnimationInterpolation(CoreAnimationInterpolation coreAnimationInterpolation) {
        this(coreAnimationInterpolation.swigValue);
    }

    private static class SwigNext {
        private static int next;

        private SwigNext() {
        }
    }
}
