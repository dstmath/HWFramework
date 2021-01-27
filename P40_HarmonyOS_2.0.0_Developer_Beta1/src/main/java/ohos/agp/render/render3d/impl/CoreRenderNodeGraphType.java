package ohos.agp.render.render3d.impl;

enum CoreRenderNodeGraphType {
    UNDEFINED(0),
    CLEAR(1);
    
    private final int swigValueRenderNodeGraphType;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValueRenderNodeGraphType;
    }

    static CoreRenderNodeGraphType swigToEnum(int i) {
        CoreRenderNodeGraphType[] coreRenderNodeGraphTypeArr = (CoreRenderNodeGraphType[]) CoreRenderNodeGraphType.class.getEnumConstants();
        if (i < coreRenderNodeGraphTypeArr.length && i >= 0 && coreRenderNodeGraphTypeArr[i].swigValueRenderNodeGraphType == i) {
            return coreRenderNodeGraphTypeArr[i];
        }
        for (CoreRenderNodeGraphType coreRenderNodeGraphType : coreRenderNodeGraphTypeArr) {
            if (coreRenderNodeGraphType.swigValueRenderNodeGraphType == i) {
                return coreRenderNodeGraphType;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreRenderNodeGraphType.class + " with value " + i);
    }

    private CoreRenderNodeGraphType() {
        this(SwigNext.next);
    }

    private CoreRenderNodeGraphType(int i) {
        this.swigValueRenderNodeGraphType = i;
        int unused = SwigNext.next = i + 1;
    }

    private CoreRenderNodeGraphType(CoreRenderNodeGraphType coreRenderNodeGraphType) {
        this(coreRenderNodeGraphType.swigValueRenderNodeGraphType);
    }

    private static class SwigNext {
        private static int next;

        private SwigNext() {
        }
    }
}
