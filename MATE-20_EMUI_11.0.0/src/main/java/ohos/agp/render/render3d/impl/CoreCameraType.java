package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public enum CoreCameraType {
    CORE_CAMERA_TYPE_ORTHOGRAPHIC(0),
    CORE_CAMERA_TYPE_PERSPECTIVE(1),
    CORE_CAMERA_TYPE_CUSTOM(2);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreCameraType swigToEnum(int i) {
        CoreCameraType[] coreCameraTypeArr = (CoreCameraType[]) CoreCameraType.class.getEnumConstants();
        if (i < coreCameraTypeArr.length && i >= 0 && coreCameraTypeArr[i].swigValue == i) {
            return coreCameraTypeArr[i];
        }
        for (CoreCameraType coreCameraType : coreCameraTypeArr) {
            if (coreCameraType.swigValue == i) {
                return coreCameraType;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreCameraType.class + " with value " + i);
    }

    private CoreCameraType() {
        this(SwigNext.next);
    }

    private CoreCameraType(int i) {
        this.swigValue = i;
        int unused = SwigNext.next = i + 1;
    }

    private CoreCameraType(CoreCameraType coreCameraType) {
        this(coreCameraType.swigValue);
    }

    private static class SwigNext {
        private static int next;

        private SwigNext() {
        }
    }
}
