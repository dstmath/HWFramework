package ohos.media.camera.device;

import ohos.agp.graphics.Surface;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.CameraAbility;
import ohos.media.image.common.Size;

public interface CameraConfig {

    public interface Builder {
        Builder addDeferredSurface(Surface surface);

        <T> Builder addDeferredSurfaceSize(Size size, Class<T> cls);

        Builder addSurface(Surface surface);

        CameraConfig build();

        Builder removeSurface(Surface surface);

        Builder setFrameStateCallback(FrameStateCallback frameStateCallback, EventHandler eventHandler);

        Builder setRunningMode(@CameraAbility.CameraRunningMode int i);
    }
}
