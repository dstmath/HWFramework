package ohos.agp.render.render3d.impl;

enum CoreMaterialAlphaMode {
    CORE_ALPHA_MODE_OPAQUE(0),
    CORE_ALPHA_MODE_MASK(1),
    CORE_ALPHA_MODE_BLEND(2);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreMaterialAlphaMode swigToEnum(int i) {
        CoreMaterialAlphaMode[] coreMaterialAlphaModeArr = (CoreMaterialAlphaMode[]) CoreMaterialAlphaMode.class.getEnumConstants();
        if (i < coreMaterialAlphaModeArr.length && i >= 0 && coreMaterialAlphaModeArr[i].swigValue == i) {
            return coreMaterialAlphaModeArr[i];
        }
        for (CoreMaterialAlphaMode coreMaterialAlphaMode : coreMaterialAlphaModeArr) {
            if (coreMaterialAlphaMode.swigValue == i) {
                return coreMaterialAlphaMode;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreMaterialAlphaMode.class + " with value " + i);
    }

    private CoreMaterialAlphaMode() {
        this(SwigNext.next);
    }

    private CoreMaterialAlphaMode(int i) {
        this.swigValue = i;
        int unused = SwigNext.next = i + 1;
    }

    private CoreMaterialAlphaMode(CoreMaterialAlphaMode coreMaterialAlphaMode) {
        this(coreMaterialAlphaMode.swigValue);
    }

    private static class SwigNext {
        private static int next;

        private SwigNext() {
        }
    }
}
