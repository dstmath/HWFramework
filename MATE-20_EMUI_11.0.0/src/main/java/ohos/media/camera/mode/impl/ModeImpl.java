package ohos.media.camera.mode.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import ohos.agp.graphics.Surface;
import ohos.agp.graphics.SurfaceOps;
import ohos.agp.utils.Rect;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.location.Location;
import ohos.media.camera.device.Camera;
import ohos.media.camera.device.CameraStateCallback;
import ohos.media.camera.device.adapter.utils.SurfaceUtils;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.ActionDataCallback;
import ohos.media.camera.mode.BurstResult;
import ohos.media.camera.mode.Mode;
import ohos.media.camera.mode.ModeConfig;
import ohos.media.camera.mode.ModeStateCallback;
import ohos.media.camera.mode.PreviewResult;
import ohos.media.camera.mode.RecordingResult;
import ohos.media.camera.mode.TakePictureResult;
import ohos.media.camera.mode.action.CaptureAction;
import ohos.media.camera.mode.action.NormalCaptureAction;
import ohos.media.camera.mode.action.NormalPreviewAction;
import ohos.media.camera.mode.action.NormalRecordAction;
import ohos.media.camera.mode.action.PreviewAction;
import ohos.media.camera.mode.action.RecordAction;
import ohos.media.camera.mode.adapter.key.ModeCharacteristicKey;
import ohos.media.camera.mode.adapter.key.ModeRequestKey;
import ohos.media.camera.mode.controller.CameraController;
import ohos.media.camera.mode.function.CaptureCallbackManagerWrapper;
import ohos.media.camera.mode.function.PreCapture;
import ohos.media.camera.mode.function.PreCaptureManager;
import ohos.media.camera.mode.function.PreviewCallbackFunction;
import ohos.media.camera.mode.function.face.FaceDetectionFunction;
import ohos.media.camera.mode.function.focus.Camera3aManager;
import ohos.media.camera.mode.function.masterai.MasterAiFunction;
import ohos.media.camera.mode.impl.ModeConfigImpl;
import ohos.media.camera.mode.impl.ModeImpl;
import ohos.media.camera.mode.tags.BaseModeTags;
import ohos.media.camera.mode.utils.CameraManagerHelper;
import ohos.media.camera.mode.utils.CameraUtil;
import ohos.media.camera.mode.utils.CollectionUtil;
import ohos.media.camera.mode.utils.ConflictChecker;
import ohos.media.camera.mode.utils.ImageReaderProxy;
import ohos.media.camera.mode.utils.OptimalSizeCombination;
import ohos.media.camera.params.FaceDetectionResult;
import ohos.media.camera.params.FocusResult;
import ohos.media.camera.params.Metadata;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.ParametersResult;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.SceneDetectionResult;
import ohos.media.image.Image;
import ohos.media.image.ImageReceiver;
import ohos.media.image.common.Size;
import ohos.media.recorder.Recorder;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.media.utils.trace.Tracer;
import ohos.media.utils.trace.TracerFactory;
import ohos.utils.Pair;

