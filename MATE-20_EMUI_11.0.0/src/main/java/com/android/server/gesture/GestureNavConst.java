package com.android.server.gesture;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.accessibility.AccessibilityManager;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.util.LogEx;
import com.huawei.utils.HwPartResourceUtils;
import java.util.HashMap;
import java.util.Map;

public class GestureNavConst extends DefaultGestureNavConst {
    public static final int ADD_HW_SPLIT_SCREEN = 0;
    public static final float BACK_WINDOW_HEIGHT_RATIO = 0.75f;
    public static final float BACK_WINDOW_HEIGHT_RATIO_FULL = 1.0f;
    public static final float BOTTOM_WINDOW_QUICK_STEP_RATIO = 0.75f;
    public static final float BOTTOM_WINDOW_QUICK_STEP_RATIO_LAND = 0.8333333f;
    public static final float BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    public static final int CHECK_ABNORMAL_POINTER_COUNT = 2;
    public static final int CHECK_AFT_TIMEOUT = 2500;
    public static final boolean CHINA_REGION = "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", ""));
    public static final int COMPENSATE_MOVE_MAX_TIME = 500;
    public static final int COMPENSATE_MOVE_MIN_TIME = 80;
    public static final boolean DEBUG = ("1".equals(SystemPropertiesEx.get("ro.debuggable", "0")) || SystemPropertiesEx.getInt("ro.logsystem.usertype", 1) == 3 || SystemPropertiesEx.getInt("ro.logsystem.usertype", 1) == 5);
    public static final boolean DEBUG_ALL = (DEBUG && LogEx.getHwLog());
    public static final boolean DEBUG_DUMP = DEBUG;
    public static final String DEFAULT_DOCK_AIDL_INTERFACE = "com.huawei.hwdockbar.IDockAidlInterface";
    public static final String DEFAULT_DOCK_MAIN_CLASS = "com.huawei.hwdockbar.DockMainService";
    public static final String DEFAULT_DOCK_PACKAGE = "com.huawei.hwdockbar";
    public static final String DEFAULT_LAUNCHER_DRAWER_WINDOW = "com.huawei.android.launcher/com.huawei.android.launcher.drawer.DrawerLauncher";
    public static final String DEFAULT_LAUNCHER_MAIN_WINDOW = "com.huawei.android.launcher/com.huawei.android.launcher.unihome.UniHomeLauncher";
    public static final String DEFAULT_LAUNCHER_PACKAGE = "com.huawei.android.launcher";
    public static final String DEFAULT_LAUNCHER_SIMPLE_WINDOW = "com.huawei.android.launcher/com.huawei.android.launcher.newsimpleui.NewSimpleLauncher";
    public static final String DEFAULT_QUICKSTEP_CLASS = "com.android.quickstep.RecentsActivity";
    public static final int DEFAULT_ROTATION = (SystemPropertiesEx.getInt("ro.panel.hw_orientation", 0) / 90);
    public static final int DEFAULT_VALUE_DISPLAY_NOTCH_STATUS = 0;
    public static final int DEFAULT_VALUE_GESTURE_NAVIGATION = 0;
    public static final int DEFAULT_VALUE_GESTURE_NAVIGATION_ASSISTANT;
    public static final int DEFAULT_VALUE_GESTURE_NAVIGATION_TIPS = 1;
    public static final int DEFAULT_VALUE_HORIZONTAL_SWITCH = 0;
    public static final int DEFAULT_VALUE_MULTI_WIN = 1;
    public static final String DEVICE_TYPE_DEFAULT = "default";
    public static final String DEVICE_TYPE_FOLD_PHONE = "fold";
    public static final String DEVICE_TYPE_TABLET = "tablet";
    public static final float DISTANCE_PERCENT_TO_SHOW_DOCK = 0.8f;
    public static final Map<String, String> DOCK_PARAM_MAP = new HashMap();
    public static final float FAST_VELOCITY_THRESHOLD_1 = 4500.0f;
    public static final float FAST_VELOCITY_THRESHOLD_2 = 6000.0f;
    public static final String FOLD_PHONE_DOCK_PARAMS = "80,200,0.05f,0.3f,25";
    public static final int FOLD_STATE_CHANGED = 2;
    public static final float GESTURE_BACK_ANIM_MAX_RATIO = 0.88f;
    public static final float GESTURE_BACK_ANIM_MIN_RATIO = 0.1f;
    public static final int GESTURE_GO_HOME_MIN_DISTANCE_THRESHOLD = 180;
    public static final int GESTURE_GO_HOME_TIMEOUT = 500;
    public static final int GESTURE_MOVE_MAX_ANGLE = 70;
    public static final int GESTURE_MOVE_MIN_DISTANCE_THRESHOLD = 15;
    public static final int GESTURE_MOVE_TIME_THRESHOLD_0 = 100;
    public static final int GESTURE_MOVE_TIME_THRESHOLD_1 = 120;
    public static final int GESTURE_MOVE_TIME_THRESHOLD_2 = 150;
    public static final int GESTURE_MOVE_TIME_THRESHOLD_3 = 200;
    public static final int GESTURE_MOVE_TIME_THRESHOLD_4 = 250;
    public static final int GESTURE_NAV_MODE_DEFAULT = 0;
    public static final int GESTURE_NAV_MODE_FORCE_DISABLE = 2;
    public static final int GESTURE_NAV_MODE_FORCE_ENABLE = 1;
    public static final float GESTURE_SLIDE_OUT_DISTANCE_THRESHOLD_RATIO = 0.4f;
    public static final int GESTURE_SLIDE_OUT_MAX_ANGLE = 45;
    public static final float HOME_SWIPE_THRESHOLD = 8.0f;
    public static final int ID_GESTURE_NAV_BOTTOM = 3;
    public static final int ID_GESTURE_NAV_INVALID = 0;
    public static final int ID_GESTURE_NAV_LEFT = 1;
    public static final int ID_GESTURE_NAV_RIGHT = 2;
    public static final int ID_SUB_SCREEN_GESTURE_NAV_BOTTOM = 13;
    public static final int ID_SUB_SCREEN_GESTURE_NAV_LEFT = 11;
    public static final int ID_SUB_SCREEN_GESTURE_NAV_RIGHT = 12;
    public static final boolean IS_SUPPORT_FULL_BACK = (!SystemPropertiesEx.getBoolean("hw_mc.launcher.disable_back_hotzone_fullscreen", false));
    public static final boolean IS_SUPPORT_GAME_ASSIST = (SystemPropertiesEx.getInt("ro.config.gameassist", 0) == 1);
    public static final boolean IS_TABLET = DEVICE_TYPE_TABLET.equals(SystemPropertiesEx.get("ro.build.characteristics", DEVICE_TYPE_DEFAULT));
    public static final String KEY_DISPLAY_NOTCH_STATUS = "display_notch_status";
    public static final String KEY_SECURE_GESTURE_HORIZONTAL_SWITCH = "secure_gesture_navigation_horizontalswitch";
    public static final String KEY_SECURE_GESTURE_NAVIGATION = "secure_gesture_navigation";
    public static final String KEY_SECURE_GESTURE_NAVIGATION_ASSISTANT = "secure_gesture_navigation_assistant";
    public static final String KEY_SECURE_GESTURE_NAVIGATION_TIPS = "secure_gestures_navigation_tips";
    public static final String KEY_SECURE_MULTI_WIN = "multi_win_interact";
    public static final String KEY_SUPER_SAVE_MODE = "sys.super_power_save";
    public static final String LAUNCHER_PACKAGE_NAME = "com.huawei.android.launcher";
    public static final int LONG_PRESS_RESTART_TIMEOUT = 150;
    public static final int LONG_PRESS_TIMEOUT = 200;
    public static final int MODE_OFF = 0;
    public static final int MODE_ON = 1;
    public static final int MOVE_INTERVAL_DISTANCE = 150;
    public static final String OOBE_MAIN_PACKAGE = "com.huawei.hwstartupguide";
    public static final String RECENT_WINDOW = "com.android.systemui/com.android.systemui.recents.RecentsActivity";
    public static final int REMOVE_HW_SPLIT_SCREEN = 1;
    public static final String REPORT_FAILURE = "failure";
    public static final String REPORT_SUCCESS = "success";
    public static final int ROTATION_MODE_CHANGED = 3;
    public static final String SETUP_WIZARD_PACKAGE = "com.google.android.setupwizard";
    public static final String SIMPLE_MODE_DB_KEY = "new_simple_mode";
    public static final String STARTUP_GUIDE_HOME_COMPONENT = "com.huawei.hwstartupguide/com.huawei.hwstartupguide.LanguageSelectActivity";
    public static final String STATUSBAR_WINDOW = "StatusBar";
    public static final boolean SUPPORT_DOCK_TRIGGER = SystemPropertiesEx.getBoolean("ro.config.hw_multiwindow_optimization", false);
    public static final String TABLET_DOCK_PARAMS = "80,200,0.05f,0.5f,25";
    public static final String TAG_GESTURE_BACK = "GestureNav_Back";
    public static final String TAG_GESTURE_BOTTOM = "GestureNav_Bottom";
    public static final String TAG_GESTURE_OPS = "GestureNav_OPS";
    public static final String TAG_GESTURE_QS = "GestureNav_QS";
    public static final String TAG_GESTURE_QSH = "GestureNav_QSH";
    public static final String TAG_GESTURE_QSO = "GestureNav_QSO";
    public static final String TAG_GESTURE_STRATEGY = "GestureNav_Strategy";
    public static final String TAG_GESTURE_TRACKER = "GestureNav_Tracker";
    public static final String TAG_GESTURE_UTILS = "GestureNav_Utils";
    public static final String UNFOLD_PHONE_DOCK_PARAMS = "80,200,0.05f,0.3f,25";
    public static final int VIBRATION_AMPLITUDE = 255;
    public static final long VIBRATION_TIME = 35;
    private static boolean isBopd = SystemPropertiesEx.getBoolean("sys.bopd", false);

    static {
        float f;
        int i = 1;
        if (SystemPropertiesEx.getInt("ro.config.curved_screen", 0) == 1) {
            f = 1.8f;
        } else {
            f = 1.4f;
        }
        BOTTOM_WINDOW_SINGLE_HAND_RATIO = f;
        if (CHINA_REGION) {
            i = 0;
        }
        DEFAULT_VALUE_GESTURE_NAVIGATION_ASSISTANT = i;
        DOCK_PARAM_MAP.put(DEVICE_TYPE_DEFAULT, "80,200,0.05f,0.3f,25");
        DOCK_PARAM_MAP.put(DEVICE_TYPE_FOLD_PHONE, "80,200,0.05f,0.3f,25");
        DOCK_PARAM_MAP.put(DEVICE_TYPE_TABLET, TABLET_DOCK_PARAMS);
    }

    public static String reportResultStr(boolean isSuccess, int side, int failedReason) {
        if (isSuccess) {
            return "result:success,side:" + side;
        }
        return "result:failure,side:" + side + ",reason:" + failedReason;
    }

    public static String reportResultStr(boolean isSuccess, int failedReason) {
        if (isSuccess) {
            return "result:success";
        }
        return "result:failure,reason:" + failedReason;
    }

    public boolean getGestureNavEnabled(Context context, int userHandle) {
        return isGestureNavEnabled(context, userHandle);
    }

    public static boolean isGestureNavEnabled(Context context, int userHandle) {
        if (context == null) {
            return false;
        }
        if (isBopd) {
            Settings.Secure.putInt(context.getContentResolver(), KEY_SECURE_GESTURE_NAVIGATION, 0);
            return false;
        } else if (SettingsEx.Secure.getIntForUser(context.getContentResolver(), KEY_SECURE_GESTURE_NAVIGATION, 0, userHandle) == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isShowDockEnabled(Context context, int userHandle) {
        return SUPPORT_DOCK_TRIGGER && isHwMultiWindowEnabled(context, userHandle);
    }

    public static boolean isHorizontalSwitchEnabled(Context context, int userHandle) {
        if (context != null && SettingsEx.Secure.getIntForUser(context.getContentResolver(), KEY_SECURE_GESTURE_HORIZONTAL_SWITCH, 0, userHandle) == 1) {
            return true;
        }
        return false;
    }

    public static boolean isHwMultiWindowEnabled(Context context, int userHandle) {
        if (context != null && SettingsEx.Secure.getIntForUser(context.getContentResolver(), KEY_SECURE_MULTI_WIN, 1, userHandle) == 1) {
            return true;
        }
        return false;
    }

    public static boolean isInSuperSaveMode() {
        return SystemPropertiesEx.getBoolean(KEY_SUPER_SAVE_MODE, false);
    }

    public static boolean isInScreenReaderMode(Context context, int userHandle) {
        AccessibilityManager accessibilityManager;
        if (context != null && (accessibilityManager = (AccessibilityManager) context.getSystemService("accessibility")) != null && accessibilityManager.isEnabled() && accessibilityManager.isTouchExplorationEnabled()) {
            return true;
        }
        return false;
    }

    public static boolean isKeyguardLocked(Context context) {
        KeyguardManager keyguardManager;
        if (context == null || (keyguardManager = (KeyguardManager) context.getSystemService(KeyguardManager.class)) == null || !keyguardManager.isKeyguardLocked()) {
            return false;
        }
        return true;
    }

    public static boolean isLauncher(String pkgName) {
        return "com.huawei.android.launcher".equals(pkgName);
    }

    public static boolean isGestureNavTipsEnabled(Context context, int userHandle) {
        if (context != null && SettingsEx.Secure.getIntForUser(context.getContentResolver(), KEY_SECURE_GESTURE_NAVIGATION_TIPS, 1, userHandle) == 1) {
            return true;
        }
        return false;
    }

    public static boolean isAssistantGestureEnabled(Context context, int userHandle) {
        if (context != null && !CHINA_REGION && SettingsEx.Secure.getIntForUser(context.getContentResolver(), KEY_SECURE_GESTURE_NAVIGATION_ASSISTANT, DEFAULT_VALUE_GESTURE_NAVIGATION_ASSISTANT, userHandle) == 1) {
            return true;
        }
        return false;
    }

    public static boolean isSimpleMode(Context context, int userHandle) {
        if (context == null) {
            return false;
        }
        int newSimpleModeValue = SettingsEx.System.getIntForUser(context.getContentResolver(), SIMPLE_MODE_DB_KEY, 0, userHandle);
        if (DEBUG) {
            Log.i("GestureNavConst", " isNewSimpleModeOn newSimpleModeValue = " + newSimpleModeValue);
        }
        if (newSimpleModeValue == 1) {
            return true;
        }
        return false;
    }

    public static int getBackWindowHeight(int displayHeight, boolean isShouldShrink, int excludedRegionHeight, boolean isForceFull) {
        if (IS_SUPPORT_FULL_BACK) {
            if (isShouldShrink) {
                return Math.round(((float) displayHeight) * 0.75f);
            }
            return displayHeight - excludedRegionHeight;
        } else if (isForceFull) {
            return displayHeight;
        } else {
            return Math.round(((float) displayHeight) * 0.75f);
        }
    }

    public static int getBackWindowWidth(Context context) {
        if (context == null) {
            return 0;
        }
        return context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("gesture_nav_back_window_width"));
    }

    public static int getBottomWindowHeight(Context context) {
        if (context == null) {
            return 0;
        }
        return context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("gesture_nav_bottom_window_height"));
    }

    public static int getBottomSideWidth(Context context) {
        if (context == null) {
            return 0;
        }
        return context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("gesture_nav_bottom_side_width"));
    }

    public static int getBottomQuickOutHeight(Context context) {
        if (context == null) {
            return 0;
        }
        return context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("gesture_nav_bottom_quick_out_height"));
    }

    public static int getGestureCurvedOffset(Context context) {
        if (context == null) {
            return 0;
        }
        return context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("gesture_nav_curved_offset"));
    }

    public static int getBackMaxDistanceOne(Context context) {
        if (context == null) {
            return 0;
        }
        return context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("gesture_nav_back_max_distance_1"));
    }

    public static int getBackMaxDistanceTwo(Context context) {
        if (context == null) {
            return 0;
        }
        return context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("gesture_nav_back_max_distance_2"));
    }

    public static int getStatusBarHeight(Context context) {
        if (context == null) {
            return 0;
        }
        return context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("status_bar_height"));
    }

    public static int convertDpToPixel(float dp) {
        if (Resources.getSystem() == null || Resources.getSystem().getDisplayMetrics() == null) {
            return 0;
        }
        return (int) (Resources.getSystem().getDisplayMetrics().density * dp);
    }

    public static int dipToPx(Context context, int dip) {
        if (context == null) {
            return 0;
        }
        return Math.round(TypedValue.applyDimension(1, (float) dip, context.getResources().getDisplayMetrics()));
    }

    public static int mmToPx(Context context, float value) {
        if (context == null) {
            return 0;
        }
        return Math.round(TypedValue.applyDimension(5, value, context.getResources().getDisplayMetrics()));
    }

    public static int getDimensionPixelSize(Context context, int resId) {
        if (context == null) {
            return 0;
        }
        return context.getResources().getDimensionPixelSize(resId);
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        return value > max ? max : value;
    }
}
