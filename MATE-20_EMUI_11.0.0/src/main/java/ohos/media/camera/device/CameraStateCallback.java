package ohos.media.camera.device;

public abstract class CameraStateCallback {

    public @interface ErrorCode {
        public static final int ERROR_CAMERA_ALREADY_IN_USE = -2;
        public static final int ERROR_CAMERA_DEVICE_DISABLED = -4;
        public static final int ERROR_CAMERA_DEVICE_FATAL = -5;
        public static final int ERROR_CAMERA_RESOURCE_LIMITED = -3;
        public static final int ERROR_CAMERA_SERVICE_FATAL = -6;
        public static final int ERROR_CAMERA_UNKNOWN = -1;
    }

    public void onConfigureFailed(Camera camera, @ErrorCode int i) {
    }

    public void onConfigured(Camera camera) {
    }

    public void onCreateFailed(String str, @ErrorCode int i) {
    }

    public void onCreated(Camera camera) {
    }

    public void onFatalError(Camera camera, @ErrorCode int i) {
    }

    public void onReleased(Camera camera) {
    }
}
