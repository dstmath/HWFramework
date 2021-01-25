package ohos.media.camera.mode;

public abstract class ModeStateCallback {

    public @interface ErrorCode {
        public static final int ERROR_MODE_CAMERA_DEVICE = 4;
        public static final int ERROR_MODE_CAMERA_DISABLED = 3;
        public static final int ERROR_MODE_CAMERA_IN_USE = 1;
        public static final int ERROR_MODE_CAMERA_SERVICE = 5;
        public static final int ERROR_MODE_MAX_CAMERAS_IN_USE = 2;
        public static final int ERROR_MODE_MEMORY_LACK = 6;
        public static final int ERROR_MODE_SURFACE_INVALID = 7;
        public static final int ERROR_MODE_UNKNOWN = 0;
    }

    public void onConfigureFailed(Mode mode, int i) {
    }

    public void onConfigured(Mode mode) {
    }

    public void onCreateFailed(String str, int i, int i2) {
    }

    public void onCreated(Mode mode) {
    }

    public void onFatalError(Mode mode, int i) {
    }

    public void onReleased(Mode mode) {
    }
}
