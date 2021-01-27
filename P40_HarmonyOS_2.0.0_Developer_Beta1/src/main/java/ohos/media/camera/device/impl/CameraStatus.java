package ohos.media.camera.device.impl;

public interface CameraStatus {

    public @interface Availability {
        public static final int STATUS_AVAILABLE = 1;
        public static final int STATUS_LINKING = 2;
        public static final int STATUS_UNKNOWN = -1;
        public static final int STATUS_UNLINK = 3;
        public static final int STATUS_USING = 0;
    }

    public @interface Torch {
        public static final int TORCH_AVAILABLE_STATUS_OFF = 1;
        public static final int TORCH_AVAILABLE_STATUS_ON = 2;
        public static final int TORCH_UNAVAILABLE = 0;
        public static final int TORCH_UNKNOWN = -1;
    }
}
