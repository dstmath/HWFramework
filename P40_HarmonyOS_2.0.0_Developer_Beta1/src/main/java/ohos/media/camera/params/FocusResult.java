package ohos.media.camera.params;

public interface FocusResult {

    public @interface State {
        public static final int ERROR_UNKNOWN = -1;
        public static final int FOCUS_FAILED = 5;
        public static final int FOCUS_LOCKED = 3;
        public static final int FOCUS_MODE_CHANGED = 1;
        public static final int FOCUS_MOVING = 2;
        public static final int FOCUS_SUCCEED = 4;
    }

    @State
    int getState();
}
