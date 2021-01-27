package ohos.media.camera.params.adapter;

import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import com.android.internal.util.ArrayUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import ohos.media.camera.device.adapter.utils.Converter;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.adapter.camera2ex.CameraCharacteristicsEx;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

/* access modifiers changed from: package-private */
public final class PropertyKeyMapper {
    private static final byte CAPTURE_MIRROR_SUPPORT = 1;
    private static final Map<PropertyKey.Key<?>, CameraCharacteristics.Key<?>> DIRECT_KEY_MAP;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(PropertyKeyMapper.class);
    private static final Map<PropertyKey.Key<?>, SpecialMappedKeyMethod> SPECIAL_KEY_MAP;
    private final CameraCharacteristics cameraCharacteristics;

    static {
        HashMap hashMap = new HashMap();
        hashMap.put(PropertyKey.SENSOR_ORIENTATION, CameraCharacteristics.SENSOR_ORIENTATION);
        hashMap.put(PropertyKey.PARTIAL_RESULT_COUNT, CameraCharacteristics.REQUEST_PARTIAL_RESULT_COUNT);
        hashMap.put(InnerPropertyKey.ZOOM_RATIO, CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
        hashMap.put(InnerPropertyKey.AVAILABLE_CAPABILITIES, CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
        hashMap.put(InnerPropertyKey.LOGICAL_CAMERA_PHYSICAL_IDS, CameraCharacteristics.LOGICAL_MULTI_CAMERA_PHYSICAL_IDS);
        hashMap.put(InnerPropertyKey.CAPTURE_MIRROR_SUPPORTED, CameraCharacteristicsEx.HUAWEI_CAPTURE_MIRROR_SUPPORTED);
        hashMap.put(InnerPropertyKey.FACE_BEAUTY_SUPPORTED, CameraCharacteristicsEx.HUAWEI_FACE_BEAUTY_SUPPORTED);
        hashMap.put(InnerPropertyKey.FACE_BEAUTY_RANGE, CameraCharacteristicsEx.HUAWEI_FACE_BEAUTY_RANGE);
        hashMap.put(InnerPropertyKey.AVAILABLE_COLOR_EFFECT_MODES, CameraCharacteristicsEx.HUAWEI_AVAILABLE_COLOR_EFFECT_MODES);
        hashMap.put(InnerPropertyKey.AVAILABLE_COLOR_EFFECT_RANGE, CameraCharacteristicsEx.HUAWEI_AVAILABLE_COLOR_EFFECT_RANGE);
        hashMap.put(InnerPropertyKey.VIDEO_BEAUTY_SUPPORTED, CameraCharacteristicsEx.HUAWEI_VIDEO_BEAUTY_SUPPORTED);
        hashMap.put(InnerPropertyKey.AVAILABLE_CONTRAST, CameraCharacteristicsEx.HUAWEI_AVAILABLE_CONTRAST);
        hashMap.put(InnerPropertyKey.AVAILABLE_SATURATION, CameraCharacteristicsEx.HUAWEI_AVAILABLE_SATURATION);
        hashMap.put(InnerPropertyKey.AVAILABLE_BRIGHTNESS, CameraCharacteristicsEx.HUAWEI_AVAILABLE_BRIGHTNESS);
        hashMap.put(InnerPropertyKey.AVAILABLE_DUAL_PRIMARY, CameraCharacteristicsEx.HUAWEI_AVAILABLE_DUAL_PRIMARY);
        hashMap.put(InnerPropertyKey.SUPPORTED_COLOR_MODES, CameraCharacteristicsEx.HUAWEI_SUPPORTED_COLOR_MODES);
        hashMap.put(InnerPropertyKey.AF_TRIGGER_LOCK_SUPPORTED, CameraCharacteristicsEx.HUAWEI_AF_TRIGGER_LOCK_SUPPORTED);
        hashMap.put(InnerPropertyKey.DM_WATERMARK_SUPPORTED, CameraCharacteristicsEx.HUAWEI_DM_WATERMARK_SUPPORTED);
        hashMap.put(InnerPropertyKey.DUAL_PRIMARY_SINGLE_REPROCESS, CameraCharacteristicsEx.HUAWEI_DUAL_PRIMARY_SINGLE_REPROCESS);
        hashMap.put(InnerPropertyKey.SUPER_RESOLUTION_PICTURE_SIZE, CameraCharacteristicsEx.HUAWEI_SUPER_RESOLUTION_PICTURE_SIZE);
        hashMap.put(InnerPropertyKey.PORTRAIT_MODE_SUPPORTED, CameraCharacteristicsEx.HUAWEI_PORTRAIT_MODE_SUPPORTED);
        hashMap.put(InnerPropertyKey.BIG_APERTURE_RESOLUTION_SUPPORTED, CameraCharacteristicsEx.HUAWEI_BIG_APERTURE_RESOLUTION_SUPPORTED);
        hashMap.put(InnerPropertyKey.SMART_SUGGEST_SUPPORT, CameraCharacteristicsEx.HAUWEI_SMART_SUGGEST_SUPPORT);
        hashMap.put(InnerPropertyKey.SMART_CAPTURE_SUPPORT, CameraCharacteristicsEx.HUAWEI_SMART_CAPTURE_SUPPORT);
        hashMap.put(InnerPropertyKey.PORTRAIT_MOVIE_MODE_SUPPORTED, CameraCharacteristicsEx.HUAWEI_PORTRAIT_MOVIE_MODE_SUPPORTED);
        hashMap.put(InnerPropertyKey.SENSOR_HDR_SUPPORTED, CameraCharacteristicsEx.HUAWEI_SENSOR_HDR_SUPPORTED);
        hashMap.put(InnerPropertyKey.AVAILABLE_VIDEO_SENSOR_HDR_CONFIGURATIONS, CameraCharacteristicsEx.HUAWEI_AVAILABLE_VIDEO_SENSORHDR_CONFIGURATIONS);
        hashMap.put(InnerPropertyKey.WIDE_ANGLE_ZOOM_CAPABILITY, CameraCharacteristicsEx.HUAWEI_WIDE_ANGLE_ZOOM_CAPABILITY);
        hashMap.put(InnerPropertyKey.BEAUTY_SETTING_SUPPORTED, CameraCharacteristicsEx.HUAWEI_BEAUTY_SETTING_SUPPORTED);
        hashMap.put(InnerPropertyKey.BEAUTY_SETTING_DEFAULT_PARA, CameraCharacteristicsEx.HUAWEI_BEAUTY_SETTING_DEFAULT_PARA);
        hashMap.put(InnerPropertyKey.BEAUTY_SETTING_SKIN_SMOOTH, CameraCharacteristicsEx.HUAWEI_BEAUTY_SETTING_SKIN_SMOOTH);
        hashMap.put(InnerPropertyKey.BEAUTY_SETTING_FRONT_SKIN_TONE, CameraCharacteristicsEx.HUAWEI_BEAUTY_SETTING_FRONT_SKIN_TONE);
        hashMap.put(InnerPropertyKey.BEAUTY_SETTING_REAR_SKIN_TONE, CameraCharacteristicsEx.HUAWEI_BEAUTY_SETTING_REAR_SKIN_TONE);
        hashMap.put(InnerPropertyKey.BEAUTY_SETTING_FRONT_FACE_SLENDER, CameraCharacteristicsEx.HUAWEI_BEAUTY_SETTING_FRONT_FACE_SLENDER);
        hashMap.put(InnerPropertyKey.BEAUTY_SETTING_REAR_FACE_SLENDER, CameraCharacteristicsEx.HUAWEI_BEAUTY_SETTING_REAR_FACE_SLENDER);
        hashMap.put(InnerPropertyKey.BEAUTY_SETTING_SKIN_SMOOTH_VALUES, CameraCharacteristicsEx.HUAWEI_BEAUTY_SETTING_SKIN_SMOOTH_VALUES);
        hashMap.put(InnerPropertyKey.BEAUTY_SETTING_FACE_SLENDER_VALUES, CameraCharacteristicsEx.HUAWEI_BEAUTY_SETTING_FACE_SLENDER_VALUES);
        hashMap.put(InnerPropertyKey.FULL_RESOLUTION_SUPPORT_FEATUREE, CameraCharacteristicsEx.HUAWEI_FULLRESOLUTION_SUPPORT_FEATUREE);
        hashMap.put(InnerPropertyKey.AVAILABLE_VIDEO_STABILIZATION_CONFIGURATIONS, CameraCharacteristicsEx.HUAWEI_AVAILABLE_VIDEO_STABILIZATION_CONFIGURATIONS);
        hashMap.put(InnerPropertyKey.AI_VIDEO_SUPPORT, CameraCharacteristicsEx.HUAWEI_AI_VIDEO_SUPPORT);
        hashMap.put(InnerPropertyKey.BODYSHAPING_MODE_SUPPORTED, CameraCharacteristicsEx.HUAWEI_BODYSHAPING_MODE_SUPPORTED);
        hashMap.put(InnerPropertyKey.QUARTER_SIZE, CameraCharacteristicsEx.HUAWEI_QUARTER_SIZE);
        hashMap.put(InnerPropertyKey.OVERDEFAULT_RESOLUTION_PICTURE_SIZE, CameraCharacteristicsEx.HUAWEI_OVERDEFAULT_RESOLUTION_PICTURE_SIZE);
        hashMap.put(InnerPropertyKey.AVAILABLE_VIDEO_WIDE_CONFIGURATIONS, CameraCharacteristicsEx.HUAWEI_AVAILABLE_VIDEO_WIDE_CONFIGURATIONS);
        hashMap.put(InnerPropertyKey.AI_SHAPING_SUPPORT, CameraCharacteristicsEx.HUAWEI_AISHAPING_SUPPORT);
        hashMap.put(InnerPropertyKey.AI_SHAPING_VALUES, CameraCharacteristicsEx.HUAWEI_AISHAPING_VALUES);
        hashMap.put(InnerPropertyKey.BEAUTY_STABILIZATION_SUPPORTED, CameraCharacteristicsEx.HUAWEI_BEAUTY_STABILIZATION_SUPPORTED);
        hashMap.put(InnerPropertyKey.HIGH_RESOLUTION_BEAUTY_SUPPORTED, CameraCharacteristicsEx.HUAWEI_HIGH_RESOLUTION_BEAUTY_SUPPORTED);
        hashMap.put(InnerPropertyKey.VIRTUAL_CAMERA_TYPE, CameraCharacteristicsEx.HUAWEI_VIRTUAL_CAMERA_TYPE);
        hashMap.put(InnerPropertyKey.WIDE_ANGLE_SUPPORT, CameraCharacteristicsEx.HUAWEI_WIDE_ANGLE_SUPPORT);
        hashMap.put(InnerPropertyKey.TELE_MODE_SUPPORT, CameraCharacteristicsEx.HUAWEI_TELE_MODE_SUPPORT);
        hashMap.put(InnerPropertyKey.AUTO_ZOOM_SUPPORT, CameraCharacteristicsEx.HUAWEI_AUTO_ZOOM_SUPPORTED);
        hashMap.put(InnerPropertyKey.FACE_AE_SUPPORT, CameraCharacteristicsEx.HUAWEI_FACE_AE_SUPPORTED);
        hashMap.put(InnerPropertyKey.VENDOR_CUSTOM_SUPPORT, CameraCharacteristicsEx.HUAWEI_VENDOR_CUSTOM_SUPPORTED);
        DIRECT_KEY_MAP = Collections.unmodifiableMap(hashMap);
        HashMap hashMap2 = new HashMap(8);
        hashMap2.put(InnerPropertyKey.MIRROR_FUNCTION, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.PropertyKeyMapper.AnonymousClass1 */

            /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: android.hardware.camera2.CameraCharacteristics */
            /* JADX WARN: Multi-variable type inference failed */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedPropertyKeyValue(CameraCharacteristics cameraCharacteristics) {
                Byte b = (Byte) cameraCharacteristics.get(CameraCharacteristicsEx.HUAWEI_CAPTURE_MIRROR_SUPPORTED);
                boolean z = false;
                if (b == null) {
                    PropertyKeyMapper.LOGGER.debug("There is no HUAWEI_CAPTURE_MIRROR_SUPPORTED in CameraCharacteristicsEx", new Object[0]);
                    return (T) false;
                }
                if (b.byteValue() == 1) {
                    z = true;
                }
                return (T) Boolean.valueOf(z);
            }
        });
        hashMap2.put(InnerPropertyKey.LENS_FACING, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.PropertyKeyMapper.AnonymousClass2 */

            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedPropertyKeyValue(CameraCharacteristics cameraCharacteristics) {
                Integer num = (Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                T t = (T) -1;
                if (num == null) {
                    PropertyKeyMapper.LOGGER.debug("There is no LENS_FACING in CameraCharacteristics", new Object[0]);
                    return t;
                }
                int intValue = num.intValue();
                return intValue != 0 ? intValue != 1 ? t : (T) 1 : (T) 0;
            }
        });
        hashMap2.put(InnerPropertyKey.FACE_DETECT_MODE, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.PropertyKeyMapper.AnonymousClass3 */

            /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: android.hardware.camera2.CameraCharacteristics */
            /* JADX DEBUG: Multi-variable search result rejected for r7v1, resolved type: java.util.HashSet */
            /* JADX WARN: Multi-variable type inference failed */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedPropertyKeyValue(CameraCharacteristics cameraCharacteristics) {
                HashSet hashSet = new HashSet();
                int[] iArr = (int[]) cameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
                Byte b = (Byte) cameraCharacteristics.get(CameraCharacteristicsEx.HUAWEI_SMILE_DETECTION_SUPPORTED);
                if (iArr == null) {
                    PropertyKeyMapper.LOGGER.debug("There is no STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES in CameraCharacteristics", new Object[0]);
                    return null;
                }
                for (int i : iArr) {
                    if (i == 0) {
                        PropertyKeyMapper.LOGGER.debug("Face detect mode is off(%{public}d), no need to process", Integer.valueOf(i));
                    } else if (i == 1 || i == 2) {
                        hashSet.add(1);
                        if (b != null && b.byteValue() == 1) {
                            hashSet.add(2);
                        }
                    } else {
                        PropertyKeyMapper.LOGGER.warn("Unknown FACE_DETECT_MODE %{public}d:", Integer.valueOf(i));
                    }
                }
                return (T) hashSet.toArray(new Integer[0]);
            }
        });
        hashMap2.put(InnerPropertyKey.AF_MODE, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.PropertyKeyMapper.AnonymousClass4 */

            /* JADX DEBUG: Multi-variable search result rejected for r8v1, resolved type: java.util.ArrayList */
            /* JADX WARN: Multi-variable type inference failed */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedPropertyKeyValue(CameraCharacteristics cameraCharacteristics) {
                ArrayList arrayList = new ArrayList();
                int[] iArr = (int[]) cameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
                if (iArr == null) {
                    PropertyKeyMapper.LOGGER.debug("There is no CONTROL_AF_AVAILABLE_MODES in CameraCharacteristics", new Object[0]);
                    return (T) arrayList.toArray();
                }
                boolean z = false;
                boolean z2 = false;
                for (int i : iArr) {
                    if (i != 0) {
                        if (i == 1) {
                            arrayList.add(2);
                        } else if (i != 2) {
                            if (i == 3) {
                                z2 = true;
                            } else if (i != 4) {
                                PropertyKeyMapper.LOGGER.warn("Unknown AF_MODE %{public}d", Integer.valueOf(i));
                            } else {
                                z = true;
                            }
                        }
                    }
                    PropertyKeyMapper.LOGGER.debug("AF mode(%{public}d) is off or macro, no need to process", Integer.valueOf(i));
                }
                if (z && z2) {
                    arrayList.add(1);
                }
                return (T) arrayList.toArray(new Integer[0]);
            }
        });
        hashMap2.put(InnerPropertyKey.AE_MODE, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.PropertyKeyMapper.AnonymousClass5 */

            /* JADX DEBUG: Multi-variable search result rejected for r5v1, resolved type: java.util.ArrayList */
            /* JADX WARN: Multi-variable type inference failed */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedPropertyKeyValue(CameraCharacteristics cameraCharacteristics) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(0);
                int[] iArr = (int[]) cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
                if (iArr == null) {
                    PropertyKeyMapper.LOGGER.debug("There is no CONTROL_AE_AVAILABLE_MODES in CameraCharacteristics", new Object[0]);
                    return (T) arrayList.toArray(new Integer[0]);
                }
                int length = iArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (iArr[i] == 1) {
                        arrayList.add(1);
                        break;
                    } else {
                        i++;
                    }
                }
                return (T) arrayList.toArray(new Integer[0]);
            }
        });
        hashMap2.put(PropertyKey.VIDEO_STABILIZATION_SUPPORT, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.PropertyKeyMapper.AnonymousClass6 */

            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedPropertyKeyValue(CameraCharacteristics cameraCharacteristics) {
                int[] iArr = (int[]) cameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
                T t = (T) false;
                if (iArr == null) {
                    PropertyKeyMapper.LOGGER.debug("There is no CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES in CameraCharacteristics", new Object[0]);
                    return t;
                }
                if (ArrayUtils.contains(iArr, 1)) {
                    t = (T) true;
                }
                PropertyKeyMapper.LOGGER.info("VIDEO_STABILIZATION_SUPPORT %{public}s", t.toString());
                return t;
            }
        });
        hashMap2.put(InnerPropertyKey.LINK_TYPE, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.PropertyKeyMapper.AnonymousClass7 */
            private static final int VIRTUAL_CAMERA_TYPE_MSDP = 1;

            /* access modifiers changed from: package-private */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedPropertyKeyValue(CameraCharacteristics cameraCharacteristics) {
                Integer num = (Integer) cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                T t = (T) -1;
                if (num == null) {
                    PropertyKeyMapper.LOGGER.debug("There is no INFO_SUPPORTED_HARDWARE_LEVEL in CameraCharacteristics", new Object[0]);
                    return t;
                }
                int intValue = num.intValue();
                if (intValue == 0 || intValue == 1 || intValue == 2 || intValue == 3) {
                    PropertyKeyMapper.LOGGER.debug("INFO_SUPPORTED_HARDWARE_LEVEL is %{public}d, convert device link type to native", num);
                    return (T) 0;
                } else if (intValue != 4) {
                    PropertyKeyMapper.LOGGER.warn("Unknown INFO_SUPPORTED_HARDWARE_LEVEL: %{public}d, convert device link type to others", num);
                    return t;
                } else if (isMsdpLinkType(cameraCharacteristics)) {
                    PropertyKeyMapper.LOGGER.debug("INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL type is msdp, convert device link type to msdp", new Object[0]);
                    return (T) 2;
                } else {
                    PropertyKeyMapper.LOGGER.debug("INFO_SUPPORTED_HARDWARE_LEVEL is %{public}d, convert device link type to usb", num);
                    return (T) 1;
                }
            }

            private boolean isMsdpLinkType(CameraCharacteristics cameraCharacteristics) {
                try {
                    Integer num = (Integer) cameraCharacteristics.get(CameraCharacteristicsEx.HUAWEI_VIRTUAL_CAMERA_TYPE);
                    if (num != null && num.intValue() == 1) {
                        return true;
                    }
                    PropertyKeyMapper.LOGGER.debug("Virtual camera type is normal(%{public}d), fallback to check other types", num);
                    return false;
                } catch (IllegalArgumentException unused) {
                    PropertyKeyMapper.LOGGER.error("Failed to get HUAWEI_VIRTUAL_CAMERA_TYPE from CameraCharacteristics, fallback to check other types", new Object[0]);
                }
            }
        });
        hashMap2.put(InnerPropertyKey.FLASH_MODE, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.PropertyKeyMapper.AnonymousClass8 */

            /* JADX DEBUG: Multi-variable search result rejected for r4v1, resolved type: java.util.ArrayList */
            /* JADX WARN: Multi-variable type inference failed */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedPropertyKeyValue(CameraCharacteristics cameraCharacteristics) {
                Boolean bool = (Boolean) cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                ArrayList arrayList = new ArrayList();
                if (bool == null) {
                    PropertyKeyMapper.LOGGER.debug("There is no FLASH_INFO_AVAILABLE in CameraCharacteristics", new Object[0]);
                    return (T) arrayList.toArray(new Integer[0]);
                }
                if (bool.booleanValue()) {
                    arrayList.add(0);
                    arrayList.add(1);
                    arrayList.add(2);
                    arrayList.add(3);
                }
                return (T) arrayList.toArray(new Integer[0]);
            }
        });
        hashMap2.put(InnerPropertyKey.SENSOR_INFO_ACTIVE_ARRAY_SIZE, new SpecialMappedKeyMethod() {
            /* class ohos.media.camera.params.adapter.PropertyKeyMapper.AnonymousClass9 */

            /* access modifiers changed from: package-private */
            @Override // ohos.media.camera.params.adapter.SpecialMappedKeyMethod
            public <T> T getMappedPropertyKeyValue(CameraCharacteristics cameraCharacteristics) {
                Rect rect = (Rect) cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                if (rect != null) {
                    return (T) Converter.convert2ZRect(rect);
                }
                PropertyKeyMapper.LOGGER.debug("There is no SENSOR_INFO_ACTIVE_ARRAY_SIZE in CameraCharacteristics", new Object[0]);
                return null;
            }
        });
        SPECIAL_KEY_MAP = Collections.unmodifiableMap(hashMap2);
    }

    PropertyKeyMapper(CameraCharacteristics cameraCharacteristics2) {
        this.cameraCharacteristics = cameraCharacteristics2;
    }

    public <T> Optional<T> getValue(PropertyKey.Key<T> key) {
        try {
            Optional<CameraCharacteristics.Key<?>> mappedKey = getMappedKey(key);
            if (mappedKey.isPresent()) {
                return Optional.ofNullable(this.cameraCharacteristics.get(mappedKey.get()));
            }
            SpecialMappedKeyMethod specialMappedKeyMethod = SPECIAL_KEY_MAP.get(key);
            if (specialMappedKeyMethod != null) {
                return Optional.ofNullable(specialMappedKeyMethod.getMappedPropertyKeyValue(this.cameraCharacteristics));
            }
            LOGGER.warn("Get mapped key %{public}s value fails", key.toString());
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            LOGGER.error("Failed to get %{public}s, exception: %{public}s", key.toString(), e.toString());
            return Optional.empty();
        }
    }

    private Optional<CameraCharacteristics.Key<?>> getMappedKey(PropertyKey.Key<?> key) {
        return Optional.ofNullable(DIRECT_KEY_MAP.get(key));
    }
}
