package ohos.media.camera.mode.function.masterai;

import java.util.HashMap;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.device.FrameResult;
import ohos.media.camera.mode.controller.CameraController;
import ohos.media.camera.mode.function.CaptureCallbackManagerWrapper;
import ohos.media.camera.mode.function.ResultHandler;
import ohos.media.camera.mode.impl.ActionStateCallbackImpl;
import ohos.media.camera.mode.impl.ModeImpl;
import ohos.media.camera.params.ParametersResult;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.camera.params.adapter.InnerResultKey;
import ohos.media.camera.params.impl.SceneDetectionResultImpl;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class MasterAiFunction extends ResultHandler {
    private static final float AUTO_SWITCH_TO_WIDE_ANGLE_VALUE = 0.99f;
    private static final int COMPOSITION_ASSISTANT_MODE_MASK = 16711680;
    private static final float DEFAULT_CONFIDENCE_VALUE = 1.0f;
    private static final float DEFAULT_ZOOM_VALUE = 1.0f;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(MasterAiFunction.class);
    private static final int SMART_SUGGEST_MODE_MASK = 255;
    private final ActionStateCallbackImpl actionStateCallback;
    private final ModeImpl baseMode;
    private final CameraController cameraController;
    private final CaptureCallbackManagerWrapper captureCallbackManagerWrapper;
    private int highStatus = 0;
    private boolean isSceneDetectionOn;
    private int lowStatus = 0;

    private int getNewHighStatus(int i) {
        return 16711680 & i;
    }

    private int getNewLowStatus(int i) {
        int i2 = i & 255;
        if (i2 == 17) {
            i2 = 0;
        }
        if (i2 == 117) {
            return 1;
        }
        return i2;
    }

    public MasterAiFunction(ModeImpl modeImpl, CameraController cameraController2, CaptureCallbackManagerWrapper captureCallbackManagerWrapper2, ActionStateCallbackImpl actionStateCallbackImpl) {
        this.baseMode = modeImpl;
        this.cameraController = cameraController2;
        this.captureCallbackManagerWrapper = captureCallbackManagerWrapper2;
        this.actionStateCallback = actionStateCallbackImpl;
        this.handlerName = MasterAiFunction.class.getSimpleName();
    }

    public void setSceneDetection(boolean z) {
        if (this.isSceneDetectionOn != z) {
            this.highStatus = 0;
            this.lowStatus = 0;
            this.isSceneDetectionOn = z;
        }
    }

    public void enterMode() {
        FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
        if (frameConfigBuilder != null) {
            if (needSwitchToMacro(this.highStatus, this.baseMode.getZoom())) {
                this.baseMode.setZoom(AUTO_SWITCH_TO_WIDE_ANGLE_VALUE);
            } else if (needSwitchOutMacro(this.baseMode.getZoom())) {
                this.baseMode.setZoom(1.0f);
            } else {
                LOGGER.debug("not macro mode", new Object[0]);
            }
            frameConfigBuilder.setParameter(InnerParameterKey.MASTER_AI_ENTER_MODE, Integer.valueOf(this.lowStatus));
            if (this.lowStatus == 7) {
                frameConfigBuilder.setParameter(InnerParameterKey.NICE_FOOD_MODE, (byte) 1);
            } else {
                frameConfigBuilder.setParameter(InnerParameterKey.NICE_FOOD_MODE, (byte) 0);
            }
            updatePreview();
        }
    }

    public void confirmMode() {
        FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
        if (frameConfigBuilder != null) {
            frameConfigBuilder.setParameter(InnerParameterKey.SMART_SUGGEST_CONFIRM, Integer.valueOf(this.lowStatus));
            updatePreview();
        }
    }

    public void dismissMode() {
        FrameConfig.Builder frameConfigBuilder = this.cameraController.getFrameConfigBuilder();
        if (frameConfigBuilder != null) {
            frameConfigBuilder.setParameter(InnerParameterKey.SMART_SUGGEST_DISMISS, Integer.valueOf(this.lowStatus));
            updatePreview();
        }
    }

    private boolean needSwitchToMacro(int i, float f) {
        return i == 262144 && ((double) Math.abs(f - 1.0f)) <= 1.0E-5d;
    }

    private boolean needSwitchOutMacro(float f) {
        if (this.highStatus == 262144 && ((double) Math.abs(f - AUTO_SWITCH_TO_WIDE_ANGLE_VALUE)) <= 1.0E-5d) {
            return true;
        }
        return false;
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
        if (!hasSmartAssistantTrigger(builder.build())) {
            return false;
        }
        LOGGER.debug("set SmartAssistantTrigger success", new Object[0]);
        builder.setParameter(InnerParameterKey.SMART_SUGGEST_RECORD_CLEAR, null);
        builder.setParameter(InnerParameterKey.SMART_SUGGEST_EXIT_MODE, null);
        builder.setParameter(InnerParameterKey.SMART_SUGGEST_CONFIRM, null);
        builder.setParameter(InnerParameterKey.SMART_SUGGEST_DISMISS, null);
        return true;
    }

    private boolean hasSmartAssistantTrigger(FrameConfig frameConfig) {
        if (frameConfig == null) {
            return false;
        }
        if (frameConfig.get(InnerParameterKey.SMART_SUGGEST_EXIT_MODE) == null && frameConfig.get(InnerParameterKey.SMART_SUGGEST_RECORD_CLEAR) == null && frameConfig.get(InnerParameterKey.SMART_SUGGEST_CONFIRM) == null && frameConfig.get(InnerParameterKey.SMART_SUGGEST_DISMISS) == null) {
            return false;
        }
        return true;
    }

    @Override // ohos.media.camera.mode.function.ResultHandler
    public void handleResult(FrameConfig frameConfig, FrameResult frameResult) {
        boolean z = false;
        if (frameResult == null) {
            LOGGER.error("handleResult: null result!", new Object[0]);
            return;
        }
        ParametersResult parametersResult = frameResult.getParametersResult();
        if (parametersResult == null) {
            LOGGER.error("handleResult: parametersResult is null.", new Object[0]);
            return;
        }
        Integer num = (Integer) parametersResult.getResultValue(InnerResultKey.SMART_SUGGEST_HINT);
        if (num != null && this.actionStateCallback != null) {
            int newHighStatus = getNewHighStatus(num.intValue());
            int newLowStatus = getNewLowStatus(num.intValue());
            if (newHighStatus != this.highStatus || newLowStatus != this.lowStatus) {
                LOGGER.debug("new highStatus %{public}d, newLowStatus %{public}d", Integer.valueOf(newHighStatus), Integer.valueOf(newLowStatus));
                this.highStatus = newHighStatus;
                this.lowStatus = newLowStatus;
                HashMap hashMap = new HashMap();
                int i = this.highStatus;
                if (i != 0) {
                    hashMap.put(Integer.valueOf(i), Float.valueOf(1.0f));
                }
                if (this.highStatus != 0 && this.lowStatus == 0) {
                    z = true;
                }
                if (!z) {
                    hashMap.put(Integer.valueOf(this.lowStatus), Float.valueOf(1.0f));
                }
                this.actionStateCallback.onSceneDetection(1, new SceneDetectionResultImpl(1, hashMap));
            }
        }
    }
}
