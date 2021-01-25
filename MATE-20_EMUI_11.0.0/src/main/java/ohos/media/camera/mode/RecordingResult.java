package ohos.media.camera.mode;

public interface RecordingResult {

    public @interface State {
        public static final int ERROR_FILE_IO = -2;
        public static final int ERROR_RECORDING_NOT_READY = -3;
        public static final int ERROR_UNKNOWN = -1;
        public static final int RECORDING_COMPLETED = 4;
        public static final int RECORDING_FILE_SAVED = 5;
        public static final int RECORDING_READY = 1;
        public static final int RECORDING_STARTED = 2;
        public static final int RECORDING_STOPPED = 3;
    }

    @State
    int getState();
}
