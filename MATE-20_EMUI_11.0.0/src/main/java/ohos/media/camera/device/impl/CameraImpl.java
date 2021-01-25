package ohos.media.camera.device.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ohos.agp.graphics.Surface;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.Camera;
import ohos.media.camera.device.CameraConfig;
import ohos.media.camera.device.CameraStateCallback;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.device.FrameStateCallback;
import ohos.media.camera.device.impl.CameraConfigImpl;
import ohos.media.camera.device.impl.FrameConfigImpl;
import ohos.media.camera.exception.AccessException;
import ohos.media.camera.zidl.FrameResultNative;
import ohos.media.camera.zidl.ICamera;
import ohos.media.camera.zidl.ICameraCallback;
import ohos.media.camera.zidl.StreamConfiguration;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.media.utils.trace.Tracer;
import ohos.media.utils.trace.TracerFactory;

public class CameraImpl implements Camera, ICameraCallback {
    private static final int CAMERA_STATE_BUSY = 3;
    private static final int CAMERA_STATE_CONFIGURED = 2;
    private static final int CAMERA_STATE_CREATED = 1;
    private static final int CAMERA_STATE_ERROR = -1;
    private static final int CAMERA_STATE_RELEASED = 0;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CameraImpl.class);
    private static final Tracer TRACER = TracerFactory.getCameraTracer();
    private ICamera cameraDevice;
    private final String cameraId;
    private int cameraState = 0;
    private final CameraStateCallback cameraStateCallback;
    private final EventHandler handler;
    private final Object processLock = new Object();
    private StreamAction streamAction;
    private final Map<Integer, StreamConfiguration> streamConfigures = new HashMap();

    @Override // ohos.media.camera.device.Camera
    public void allocateBuffers(Surface surface, int i) {
    }

    @Override // ohos.media.camera.device.Camera
    public void freeBuffers(Surface surface) {
    }

    @Override // ohos.media.camera.zidl.ICameraCallback
    public void onBufferAllocated(int i) {
    }

    public CameraImpl(String str, CameraStateCallback cameraStateCallback2, EventHandler eventHandler) {
        this.cameraId = str;
        this.cameraStateCallback = cameraStateCallback2;
        this.handler = eventHandler;
    }

    public void setCameraDevice(ICamera iCamera) {
        synchronized (this.processLock) {
            this.cameraDevice = iCamera;
            this.cameraState = 1;
            LOGGER.info("Camera created, change state to created", new Object[0]);
        }
    }

    public void setFrameStateCallback(FrameStateCallback frameStateCallback) {
        synchronized (this.processLock) {
            this.streamAction.setFrameStateCallback(frameStateCallback);
        }
    }

    /* access modifiers changed from: package-private */
    public ICamera getCameraDevice() {
        ICamera iCamera;
        synchronized (this.processLock) {
            iCamera = this.cameraDevice;
        }
        return iCamera;
    }

    @Override // ohos.media.camera.device.Camera
    public String getCameraId() {
        return this.cameraId;
    }

    private static void checkFrameConfig(FrameConfig frameConfig) {
        if (frameConfig != null) {
            List<Surface> surfaces = frameConfig.getSurfaces();
            if (surfaces == null || surfaces.isEmpty()) {
                throw new IllegalArgumentException("The surfaces in the frameConfig should not null or empty");
            }
            return;
        }
        throw new IllegalArgumentException("frameConfig should not be null");
    }

    private void stopStreamAction() {
        try {
            TRACER.startTrace("stop-stream-action");
            this.streamAction.stop();
        } catch (AccessException e) {
            LOGGER.error("streamAction stop failed, errorCode: %{public}d", Integer.valueOf(e.getErrorCode()));
            this.cameraState = -1;
            emitFatalErrorEvent(e.getErrorCode());
            release();
        } catch (Throwable th) {
            TRACER.finishTrace("stop-stream-action");
            throw th;
        }
        TRACER.finishTrace("stop-stream-action");
    }

    private void configure(CameraConfigImpl cameraConfigImpl, List<Integer> list, List<StreamConfiguration> list2) {
        try {
            TRACER.startTrace("config-camera-waitIdle");
            this.cameraDevice.waitIdle();
            TRACER.finishTrace("config-camera-waitIdle");
            TRACER.startTrace("config-camera-beginConfig");
            this.cameraDevice.beginConfig();
            TRACER.finishTrace("config-camera-beginConfig");
            TRACER.startTrace("config-camera-deleteOutput");
            for (Integer num : list) {
                int intValue = num.intValue();
                this.streamConfigures.remove(Integer.valueOf(intValue));
                this.cameraDevice.deleteOutput(intValue);
                LOGGER.info("Deleted stream: %{public}d", Integer.valueOf(intValue));
            }
            TRACER.finishTrace("config-camera-deleteOutput");
            TRACER.startTrace("config-camera-createOutput");
            for (StreamConfiguration streamConfiguration : list2) {
                if (!this.streamConfigures.containsValue(streamConfiguration)) {
                    int createOutput = this.cameraDevice.createOutput(streamConfiguration);
                    if (createOutput < 0) {
                        LOGGER.warn("Invalid output stream id %{public}d, continue", Integer.valueOf(createOutput));
                    } else {
                        this.streamConfigures.put(Integer.valueOf(createOutput), streamConfiguration);
                        LOGGER.info("Created stream: %{public}d", Integer.valueOf(createOutput));
                    }
                }
            }
            TRACER.finishTrace("config-camera-createOutput");
            TRACER.startTrace("config-camera-endConfig");
            this.cameraDevice.endConfig();
            TRACER.finishTrace("config-camera-endConfig");
            this.streamAction = new StreamAction(this, cameraConfigImpl, this.streamConfigures);
            TRACER.finishTrace("config-camera");
            LOGGER.info("Camera configure success, cameraId: %{public}s", this.cameraId);
            this.cameraState = 2;
            emitConfiguredEvent();
        } catch (AccessException e) {
            int errorCode = e.getErrorCode();
            LOGGER.error("Camera configure failed, id: %{public}s, errorCode: %{public}d", this.cameraId, Integer.valueOf(errorCode));
            handleCallbackByErrorCode(errorCode);
        }
    }

    private void handleCallbackByErrorCode(int i) {
        if (i == -3 || i == -2) {
            emitConfigureFailedEvent(i);
            return;
        }
        LOGGER.warn("Change camera state to error, emit fatal error event and release the camera", new Object[0]);
        this.cameraState = -1;
        emitFatalErrorEvent(i);
        release();
    }

    @Override // ohos.media.camera.device.Camera
    public void release() {
        Tracer tracer;
        String str;
        LOGGER.info("Camera release start, cameraId: %{public}s", this.cameraId);
        synchronized (this.processLock) {
            if (this.cameraState == 0) {
                LOGGER.warn("Camera is already released", new Object[0]);
                return;
            }
            try {
                TRACER.startTrace("release-camera");
                if (this.streamAction != null) {
                    LOGGER.info("Stop stream action before release", new Object[0]);
                    TRACER.startTrace("stop-stream-action");
                    this.streamAction.stop();
                    TRACER.finishTrace("stop-stream-action");
                    TRACER.startTrace("release-wait-idle");
                    this.cameraDevice.waitIdle();
                    TRACER.finishTrace("release-wait-idle");
                }
                this.cameraDevice.release();
                LOGGER.info("Camera release success, cameraId: %{public}s", this.cameraId);
                this.cameraState = 0;
                emitReleasedEvent();
                tracer = TRACER;
                str = "release-camera";
            } catch (AccessException e) {
                LOGGER.error("Camera release failed, cameraId: %{public}s, errorCode: %{public}d", this.cameraId, Integer.valueOf(e.getErrorCode()));
                this.cameraState = -1;
                emitFatalErrorEvent(e.getErrorCode());
                tracer = TRACER;
                str = "release-camera";
            } catch (Throwable th) {
                TRACER.finishTrace("release-camera");
                throw th;
            }
            tracer.finishTrace(str);
        }
    }

    @Override // ohos.media.camera.device.Camera
    public CameraConfig.Builder getCameraConfigBuilder() {
        return new CameraConfigImpl.Builder(new CameraConfigImpl());
    }

    @Override // ohos.media.camera.device.Camera
    public FrameConfig.Builder getFrameConfigBuilder(@Camera.FrameConfigType int i) {
        FrameConfigImpl.Builder builder;
        synchronized (this.processLock) {
            try {
                builder = new FrameConfigImpl.Builder(new FrameConfigImpl(i, this.cameraDevice.getDefaultFrameConfigParameters(i)));
            } catch (AccessException e) {
                LOGGER.error("Camera device getDefaultFrameConfigParameters failed, cameraId: %{public}s, errorCode: %{public}d", this.cameraId, Integer.valueOf(e.getErrorCode()));
                emitFatalErrorEvent(e.getErrorCode());
                release();
                return null;
            } catch (Throwable th) {
                throw th;
            }
        }
        return builder;
    }

    private static void checkFrameConfigs(List<FrameConfig> list) {
        if (list != null) {
            for (FrameConfig frameConfig : list) {
                checkFrameConfig(frameConfig);
            }
            return;
        }
        throw new IllegalArgumentException("frameConfigs should not be null");
    }

    @Override // ohos.media.camera.device.Camera
    public void configure(CameraConfig cameraConfig) {
        Objects.requireNonNull(cameraConfig, "CameraConfig should not be null");
        if (cameraConfig instanceof CameraConfigImpl) {
            synchronized (this.processLock) {
                if (this.cameraState == -1 || this.cameraState == 0) {
                    LOGGER.error("Invoked configure on invalid state %{public}d", Integer.valueOf(this.cameraState));
                    throw new IllegalStateException("Configure the camera after created or configured one");
                }
                CameraConfigImpl cameraConfigImpl = (CameraConfigImpl) cameraConfig;
                List<Surface> surfaceList = cameraConfigImpl.getSurfaceList();
                if (!surfaceList.isEmpty()) {
                    LOGGER.info("Camera configure start, cameraId: %{public}s", this.cameraId);
                    TRACER.startTrace("config-camera");
                    TRACER.startTrace("config-camera-pretreatment");
                    ArrayList arrayList = new ArrayList(surfaceList.size());
                    for (Surface surface : surfaceList) {
                        arrayList.add(new StreamConfiguration(surface));
                    }
                    ArrayList arrayList2 = new ArrayList();
                    for (Map.Entry<Integer, StreamConfiguration> entry : this.streamConfigures.entrySet()) {
                        if (!arrayList.contains(entry.getValue())) {
                            arrayList2.add(entry.getKey());
                        }
                    }
                    if (this.streamAction != null) {
                        stopStreamAction();
                    }
                    TRACER.finishTrace("config-camera-pretreatment");
                    configure(cameraConfigImpl, arrayList2, arrayList);
                } else {
                    LOGGER.error("Camera config has no surface", new Object[0]);
                    throw new IllegalArgumentException("Camera config has no surface");
                }
            }
            return;
        }
        LOGGER.error("Camera config instance error, config: %{public}s", cameraConfig.toString());
        throw new IllegalArgumentException("Camera config instance error");
    }

    public boolean isLoopingCaptureStarted() {
        boolean z;
        synchronized (this.processLock) {
            z = this.cameraState == 3;
        }
        return z;
    }

    @Override // ohos.media.camera.device.Camera
    public void stopLoopingCapture() {
        LOGGER.info("Camera stopLoopingCapture start, cameraId: %{public}s", this.cameraId);
        synchronized (this.processLock) {
            if (this.cameraState == 3) {
                try {
                    this.streamAction.stopLoopingCapture();
                    this.cameraState = 2;
                    LOGGER.info("Camera stopLoopingCapture success, change camera state to configured", new Object[0]);
                } catch (AccessException e) {
                    int errorCode = e.getErrorCode();
                    LOGGER.error("Camera stopLoopingCapture failed, cameraId: %{public}s, errorCode: %{public}d", this.cameraId, Integer.valueOf(errorCode));
                    LOGGER.warn("Change camera state to error, emit fatal error event and release the camera", new Object[0]);
                    this.cameraState = -1;
                    emitFatalErrorEvent(errorCode);
                    release();
                }
            } else {
                throw new IllegalStateException("Call stopLoopingCapture after starting looping capture");
            }
        }
    }

    @Override // ohos.media.camera.device.Camera
    public int triggerLoopingCapture(FrameConfig frameConfig) {
        int i;
        AccessException e;
        Tracer tracer;
        String str;
        checkFrameConfig(frameConfig);
        LOGGER.info("Camera triggerLoopingCapture start, cameraId: %{public}s", this.cameraId);
        synchronized (this.processLock) {
            if (this.cameraState != 2) {
                if (this.cameraState != 3) {
                    throw new IllegalStateException("Call triggerLoopingCapture after configuring the camera");
                }
            }
            try {
                this.cameraState = 3;
                TRACER.startTrace("trigger-looping-capture");
                i = this.streamAction.startLoopingCapture(frameConfig);
                try {
                    LOGGER.info("Camera triggerLoopingCapture success, cameraId: %{public}s, captureTriggerId: %{public}d", this.cameraId, Integer.valueOf(i));
                    TRACER.startAsyncTrace(Tracer.Camera.FIRST_FRAME, "first-frame-of-trigger-" + i);
                    tracer = TRACER;
                    str = "trigger-looping-capture";
                } catch (AccessException e2) {
                    e = e2;
                    try {
                        int errorCode = e.getErrorCode();
                        LOGGER.error("Camera triggerLoopingCapture failed, cameraId: %{public}s, errorCode: %{public}d", this.cameraId, Integer.valueOf(errorCode));
                        LOGGER.warn("Change camera state to error, emit fatal error event and release the camera", new Object[0]);
                        this.cameraState = -1;
                        emitFatalErrorEvent(errorCode);
                        release();
                        tracer = TRACER;
                        str = "trigger-looping-capture";
                        tracer.finishTrace(str);
                        return i;
                    } catch (Throwable th) {
                        TRACER.finishTrace("trigger-looping-capture");
                        throw th;
                    }
                }
            } catch (AccessException e3) {
                e = e3;
                i = -1;
                int errorCode2 = e.getErrorCode();
                LOGGER.error("Camera triggerLoopingCapture failed, cameraId: %{public}s, errorCode: %{public}d", this.cameraId, Integer.valueOf(errorCode2));
                LOGGER.warn("Change camera state to error, emit fatal error event and release the camera", new Object[0]);
                this.cameraState = -1;
                emitFatalErrorEvent(errorCode2);
                release();
                tracer = TRACER;
                str = "trigger-looping-capture";
                tracer.finishTrace(str);
                return i;
            }
            tracer.finishTrace(str);
        }
        return i;
    }

    @Override // ohos.media.camera.device.Camera
    public int triggerSingleCapture(FrameConfig frameConfig) {
        int i;
        AccessException e;
        Tracer tracer;
        String str;
        checkFrameConfig(frameConfig);
        LOGGER.info("Camera triggerSingleCapture start, cameraId: %{public}s", this.cameraId);
        synchronized (this.processLock) {
            try {
                if (this.cameraState != 2) {
                    if (this.cameraState != 3) {
                        throw new IllegalStateException("Call triggerSingleCapture after configured the camera");
                    }
                }
                this.cameraState = 3;
                TRACER.startTrace("trigger-single-capture");
                i = this.streamAction.captureFrame(frameConfig);
                try {
                    LOGGER.info("Camera triggerSingleCapture success, cameraId: %{public}s, captureTriggerId: %{public}d", this.cameraId, Integer.valueOf(i));
                    TRACER.startAsyncTrace(Tracer.Camera.FIRST_FRAME, "first-frame-of-trigger-" + i);
                    tracer = TRACER;
                    str = "trigger-single-capture";
                } catch (AccessException e2) {
                    e = e2;
                    try {
                        int errorCode = e.getErrorCode();
                        LOGGER.error("Camera triggerSingleCapture failed, cameraId: %{public}s, errorCode: %{public}d", this.cameraId, Integer.valueOf(errorCode));
                        LOGGER.warn("Change camera state to error, emit fatal error event and release the camera", new Object[0]);
                        this.cameraState = -1;
                        emitFatalErrorEvent(errorCode);
                        release();
                        tracer = TRACER;
                        str = "trigger-single-capture";
                        tracer.finishTrace(str);
                        return i;
                    } catch (Throwable th) {
                        TRACER.finishTrace("trigger-single-capture");
                        throw th;
                    }
                }
            } catch (AccessException e3) {
                e = e3;
                i = -1;
                int errorCode2 = e.getErrorCode();
                LOGGER.error("Camera triggerSingleCapture failed, cameraId: %{public}s, errorCode: %{public}d", this.cameraId, Integer.valueOf(errorCode2));
                LOGGER.warn("Change camera state to error, emit fatal error event and release the camera", new Object[0]);
                this.cameraState = -1;
                emitFatalErrorEvent(errorCode2);
                release();
                tracer = TRACER;
                str = "trigger-single-capture";
                tracer.finishTrace(str);
                return i;
            }
            tracer.finishTrace(str);
        }
        return i;
    }

    @Override // ohos.media.camera.device.Camera
    public int triggerMultiCapture(List<FrameConfig> list) {
        int i;
        AccessException e;
        Tracer tracer;
        String str;
        checkFrameConfigs(list);
        LOGGER.info("Camera triggerMultiCapture start, cameraId: %{public}s", this.cameraId);
        synchronized (this.processLock) {
            try {
                if (this.cameraState != 2) {
                    if (this.cameraState != 3) {
                        throw new IllegalStateException("Call triggerMultiCapture after configured the camera");
                    }
                }
                this.cameraState = 3;
                TRACER.startTrace("trigger-multi-capture");
                i = this.streamAction.captureFrames(list);
                try {
                    LOGGER.info("Camera triggerMultiCapture success, cameraId: %{public}s, captureTriggerId: %{public}d", this.cameraId, Integer.valueOf(i));
                    TRACER.startAsyncTrace(Tracer.Camera.FIRST_FRAME, "first-frame-of-trigger-" + i);
                    tracer = TRACER;
                    str = "trigger-multi-capture";
                } catch (AccessException e2) {
                    e = e2;
                    try {
                        int errorCode = e.getErrorCode();
                        LOGGER.error("Camera triggerMultiCapture failed, cameraId: %{public}s, errorCode: %{public}d", this.cameraId, Integer.valueOf(errorCode));
                        LOGGER.warn("Change camera state to error, emit fatal error event and release the camera", new Object[0]);
                        this.cameraState = -1;
                        emitFatalErrorEvent(errorCode);
                        release();
                        tracer = TRACER;
                        str = "trigger-multi-capture";
                        tracer.finishTrace(str);
                        return i;
                    } catch (Throwable th) {
                        TRACER.finishTrace("trigger-multi-capture");
                        throw th;
                    }
                }
            } catch (AccessException e3) {
                e = e3;
                i = -1;
                int errorCode2 = e.getErrorCode();
                LOGGER.error("Camera triggerMultiCapture failed, cameraId: %{public}s, errorCode: %{public}d", this.cameraId, Integer.valueOf(errorCode2));
                LOGGER.warn("Change camera state to error, emit fatal error event and release the camera", new Object[0]);
                this.cameraState = -1;
                emitFatalErrorEvent(errorCode2);
                release();
                tracer = TRACER;
                str = "trigger-multi-capture";
                tracer.finishTrace(str);
                return i;
            }
            tracer.finishTrace(str);
        }
        return i;
    }

    @Override // ohos.media.camera.device.Camera
    public void flushCaptures() {
        LOGGER.info("Camera flushCaptures start, cameraId: %{public}s", this.cameraId);
        synchronized (this.processLock) {
            try {
                if (this.cameraState != 2) {
                    if (this.cameraState != 3) {
                        throw new IllegalStateException("Call flushCaptures after start capture");
                    }
                }
                this.streamAction.flushCaptures();
                LOGGER.info("Camera flushCaptures success, cameraId: %{public}s", this.cameraId);
                this.cameraState = 2;
            } catch (AccessException e) {
                int errorCode = e.getErrorCode();
                LOGGER.error("Camera flushCaptures error, cameraId: %{public}s, errorCode: %{public}d", this.cameraId, Integer.valueOf(errorCode));
                LOGGER.warn("Change camera state to error, emit fatal error event and release the camera", new Object[0]);
                this.cameraState = -1;
                emitFatalErrorEvent(errorCode);
                release();
            }
        }
    }

    private void emitConfiguredEvent() {
        this.handler.postTask(new Runnable() {
            /* class ohos.media.camera.device.impl.$$Lambda$CameraImpl$3J_X0KIQR_pPcFgw5_80GeNtE */

            @Override // java.lang.Runnable
            public final void run() {
                CameraImpl.this.lambda$emitConfiguredEvent$0$CameraImpl();
            }
        });
    }

    public /* synthetic */ void lambda$emitConfiguredEvent$0$CameraImpl() {
        this.cameraStateCallback.onConfigured(this);
    }

    private void emitConfigureFailedEvent(int i) {
        this.handler.postTask(new Runnable(i) {
            /* class ohos.media.camera.device.impl.$$Lambda$CameraImpl$xX2r3G_VcAiK7UL0OvNkz8p4Y5s */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                CameraImpl.this.lambda$emitConfigureFailedEvent$1$CameraImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$emitConfigureFailedEvent$1$CameraImpl(int i) {
        this.cameraStateCallback.onConfigureFailed(this, i);
    }

    private void emitReleasedEvent() {
        this.handler.postTask(new Runnable() {
            /* class ohos.media.camera.device.impl.$$Lambda$CameraImpl$KDUiJFGZj2svM2RTcKpAMmm8NAg */

            @Override // java.lang.Runnable
            public final void run() {
                CameraImpl.this.lambda$emitReleasedEvent$2$CameraImpl();
            }
        });
    }

    public /* synthetic */ void lambda$emitReleasedEvent$2$CameraImpl() {
        this.cameraStateCallback.onReleased(this);
    }

    private void emitFatalErrorEvent(int i) {
        this.handler.postTask(new Runnable(i) {
            /* class ohos.media.camera.device.impl.$$Lambda$CameraImpl$tGgdfI7ZoLE7laVuyh1jDipE3Gs */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                CameraImpl.this.lambda$emitFatalErrorEvent$3$CameraImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$emitFatalErrorEvent$3$CameraImpl(int i) {
        this.cameraStateCallback.onFatalError(this, i);
    }

    @Override // ohos.media.camera.zidl.ICameraCallback
    public void onCameraError(int i) {
        LOGGER.error("Camera occurred fatal error, id: %{public}s, errorCode: %{public}d", this.cameraId, Integer.valueOf(i));
        synchronized (this.processLock) {
            LOGGER.warn("Change camera state to error, emit fatal error event and release the camera", new Object[0]);
            this.cameraState = -1;
            emitFatalErrorEvent(i);
            release();
        }
    }

    @Override // ohos.media.camera.zidl.ICameraCallback
    public void onCaptureTriggerStarted(int i, long j) {
        LOGGER.info("onCaptureTriggerStarted captureTriggerId: %{public}d, firstFrameNumber: %{public}d", Integer.valueOf(i), Long.valueOf(j));
        synchronized (this.processLock) {
            this.streamAction.handleCaptureTriggerStarted(i, j);
        }
    }

    @Override // ohos.media.camera.zidl.ICameraCallback
    public void onCaptureTriggerCompleted(int i, long j) {
        LOGGER.info("onCaptureTriggerCompleted captureTriggerId: %{public}d, lastFrameNumber: %{public}d", Integer.valueOf(i), Long.valueOf(j));
        synchronized (this.processLock) {
            this.streamAction.handleCaptureTriggerCompleted(i, j);
        }
    }

    @Override // ohos.media.camera.zidl.ICameraCallback
    public void onCaptureTriggerInterrupted(int i) {
        LOGGER.info("onCaptureTriggerInterrupted captureTriggerId: %{public}d", Integer.valueOf(i));
        synchronized (this.processLock) {
            this.streamAction.handleCaptureTriggerInterrupted(i);
        }
    }

    @Override // ohos.media.camera.zidl.ICameraCallback
    public void onFrameStarted(FrameResultNative frameResultNative) {
        LOGGER.debug("onFrameStarted captureTriggerId: %{public}d, frameNumber: %{public}d", Integer.valueOf(frameResultNative.getCaptureTriggerId()), Long.valueOf(frameResultNative.getFrameNumber()));
        synchronized (this.processLock) {
            this.streamAction.handleFrameStarted(new FrameResultImpl(frameResultNative));
        }
    }

    @Override // ohos.media.camera.zidl.ICameraCallback
    public void onFrameProgressed(FrameResultNative frameResultNative) {
        LOGGER.debug("onFrameProgressed captureTriggerId: %{public}d, frameNumber: %{public}d", Integer.valueOf(frameResultNative.getCaptureTriggerId()), Long.valueOf(frameResultNative.getFrameNumber()));
        synchronized (this.processLock) {
            this.streamAction.handleFrameProgressed(new FrameResultImpl(frameResultNative));
        }
    }

    @Override // ohos.media.camera.zidl.ICameraCallback
    public void onFrameCompleted(FrameResultNative frameResultNative) {
        LOGGER.debug("onFrameCompleted captureTriggerId: %{public}d, frameNumber: %{public}d", Integer.valueOf(frameResultNative.getCaptureTriggerId()), Long.valueOf(frameResultNative.getFrameNumber()));
        synchronized (this.processLock) {
            this.streamAction.handleFrameCompleted(new FrameResultImpl(frameResultNative));
        }
    }

    @Override // ohos.media.camera.zidl.ICameraCallback
    public void onFrameError(FrameResultNative frameResultNative, int i) {
        LOGGER.warn("onFrameError captureTriggerId: %{public}d, frameNumber: %{public}d, errorCode: %{public}d", Integer.valueOf(frameResultNative.getCaptureTriggerId()), Long.valueOf(frameResultNative.getFrameNumber()), Integer.valueOf(i));
        synchronized (this.processLock) {
            long frameNumber = frameResultNative.getFrameNumber();
            int captureTriggerId = frameResultNative.getCaptureTriggerId();
            int sequenceId = frameResultNative.getSequenceId();
            int errorStreamId = frameResultNative.getErrorStreamId();
            LOGGER.warn("handleFrameError captureTriggerId: %{public}d, frameNumber: %{public}d, sequenceId: %{public}d, errorStreamId: %{public}d, errorCode: %{public}d", Integer.valueOf(captureTriggerId), Long.valueOf(frameNumber), Integer.valueOf(sequenceId), Integer.valueOf(errorStreamId), Integer.valueOf(i));
            this.streamAction.handleFrameError(new FrameResultImpl(frameResultNative), errorStreamId, i);
        }
    }
}