public class ModeImpl implements Mode {
    private static final int DEFAULT_MAX_IMAGES = 1;
    private static final long INTERVAL_OF_SET_PARAMETER_AND_TAKE_PICTURE_MS = 500;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ModeImpl.class);
    public static final int MAX_DEFERRED_PREVIEW_SURFACE_NUMBER = 1;
    public static final int MAX_PREVIEW_SURFACE_NUMBER = 2;
    private static final int MAX_SURFACE_NUMBER = 10;
    private static final int MODE_STATE_ACTIVATED = 2;
    private static final int MODE_STATE_ERROR = -1;
    private static final int MODE_STATE_IDLE = 0;
    private static final int MODE_STATE_OPENED = 1;
    public static final List<Class<?>> PHOTO_SUPPORTED_CLASS = Collections.unmodifiableList(Arrays.asList(ImageReceiver.class, SurfaceOps.class));
    private static final int ROTATION_DEGREE_CIRCLE = 360;
    private static final int ROTATION_DEGREE_UNIT = 90;
    public static final List<Integer> SUPPORTED_FORMATS = Collections.unmodifiableList(Arrays.asList(3));
    private static final Tracer TRACER = TracerFactory.getCameraTracer(LOGGER);
    public static final List<Class<?>> VIDEO_SUPPORTED_CLASS = Collections.unmodifiableList(Arrays.asList(ImageReceiver.class, SurfaceOps.class, Recorder.class));
    protected volatile InnerActionDataCallbackImpl actionDataCallback;
    protected volatile InnerActionStateCallbackImpl actionStateCallback;
    protected List<OptimalSizeCombination> backPassThroughCombinations = new ArrayList();
    private boolean callbackOnReleased = true;
    private final Camera3aManager camera3aManager;
    private final CameraAbilityImpl cameraAbility;
    private final String cameraId;
    protected volatile CaptureAction capture;
    protected final CaptureCallbackManagerWrapper captureCallbackManagerWrapper;
    private volatile CaptureState captureState = CaptureState.IDLE;
    protected int captureTriggerId = -1;
    private final CameraController controller;
    private final Map<PropertyKey.Key<?>, Boolean> currentFunctionStatus;
    private final EventHandler dataCallbackHandler;
    private volatile int enableActions;
    private final EventHandler externalDeviceOperateHandler;
    protected FaceDetectionFunction faceDetectionFunction;
    private volatile int imageFormat = 3;
    protected volatile int imageRotation;
    private volatile Size imageSize;
    private final Object interfaceLock = new Object();
    private boolean isDeferred;
    private boolean isFaceDetectionOn = false;
    protected boolean isPassThroughResolution = false;
    protected boolean isPostProcess;
    private volatile boolean isPreviewStarted;
    private boolean isSmileDetectionOn = false;
    private long lastSetParameterTime;
    protected volatile MasterAiFunction masterAiFunction;
    protected volatile ModeAbilityImpl modeAbility;
    private volatile int modeState = 0;
    private final ModeStateCallback modeStateCallback;
    protected volatile BaseModeTags modeTags;
    @Mode.Type
    protected volatile int modeType;
    private final EventHandler modeWorkHandler;
    protected volatile PreCapture preCapture;
    protected final PreCaptureManager preCaptureManager;
    protected volatile PreviewAction preview;
    protected PreviewCallbackFunction previewCallbackFunction;
    private volatile Size previewSize;
    protected volatile RecordAction record;
    private volatile RecordState recordState = RecordState.IDLE;
    private final EventHandler stateCallbackHandler;
    private Class<?> surfaceClass;
    private Size surfaceSize;
    protected List<Surface> surfaces = new ArrayList(10);
    private volatile Size videoSize;
    private float zoomValue;

    public @interface Action {
        public static final int ACTION_BURST = 3;
        public static final int ACTION_CAPTURE = 2;
        public static final int ACTION_FACE_DETECTION = 6;
        public static final int ACTION_FOCUS = 5;
        public static final int ACTION_PARAMETERS = 9;
        public static final int ACTION_PREVIEW = 1;
        public static final int ACTION_RECORDING = 4;
        public static final int ACTION_SCENE_DETECTION = 8;
        public static final int ACTION_SMILE_DETECTION = 7;
    }

    /* access modifiers changed from: protected */
    public enum CaptureState {
        IDLE,
        CAPTUREING,
        Burst
    }

    /* access modifiers changed from: protected */
    public enum RecordState {
        IDLE,
        PREPARED,
        RECORDING,
        PAUSED
    }

    public void checkModeConfig(ModeConfig modeConfig) {
    }

    /* access modifiers changed from: protected */
    public int getCaptureTemplateType() {
        return 2;
    }

    /* access modifiers changed from: protected */
    public int getMaxPreviewSurfaceNumber() {
        return 2;
    }

    public ModeImpl(String str, ModeStateCallback modeStateCallback2, EventHandler eventHandler) {
        this.cameraId = str;
        this.modeStateCallback = modeStateCallback2;
        this.externalDeviceOperateHandler = eventHandler;
        this.modeWorkHandler = new EventHandler(EventRunner.create("ModeWorkHandler"));
        this.dataCallbackHandler = new EventHandler(EventRunner.create("DataCallbackThread"));
        this.stateCallbackHandler = new EventHandler(EventRunner.create("StateCallbackThread"));
        this.cameraAbility = CameraManagerHelper.getCameraManager().getCameraAbility(str);
        this.captureCallbackManagerWrapper = new CaptureCallbackManagerWrapper();
        this.controller = getCameraController(eventHandler, this.stateCallbackHandler, this.modeStateCallback, this.captureCallbackManagerWrapper);
        this.camera3aManager = new Camera3aManager(this.controller, this.modeWorkHandler, this.cameraAbility, this.captureCallbackManagerWrapper);
        this.preCaptureManager = new PreCaptureManager(this.controller, this.modeWorkHandler, this.captureCallbackManagerWrapper, this.camera3aManager);
        this.currentFunctionStatus = new ConcurrentHashMap();
    }

    @Override // ohos.media.camera.mode.Mode
    @Mode.Type
    public int getType() {
        return this.modeType;
    }

    @Override // ohos.media.camera.mode.Mode
    public ModeConfig.Builder getModeConfigBuilder() {
        return new ModeConfigImpl.Builder(this);
    }

    @Override // ohos.media.camera.mode.Mode
    public String getCameraId() {
        return this.cameraId;
    }

    @Override // ohos.media.camera.mode.Mode
    public ModeAbilityImpl getModeAbility() {
        return this.modeAbility;
    }

    public EventHandler getHandler() {
        return this.externalDeviceOperateHandler;
    }

    /* access modifiers changed from: package-private */
    public CameraController getCameraController(EventHandler eventHandler, EventHandler eventHandler2, ModeStateCallback modeStateCallback2, CaptureCallbackManagerWrapper captureCallbackManagerWrapper2) {
        CameraController cameraController = new CameraController(eventHandler, eventHandler2, modeStateCallback2, captureCallbackManagerWrapper2);
        cameraController.prepare(this.cameraId);
        return cameraController;
    }

    public void setStateCallback(ActionStateCallbackImpl actionStateCallbackImpl, EventHandler eventHandler) {
        this.actionStateCallback = new InnerActionStateCallbackImpl(this, actionStateCallbackImpl, eventHandler);
    }

    public void setDataCallback(ActionDataCallbackImpl actionDataCallbackImpl, EventHandler eventHandler) {
        this.actionDataCallback = new InnerActionDataCallbackImpl(this, actionDataCallbackImpl, eventHandler);
    }

    public void createAction(int[] iArr) {
        LOGGER.debug("createAction: ", new Object[0]);
        int i = 0;
        for (int i2 : iArr) {
            i |= i2;
        }
        if ((i & 1) == 1) {
            createPreviewAction();
        }
        if ((i & 2) == 2) {
            createCaptureAction();
        }
        if ((i & 4) == 4) {
            createRecordAction();
        }
    }

    /* access modifiers changed from: protected */
    public void createPreviewAction() {
        this.preview = new NormalPreviewAction(this.controller, this.modeTags, this.cameraAbility);
    }

    /* access modifiers changed from: protected */
    public void createCaptureAction() {
        this.capture = new NormalCaptureAction(this.controller, this.modeTags, this.cameraAbility, new EventHandler[]{this.externalDeviceOperateHandler, this.dataCallbackHandler}, this.preCapture);
    }

    /* access modifiers changed from: protected */
    public void createRecordAction() {
        this.record = new NormalRecordAction(this.controller, this.modeTags, this.cameraAbility);
    }

    /* access modifiers changed from: protected */
    public final int updatePreview() {
        if (this.isPreviewStarted) {
            return this.preview.updatePreview();
        }
        LOGGER.debug("updatePreview ignore", new Object[0]);
        return -1;
    }

    /* access modifiers changed from: protected */
    public List<Surface> getEffectSurfaces() {
        ArrayList arrayList = new ArrayList();
        if (this.preview != null && !CollectionUtil.isEmptyCollection(this.preview.getSurfaces())) {
            arrayList.addAll(this.preview.getSurfaces());
            LOGGER.debug("add preview effectSurfaces instance : %{public}s and preview size = %{public}s", this.preview.getSurface(), SurfaceUtils.getSurfaceSize(this.preview.getSurface()));
        }
        if (!(this.record == null || this.record.getSurface() == null || this.recordState == RecordState.IDLE)) {
            arrayList.add(this.record.getSurface());
            LOGGER.debug("add record effectSurfaces : %{public}s and record size =%{public}s, recordState: %{public}d", this.record.getSurface(), SurfaceUtils.getSurfaceSize(this.record.getSurface()), this.recordState);
        }
        return arrayList;
    }

    public List<Surface> getSurfaces(boolean z) {
        this.surfaces.clear();
        if (!(this.preview == null || this.preview.getSurfaces() == null || this.preview.getSurfaces().isEmpty())) {
            this.surfaces.addAll(this.preview.getSurfaces());
            LOGGER.debug("add preview surface instance : %{public}s and preview size = %{public}s", this.preview.getSurface(), SurfaceUtils.getSurfaceSize(this.preview.getSurface()));
        }
        if (!(this.capture == null || this.capture.getSurface() == null)) {
            this.surfaces.add(this.capture.getSurface());
            LOGGER.debug("add capture surface : %{public}s and capture size = %{public}s", this.capture.getSurface(), SurfaceUtils.getSurfaceSize(this.capture.getSurface()));
        }
        if (!(this.capture == null || this.capture.getRawSurface() == null)) {
            this.surfaces.add(this.capture.getRawSurface());
            LOGGER.debug("add raw capture surface : %{public}s and capture size =%{public}s", this.capture.getRawSurface(), SurfaceUtils.getSurfaceSize(this.capture.getRawSurface()));
        }
        if (!(this.record == null || this.record.getSurface() == null || !z)) {
            this.surfaces.add(this.record.getSurface());
            LOGGER.debug("add record surface : %{public}s and record size =%{public}s", this.record.getSurface(), SurfaceUtils.getSurfaceSize(this.record.getSurface()));
        }
        return this.surfaces;
    }

    @Override // ohos.media.camera.mode.Mode
    public void configure(ModeConfig modeConfig) {
        TRACER.startTrace("configure-mode");
        Objects.requireNonNull(modeConfig, "ModeConfig should not be null!");
        if (modeConfig instanceof ModeConfigImpl) {
            ModeConfigImpl modeConfigImpl = (ModeConfigImpl) modeConfig;
            checkModeConfig(modeConfigImpl);
            configCallbacks(modeConfigImpl);
            if (isModeActivated() && (modeConfigImpl.isDeferredPreview() || modeConfigImpl.isWaitForDeferredSurface())) {
                throw new IllegalStateException("Mode is running, Deferred preview is not supported!");
            } else if (modeConfigImpl.isDeferredPreview() || !modeConfigImpl.getPreviewSurfaces().isEmpty()) {
                TRACER.startTrace("configure-mode-finalizeDeferredSurface");
                if (finalizeDeferredSurface(modeConfigImpl)) {
                    modeConfigImpl.finishDeferredSurface();
                    TRACER.finishTrace("configure-mode-finalizeDeferredSurface");
                    return;
                }
                if (modeConfigImpl.isSurfaceUpdated()) {
                    LOGGER.debug("pending.isSurfaceUpdated. configure: %{public}d", Integer.valueOf(this.modeState));
                    deactive();
                    configSurfaces(modeConfigImpl);
                    active();
                }
                TRACER.finishTrace("configure-mode");
            } else {
                throw new IllegalArgumentException("In ModeConfig, previewSurface should not be null");
            }
        }
    }

    public final void active() {
        TRACER.startTrace("mode-active");
        synchronized (this.interfaceLock) {
            if (!isModeActivated()) {
                if (this.preCapture != null) {
                    this.preCapture.active();
                }
                checkModeOpened();
                if (!(this.imageSize == null || this.capture == null)) {
                    this.capture.createSurface(this.imageSize, this.imageFormat, 1);
                }
                this.controller.setModeType(this.modeType);
                if (this.record == null || this.record.getSurface() == null) {
                    this.controller.setPreviewTemplateType(1);
                } else {
                    this.controller.setPreviewTemplateType(3);
                }
                if (this.isDeferred) {
                    ArrayList arrayList = new ArrayList(10);
                    if (!(this.capture == null || this.capture.getSurface() == null)) {
                        arrayList.add(this.capture.getSurface());
                    }
                    if (!(this.record == null || this.record.getSurface() == null)) {
                        arrayList.add(this.record.getSurface());
                    }
                    this.controller.configure(this.surfaceSize, this.surfaceClass, arrayList);
                } else {
                    this.controller.configure(getSurfaces(true));
                }
                activeInternal();
                TRACER.finishTrace("mode-active");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void activeInternal() {
        LOGGER.debug("Nothing to do in BaseMode", new Object[0]);
    }

    public final void deactive() {
        LOGGER.debug("deactive: modeState = %{public}d", Integer.valueOf(this.modeState));
        TRACER.startTrace("mode-deactive");
        synchronized (this.interfaceLock) {
            if (isModeActivated()) {
                if (this.preCapture != null) {
                    this.preCapture.deactive();
                }
                deactiveInternal();
            }
        }
        TRACER.finishTrace("mode-deactive");
    }

    /* access modifiers changed from: protected */
    public void deactiveInternal() {
        LOGGER.debug("deactiveInternal modeState = %{public}d", Integer.valueOf(this.modeState));
        if (this.capture != null) {
            this.capture.destroySurface();
        }
        if (isModeActivated()) {
            this.modeState = 1;
            this.isPreviewStarted = false;
            LOGGER.debug("isPreviewStarted = false @deactiveInternal", new Object[0]);
            this.controller.destroy();
        }
    }

    public final void open() {
        synchronized (this.interfaceLock) {
            this.controller.openCamera(this.cameraId, new CameraStateCallback() {
                /* class ohos.media.camera.mode.impl.ModeImpl.AnonymousClass1StateCallbackImpl */

                private int mapDeviceErrorToModeError(int i) {
                    if (i == -6) {
                        return 5;
                    }
                    if (i == -5) {
                        return 4;
                    }
                    if (i == -4) {
                        return 3;
                    }
                    if (i != -3) {
                        return i != -2 ? 0 : 1;
                    }
                    return 2;
                }

                @Override // ohos.media.camera.device.CameraStateCallback
                public void onCreated(Camera camera) {
                    ModeImpl.LOGGER.debug("StateCallbackImpl onCreated", new Object[0]);
                    synchronized (ModeImpl.this.interfaceLock) {
                        ModeImpl.this.modeState = 1;
                        ModeImpl.this.callbackOnReleased = false;
                    }
                    ModeImpl.this.modeStateCallback.onCreated(ModeImpl.this);
                }

                @Override // ohos.media.camera.device.CameraStateCallback
                public void onCreateFailed(String str, int i) {
                    boolean z = false;
                    ModeImpl.LOGGER.debug("StateCallbackImpl onCreatedFailed", new Object[0]);
                    synchronized (ModeImpl.this.interfaceLock) {
                        if (ModeImpl.this.modeState == 0) {
                            ModeImpl.this.callbackOnReleased = true;
                            z = true;
                        }
                    }
                    if (z) {
                        ModeImpl.this.modeStateCallback.onCreateFailed(str, ModeImpl.this.modeType, mapDeviceErrorToModeError(i));
                    } else {
                        ModeImpl.this.modeStateCallback.onFatalError(ModeImpl.this, 5);
                    }
                    ModeImpl.this.release();
                }

                @Override // ohos.media.camera.device.CameraStateCallback
                public void onConfigured(Camera camera) {
                    ModeImpl.LOGGER.debug("StateCallbackImpl onConfigured", new Object[0]);
                    synchronized (ModeImpl.this.interfaceLock) {
                        ModeImpl.this.modeState = 2;
                    }
                    ModeImpl.this.modeStateCallback.onConfigured(ModeImpl.this);
                }

                @Override // ohos.media.camera.device.CameraStateCallback
                public void onConfigureFailed(Camera camera, int i) {
                    ModeImpl.LOGGER.debug("StateCallbackImpl onConfigureFailed", new Object[0]);
                    ModeImpl.this.modeStateCallback.onConfigureFailed(ModeImpl.this, mapDeviceErrorToModeError(i));
                }

                @Override // ohos.media.camera.device.CameraStateCallback
                public void onReleased(Camera camera) {
                    ModeImpl.LOGGER.debug("StateCallbackImpl onReleased", new Object[0]);
                    synchronized (ModeImpl.this.interfaceLock) {
                        ModeImpl.this.modeState = 0;
                        if (ModeImpl.this.callbackOnReleased) {
                            ModeImpl.LOGGER.debug("already released!", new Object[0]);
                            return;
                        }
                        ModeImpl.this.callbackOnReleased = true;
                        ModeImpl.this.modeStateCallback.onReleased(ModeImpl.this);
                    }
                }

                @Override // ohos.media.camera.device.CameraStateCallback
                public void onFatalError(Camera camera, int i) {
                    boolean z = false;
                    ModeImpl.LOGGER.debug("StateCallbackImpl onFatalError", new Object[0]);
                    synchronized (ModeImpl.this.interfaceLock) {
                        if (ModeImpl.this.modeState == 0) {
                            ModeImpl.this.callbackOnReleased = true;
                            z = true;
                        }
                        ModeImpl.this.modeState = -1;
                    }
                    if (z) {
                        ModeImpl.this.modeStateCallback.onCreateFailed(camera.getCameraId(), ModeImpl.this.modeType, mapDeviceErrorToModeError(i));
                    } else {
                        ModeImpl.this.modeStateCallback.onFatalError(ModeImpl.this, mapDeviceErrorToModeError(i));
                    }
                    ModeImpl.this.release();
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        LOGGER.debug("finalize.", new Object[0]);
        try {
            synchronized (this.interfaceLock) {
                this.callbackOnReleased = true;
            }
            release();
        } finally {
            super.finalize();
        }
    }

    @Override // ohos.media.camera.mode.Mode
    public final void release() {
        LOGGER.debug("release: modeState = %{public}d", Integer.valueOf(this.modeState));
        synchronized (this.interfaceLock) {
            if (this.modeState == 0) {
                LOGGER.debug("release: modeState is MODE_STATE_IDLE then return.", new Object[0]);
                return;
            }
            deactive();
            this.modeState = 0;
            this.controller.closeCamera(this.cameraId);
        }
    }

    public final void releaseStrong() {
        LOGGER.debug("releaseStrong cameraId: %{public}s", this.cameraId);
        synchronized (this.interfaceLock) {
            deactive();
            this.controller.closeCameraStrong(this.cameraId);
        }
    }

    public final void releaseWeak() {
        LOGGER.debug("releaseWeak", new Object[0]);
        synchronized (this.interfaceLock) {
            this.modeState = 0;
            this.controller.closeCameraWeak(this.cameraId);
        }
    }

    public final int setPreviewSurfaces(List<Surface> list) {
        synchronized (this.interfaceLock) {
            if (list != null) {
                if (!list.isEmpty()) {
                    if (list.get(0) != null) {
                        Size surfaceSize2 = SurfaceUtils.getSurfaceSize(list.get(0));
                        for (Surface surface : list) {
                            if (!surfaceSize2.equals(SurfaceUtils.getSurfaceSize(surface))) {
                                throw new IllegalArgumentException("All preview sizes must be the same!");
                            }
                        }
                        if (list.size() <= 2) {
                            if (this.isDeferred) {
                                checkModeAvailable();
                            } else {
                                checkModeOpened();
                            }
                            LOGGER.debug("setPreviewSurface: %{public}s", list);
                            if (this.isDeferred) {
                                if (this.controller != null) {
                                    this.controller.finalizeDeferredPreviewSurface(list);
                                }
                                this.isDeferred = false;
                            }
                            this.enableActions |= 1;
                            this.preview.setSurfaces(list);
                            this.previewSize = SurfaceUtils.getSurfaceSize(list.get(0));
                            this.preview.setCoordinateSurface(list.get(0));
                            int min = Math.min(list.size(), 2);
                            for (int i = 1; i < min; i++) {
                                Size surfaceSize3 = SurfaceUtils.getSurfaceSize(list.get(i));
                                if (surfaceSize3.width * surfaceSize3.height > this.previewSize.width * this.previewSize.height) {
                                    this.previewSize = surfaceSize3;
                                    this.preview.setCoordinateSurface(list.get(i));
                                }
                            }
                            LOGGER.info("setPreviewSurface: surfaceSize = %{public}s", this.previewSize);
                            return 0;
                        }
                        throw new IllegalArgumentException("Exceeded the maximum number of preview surfaces supported by this mode.");
                    }
                }
            }
            checkModeOpened();
            LOGGER.debug("setPreviewSurface: surface = null", new Object[0]);
            this.enableActions &= -2;
            this.isDeferred = false;
            return -1;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: java.lang.Class<T> */
    /* JADX WARN: Multi-variable type inference failed */
    public final <T> int setDeferredPreviewSize(Size size, Class<T> cls) {
        synchronized (this.interfaceLock) {
            checkModeAvailable();
            if (size == null) {
                LOGGER.debug("setDeferredPreviewSize: size = null", new Object[0]);
                this.enableActions &= -2;
                this.isDeferred = false;
            } else if (cls != 0) {
                LOGGER.debug("setDeferredPreviewSize: %{public}s", size);
                this.enableActions |= 1;
                this.isDeferred = true;
            } else {
                throw new IllegalArgumentException("class cannot be null");
            }
            this.surfaceSize = size;
            this.surfaceClass = cls;
            this.previewSize = size;
            if (!this.modeAbility.getSupportedPreviewSizes(SurfaceOps.class).contains(this.previewSize)) {
                if (!isMatchBackPassThrough(this.previewSize, null, null)) {
                    throw new IllegalArgumentException("Unsupported Preview Size!");
                }
            }
        }
        return 0;
    }

    @Override // ohos.media.camera.mode.Mode
    public final int setImageRotation(int i) {
        synchronized (this.interfaceLock) {
            int i2 = i % ROTATION_DEGREE_CIRCLE;
            if (i2 < 0) {
                i2 += ROTATION_DEGREE_CIRCLE;
            }
            this.imageRotation = Math.round(((float) i2) / 90.0f) * 90;
        }
        return 0;
    }

    public final int setImageSize(Size size) {
        synchronized (this.interfaceLock) {
            checkModeOpened();
            if (size == null) {
                LOGGER.info("setImageSize: size = null", new Object[0]);
                this.enableActions &= -3;
            } else {
                LOGGER.debug("setImageSize: %{public}s", size);
                this.enableActions |= 2;
            }
            this.imageSize = size;
        }
        return 0;
    }

    public final int setVideoSurface(Surface surface) {
        synchronized (this.interfaceLock) {
            checkModeOpened();
            if (surface == null) {
                LOGGER.info("setVideoSurface: surface = null", new Object[0]);
                this.enableActions &= -5;
            } else {
                LOGGER.debug("setVideoSurface: %{public}s", surface);
                this.enableActions |= 4;
            }
            this.record.setSurface(surface);
            this.videoSize = SurfaceUtils.getSurfaceSize(surface);
            LOGGER.info("setVideoSurface: surfaceSize = %{public}s", this.videoSize);
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public void startPreviewInternal() {
        if (this.preview != null) {
            checkModeActivated();
            checkPreviewActionEnable();
            this.captureCallbackManagerWrapper.removeResultHandler(this.previewCallbackFunction);
            this.previewCallbackFunction = new PreviewCallbackFunction(this.actionStateCallback);
            this.captureCallbackManagerWrapper.addResultHandler(this.previewCallbackFunction);
            if (this.record == null || this.record.getSurface() == null) {
                this.preview.startPreview(this.captureCallbackManagerWrapper.getCaptureCallbackManager(), 1, null);
                return;
            }
            this.modeTags.setVideoSize(SurfaceUtils.getSurfaceSize(this.record.getSurface()));
            this.preview.startPreview(this.captureCallbackManagerWrapper.getCaptureCallbackManager(), 3, null);
            return;
        }
        throw new IllegalStateException("Preview action is not created");
    }

    /* access modifiers changed from: protected */
    public void stopPreviewInternal() {
        if (this.preview != null) {
            checkModeAvailable();
            LOGGER.debug("isPreviewStarted = false @deactiveInternal", new Object[0]);
            this.isPreviewStarted = false;
            this.preview.stopPreview();
            return;
        }
        throw new IllegalStateException("Preview action is not created");
    }

    @Override // ohos.media.camera.mode.Mode
    public final void startPreview() {
        TRACER.startTrace("start-preview");
        synchronized (this.interfaceLock) {
            startPreviewInternal();
        }
        TRACER.finishTrace("start-preview");
    }

    @Override // ohos.media.camera.mode.Mode
    public final void stopPreview() {
        LOGGER.begin("stopPreview.");
        synchronized (this.interfaceLock) {
            stopPreviewInternal();
        }
        LOGGER.end("stopPreview.");
    }

    /* access modifiers changed from: protected */
    public void takePictureInternal(File file) {
        if (this.capture != null) {
            this.captureState = CaptureState.CAPTUREING;
            checkPreviewStarted();
            checkCaptureActionEnable();
            this.capture.setCaptureTemplateType(getCaptureTemplateType());
            this.capture.capture(file, this.actionDataCallback, this.actionStateCallback, this.imageRotation, getEffectSurfaces());
            return;
        }
        throw new IllegalStateException("Capture action is not created");
    }

    /* access modifiers changed from: protected */
    public void takePictureInternal() {
        if (this.capture != null) {
            this.captureState = CaptureState.CAPTUREING;
            checkPreviewStarted();
            checkCaptureActionEnable();
            this.capture.setCaptureTemplateType(getCaptureTemplateType());
            this.capture.capture(this.actionDataCallback, this.actionStateCallback, this.imageRotation, getEffectSurfaces());
            return;
        }
        throw new IllegalStateException("Capture action is not created");
    }

    @Override // ohos.media.camera.mode.Mode
    public final void takePicture(File file) {
        TRACER.startTrace("take-picture-file");
        synchronized (this.interfaceLock) {
            waitForParameterUpdate();
            takePictureInternal(file);
        }
        TRACER.finishTrace("take-picture-file");
    }

    @Override // ohos.media.camera.mode.Mode
    public final void takePicture() {
        TRACER.startTrace("take-picture");
        synchronized (this.interfaceLock) {
            waitForParameterUpdate();
            takePictureInternal();
        }
        TRACER.finishTrace("take-picture");
    }

    private void waitForParameterUpdate() {
        if (this.modeType == 5) {
            long currentTimeMillis = System.currentTimeMillis() - this.lastSetParameterTime;
            if (currentTimeMillis < INTERVAL_OF_SET_PARAMETER_AND_TAKE_PICTURE_MS && currentTimeMillis > 0) {
                LOGGER.debug(" waitForParameterUpdate ", new Object[0]);
                try {
                    Thread.sleep(currentTimeMillis);
                } catch (InterruptedException e) {
                    LOGGER.debug("waitForParameterUpdate sleep fail %{public}s", e.getMessage());
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void takePictureBurstInternal(File file) {
        takePictureBurstInternal();
    }

    /* access modifiers changed from: protected */
    public void takePictureBurstInternal() {
        if (this.capture == null) {
            throw new IllegalStateException("Capture action is not created");
        } else if (!CameraUtil.isFrontCamera(this.cameraAbility)) {
            checkPreviewStarted();
            this.captureState = CaptureState.Burst;
            checkCaptureActionEnable();
        } else {
            throw new IllegalStateException("Burst is not supported by front camera");
        }
    }

    @Override // ohos.media.camera.mode.Mode
    public final void takePictureBurst(File file) {
        synchronized (this.interfaceLock) {
            takePictureBurstInternal(file);
        }
    }

    @Override // ohos.media.camera.mode.Mode
    public final void takePictureBurst() {
        synchronized (this.interfaceLock) {
            takePictureBurstInternal();
        }
    }

    /* access modifiers changed from: protected */
    public void stopPictureInternal() {
        this.captureState = CaptureState.IDLE;
        if (this.capture != null) {
            this.capture.stop();
            if (this.preview != null) {
                synchronized (this.interfaceLock) {
                    if (isModeActivated()) {
                        updatePreview();
                    }
                }
            }
        }
    }

    @Override // ohos.media.camera.mode.Mode
    public final void stopPicture() {
        synchronized (this.interfaceLock) {
            checkModeAvailable();
            stopPictureInternal();
        }
    }

    /* access modifiers changed from: protected */
    public void startRecordingInternal() {
        synchronized (this.interfaceLock) {
            if (this.record != null) {
                this.recordState = RecordState.RECORDING;
                checkModeActivated();
                checkRecordActionEnable();
                this.record.start(null, this.actionStateCallback);
            } else {
                throw new IllegalStateException("Record action is not created");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void startRecordingInternal(File file) {
        throw new UnsupportedOperationException("Recording by file is not supported");
    }

    @Override // ohos.media.camera.mode.Mode
    public final void startRecording() {
        LOGGER.begin("startRecording");
        startRecordingInternal();
        LOGGER.end("startRecording");
    }

    @Override // ohos.media.camera.mode.Mode
    public final void startRecording(File file) {
        synchronized (this.interfaceLock) {
            startRecordingInternal(file);
        }
    }

    /* access modifiers changed from: protected */
    public void pauseRecordingInternal() {
        if (this.record != null) {
            this.recordState = RecordState.PAUSED;
            this.record.pause();
            LOGGER.info("pauseRecording now has empty logic", new Object[0]);
            return;
        }
        throw new IllegalStateException("Record action is not created");
    }

    @Override // ohos.media.camera.mode.Mode
    public final void pauseRecording() {
        LOGGER.begin("pauseRecording");
        synchronized (this.interfaceLock) {
            pauseRecordingInternal();
        }
        LOGGER.end("pauseRecording");
    }

    @Override // ohos.media.camera.mode.Mode
    public final void resumeRecording() {
        LOGGER.begin("resumeRecording");
        synchronized (this.interfaceLock) {
            resumeRecordingInternal();
        }
        LOGGER.end("resumeRecording");
    }

    /* access modifiers changed from: protected */
    public void resumeRecordingInternal() {
        if (this.record != null) {
            this.recordState = RecordState.RECORDING;
            this.record.resume();
            LOGGER.info("resumeRecording now has empty logic", new Object[0]);
            return;
        }
        throw new IllegalStateException("Record action is not created");
    }

    /* access modifiers changed from: protected */
    public void stopRecordingInternal() {
        if (this.record != null) {
            checkModeAvailable();
            this.record.stop();
            this.recordState = RecordState.IDLE;
            return;
        }
        throw new IllegalStateException("Record action is not created");
    }

    @Override // ohos.media.camera.mode.Mode
    public final void stopRecording() {
        LOGGER.begin("stopRecording");
        synchronized (this.interfaceLock) {
            stopRecordingInternal();
        }
        LOGGER.end("stopRecording");
    }

    @Override // ohos.media.camera.mode.Mode
    public int setFocus(int i, Rect rect) {
        return autoFocus(i, rect);
    }

    public final int autoFocus(@Metadata.FocusMode int i, Rect rect) {
        LOGGER.debug("autoFocus: modeState = %{public}d, focusMode %{public}d", Integer.valueOf(this.modeState), Integer.valueOf(i));
        synchronized (this.interfaceLock) {
            if (!isModeActivated()) {
                return -4;
            }
            return autoFocusInternal(i, rect);
        }
    }

    /* access modifiers changed from: protected */
    public int autoFocusInternal(@Metadata.FocusMode int i, Rect rect) {
        if (this.captureState == CaptureState.CAPTUREING || this.captureState == CaptureState.Burst) {
            return -4;
        }
        Camera3aManager camera3aManager2 = this.camera3aManager;
        if (camera3aManager2 != null) {
            boolean z = true;
            if (i == 1) {
                camera3aManager2.setCafFocus(rect);
            } else if (i == 2 || i == 3) {
                if (i != 2) {
                    z = false;
                }
                this.camera3aManager.setTafFocus(z, rect, this.actionStateCallback);
            } else if (i != 0) {
                return -3;
            } else {
                camera3aManager2.setMfFocus();
            }
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public int setZoomInternal(float f) {
        if (this.captureState == CaptureState.CAPTUREING || this.captureState == CaptureState.Burst) {
            return -4;
        }
        int zoomValueToTag = setZoomValueToTag(f);
        if (zoomValueToTag != 0) {
            LOGGER.warn("setZoom failed: %{public}d", Integer.valueOf(zoomValueToTag));
            return zoomValueToTag;
        }
        this.captureTriggerId = updatePreview();
        if (this.captureTriggerId == -1) {
            return -1;
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public final int setZoomValueToTag(float f) {
        try {
            float[] supportedZoom = this.modeAbility.getSupportedZoom();
            if (supportedZoom == null || supportedZoom[0] > f || supportedZoom[1] < f) {
                return -3;
            }
            int zoom = this.modeTags.setZoom(f);
            if (zoom != 0) {
                LOGGER.warn("setZoom failed: %{public}d", Integer.valueOf(zoom));
                return zoom;
            }
            this.zoomValue = f;
            return zoom;
        } catch (IllegalArgumentException unused) {
            return -2;
        }
    }

    @Override // ohos.media.camera.mode.Mode
    public final int setZoom(float f) {
        LOGGER.debug("setZoom: modeState = %{public}d", Integer.valueOf(this.modeState));
        synchronized (this.interfaceLock) {
            if (!isModeActivated()) {
                return -4;
            }
            this.lastSetParameterTime = System.currentTimeMillis();
            return setZoomInternal(f);
        }
    }

    public float getZoom() {
        return this.zoomValue;
    }

    /* access modifiers changed from: protected */
    public int setFlashModeInternal(int i) {
        if (this.captureState == CaptureState.CAPTUREING || this.captureState == CaptureState.Burst) {
            return -4;
        }
        try {
            if (!CollectionUtil.contains(this.modeAbility.getSupportedFlashMode(), i)) {
                return -3;
            }
            int flashMode = this.modeTags.setFlashMode(i);
            if (flashMode != 0) {
                LOGGER.warn("setFlashMode failed: %{public}d", Integer.valueOf(flashMode));
                return flashMode;
            }
            updatePreview();
            return 0;
        } catch (IllegalArgumentException unused) {
            return -2;
        }
    }

    @Override // ohos.media.camera.mode.Mode
    public final int setFlashMode(int i) {
        LOGGER.debug("setFlashMode: modeState = %{public}d", Integer.valueOf(this.modeState));
        synchronized (this.interfaceLock) {
            if (!isModeActivated()) {
                return -4;
            }
            return setFlashModeInternal(i);
        }
    }

    @Override // ohos.media.camera.mode.Mode
    public final int setFaceDetection(int i, boolean z) {
        LOGGER.debug("setFaceDetection: modeState = %{public}d", Integer.valueOf(this.modeState));
        synchronized (this.interfaceLock) {
            if (!isModeActivated()) {
                return -4;
            }
            if (this.recordState != RecordState.PAUSED) {
                if (this.recordState != RecordState.RECORDING) {
                    if (this.captureState != CaptureState.CAPTUREING) {
                        if (this.captureState != CaptureState.Burst) {
                            int[] supportedFaceDetection = this.modeAbility.getSupportedFaceDetection();
                            boolean contains = CollectionUtil.contains(supportedFaceDetection, 1);
                            boolean contains2 = CollectionUtil.contains(supportedFaceDetection, 2);
                            if (i != 1) {
                                if (i != 2) {
                                    LOGGER.error("face detection type NOT supported by this mode", new Object[0]);
                                    return -3;
                                } else if (!contains2) {
                                    return -2;
                                } else {
                                    this.isSmileDetectionOn = z;
                                    this.modeTags.setSmileDetection(this.isSmileDetectionOn);
                                }
                            } else if (!contains) {
                                return -2;
                            } else {
                                this.isFaceDetectionOn = z;
                            }
                            this.captureCallbackManagerWrapper.removeResultHandler(this.faceDetectionFunction);
                            this.faceDetectionFunction = new FaceDetectionFunction(this.isFaceDetectionOn, this.isSmileDetectionOn, this.previewSize, this.modeTags, this.cameraAbility);
                            this.faceDetectionFunction.setFaceDetectionCallback(this.actionStateCallback);
                            this.captureCallbackManagerWrapper.addResultHandler(this.faceDetectionFunction);
                            updatePreview();
                            this.lastSetParameterTime = System.currentTimeMillis();
                            return 0;
                        }
                    }
                    return -4;
                }
            }
            return -4;
        }
    }

    /* access modifiers changed from: protected */
    public int setBeautyModeInternal(int i, int i2) {
        if (!isModeActivated() || this.recordState == RecordState.PAUSED || this.recordState == RecordState.RECORDING || this.captureState == CaptureState.CAPTUREING || this.captureState == CaptureState.Burst) {
            return -4;
        }
        if (!(!CollectionUtil.isEmptyCollection(this.modeAbility.getSupportedBeauty(i)))) {
            return -2;
        }
        LOGGER.info("setBeauty currentFunctionStatus = %{public}s", this.currentFunctionStatus);
        if (ConflictChecker.checkBeautyConflict(ConflictChecker.ConflictParam.create(this.previewSize, this.imageSize, this.videoSize, this.modeAbility, this.currentFunctionStatus), i2)) {
            return -5;
        }
        int beauty = this.modeTags.setBeauty(i, i2);
        this.currentFunctionStatus.put(ModeCharacteristicKey.BEAUTY_FUNCTION, Boolean.valueOf(this.modeTags.isBeautyEnabled()));
        LOGGER.info("currentFunctionStatus = %{public}s", this.currentFunctionStatus);
        if (beauty != 0) {
            LOGGER.warn("setBeauty failed: type %{public}d, level %{public}d, ret %{public}d", Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(beauty));
            return -3;
        }
        updatePreview();
        return 0;
    }

    @Override // ohos.media.camera.mode.Mode
    public final int setBeauty(int i, int i2) {
        int beautyModeInternal;
        LOGGER.debug("setBeauty: modeState = %{public}d", Integer.valueOf(this.modeState));
        synchronized (this.interfaceLock) {
            this.lastSetParameterTime = System.currentTimeMillis();
            beautyModeInternal = setBeautyModeInternal(i, i2);
        }
        return beautyModeInternal;
    }

    /* access modifiers changed from: protected */
    public int setColorModeInternal(int i) {
        int colorMode = this.modeTags.setColorMode(i);
        if (colorMode != 0) {
            LOGGER.warn("setColorMode failed: %{public}d", Integer.valueOf(colorMode));
            return colorMode;
        }
        updatePreview();
        return 0;
    }

    @Override // ohos.media.camera.mode.Mode
    public final int setColorMode(int i) {
        LOGGER.debug("setColorMode: modeState = %{public}d", Integer.valueOf(this.modeState));
        synchronized (this.interfaceLock) {
            if (!isModeActivated()) {
                return -4;
            }
            if (this.recordState != RecordState.PAUSED) {
                if (this.recordState != RecordState.RECORDING) {
                    if (this.captureState != CaptureState.CAPTUREING) {
                        if (this.captureState != CaptureState.Burst) {
                            if (!CollectionUtil.contains(this.modeAbility.getSupportedColorMode(), i)) {
                                return -3;
                            }
                            this.lastSetParameterTime = System.currentTimeMillis();
                            return setColorModeInternal(i);
                        }
                    }
                    return -4;
                }
            }
            return -4;
        }
    }

    @Override // ohos.media.camera.mode.Mode
    public final int setSceneDetection(boolean z) {
        LOGGER.debug("setSceneDetection: modeState = %{public}d, status = %{public}b", Integer.valueOf(this.modeState), Boolean.valueOf(z));
        synchronized (this.interfaceLock) {
            if (!isModeActivated()) {
                return -4;
            }
            if (this.captureState != CaptureState.CAPTUREING) {
                if (this.captureState != CaptureState.Burst) {
                    if (!this.modeAbility.getSupportedSceneDetection()) {
                        return -2;
                    }
                    this.modeTags.setSceneDetection(z);
                    if (this.masterAiFunction == null) {
                        this.masterAiFunction = new MasterAiFunction(this, this.controller, this.captureCallbackManagerWrapper, this.actionStateCallback);
                    }
                    if (!z) {
                        this.masterAiFunction.setSceneDetection(false);
                        this.captureCallbackManagerWrapper.removeResultHandler(this.masterAiFunction);
                    } else {
                        this.masterAiFunction.setSceneDetection(true);
                        this.captureCallbackManagerWrapper.addResultHandler(this.masterAiFunction);
                    }
                    updatePreview();
                    this.lastSetParameterTime = System.currentTimeMillis();
                    return 0;
                }
            }
            return -4;
        }
    }

    @Override // ohos.media.camera.mode.Mode
    public final int setLocation(Location location) {
        LOGGER.debug("setLocation: modeState = %{public}d", Integer.valueOf(this.modeState));
        synchronized (this.interfaceLock) {
            if (!isModeActivated()) {
                return -4;
            }
            return setLocationInternal(location);
        }
    }

    /* access modifiers changed from: protected */
    public int setLocationInternal(Location location) {
        if (this.recordState == RecordState.PAUSED || this.recordState == RecordState.RECORDING || this.captureState == CaptureState.CAPTUREING || this.captureState == CaptureState.Burst) {
            return -4;
        }
        int location2 = this.modeTags.setLocation(location);
        if (location2 != 0) {
            LOGGER.warn("setLocation failed: %{public}d", Integer.valueOf(location2));
            return location2;
        }
        updatePreview();
        return 0;
    }

    /* access modifiers changed from: protected */
    public <T> int setParameterInternal(ParameterKey.Key<T> key, T t) {
        this.modeTags.setParameter(key, t);
        return 0;
    }

    @Override // ohos.media.camera.mode.Mode
    public <T> int setParameter(ParameterKey.Key<T> key, T t) {
        synchronized (this.interfaceLock) {
            if (key == null || t == null) {
                return -3;
            }
            LOGGER.debug("setParameter key: %{public}s, value: %{public}s", key, t);
            if (!this.modeAbility.getSupportedParameters().contains(key)) {
                LOGGER.warn("Unavailable Key : %{public}s", key);
                return -2;
            }
            if (this.captureState != CaptureState.CAPTUREING) {
                if (this.captureState != CaptureState.Burst) {
                    if (!isKeyValueValid(key, t)) {
                        return -3;
                    }
                    LOGGER.info("setParameter currentFunctionStatus = %{public}d", this.currentFunctionStatus);
                    if (ConflictChecker.checkKeyConflict(ConflictChecker.ConflictParam.create(this.previewSize, this.imageSize, this.videoSize, this.modeAbility, this.currentFunctionStatus), key, t)) {
                        return -5;
                    }
                    int parameterInternal = setParameterInternal(key, t);
                    if (parameterInternal == 0) {
                        updateFunctionStatus(key, t);
                    }
                    this.lastSetParameterTime = System.currentTimeMillis();
                    return parameterInternal;
                }
            }
            return -4;
        }
    }

    public <T> boolean isKeyValueValid(ParameterKey.Key<T> key, T t) {
        S s;
        Pair<Boolean, Pair<PropertyKey.Key<?>, ModeRequestKey.CheckValid<?>>> rangeKey = ModeRequestKey.getRangeKey(key);
        if (rangeKey == null || rangeKey.s == null || (s = rangeKey.s.s) == null) {
            return false;
        }
        return s.isValueValid(this.modeAbility.getParameterRange(key), t);
    }

    private <T> void updateFunctionStatus(ParameterKey.Key<T> key, T t) {
        PropertyKey.Key<?> mapKeyToFunction = ConflictChecker.mapKeyToFunction(key);
        if (mapKeyToFunction != null) {
            this.currentFunctionStatus.put(mapKeyToFunction, Boolean.valueOf(!t.equals(ConflictChecker.getKeyOffValue(key))));
            LOGGER.info("currentFunctionStatus = %{public}d", this.currentFunctionStatus);
        }
    }

    @Override // ohos.media.camera.mode.Mode
    public final <T> int setParameters(Map<ParameterKey.Key<T>, T> map) {
        synchronized (this.interfaceLock) {
            if (this.recordState != RecordState.PAUSED) {
                if (this.recordState != RecordState.RECORDING) {
                    if (this.captureState != CaptureState.CAPTUREING) {
                        if (this.captureState != CaptureState.Burst) {
                            this.lastSetParameterTime = System.currentTimeMillis();
                            LOGGER.warn("setParameters has not been implemented", new Object[0]);
                            return 0;
                        }
                    }
                    return -4;
                }
            }
            return -4;
        }
    }

    private void checkModeOpened() {
        if (this.modeState != 1) {
            throw new IllegalStateException("Mode is not opened");
        }
    }

    /* access modifiers changed from: protected */
    public void checkModeActivated() {
        if (this.modeState != 2) {
            throw new IllegalStateException("Mode is not activated");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isModeActivated() {
        return this.modeState == 2;
    }

    private void checkPreviewStarted() {
        checkModeActivated();
        if (!this.isPreviewStarted) {
            throw new IllegalStateException("Preview is not started, do not support other actions");
        }
    }

    private void checkModeAvailable() {
        if (this.modeState == -1) {
            throw new IllegalStateException("Current mode is unavailable");
        }
    }

    private void checkPreviewActionEnable() {
        if ((this.enableActions & 1) != 1) {
            throw new IllegalStateException("Preview action is not supported by current mode");
        }
    }

    private void checkCaptureActionEnable() {
        if ((this.enableActions & 2) != 2) {
            throw new IllegalStateException("Capture action is not supported by current mode");
        }
    }

    /* access modifiers changed from: protected */
    public void checkRecordActionEnable() {
        if ((this.enableActions & 4) != 4) {
            throw new IllegalStateException("Record action is not supported by current mode");
        }
    }

    private void configDeferredPreview(ModeConfigImpl modeConfigImpl) {
        List<Pair<Size, Class<?>>> deferredPreviewProfiles = modeConfigImpl.getDeferredPreviewProfiles();
        if (deferredPreviewProfiles != null && !deferredPreviewProfiles.isEmpty()) {
            if (deferredPreviewProfiles.size() <= 1) {
                Pair<Size, Class<?>> pair = deferredPreviewProfiles.get(0);
                setDeferredPreviewSize(pair.f, pair.s);
                return;
            }
            throw new IllegalArgumentException("Exceeded the maximum number of deferred preview surfaces");
        }
    }

    private void configSurfaces(ModeConfigImpl modeConfigImpl) {
        TRACER.startTrace("configure-mode-configSurfaces");
        if (modeConfigImpl.isDeferredPreview()) {
            configDeferredPreview(modeConfigImpl);
            modeConfigImpl.waitForDeferredSurface();
        } else {
            configPreview(modeConfigImpl);
            modeConfigImpl.finishSurfaceUpdate();
        }
        configCapture(modeConfigImpl);
        configRecording(modeConfigImpl);
        TRACER.finishTrace("configure-mode-configSurfaces");
    }

    private boolean finalizeDeferredSurface(ModeConfigImpl modeConfigImpl) {
        if (!modeConfigImpl.isWaitForDeferredSurface() || modeConfigImpl.getDeferredPreviewProfiles().size() != modeConfigImpl.getDeferredPreviewSurfaces().size()) {
            return false;
        }
        setPreviewSurfaces(modeConfigImpl.getDeferredPreviewSurfaces());
        return true;
    }

    private void configPreview(ModeConfigImpl modeConfigImpl) {
        List<Surface> list = (List) Objects.requireNonNull(modeConfigImpl.getPreviewSurfaces(), "Preview surface is not null!");
        List<Size> supportedPreviewSizes = this.modeAbility.getSupportedPreviewSizes(SurfaceOps.class);
        for (Surface surface : list) {
            Size surfaceSize2 = SurfaceUtils.getSurfaceSize(surface);
            LOGGER.debug("configPreview: set in surface size = %{public}s", surfaceSize2);
            if (!(supportedPreviewSizes.contains(surfaceSize2) || isMatchBackPassThrough(surfaceSize2, null, null))) {
                throw new IllegalArgumentException("Unsupported Preview Size!");
            }
        }
        setPreviewSurfaces(list);
    }

    private boolean isMatchBackPassThrough(Size size, Size size2, Size size3) {
        if (CameraUtil.isFrontCamera(this.cameraAbility)) {
            return false;
        }
        if (size != null && size2 != null && size3 != null) {
            for (OptimalSizeCombination optimalSizeCombination : this.backPassThroughCombinations) {
                if (size.equals(optimalSizeCombination.getPreviewSize()) && size2.equals(optimalSizeCombination.getCaptureSize()) && size3.equals(optimalSizeCombination.getVideoSize())) {
                    this.isPassThroughResolution = true;
                    return true;
                }
            }
        } else if (size != null && size2 != null) {
            for (OptimalSizeCombination optimalSizeCombination2 : this.backPassThroughCombinations) {
                if (size.equals(optimalSizeCombination2.getPreviewSize()) && size2.equals(optimalSizeCombination2.getCaptureSize())) {
                    this.isPassThroughResolution = true;
                    return true;
                }
            }
        } else if (size == null || size3 != null) {
            this.isPassThroughResolution = false;
        } else {
            for (OptimalSizeCombination optimalSizeCombination3 : this.backPassThroughCombinations) {
                if (size.equals(optimalSizeCombination3.getPreviewSize())) {
                    this.isPassThroughResolution = true;
                    return true;
                }
            }
        }
        this.isPassThroughResolution = false;
        return false;
    }

    /* access modifiers changed from: protected */
    public void configCapture(ModeConfigImpl modeConfigImpl) {
        if (modeConfigImpl == null || modeConfigImpl.getCaptureImageProfiles() == null) {
            LOGGER.error("configCapture: null pending or pending.getCaptureImageProfiles() is null", new Object[0]);
        } else if (!modeConfigImpl.getCaptureImageProfiles().isEmpty()) {
            Pair<Size, Integer> pair = modeConfigImpl.getCaptureImageProfiles().get(0);
            List<Size> supportedCaptureSizes = this.modeAbility.getSupportedCaptureSizes(pair.s.intValue());
            LOGGER.debug("configCapture: set in surface size = %{public}s, available capture sizes = %{public}s", pair.f.toString(), Arrays.toString(supportedCaptureSizes.toArray()));
            if (supportedCaptureSizes.contains(pair.f) || isMatchBackPassThrough(this.previewSize, pair.f, null)) {
                synchronized (this.interfaceLock) {
                    this.imageFormat = pair.s.intValue();
                }
                setImageSize(pair.f);
                return;
            }
            throw new IllegalArgumentException("Unsupported Capture Size!");
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0034  */
    public final void configRecordingSurface(ModeConfigImpl modeConfigImpl) {
        Objects.requireNonNull(modeConfigImpl, "mode config should not be null!");
        if (!modeConfigImpl.getVideoSurfaces().isEmpty()) {
            Surface surface = modeConfigImpl.getVideoSurfaces().get(0);
            Size surfaceSize2 = SurfaceUtils.getSurfaceSize(surface);
            for (Map.Entry<Integer, List<Size>> entry : this.modeAbility.getSupportedVideoSizes(Recorder.class).entrySet()) {
                if (CollectionUtil.contains(entry.getValue(), surfaceSize2) || isMatchBackPassThrough(this.previewSize, this.imageSize, surfaceSize2)) {
                    setVideoSurface(surface);
                    return;
                }
                while (r1.hasNext()) {
                }
            }
            throw new IllegalArgumentException("Unsupported Video Size!");
        }
    }

    /* access modifiers changed from: protected */
    public void configRecording(ModeConfigImpl modeConfigImpl) {
        Objects.requireNonNull(modeConfigImpl, "mode config should not be null!");
        if (modeConfigImpl.getVideoFps() != 30) {
            throw new UnsupportedOperationException("setVideoFps interface is only supported by SuperSlow and SlowMotion Mode!");
        } else if (CollectionUtil.isEmptyCollection(modeConfigImpl.getVideoSizes())) {
            configRecordingSurface(modeConfigImpl);
        } else {
            throw new UnsupportedOperationException("addVideoSize interface is only supported by SuperSlow Mode!");
        }
    }

    private void configCallbacks(ModeConfigImpl modeConfigImpl) {
        setDataCallback(modeConfigImpl.getDataCallback(), modeConfigImpl.getDataHandler());
        setStateCallback(modeConfigImpl.getStateCallback(), modeConfigImpl.getStateHandler());
    }

    public static class InnerActionDataCallbackImpl extends ActionDataCallbackImpl {
        private final ActionDataCallbackImpl callback;
        private final EventHandler handler;
        private ImageReaderProxy imageReaderProxy;

        InnerActionDataCallbackImpl(Mode mode, ActionDataCallbackImpl actionDataCallbackImpl, EventHandler eventHandler) {
            super(mode, actionDataCallbackImpl, eventHandler);
            this.callback = actionDataCallbackImpl;
            this.handler = eventHandler;
        }

        @Override // ohos.media.camera.mode.impl.ActionDataCallbackImpl
        public void onThumbnailAvailable(@ActionDataCallback.Type int i, Size size, byte[] bArr) {
            ActionDataCallbackImpl actionDataCallbackImpl = this.callback;
            if (actionDataCallbackImpl != null && this.handler != null) {
                actionDataCallbackImpl.onThumbnailAvailable(i, size, bArr);
            }
        }

        @Override // ohos.media.camera.mode.impl.ActionDataCallbackImpl
        public void onImageAvailable(@ActionDataCallback.Type int i, Image image) {
            ActionDataCallbackImpl actionDataCallbackImpl = this.callback;
            if (actionDataCallbackImpl != null && this.handler != null) {
                actionDataCallbackImpl.onImageAvailable(i, image);
                this.handler.postTask(new Runnable(image) {
                    /* class ohos.media.camera.mode.impl.$$Lambda$ModeImpl$InnerActionDataCallbackImpl$tPUWkm0v6Ceskg_JXiT4DVlIQA */
                    private final /* synthetic */ Image f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        ModeImpl.InnerActionDataCallbackImpl.this.lambda$onImageAvailable$0$ModeImpl$InnerActionDataCallbackImpl(this.f$1);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onImageAvailable$0$ModeImpl$InnerActionDataCallbackImpl(Image image) {
            image.release();
            ImageReaderProxy imageReaderProxy2 = this.imageReaderProxy;
            if (imageReaderProxy2 != null) {
                imageReaderProxy2.onImageClosed(image);
            }
        }

        public void setImageReaderProxy(ImageReaderProxy imageReaderProxy2) {
            this.imageReaderProxy = imageReaderProxy2;
        }
    }

    public class InnerActionStateCallbackImpl extends ActionStateCallbackImpl {
        private final ActionStateCallbackImpl callback;

        InnerActionStateCallbackImpl(Mode mode, ActionStateCallbackImpl actionStateCallbackImpl, EventHandler eventHandler) {
            super(mode, actionStateCallbackImpl, eventHandler);
            this.callback = actionStateCallbackImpl;
        }

        @Override // ohos.media.camera.mode.impl.ActionStateCallbackImpl
        public void onPreview(int i, PreviewResult previewResult) {
            if (i == 1) {
                synchronized (ModeImpl.this.interfaceLock) {
                    ModeImpl.this.isPreviewStarted = true;
                    ModeImpl.this.updatePreview();
                    if (ModeImpl.this.modeType != 2) {
                        ModeImpl.this.autoFocus(1, null);
                    }
                }
            }
            ActionStateCallbackImpl actionStateCallbackImpl = this.callback;
            if (actionStateCallbackImpl != null) {
                actionStateCallbackImpl.onPreview(i, previewResult);
            }
        }

        @Override // ohos.media.camera.mode.impl.ActionStateCallbackImpl
        public void onTakePicture(int i, TakePictureResult takePictureResult) {
            ActionStateCallbackImpl actionStateCallbackImpl = this.callback;
            if (actionStateCallbackImpl != null) {
                actionStateCallbackImpl.onTakePicture(i, takePictureResult);
            }
            synchronized (ModeImpl.this.interfaceLock) {
                if (i == 5 || i < 0) {
                    ModeImpl.this.captureState = CaptureState.IDLE;
                    ModeImpl.this.autoFocus(1, null);
                    if (ModeImpl.this.masterAiFunction != null) {
                        ModeImpl.this.masterAiFunction.confirmMode();
                    }
                }
            }
        }

        @Override // ohos.media.camera.mode.impl.ActionStateCallbackImpl
        public void onBurst(int i, BurstResult burstResult) {
            ActionStateCallbackImpl actionStateCallbackImpl = this.callback;
            if (actionStateCallbackImpl != null) {
                actionStateCallbackImpl.onBurst(i, burstResult);
            }
            synchronized (ModeImpl.this.interfaceLock) {
                if (i == 3 || i < 0) {
                    ModeImpl.this.captureState = CaptureState.IDLE;
                }
            }
        }

        @Override // ohos.media.camera.mode.impl.ActionStateCallbackImpl
        public void onRecording(int i, RecordingResult recordingResult) {
            ActionStateCallbackImpl actionStateCallbackImpl = this.callback;
            if (actionStateCallbackImpl != null) {
                actionStateCallbackImpl.onRecording(i, recordingResult);
            }
        }

        @Override // ohos.media.camera.mode.impl.ActionStateCallbackImpl
        public void onFocus(int i, FocusResult focusResult) {
            ActionStateCallbackImpl actionStateCallbackImpl = this.callback;
            if (actionStateCallbackImpl != null) {
                actionStateCallbackImpl.onFocus(i, focusResult);
            }
        }

        @Override // ohos.media.camera.mode.impl.ActionStateCallbackImpl
        public void onFaceDetection(int i, FaceDetectionResult faceDetectionResult) {
            ActionStateCallbackImpl actionStateCallbackImpl = this.callback;
            if (actionStateCallbackImpl != null) {
                actionStateCallbackImpl.onFaceDetection(i, faceDetectionResult);
            }
        }

        @Override // ohos.media.camera.mode.impl.ActionStateCallbackImpl
        public void onSceneDetection(int i, SceneDetectionResult sceneDetectionResult) {
            ActionStateCallbackImpl actionStateCallbackImpl = this.callback;
            if (actionStateCallbackImpl != null) {
                actionStateCallbackImpl.onSceneDetection(i, sceneDetectionResult);
            }
        }

        @Override // ohos.media.camera.mode.impl.ActionStateCallbackImpl
        public void onParameters(int i, ParametersResult parametersResult) {
            ActionStateCallbackImpl actionStateCallbackImpl = this.callback;
            if (actionStateCallbackImpl != null) {
                actionStateCallbackImpl.onParameters(i, parametersResult);
            }
        }
    }
}
