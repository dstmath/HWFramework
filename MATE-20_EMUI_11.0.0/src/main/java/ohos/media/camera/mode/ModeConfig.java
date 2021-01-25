package ohos.media.camera.mode;

import ohos.agp.graphics.Surface;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.params.Metadata;
import ohos.media.image.common.Size;

public interface ModeConfig {

    public interface Builder {
        Builder addCaptureImage(Size size, int i);

        <T> Builder addDeferredPreviewSize(Size size, Class<T> cls);

        Builder addDeferredPreviewSurface(Surface surface);

        Builder addPreviewSurface(Surface surface);

        Builder addVideoSize(Size size);

        Builder addVideoSurface(Surface surface);

        ModeConfig build();

        Builder removeCaptureImage(Size size, int i);

        Builder removePreviewSurface(Surface surface);

        Builder removeVideoSize(Size size);

        Builder removeVideoSurface(Surface surface);

        Builder setDataCallback(ActionDataCallback actionDataCallback, EventHandler eventHandler);

        Builder setStateCallback(ActionStateCallback actionStateCallback, EventHandler eventHandler);

        Builder setVideoFps(@Metadata.FpsRange int i);
    }
}
