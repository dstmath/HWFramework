package ohos.media.camera.mode.action;

import java.util.ArrayList;
import java.util.List;
import ohos.agp.graphics.Surface;
import ohos.media.camera.device.Camera;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.device.FrameResult;
import ohos.media.camera.device.FrameStateCallback;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.controller.CameraController;
import ohos.media.camera.mode.impl.ActionDataCallbackImpl;
import ohos.media.camera.mode.impl.ActionStateCallbackImpl;
import ohos.media.camera.mode.tags.BaseModeTags;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class NormalRecordAction implements RecordAction {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(NormalRecordAction.class);
    private static final int MAX_SURFACE_NUMBER = 3;
    protected CameraController controller;
    protected FrameStateCallback frameStateCallback = new FrameStateCallback() {
        /* class ohos.media.camera.mode.action.NormalRecordAction.AnonymousClass1 */

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onFrameProgressed(Camera camera, FrameConfig frameConfig, FrameResult frameResult) {
            super.onFrameProgressed(camera, frameConfig, frameResult);
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onFrameFinished(Camera camera, FrameConfig frameConfig, FrameResult frameResult) {
            super.onFrameFinished(camera, frameConfig, frameResult);
        }
    };
    protected Surface recordSurface;
    private final List<Surface> surfaces = new ArrayList(3);
    protected FrameConfig.Builder videoFrameConfigBuilder = null;

    @Override // ohos.media.camera.mode.action.RecordAction
    public void capture(String str, ActionStateCallbackImpl actionStateCallbackImpl) {
    }

    @Override // ohos.media.camera.mode.action.RecordAction
    public void capture(ActionDataCallbackImpl actionDataCallbackImpl, ActionStateCallbackImpl actionStateCallbackImpl, int i) {
    }

    @Override // ohos.media.camera.mode.action.RecordAction
    public void pause() {
    }

    @Override // ohos.media.camera.mode.action.RecordAction
    public void resume() {
    }

    public NormalRecordAction(CameraController cameraController, BaseModeTags baseModeTags, CameraAbilityImpl cameraAbilityImpl) {
        this.controller = cameraController;
    }

    @Override // ohos.media.camera.mode.action.RecordAction
    public void start(String str, ActionStateCallbackImpl actionStateCallbackImpl) {
        LOGGER.debug("start record", new Object[0]);
        this.videoFrameConfigBuilder = this.controller.getFrameConfigBuilder();
        this.videoFrameConfigBuilder.addSurface(this.recordSurface);
        this.controller.setRepeatingRequest(this.videoFrameConfigBuilder, this.frameStateCallback, null);
    }

    @Override // ohos.media.camera.mode.action.RecordAction
    public void stop() {
        this.videoFrameConfigBuilder.removeSurface(this.recordSurface);
        this.controller.setRepeatingRequest(this.videoFrameConfigBuilder, this.frameStateCallback, null);
    }

    @Override // ohos.media.camera.mode.action.Action
    public List<Surface> getSurfaces() {
        LOGGER.debug("getSurfaces: %{public}s", this.surfaces);
        return this.surfaces;
    }

    @Override // ohos.media.camera.mode.action.Action
    public Surface getSurface() {
        return this.recordSurface;
    }

    @Override // ohos.media.camera.mode.action.Action
    public void setSurface(Surface surface) {
        LOGGER.debug("set record surface: %{public}s", surface);
        this.recordSurface = surface;
    }
}
