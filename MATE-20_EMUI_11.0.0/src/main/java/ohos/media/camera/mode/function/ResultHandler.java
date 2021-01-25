package ohos.media.camera.mode.function;

import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.device.FrameResult;

public abstract class ResultHandler {
    protected String handlerName;

    public void handleError(int i, FrameResult frameResult) {
    }

    public void handleResult(FrameConfig frameConfig, FrameResult frameResult) {
    }

    public String getName() {
        return this.handlerName;
    }
}
