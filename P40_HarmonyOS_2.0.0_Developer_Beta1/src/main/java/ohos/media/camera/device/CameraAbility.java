package ohos.media.camera.device;

import java.util.List;
import ohos.media.camera.params.Metadata;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.ResultKey;
import ohos.media.image.common.Size;
import ohos.utils.Scope;

public interface CameraAbility {

    public @interface CameraRunningMode {
        public static final int CAMERA_RUNNING_MODE_HIGH_FRAME_RATE = 1;
        public static final int CAMERA_RUNNING_MODE_NORMAL = 0;
    }

    long getMinCaptureDuration(int i, Size size);

    <T> long getMinCaptureDuration(Class<T> cls, Size size);

    <T> List<T> getParameterRange(ParameterKey.Key<T> key);

    <T> T getPropertyValue(PropertyKey.Key<T> key);

    @Metadata.AeMode
    int[] getSupportedAeMode();

    @Metadata.AfMode
    int[] getSupportedAfMode();

    int[] getSupportedFaceDetection();

    @Metadata.FlashMode
    int[] getSupportedFlashMode();

    List<Integer> getSupportedFormats();

    List<Scope<Integer>> getSupportedHighFrameRate(int i, Size size);

    <T> List<Scope<Integer>> getSupportedHighFrameRate(Class<T> cls, Size size);

    List<Size> getSupportedHighSizes(int i);

    List<ParameterKey.Key<?>> getSupportedParameters();

    List<PropertyKey.Key<?>> getSupportedProperties();

    List<ResultKey.Key<?>> getSupportedResults();

    @CameraRunningMode
    int[] getSupportedRunningModes();

    List<Size> getSupportedSizes(int i);

    <T> List<Size> getSupportedSizes(Class<T> cls);

    float[] getSupportedZoom();
}
