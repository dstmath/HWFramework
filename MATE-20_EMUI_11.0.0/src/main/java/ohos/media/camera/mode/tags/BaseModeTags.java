package ohos.media.camera.mode.tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ohos.agp.utils.Rect;
import ohos.location.Location;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.tags.hisi.utils.HisiBeautyCapabilityUtil;
import ohos.media.camera.mode.tags.hisi.utils.HisiZoomCapabilityUtil;
import ohos.media.camera.params.ParameterKey;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public abstract class BaseModeTags implements ModeTags {
    protected static final int FOREGROUND_ENABLE = 1024;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(BaseModeTags.class);
    public static final int MAX_SUPPORT_AI_MOVIE_TYPES = 6;
    protected int bodyShapeLevel;
    protected CameraAbilityImpl cameraAbility;
    private CaptureParameters captureParameters;
    protected int colorMode;
    protected int faceSlenderLevel;
    protected int flashMode;
    protected boolean isBurstEnabled;
    protected boolean isForegroundProcessEnabled;
    protected boolean isSmileDetectionEnabled;
    protected boolean isSupportedForegroundProcess;
    protected Location location;
    private CaptureParameters previewParameters;
    private CaptureParameters recordParameters;
    protected byte sceneDetectionStatus;
    protected byte sensorHdr;
    protected int skinSmoothLevel;
    protected int skinToneLevel;
    protected int stageId;
    protected byte waterMarkStatus;
    protected WorkMode workMode;
    protected Rect zoomCropRegion;

    public enum WorkMode {
        FULL,
        LIMITED
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public void setVideoSize(Size size) {
    }

    public BaseModeTags(CameraAbilityImpl cameraAbilityImpl, int i) {
        this.flashMode = 1;
        this.colorMode = 0;
        this.sceneDetectionStatus = 0;
        this.waterMarkStatus = 0;
        this.sensorHdr = 0;
        this.location = null;
        this.workMode = WorkMode.FULL;
        this.cameraAbility = cameraAbilityImpl;
        this.stageId = i;
        this.isForegroundProcessEnabled = false;
        this.isSmileDetectionEnabled = false;
        this.previewParameters = new CaptureParameters();
        this.captureParameters = new CaptureParameters(i);
        this.recordParameters = new CaptureParameters();
    }

    public BaseModeTags(CameraAbilityImpl cameraAbilityImpl, int i, WorkMode workMode2) {
        this(cameraAbilityImpl, i);
        this.workMode = workMode2;
    }

    public static List<Size> getOutputSize(int i, CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl != null) {
            return cameraAbilityImpl.getSupportedSizes(i);
        }
        LOGGER.error("getOutputSize: cameraAbility is null!", new Object[0]);
        return new ArrayList();
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public List<Size> getOutputSize(int i) {
        return getOutputSize(i, this.cameraAbility);
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public void enableForegroundProcess() {
        if (this.isSupportedForegroundProcess) {
            this.isForegroundProcessEnabled = true;
        } else {
            LOGGER.debug("not supported foreground process", new Object[0]);
        }
    }

    public boolean isGetImageFromPostProc() {
        return this.isSupportedForegroundProcess && !this.isForegroundProcessEnabled;
    }

    public void enableBurst(boolean z) {
        this.isBurstEnabled = false;
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public CaptureParameters enablePreview() {
        LOGGER.debug("enablePreview ", new Object[0]);
        CaptureParameters captureParameters2 = new CaptureParameters();
        DefaultExtendParameters.applyToBuilder(this.cameraAbility, captureParameters2);
        captureParameters2.addParameters(this.previewParameters);
        return captureParameters2;
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public List<CaptureParameters> enableCapture() {
        LOGGER.debug("enableCapture ", new Object[0]);
        CaptureParameters captureParameters2 = new CaptureParameters(this.stageId);
        DefaultExtendParameters.applyToBuilder(this.cameraAbility, captureParameters2);
        captureParameters2.addParameters(this.captureParameters);
        return Collections.singletonList(captureParameters2);
    }

    public CaptureParameters enableCapture(int i) {
        LOGGER.debug("enableCapture %{public}d", Integer.valueOf(i));
        CaptureParameters captureParameters2 = new CaptureParameters(i);
        DefaultExtendParameters.applyToBuilder(this.cameraAbility, captureParameters2);
        captureParameters2.addParameters(this.captureParameters);
        return captureParameters2;
    }

    public CaptureParameters enableRecord() {
        LOGGER.debug("enableRecord ", new Object[0]);
        return this.recordParameters;
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public int setZoom(float f) {
        this.zoomCropRegion = HisiZoomCapabilityUtil.getCenterZoomRect(f, this.cameraAbility);
        return 0;
    }

    public Rect getCropRegion() {
        return this.zoomCropRegion;
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public int setFlashMode(int i) {
        this.flashMode = i;
        return 0;
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public int setColorMode(int i) {
        this.colorMode = i;
        return 0;
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public int setBeauty(int i, int i2) {
        LOGGER.debug("type: %{public}d, level: %{public}d", Integer.valueOf(i), Integer.valueOf(i2));
        if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    if (i == 4 && HisiBeautyCapabilityUtil.isBodyShapingAvailable(this.cameraAbility) && HisiBeautyCapabilityUtil.isBodyShapingValueValid(i2, this.cameraAbility)) {
                        this.bodyShapeLevel = i2;
                        return 0;
                    }
                } else if (HisiBeautyCapabilityUtil.isSkinToneAvailable(this.cameraAbility) && HisiBeautyCapabilityUtil.isFaceColorValueValid(i2, this.cameraAbility)) {
                    this.skinToneLevel = i2;
                    return 0;
                }
            } else if (HisiBeautyCapabilityUtil.isUnifyFaceSlenderAvailable(this.cameraAbility) && HisiBeautyCapabilityUtil.isUnifyFaceSlenderValueValid(i2, this.cameraAbility)) {
                this.faceSlenderLevel = i2;
                return 0;
            }
        } else if (HisiBeautyCapabilityUtil.isUnifySkinSmoothAvailable(this.cameraAbility) && HisiBeautyCapabilityUtil.isUnifySkinSmoothValueValid(i2, this.cameraAbility)) {
            this.skinSmoothLevel = i2;
            return 0;
        }
        return -1;
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public boolean isBeautyEnabled() {
        if (this.skinSmoothLevel == 0 && this.faceSlenderLevel == 0 && this.skinToneLevel == 0 && this.bodyShapeLevel == 0) {
            return false;
        }
        return true;
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public void setSmileDetection(boolean z) {
        this.isSmileDetectionEnabled = z;
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public void setSceneDetection(boolean z) {
        this.sceneDetectionStatus = z ? (byte) 1 : 0;
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public int setWaterMarkEnabled(boolean z) {
        this.waterMarkStatus = z ? (byte) 1 : 0;
        return 0;
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public int setLocation(Location location2) {
        this.location = location2;
        return 0;
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public int setSensorHdr(boolean z) {
        LOGGER.info("setSensorHdr %{public}b", Boolean.valueOf(z));
        this.sensorHdr = z ? (byte) 1 : 0;
        return 0;
    }

    public <T> void setParameter(ParameterKey.Key<T> key, T t) {
        if (key == null || t == null) {
            LOGGER.warn("invalid key or value!", new Object[0]);
            return;
        }
        this.captureParameters.addParameter(key, t);
        this.previewParameters.addParameter(key, t);
    }

    public void clearParameters() {
        this.captureParameters.clearParameters();
        this.previewParameters.clearParameters();
    }
}
