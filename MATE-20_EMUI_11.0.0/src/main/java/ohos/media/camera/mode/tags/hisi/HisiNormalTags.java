package ohos.media.camera.mode.tags.hisi;

import java.util.Collections;
import java.util.List;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.tags.BaseModeTags;
import ohos.media.camera.mode.tags.CaptureParameter;
import ohos.media.camera.mode.tags.CaptureParameters;
import ohos.media.camera.mode.tags.HuaweiTags;
import ohos.media.camera.mode.tags.hisi.utils.HisiFilterCapabilityUtil;
import ohos.media.camera.mode.tags.hisi.utils.HisiFlashCapabilityUtil;
import ohos.media.camera.mode.tags.hisi.utils.HisiSensorHdrCapabilityUtil;
import ohos.media.camera.mode.tags.hisi.utils.HisiSmartCaptureCapabilityUtil;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class HisiNormalTags extends BaseModeTags {
    private static final int DEFAULT_FILTER_LEVEL = 16;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(HisiNormalTags.class);
    private byte filterEffectValue = 0;
    private int filterLevelValue = 16;
    private byte mirrorStatus = 0;
    private byte smartCaptureStatus = 0;

    public static boolean isAvailable(CameraAbilityImpl cameraAbilityImpl) {
        return true;
    }

    public HisiNormalTags(CameraAbilityImpl cameraAbilityImpl, int i) {
        super(cameraAbilityImpl, i);
        this.isSupportedForegroundProcess = true;
    }

    public HisiNormalTags(int i) {
        super(null, i);
        this.isSupportedForegroundProcess = true;
    }

    @Override // ohos.media.camera.mode.tags.BaseModeTags, ohos.media.camera.mode.tags.ModeTags
    public CaptureParameters enablePreview() {
        LOGGER.begin("enablePreview");
        CaptureParameters enablePreview = super.enablePreview();
        if (this.zoomCropRegion != null) {
            enablePreview.addParameter(InnerParameterKey.SCALER_CROP_REGION, this.zoomCropRegion);
        }
        enablePreview.addParameter(InnerParameterKey.AE_MODE, Integer.valueOf(HisiFlashCapabilityUtil.getAeMode(this.flashMode)));
        enablePreview.addParameter(InnerParameterKey.FLASH_MODE, Integer.valueOf(HisiFlashCapabilityUtil.getFlashMode(this.flashMode)));
        enablePreview.addParameter(InnerParameterKey.FACE_DETECTION_TYPE, 2);
        if (this.isForegroundProcessEnabled) {
            enablePreview.addParameter(InnerParameterKey.MANUAL_FOCUS_VALUE, 1024);
            enablePreview.addParameter(InnerParameterKey.IMAGE_FOREGROUND_PROCESS_MODE, (byte) 1);
        }
        enablePreview.addParameter(InnerParameterKey.LENS_OPTICAL_STABILIZATION_MODE, 1);
        enablePreview.addParameter(InnerParameterKey.COLOR_MODE, Byte.valueOf(Byte.parseByte(String.valueOf(this.colorMode))));
        if (HisiSensorHdrCapabilityUtil.isAvailable(this.cameraAbility)) {
            enablePreview.addParameter(InnerParameterKey.SENSOR_HDR_MODE, Byte.valueOf(this.sensorHdr));
        }
        if (this.isSmileDetectionEnabled) {
            enablePreview.addParameter(InnerParameterKey.SMILE_DETECTION, (byte) 1);
        } else {
            enablePreview.addParameter(InnerParameterKey.SMILE_DETECTION, (byte) 0);
        }
        enablePreview.addParameter(new CaptureParameter<>(InnerParameterKey.CAMERA_FLAG, (byte) 1));
        enablePreview.addParameter(new CaptureParameter<>(InnerParameterKey.IMAGE_POST_PROCESS_MODE, (byte) 1));
        enablePreview.addParameter(new CaptureParameter<>(InnerParameterKey.CAMERA_SCENE_MODE, 0));
        enablePreview.addParameter(new CaptureParameter<>(InnerParameterKey.CAMERA_SESSION_SCENE_MODE, 0));
        enablePreview.addParameter(InnerParameterKey.DM_WATERMARK_MODE, Byte.valueOf(this.waterMarkStatus));
        enablePreview.addParameter(InnerParameterKey.CAPTURE_MIRROR, Byte.valueOf(this.mirrorStatus));
        HisiSmartCaptureCapabilityUtil.setSmartCaptureParameter(enablePreview, this.smartCaptureStatus, this.sceneDetectionStatus);
        HisiFilterCapabilityUtil.enableFilterEffect(enablePreview, this.filterEffectValue, this.filterLevelValue);
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
        if (this.zoomCropRegion != null) {
            captureParameters.addParameter(InnerParameterKey.SCALER_CROP_REGION, this.zoomCropRegion);
        }
        captureParameters.addParameter(InnerParameterKey.AE_MODE, Integer.valueOf(HisiFlashCapabilityUtil.getAeMode(this.flashMode)));
        captureParameters.addParameter(InnerParameterKey.FLASH_MODE, Integer.valueOf(HisiFlashCapabilityUtil.getFlashMode(this.flashMode)));
        captureParameters.addParameter(ParameterKey.IMAGE_COMPRESSION_QUALITY, Byte.valueOf((byte) HuaweiTags.DEFAULT_JPEG_QUALITY));
        captureParameters.addParameter(InnerParameterKey.LOCATION, this.location);
        if (this.isForegroundProcessEnabled) {
            captureParameters.addParameter(InnerParameterKey.MANUAL_FOCUS_VALUE, 1024);
            captureParameters.addParameter(InnerParameterKey.IMAGE_FOREGROUND_PROCESS_MODE, (byte) 1);
        }
        captureParameters.addParameter(InnerParameterKey.COLOR_MODE, Byte.valueOf(Byte.parseByte(String.valueOf(this.colorMode))));
        if (HisiSensorHdrCapabilityUtil.isAvailable(this.cameraAbility)) {
            captureParameters.addParameter(InnerParameterKey.SENSOR_HDR_MODE, Byte.valueOf(this.sensorHdr));
        }
        captureParameters.addParameter(new CaptureParameter<>(InnerParameterKey.CAMERA_FLAG, (byte) 1));
        captureParameters.addParameter(new CaptureParameter<>(InnerParameterKey.IMAGE_POST_PROCESS_MODE, (byte) 1));
        captureParameters.addParameter(new CaptureParameter<>(InnerParameterKey.API_VERSION, 2));
        captureParameters.addParameter(new CaptureParameter<>(InnerParameterKey.CAMERA_SESSION_SCENE_MODE, 0));
        if (this.isBurstEnabled) {
            captureParameters.addParameter(InnerParameterKey.BURST_SNAPSHOT_MODE, (byte) 1);
            captureParameters.addParameter(InnerParameterKey.BEST_SHOT_MODE, (byte) 1);
            captureParameters.addParameter(InnerParameterKey.CAMERA_SCENE_MODE, 1);
        } else {
            captureParameters.addParameter(new CaptureParameter<>(InnerParameterKey.CAMERA_SCENE_MODE, 0));
        }
        captureParameters.addParameter(InnerParameterKey.DM_WATERMARK_MODE, Byte.valueOf(this.waterMarkStatus));
        captureParameters.addParameter(InnerParameterKey.CAPTURE_MIRROR, Byte.valueOf(this.mirrorStatus));
        HisiSmartCaptureCapabilityUtil.setSmartCaptureParameter(captureParameters, this.smartCaptureStatus, this.sceneDetectionStatus);
        HisiFilterCapabilityUtil.enableFilterEffect(captureParameters, this.filterEffectValue, this.filterLevelValue);
        LOGGER.end("enableCapture");
        return Collections.singletonList(captureParameters);
    }

    public void setMirrorEnabled(boolean z) {
        this.mirrorStatus = z ? (byte) 1 : 0;
    }

    public void setSmartCaptureEnabled(boolean z) {
        this.smartCaptureStatus = z ? (byte) 1 : 0;
    }

    public void setFilterEffect(byte b) {
        this.filterEffectValue = b;
    }

    public void setFilterLevel(int i) {
        this.filterLevelValue = i;
    }

    @Override // ohos.media.camera.mode.tags.BaseModeTags
    public void enableBurst(boolean z) {
        this.isBurstEnabled = z;
    }
}
