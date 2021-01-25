package ohos.media.camera.device;

import java.util.List;
import ohos.agp.graphics.Surface;
import ohos.agp.utils.Rect;
import ohos.location.Location;
import ohos.media.camera.device.Camera;
import ohos.media.camera.params.Metadata;
import ohos.media.camera.params.ParameterKey;

public interface FrameConfig {

    public interface Builder {
        Builder addSurface(Surface surface);

        FrameConfig build();

        <T> T get(ParameterKey.Key<T> key);

        @Metadata.AeMode
        int getAeMode();

        Rect getAeRect();

        @Metadata.AeTrigger
        int getAeTrigger();

        @Metadata.AfMode
        int getAfMode();

        Rect getAfRect();

        @Metadata.AfTrigger
        int getAfTrigger();

        Surface getCoordinateSurface();

        int getFaceDetectionType();

        @Metadata.FlashMode
        int getFlashMode();

        @Camera.FrameConfigType
        int getFrameConfigType();

        int getImageRotation();

        List<ParameterKey.Key<?>> getKeys();

        Location getLocation();

        List<Surface> getSurfaces();

        float getZoomValue();

        Builder removeSurface(Surface surface);

        Builder setAeMode(@Metadata.AeMode int i, Rect rect);

        Builder setAeTrigger(@Metadata.AeTrigger int i);

        Builder setAfMode(@Metadata.AfMode int i, Rect rect);

        Builder setAfTrigger(@Metadata.AfTrigger int i);

        Builder setCoordinateSurface(Surface surface);

        Builder setFaceDetection(int i, boolean z);

        Builder setFlashMode(@Metadata.FlashMode int i);

        Builder setImageRotation(int i);

        Builder setLocation(Location location);

        Builder setMark(Object obj);

        <T> Builder setParameter(ParameterKey.Key<T> key, T t);

        Builder setZoom(float f);
    }

    <T> T get(ParameterKey.Key<T> key);

    @Metadata.AeMode
    int getAeMode();

    Rect getAeRect();

    @Metadata.AeTrigger
    int getAeTrigger();

    @Metadata.AfMode
    int getAfMode();

    Rect getAfRect();

    @Metadata.AfTrigger
    int getAfTrigger();

    Surface getCoordinateSurface();

    int getFaceDetectionType();

    @Metadata.FlashMode
    int getFlashMode();

    @Camera.FrameConfigType
    int getFrameConfigType();

    int getImageRotation();

    List<ParameterKey.Key<?>> getKeys();

    Location getLocation();

    Object getMark();

    List<Surface> getSurfaces();

    float getZoomValue();
}
