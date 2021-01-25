package ohos.media.camera.mode.function.focus;

import java.util.Objects;
import ohos.agp.utils.Rect;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.device.FrameResult;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.ActionStateCallback;
import ohos.media.camera.mode.controller.CameraController;
import ohos.media.camera.mode.function.CaptureCallbackManagerWrapper;
import ohos.media.camera.mode.function.PreCaptureManager;
import ohos.media.camera.mode.function.Promise;
import ohos.media.camera.mode.function.ResultHandler;
import ohos.media.camera.mode.utils.CameraUtil;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class FocusFunction extends PreCaptureManager.PreCaptureHandler {
    private static final int AF_TRIGGER_TIMEOUT = 6000;
    private static final int CAF_DELAY = 3000;
    private static final int INVALID_VALUE = -1;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(FocusFunction.class);
    private static final int UNCHANGED_AF_STATE_PRINT_INTERVAL_MS = 1000;
    private static final int WAIT_FRAME_NUM = 6;
    private ActionStateCallback actionStateCallback;
    private final CameraController cameraController;
    private final CaptureCallbackManagerWrapper captureCallbackManagerWrapper;
    private int focusMode;
    private final EventHandler handler;
    private boolean isAfInMotion;
    private final boolean isCameraAeWithoutAf;
    private boolean isCapturing;
    private boolean isInformCompleted;
    private boolean isTriggerChanged;
    private final boolean isTriggerLockSupported;
    private boolean isTriggerSequenceIdReached;
    private Integer lastAfState;
    private long lastFrameNumber = -1;
    private int lastSequenceId = -1;
    private long lastUnchangedAfStateTime;
    private Promise preCapturePromise;
    private int triggerSequenceId = -1;
    private int waitedFrames;

    @Override // ohos.media.camera.mode.function.PreCaptureManager.PreCaptureHandler
    public void cancel() {
    }

    @Override // ohos.media.camera.mode.function.PreCaptureManager.PreCaptureHandler
    public void stop() {
    }

    static /* synthetic */ int access$608(FocusFunction focusFunction) {
        int i = focusFunction.waitedFrames;
        focusFunction.waitedFrames = i + 1;
        return i;
    }

    public FocusFunction(CameraController cameraController2, EventHandler eventHandler, CameraAbilityImpl cameraAbilityImpl, CaptureCallbackManagerWrapper captureCallbackManagerWrapper2) {
        this.cameraController = cameraController2;
        this.handler = eventHandler;
        this.captureCallbackManagerWrapper = captureCallbackManagerWrapper2;
        this.captureCallbackManagerWrapper.addResultHandler(new FocusResultHandler());
        this.isCameraAeWithoutAf = !CameraUtil.isCameraAutoFocusSupported(cameraAbilityImpl);
        this.isTriggerLockSupported = CameraUtil.isTriggerLockSupported(cameraAbilityImpl);
    }

    public void captureFocus(Promise promise) {
        if (promise == null) {
            LOGGER.error("captureFocus: promise is null", new Object[0]);
        } else if (this.isCameraAeWithoutAf) {
            LOGGER.debug("captureFocus, af not supported", new Object[0]);
            promise.done();
        } else {
            FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
            if (frameConfigBuilder != null) {
                if (!shouldDoAfTrigger(frameConfigBuilder)) {
                    promise.done();
                    return;
                }
                this.isCapturing = true;
                this.focusMode = 2;
                frameConfigBuilder.setParameter(InnerParameterKey.AF_MODE, Integer.valueOf(this.focusMode));
                sendAfTrigger();
                this.preCapturePromise = promise;
                this.handler.postTask(new Runnable() {
                    /* class ohos.media.camera.mode.function.focus.$$Lambda$FocusFunction$4atJv18mh64juWOrOXAAGu3UFFM */

                    @Override // java.lang.Runnable
                    public final void run() {
                        FocusFunction.this.lambda$captureFocus$0$FocusFunction();
                    }
                }, 6000);
            }
        }
    }

    public /* synthetic */ void lambda$captureFocus$0$FocusFunction() {
        LOGGER.info("preCaptureTimeout run", new Object[0]);
        finishPreCapture();
    }

    public void setCafFocus(Rect rect) {
        if (this.isCameraAeWithoutAf) {
            LOGGER.debug("setCafFocus, af not supported", new Object[0]);
            return;
        }
        FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
        if (frameConfigBuilder != null) {
            this.focusMode = 1;
            frameConfigBuilder.setAfMode(this.focusMode, rect);
            frameConfigBuilder.setAfTrigger(1);
            updatePreview();
        }
    }

    public void setTafFocus(boolean z, Rect rect, ActionStateCallback actionStateCallback2) {
        if (this.isCameraAeWithoutAf) {
            LOGGER.debug("setTafFocus, af not supported", new Object[0]);
            return;
        }
        FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
        if (frameConfigBuilder != null) {
            this.actionStateCallback = actionStateCallback2;
            this.focusMode = 2;
            frameConfigBuilder.setAfMode(this.focusMode, rect);
            sendAfTrigger();
            postCafDelay(Boolean.valueOf(z), rect);
        }
    }

    public void setMfFocus() {
        if (this.isCameraAeWithoutAf) {
            LOGGER.debug("setMfFocus, af not supported", new Object[0]);
            return;
        }
        FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
        if (frameConfigBuilder != null) {
            this.focusMode = 0;
            frameConfigBuilder.setAfMode(this.focusMode, null);
            frameConfigBuilder.setAfTrigger(0);
            updatePreview();
        }
    }

    public void setMfDistance(float f) {
        FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
        if (frameConfigBuilder != null) {
            frameConfigBuilder.setParameter(InnerParameterKey.LENS_FOCUS_DISTANCE, Float.valueOf(f));
            updatePreview();
        }
    }

    private boolean shouldDoAfTrigger(FrameConfig.Builder builder) {
        if (!canLockImmediately() || !lockImmediately(builder)) {
            return true;
        }
        this.isTriggerChanged = false;
        return false;
    }

    private boolean canLockImmediately() {
        if (this.lastAfState == null) {
            return false;
        }
        boolean z = this.focusMode == 1;
        boolean z2 = this.lastAfState.intValue() == 5 || this.lastAfState.intValue() == 2;
        LOGGER.debug("canLockImmediately isContinuous %{public}b, isLocked %{public}b", Boolean.valueOf(z), Boolean.valueOf(z2));
        if (!z || !z2) {
            return false;
        }
        return true;
    }

    private boolean lockImmediately(FrameConfig.Builder builder) {
        LOGGER.debug("lockImmediately isTriggerLockSupported %{public}b", Boolean.valueOf(this.isTriggerLockSupported));
        if (this.isTriggerLockSupported) {
            builder.setParameter(InnerParameterKey.AF_TRIGGER_LOCK, (byte) 1);
            updatePreview();
        }
        return this.isTriggerLockSupported;
    }

    private void sendAfTrigger() {
        FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
        if (frameConfigBuilder != null) {
            frameConfigBuilder.setAfTrigger(1);
            this.isTriggerChanged = true;
            this.triggerSequenceId = updatePreview();
            this.waitedFrames = 0;
            this.isTriggerSequenceIdReached = false;
            this.isInformCompleted = true;
            LOGGER.debug("start waiting converged. trigger sequence id: %{public}d", Long.valueOf(this.lastFrameNumber));
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
        this.isCapturing = false;
    }

    private void postCafDelay(Boolean bool, Rect rect) {
        if (bool.booleanValue()) {
            this.handler.postTask(new Runnable(rect) {
                /* class ohos.media.camera.mode.function.focus.$$Lambda$FocusFunction$VnxfvNHmtAJzm3uHwFJ8iLqweog */
                private final /* synthetic */ Rect f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    FocusFunction.this.lambda$postCafDelay$1$FocusFunction(this.f$1);
                }
            }, 3000);
        }
    }

    public /* synthetic */ void lambda$postCafDelay$1$FocusFunction(Rect rect) {
        LOGGER.debug("cafDelay setCafFocus", new Object[0]);
        setCafFocus(rect);
    }

    private int updatePreview() {
        FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
        if (frameConfigBuilder == null) {
            return -1;
        }
        FrameConfig build = frameConfigBuilder.build();
        boolean isShouldCaptureOnce = isShouldCaptureOnce(frameConfigBuilder);
        int previewRequest = this.cameraController.setPreviewRequest(frameConfigBuilder, this.captureCallbackManagerWrapper.getCaptureCallbackManager(), null);
        if (!isShouldCaptureOnce) {
            return previewRequest;
        }
        LOGGER.begin("doCaptureRequest");
        int capture = this.cameraController.capture(build, this.captureCallbackManagerWrapper.getCaptureCallbackManager(), null);
        LOGGER.end("doCaptureRequest");
        return capture;
    }

    private boolean isShouldCaptureOnce(FrameConfig.Builder builder) {
        boolean z;
        if (hasAfTrigger(builder)) {
            builder.setAfTrigger(0);
            z = true;
        } else {
            z = false;
        }
        if (!hasAfTriggerLock(builder)) {
            return z;
        }
        LOGGER.debug("set af trigger lock success", new Object[0]);
        builder.setParameter(InnerParameterKey.AF_TRIGGER_LOCK, (byte) 0);
        return true;
    }

    private boolean hasAfTrigger(FrameConfig.Builder builder) {
        int afTrigger = builder.build().getAfTrigger();
        return afTrigger == 1 || afTrigger == 2;
    }

    private boolean hasAfTriggerLock(FrameConfig.Builder builder) {
        Byte b = builder != null ? (Byte) builder.build().get(InnerParameterKey.AF_TRIGGER_LOCK) : null;
        if (b == null || b.byteValue() != 1) {
            return false;
        }
        return true;
    }

    @Override // ohos.media.camera.mode.function.PreCaptureManager.PreCaptureHandler
    public void handle(Promise promise) {
        captureFocus(promise);
    }

    /* access modifiers changed from: private */
    public static class AutoFocusResultState {
        private boolean isInMotion;
        private boolean isSendCompletedMessage;
        private boolean isSendMovingMessage;
        private boolean isSuccess;

        private AutoFocusResultState() {
            this.isSendCompletedMessage = false;
            this.isSendMovingMessage = false;
            this.isSuccess = false;
            this.isInMotion = false;
        }
    }

    private class FocusResultHandler extends ResultHandler {
        FocusResultHandler() {
            this.handlerName = FocusResultHandler.class.getSimpleName();
        }

        @Override // ohos.media.camera.mode.function.ResultHandler
        public void handleResult(FrameConfig frameConfig, FrameResult frameResult) {
            if (frameResult != null) {
                if (frameResult.getFrameNumber() < FocusFunction.this.lastFrameNumber) {
                    FocusFunction.LOGGER.error("frame number error: last %{public}d, current %{public}d", Long.valueOf(FocusFunction.this.lastFrameNumber), Long.valueOf(frameResult.getFrameNumber()));
                    return;
                }
                FocusFunction.this.lastFrameNumber = frameResult.getFrameNumber();
                processFrame(frameResult);
            }
        }

        private void processFrame(FrameResult frameResult) {
            Integer valueOf = Integer.valueOf(frameResult.getAfResult().getState());
            int captureTriggerId = frameResult.getCaptureTriggerId();
            if (captureTriggerId >= FocusFunction.this.triggerSequenceId) {
                FocusFunction.this.isTriggerSequenceIdReached = true;
            }
            if ((FocusFunction.this.focusMode == 2 && FocusFunction.this.isInformCompleted) && FocusFunction.this.waitedFrames <= 6) {
                FocusFunction.LOGGER.debug("ignore waiting frame, af_state:%{public}d, sequenceId:%{public}d, waitFrames:%{public}d", valueOf, Integer.valueOf(captureTriggerId), Integer.valueOf(FocusFunction.this.waitedFrames));
                if (FocusFunction.this.isTriggerSequenceIdReached) {
                    FocusFunction.access$608(FocusFunction.this);
                }
            } else if (!Objects.equals(valueOf, FocusFunction.this.lastAfState) || captureTriggerId != FocusFunction.this.lastSequenceId) {
                FocusFunction.this.lastAfState = valueOf;
                FocusFunction.this.lastSequenceId = captureTriggerId;
                if (!FocusFunction.this.isTriggerChanged || !FocusFunction.this.isTriggerSequenceIdReached) {
                    FocusFunction.LOGGER.debug("ignore useless frame, af_state:%{public}d, sequenceId:%{public}d", valueOf, Integer.valueOf(captureTriggerId));
                    return;
                }
                FocusFunction.LOGGER.debug("process frame, af_state:%{public}d, sequenceId:%{public}d, focusMode:%d", valueOf, Integer.valueOf(captureTriggerId), Integer.valueOf(FocusFunction.this.focusMode));
                processPreCapture(valueOf, captureTriggerId);
            } else if (System.currentTimeMillis() - FocusFunction.this.lastUnchangedAfStateTime >= 1000) {
                FocusFunction.LOGGER.debug("ignore unchanged frame, af_state:%{public}d, sequenceId:%{public}d", valueOf, Integer.valueOf(captureTriggerId));
                FocusFunction.this.lastUnchangedAfStateTime = System.currentTimeMillis();
            }
        }

        private void processPreCapture(Integer num, int i) {
            AutoFocusResultState autoFocusResultState = new AutoFocusResultState();
            int i2 = FocusFunction.this.focusMode;
            if (i2 == 1) {
                processContinous(num, autoFocusResultState);
            } else if (i2 == 2) {
                processMacroAuto(num, autoFocusResultState);
            } else if (num.intValue() != 0) {
                FocusFunction.LOGGER.debug("Unexpected AF state change %{public}d (ID %{public}d) in focus mode %{public}d", num, Integer.valueOf(i), Integer.valueOf(FocusFunction.this.focusMode));
            }
            if (FocusFunction.this.isCameraAeWithoutAf) {
                autoFocusResultState.isSuccess = true;
                autoFocusResultState.isSendCompletedMessage = true;
            }
            processCallBack(autoFocusResultState);
        }

        private void processMacroAuto(Integer num, AutoFocusResultState autoFocusResultState) {
            int intValue = num.intValue();
            if (intValue == 4) {
                return;
            }
            if (intValue == 5 || intValue == 6) {
                if (num.intValue() == 5) {
                    autoFocusResultState.isSuccess = true;
                }
                autoFocusResultState.isSendCompletedMessage = true;
                FocusFunction.this.isTriggerChanged = false;
                return;
            }
            FocusFunction.LOGGER.error("Unexpected AF state transition in AUTO/MACRO mode: %{public}d", num);
        }

        private void processContinous(Integer num, AutoFocusResultState autoFocusResultState) {
            int intValue = num.intValue();
            if (intValue == 0) {
                autoFocusResultState.isInMotion = false;
                if (FocusFunction.this.isAfInMotion) {
                    autoFocusResultState.isSendMovingMessage = true;
                }
            } else if (intValue == 1) {
                autoFocusResultState.isInMotion = true;
                autoFocusResultState.isSuccess = true;
                autoFocusResultState.isSendMovingMessage = true;
            } else if (intValue == 2) {
                autoFocusResultState.isSuccess = true;
                autoFocusResultState.isSendMovingMessage = true;
            } else if (intValue == 3) {
                autoFocusResultState.isSendMovingMessage = true;
            } else if (intValue == 5 || intValue == 6) {
                if (num.intValue() == 5) {
                    autoFocusResultState.isSuccess = true;
                }
                autoFocusResultState.isSendCompletedMessage = true;
                autoFocusResultState.isInMotion = false;
                if (FocusFunction.this.isAfInMotion) {
                    autoFocusResultState.isSendMovingMessage = true;
                }
                if (FocusFunction.this.isInformCompleted) {
                    FocusFunction.this.isTriggerChanged = false;
                }
            }
            FocusFunction.this.isAfInMotion = autoFocusResultState.isInMotion;
        }

        private void processCallBack(AutoFocusResultState autoFocusResultState) {
            if (autoFocusResultState.isSendMovingMessage && !FocusFunction.this.isInformCompleted) {
                if (autoFocusResultState.isInMotion) {
                    FocusFunction.LOGGER.info("on focus move start.", new Object[0]);
                } else {
                    FocusFunction.LOGGER.info("on focus move stop.", new Object[0]);
                }
            }
            if (autoFocusResultState.isSendCompletedMessage && FocusFunction.this.isInformCompleted) {
                FocusFunction.LOGGER.info("on focus completed: %{public}b, isCapturing %{public}b", Boolean.valueOf(autoFocusResultState.isSuccess), Boolean.valueOf(FocusFunction.this.isCapturing));
                if (FocusFunction.this.isCapturing) {
                    FocusFunction.this.finishPreCapture();
                } else if (FocusFunction.this.actionStateCallback != null) {
                    FocusFunction.this.actionStateCallback.onFocus(null, autoFocusResultState.isSuccess ? 4 : 5, null);
                }
            }
        }
    }
}
