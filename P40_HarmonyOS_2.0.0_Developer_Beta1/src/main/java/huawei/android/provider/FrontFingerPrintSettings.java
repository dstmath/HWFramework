package huawei.android.provider;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.os.SystemProperties;
import android.provider.Settings;
import huawei.android.os.HwGeneralManager;
import huawei.android.provider.HwSettings;

public class FrontFingerPrintSettings {
    public static final String CN_OPTB = "156";
    public static final int DEFAULT_FINGERPRINT_DEVICEID = -2;
    public static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    public static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    public static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY_MODE = SystemProperties.getInt("ro.config.front_fp_trikey_mode", 0);
    public static final int SINGLE_VIRTUAL_AI_MODE = SystemProperties.getInt("ro.config.show_navbar_slide", CN_OPTB.equals(SystemProperties.get("ro.config.hw_optb", HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF)) ? 1 : 0);
    public static final int SINGLE_VIRTUAL_NAVIGATION_MODE = SystemProperties.getInt("ro.config.single_virtual_navbar", 0);

    public static boolean isNaviBarEnabled(ContentResolver resolver) {
        int i;
        int navibarDefaultState = 1;
        if (!FRONT_FINGERPRINT_NAVIGATION) {
            return true;
        }
        int i2 = FRONT_FINGERPRINT_NAVIGATION_TRIKEY;
        if (i2 == 0) {
            if (isChinaArea()) {
                if (SINGLE_VIRTUAL_NAVIGATION_MODE == 1) {
                    i = 1;
                } else {
                    i = 0;
                }
                navibarDefaultState = i;
            } else {
                navibarDefaultState = 1;
            }
        } else if (i2 == 1) {
            return false;
        }
        if (Settings.System.getIntForUser(resolver, HwSettings.System.NAVIGATION_BAR_ENABLE, navibarDefaultState, ActivityManager.getCurrentUser()) == 1) {
            return true;
        }
        return false;
    }

    public static boolean isSingleVirtualNavbarEnable(ContentResolver resolver) {
        int defaultValue;
        if (!isNaviBarEnabled(resolver)) {
            return false;
        }
        if (SINGLE_VIRTUAL_NAVIGATION_MODE == 1) {
            defaultValue = 1;
        } else {
            defaultValue = 0;
        }
        if (Settings.System.getIntForUser(resolver, HwSettings.System.SINGLE_VIRTUAL_NAVBAR, defaultValue, ActivityManager.getCurrentUser()) == 1) {
            return true;
        }
        return false;
    }

    public static boolean isSingleNavBarAIEnable(ContentResolver resolver) {
        int defaultValue;
        if (!isSingleVirtualNavbarEnable(resolver)) {
            return false;
        }
        if (SINGLE_VIRTUAL_AI_MODE == 1) {
            defaultValue = 1;
        } else {
            defaultValue = 0;
        }
        if (Settings.System.getIntForUser(resolver, HwSettings.System.AI_ENTRANCE, defaultValue, ActivityManager.getCurrentUser()) == 1) {
            return true;
        }
        return false;
    }

    public static int getDefaultNaviMode() {
        int i;
        if (!isSupportTrikey() || (i = FRONT_FINGERPRINT_NAVIGATION_TRIKEY) == 0 || i != 1) {
            return -1;
        }
        int i2 = FRONT_FINGERPRINT_NAVIGATION_TRIKEY_MODE;
        if (i2 == 1) {
            return 0;
        }
        if (i2 == 2) {
            return -1;
        }
        if (i2 == 3) {
            if (isChinaArea()) {
                return 0;
            }
            return -1;
        } else if (isChinaArea()) {
            return -1;
        } else {
            return 0;
        }
    }

    public static int getDefaultBtnLightMode() {
        return SystemProperties.getInt("ro.config.front_fp_btnlight", 1);
    }

    public static boolean isSupportTrikey() {
        return HwGeneralManager.getInstance().isSupportTrikey();
    }

    public static boolean isChinaArea() {
        return CN_OPTB.equals(SystemProperties.get("ro.config.hw_optb", HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF));
    }

    public static boolean isGestureNavigationMode(ContentResolver resolver) {
        return Settings.Secure.getIntForUser(resolver, HwSettings.Secure.KEY_SECURE_GESTURE_NAVIGATION, -1, ActivityManager.getCurrentUser()) == 1;
    }
}
