package ohos.media.camera.mode.modes;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.ModeStateCallback;
import ohos.media.camera.mode.adapter.key.ModeCharacteristicKey;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.impl.ModeImpl;
import ohos.media.camera.mode.tags.hisi.HisiVideoTags;
import ohos.media.camera.mode.utils.CameraManagerHelper;
import ohos.media.camera.mode.utils.ModeAbilityHelper;
import ohos.media.camera.mode.utils.ModeNameUtil;
import ohos.media.camera.mode.utils.OptimalSizeCombination;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class VideoMode extends ModeImpl {
    private static final OptimalSizeCombination COMBINATION_4K = new OptimalSizeCombination(new Size(1920, 1080), new Size(3840, 2160), new Size(3840, 2160));
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(VideoMode.class);
    private static final String MODE_NAME = ModeNameUtil.getModeNameById(5);
    private static final int[] SUPPORTED_ACTIONS = {2, 4, 1};
    private static final List<PropertyKey.Key<?>> SUPPORTED_FUNCTIONS = Arrays.asList(ModeCharacteristicKey.ZOOM_FUNCTION, ModeCharacteristicKey.BEAUTY_FUNCTION, ModeCharacteristicKey.FLASH_MODE_FUNCTION, ModeCharacteristicKey.FACE_DETECTION_FUNCTION, ModeCharacteristicKey.WATER_MARK_FUNCTION, ModeCharacteristicKey.LOCATION_FUNCTION, ModeCharacteristicKey.AI_MOVIE_FUNCTION, ModeCharacteristicKey.FILTER_EFFECT_FUNCTION, ModeCharacteristicKey.SENSOR_HDR_FUNCTION, ModeCharacteristicKey.VIDEO_STABILIZATION_FUNCTION);
    private final HisiVideoTags hisiVideoTags;

    public static boolean isAvailable(CameraAbilityImpl cameraAbilityImpl) {
        return true;
    }

    public VideoMode(String str, ModeStateCallback modeStateCallback, EventHandler eventHandler) {
        super(str, modeStateCallback, eventHandler);
        this.preCapture = this.preCaptureManager.getPreCapture(1);
        this.modeType = 5;
        CameraAbilityImpl cameraAbility = CameraManagerHelper.getCameraManager().getCameraAbility(str);
        this.modeAbility = getModeAbility(cameraAbility);
        this.hisiVideoTags = new HisiVideoTags(cameraAbility, 0);
        this.modeTags = this.hisiVideoTags;
        this.preCaptureManager.setModeTags(this.modeTags);
        createAction(SUPPORTED_ACTIONS);
        this.backPassThroughCombinations.add(COMBINATION_4K);
    }

    public static int[] getSupportedActions() {
        return SUPPORTED_ACTIONS;
    }

    public static ModeAbilityImpl getModeAbility(CameraAbilityImpl cameraAbilityImpl) {
        ModeAbilityImpl createModeAbility = ModeAbilityHelper.createModeAbility(5, SUPPORTED_ACTIONS, SUPPORTED_FUNCTIONS, cameraAbilityImpl);
        createModeAbility.setMaxPreviewSurfaceNumber(2);
        return createModeAbility;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.media.camera.mode.impl.ModeImpl
    public <T> int setParameterInternal(ParameterKey.Key<T> key, T t) {
        int i;
        LOGGER.debug("setParameter key: %{public}s, value: %{public}s", key, t);
        if (key == null || t == null || this.hisiVideoTags == null) {
            return -3;
        }
        if (this.isPassThroughResolution) {
            return -2;
        }
        if (key.equals(InnerParameterKey.WATER_MARK) && (t instanceof Boolean)) {
            i = this.hisiVideoTags.setWaterMarkEnabled(t.booleanValue());
        } else if (key.equals(ParameterKey.AI_MOVIE) && (t instanceof Byte)) {
            i = this.hisiVideoTags.setAiMovieEffect(t);
        } else if (key.equals(ParameterKey.FILTER_EFFECT) && (t instanceof Byte)) {
            i = this.hisiVideoTags.setFilterEffect(t.byteValue());
        } else if (key.equals(ParameterKey.FILTER_LEVEL) && (t instanceof Integer)) {
            i = this.hisiVideoTags.setFilterLevel(t.intValue());
        } else if (key.equals(ParameterKey.SENSOR_HDR) && (t instanceof Boolean)) {
            i = this.hisiVideoTags.setSensorHdr(t.booleanValue());
        } else if (!key.equals(ParameterKey.VIDEO_STABILIZATION) || !(t instanceof Boolean)) {
            LOGGER.debug("key %{public}s unknown value %{public}s", key, t);
            return super.setParameterInternal(key, t);
        } else {
            i = this.hisiVideoTags.setVideoStabilization(t.booleanValue());
        }
        if (i == 0) {
            updatePreview();
        }
        return i;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.media.camera.mode.impl.ModeImpl
    public void takePictureBurstInternal(File file) {
        LOGGER.debug("videoMode is Not Support Burst:", new Object[0]);
        throw new UnsupportedOperationException("videoMode is not support burst");
    }

    /* access modifiers changed from: protected */
    @Override // ohos.media.camera.mode.impl.ModeImpl
    public void takePictureBurstInternal() {
        LOGGER.debug("videoMode is Not Support Burst:", new Object[0]);
        throw new UnsupportedOperationException("videoMode is not support burst");
    }

    /* access modifiers changed from: protected */
    @Override // ohos.media.camera.mode.impl.ModeImpl
    public int setBeautyModeInternal(int i, int i2) {
        if (this.isPassThroughResolution) {
            return -2;
        }
        return super.setBeautyModeInternal(i, i2);
    }
}
