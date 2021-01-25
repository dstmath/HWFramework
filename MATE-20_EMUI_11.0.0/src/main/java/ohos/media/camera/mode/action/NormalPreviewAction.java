package ohos.media.camera.mode.action;

import java.util.ArrayList;
import java.util.List;
import ohos.agp.graphics.Surface;
import ohos.agp.utils.Point;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.device.FrameStateCallback;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.controller.CameraController;
import ohos.media.camera.mode.tags.BaseModeTags;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class NormalPreviewAction implements PreviewAction {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(NormalPreviewAction.class);
    private static final int MAX_PREVIEW_SURFACE_NUMBER = 2;
    protected CameraController controller;
    private Surface coordinateSurface;
    protected FrameStateCallback frameStateCallback;
    protected BaseModeTags modeTags;
    protected FrameConfig.Builder previewRequest;
    private List<Surface> previewSurfaces = new ArrayList();

    @Override // ohos.media.camera.mode.action.PreviewAction
    public void autoFocus(Point point) {
    }

    public NormalPreviewAction(CameraController cameraController, BaseModeTags baseModeTags, CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.debug("NormalPreviewAction: ", new Object[0]);
        this.controller = cameraController;
        this.modeTags = baseModeTags;
    }

    @Override // ohos.media.camera.mode.action.PreviewAction
    public void startPreview(FrameStateCallback frameStateCallback2, int i, Surface surface) {
        int i2 = 0;
        LOGGER.debug("startPreview: ", new Object[0]);
        this.frameStateCallback = frameStateCallback2;
        this.previewRequest = this.controller.createCaptureRequest(i);
        if (this.previewRequest == null) {
            LOGGER.warn("create previewRequest failed", new Object[0]);
            return;
        }
        for (Surface surface2 : this.previewSurfaces) {
            if (surface2 == null || i2 >= 2) {
                throw new IllegalArgumentException("Invalid surface or exceed MAX_PREVIEW_SURFACE_NUMBER: 2");
            }
            this.previewRequest.addSurface(surface2);
            i2++;
        }
        if (surface != null) {
            this.previewRequest.addSurface(surface);
        }
        this.previewRequest.setCoordinateSurface(this.coordinateSurface);
        this.modeTags.enablePreview().applyToBuilder(this.previewRequest);
        startPreviewInternal(this.previewRequest, this.frameStateCallback, null);
    }

    /* access modifiers changed from: protected */
    public void startPreviewInternal(FrameConfig.Builder builder, FrameStateCallback frameStateCallback2, EventHandler eventHandler) {
        this.controller.setRepeatingRequest(this.previewRequest, this.frameStateCallback, null);
    }

    @Override // ohos.media.camera.mode.action.PreviewAction
    public void stopPreview() {
        this.controller.stopRepeating();
    }

    @Override // ohos.media.camera.mode.action.Action
    public List<Surface> getSurfaces() {
        LOGGER.debug("getSurfaces: ", new Object[0]);
        return this.previewSurfaces;
    }

    @Override // ohos.media.camera.mode.action.PreviewAction
    public void setSurfaces(List<Surface> list) {
        LOGGER.debug("set preview surfaces", new Object[0]);
        this.previewSurfaces = list;
    }

    @Override // ohos.media.camera.mode.action.Action
    public Surface getSurface() {
        return this.previewSurfaces.get(0);
    }

    @Override // ohos.media.camera.mode.action.Action
    @Deprecated
    public void setSurface(Surface surface) {
        this.previewSurfaces.add(surface);
    }

    @Override // ohos.media.camera.mode.action.PreviewAction
    public void setCoordinateSurface(Surface surface) {
        LOGGER.debug("setCoordinateSurface: %{public}s", surface);
        this.coordinateSurface = surface;
    }

    @Override // ohos.media.camera.mode.action.PreviewAction
    public int updatePreview() {
        LOGGER.debug("updatePreview", new Object[0]);
        if (this.previewRequest == null) {
            return -1;
        }
        this.modeTags.enablePreview().applyToBuilder(this.previewRequest);
        return this.controller.setRepeatingRequest(this.previewRequest, this.frameStateCallback, null);
    }
}
