package ohos.media.camera.mode.adapter.key;

import android.hardware.camera2.CaptureRequest;
import android.util.Log;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.Pair;

public final class ModeRequestKey {
    public static final CaptureRequest.Key<Byte> AI_MOVIE_VALUE = new CaptureRequest.Key<>(ParameterKey.AI_MOVIE.getName(), Byte.class);
    public static final CaptureRequest.Key<Float> APERTURE_VALUE = new CaptureRequest.Key<>(ParameterKey.BOKEH_APERTURE.getName(), Float.TYPE);
    public static final CaptureRequest.Key<Byte> BOKEHSPOT_VALUE = new CaptureRequest.Key<>(ParameterKey.PORTRAIT_SPOTS_BOKEH.getName(), Byte.class);
    public static final CheckValid<Float> CHECK_DISTANCE_FLOAT_VALID = $$Lambda$ModeRequestKey$YCjpTpwny45MmpALVDHY7FnNUsc.INSTANCE;
    public static final CheckValid<Integer> CHECK_INT_RANGE_VALID = $$Lambda$ModeRequestKey$Ofg6f0kLXNx2qrv2WXNLDH3cyos.INSTANCE;
    public static final CheckValid<?> CHECK_LIST_VALID = $$Lambda$ModeRequestKey$xyKc6lASC4y5QrRs4LBA4ZBp6ys.INSTANCE;
    public static final CaptureRequest.Key<Float> EXPOSURE_COMPENSATION_VALUE = new CaptureRequest.Key<>(ParameterKey.EXPOSURE_COMPENSATION.getName(), Float.TYPE);
    public static final CaptureRequest.Key<Byte> FAIRLIGHT_VALUE = new CaptureRequest.Key<>(ParameterKey.PORTRAIT_FAIRLIGHT.getName(), Byte.class);
    public static final CaptureRequest.Key<Byte> FILTER_EFFECT_VALUE = new CaptureRequest.Key<>(ParameterKey.FILTER_EFFECT.getName(), Byte.TYPE);
    public static final CaptureRequest.Key<Integer> FILTER_LEVEL_VALUE = new CaptureRequest.Key<>(ParameterKey.FILTER_LEVEL.getName(), Integer.TYPE);
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ModeRequestKey.class);
    public static final CaptureRequest.Key<Long> MANUAL_EXPOSURE_VALUE = new CaptureRequest.Key<>(ParameterKey.SUPER_NIGHT_EXPOSURE.getName(), Long.class);
    public static final CaptureRequest.Key<Long> MANUAL_ISO_VALUE = new CaptureRequest.Key<>(ParameterKey.SUPER_NIGHT_ISO.getName(), Long.class);
    public static final CaptureRequest.Key<Boolean> MIRROR_VALUE = new CaptureRequest.Key<>(ParameterKey.IMAGE_MIRROR.getName(), Boolean.TYPE);
    public static final CaptureRequest.Key<Integer> PRO_AWB_VALUE = new CaptureRequest.Key<>(ParameterKey.PRO_AWB_TYPE.getName(), Integer.TYPE);
    public static final CaptureRequest.Key<Boolean> PRO_EXPOSURE_HINT_VALUE = new CaptureRequest.Key<>(ParameterKey.PRO_EXPOSURE_HINT.getName(), Boolean.TYPE);
    public static final CaptureRequest.Key<Float> PRO_FOCUS_DISTANCE_VALUE = new CaptureRequest.Key<>(ParameterKey.PRO_FOCUS_DISTANCE.getName(), Float.TYPE);
    public static final CaptureRequest.Key<Byte> PRO_METERING_VALUE = new CaptureRequest.Key<>(ParameterKey.PRO_METERING.getName(), Byte.TYPE);
    public static final CaptureRequest.Key<Integer> PRO_SENSOR_EXPOSURE_TIME_VALUE = new CaptureRequest.Key<>(ParameterKey.PRO_SENSOR_EXPOSURE_TIME.getName(), Integer.TYPE);
    public static final CaptureRequest.Key<Integer> PRO_SENSOR_ISO_VALUE = new CaptureRequest.Key<>(ParameterKey.PRO_SENSOR_ISO.getName(), Integer.TYPE);
    public static final CaptureRequest.Key<Integer> PRO_WB_VALUE = new CaptureRequest.Key<>(ParameterKey.PRO_MANUAL_WB.getName(), Integer.TYPE);
    private static final Map<String, Pair<PropertyKey.Key<?>, CheckValid<?>>> REQUEST_TO_RANGES = new HashMap();
    private static final Map<String, Pair<PropertyKey.Key<?>, CheckValid<?>>> REQUEST_TO_RANGES_BOOL = new HashMap();
    public static final CaptureRequest.Key<Boolean> SCENE_EFFECT_ENABLE = new CaptureRequest.Key<>(ParameterKey.SCENE_EFFECT_ENABLE.getName(), Boolean.TYPE);
    public static final CaptureRequest.Key<Boolean> SENSOR_HDR_VALUE = new CaptureRequest.Key<>(ParameterKey.SENSOR_HDR.getName(), Boolean.TYPE);
    public static final CaptureRequest.Key<Boolean> SMART_CAPTURE_VALUE = new CaptureRequest.Key<>(InnerParameterKey.SMART_CAPTURE.getName(), Boolean.TYPE);
    public static final CaptureRequest.Key<Boolean> VIDEO_STABILIZATION = new CaptureRequest.Key<>(ParameterKey.VIDEO_STABILIZATION.getName(), Boolean.TYPE);
    public static final CaptureRequest.Key<Boolean> WATER_MARK_VALUE = new CaptureRequest.Key<>(InnerParameterKey.WATER_MARK.getName(), Boolean.TYPE);

    public interface CheckValid<T> {
        boolean isValueValid(List<T> list, T t);
    }

    static {
        initCheckValid(APERTURE_VALUE, ModeCharacteristicKey.APERTURE_RANGE, CHECK_LIST_VALID);
        initCheckValid(FAIRLIGHT_VALUE, ModeCharacteristicKey.FAIRLIGHT_RANGE, CHECK_LIST_VALID);
        initCheckValid(BOKEHSPOT_VALUE, ModeCharacteristicKey.BOKEHSPOT_RANGE, CHECK_LIST_VALID);
        initCheckValid(SENSOR_HDR_VALUE, ModeCharacteristicKey.SENSOR_HDR_FUNCTION, CHECK_LIST_VALID);
        initCheckValid(WATER_MARK_VALUE, ModeCharacteristicKey.WATER_MARK_FUNCTION, CHECK_LIST_VALID);
        initCheckValid(MIRROR_VALUE, ModeCharacteristicKey.MIRROR_FUNCTION, CHECK_LIST_VALID);
        initCheckValid(SMART_CAPTURE_VALUE, ModeCharacteristicKey.SMART_CAPTURE_FUNCTION, CHECK_LIST_VALID);
        initCheckValid(SCENE_EFFECT_ENABLE, ModeCharacteristicKey.SCENE_DETECTION_FUNCTION, CHECK_LIST_VALID);
        initCheckValid(AI_MOVIE_VALUE, ModeCharacteristicKey.AI_MOVIE_RANGE, CHECK_LIST_VALID);
        initCheckValid(FILTER_EFFECT_VALUE, ModeCharacteristicKey.FILTER_EFFECT_TYPE, CHECK_LIST_VALID);
        initCheckValid(FILTER_LEVEL_VALUE, ModeCharacteristicKey.FILTER_EFFECT_LEVEL, CHECK_INT_RANGE_VALID);
        initCheckValid(MANUAL_ISO_VALUE, ModeCharacteristicKey.SUPERNIGHT_ISO_RANGE, CHECK_LIST_VALID);
        initCheckValid(MANUAL_EXPOSURE_VALUE, ModeCharacteristicKey.SUPERNIGHT_EXPOSURE_RANGE, CHECK_LIST_VALID);
        initCheckValid(VIDEO_STABILIZATION, ModeCharacteristicKey.VIDEO_STABILIZATION_FUNCTION, CHECK_LIST_VALID);
        initCheckValid(PRO_METERING_VALUE, ModeCharacteristicKey.PRO_METERING_RANGE, CHECK_LIST_VALID);
        initCheckValid(PRO_SENSOR_ISO_VALUE, ModeCharacteristicKey.PRO_SENSOR_ISO_RANGE, CHECK_LIST_VALID);
        initCheckValid(PRO_SENSOR_EXPOSURE_TIME_VALUE, ModeCharacteristicKey.PRO_EXPOSURE_TIME_RANGE, CHECK_LIST_VALID);
        initCheckValid(EXPOSURE_COMPENSATION_VALUE, ModeCharacteristicKey.PRO_EXPOSURE_COM_RANGE, CHECK_LIST_VALID);
        initCheckValid(PRO_FOCUS_DISTANCE_VALUE, ModeCharacteristicKey.PRO_FOCUS_DISTANCE_RANGE, CHECK_DISTANCE_FLOAT_VALID);
        initCheckValid(PRO_EXPOSURE_HINT_VALUE, ModeCharacteristicKey.PRO_EXPOSURE_HINT_FUNCTION, CHECK_LIST_VALID);
        initCheckValid(PRO_AWB_VALUE, ModeCharacteristicKey.PRO_AWB_RANGE, CHECK_LIST_VALID);
        initCheckValid(PRO_WB_VALUE, ModeCharacteristicKey.PRO_WB_RANGE, CHECK_INT_RANGE_VALID);
    }

    static /* synthetic */ boolean lambda$static$0(List list, Object obj) {
        if (list == null || list.isEmpty()) {
            Log.e("ModeRequestKey", "the range is null or it's size() is zero");
            return false;
        } else if (obj != null) {
            return list.contains(obj);
        } else {
            Log.e("ModeRequestKey", "the value is null");
            return false;
        }
    }

    static /* synthetic */ boolean lambda$static$1(List list, Float f) {
        if (list == null || list.isEmpty()) {
            LOGGER.error("the range is null or it's size() is zero", new Object[0]);
            return false;
        } else if (f == null) {
            LOGGER.error("the value is null", new Object[0]);
            return false;
        } else if (f.floatValue() >= ((Float) list.get(0)).floatValue()) {
            return true;
        } else {
            return false;
        }
    }

    static /* synthetic */ boolean lambda$static$2(List list, Integer num) {
        if (list == null || list.isEmpty()) {
            LOGGER.error("isValueValid: range is null", new Object[0]);
            return false;
        } else if (num == null) {
            LOGGER.error("isValueValid: value is null", new Object[0]);
            return false;
        } else if (((Integer) list.get(0)).intValue() > num.intValue() || num.intValue() > ((Integer) list.get(1)).intValue()) {
            return false;
        } else {
            return true;
        }
    }

    private ModeRequestKey() {
    }

    private static void initCheckValid(CaptureRequest.Key<?> key, PropertyKey.Key<?> key2, CheckValid<?> checkValid) {
        String name = key.getName();
        Class type = key.getNativeKey().getType();
        if (type == Boolean.TYPE || type == Boolean.class) {
            if (!REQUEST_TO_RANGES_BOOL.containsKey(name) && key2 != null) {
                REQUEST_TO_RANGES_BOOL.put(name, new Pair<>(key2, checkValid));
            }
        } else if (!REQUEST_TO_RANGES.containsKey(name) && key2 != null) {
            REQUEST_TO_RANGES.put(name, new Pair<>(key2, checkValid));
        }
    }

    public static Pair<Boolean, Pair<PropertyKey.Key<?>, CheckValid<?>>> getRangeKey(ParameterKey.Key<?> key) {
        if (key == null) {
            return null;
        }
        String name = key.getName();
        if (REQUEST_TO_RANGES_BOOL.containsKey(name)) {
            return new Pair<>(false, REQUEST_TO_RANGES_BOOL.get(name));
        }
        if (REQUEST_TO_RANGES.containsKey(name)) {
            return new Pair<>(true, REQUEST_TO_RANGES.get(name));
        }
        return null;
    }
}
