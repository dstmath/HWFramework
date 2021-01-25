package ohos.media.camera.device;

public abstract class CameraDeviceCallback {

    public @interface CameraStatus {
        public static final int CAMERA_DEVICE_AVAILABLE = 1;
        public static final int CAMERA_DEVICE_UNAVAILABLE = 0;
    }

    public @interface FlashlightStatus {
        public static final int FLASHLIGHT_OFF = 1;
        public static final int FLASHLIGHT_OPEN = 2;
        public static final int FLASHLIGHT_UNAVAILABLE = 0;
    }

    public void onCameraStatus(String str, @CameraStatus int i) {
    }

    public void onFlashlightStatus(String str, @FlashlightStatus int i) {
    }

    public void onPhysicalCameraStatus(String str, @CameraStatus int i) {
    }
}
