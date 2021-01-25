package ohos.media.camera.mode.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import ohos.agp.graphics.Surface;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.Camera;
import ohos.media.camera.device.CameraConfig;
import ohos.media.camera.device.CameraStateCallback;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.device.FrameStateCallback;
import ohos.media.camera.device.impl.CameraImpl;
import ohos.media.camera.mode.ModeStateCallback;
import ohos.media.camera.mode.function.CaptureCallbackManagerWrapper;
import ohos.media.camera.mode.utils.CameraDeviceHelper;
import ohos.media.camera.mode.utils.CollectionUtil;
import ohos.media.camera.mode.utils.StringUtil;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.camera.zidl.StreamConfiguration;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public final class CameraController {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CameraController.class);
    private Camera camera;
    private CaptureCallbackManagerWrapper captureCallbackManager;
    private EventHandler deviceOperateHandler;
    private DeviceStateCallback deviceStateCallback;
    private FrameConfig.Builder frameConfigBuilder;
    private boolean isBurstRequest;
    private volatile boolean isDeferredConfig;
    private ModeStateCallback modeStateCallback;
    private int modeType;
    private final List<StreamConfiguration> outputConfigurations = new ArrayList();
    private StreamConfiguration previewOutputConfiguration;
    private final List<Surface> previewSurfaces = new CopyOnWriteArrayList();
    private int previewTemplateType;
    private EventHandler stateCallbackHandler;

    public CameraController(EventHandler eventHandler, EventHandler eventHandler2, ModeStateCallback modeStateCallback2, CaptureCallbackManagerWrapper captureCallbackManagerWrapper) {
        LOGGER.debug("CameraController: ", new Object[0]);
        this.deviceOperateHandler = eventHandler;
        this.stateCallbackHandler = eventHandler2;
        this.modeStateCallback = modeStateCallback2;
        this.captureCallbackManager = captureCallbackManagerWrapper;
    }

    public CameraController(Camera camera2) {
        LOGGER.debug("CameraController: ", new Object[0]);
        this.camera = camera2;
    }

    public void prepare(String str) {
        LOGGER.debug("prepare: %{public}s", str);
        if (this.deviceStateCallback == null) {
            this.deviceStateCallback = new DeviceStateCallback();
        }
        Camera camera2 = CameraDeviceHelper.getCamera(str, this.deviceStateCallback);
        if (camera2 != null) {
            this.camera = camera2;
        }
    }

    public void openCamera(String str, CameraStateCallback cameraStateCallback) {
        if (StringUtil.isEmptyString(str)) {
            LOGGER.error("openCamera: null cameraId!", new Object[0]);
            return;
        }
        LOGGER.debug("openCamera: %{public}s", str);
        if (this.deviceStateCallback == null) {
            this.deviceStateCallback = new DeviceStateCallback();
        }
        this.deviceStateCallback.setCallback(cameraStateCallback);
        Camera camera2 = this.camera;
        if (camera2 == null || !camera2.getCameraId().equals(str)) {
            CameraDeviceHelper.openCamera(str, this.deviceStateCallback, this.deviceOperateHandler);
        } else {
            this.deviceOperateHandler.postTask(new Runnable() {
                /* class ohos.media.camera.mode.controller.$$Lambda$CameraController$OCsMXKFzC6yhXdh8BoKTT61vbI0 */

                @Override // java.lang.Runnable
                public final void run() {
                    CameraController.this.lambda$openCamera$0$CameraController();
                }
            });
        }
    }

    public /* synthetic */ void lambda$openCamera$0$CameraController() {
        this.deviceStateCallback.onCreated(this.camera);
    }

    public void closeCamera(String str) {
        if (StringUtil.isEmptyString(str)) {
            LOGGER.error("closeCamera: null cameraId!", new Object[0]);
            return;
        }
        LOGGER.debug("closeCamera: %{public}s", str);
        if (this.camera.getCameraId().equals(str)) {
            CameraDeviceHelper.closeCamera(str);
        } else {
            LOGGER.error("closeCamera unknown cameraId %{public}s", str);
        }
    }

    public void closeCameraStrong(String str) {
        if (StringUtil.isEmptyString(str)) {
            LOGGER.warn("closeCameraStrong: cameraId is null!", new Object[0]);
            return;
        }
        LOGGER.debug("closeCameraStrong: %{public}s", str);
        Camera camera2 = this.camera;
        if (camera2 == null || !camera2.getCameraId().equals(str)) {
            LOGGER.warn("closeCameraStrong unknown cameraId %{public}s", str);
        } else {
            CameraDeviceHelper.closeCameraStrong(str);
        }
    }

    public void closeCameraWeak(String str) {
        if (StringUtil.isEmptyString(str)) {
            LOGGER.warn("closeCameraWeak: cameraId is null", new Object[0]);
            return;
        }
        LOGGER.debug("closeCameraWeak: %{public}s", str);
        if (!this.camera.getCameraId().equals(str)) {
            LOGGER.warn("closeCameraWeak: unknown cameraId %{public}s", str);
        } else if (CameraDeviceHelper.closeCameraWeak(str)) {
            this.deviceOperateHandler.postTask(new Runnable() {
                /* class ohos.media.camera.mode.controller.$$Lambda$CameraController$gw4mnltocu2nfdC4PJenQeSkeQM */

                @Override // java.lang.Runnable
                public final void run() {
                    CameraController.this.lambda$closeCameraWeak$1$CameraController();
                }
            });
        }
    }

    public /* synthetic */ void lambda$closeCameraWeak$1$CameraController() {
        this.deviceStateCallback.onReleased(this.camera);
        this.camera = null;
    }

    public void configure(List<Surface> list) {
        if (CollectionUtil.isEmptyCollection(list)) {
            LOGGER.warn("configure: surfaces should not be null!", new Object[0]);
            return;
        }
        this.previewSurfaces.clear();
        this.previewSurfaces.addAll(list);
        CameraConfig.Builder cameraConfigBuilder = this.camera.getCameraConfigBuilder();
        for (Surface surface : list) {
            cameraConfigBuilder.addSurface(surface);
        }
        cameraConfigBuilder.setFrameStateCallback(this.captureCallbackManager.getCaptureCallbackManager(), this.stateCallbackHandler);
        FrameConfig.Builder frameConfigBuilder2 = this.camera.getFrameConfigBuilder(this.previewTemplateType);
        if (frameConfigBuilder2 == null) {
            LOGGER.warn("configure: frameConfigBuilder is null.", new Object[0]);
            return;
        }
        frameConfigBuilder2.setParameter(InnerParameterKey.CAMERA_SCENE_MODE, Integer.valueOf(getSceneByMode()));
        this.camera.configure(cameraConfigBuilder.build());
    }

    public void configure(List<Surface> list, EventHandler eventHandler) {
        if (list == null || eventHandler == null) {
            LOGGER.warn("createSession: null surfaces or null callback or null givenHandler!", new Object[0]);
            return;
        }
        LOGGER.debug("bind: createCaptureSession", new Object[0]);
        CameraConfig.Builder cameraConfigBuilder = this.camera.getCameraConfigBuilder();
        for (Surface surface : list) {
            cameraConfigBuilder.addSurface(surface);
        }
        cameraConfigBuilder.setFrameStateCallback(this.captureCallbackManager.getCaptureCallbackManager(), this.stateCallbackHandler);
        this.camera.configure(cameraConfigBuilder.build());
    }

    public <T> void configure(Size size, Class<T> cls, List<Surface> list) {
        if (size == null) {
            LOGGER.warn("the input value surfaceSize is null", new Object[0]);
        } else if (cls == null) {
            LOGGER.warn("the input value clazz is null", new Object[0]);
        } else if (CollectionUtil.isEmptyCollection(list)) {
            LOGGER.warn("the input value surfaces is null or it's size is zero", new Object[0]);
        } else {
            this.isDeferredConfig = true;
            CameraConfig.Builder cameraConfigBuilder = this.camera.getCameraConfigBuilder();
            cameraConfigBuilder.addDeferredSurfaceSize(size, cls);
            for (Surface surface : list) {
                cameraConfigBuilder.addSurface(surface);
            }
            cameraConfigBuilder.setFrameStateCallback(this.captureCallbackManager.getCaptureCallbackManager(), this.stateCallbackHandler);
            LOGGER.debug("bind: createCaptureSessionByOutputConfigurations!", new Object[0]);
            this.camera.configure(cameraConfigBuilder.build());
        }
    }

    public void setModeType(int i) {
        LOGGER.info("setModeType %{public}d", Integer.valueOf(i));
        this.modeType = i;
    }

    public void setPreviewTemplateType(int i) {
        LOGGER.info("setPreviewTemplateType %{public}d", Integer.valueOf(i));
        this.previewTemplateType = i;
    }

    private int getSceneByMode() {
        int i = 33;
        switch (this.modeType) {
            case 1:
            default:
                i = 0;
                break;
            case 2:
                i = 19;
                break;
            case 3:
                i = 5;
                break;
            case 4:
                i = 23;
                break;
            case 5:
                i = 28;
                break;
            case 6:
                i = 7;
                break;
            case 7:
            case 8:
                break;
            case 9:
                i = 2;
                break;
            case 10:
                i = 31;
                break;
        }
        LOGGER.info("getSceneByMode modeType = %{public}d, scene = %{public}d", Integer.valueOf(this.modeType), Integer.valueOf(i));
        return i;
    }

    public int finalizeDeferredPreviewSurface(List<Surface> list) {
        if (list == null || list.isEmpty() || list.get(0) == null) {
            LOGGER.warn("surface should not be null!", new Object[0]);
            return -1;
        }
        this.isDeferredConfig = true;
        this.previewSurfaces.clear();
        this.previewSurfaces.addAll(list);
        return 0;
    }

    public void destroy() {
        try {
            LOGGER.begin("destroy");
            flush();
        } finally {
            this.frameConfigBuilder = null;
            if (this.isDeferredConfig) {
                this.previewSurfaces.clear();
                this.previewOutputConfiguration = null;
                this.outputConfigurations.clear();
                this.isDeferredConfig = false;
            }
            LOGGER.end("destroy");
        }
    }

    private void flush() {
        Camera camera2 = this.camera;
        if (camera2 instanceof CameraImpl) {
            if (((CameraImpl) camera2).isLoopingCaptureStarted()) {
                this.camera.stopLoopingCapture();
            }
            this.camera.flushCaptures();
        }
    }

    public FrameConfig.Builder createCaptureRequest(int i) {
        Camera camera2 = this.camera;
        if (camera2 == null) {
            return null;
        }
        try {
            return camera2.getFrameConfigBuilder(i);
        } catch (IllegalStateException e) {
            LOGGER.error("IllegalStateException when capture %{public}s", e.getMessage());
            return null;
        }
    }

    public int capture(FrameConfig frameConfig, FrameStateCallback frameStateCallback, EventHandler eventHandler) {
        LOGGER.debug("capture: ", new Object[0]);
        try {
            if (!(this.camera instanceof CameraImpl)) {
                return -1;
            }
            CameraImpl cameraImpl = (CameraImpl) this.camera;
            cameraImpl.setFrameStateCallback(frameStateCallback);
            return cameraImpl.triggerSingleCapture(frameConfig);
        } catch (IllegalStateException e) {
            LOGGER.error("IllegalStateException when capture %{public}s", e.getMessage());
            return -1;
        }
    }

    public int captureBurst(List<FrameConfig> list, FrameStateCallback frameStateCallback) {
        try {
            if (!(this.camera instanceof CameraImpl)) {
                return -1;
            }
            CameraImpl cameraImpl = (CameraImpl) this.camera;
            cameraImpl.setFrameStateCallback(frameStateCallback);
            return cameraImpl.triggerMultiCapture(list);
        } catch (IllegalStateException e) {
            LOGGER.error("IllegalStateException when captureBurst %{public}s", e.getMessage());
            return -1;
        }
    }

    public int setRepeatingRequest(FrameConfig.Builder builder, FrameStateCallback frameStateCallback, EventHandler eventHandler) {
        try {
            if (!(this.camera instanceof CameraImpl)) {
                return -1;
            }
            this.isBurstRequest = false;
            this.frameConfigBuilder = builder;
            CameraImpl cameraImpl = (CameraImpl) this.camera;
            cameraImpl.setFrameStateCallback(frameStateCallback);
            return cameraImpl.triggerLoopingCapture(this.frameConfigBuilder.build());
        } catch (IllegalStateException e) {
            LOGGER.error("IllegalStateException when setRepeatingRequest %{public}s", e.getMessage());
            return -1;
        }
    }

    public int setRepeatingBurst(List<FrameConfig> list, FrameStateCallback frameStateCallback, EventHandler eventHandler) {
        try {
            if (!(this.camera instanceof CameraImpl)) {
                return -1;
            }
            this.isBurstRequest = true;
            CameraImpl cameraImpl = (CameraImpl) this.camera;
            cameraImpl.setFrameStateCallback(frameStateCallback);
            return cameraImpl.triggerMultiCapture(list);
        } catch (IllegalStateException e) {
            LOGGER.error("IllegalStateException when setRepeatingBurst %{public}s", e.getMessage());
            return -1;
        }
    }

    public int setRepeatingBurst(FrameConfig.Builder builder, FrameStateCallback frameStateCallback, EventHandler eventHandler) {
        if (builder == null) {
            LOGGER.error("setRepeatingBurst: null captureRequest!", new Object[0]);
            return -1;
        }
        this.frameConfigBuilder = builder;
        return setRepeatingBurst(getBurstRequestList(builder.build()), frameStateCallback, eventHandler);
    }

    public List<FrameConfig> getBurstRequestList(FrameConfig frameConfig) {
        if (frameConfig == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(frameConfig);
    }

    public int setPreviewRequest(FrameConfig.Builder builder, FrameStateCallback frameStateCallback, EventHandler eventHandler) {
        if (this.isBurstRequest) {
            return setRepeatingBurst(builder, frameStateCallback, eventHandler);
        }
        return setRepeatingRequest(builder, frameStateCallback, eventHandler);
    }

    public FrameConfig.Builder getFrameConfigBuilder() {
        return this.frameConfigBuilder;
    }

    public void stopRepeating() {
        LOGGER.debug("stopRepeating.", new Object[0]);
        Camera camera2 = this.camera;
        if (camera2 == null) {
            LOGGER.error("stopRepeating: captureSession is null", new Object[0]);
        } else {
            camera2.stopLoopingCapture();
        }
    }

    private class DeviceStateCallback extends CameraStateCallback {
        private CameraStateCallback callback;

        DeviceStateCallback() {
        }

        public void setCallback(CameraStateCallback cameraStateCallback) {
            this.callback = cameraStateCallback;
        }

        @Override // ohos.media.camera.device.CameraStateCallback
        public void onCreated(Camera camera) {
            CameraController.LOGGER.debug("DeviceStateCallback onCreated", new Object[0]);
            if (camera == null) {
                CameraController.LOGGER.debug("onOpened with a null camera device", new Object[0]);
                return;
            }
            CameraController.this.camera = camera;
            CameraStateCallback cameraStateCallback = this.callback;
            if (cameraStateCallback != null) {
                cameraStateCallback.onCreated(camera);
            }
        }

        @Override // ohos.media.camera.device.CameraStateCallback
        public void onReleased(Camera camera) {
            CameraController.LOGGER.debug("DeviceStateCallback onReleased", new Object[0]);
            if (camera == null) {
                CameraController.LOGGER.debug("DeviceStateCallback onReleased with a null camera device", new Object[0]);
            } else if (CameraController.this.camera != null && CameraController.this.camera.getCameraId().equals(camera.getCameraId())) {
                CameraStateCallback cameraStateCallback = this.callback;
                if (cameraStateCallback != null) {
                    cameraStateCallback.onReleased(camera);
                }
                CameraController.this.camera = null;
            }
        }

        @Override // ohos.media.camera.device.CameraStateCallback
        public void onCreateFailed(String str, int i) {
            CameraController.LOGGER.debug("DeviceStateCallback onCreateFailed, errorCode: %{public}d", Integer.valueOf(i));
            CameraStateCallback cameraStateCallback = this.callback;
            if (cameraStateCallback != null) {
                cameraStateCallback.onCreateFailed(str, i);
            }
        }

        @Override // ohos.media.camera.device.CameraStateCallback
        public void onConfigured(Camera camera) {
            CameraController.LOGGER.debug("DeviceStateCallback onConfigured", new Object[0]);
            CameraStateCallback cameraStateCallback = this.callback;
            if (cameraStateCallback != null) {
                cameraStateCallback.onConfigured(camera);
            }
        }

        @Override // ohos.media.camera.device.CameraStateCallback
        public void onConfigureFailed(Camera camera, int i) {
            CameraController.LOGGER.debug("DeviceStateCallback onConfigureFailed, errorCode: %{public}d", Integer.valueOf(i));
            CameraStateCallback cameraStateCallback = this.callback;
            if (cameraStateCallback != null) {
                cameraStateCallback.onConfigureFailed(camera, i);
            }
        }

        @Override // ohos.media.camera.device.CameraStateCallback
        public void onFatalError(Camera camera, int i) {
            CameraController.LOGGER.debug("DeviceStateCallback onFatalError, errorCode: %{public}d", Integer.valueOf(i));
            CameraStateCallback cameraStateCallback = this.callback;
            if (cameraStateCallback == null) {
                CameraController.LOGGER.error("DeviceStateCallback onFatalError with a null callback", new Object[0]);
            } else {
                cameraStateCallback.onFatalError(camera, i);
            }
        }
    }
}
