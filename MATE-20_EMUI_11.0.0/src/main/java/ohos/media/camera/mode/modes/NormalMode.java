package ohos.media.camera.mode.modes;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.device.FrameResult;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.ModeStateCallback;
import ohos.media.camera.mode.adapter.key.ModeCharacteristicKey;
import ohos.media.camera.mode.adapter.key.ModeResultKey;
import ohos.media.camera.mode.function.ResultHandler;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.impl.ModeImpl;
import ohos.media.camera.mode.tags.hisi.HisiNormalTags;
import ohos.media.camera.mode.utils.CameraManagerHelper;
import ohos.media.camera.mode.utils.DeviceUtil;
import ohos.media.camera.mode.utils.ModeAbilityHelper;
import ohos.media.camera.mode.utils.ModeNameUtil;
import ohos.media.camera.mode.utils.PhotoResolutionUtil;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.ParametersResult;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.camera.params.adapter.InnerResultKey;
import ohos.media.camera.params.impl.ParametersResultImpl;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class NormalMode extends ModeImpl {
    private static final int HUAWEI_SMART_SUGGEST_MODE_MASK = 255;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(NormalMode.class);
    private static final String MODE_NAME = ModeNameUtil.getModeNameById(1);
    private static final int[] SUPPORTED_ACTIONS = {2, 1, 3};
    private static final List<PropertyKey.Key<?>> SUPPORTED_FUNCTIONS = Arrays.asList(ModeCharacteristicKey.FACE_DETECTION_FUNCTION, ModeCharacteristicKey.SMILE_DETECTION_FUNCTION, ModeCharacteristicKey.ZOOM_FUNCTION, ModeCharacteristicKey.SENSOR_HDR_FUNCTION, ModeCharacteristicKey.FLASH_MODE_FUNCTION, ModeCharacteristicKey.COLOR_MODE_FUNCTION, ModeCharacteristicKey.WATER_MARK_FUNCTION, ModeCharacteristicKey.MIRROR_FUNCTION, ModeCharacteristicKey.LOCATION_FUNCTION, ModeCharacteristicKey.SMART_CAPTURE_FUNCTION, ModeCharacteristicKey.SCENE_DETECTION_FUNCTION, ModeCharacteristicKey.FILTER_EFFECT_FUNCTION);
    private final HisiNormalTags hisiNormalTags;
    private boolean isSmartCaptureEnabled;

    public static boolean isAvailable(CameraAbilityImpl cameraAbilityImpl) {
        return true;
    }

    public NormalMode(String str, ModeStateCallback modeStateCallback, EventHandler eventHandler) {
        super(str, modeStateCallback, eventHandler);
        this.preCapture = this.preCaptureManager.getPreCapture(0);
        this.modeType = 1;
        CameraAbilityImpl cameraAbility = CameraManagerHelper.getCameraManager().getCameraAbility(str);
        this.modeAbility = getModeAbility(cameraAbility);
        this.hisiNormalTags = new HisiNormalTags(cameraAbility, 0);
        this.modeTags = this.hisiNormalTags;
        this.preCaptureManager.setModeTags(this.modeTags);
        if (!this.isPostProcess) {
            this.hisiNormalTags.enableForegroundProcess();
        }
        createAction(SUPPORTED_ACTIONS);
        this.captureCallbackManagerWrapper.addResultHandler(new ModeResultHandler());
    }

    public static int[] getSupportedActions() {
        return (int[]) SUPPORTED_ACTIONS.clone();
    }

    public static Map<Integer, List<Size>> getFormatOutputSizes(CameraAbilityImpl cameraAbilityImpl) {
        Object obj;
        HashMap hashMap = new HashMap(ModeImpl.SUPPORTED_FORMATS.size());
        for (Integer num : ModeImpl.SUPPORTED_FORMATS) {
            int intValue = num.intValue();
            if (intValue == 3) {
                obj = PhotoResolutionUtil.getCaptureSupports(cameraAbilityImpl, 1, DeviceUtil.getMaxScreenRatio());
            } else {
                obj = new ArrayList();
            }
            hashMap.put(Integer.valueOf(intValue), obj);
        }
        return hashMap;
    }

    public static ModeAbilityImpl getModeAbility(CameraAbilityImpl cameraAbilityImpl) {
        ModeAbilityImpl createModeAbility = ModeAbilityHelper.createModeAbility(1, SUPPORTED_ACTIONS, SUPPORTED_FUNCTIONS, cameraAbilityImpl);
        createModeAbility.setMaxPreviewSurfaceNumber(2);
        return createModeAbility;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.media.camera.mode.impl.ModeImpl
    public void takePictureBurstInternal() {
        super.takePictureBurstInternal();
        this.hisiNormalTags.enableBurst(true);
        if (this.capture != null && this.preview != null) {
            this.capture.setCaptureTemplateType(getCaptureTemplateType());
            this.capture.captureBurst(this.actionDataCallback, this.actionStateCallback, this.imageRotation, getEffectSurfaces());
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.media.camera.mode.impl.ModeImpl
    public void takePictureBurstInternal(File file) {
        super.takePictureBurstInternal(file);
        this.hisiNormalTags.enableBurst(true);
        if (this.capture != null && this.preview != null) {
            this.capture.setCaptureTemplateType(getCaptureTemplateType());
            this.capture.captureBurst(file, this.actionDataCallback, this.actionStateCallback, this.imageRotation, getEffectSurfaces());
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.media.camera.mode.impl.ModeImpl
    public void stopPictureInternal() {
        this.hisiNormalTags.enableBurst(false);
        super.stopPictureInternal();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.media.camera.mode.impl.ModeImpl
    public <T> int setParameterInternal(ParameterKey.Key<T> key, T t) {
        if (key == null || t == null) {
            return -3;
        }
        if (key.equals(ParameterKey.SENSOR_HDR) && (t instanceof Boolean)) {
            this.hisiNormalTags.setSensorHdr(t.booleanValue());
            updatePreview();
            return 0;
        } else if (key.equals(InnerParameterKey.WATER_MARK) && (t instanceof Boolean)) {
            this.hisiNormalTags.setWaterMarkEnabled(t.booleanValue());
            updatePreview();
            return 0;
        } else if (key.equals(ParameterKey.IMAGE_MIRROR) && (t instanceof Boolean)) {
            this.hisiNormalTags.setMirrorEnabled(t.booleanValue());
            updatePreview();
            return 0;
        } else if (key.equals(InnerParameterKey.SMART_CAPTURE) && (t instanceof Boolean)) {
            T t2 = t;
            this.hisiNormalTags.setSmartCaptureEnabled(t2.booleanValue());
            this.isSmartCaptureEnabled = t2.booleanValue();
            updatePreview();
            return 0;
        } else if (key.equals(ParameterKey.FILTER_EFFECT) && (t instanceof Byte)) {
            this.hisiNormalTags.setFilterEffect(t.byteValue());
            updatePreview();
            return 0;
        } else if (key.equals(ParameterKey.FILTER_LEVEL) && (t instanceof Integer)) {
            this.hisiNormalTags.setFilterLevel(t.intValue());
            updatePreview();
            return 0;
        } else if (!key.equals(ParameterKey.SCENE_EFFECT_ENABLE) || !(t instanceof Boolean)) {
            LOGGER.debug("key %{public}s unknown value %{public}s", key, t);
            return super.setParameterInternal(key, t);
        } else if (this.masterAiFunction == null) {
            return -3;
        } else {
            if (t.booleanValue()) {
                this.masterAiFunction.enterMode();
            } else {
                this.masterAiFunction.dismissMode();
            }
            return 0;
        }
    }

    private class ModeResultHandler extends ResultHandler {
        ModeResultHandler() {
            this.handlerName = ModeResultHandler.class.getSimpleName();
        }

        @Override // ohos.media.camera.mode.function.ResultHandler
        public void handleResult(FrameConfig frameConfig, FrameResult frameResult) {
            if (frameResult == null) {
                NormalMode.LOGGER.warn("handleResult: TotalCaptureResult is null", new Object[0]);
                return;
            }
            try {
                if (NormalMode.this.isSmartCaptureEnabled) {
                    ParametersResult parametersResult = frameResult.getParametersResult();
                    if (parametersResult == null) {
                        NormalMode.LOGGER.warn("handleResult: parametersResult is null", new Object[0]);
                        return;
                    }
                    Integer num = (Integer) parametersResult.getResultValue(InnerResultKey.SMART_SUGGEST_HINT);
                    if (num != null && NormalMode.this.actionStateCallback != null) {
                        HashMap hashMap = new HashMap();
                        hashMap.put(ModeResultKey.SMART_CAPTURE_RESULT, Integer.valueOf(num.intValue() & 255));
                        NormalMode.this.actionStateCallback.onParameters(1, new ParametersResultImpl(1, hashMap));
                    }
                }
            } catch (IllegalArgumentException e) {
                NormalMode.LOGGER.debug("IllegalArgumentException %{public}s", e.getMessage());
            }
        }
    }
}
