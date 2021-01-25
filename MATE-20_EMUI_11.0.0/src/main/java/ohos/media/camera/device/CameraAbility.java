package ohos.media.camera.device;

import java.util.List;
import ohos.media.camera.params.Metadata;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.ResultKey;
import ohos.media.image.common.Size;

public interface CameraAbility {
    <T> List<T> getParameterRange(ParameterKey.Key<T> key);

    <T> T getPropertyValue(PropertyKey.Key<T> key);

    @Metadata.AeMode
    int[] getSupportedAeMode();

    @Metadata.AfMode
    int[] getSupportedAfMode();

    int[] getSupportedFaceDetection();

    @Metadata.FlashMode
    int[] getSupportedFlashMode();

    List<ParameterKey.Key<?>> getSupportedParameters();

    List<PropertyKey.Key<?>> getSupportedProperties();

    List<ResultKey.Key<?>> getSupportedResults();

    List<Size> getSupportedSizes(int i);

    <T> List<Size> getSupportedSizes(Class<T> cls);

    float[] getSupportedZoom();
}
