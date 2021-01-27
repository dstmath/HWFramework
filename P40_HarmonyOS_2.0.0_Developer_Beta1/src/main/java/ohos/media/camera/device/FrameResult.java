package ohos.media.camera.device;

import java.util.List;
import ohos.agp.graphics.Surface;
import ohos.media.camera.params.AeResult;
import ohos.media.camera.params.AfResult;
import ohos.media.camera.params.FaceDetectionResult;
import ohos.media.camera.params.ParametersResult;

public interface FrameResult {

    public @interface State {
        public static final int ERROR_BUFFER_DROPPED = -4;
        public static final int ERROR_FLUSHED = -2;
        public static final int ERROR_FRAME_CONFIG = -3;
        public static final int ERROR_FRAME_RESULT = -5;
        public static final int ERROR_UNKNOWN = -1;
        public static final int FRAME_FULL_RESULT = 1;
        public static final int FRAME_PARTIAL_RESULT = 2;
    }

    AeResult getAeResult();

    AfResult getAfResult();

    int getCaptureTriggerId();

    List<Surface> getDroppedBufferOwners();

    FaceDetectionResult getFaceDetectionResult();

    FrameConfig getFrameConfig();

    long getFrameNumber();

    ParametersResult getParametersResult();

    @State
    int getState();

    long getTimestamp();

    boolean isFullResult();
}
