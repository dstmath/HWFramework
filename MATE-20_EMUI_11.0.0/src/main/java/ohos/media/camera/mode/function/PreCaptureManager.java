package ohos.media.camera.mode.function;

import java.util.ArrayList;
import java.util.List;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.mode.ActionStateCallback;
import ohos.media.camera.mode.controller.CameraController;
import ohos.media.camera.mode.function.focus.Camera3aManager;
import ohos.media.camera.mode.tags.BaseModeTags;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class PreCaptureManager {
    private static final int HANDLER_LIST_DEFAULT_LEN = 2;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(PreCaptureManager.class);
    private final CaptureCallbackManagerWrapper callbackManagerWrapper;
    private final Camera3aManager camera3aManager;
    private final CameraController controller;
    private PreCaptureHandler currentHandler;
    private final EventHandler handler;
    private List<PreCaptureHandler> handlers;
    private boolean isActive;
    private BaseModeTags modeTags;

    public static abstract class PreCaptureHandler {
        public void cancel() {
        }

        public abstract void handle(Promise promise);

        public void setActionStateCallback(ActionStateCallback actionStateCallback) {
        }

        public void setRotation(int i) {
        }

        public void stop() {
        }
    }

    public PreCaptureManager(CameraController cameraController, EventHandler eventHandler, CaptureCallbackManagerWrapper captureCallbackManagerWrapper, Camera3aManager camera3aManager2) {
        this.controller = cameraController;
        this.handler = eventHandler;
        this.callbackManagerWrapper = captureCallbackManagerWrapper;
        this.camera3aManager = camera3aManager2;
    }

    public PreCapture getPreCapture(int i) {
        if (i == 0) {
            return getNormalPreCapture();
        }
        if (i == 1) {
            return getVideoPreCapture();
        }
        LOGGER.warn("getPreCapture: invalid preCapture type%{public}d", Integer.valueOf(i));
        return getNormalPreCapture();
    }

    private PreCapture getNormalPreCapture() {
        this.handlers = new ArrayList(2);
        this.handlers.add(this.camera3aManager.getFocusFunction());
        this.handlers.add(this.camera3aManager.getExposureFunction());
        return new NormalPreCapture();
    }

    private PreCapture getVideoPreCapture() {
        this.handlers = new ArrayList(2);
        this.handlers.add(new PreCaptureHandler() {
            /* class ohos.media.camera.mode.function.PreCaptureManager.AnonymousClass1 */

            @Override // ohos.media.camera.mode.function.PreCaptureManager.PreCaptureHandler
            public void handle(Promise promise) {
                if (promise == null) {
                    PreCaptureManager.LOGGER.warn("promise is null", new Object[0]);
                } else {
                    promise.done();
                }
            }
        });
        return new NormalPreCapture();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePreCapture(final Promise promise, final int i) {
        if (!this.isActive) {
            LOGGER.debug("handlePreCapture, is not active", new Object[0]);
            promise.cancel();
        } else if (i < this.handlers.size()) {
            this.currentHandler = this.handlers.get(i);
            LOGGER.debug("handling pre %{public}d %{public}s", Integer.valueOf(i), this.currentHandler.getClass().getSimpleName());
            this.currentHandler.handle(new Promise() {
                /* class ohos.media.camera.mode.function.PreCaptureManager.AnonymousClass2 */

                @Override // ohos.media.camera.mode.function.Promise
                public void done() {
                    PreCaptureManager.this.handlePreCapture(promise, i + 1);
                }

                @Override // ohos.media.camera.mode.function.Promise
                public void cancel() {
                    promise.cancel();
                }
            });
        } else if (i == this.handlers.size()) {
            this.currentHandler = null;
            promise.done();
        } else {
            LOGGER.warn("getSuperNightPreCapture go to the else branch", new Object[0]);
        }
    }

    public void setModeTags(BaseModeTags baseModeTags) {
        this.modeTags = baseModeTags;
    }

    /* access modifiers changed from: private */
    public class NormalPreCapture implements PreCapture {
        @Override // ohos.media.camera.mode.function.PreCapture
        public void setActionStateCallback(ActionStateCallback actionStateCallback) {
        }

        @Override // ohos.media.camera.mode.function.PreCapture
        public void setRotation(int i) {
        }

        @Override // ohos.media.camera.mode.function.PreCapture
        public void stop() {
        }

        private NormalPreCapture() {
        }

        @Override // ohos.media.camera.mode.function.PreCapture
        public void active() {
            PreCaptureManager.this.isActive = true;
        }

        @Override // ohos.media.camera.mode.function.PreCapture
        public void deactive() {
            PreCaptureManager.this.isActive = false;
            if (PreCaptureManager.this.currentHandler != null) {
                PreCaptureManager.this.currentHandler.cancel();
            }
        }

        @Override // ohos.media.camera.mode.function.PreCapture
        public void capture(Promise promise) {
            PreCaptureManager.this.handlePreCapture(promise, 0);
        }
    }
}
