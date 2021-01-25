package ohos.media.camera.mode.action;

import java.util.List;
import ohos.agp.graphics.Surface;

public interface Action {
    Surface getSurface();

    List<Surface> getSurfaces();

    void setSurface(Surface surface);
}
