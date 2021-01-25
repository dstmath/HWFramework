package ohos.agp.render.render3d.impl;

enum CoreAnimationType {
    TRANSLATION,
    ROTATION,
    SCALE,
    WEIGHTS;
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreAnimationType swigToEnum(int i) {
        CoreAnimationType[] coreAnimationTypeArr = (CoreAnimationType[]) CoreAnimationType.class.getEnumConstants();
        if (i < coreAnimationTypeArr.length && i >= 0 && coreAnimationTypeArr[i].swigValue == i) {
            return coreAnimationTypeArr[i];
        }
        for (CoreAnimationType coreAnimationType : coreAnimationTypeArr) {
            if (coreAnimationType.swigValue == i) {
                return coreAnimationType;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreAnimationType.class + " with value " + i);
    }

    private CoreAnimationType() {
        this(SwigNext.next);
    }

    private CoreAnimationType(int i) {
        this.swigValue = i;
        int unused = SwigNext.next = i + 1;
    }

    private CoreAnimationType(CoreAnimationType coreAnimationType) {
        this(coreAnimationType.swigValue);
    }

    private static class SwigNext {
        private static int next;

        private SwigNext() {
        }
    }
}
