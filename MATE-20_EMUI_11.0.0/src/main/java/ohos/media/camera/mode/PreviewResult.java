package ohos.media.camera.mode;

public interface PreviewResult {

    public @interface State {
        public static final int ERROR_UNKNOWN = -1;
        public static final int PREVIEW_STARTED = 1;
        public static final int PREVIEW_STOPPED = 2;
    }

    @State
    int getState();
}
