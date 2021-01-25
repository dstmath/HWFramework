package ohos.media.camera.mode.function.face;

import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.device.FrameResult;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.function.ResultHandler;
import ohos.media.camera.mode.impl.ActionStateCallbackImpl;
import ohos.media.camera.mode.tags.BaseModeTags;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class FaceDetectionFunction extends ResultHandler {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(FaceDetectionFunction.class);
    private CameraAbilityImpl cameraAbility;
    private ActionStateCallbackImpl faceDetectionCallback;
    private final boolean isFaceDetectionOn;
    private final boolean isRealFaceDetectionOn;
    private final boolean isRealSmileDetectionOn;
    private final boolean isSmileDetectionOn;
    private BaseModeTags modeTags;
    private Size previewSize;

    public FaceDetectionFunction(boolean z, boolean z2, Size size, BaseModeTags baseModeTags, CameraAbilityImpl cameraAbilityImpl) {
        this.isFaceDetectionOn = z;
        this.isSmileDetectionOn = z2;
        this.isRealFaceDetectionOn = this.isFaceDetectionOn || this.isSmileDetectionOn;
        this.isRealSmileDetectionOn = this.isSmileDetectionOn;
        this.previewSize = size;
        this.modeTags = baseModeTags;
        this.cameraAbility = cameraAbilityImpl;
        this.handlerName = "FaceDetectionFunction";
    }

    @Override // ohos.media.camera.mode.function.ResultHandler
    public void handleResult(FrameConfig frameConfig, FrameResult frameResult) {
        if (frameResult == null) {
            LOGGER.error("handleResult: null handler!", new Object[0]);
            return;
        }
        LOGGER.begin("face detection result process");
        if (this.isRealFaceDetectionOn) {
            if (this.isFaceDetectionOn) {
                this.faceDetectionCallback.onFaceDetection(1, frameResult.getFaceDetectionResult());
            }
            handleSmileResult(frameResult);
            LOGGER.end("face detection result process");
        }
    }

    private void handleSmileResult(FrameResult frameResult) {
        if (this.isRealSmileDetectionOn && this.isSmileDetectionOn) {
            this.faceDetectionCallback.onFaceDetection(2, frameResult.getFaceDetectionResult());
        }
    }

    public void setFaceDetectionCallback(ActionStateCallbackImpl actionStateCallbackImpl) {
        this.faceDetectionCallback = actionStateCallbackImpl;
    }
}
