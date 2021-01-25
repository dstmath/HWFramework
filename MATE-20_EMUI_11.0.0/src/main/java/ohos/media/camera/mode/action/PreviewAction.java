package ohos.media.camera.mode.action;

import java.util.List;
import ohos.agp.graphics.Surface;
import ohos.agp.utils.Point;
import ohos.media.camera.device.FrameStateCallback;

public interface PreviewAction extends Action {
    void autoFocus(Point point);

    void setCoordinateSurface(Surface surface);

    void setSurfaces(List<Surface> list);

    void startPreview(FrameStateCallback frameStateCallback, int i, Surface surface);

    void stopPreview();

    int updatePreview();
}
