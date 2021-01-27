package ohos.media.camera.device.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.Camera;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.device.FrameResult;
import ohos.media.camera.device.FrameStateCallback;
import ohos.media.camera.exception.AccessException;
import ohos.media.camera.zidl.CaptureTriggerInfo;
import ohos.media.camera.zidl.FrameConfigNative;
import ohos.media.camera.zidl.ICamera;
import ohos.media.camera.zidl.StreamConfiguration;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.media.utils.trace.Tracer;
import ohos.media.utils.trace.TracerFactory;

/* access modifiers changed from: package-private */
public final class StreamAction {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(StreamAction.class);
    private static final Tracer TRACER = TracerFactory.getCameraTracer();
    private final Camera camera;
    private final ICamera cameraDevice;
    private int currentLoopingCaptureTriggerId = -1;
    private FrameStateCallback frameStateCallback;
    private final EventHandler handler;
    private boolean isStopped;
    private final Map<Integer, StreamConfiguration> streamConfigureMap;

    StreamAction(CameraImpl cameraImpl, CameraConfigImpl cameraConfigImpl, Map<Integer, StreamConfiguration> map) {
        this.camera = cameraImpl;
        this.cameraDevice = cameraImpl.getCameraDevice();
        this.frameStateCallback = cameraConfigImpl.getFrameStateCallback();
        this.handler = cameraConfigImpl.getFrameStateHandler();
        this.streamConfigureMap = map;
    }

    /* access modifiers changed from: package-private */
    public int startLoopingCapture(FrameConfig frameConfig) throws AccessException {
        return captureFrameInternal(Collections.singletonList(frameConfig), true);
    }

    /* access modifiers changed from: package-private */
    public void stopLoopingCapture() throws AccessException {
        int i = this.currentLoopingCaptureTriggerId;
        if (i == -1) {
            LOGGER.warn("stopLoppingCapture currentLoopingCaptureTriggerId is -1, no need to stop again", new Object[0]);
            return;
        }
        LOGGER.debug("stopLoppingCapture currentLoopingCaptureTriggerId: %{public}d", Integer.valueOf(i));
        LOGGER.debug("stopLoppingCapture lastFrameNumber: %{public}d", Long.valueOf(this.cameraDevice.cancelCaptureFrames(this.currentLoopingCaptureTriggerId)));
        this.currentLoopingCaptureTriggerId = -1;
        LOGGER.debug("stopLoppingCapture change currentLoopingCaptureTriggerId to CAPTURE_TRIGGER_ERROR_ID %{public}d", -1);
    }

    /* access modifiers changed from: package-private */
    public int startLoopingCapture(List<FrameConfig> list) throws AccessException {
        return captureFrameInternal(list, true);
    }

    /* access modifiers changed from: package-private */
    public int captureFrame(FrameConfig frameConfig) throws AccessException {
        return captureFrames(Collections.singletonList(frameConfig));
    }

    /* access modifiers changed from: package-private */
    public int captureFrames(List<FrameConfig> list) throws AccessException {
        return captureFrameInternal(list, false);
    }

    private int captureFrameInternal(List<FrameConfig> list, boolean z) throws AccessException {
        CaptureTriggerInfo captureFrames = this.cameraDevice.captureFrames(cast2Natives(list), z);
        if (captureFrames == null) {
            LOGGER.warn("captureFrameInternal service returned captureTriggerInfo is null", new Object[0]);
            return -1;
        }
        int id = captureFrames.getId();
        if (z) {
            this.currentLoopingCaptureTriggerId = id;
            LOGGER.debug("captureFrameInternal change currentLoopingCaptureTriggerId to %{public}d", Integer.valueOf(id));
        }
        return id;
    }

