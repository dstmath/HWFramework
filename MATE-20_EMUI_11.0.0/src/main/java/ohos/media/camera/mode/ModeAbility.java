package ohos.media.camera.mode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import ohos.media.camera.params.Metadata;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.PropertyKey;
import ohos.media.image.common.Size;

public interface ModeAbility {

    public @interface ConflictAction {
        public static final int AI_MOVIE_ACTION = 1;
        public static final int BEAUTY_ACTION = 2;
        public static final int BOKEHSPOT_ACTION = 4;
        public static final int FAIRLIGHT_ACTION = 5;
        public static final int FILTER_EFFECT_ACTION = 3;
        public static final int VIDEO_STABILIZATION_ACTION = 6;
    }

    Set<Integer> getConflictActions();

    int getMaxPreviewSurfaceNumber();

    <T> List<T> getParameterRange(ParameterKey.Key<T> key);

    <T> T getPropertyValue(PropertyKey.Key<T> key);

    @Metadata.FocusMode
    int[] getSupportedAutoFocus();

    int[] getSupportedBeauty(@Metadata.BeautyType int i);

    List<Size> getSupportedCaptureSizes(int i);

    @Metadata.ColorType
    int[] getSupportedColorMode();

    int[] getSupportedFaceDetection();

    @Metadata.FlashMode
    int[] getSupportedFlashMode();

    List<ParameterKey.Key<?>> getSupportedParameters();

    <T> List<Size> getSupportedPreviewSizes(Class<T> cls);

    List<PropertyKey.Key<?>> getSupportedProperties();

    boolean getSupportedSceneDetection();

    <T> Map<Integer, List<Size>> getSupportedVideoSizes(Class<T> cls);

    float[] getSupportedZoom();

    boolean isBurstSupported();

    boolean isCaptureSupported();

    boolean isPreviewSupported();

    boolean isVideoSupported();
}
