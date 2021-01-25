package ohos.media.camera.mode;

public interface TakePictureResult {

    public @interface State {
        public static final int CAPTURE_COMPLETED = 5;
        public static final int CAPTURE_EXPOSURE_BEGIN = 2;
        public static final int CAPTURE_EXPOSURE_END = 3;
        public static final int CAPTURE_FILE_SAVED = 6;
        public static final int CAPTURE_SHUTTER = 4;
        public static final int CAPTURE_STARTED = 1;
        public static final int ERROR_CAPTURE_NOT_READY = -3;
        public static final int ERROR_FILE_IO = -2;
        public static final int ERROR_UNKNOWN = -1;
    }

    int getExposureTime();

    @State
    int getState();
}
