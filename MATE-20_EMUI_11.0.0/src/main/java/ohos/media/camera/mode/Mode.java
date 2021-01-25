package ohos.media.camera.mode;

import java.io.File;
import java.util.Map;
import ohos.agp.utils.Rect;
import ohos.location.Location;
import ohos.media.camera.mode.ModeConfig;
import ohos.media.camera.params.Metadata;
import ohos.media.camera.params.ParameterKey;

public interface Mode {

    public @interface ResultCode {
        public static final int RESULT_CONFLICTED = -5;
        public static final int RESULT_FAILED_UNKNOWN = -1;
        public static final int RESULT_ILLEGAL_ARGUMENT = -3;
        public static final int RESULT_ILLEGAL_STATE = -4;
        public static final int RESULT_SUCCESS = 0;
        public static final int RESULT_UNSUPPORTED_OPERATION = -2;
    }

    public @interface Type {
        public static final int BOKEH_MODE = 2;
        public static final int HDR_MODE = 3;
        public static final int INVALID_MODE = 0;
        public static final int NORMAL_MODE = 1;
        public static final int PORTRAIT_MODE = 4;
        public static final int PRO_PHOTO_MODE = 9;
        public static final int PRO_VIDEO_MODE = 10;
        public static final int SLOW_MOTION_MODE = 8;
        public static final int SUPER_NIGHT_MODE = 6;
        public static final int SUPER_SLOW_MOTION = 7;
        public static final int VIDEO_MODE = 5;
    }

    void configure(ModeConfig modeConfig);

    String getCameraId();

    ModeAbility getModeAbility();

    ModeConfig.Builder getModeConfigBuilder();

    int getType();

    void pauseRecording();

    void release();

    void resumeRecording();

    int setBeauty(@Metadata.BeautyType int i, int i2);

    int setColorMode(@Metadata.ColorType int i);

    int setFaceDetection(int i, boolean z);

    int setFlashMode(@Metadata.FlashMode int i);

    int setFocus(@Metadata.FocusMode int i, Rect rect);

    int setImageRotation(int i);

    int setLocation(Location location);

    <T> int setParameter(ParameterKey.Key<T> key, T t);

    <T> int setParameters(Map<ParameterKey.Key<T>, T> map);

    int setSceneDetection(boolean z);

    int setZoom(float f);

    void startPreview();

    void startRecording();

    void startRecording(File file);

    void stopPicture();

    void stopPreview();

    void stopRecording();

    void takePicture();

    void takePicture(File file);

    void takePictureBurst();

    void takePictureBurst(File file);
}
