package ohos.media.camera.zidl;

public interface ICameraCallback {

    public @interface ErrorCode {
        public static final int ERROR_CAMERA_ALREADY_IN_USE = -2;
        public static final int ERROR_CAMERA_DEVICE_DISABLED = -4;
        public static final int ERROR_CAMERA_DEVICE_FATAL = -5;
        public static final int ERROR_CAMERA_RESOURCE_LIMITED = -3;
        public static final int ERROR_CAMERA_SERVICE_FATAL = -6;
        public static final int ERROR_CAMERA_UNKNOWN = -1;
        public static final int ERROR_FRAME_BUFFER = -7;
        public static final int ERROR_FRAME_CONFIG = -8;
        public static final int ERROR_FRAME_RESULT = -9;
    }

    void onBufferAllocated(int i);

    void onCameraError(int i);

    void onCaptureTriggerCompleted(int i, long j);

    void onCaptureTriggerInterrupted(int i);

    void onCaptureTriggerStarted(int i, long j);

    void onFrameCompleted(FrameResultNative frameResultNative);

    void onFrameError(FrameResultNative frameResultNative, int i);

    void onFrameProgressed(FrameResultNative frameResultNative);

    void onFrameStarted(FrameResultNative frameResultNative);
}
