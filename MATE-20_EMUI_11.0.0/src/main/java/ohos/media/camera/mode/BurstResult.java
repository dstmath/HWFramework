package ohos.media.camera.mode;

public interface BurstResult {

    public @interface State {
        public static final int BURST_ALL_FILE_SAVED = 4;
        public static final int BURST_COMPLETED = 3;
        public static final int BURST_ONCE = 2;
        public static final int BURST_STARTED = 1;
        public static final int ERROR_BURST_NOT_READY = -3;
        public static final int ERROR_FILE_IO = -2;
        public static final int ERROR_UNKNOWN = -1;
    }

    @State
    int getState();
}
