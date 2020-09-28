package android.content.pm;

public abstract class AbsApplicationInfo {
    public static final int BLACK_LIST_APK = 268435456;
    public static final int FLAG_MARKETED_SYSTEM_APP = 536870912;
    public static final int FLAG_UPDATED_REMOVEABLE_APP = 67108864;
    public static final String FORCE_DARK_MODE = "forceDarkMode";
    public static final int HW_EXTFLAG_HASPLUGIN = 1;
    public static final int HW_EXTFLAG_MIN_ASPECT_CAN_CHANGE = 1024;
    public static final int HW_SPLIT_CONFIG = 536870912;
    public static final int HW_SPLIT_FEATURE = 1073741824;
    public static final int HW_SPLIT_PLUGIN = Integer.MIN_VALUE;
    public static final String MAX_ASPECT_RATIO = "maxAspectRatio";
    public static final String MIN_ASPECT_RATIO = "minAspectRatio";
    public static final int PARSE_CUST_APK = 134217728;
    public static final int PARSE_IS_BOTH_APK = 8388608;
    public static final int PARSE_IS_DEPEND_WEBVIEW_APK = 2097152;
    public static final int PARSE_IS_MAPLE_APK = 16777216;
    public static final int PARSE_IS_MAPLE_GC_ONLY = 524288;
    public static final int PARSE_IS_MAPLE_ONLY_APK = 4194304;
    public static final int PARSE_IS_REMOVABLE_PREINSTALLED_APK = 33554432;
    public static final int PARSE_IS_ZIDANE_APK = 1048576;
    public static final int SCAN_INSTALL_APK = 1073741824;

    public int getHwFlags() {
        return 0;
    }
}
