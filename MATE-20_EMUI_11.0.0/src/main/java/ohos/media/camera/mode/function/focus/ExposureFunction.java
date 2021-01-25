package ohos.media.camera.mode.function.focus;

import ohos.agp.utils.Rect;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.device.FrameResult;
import ohos.media.camera.mode.controller.CameraController;
import ohos.media.camera.mode.function.CaptureCallbackManagerWrapper;
import ohos.media.camera.mode.function.PreCaptureManager;
import ohos.media.camera.mode.function.Promise;
import ohos.media.camera.mode.function.ResultHandler;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ExposureFunction extends PreCaptureManager.PreCaptureHandler {
    private static final int AE_TRIGGER_TIMEOUT = 2000;
    private static final int CAF_DELAY = 3000;
    private static final int INVALID_VALUE = -1;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ExposureFunction.class);
    private final CameraController cameraController;
    private final CaptureCallbackManagerWrapper captureCallbackManagerWrapper;
    private final EventHandler handler;
    private boolean isTriggerResponded;
    private int lastAeMode = -1;
    private int lastAeState = -1;
    private int lastFlashMode = -1;
    private int lastPreviewTriggerId = -1;
    private int lastTriggerId = -1;
    private byte meteringMode = 0;
    private AeState preCaptureAeState = AeState.STATE_WAITING_HANDLE_CAPTURE;
    private Promise preCapturePromise;

    /* access modifiers changed from: private */
    public enum AeState {
        STATE_WAITING_HANDLE_CAPTURE,
        STATE_WAITING_STATE_CONVERGED,
        STATE_WAITING_STATE_NON_PRECAPTURE
    }

    @Override // ohos.media.camera.mode.function.PreCaptureManager.PreCaptureHandler
    public void cancel() {
    }

    @Override // ohos.media.camera.mode.function.PreCaptureManager.PreCaptureHandler
    public void stop() {
    }

    public ExposureFunction(CameraController cameraController2, EventHandler eventHandler, CaptureCallbackManagerWrapper captureCallbackManagerWrapper2) {
        this.cameraController = cameraController2;
        this.handler = eventHandler;
        this.captureCallbackManagerWrapper = captureCallbackManagerWrapper2;
        this.captureCallbackManagerWrapper.addResultHandler(new ExposureResultHandler());
    }

    public void captureFocus(Promise promise) {
        if (promise == null) {
            LOGGER.error("captureFocus: null promise!", new Object[0]);
            return;
        }
        LOGGER.debug("captureFocus, preCaptureAeState %{public}s", this.preCaptureAeState);
        if (this.preCaptureAeState == AeState.STATE_WAITING_HANDLE_CAPTURE) {
            if (!shouldDoAeTrigger(this.lastAeState, this.lastAeMode, this.lastFlashMode)) {
                promise.done();
                return;
            }
            sendAeTrigger();
            this.preCapturePromise = promise;
            this.handler.postTask(new Runnable() {
                /* class ohos.media.camera.mode.function.focus.$$Lambda$ExposureFunction$sTxnVotjz1nCDRrM47w8RYlneg0 */

                @Override // java.lang.Runnable
                public final void run() {
                    ExposureFunction.this.lambda$captureFocus$0$ExposureFunction();
                }
            }, 2000);
        }
    }

    public /* synthetic */ void lambda$captureFocus$0$ExposureFunction() {
        LOGGER.info("preCaptureTimeout run", new Object[0]);
        finishPreCapture();
        this.preCaptureAeState = AeState.STATE_WAITING_HANDLE_CAPTURE;
    }

    public void setCafFocus(Rect rect) {
        FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
        if (frameConfigBuilder != null) {
            frameConfigBuilder.setAeMode(1, rect);
            frameConfigBuilder.setAeTrigger(1);
            updatePreview();
        }
    }

    public void setTafFocus(Boolean bool, Rect rect) {
        FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
        if (frameConfigBuilder != null) {
            frameConfigBuilder.setAeMode(0, rect);
            frameConfigBuilder.setAeTrigger(1);
            updatePreview();
            postCafDelay(bool, rect);
        }
    }

    public void setMeteringMode(byte b) {
        this.meteringMode = b;
        FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
        if (frameConfigBuilder != null) {
            frameConfigBuilder.setParameter(InnerParameterKey.METERING_MODE, Byte.valueOf(b));
            updatePreview();
        }
    }

    public void setAeLock(boolean z) {
        FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
        if (frameConfigBuilder != null) {
            frameConfigBuilder.setParameter(InnerParameterKey.AE_LOCK, Boolean.valueOf(z));
            updatePreview();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldDoAeTrigger(int i, int i2, int i3) {
        boolean z = i2 == 1 && (i3 == 0 || i3 == 2);
        boolean z2 = z || i != 2;
        if (i2 == 0 && i == 0) {
            z2 = false;
        }
        LOGGER.debug("shouldDoAeTrigger, isFlashOn %{public}b aeState %{public}d aeMode %{public}d shouldDoAeTrigger %{public}b", Boolean.valueOf(z), Integer.valueOf(i), Integer.valueOf(i2), Boolean.valueOf(z2));
        return z2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAeTrigger() {
        FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
        if (frameConfigBuilder != null) {
            frameConfigBuilder.setParameter(InnerParameterKey.AE_PRECAPTURE_TRIGGER, 1);
            this.lastPreviewTriggerId = updatePreview();
            this.isTriggerResponded = false;
            this.preCaptureAeState = AeState.STATE_WAITING_STATE_CONVERGED;
            LOGGER.debug("start waiting converged. trigger sequence id: %{public}d", Integer.valueOf(this.lastPreviewTriggerId));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finishPreCapture() {
        Promise promise = this.preCapturePromise;
        if (promise != null) {
            promise.done();
            this.preCapturePromise = null;
        }
    }

    private void postCafDelay(Boolean bool, Rect rect) {
        if (bool.booleanValue()) {
            this.handler.postTask(new Runnable(rect) {
                /* class ohos.media.camera.mode.function.focus.$$Lambda$ExposureFunction$g0sa5pizyaV0Qqxv3U5AQlatD84 */
                private final /* synthetic */ Rect f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ExposureFunction.this.lambda$postCafDelay$1$ExposureFunction(this.f$1);
                }
            }, 3000);
        }
    }

    public /* synthetic */ void lambda$postCafDelay$1$ExposureFunction(Rect rect) {
        LOGGER.debug("cafDelay setCafFocus", new Object[0]);
        setCafFocus(rect);
    }

    private int updatePreview() {
        FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
        if (frameConfigBuilder == null) {
            return -1;
        }
        FrameConfig build = frameConfigBuilder.build();
        boolean isShouldCaptureOnce = isShouldCaptureOnce(build, frameConfigBuilder);
        int previewRequest = this.cameraController.setPreviewRequest(frameConfigBuilder, this.captureCallbackManagerWrapper.getCaptureCallbackManager(), null);
        if (!isShouldCaptureOnce) {
            return previewRequest;
        }
        LOGGER.begin("doCapture");
        int capture = this.cameraController.capture(build, this.captureCallbackManagerWrapper.getCaptureCallbackManager(), null);
        LOGGER.end("doCapture");
        return capture;
    }

    private boolean isShouldCaptureOnce(FrameConfig frameConfig, FrameConfig.Builder builder) {
        if (!hasAeTrigger(frameConfig)) {
            return false;
        }
        builder.setAfTrigger(0);
        return true;
    }

    private boolean hasAeTrigger(FrameConfig frameConfig) {
        int afTrigger = frameConfig.getAfTrigger();
        return afTrigger == 1 || afTrigger == 2;
    }

    @Override // ohos.media.camera.mode.function.PreCaptureManager.PreCaptureHandler
    public void handle(Promise promise) {
        captureFocus(promise);
    }

    private class ExposureResultHandler extends ResultHandler {
        ExposureResultHandler() {
            this.handlerName = ExposureResultHandler.class.getSimpleName();
        }

        @Override // ohos.media.camera.mode.function.ResultHandler
        public void handleResult(FrameConfig frameConfig, FrameResult frameResult) {
            processFrame(frameConfig, frameResult);
        }

        private void processFrame(FrameConfig frameConfig, FrameResult frameResult) {
            ExposureFunction.this.lastFlashMode = frameConfig.getFlashMode();
            ExposureFunction.this.lastAeMode = frameConfig.getAeMode();
            int state = frameResult.getAeResult().getState();
            if (!(ExposureFunction.this.lastAeState == state && ExposureFunction.this.lastTriggerId == frameResult.getCaptureTriggerId())) {
                ExposureFunction.this.lastAeState = state;
                ExposureFunction.this.lastTriggerId = frameResult.getCaptureTriggerId();
                ExposureFunction.LOGGER.debug("processFrame ae_state: %{public}d, sequenceId: %{public}d", Integer.valueOf(ExposureFunction.this.lastAeState), Integer.valueOf(ExposureFunction.this.lastTriggerId));
            }
            if (ExposureFunction.this.preCapturePromise != null) {
                if (frameResult.getCaptureTriggerId() >= ExposureFunction.this.lastPreviewTriggerId) {
                    ExposureFunction.this.isTriggerResponded = true;
                }
                processPreCapture(state, frameResult.getCaptureTriggerId());
            }
        }

        private void processPreCapture(int i, int i2) {
            int i3 = AnonymousClass1.$SwitchMap$ohos$media$camera$mode$function$focus$ExposureFunction$AeState[ExposureFunction.this.preCaptureAeState.ordinal()];
            if (i3 == 1) {
                ExposureFunction exposureFunction = ExposureFunction.this;
                if (!exposureFunction.shouldDoAeTrigger(i, exposureFunction.lastAeMode, ExposureFunction.this.lastFlashMode)) {
                    ExposureFunction.this.finishPreCapture();
                } else {
                    ExposureFunction.this.sendAeTrigger();
                }
            } else if (i3 != 2) {
                if (i3 == 3 && i != 4) {
                    ExposureFunction.this.preCaptureAeState = AeState.STATE_WAITING_HANDLE_CAPTURE;
                    ExposureFunction.LOGGER.debug("GOT NONE PRECAPTURE, finsh precapture.", new Object[0]);
                    ExposureFunction.this.finishPreCapture();
                }
            } else if (!ExposureFunction.this.isTriggerResponded) {
                ExposureFunction.LOGGER.debug("ignored state, current sequence id: %{public}d is smaller than last trigger sequence id: %{public}d", Integer.valueOf(i2), Integer.valueOf(ExposureFunction.this.lastPreviewTriggerId));
            } else if (i == 4) {
                ExposureFunction.this.preCaptureAeState = AeState.STATE_WAITING_STATE_NON_PRECAPTURE;
                ExposureFunction.LOGGER.debug("GOT PRECAPTURE, start waiting none precapture.", new Object[0]);
            } else if (i == 2 || i == 3) {
                ExposureFunction.this.preCaptureAeState = AeState.STATE_WAITING_HANDLE_CAPTURE;
                ExposureFunction.LOGGER.debug("GOT CONVERGED, finish precapture.", new Object[0]);
                ExposureFunction.this.finishPreCapture();
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.media.camera.mode.function.focus.ExposureFunction$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$media$camera$mode$function$focus$ExposureFunction$AeState = new int[AeState.values().length];

        static {
            try {
                $SwitchMap$ohos$media$camera$mode$function$focus$ExposureFunction$AeState[AeState.STATE_WAITING_HANDLE_CAPTURE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$media$camera$mode$function$focus$ExposureFunction$AeState[AeState.STATE_WAITING_STATE_CONVERGED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$media$camera$mode$function$focus$ExposureFunction$AeState[AeState.STATE_WAITING_STATE_NON_PRECAPTURE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }
}
