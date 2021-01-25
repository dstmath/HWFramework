package ohos.media.camera.mode.adapter.utils;

import android.graphics.Point;
import android.view.WindowManager;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import ohos.app.Context;
import ohos.light.bean.LightEffect;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class CustomConfigurationUtil {
    private static final int BEAUTY_DEFAULT_VALUE = 5;
    private static final String CURVED_SIDE = "ro.config.hw_curved_side_disp";
    private static final int DEFAULT_PADDING = 0;
    private static final int DOMESTIC_BETA_VERSION = 3;
    private static final int DOMESTIC_COMMERCIAL_VERSION = 1;
    private static final int HIGH_PIXELS_VALUE = 800;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CustomConfigurationUtil.class);
    private static final int MAX_SYSTEM_PROPERTIES = 300;
    private static final String NULL_STRING = "";
    private static final int OVERSEA_BETA_VERSION = 5;
    private static final int PANEL_ORIENTATION = 90;
    private static final int SDK_VERSION_DEFAULT_VALUE = 0;
    private static final String SPLIT_SIGNAL = ",";
    private static Method getBrandMethod;
    private static Class<?> hwDeliverInfoClass;
    private static boolean isFyuseSdkSupport = false;
    private static ReflectClass properties;
    private static Map<String, Object> systemPropertiesMaps = new HashMap(300);

    private @interface CurvedSideIndex {
        public static final int LEFT_PADDING = 0;
        public static final int MAX = 4;
        public static final int PADDING_BASE_LENGTH = 3;
        public static final int RIGHT_PADDING = 2;
    }

    static {
        hwDeliverInfoClass = null;
        getBrandMethod = null;
        try {
            properties = new ReflectClass("android.os.SystemProperties");
            LOGGER.info("Initialize hwDeliverInfoClass.", new Object[0]);
            hwDeliverInfoClass = Class.forName("com.huawei.deliver.info.HwDeliverInfo");
            getBrandMethod = hwDeliverInfoClass.getDeclaredMethod("getBrand", new Class[0]);
        } catch (ClassNotFoundException unused) {
            LOGGER.error("Initialize hwDeliverInfoClass failed, ClassNotFoundException", new Object[0]);
        } catch (SecurityException unused2) {
            LOGGER.error("Initialize hwDeliverInfoClass failed.", new Object[0]);
        } catch (NoSuchMethodException unused3) {
            LOGGER.error("Initialize hwDeliverInfoClass failed.", new Object[0]);
        }
        try {
            properties = new ReflectClass("android.os.SystemProperties");
        } catch (SecurityException unused4) {
            LOGGER.error("Initialize SystemProperties failed.", new Object[0]);
        }
    }

    private CustomConfigurationUtil() {
    }

    private static synchronized Object getSystemProperty(String str, Object obj, Class<?> cls) {
        Object obj2;
        synchronized (CustomConfigurationUtil.class) {
            Object obj3 = systemPropertiesMaps.get(str);
            if (obj3 != null && obj3.getClass() == cls) {
                return obj3;
            }
            if (cls == String.class) {
                obj2 = new ReflectMethod(properties, "get", String.class, String.class).invokeS(str, (String) obj);
            } else if (cls == Boolean.class) {
                obj2 = properties.invokeS("getBoolean", str, (Boolean) obj);
            } else if (cls == Integer.class) {
                obj2 = properties.invokeS("getInt", str, (Integer) obj);
            } else {
                LOGGER.error("getSystemProperty error key: %{public}s", str);
                return null;
            }
            systemPropertiesMaps.put(str, obj2);
            return obj2;
        }
    }

    public static synchronized boolean isDebugMemoryEnabled() {
        boolean z;
        synchronized (CustomConfigurationUtil.class) {
            z = false;
            Object invokeS = properties.invokeS("getBoolean", "persist.hwcamera.debug_memory", false);
            if (invokeS != null && ((Boolean) invokeS).booleanValue()) {
                z = true;
            }
        }
        return z;
    }

    public static boolean isHuaweiProduct() {
        Object systemProperty = getSystemProperty("ro.product.manufacturer", "HUAWEI", String.class);
        return systemProperty == null || "HUAWEI".equals(systemProperty);
    }

    public static boolean isDmSupported() {
        Object systemProperty = getSystemProperty("ro.config.isDmProduct", false, Boolean.class);
        if (systemProperty == null || !((Boolean) systemProperty).booleanValue()) {
            return false;
        }
        return true;
    }

    public static boolean isHighPixels() {
        Object systemProperty = getSystemProperty("ro.hwcamera.back.pixels", 800, Integer.class);
        if (systemProperty != null && ((Integer) systemProperty).intValue() > 800) {
            return true;
        }
        return false;
    }

    public static boolean debugMockEnable() {
        Object systemProperty = getSystemProperty("HW.CameraKit.Mock", false, Boolean.class);
        if (systemProperty == null || !((Boolean) systemProperty).booleanValue()) {
            return false;
        }
        return true;
    }

    public static boolean isAiMovieEnabled() {
        Object systemProperty = getSystemProperty("ro.hwcamera.aimovie_enable", 0, Integer.class);
        if (systemProperty == null || ((Integer) systemProperty).intValue() <= 0) {
            return false;
        }
        return true;
    }

    public static boolean isFrontVideoSnapshotSizeLimitToVideoSize() {
        Object systemProperty = getSystemProperty("ro.hwcamera.fvsnapsizelimit", true, Boolean.class);
        if (systemProperty == null || ((Boolean) systemProperty).booleanValue()) {
            return true;
        }
        return false;
    }

    public static boolean isBackVideoSnapshotSizeLimitToVideoSize() {
        Object systemProperty = getSystemProperty("ro.hwcamera.BackSnapShotLimit", false, Boolean.class);
        if (systemProperty == null || !((Boolean) systemProperty).booleanValue()) {
            return false;
        }
        return true;
    }

    public static int getDefaultBeautyLevel() {
        Object systemProperty = getSystemProperty("ro.hwcamera.def_beauty_level", 5, Integer.class);
        if (systemProperty != null) {
            return ((Integer) systemProperty).intValue();
        }
        return 5;
    }

    public static String getProductName() {
        Object systemProperty = getSystemProperty("ro.product.board", null, String.class);
        if (systemProperty == null) {
            return null;
        }
        return (String) systemProperty;
    }

    public static int getAndroidVersion() {
        Object systemProperty = getSystemProperty("ro.build.version.sdk", 0, Integer.class);
        if (systemProperty == null) {
            return 0;
        }
        return ((Integer) systemProperty).intValue();
    }

    public static boolean isBeautyVideoSupported() {
        Object systemProperty = getSystemProperty("ro.feature.hwcamera.beautyvideo", true, Boolean.class);
        if (systemProperty == null || ((Boolean) systemProperty).booleanValue()) {
            return true;
        }
        return false;
    }

    public static boolean isDebugVersion() {
        int i;
        Object systemProperty = getSystemProperty("ro.logsystem.usertype", 1, Integer.class);
        if (systemProperty == null) {
            i = 1;
        } else {
            i = ((Integer) systemProperty).intValue();
        }
        if ("1".equals(getSystemProperty("ro.debuggable", LightEffect.LIGHT_ID_LED, String.class)) || i == 3 || i == 5) {
            return true;
        }
        return false;
    }

    public static boolean isFoldDispProduct() {
        String str = (String) getSystemProperty("ro.config.hw_fold_disp", null, String.class);
        return str != null && !str.trim().isEmpty();
    }

    public static boolean isSupportedRawSaved() {
        Object systemProperty = getSystemProperty("ro.hwcamera.rawphoto.save", true, Boolean.class);
        if (systemProperty == null || ((Boolean) systemProperty).booleanValue()) {
            return true;
        }
        return false;
    }

    public static boolean isLandScapeProduct() {
        Object systemProperty = getSystemProperty("ro.panel.hw_orientation", 0, Integer.class);
        if (systemProperty != null && ((Integer) systemProperty).intValue() == 90) {
            return true;
        }
        return false;
    }

    public static int getCurvedSidePadding(Context context) {
        NumberFormatException e;
        int i;
        if (!(context instanceof android.content.Context)) {
            LOGGER.debug("getCurvedSidePadding context instance error, return curved side padding: 0", new Object[0]);
            return 0;
        }
        android.content.Context context2 = (android.content.Context) context.getHostContext();
        Object systemProperty = getSystemProperty(CURVED_SIDE, "", String.class);
        if (!(systemProperty instanceof String)) {
            return 0;
        }
        String str = (String) systemProperty;
        if (isEmptyString(str)) {
            return 0;
        }
        String[] split = str.split(SPLIT_SIGNAL);
        if (split.length > 4) {
            return 0;
        }
        try {
            i = Integer.parseInt(split[0]) + Integer.parseInt(split[2]);
            try {
                Point point = new Point();
                Object systemService = context2.getSystemService("window");
                if (!(systemService instanceof WindowManager)) {
                    return i;
                }
                ((WindowManager) systemService).getDefaultDisplay().getRealSize(point);
                int i2 = point.y;
                if (point.x > point.y) {
                    i2 = point.x;
                }
                int parseInt = Integer.parseInt(split[3]);
                if (i2 == parseInt || parseInt == 0) {
                    return i;
                }
                return (i2 * i) / parseInt;
            } catch (NumberFormatException e2) {
                e = e2;
                LOGGER.error("int parse exception %{public}s", e.getLocalizedMessage());
                return i;
            }
        } catch (NumberFormatException e3) {
            e = e3;
            i = 0;
            LOGGER.error("int parse exception %{public}s", e.getLocalizedMessage());
            return i;
        }
    }

    private static boolean isEmptyString(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String getSupportedPicSizeInWideApertureMode() {
        Object systemProperty = getSystemProperty("ro.hwcamera.aperture.picsize", "", String.class);
        if (systemProperty == null) {
            return "";
        }
        return (String) systemProperty;
    }

    public static boolean needReduceResolution() {
        Object systemProperty = getSystemProperty("ro.hwcamera.reduce_resolution", true, Boolean.class);
        if (systemProperty == null || ((Boolean) systemProperty).booleanValue()) {
            return true;
        }
        return false;
    }
}
