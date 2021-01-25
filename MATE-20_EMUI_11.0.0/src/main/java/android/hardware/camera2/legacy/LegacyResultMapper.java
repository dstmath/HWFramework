package android.hardware.camera2.legacy;

import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.legacy.ParameterUtils;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.utils.ParamsUtils;
import android.location.Location;
import android.util.Log;
import android.util.Size;
import java.util.ArrayList;
import java.util.List;

public class LegacyResultMapper {
    private static final boolean DEBUG = false;
    private static final String TAG = "LegacyResultMapper";
    private LegacyRequest mCachedRequest = null;
    private CameraMetadataNative mCachedResult = null;

    public CameraMetadataNative cachedConvertResultMetadata(LegacyRequest legacyRequest, long timestamp) {
        CameraMetadataNative result;
        if (this.mCachedRequest == null || !legacyRequest.parameters.same(this.mCachedRequest.parameters) || !legacyRequest.captureRequest.equals((Object) this.mCachedRequest.captureRequest)) {
            result = convertResultMetadata(legacyRequest);
            this.mCachedRequest = legacyRequest;
            this.mCachedResult = new CameraMetadataNative(result);
        } else {
            result = new CameraMetadataNative(this.mCachedResult);
        }
        result.set((CaptureResult.Key<CaptureResult.Key<Long>>) CaptureResult.SENSOR_TIMESTAMP, (CaptureResult.Key<Long>) Long.valueOf(timestamp));
        return result;
    }

