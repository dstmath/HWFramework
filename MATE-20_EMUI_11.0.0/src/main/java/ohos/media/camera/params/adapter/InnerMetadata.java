package ohos.media.camera.params.adapter;

public class InnerMetadata {

    public @interface Capabilities {
        public static final int LOGICAL_MULTI_CAMERA = 11;
        public static final int MONOCHROME = 12;
    }

    public @interface DeviceLinkType {
        public static final int DEVICE_LINK_EXTERNAL_MSDP = 2;
        public static final int DEVICE_LINK_EXTERNAL_USB = 1;
        public static final int DEVICE_LINK_NATIVE = 0;
        public static final int DEVICE_LINK_OTHERS = -1;
    }

    public @interface LensFacing {
        public static final int FACING_BACK = 1;
        public static final int FACING_FRONT = 0;
        public static final int FACING_OTHER = -1;
    }

    public @interface LensOpticalStabilization {
        public static final int STABILIZATION_OFF = 0;
        public static final int STABILIZATION_ON = 1;
    }

    public @interface SceneDetectionType {
        public static final int SMART_SUGGEST_MODE_BEAUTY = 117;
        public static final int SMART_SUGGEST_MODE_DOCUMENT = 17;
    }

    private InnerMetadata() {
    }
}
