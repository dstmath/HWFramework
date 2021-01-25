package ohos.media.camera.mode.function;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import ohos.media.camera.device.Camera;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.device.FrameResult;
import ohos.media.camera.device.FrameStateCallback;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class CaptureCallbackManagerWrapper {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CaptureCallbackManagerWrapper.class);
    private final CaptureCallbackManager captureCallbackManager = new CaptureCallbackManager();

    public CaptureCallbackManager getCaptureCallbackManager() {
        return this.captureCallbackManager;
    }

    public void addResultHandler(ResultHandler resultHandler) {
        this.captureCallbackManager.addResultHandler(resultHandler);
    }

    public void removeResultHandler(ResultHandler resultHandler) {
        this.captureCallbackManager.removeResultHandler(resultHandler);
    }

    /* access modifiers changed from: private */
    public static class CaptureCallbackManager extends FrameStateCallback {
        private final List<ResultHandler> resultHandlerList;

        private CaptureCallbackManager() {
            this.resultHandlerList = new CopyOnWriteArrayList();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addResultHandler(ResultHandler resultHandler) {
            if (resultHandler == null) {
                CaptureCallbackManagerWrapper.LOGGER.error("addResultHandler: null handler!", new Object[0]);
            } else if (!this.resultHandlerList.contains(resultHandler)) {
                this.resultHandlerList.add(resultHandler);
                CaptureCallbackManagerWrapper.LOGGER.debug("add new result handler: %{public}s", resultHandler.getName());
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void removeResultHandler(ResultHandler resultHandler) {
            if (resultHandler == null) {
                CaptureCallbackManagerWrapper.LOGGER.error("removeResultHandler: null handler!", new Object[0]);
            } else if (this.resultHandlerList.contains(resultHandler)) {
                this.resultHandlerList.remove(resultHandler);
                CaptureCallbackManagerWrapper.LOGGER.debug("remove result handler: %{public}s", resultHandler.getName());
            }
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onFrameStarted(Camera camera, FrameConfig frameConfig, long j, long j2) {
            super.onFrameStarted(camera, frameConfig, j, j2);
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onFrameProgressed(Camera camera, FrameConfig frameConfig, FrameResult frameResult) {
            super.onFrameProgressed(camera, frameConfig, frameResult);
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onFrameFinished(Camera camera, FrameConfig frameConfig, FrameResult frameResult) {
            super.onFrameFinished(camera, frameConfig, frameResult);
            for (ResultHandler resultHandler : this.resultHandlerList) {
                resultHandler.handleResult(frameConfig, frameResult);
            }
        }

        @Override // ohos.media.camera.device.FrameStateCallback
        public void onFrameError(Camera camera, FrameConfig frameConfig, int i, FrameResult frameResult) {
            super.onFrameError(camera, frameConfig, i, frameResult);
            for (ResultHandler resultHandler : this.resultHandlerList) {
                resultHandler.handleError(i, frameResult);
            }
        }
    }
}