    private List<FrameConfigNative> cast2Natives(List<FrameConfig> list) {
        ArrayList arrayList = new ArrayList(list.size());
        for (FrameConfig frameConfig : list) {
            if (frameConfig instanceof FrameConfigImpl) {
                arrayList.add(((FrameConfigImpl) frameConfig).cast2Native());
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: package-private */
    public void flushCaptures() throws AccessException {
        LOGGER.debug("flushCaptures success, currentLoopingCaptureTriggerId: %{public}d, lastFrameNumber: %{public}d", Integer.valueOf(this.currentLoopingCaptureTriggerId), Long.valueOf(this.cameraDevice.flush()));
        this.currentLoopingCaptureTriggerId = -1;
    }

    /* access modifiers changed from: package-private */
    public void stop() throws AccessException {
        if (this.isStopped) {
            LOGGER.warn("StreamAction is already stopped", new Object[0]);
            return;
        }
        this.isStopped = true;
        stopLoopingCapture();
        LOGGER.info("StreamAction is stopped", new Object[0]);
    }

    /* access modifiers changed from: package-private */
    public void handleCaptureTriggerStarted(int i, long j) {
        emitCaptureTriggerStartedEvent(i, j);
    }

    private void emitCaptureTriggerStartedEvent(int i, long j) {
        if (this.frameStateCallback != null) {
            LOGGER.debug("emitCaptureTriggerStartedEvent for captureTriggerId: %{public}d, frameNumber: %{public}d", Integer.valueOf(i), Long.valueOf(j));
            this.handler.postTask(new Runnable(i, j) {
                /* class ohos.media.camera.device.impl.$$Lambda$StreamAction$_rjm9f3X9RXzEGCPECWb5MncJ7Q */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ long f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    StreamAction.this.lambda$emitCaptureTriggerStartedEvent$0$StreamAction(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$emitCaptureTriggerStartedEvent$0$StreamAction(int i, long j) {
        this.frameStateCallback.onCaptureTriggerStarted(this.camera, i, j);
    }

    /* access modifiers changed from: package-private */
    public void handleCaptureTriggerCompleted(int i, long j) {
        emitCaptureTriggerCompletedEvent(i, j);
    }

    private void emitCaptureTriggerCompletedEvent(int i, long j) {
        LOGGER.debug("onCaptureTriggerCompleted for captureTriggerId: %{public}d, lastFrameNumber: %{public}d", Integer.valueOf(i), Long.valueOf(j));
        if (this.frameStateCallback != null) {
            this.handler.postTask(new Runnable(i, j) {
                /* class ohos.media.camera.device.impl.$$Lambda$StreamAction$1TH2AwMw_BZkQ_EMAdT39nHgY */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ long f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    StreamAction.this.lambda$emitCaptureTriggerCompletedEvent$1$StreamAction(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$emitCaptureTriggerCompletedEvent$1$StreamAction(int i, long j) {
        this.frameStateCallback.onCaptureTriggerFinished(this.camera, i, j);
    }

    /* access modifiers changed from: package-private */
    public void handleCaptureTriggerInterrupted(int i) {
        LOGGER.debug("handleCaptureTriggerInterrupted set currentLoopingCaptureTriggerId to CAPTURE_TRIGGER_ERROR_ID", new Object[0]);
        this.currentLoopingCaptureTriggerId = -1;
        emitCaptureTriggerInterruptedEvent(i);
    }

    private void emitCaptureTriggerInterruptedEvent(int i) {
        if (this.frameStateCallback != null) {
            LOGGER.warn("emitCaptureTriggerInterruptedEvent for captureTriggerId: %{public}d", Integer.valueOf(i));
            this.handler.postTask(new Runnable(i) {
                /* class ohos.media.camera.device.impl.$$Lambda$StreamAction$TEf_TYFaVkL6S3jpsZn1FceLcM */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    StreamAction.this.lambda$emitCaptureTriggerInterruptedEvent$2$StreamAction(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$emitCaptureTriggerInterruptedEvent$2$StreamAction(int i) {
        this.frameStateCallback.onCaptureTriggerInterrupted(this.camera, i);
    }

    /* access modifiers changed from: package-private */
    public void handleFrameStarted(FrameResultImpl frameResultImpl) {
        emitFrameStartedEvent(frameResultImpl.getCaptureTriggerId(), frameResultImpl.getFrameConfig(), frameResultImpl.getFrameNumber(), frameResultImpl.getTimestamp());
    }

    private void emitFrameStartedEvent(int i, FrameConfig frameConfig, long j, long j2) {
        if (this.frameStateCallback != null) {
            LOGGER.debug("emitFrameStartedEvent, captureTriggerId: %{public}d, frameNumber: %{public}d, timestamp: %{public}d", Integer.valueOf(i), Long.valueOf(j), Long.valueOf(j2));
            this.handler.postTask(new Runnable(frameConfig, j, j2) {
                /* class ohos.media.camera.device.impl.$$Lambda$StreamAction$fTgukoJFvLt1JjIWFe8ClVFAYtQ */
                private final /* synthetic */ FrameConfig f$1;
                private final /* synthetic */ long f$2;
                private final /* synthetic */ long f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r5;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    StreamAction.this.lambda$emitFrameStartedEvent$3$StreamAction(this.f$1, this.f$2, this.f$3);
                }
            });
        }
    }

    public /* synthetic */ void lambda$emitFrameStartedEvent$3$StreamAction(FrameConfig frameConfig, long j, long j2) {
        this.frameStateCallback.onFrameStarted(this.camera, frameConfig, j, j2);
    }

    /* access modifiers changed from: package-private */
    public void handleFrameProgressed(FrameResultImpl frameResultImpl) {
        emitFrameProgressedEvent(frameResultImpl);
    }

    private void emitFrameProgressedEvent(FrameResult frameResult) {
        if (this.frameStateCallback != null) {
            LOGGER.debug("emitFrameProgressedEvent for captureTriggerId: %{public}d, frameNumber: %{public}d", Integer.valueOf(frameResult.getCaptureTriggerId()), Long.valueOf(frameResult.getFrameNumber()));
            this.handler.postTask(new Runnable(frameResult) {
                /* class ohos.media.camera.device.impl.$$Lambda$StreamAction$MeOnh6fSUfyY3PxCKuyvhf0zX5E */
                private final /* synthetic */ FrameResult f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    StreamAction.this.lambda$emitFrameProgressedEvent$4$StreamAction(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$emitFrameProgressedEvent$4$StreamAction(FrameResult frameResult) {
        this.frameStateCallback.onFrameProgressed(this.camera, frameResult.getFrameConfig(), frameResult);
    }

    /* access modifiers changed from: package-private */
    public void handleFrameCompleted(FrameResultImpl frameResultImpl) {
        emitFrameCompletedEvent(frameResultImpl);
        Tracer tracer = TRACER;
        tracer.finishAsyncTrace(Tracer.Camera.FIRST_FRAME, "first-frame-of-trigger-" + this.currentLoopingCaptureTriggerId);
    }

    private void emitFrameCompletedEvent(FrameResult frameResult) {
        if (this.frameStateCallback != null) {
            LOGGER.debug("emitFrameCompletedEvent for captureTriggerId: %{public}d, frameNumber: %{public}d", Integer.valueOf(frameResult.getCaptureTriggerId()), Long.valueOf(frameResult.getFrameNumber()));
            this.handler.postTask(new Runnable(frameResult) {
                /* class ohos.media.camera.device.impl.$$Lambda$StreamAction$zyGFlzsFv4nFfneFPdjksbqCY */
                private final /* synthetic */ FrameResult f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    StreamAction.this.lambda$emitFrameCompletedEvent$5$StreamAction(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$emitFrameCompletedEvent$5$StreamAction(FrameResult frameResult) {
        this.frameStateCallback.onFrameFinished(this.camera, frameResult.getFrameConfig(), frameResult);
    }

    /* access modifiers changed from: package-private */
    public void handleFrameError(FrameResultImpl frameResultImpl, int i, int i2) {
        FrameConfig frameConfig = frameResultImpl.getFrameConfig();
        if (i2 == -7) {
            if (frameConfig.getSurfaces().contains(this.streamConfigureMap.get(Integer.valueOf(i)).getSurface())) {
                emitFrameErrorEvent(frameResultImpl, -4);
            } else {
                LOGGER.error("handleFrameError surface is not in frameConfigs", new Object[0]);
            }
        } else if (i2 == -8) {
            emitFrameErrorEvent(frameResultImpl, -3);
        } else {
            emitFrameErrorEvent(frameResultImpl, -5);
        }
    }

    private void emitFrameErrorEvent(FrameResult frameResult, int i) {
        LOGGER.debug("emitFrameErrorEvent captureTriggerId: %{public}d, frameNumber: %{public}d, errorCode: %{public}d", Integer.valueOf(frameResult.getCaptureTriggerId()), Long.valueOf(frameResult.getFrameNumber()), Integer.valueOf(i));
        if (this.frameStateCallback != null) {
            this.handler.postTask(new Runnable(frameResult, i) {
                /* class ohos.media.camera.device.impl.$$Lambda$StreamAction$R7KqEDjF0eOX8RN0_e46itlG64 */
                private final /* synthetic */ FrameResult f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    StreamAction.this.lambda$emitFrameErrorEvent$6$StreamAction(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$emitFrameErrorEvent$6$StreamAction(FrameResult frameResult, int i) {
        this.frameStateCallback.onFrameError(this.camera, frameResult.getFrameConfig(), i, frameResult);
    }

    /* access modifiers changed from: package-private */
    public void setFrameStateCallback(FrameStateCallback frameStateCallback2) {
        this.frameStateCallback = frameStateCallback2;
    }
}
