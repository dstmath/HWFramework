package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public enum CoreLightType {
    CORE_LIGHT_TYPE_INVALID(0),
    CORE_LIGHT_TYPE_DIRECTIONAL(1),
    CORE_LIGHT_TYPE_POINT(2),
    CORE_LIGHT_TYPE_SPOT(3);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreLightType swigToEnum(int i) {
        CoreLightType[] coreLightTypeArr = (CoreLightType[]) CoreLightType.class.getEnumConstants();
        if (i < coreLightTypeArr.length && i >= 0 && coreLightTypeArr[i].swigValue == i) {
            return coreLightTypeArr[i];
        }
        for (CoreLightType coreLightType : coreLightTypeArr) {
            if (coreLightType.swigValue == i) {
                return coreLightType;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreLightType.class + " with value " + i);
    }

    private CoreLightType() {
        this(SwigNext.next);
    }

    private CoreLightType(int i) {
        this.swigValue = i;
        int unused = SwigNext.next = i + 1;
    }

    private CoreLightType(CoreLightType coreLightType) {
        this(coreLightType.swigValue);
    }

    private static class SwigNext {
        private static int next;

        private SwigNext() {
        }
    }
}
