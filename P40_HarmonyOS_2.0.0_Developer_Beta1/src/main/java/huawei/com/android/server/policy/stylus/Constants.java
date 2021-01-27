package huawei.com.android.server.policy.stylus;

import com.huawei.android.os.SystemPropertiesEx;

public class Constants {
    public static final int ACTIVITY_TYPE_HOME = 2;
    public static final int ACTIVITY_TYPE_RECENTS = 3;
    public static final String FLOAT_ENTRANCE_CLASSNAME = "com.huawei.stylus.floatmenu.FloatMenuService";
    public static final String FLOAT_ENTRANCE_PACKAGE_NAME = "com.huawei.stylus.floatmenu";
    public static final boolean IS_TABLET = "tablet".equals(SystemPropertiesEx.get("ro.build.characteristics", "default"));
    public static final int KEYCODE_F20 = 718;
    public static final int LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS = 1;
    public static final String PKG_HUAWEI_SMARTSHOT = "com.huawei.smartshot";
    public static final String PKG_QEEXO_SMARTSHOT = "com.qeexo.smartshot";
    public static final int TAKE_SCREENSHOT_FULLSCREEN = 1;
    public static final int TYPE_SECURE_SYSTEM_OVERLAY = 2015;
    public static final int TYPE_STYLUS_CLICK = 160;
    public static final int TYPE_STYLUS_DOUBLE_CLICK = 161;
    public static final int TYPE_STYLUS_LETTER_GESTURE = 163;
    public static final int TYPE_STYLUS_OPERATION_SUCCESSD = 952;
    public static final int TYPE_STYLUS_REGION_GESTURE = 162;
    public static final int TYPE_STYLUS_SWTICH_METHOD = 164;
    public static final int TYPE_STYLUS_USAGED_COUNT = 951;
    public static final int TYPE_STYLUS_USAGED_PKG = 950;
    public static final int WINDOWING_MODE_PINNED = 2;

    private Constants() {
    }
}
