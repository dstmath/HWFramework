package ohos.agp.render.render3d.impl;

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

    static CoreEnvironmentBackgroundType swigToEnum(int i) {
        CoreEnvironmentBackgroundType[] coreEnvironmentBackgroundTypeArr = (CoreEnvironmentBackgroundType[]) CoreEnvironmentBackgroundType.class.getEnumConstants();
        if (i < coreEnvironmentBackgroundTypeArr.length && i >= 0 && coreEnvironmentBackgroundTypeArr[i].swigValue == i) {
            return coreEnvironmentBackgroundTypeArr[i];
        }
        for (CoreEnvironmentBackgroundType coreEnvironmentBackgroundType : coreEnvironmentBackgroundTypeArr) {
            if (coreEnvironmentBackgroundType.swigValue == i) {
                return coreEnvironmentBackgroundType;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreEnvironmentBackgroundType.class + " with value " + i);
    }

    private CoreEnvironmentBackgroundType() {
        this(SwigNext.next);
    }

    private CoreEnvironmentBackgroundType(int i) {
        this.swigValue = i;
        int unused = SwigNext.next = i + 1;
    }

    private CoreEnvironmentBackgroundType(CoreEnvironmentBackgroundType coreEnvironmentBackgroundType) {
        this(coreEnvironmentBackgroundType.swigValue);
    }

    private static class SwigNext {
        private static int next;

        private SwigNext() {
        }
    }
}
