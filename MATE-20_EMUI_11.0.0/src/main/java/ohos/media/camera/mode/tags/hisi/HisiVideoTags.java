package ohos.media.camera.mode.tags.hisi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.adapter.utils.CustomConfigurationUtil;
import ohos.media.camera.mode.adapter.utils.constant.AiMovieEffects;
import ohos.media.camera.mode.tags.BaseModeTags;
import ohos.media.camera.mode.tags.CaptureParameters;
import ohos.media.camera.mode.tags.HuaweiTags;
import ohos.media.camera.mode.tags.hisi.utils.HisiBeautyCapabilityUtil;
import ohos.media.camera.mode.tags.hisi.utils.HisiFilterCapabilityUtil;
import ohos.media.camera.mode.tags.hisi.utils.HisiFlashCapabilityUtil;
import ohos.media.camera.mode.tags.hisi.utils.HisiSensorHdrCapabilityUtil;
import ohos.media.camera.mode.utils.CameraUtil;
import ohos.media.camera.mode.utils.CollectionUtil;
import ohos.media.camera.mode.utils.VideoResolutionUtil;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class HisiVideoTags extends BaseModeTags {
    private static final int AI_COLOR_MASK = 2;
    private static final int DEFAULT_FILTER_LEVEL = 16;
    private static final int DEFAULT_FPS = 30;
    private static final int FICTITIOUS_MASK = 4;
    private static final int FILTER_MASK = 1;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(HisiVideoTags.class);
    private static final int SUPPORT_TYPE_MASK = 7;
    private byte aiMovieValue = 0;
    private byte filterEffectValue = 0;
    private int filterLevelValue = 16;
    private boolean isSetVideoStabilization = false;
    private boolean stabilizationValue = true;
    private List<Size> stabilizeSizes;
    private boolean userVideoStabilization = false;
    private Size videoSize;

    public static boolean isAvailable(CameraAbilityImpl cameraAbilityImpl) {
        return true;
    }

    public HisiVideoTags(CameraAbilityImpl cameraAbilityImpl, int i) {
        super(cameraAbilityImpl, i);
        this.stabilizeSizes = VideoResolutionUtil.getVideoStabilizeSupports(cameraAbilityImpl, 30);
    }

    public HisiVideoTags(int i) {
        super(null, i);
        this.isSupportedForegroundProcess = false;
    }

    public static int[] getSupportBeautyTypes(CameraAbilityImpl cameraAbilityImpl) {
        ArrayList arrayList = new ArrayList();
        if (HisiBeautyCapabilityUtil.isVideoBeautyAvailable(cameraAbilityImpl)) {
            arrayList.add(1);
        }
        if (HisiBeautyCapabilityUtil.isBodyShapingAvailable(cameraAbilityImpl)) {
            arrayList.add(4);
        }
        return arrayList.stream().mapToInt($$Lambda$HisiVideoTags$qyjCOMlWVPFcUlb2_9oFVdJbw.INSTANCE).toArray();
    }

    public static boolean isUnifySkinSmoothAvailable(CameraAbilityImpl cameraAbilityImpl) {
        return HisiBeautyCapabilityUtil.isVideoBeautyAvailable(cameraAbilityImpl);
    }

    public static int[] getUnifySkinSmoothRange(CameraAbilityImpl cameraAbilityImpl) {
        return HisiBeautyCapabilityUtil.getVideoBeautyRange(cameraAbilityImpl);
    }

    public static boolean isBodyShapingAvailable(CameraAbilityImpl cameraAbilityImpl) {
        return HisiBeautyCapabilityUtil.isBodyShapingAvailable(cameraAbilityImpl);
    }

    public static int[] getBodyShapingRange(CameraAbilityImpl cameraAbilityImpl) {
        return HisiBeautyCapabilityUtil.getBodyShapingRange(cameraAbilityImpl);
    }

    public static boolean isSupportAiMovie(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            LOGGER.error("isSupportAiMovie cameraAbility is null", new Object[0]);
            return false;
        } else if (CameraUtil.isFrontCamera(cameraAbilityImpl)) {
            LOGGER.error("isSupportAiMovie FrontCamera not support", new Object[0]);
            return false;
        } else {
            Integer num = (Integer) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.AI_VIDEO_SUPPORT);
            boolean isAiMovieEnabled = (num == null || num.intValue() <= 0) ? false : CustomConfigurationUtil.isAiMovieEnabled();
            LOGGER.debug("isSupportAiMovie %{public}b", Boolean.valueOf(isAiMovieEnabled));
            return isAiMovieEnabled;
        }
    }

    public static Byte[] getSupportAiMovieRange(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            LOGGER.error("getSupportAiMovieRange return false, cameraAbility == null", new Object[0]);
            return new Byte[0];
        }
        int i = 7;
        if (isSupportAiMovie(cameraAbilityImpl)) {
            Integer num = (Integer) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.AI_VIDEO_SUPPORT);
            if (num != null) {
                i = num.intValue();
            }
        } else {
            LOGGER.error("getSupportAiMovieRange return false, support ai movie", new Object[0]);
        }
        boolean z = (i & 1) != 0;
        boolean z2 = (i & 2) != 0;
        boolean z3 = (i & 4) != 0;
        LOGGER.debug("supportFilter: %{public}b, supportAiColor: %{public}b, supportFictitious: %{public}b", Boolean.valueOf(z), Boolean.valueOf(z2), Boolean.valueOf(z3));
        ArrayList arrayList = new ArrayList(6);
        arrayList.add(AiMovieEffects.AI_MOVIE_NO_EFFECT.getAiMovieValue());
        if (z2) {
            arrayList.add(AiMovieEffects.AI_MOVIE_AICOLOR_EFFECT.getAiMovieValue());
        }
        if (z3) {
            arrayList.add(AiMovieEffects.AI_MOVIE_PORTRAIT_FICTITIOUS_EFFECT.getAiMovieValue());
        }
        if (z) {
            arrayList.add(AiMovieEffects.AI_MOVIE_NOSTALGIA_EFFECT.getAiMovieValue());
            arrayList.add(AiMovieEffects.AI_MOVIE_HITCHCOCK_EFFECT.getAiMovieValue());
            arrayList.add(AiMovieEffects.AI_MOVIE_FRESH_EFFECT.getAiMovieValue());
        }
        Byte[] bArr = (Byte[]) arrayList.toArray(new Byte[0]);
        LOGGER.debug("getSupportAiMovieRange %{public}s", Arrays.toString(bArr));
        return bArr;
    }

    @Override // ohos.media.camera.mode.tags.BaseModeTags, ohos.media.camera.mode.tags.ModeTags
    public CaptureParameters enablePreview() {
        LOGGER.begin("enablePreview");
        CaptureParameters enablePreview = super.enablePreview();
        enablePreview.addParameter(InnerParameterKey.CAMERA_FLAG, (byte) 1);
        enablePreview.addParameter(InnerParameterKey.API_VERSION, 2);
        if (CameraUtil.isDualBothSupported(this.cameraAbility)) {
            enablePreview.addParameter(InnerParameterKey.DUAL_SENSOR_MODE, (byte) 3);
        }
        enablePreview.addParameter(InnerParameterKey.CAMERA_SCENE_MODE, 28);
        enablePreview.addParameter(InnerParameterKey.CAMERA_SESSION_SCENE_MODE, 28);
        List<Size> list = this.stabilizeSizes;
        if (list == null || list.contains(this.videoSize)) {
            enablePreview.addParameter(ParameterKey.VIDEO_STABILIZATION, Boolean.valueOf(this.stabilizationValue));
        } else {
            enablePreview.addParameter(ParameterKey.VIDEO_STABILIZATION, false);
        }
        enablePreview.addParameter(InnerParameterKey.LENS_OPTICAL_STABILIZATION_MODE, 1);
        HisiBeautyCapabilityUtil.enableVideoSkinSmooth(this.cameraAbility, enablePreview, this.skinSmoothLevel);
        HisiBeautyCapabilityUtil.enableBodyShaping(this.cameraAbility, enablePreview, this.bodyShapeLevel);
        if (this.zoomCropRegion != null) {
            enablePreview.addParameter(InnerParameterKey.SCALER_CROP_REGION, this.zoomCropRegion);
        }
        enablePreview.addParameter(InnerParameterKey.AE_MODE, Integer.valueOf(HisiFlashCapabilityUtil.getAeMode(this.flashMode)));
        enablePreview.addParameter(InnerParameterKey.FLASH_MODE, Integer.valueOf(HisiFlashCapabilityUtil.getFlashMode(this.flashMode)));
        enablePreview.addParameter(InnerParameterKey.FACE_DETECTION_TYPE, 2);
        enableColorMode(enablePreview);
        enablePreview.addParameter(ParameterKey.AI_MOVIE, Byte.valueOf(this.aiMovieValue));
        HisiFilterCapabilityUtil.enableFilterEffect(enablePreview, this.filterEffectValue, this.filterLevelValue);
        enableFaceBeautyMode(enablePreview);
        if (HisiSensorHdrCapabilityUtil.isAvailable(this.cameraAbility)) {
            enablePreview.addParameter(InnerParameterKey.SENSOR_HDR_MODE, Byte.valueOf(this.sensorHdr));
        }
        enablePreview.addParameter(InnerParameterKey.VIDEO_DYNAMIC_FPS_MODE, 0);
        enablePreview.addParameter(InnerParameterKey.HIGH_VIDEO_FPS, 30);
        if (this.videoSize != null) {
            enablePreview.addParameter(InnerParameterKey.REAL_VIDEO_SIZE, new int[]{this.videoSize.width, this.videoSize.height});
        }
        LOGGER.end("enablePreview");
        return enablePreview;
    }

    @Override // ohos.media.camera.mode.tags.BaseModeTags, ohos.media.camera.mode.tags.ModeTags
    public List<CaptureParameters> enableCapture() {
        LOGGER.begin("enableCapture");
        List<CaptureParameters> enableCapture = super.enableCapture();
        CaptureParameters captureParameters = new CaptureParameters(this.stageId);
        for (CaptureParameters captureParameters2 : enableCapture) {
            captureParameters.addParameters(captureParameters2);
        }
        captureParameters.addParameter(InnerParameterKey.CAMERA_FLAG, (byte) 1);
        captureParameters.addParameter(InnerParameterKey.API_VERSION, 2);
        if (CameraUtil.isDualBothSupported(this.cameraAbility)) {
            captureParameters.addParameter(InnerParameterKey.DUAL_SENSOR_MODE, (byte) 3);
        }
        captureParameters.addParameter(InnerParameterKey.CAMERA_SCENE_MODE, 28);
        captureParameters.addParameter(InnerParameterKey.CAMERA_SESSION_SCENE_MODE, 28);
        captureParameters.addParameter(ParameterKey.IMAGE_COMPRESSION_QUALITY, Byte.valueOf((byte) HuaweiTags.DEFAULT_JPEG_QUALITY));
        List<Size> list = this.stabilizeSizes;
        if (list == null || list.contains(this.videoSize)) {
            captureParameters.addParameter(ParameterKey.VIDEO_STABILIZATION, Boolean.valueOf(this.stabilizationValue));
        } else {
            captureParameters.addParameter(ParameterKey.VIDEO_STABILIZATION, false);
        }
        captureParameters.addParameter(InnerParameterKey.LENS_OPTICAL_STABILIZATION_MODE, 1);
        HisiBeautyCapabilityUtil.enableVideoSkinSmooth(this.cameraAbility, captureParameters, this.skinSmoothLevel);
        HisiBeautyCapabilityUtil.enableBodyShaping(this.cameraAbility, captureParameters, this.bodyShapeLevel);
        if (this.zoomCropRegion != null) {
            captureParameters.addParameter(InnerParameterKey.SCALER_CROP_REGION, this.zoomCropRegion);
        }
        captureParameters.addParameter(InnerParameterKey.AE_MODE, Integer.valueOf(HisiFlashCapabilityUtil.getAeMode(this.flashMode)));
        captureParameters.addParameter(InnerParameterKey.FLASH_MODE, Integer.valueOf(HisiFlashCapabilityUtil.getFlashMode(this.flashMode)));
        captureParameters.addParameter(InnerParameterKey.FACE_DETECTION_TYPE, 2);
        enableColorMode(captureParameters);
        captureParameters.addParameter(ParameterKey.AI_MOVIE, Byte.valueOf(this.aiMovieValue));
        HisiFilterCapabilityUtil.enableFilterEffect(captureParameters, this.filterEffectValue, this.filterLevelValue);
        enableFaceBeautyMode(captureParameters);
        if (HisiSensorHdrCapabilityUtil.isAvailable(this.cameraAbility)) {
            captureParameters.addParameter(InnerParameterKey.SENSOR_HDR_MODE, Byte.valueOf(this.sensorHdr));
        }
        captureParameters.addParameter(InnerParameterKey.VIDEO_DYNAMIC_FPS_MODE, 0);
        captureParameters.addParameter(InnerParameterKey.HIGH_VIDEO_FPS, 30);
        if (this.videoSize != null) {
            captureParameters.addParameter(InnerParameterKey.REAL_VIDEO_SIZE, new int[]{this.videoSize.width, this.videoSize.height});
        }
        LOGGER.end("enableCapture");
        return Collections.singletonList(captureParameters);
    }

    @Override // ohos.media.camera.mode.tags.BaseModeTags, ohos.media.camera.mode.tags.ModeTags
    public int setBeauty(int i, int i2) {
        if (this.filterEffectValue != 0 && i2 != 0) {
            return -5;
        }
        if (this.aiMovieValue != 0 && i2 != 0) {
            return -5;
        }
        if (this.isSetVideoStabilization && this.stabilizationValue && i2 != 0) {
            return -5;
        }
        if (i != 1) {
            if (i == 4 && HisiBeautyCapabilityUtil.isBodyShapingAvailable(this.cameraAbility) && HisiBeautyCapabilityUtil.isBodyShapingValueValid(i2, this.cameraAbility)) {
                this.bodyShapeLevel = i2;
                return 0;
            }
        } else if (HisiBeautyCapabilityUtil.isVideoBeautyValueValid(i2, this.cameraAbility)) {
            this.skinSmoothLevel = i2;
            return 0;
        }
        return -1;
    }

    public int setAiMovieEffect(Byte b) {
        boolean z;
        LOGGER.begin("setAiMovieEffect " + b);
        if (!CollectionUtil.contains(getSupportAiMovieRange(this.cameraAbility), b)) {
            LOGGER.error("not valid value: %{public}s", b);
            return -3;
        } else if (this.filterEffectValue != 0 && b.byteValue() != 0) {
            return -5;
        } else {
            if ((this.skinSmoothLevel != 0 || this.bodyShapeLevel != 0) && b.byteValue() != 0) {
                return -5;
            }
            try {
                this.aiMovieValue = Byte.valueOf(b.byteValue()).byteValue();
                if (this.aiMovieValue != 0) {
                    this.stabilizationValue = true;
                } else {
                    if (this.isSetVideoStabilization) {
                        if (!this.userVideoStabilization) {
                            z = false;
                            this.stabilizationValue = z;
                        }
                    }
                    z = true;
                    this.stabilizationValue = z;
                }
            } catch (NumberFormatException unused) {
                LOGGER.error("NumberFormatException value: %{public}s", b);
            }
            LOGGER.end("setAiMovieEffect");
            return 0;
        }
    }

    public int setFilterEffect(byte b) {
        boolean z;
        LOGGER.begin("setFilterEffect " + ((int) b));
        if (!CollectionUtil.contains(HisiFilterCapabilityUtil.getSupportedFilterRange(this.cameraAbility), (int) b)) {
            LOGGER.error("not valid filter effect value: %{public}s", Byte.valueOf(b));
            return -3;
        } else if (this.aiMovieValue != 0 && b != 0) {
            return -5;
        } else {
            if ((this.skinSmoothLevel != 0 || this.bodyShapeLevel != 0) && b != 0) {
                return -5;
            }
            if (this.isSetVideoStabilization && this.stabilizationValue && b != 0) {
                return -5;
            }
            try {
                this.filterEffectValue = Byte.valueOf(b).byteValue();
                if (this.filterEffectValue != 0) {
                    this.stabilizationValue = false;
                } else {
                    if (this.isSetVideoStabilization) {
                        if (!this.userVideoStabilization) {
                            z = false;
                            this.stabilizationValue = z;
                        }
                    }
                    z = true;
                    this.stabilizationValue = z;
                }
            } catch (NumberFormatException unused) {
                LOGGER.error("NumberFormatException value: %{public}s", Byte.valueOf(b));
            }
            LOGGER.end("setFilterEffect");
            return 0;
        }
    }

    public int setFilterLevel(int i) {
        this.filterLevelValue = i;
        return 0;
    }

    @Override // ohos.media.camera.mode.tags.BaseModeTags, ohos.media.camera.mode.tags.ModeTags
    public void setVideoSize(Size size) {
        this.videoSize = size;
    }

    @Override // ohos.media.camera.mode.tags.ModeTags
    public int setVideoStabilization(boolean z) {
        LOGGER.info("setVideoStabilization %{public}b", Boolean.valueOf(z));
        if (this.aiMovieValue != 0 && !z) {
            return -5;
        }
        if (this.filterEffectValue != 0 && z) {
            return -5;
        }
        if (!HuaweiTags.isSupported(this.cameraAbility, InnerPropertyKey.BEAUTY_STABILIZATION_SUPPORTED) && ((this.skinSmoothLevel != 0 || this.bodyShapeLevel != 0) && z)) {
            return -5;
        }
        this.isSetVideoStabilization = true;
        this.userVideoStabilization = z;
        this.stabilizationValue = this.userVideoStabilization;
        return 0;
    }

    private void enableColorMode(CaptureParameters captureParameters) {
        try {
            captureParameters.addParameter(InnerParameterKey.COLOR_MODE, Byte.valueOf(Byte.parseByte(String.valueOf(this.colorMode))));
        } catch (NumberFormatException unused) {
            LOGGER.error("colorMode FormatException", new Object[0]);
        }
    }

    private void enableFaceBeautyMode(CaptureParameters captureParameters) {
        if (!HisiBeautyCapabilityUtil.isVideoBeautyAvailable(this.cameraAbility) || this.aiMovieValue == 0) {
            captureParameters.addParameter(InnerParameterKey.FACE_BEAUTY_MODE, (byte) 1);
        } else {
            captureParameters.addParameter(InnerParameterKey.FACE_BEAUTY_MODE, (byte) 0);
        }
    }
}
