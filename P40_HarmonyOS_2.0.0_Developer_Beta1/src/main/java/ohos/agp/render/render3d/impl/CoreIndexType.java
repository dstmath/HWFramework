package ohos.agp.render.render3d.impl;

enum CoreIndexType {
    CORE_INDEX_TYPE_UINT16(0),
    CORE_INDEX_TYPE_UINT32(1),
    CORE_INDEX_TYPE_MAX_ENUM(Integer.MAX_VALUE);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreIndexType swigToEnum(int i) {
        CoreIndexType[] coreIndexTypeArr = (CoreIndexType[]) CoreIndexType.class.getEnumConstants();
        if (i < coreIndexTypeArr.length && i >= 0 && coreIndexTypeArr[i].swigValue == i) {
            return coreIndexTypeArr[i];
        }
        for (CoreIndexType coreIndexType : coreIndexTypeArr) {
            if (coreIndexType.swigValue == i) {
                return coreIndexType;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreIndexType.class + " with value " + i);
    }

    private CoreIndexType() {
        this(SwigNext.next);
    }

    private CoreIndexType(int i) {
        this.swigValue = i;
        int unused = SwigNext.next = i + 1;
    }

    private CoreIndexType(CoreIndexType coreIndexType) {
        this(coreIndexType.swigValue);
    }

    private static class SwigNext {
        private static int next;

        private SwigNext() {
        }
    }
}
