package ohos.media.camera.params;

public interface AeResult {

    public @interface State {
        public static final int AE_STATE_AUTO_SCAN = 1;
        public static final int AE_STATE_CONVERGED = 2;
        public static final int AE_STATE_FLASH_REQUIRED = 3;
        public static final int AE_STATE_INACTIVE = 0;
        public static final int AE_STATE_TRIGGER_SCAN = 4;
    }

    @State
    int getState();
}