    private static CameraMetadataNative convertResultMetadata(LegacyRequest legacyRequest) {
        CameraCharacteristics characteristics = legacyRequest.characteristics;
        CaptureRequest request = legacyRequest.captureRequest;
        Size previewSize = legacyRequest.previewSize;
        Camera.Parameters params = legacyRequest.parameters;
        CameraMetadataNative result = new CameraMetadataNative();
        Rect activeArraySize = (Rect) characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        ParameterUtils.ZoomData zoomData = ParameterUtils.convertScalerCropRegion(activeArraySize, (Rect) request.get(CaptureRequest.SCALER_CROP_REGION), previewSize, params);
        result.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.COLOR_CORRECTION_ABERRATION_MODE, (CaptureResult.Key<Integer>) ((Integer) request.get(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE)));
        mapAe(result, characteristics, request, activeArraySize, zoomData, params);
        mapAf(result, activeArraySize, zoomData, params);
        mapAwb(result, params);
        int stabMode = 1;
        result.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.CONTROL_CAPTURE_INTENT, (CaptureResult.Key<Integer>) Integer.valueOf(LegacyRequestMapper.filterSupportedCaptureIntent(((Integer) ParamsUtils.getOrDefault(request, CaptureRequest.CONTROL_CAPTURE_INTENT, 1)).intValue())));
        if (((Integer) ParamsUtils.getOrDefault(request, CaptureRequest.CONTROL_MODE, 1)).intValue() == 2) {
            result.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.CONTROL_MODE, (CaptureResult.Key<Integer>) 2);
        } else {
            result.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.CONTROL_MODE, (CaptureResult.Key<Integer>) 1);
        }
        String legacySceneMode = params.getSceneMode();
        int mode = LegacyMetadataMapper.convertSceneModeFromLegacy(legacySceneMode);
        if (mode != -1) {
            result.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.CONTROL_SCENE_MODE, (CaptureResult.Key<Integer>) Integer.valueOf(mode));
        } else {
            Log.w(TAG, "Unknown scene mode " + legacySceneMode + " returned by camera HAL, setting to disabled.");
            result.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.CONTROL_SCENE_MODE, (CaptureResult.Key<Integer>) 0);
        }
        String legacyEffectMode = params.getColorEffect();
        int mode2 = LegacyMetadataMapper.convertEffectModeFromLegacy(legacyEffectMode);
        if (mode2 != -1) {
            result.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.CONTROL_EFFECT_MODE, (CaptureResult.Key<Integer>) Integer.valueOf(mode2));
        } else {
            Log.w(TAG, "Unknown effect mode " + legacyEffectMode + " returned by camera HAL, setting to off.");
            result.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.CONTROL_EFFECT_MODE, (CaptureResult.Key<Integer>) 0);
        }
        if (!params.isVideoStabilizationSupported() || !params.getVideoStabilization()) {
            stabMode = 0;
        }
        result.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.CONTROL_VIDEO_STABILIZATION_MODE, (CaptureResult.Key<Integer>) Integer.valueOf(stabMode));
        if (Camera.Parameters.FOCUS_MODE_INFINITY.equals(params.getFocusMode())) {
            result.set((CaptureResult.Key<CaptureResult.Key<Float>>) CaptureResult.LENS_FOCUS_DISTANCE, (CaptureResult.Key<Float>) Float.valueOf(0.0f));
        }
        result.set((CaptureResult.Key<CaptureResult.Key<Float>>) CaptureResult.LENS_FOCAL_LENGTH, (CaptureResult.Key<Float>) Float.valueOf(params.getFocalLength()));
        result.set((CaptureResult.Key<CaptureResult.Key<Byte>>) CaptureResult.REQUEST_PIPELINE_DEPTH, (CaptureResult.Key<Byte>) ((Byte) characteristics.get(CameraCharacteristics.REQUEST_PIPELINE_MAX_DEPTH)));
        mapScaler(result, zoomData, params);
        result.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.SENSOR_TEST_PATTERN_MODE, (CaptureResult.Key<Integer>) 0);
        result.set((CaptureResult.Key<CaptureResult.Key<Location>>) CaptureResult.JPEG_GPS_LOCATION, (CaptureResult.Key<Location>) ((Location) request.get(CaptureRequest.JPEG_GPS_LOCATION)));
        result.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.JPEG_ORIENTATION, (CaptureResult.Key<Integer>) ((Integer) request.get(CaptureRequest.JPEG_ORIENTATION)));
        result.set((CaptureResult.Key<CaptureResult.Key<Byte>>) CaptureResult.JPEG_QUALITY, (CaptureResult.Key<Byte>) Byte.valueOf((byte) params.getJpegQuality()));
        result.set((CaptureResult.Key<CaptureResult.Key<Byte>>) CaptureResult.JPEG_THUMBNAIL_QUALITY, (CaptureResult.Key<Byte>) Byte.valueOf((byte) params.getJpegThumbnailQuality()));
        Camera.Size s = params.getJpegThumbnailSize();
        if (s != null) {
            result.set((CaptureResult.Key<CaptureResult.Key<Size>>) CaptureResult.JPEG_THUMBNAIL_SIZE, (CaptureResult.Key<Size>) ParameterUtils.convertSize(s));
        } else {
            Log.w(TAG, "Null thumbnail size received from parameters.");
        }
        result.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.NOISE_REDUCTION_MODE, (CaptureResult.Key<Integer>) ((Integer) request.get(CaptureRequest.NOISE_REDUCTION_MODE)));
        return result;
    }

    private static void mapAe(CameraMetadataNative m, CameraCharacteristics characteristics, CaptureRequest request, Rect activeArray, ParameterUtils.ZoomData zoomData, Camera.Parameters p) {
        m.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.CONTROL_AE_ANTIBANDING_MODE, (CaptureResult.Key<Integer>) Integer.valueOf(LegacyMetadataMapper.convertAntiBandingModeOrDefault(p.getAntibanding())));
        m.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION, (CaptureResult.Key<Integer>) Integer.valueOf(p.getExposureCompensation()));
        boolean lock = p.isAutoExposureLockSupported() ? p.getAutoExposureLock() : false;
        m.set((CaptureResult.Key<CaptureResult.Key<Boolean>>) CaptureResult.CONTROL_AE_LOCK, (CaptureResult.Key<Boolean>) Boolean.valueOf(lock));
        Boolean requestLock = (Boolean) request.get(CaptureRequest.CONTROL_AE_LOCK);
        if (!(requestLock == null || requestLock.booleanValue() == lock)) {
            Log.w(TAG, "mapAe - android.control.aeLock was requested to " + requestLock + " but resulted in " + lock);
        }
        mapAeAndFlashMode(m, characteristics, p);
        if (p.getMaxNumMeteringAreas() > 0) {
            m.set((CaptureResult.Key<CaptureResult.Key<MeteringRectangle[]>>) CaptureResult.CONTROL_AE_REGIONS, (CaptureResult.Key<MeteringRectangle[]>) getMeteringRectangles(activeArray, zoomData, p.getMeteringAreas(), "AE"));
        }
    }

    private static void mapAf(CameraMetadataNative m, Rect activeArray, ParameterUtils.ZoomData zoomData, Camera.Parameters p) {
        m.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.CONTROL_AF_MODE, (CaptureResult.Key<Integer>) Integer.valueOf(convertLegacyAfMode(p.getFocusMode())));
        if (p.getMaxNumFocusAreas() > 0) {
            m.set((CaptureResult.Key<CaptureResult.Key<MeteringRectangle[]>>) CaptureResult.CONTROL_AF_REGIONS, (CaptureResult.Key<MeteringRectangle[]>) getMeteringRectangles(activeArray, zoomData, p.getFocusAreas(), "AF"));
        }
    }

    private static void mapAwb(CameraMetadataNative m, Camera.Parameters p) {
        m.set((CaptureResult.Key<CaptureResult.Key<Boolean>>) CaptureResult.CONTROL_AWB_LOCK, (CaptureResult.Key<Boolean>) Boolean.valueOf(p.isAutoWhiteBalanceLockSupported() ? p.getAutoWhiteBalanceLock() : false));
        m.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.CONTROL_AWB_MODE, (CaptureResult.Key<Integer>) Integer.valueOf(convertLegacyAwbMode(p.getWhiteBalance())));
    }

    private static MeteringRectangle[] getMeteringRectangles(Rect activeArray, ParameterUtils.ZoomData zoomData, List<Camera.Area> meteringAreaList, String regionName) {
        List<MeteringRectangle> meteringRectList = new ArrayList<>();
        if (meteringAreaList != null) {
            for (Camera.Area area : meteringAreaList) {
                meteringRectList.add(ParameterUtils.convertCameraAreaToActiveArrayRectangle(activeArray, zoomData, area).toMetering());
            }
        }
        return (MeteringRectangle[]) meteringRectList.toArray(new MeteringRectangle[0]);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0050, code lost:
        if (r4.equals("off") != false) goto L_0x005e;
     */
    private static void mapAeAndFlashMode(CameraMetadataNative m, CameraCharacteristics characteristics, Camera.Parameters p) {
        int flashMode = 0;
        boolean z = false;
        int flashState = ((Boolean) characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)).booleanValue() ? null : 0;
        int aeMode = 1;
        String flashModeSetting = p.getFlashMode();
        if (flashModeSetting != null) {
            switch (flashModeSetting.hashCode()) {
                case 3551:
                    if (flashModeSetting.equals(Camera.Parameters.FLASH_MODE_ON)) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 109935:
                    break;
                case 3005871:
                    if (flashModeSetting.equals("auto")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 110547964:
                    if (flashModeSetting.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 1081542389:
                    if (flashModeSetting.equals(Camera.Parameters.FLASH_MODE_RED_EYE)) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                default:
                    z = true;
                    break;
            }
            if (z) {
                if (z) {
                    aeMode = 2;
                } else if (z) {
                    flashMode = 1;
                    aeMode = 3;
                    flashState = 3;
                } else if (z) {
                    aeMode = 4;
                } else if (!z) {
                    Log.w(TAG, "mapAeAndFlashMode - Ignoring unknown flash mode " + p.getFlashMode());
                } else {
                    flashMode = 2;
                    flashState = 3;
                }
            }
        }
        m.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.FLASH_STATE, (CaptureResult.Key<Integer>) flashState);
        m.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.FLASH_MODE, (CaptureResult.Key<Integer>) Integer.valueOf(flashMode));
        m.set((CaptureResult.Key<CaptureResult.Key<Integer>>) CaptureResult.CONTROL_AE_MODE, (CaptureResult.Key<Integer>) Integer.valueOf(aeMode));
    }

    private static int convertLegacyAfMode(String mode) {
        if (mode == null) {
            Log.w(TAG, "convertLegacyAfMode - no AF mode, default to OFF");
            return 0;
        }
        char c = 65535;
        switch (mode.hashCode()) {
            case -194628547:
                if (mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    c = 2;
                    break;
                }
                break;
            case 3005871:
                if (mode.equals("auto")) {
                    c = 0;
                    break;
                }
                break;
            case 3108534:
                if (mode.equals(Camera.Parameters.FOCUS_MODE_EDOF)) {
                    c = 3;
                    break;
                }
                break;
            case 97445748:
                if (mode.equals(Camera.Parameters.FOCUS_MODE_FIXED)) {
                    c = 5;
                    break;
                }
                break;
            case 103652300:
                if (mode.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                    c = 4;
                    break;
                }
                break;
            case 173173288:
                if (mode.equals(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                    c = 6;
                    break;
                }
                break;
            case 910005312:
                if (mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    c = 1;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return 1;
            case 1:
                return 4;
            case 2:
                return 3;
            case 3:
                return 5;
            case 4:
                return 2;
            case 5:
                return 0;
            case 6:
                return 0;
            default:
                Log.w(TAG, "convertLegacyAfMode - unknown mode " + mode + " , ignoring");
                return 0;
        }
    }

    private static int convertLegacyAwbMode(String mode) {
        if (mode == null) {
            return 1;
        }
        char c = 65535;
        switch (mode.hashCode()) {
            case -939299377:
                if (mode.equals(Camera.Parameters.WHITE_BALANCE_INCANDESCENT)) {
                    c = 1;
                    break;
                }
                break;
            case -719316704:
                if (mode.equals(Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT)) {
                    c = 3;
                    break;
                }
                break;
            case 3005871:
                if (mode.equals("auto")) {
                    c = 0;
                    break;
                }
                break;
            case 109399597:
                if (mode.equals(Camera.Parameters.WHITE_BALANCE_SHADE)) {
                    c = 7;
                    break;
                }
                break;
            case 474934723:
                if (mode.equals(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT)) {
                    c = 5;
                    break;
                }
                break;
            case 1650323088:
                if (mode.equals(Camera.Parameters.WHITE_BALANCE_TWILIGHT)) {
                    c = 6;
                    break;
                }
                break;
            case 1902580840:
                if (mode.equals(Camera.Parameters.WHITE_BALANCE_FLUORESCENT)) {
                    c = 2;
                    break;
                }
                break;
            case 1942983418:
                if (mode.equals(Camera.Parameters.WHITE_BALANCE_DAYLIGHT)) {
                    c = 4;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
            case 4:
                return 5;
            case 5:
                return 6;
            case 6:
                return 7;
            case 7:
                return 8;
            default:
                Log.w(TAG, "convertAwbMode - unrecognized WB mode " + mode);
                return 1;
        }
    }

    private static void mapScaler(CameraMetadataNative m, ParameterUtils.ZoomData zoomData, Camera.Parameters p) {
        m.set((CaptureResult.Key<CaptureResult.Key<Rect>>) CaptureResult.SCALER_CROP_REGION, (CaptureResult.Key<Rect>) zoomData.reportedCrop);
    }
}
