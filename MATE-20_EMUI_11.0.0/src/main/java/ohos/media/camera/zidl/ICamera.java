package ohos.media.camera.zidl;

import java.util.List;
import java.util.Map;
import ohos.media.camera.exception.AccessException;
import ohos.media.camera.params.ParameterKey;

public interface ICamera {
    public static final int FRAME_CONFIG_PICTURE = 2;
    public static final int FRAME_CONFIG_PREVIEW = 1;
    public static final int FRAME_CONFIG_RECORD = 3;
    public static final int INVALID_STREAM_ID = -1;
    public static final long NO_FRAME_NUMBER = -1;

    void beginConfig() throws AccessException;

    long cancelCaptureFrames(int i) throws AccessException;

    CaptureTriggerInfo captureFrames(List<FrameConfigNative> list, boolean z) throws AccessException;

    int createOutput(StreamConfiguration streamConfiguration) throws AccessException;

    void deleteOutput(int i) throws AccessException;

    void endConfig() throws AccessException;

    long flush() throws AccessException;

    Map<ParameterKey.Key<?>, Object> getDefaultFrameConfigParameters(int i) throws AccessException;

    void release() throws AccessException;

    void waitIdle() throws AccessException;
}
