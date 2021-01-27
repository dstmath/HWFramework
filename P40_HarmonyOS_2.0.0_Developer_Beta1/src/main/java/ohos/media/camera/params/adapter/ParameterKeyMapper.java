package ohos.media.camera.params.adapter;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.util.Range;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.agp.utils.Rect;
import ohos.media.camera.device.adapter.utils.Converter;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.adapter.camera2ex.CameraMetadataEx;
import ohos.media.camera.params.adapter.camera2ex.CaptureRequestEx;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.Scope;

public class ParameterKeyMapper {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ParameterKeyMapper.class);
    private static final Map<CaptureRequest.Key<?>, SpecialMappedKeyMethod> MAPPED_METHOD_FOR_CAPTURE_REQUEST;
    private static final Map<ParameterKey.Key<?>, SpecialMappedKeyMethod> MAPPED_METHOD_FOR_PARAMETER;
    private static final Map<ParameterKey.Key<?>, MappedKey> PARAMETER_TO_REQUEST_MAPPER;

    static {
        HashMap hashMap = new HashMap();
        hashMap.put(ParameterKey.IMAGE_COMPRESSION_QUALITY, new MappedKey(CaptureRequest.JPEG_QUALITY, true));
        hashMap.put(ParameterKey.AUTO_ZOOM, new MappedKey(CaptureRequestEx.HUAWEI_AUTO_ZOOM, true));
        hashMap.put(ParameterKey.FACE_AE, new MappedKey(CaptureRequestEx.HUAWEI_FACE_AE, true));
        hashMap.put(ParameterKey.VENDOR_CUSTOM, new MappedKey(CaptureRequestEx.HUAWEI_VENDOR_CUSTOM, true));
        hashMap.put(InnerParameterKey.CAPTURE_MIRROR, new MappedKey(CaptureRequestEx.HUAWEI_CAPTURE_MIRROR, true));
        hashMap.put(InnerParameterKey.FACE_BEAUTY_MODE, new MappedKey(CaptureRequestEx.HUAWEI_FACE_BEAUTY_MODE, true));
        hashMap.put(InnerParameterKey.FACE_BEAUTY_LEVEL, new MappedKey(CaptureRequestEx.HUAWEI_FACE_BEAUTY_LEVEL, true));
        hashMap.put(InnerParameterKey.COLOR_EFFECT_MODE, new MappedKey(CaptureRequestEx.HUAWEI_COLOR_EFFECT_MODE, true));
        hashMap.put(InnerParameterKey.COLOR_EFFECT_LEVEL, new MappedKey(CaptureRequestEx.HUAWEI_COLOR_EFFECT_LEVEL, true));
        hashMap.put(InnerParameterKey.CONTRAST_VALUE, new MappedKey(CaptureRequestEx.HUAWEI_CONTRAST_VALUE, true));
        hashMap.put(InnerParameterKey.SATURATION_VALUE, new MappedKey(CaptureRequestEx.HUAWEI_SATURATION_VALUE, true));
        hashMap.put(InnerParameterKey.BRIGHTNESS_VALUE, new MappedKey(CaptureRequestEx.HUAWEI_BRIGHTNESS_VALUE, true));
        hashMap.put(InnerParameterKey.METERING_MODE, new MappedKey(CaptureRequestEx.HUAWEI_METERING_MODE, true));
        hashMap.put(InnerParameterKey.BURST_SNAPSHOT_MODE, new MappedKey(CaptureRequestEx.HUAWEI_BURST_SNAPSHOT_MODE, true));
        hashMap.put(InnerParameterKey.SMILE_DETECTION, new MappedKey(CaptureRequestEx.HUAWEI_SMILE_DETECTION, true));
        hashMap.put(InnerParameterKey.CAMERA_FLAG, new MappedKey(CaptureRequestEx.HUAWEI_CAMERA_FLAG, true));
        hashMap.put(InnerParameterKey.IMAGE_POST_PROCESS_MODE, new MappedKey(CaptureRequestEx.HUAWEI_IMAGE_POST_PROCESS_MODE, true));
        hashMap.put(InnerParameterKey.IMAGE_FOREGROUND_PROCESS_MODE, new MappedKey(CaptureRequestEx.HUAWEI_IMAGE_FOREGROUND_PROCESS_MODE, true));
        hashMap.put(InnerParameterKey.BEST_SHOT_MODE, new MappedKey(CaptureRequestEx.HUAWEI_BEST_SHOT_MODE, true));
        hashMap.put(InnerParameterKey.MANUAL_FOCUS_VALUE, new MappedKey(CaptureRequestEx.HUAWEI_MANUAL_FOCUS_VALUE, true));
        hashMap.put(InnerParameterKey.DUAL_SENSOR_MODE, new MappedKey(CaptureRequestEx.HUAWEI_DUAL_SENSOR_MODE, true));
        hashMap.put(InnerParameterKey.API_VERSION, new MappedKey(CaptureRequestEx.HUAWEI_API_VERSION, true));
        hashMap.put(InnerParameterKey.HIGH_VIDEO_FPS, new MappedKey(CaptureRequestEx.HUAWEI_HIGH_VIDEO_FPS, true));
        hashMap.put(InnerParameterKey.AF_TRIGGER_LOCK, new MappedKey(CaptureRequestEx.HUAWEI_AF_TRIGGER_LOCK, true));
        hashMap.put(InnerParameterKey.DM_WATERMARK_MODE, new MappedKey(CaptureRequestEx.HUAWEI_DM_WATERMARK_MODE, true));
        hashMap.put(InnerParameterKey.JPEG_FILE_NAME, new MappedKey(CaptureRequestEx.HUAWEI_JPEG_FILE_NAME, true));
        hashMap.put(InnerParameterKey.CAMERA_SCENE_MODE, new MappedKey(CaptureRequestEx.HAUWEI_CAMERA_SCENE_MODE, true));
        hashMap.put(InnerParameterKey.SMART_CAPTURE_ENABLE, new MappedKey(CaptureRequestEx.HUAWEI_SMART_CAPTURE_ENABLE, true));
        hashMap.put(InnerParameterKey.SENSOR_HDR_MODE, new MappedKey(CaptureRequestEx.HUAWEI_SENSOR_HDR_MODE, true));
        hashMap.put(InnerParameterKey.BEAUTY_MULTI_SETTING_MODE, new MappedKey(CaptureRequestEx.HUAWEI_BEAUTY_MULTI_SETTING_MODE, true));
        hashMap.put(InnerParameterKey.CONTROL_AE_REGIONS, new MappedKey(CaptureRequestEx.HUAWEI_CONTROL_AE_REGIONS, true));
        hashMap.put(InnerParameterKey.CONTROL_AF_REGIONS, new MappedKey(CaptureRequestEx.HUAWEI_CONTROL_AF_REGIONS, true));
        hashMap.put(InnerParameterKey.CAMERA_SESSION_SCENE_MODE, new MappedKey(CaptureRequestEx.HAUWEI_CAMERA_SESSION_SCENE_MODE, true));
        hashMap.put(InnerParameterKey.BODY_SHAPING_LEVEL, new MappedKey(CaptureRequestEx.HUAWEI_BODYSHAPING_LEVEL, true));
        hashMap.put(InnerParameterKey.MASTER_AI_ENABLE, new MappedKey(CaptureRequestEx.MASTER_AI_ENABLE, true));
        hashMap.put(InnerParameterKey.MASTER_AI_ENTER_MODE, new MappedKey(CaptureRequestEx.MASTER_AI_ENTER_MODE, true));
        hashMap.put(InnerParameterKey.SMART_SUGGEST_RECORD_CLEAR, new MappedKey(CaptureRequestEx.HAUWEI_SMART_SUGGEST_RECORD_CLEAR, true));
        hashMap.put(InnerParameterKey.SMART_SUGGEST_EXIT_MODE, new MappedKey(CaptureRequestEx.HAUWEI_SMART_SUGGEST_EXIT_MODE, true));
        hashMap.put(InnerParameterKey.SMART_SUGGEST_CONFIRM, new MappedKey(CaptureRequestEx.HAUWEI_SMART_SUGGEST_CONFIRM, true));
        hashMap.put(InnerParameterKey.SMART_SUGGEST_DISMISS, new MappedKey(CaptureRequestEx.HAUWEI_SMART_SUGGEST_DISMISS, true));
        hashMap.put(InnerParameterKey.VIDEO_DYNAMIC_FPS_MODE, new MappedKey(CaptureRequestEx.HW_VIDEO_DYNAMIC_FPS_MODE, true));
        hashMap.put(InnerParameterKey.REAL_VIDEO_SIZE, new MappedKey(CaptureRequestEx.HUAWEI_REAL_VIDEO_SIZE, true));
        hashMap.put(InnerParameterKey.LENS_OPTICAL_STABILIZATION_MODE, new MappedKey(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, true));
        hashMap.put(InnerParameterKey.LENS_FOCUS_DISTANCE, new MappedKey(CaptureRequest.LENS_FOCUS_DISTANCE, true));
        hashMap.put(InnerParameterKey.AE_PRECAPTURE_TRIGGER, new MappedKey(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, true));
        hashMap.put(InnerParameterKey.COLOR_MODE, new MappedKey(CaptureRequestEx.HUAWEI_COLOR_MODE, true));
        hashMap.put(ParameterKey.IMAGE_MIRROR, new MappedKey(CaptureRequestEx.HUAWEI_CAPTURE_MIRROR, false));
        hashMap.put(ParameterKey.VIDEO_STABILIZATION, new MappedKey(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, false));
        hashMap.put(InnerParameterKey.SCALER_CROP_REGION, new MappedKey(CaptureRequest.SCALER_CROP_REGION, false));
        hashMap.put(ParameterKey.EXPOSURE_FPS_RANGE, new MappedKey(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, false));
        PARAMETER_TO_REQUEST_MAPPER = Collections.unmodifiableMap(hashMap);
        HashMap hashMap2 = new HashMap();
        hashMap2.put(ParameterKey.IMAGE_MIRROR, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.ParameterKeyMapper.AnonymousClass1 */

            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedParameterKeyValue(Map<ParameterKey.Key<?>, Object> map) {
                Boolean bool = (Boolean) map.get(ParameterKey.IMAGE_MIRROR);
                if (bool != null) {
                    return bool.booleanValue() ? (T) CameraMetadataEx.MIRROR_ON : (T) CameraMetadataEx.MIRROR_OFF;
                }
                ParameterKeyMapper.LOGGER.debug("There is no IMAGE_MIRROR in configParameters", new Object[0]);
                return null;
            }
        });
        hashMap2.put(ParameterKey.VIDEO_STABILIZATION, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.ParameterKeyMapper.AnonymousClass2 */

            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedParameterKeyValue(Map<ParameterKey.Key<?>, Object> map) {
                Boolean bool = (Boolean) map.get(ParameterKey.VIDEO_STABILIZATION);
                if (bool != null) {
                    return bool.booleanValue() ? (T) 1 : (T) 0;
                }
                ParameterKeyMapper.LOGGER.debug("There is no VIDEO_STABILIZATION in configParameters", new Object[0]);
                return null;
            }
        });
        hashMap2.put(InnerParameterKey.SCALER_CROP_REGION, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.ParameterKeyMapper.AnonymousClass3 */

            /* access modifiers changed from: package-private */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedParameterKeyValue(Map<ParameterKey.Key<?>, Object> map) {
                Object obj = map.get(InnerParameterKey.SCALER_CROP_REGION);
                if (obj instanceof Rect) {
                    return (T) Converter.convert2ARect((Rect) obj);
                }
                ParameterKeyMapper.LOGGER.debug("There is no SCALER_CROP_REGION in configParameters", new Object[0]);
                return null;
            }
        });
        hashMap2.put(ParameterKey.EXPOSURE_FPS_RANGE, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.ParameterKeyMapper.AnonymousClass4 */

            /* access modifiers changed from: package-private */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedParameterKeyValue(Map<ParameterKey.Key<?>, Object> map) {
                Scope scope = (Scope) map.get(ParameterKey.EXPOSURE_FPS_RANGE);
                if (scope != null) {
                    return (T) Range.create((Integer) scope.getLower(), (Integer) scope.getUpper());
                }
                ParameterKeyMapper.LOGGER.debug("There is no EXPOSURE_FPS_RANGE in configParameters", new Object[0]);
                return null;
            }
        });
        MAPPED_METHOD_FOR_PARAMETER = Collections.unmodifiableMap(hashMap2);
        HashMap hashMap3 = new HashMap(2);
        hashMap3.put(CaptureRequestEx.HUAWEI_CAPTURE_MIRROR, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.ParameterKeyMapper.AnonymousClass5 */

            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedRequestKeyValue(CameraMetadataNative cameraMetadataNative) {
                Byte b = (Byte) cameraMetadataNative.get(CaptureRequestEx.HUAWEI_CAPTURE_MIRROR);
                if (b == null) {
                    ParameterKeyMapper.LOGGER.debug("There is no HUAWEI_CAPTURE_MIRROR in CameraMetadataNative", new Object[0]);
                    return null;
                } else if (CameraMetadataEx.MIRROR_OFF.equals(b)) {
                    return (T) true;
                } else {
                    if (CameraMetadataEx.MIRROR_ON.equals(b)) {
                        return (T) false;
                    }
                    ParameterKeyMapper.LOGGER.debug("HUAWEI_CAPTURE_MIRROR value (%{public}d) is invalid", b);
                    return null;
                }
            }
        });
        hashMap3.put(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.ParameterKeyMapper.AnonymousClass6 */

            /* access modifiers changed from: package-private */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedRequestKeyValue(CameraMetadataNative cameraMetadataNative) {
                Integer num = (Integer) cameraMetadataNative.get(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE);
                if (num == null) {
                    ParameterKeyMapper.LOGGER.debug("There is no CONTROL_VIDEO_STABILIZATION_MODE in CameraMetadataNative", new Object[0]);
                    return null;
                } else if (num.intValue() == 1) {
                    return (T) true;
                } else {
                    if (num.intValue() == 0) {
                        return (T) false;
                    }
                    ParameterKeyMapper.LOGGER.debug("CONTROL_VIDEO_STABILIZATION_MODE value(%{public}d) is invalid", num);
                    return null;
                }
            }
        });
        hashMap3.put(CaptureRequest.SCALER_CROP_REGION, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.ParameterKeyMapper.AnonymousClass7 */

            /* access modifiers changed from: package-private */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedRequestKeyValue(CameraMetadataNative cameraMetadataNative) {
                android.graphics.Rect rect = (android.graphics.Rect) cameraMetadataNative.get(CaptureRequest.SCALER_CROP_REGION);
                if (rect != null) {
                    return (T) Converter.convert2ZRect(rect);
                }
                ParameterKeyMapper.LOGGER.debug("There is no SCALER_CROP_REGION", new Object[0]);
                return null;
            }
        });
        hashMap3.put(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.ParameterKeyMapper.AnonymousClass8 */

            /* access modifiers changed from: package-private */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedRequestKeyValue(CameraMetadataNative cameraMetadataNative) {
                Range range = (Range) cameraMetadataNative.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);
                if (range != null) {
                    return (T) Scope.create((Integer) range.getLower(), (Integer) range.getUpper());
                }
                ParameterKeyMapper.LOGGER.debug("There is no CONTROL_AE_TARGET_FPS_RANGE in CameraMetadataNative", new Object[0]);
                return null;
            }
        });
        MAPPED_METHOD_FOR_CAPTURE_REQUEST = Collections.unmodifiableMap(hashMap3);
    }

    public static CameraMetadataNative getCameraRequestMetadatas(Map<ParameterKey.Key<?>, Object> map, CameraMetadataNative cameraMetadataNative) {
        CameraMetadataNative cameraMetadataNative2;
        if (cameraMetadataNative == null) {
            cameraMetadataNative2 = new CameraMetadataNative();
        } else {
            cameraMetadataNative2 = new CameraMetadataNative(cameraMetadataNative);
        }
        for (Map.Entry<ParameterKey.Key<?>, Object> entry : map.entrySet()) {
            ParameterKey.Key<?> key = entry.getKey();
            try {
                MappedKey mappedKey = PARAMETER_TO_REQUEST_MAPPER.get(key);
                if (mappedKey == null) {
                    LOGGER.debug("There is no mapped key for %{public}s in the PARAMETER_TO_REQUEST_MAPPER", key.toString());
                } else {
                    CameraMetadataNative.Key nativeKey = mappedKey.getKey().getNativeKey();
                    if (mappedKey.isDirectMapped()) {
                        cameraMetadataNative2.set(nativeKey, entry.getValue());
                    } else {
                        SpecialMappedKeyMethod specialMappedKeyMethod = MAPPED_METHOD_FOR_PARAMETER.get(key);
                        if (specialMappedKeyMethod == null) {
                            LOGGER.debug("There is no mapped method for %{public}s in MAPPED_METHOD_FOR_PARAMETER", key.toString());
                        } else {
                            Object mappedParameterKeyValue = specialMappedKeyMethod.getMappedParameterKeyValue(map);
                            if (mappedParameterKeyValue != null) {
                                cameraMetadataNative2.set(nativeKey, mappedParameterKeyValue);
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error("Failed to get %{public}s, exception: %{public}s", key.toString(), e.toString());
            }
        }
        return cameraMetadataNative2;
    }

    public static Map<ParameterKey.Key<?>, Object> getParameterKeyValues(CameraMetadataNative cameraMetadataNative, List<ParameterKey.Key<?>> list) {
        HashMap hashMap = new HashMap(list.size());
        for (ParameterKey.Key<?> key : list) {
            try {
                MappedKey mappedKey = PARAMETER_TO_REQUEST_MAPPER.get(key);
                if (mappedKey != null) {
                    CaptureRequest.Key<?> key2 = mappedKey.getKey();
                    if (mappedKey.isDirectMapped()) {
                        Object obj = cameraMetadataNative.get(key2);
                        if (obj != null) {
                            hashMap.put(key, obj);
                        }
                    } else {
                        SpecialMappedKeyMethod specialMappedKeyMethod = MAPPED_METHOD_FOR_CAPTURE_REQUEST.get(key2);
                        if (specialMappedKeyMethod == null) {
                            LOGGER.warn("There is no mapped method for %{public}s in MAPPED_METHOD_FOR_CAPTURE_REQUEST", key2.toString());
                        } else {
                            Object mappedRequestKeyValue = specialMappedKeyMethod.getMappedRequestKeyValue(cameraMetadataNative);
                            if (mappedRequestKeyValue != null) {
                                hashMap.put(key, mappedRequestKeyValue);
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error("Failed to get %{public}s, exception: %{public}s", key.toString(), e.toString());
            }
        }
        return hashMap;
    }

    /* access modifiers changed from: package-private */
    public static class MappedKey {
        private final boolean isDirectMapped;
        private final CaptureRequest.Key<?> key;

        public MappedKey(CaptureRequest.Key<?> key2, boolean z) {
            this.key = key2;
            this.isDirectMapped = z;
        }

        public boolean isDirectMapped() {
            return this.isDirectMapped;
        }

        public CaptureRequest.Key<?> getKey() {
            return this.key;
        }
    }
}
