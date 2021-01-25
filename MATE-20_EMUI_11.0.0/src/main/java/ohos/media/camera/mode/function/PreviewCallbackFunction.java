package ohos.media.camera.mode.function;

import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.device.FrameResult;
import ohos.media.camera.mode.impl.ActionStateCallbackImpl;

public class PreviewCallbackFunction extends ResultHandler {
    private volatile boolean isFirstFramePassed = false;
    private final ActionStateCallbackImpl previewCallback;

    public PreviewCallbackFunction(ActionStateCallbackImpl actionStateCallbackImpl) {
        this.previewCallback = actionStateCallbackImpl;
        this.handlerName = PreviewCallbackFunction.class.getSimpleName();
    }

    @Override // ohos.media.camera.mode.function.ResultHandler
    public void handleResult(FrameConfig frameConfig, FrameResult frameResult) {
        if (!this.isFirstFramePassed) {
            this.previewCallback.onPreview(1, null);
            this.isFirstFramePassed = true;
        }
    }
}
