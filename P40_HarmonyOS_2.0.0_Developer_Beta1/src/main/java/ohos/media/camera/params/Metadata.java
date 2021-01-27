package ohos.media.camera.params;

public final class Metadata {

    public @interface AeMode {
        public static final int AE_MODE_OFF = 0;
        public static final int AE_MODE_ON = 1;
    }

    public @interface AeTrigger {
        public static final int AE_TRIGGER_CANCEL = 2;
        public static final int AE_TRIGGER_NONE = 0;
        public static final int AE_TRIGGER_START = 1;
    }

    public @interface AfMode {
        public static final int AF_MODE_CONTINUOUS = 1;
        public static final int AF_MODE_OFF = 0;
        public static final int AF_MODE_TOUCH_LOCK = 2;
    }

    public @interface AfTrigger {
        public static final int AF_TRIGGER_CANCEL = 2;
        public static final int AF_TRIGGER_NONE = 0;
        public static final int AF_TRIGGER_START = 1;
    }

    public @interface AutoZoom {
        public static final int AUTO_ZOOM_FAILED = 2;
        public static final int AUTO_ZOOM_OFF = 0;
        public static final int AUTO_ZOOM_OPEN = 1;
    }

    public @interface FaceAe {
        public static final int FACE_AE_FAILED = 2;
        public static final int FACE_AE_OFF = 0;
        public static final int FACE_AE_OPEN = 1;
    }

    public @interface FaceDetectionType {
        public static final int FACE_DETECTION = 1;
        public static final int FACE_DETECTION_OFF = 0;
        public static final int FACE_SMILE_DETECTION = 2;
    }

    public @interface FlashMode {
        public static final int FLASH_ALWAYS_OPEN = 3;
        public static final int FLASH_AUTO = 0;
        public static final int FLASH_CLOSE = 1;
        public static final int FLASH_OPEN = 2;
    }

    private Metadata() {
    }
}
