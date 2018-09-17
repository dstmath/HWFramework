package android.util;

import android.app.ActivityOptions;
import android.app.ContextImpl;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.ContextWrapper;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.pc.HwPCManager;
import android.pc.IHwPCManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.inputmethod.InputMethodInfo;
import java.util.ArrayList;

public final class HwPCUtils {
    public static final String ACTION_CHANGE_PC_WALLPAPER = "com.huawei.pcfilemanager.change_wallpaper";
    public static final String APP_BRIGHTNESS_NAME = "com.android.systemui/.settings.BrightnessDialog";
    public static final String APP_GLOBALACTIONS_NAME = "HwGlobalActions";
    public static final String APP_TOUCHPAD_COMPONENT_NAME = "com.huawei.desktop.systemui/com.huawei.systemui.mk.activity.ImitateActivity";
    public static final String APP_VOLUME_NAME = "VolumeDialog";
    public static final String CLOUD_ACTIVITY_NAME = "com.huawei.ahdp.session.VmActivity";
    public static final String CLOUD_PKG_NAME = "com.huawei.cloud";
    private static final boolean ENABLED = SystemProperties.getBoolean("ro.config.hw_emui_desktop_mode", false);
    private static final boolean ENABLED_IN_PAD = SystemProperties.getBoolean("ro.config.hw_emui_pad_pc_mode", false);
    public static final int FIRST_PC_DYNAMIC_STACK_ID = 10007;
    public static final int FORCED_PC_DISPLAY_DENSITY_160 = 1001;
    public static final int FORCED_PC_DISPLAY_DENSITY_240 = 1002;
    public static final int FORCED_PC_DISPLAY_DENSITY_DEFAULT_240 = 1004;
    public static final int FORCED_PC_DISPLAY_DENSITY_GET = 1000;
    public static final int FORCED_PC_DISPLAY_DENSITY_OTHER = 1003;
    public static final int FORCED_PC_DISPLAY_SIZE_GET_OVERSCAN_MODE = 1005;
    public static final int FORCED_PC_DISPLAY_SIZE_OVERSCAN_MODE = 1006;
    public static final int FORCED_PC_DISPLAY_SIZE_UNOVERSCAN1_MODE = 1008;
    public static final int FORCED_PC_DISPLAY_SIZE_UNOVERSCAN_MODE = 1007;
    private static final boolean IS_FACTORY_MODE = SystemProperties.get("ro.runmode", "normal").equals("factory");
    private static final boolean IS_MMI_RUNNING = SystemProperties.get("runtime.mmitest.isrunning", "false").equals("true");
    private static final boolean LOG_ENABLED = SystemProperties.getBoolean("hw.pc.log.enabled", true);
    public static final String PC_CAST_MODE_IN_SERVER = "hw.pc.cast.mode";
    private static int PC_DISPLAYID = -1;
    private static boolean PC_MODE = false;
    private static volatile boolean PC_MODE_EARLY = false;
    private static int PHONE_DISPLAYID = -1;
    public static final String PHONE_PROCESS = "com.android.phone";
    public static final String PKG_ARTICLE_NEWS = "com.ss.android.article.news";
    public static final String PKG_BLUETOOTH = "com.android.bluetooth";
    public static final String PKG_CHINA_CCB = "com.chinamworld.main";
    public static final String PKG_DESKTOP_EXPLORER = "com.huawei.desktop.explorer";
    public static final String PKG_DESKTOP_SYSTEMUI = "com.huawei.desktop.systemui";
    public static final String PKG_HIMOVIE = "com.huawei.himovie";
    public static final String PKG_HWPAY = "com.huawei.android.hwpay";
    public static final String PKG_INSTANTSHARE = "com.huawei.android.instantshare";
    public static final String PKG_PHONE_CONTACTS = "com.android.contacts";
    public static final String PKG_PHONE_CONTACTS_2 = "com.android.incallui";
    public static final String PKG_PHONE_PROJECTMENU = "com.huawei.android.projectmenu";
    public static final String PKG_PHONE_SYSTEMUI = "com.android.systemui";
    public static final String PKG_PHONE_WFDFT = "com.huawei.android.wfdft";
    public static final String PKG_SCREEN_RECORDER = "com.huawei.screenrecorder";
    public static final String PKG_SETTINGS = "com.android.settings";
    public static final String PKG_TALK_BACK = "com.google.android.marvin.talkback";
    public static final String PROP_PC_DISPLAY_HEIGHT = "hw.pc.display.height";
    public static final String PROP_PC_DISPLAY_MODE = "hw.pc.display.mode";
    public static final String PROP_PC_DISPLAY_WIDTH = "hw.pc.display.width";
    public static final String REASON_RELAUNCH_IME = "relaunchIME";
    public static final String REASON_RELAUNCH_IN_DIFF_DISPLAY = "relaunch due to in diff display";
    public static final int REPORT_CLICK_HOME = 10000;
    public static final int REPORT_CLICK_KEYBOARD = 10006;
    public static final int REPORT_CLICK_MUTETILE = 10007;
    public static final int REPORT_CLICK_NOTIFY_SWITCH = 10003;
    public static final int REPORT_CLICK_SEARCH = 10012;
    public static final int REPORT_CONNECT_DISPLAY_BY_WIRED = 10008;
    public static final int REPORT_CONNECT_DISPLAY_RATIO = 10011;
    public static final int REPORT_CONNECT_DISPLAY_SIZE = 10010;
    public static final int REPORT_CONNECT_DISPLAY_TYPE = 10009;
    public static final int REPORT_CONNECT_KEYBOARD = 10005;
    public static final int REPORT_CONNECT_MOUSE = 10004;
    public static final int REPORT_CONNECT_PAD_KEYBORAD = 10027;
    public static final int REPORT_CURRENT_TASK_SIZE = 10017;
    public static final int REPORT_ENTER_HELP_PAGE = 10019;
    public static final int REPORT_ENTER_HELP_PAGE_CONNECT = 10020;
    public static final int REPORT_ENTER_HELP_PAGE_GUIDE = 10021;
    public static final int REPORT_ENTER_MY_COMPUTER = 10022;
    public static final int REPORT_PAD_DESKTOP_MODE = 10026;
    public static final int REPORT_PINED_APP = 10013;
    public static final int REPORT_SKIP_GUIDE = 10018;
    public static final int REPORT_START_APP_AT_PC = 10015;
    public static final int REPORT_START_APP_FROM_DOCK = 10014;
    public static final int REPORT_STOP_APP_AT_PC = 10016;
    public static final int REPORT_TIME_DIFF_SRC = 10002;
    public static final int REPORT_TIME_SAME_SRC = 10001;
    public static final int REPORT_TOUCHPAD_ON_CREATE = 10024;
    public static final int REPORT_TOUCHPAD_ON_STOP = 10025;
    public static final int REPORT_TOUCHPAD_SHOW_INPUT = 10023;
    public static final float SCALE_PC_SCREEN_MINOR = 0.95f;
    public static final float SCALE_PC_SCREEN_NORMAL = 1.0f;
    public static final float SCALE_PC_SCREEN_SMALLER = 0.9f;
    private static final String TAG = "HwPCUtils#";
    public static final int TYPE_LAUNCHER_LIKE = 2103;
    public static final int TYPE_PC_SCREEN_MINOR = 1;
    public static final int TYPE_PC_SCREEN_NORMAL = 0;
    public static final int TYPE_PC_SCREEN_SMALLER = 2;
    private static boolean mSupportOverlay = SystemProperties.getBoolean("hw_pc_support_overlay", false);
    private static ArrayList<InputMethodInfo> sMethodList = new ArrayList();

    public enum ProjectionMode {
        DESKTOP_MODE,
        PHONE_MODE
    }

    public static final boolean enabled() {
        if (IS_FACTORY_MODE || IS_MMI_RUNNING) {
            return false;
        }
        return ENABLED;
    }

    public static final boolean enabledInPad() {
        return enabled() ? ENABLED_IN_PAD : false;
    }

    public static final boolean isPcCastModeInServer() {
        return enabled() ? PC_MODE : false;
    }

    public static void setPcCastModeInServer(boolean enabled) {
        PC_MODE = enabled;
        SystemProperties.set(PC_CAST_MODE_IN_SERVER, LogException.NO_VALUE + enabled);
    }

    public static final boolean isPcCastModeInServerEarly() {
        return enabled() ? PC_MODE_EARLY : false;
    }

    public static final void setPcCastModeInServerEarly(ProjectionMode mode) {
        PC_MODE_EARLY = ProjectionMode.DESKTOP_MODE == mode;
    }

    public static void setPCDisplayID(int displayId) {
        PC_DISPLAYID = displayId;
    }

    public static int getPCDisplayID() {
        return PC_DISPLAYID;
    }

    public static void setPhoneDisplayID(int displayId) {
        PHONE_DISPLAYID = displayId;
    }

    public static int getPhoneDisplayID() {
        return PHONE_DISPLAYID;
    }

    public static final boolean isValidExtDisplayId(int displayId) {
        return enabled() && displayId == PC_DISPLAYID && displayId != -1 && displayId != 0;
    }

    public static final boolean isValidExtDisplayId(Context context) {
        if (!enabled() || context == null) {
            return false;
        }
        int count = 0;
        if (!(!(context instanceof ContextWrapper) ? context instanceof ContextImpl : true)) {
            context = context.getApplicationContext();
            if (context == null) {
                return false;
            }
        }
        while (context instanceof ContextWrapper) {
            Context nextContext = ((ContextWrapper) context).getBaseContext();
            if (nextContext != null && context != nextContext) {
                context = nextContext;
                count++;
                if (count > 12) {
                    break;
                }
            }
            break;
        }
        if (context instanceof ContextImpl) {
            return isValidExtDisplayId(((ContextImpl) context).peekHwPCDisplayId());
        }
        return false;
    }

    public static boolean isPcDynamicStack(int stackId) {
        return enabled() && stackId >= 10007;
    }

    public static boolean isPcCastMode() {
        boolean z = false;
        try {
            HwPCManager hwPcManager = HwFrameworkFactory.getHwPCManager();
            if (hwPcManager != null) {
                IHwPCManager mService = hwPcManager.getService();
                if (mService != null) {
                    z = mService.getCastMode();
                }
                return z;
            }
        } catch (RemoteException e) {
            Log.e("HwPCUtils", "call isPcCastMode error");
        }
        return false;
    }

    public static IHwPCManager getHwPCManager() {
        try {
            HwPCManager hwPcManager = HwFrameworkFactory.getHwPCManager();
            if (hwPcManager != null) {
                return hwPcManager.getService();
            }
            return null;
        } catch (Exception e) {
            Log.e("HwPCUtils", "getHwPCManager error");
            return null;
        }
    }

    private static boolean supportHwPCCastFeature(Display display) {
        boolean z = false;
        if (display == null) {
            return false;
        }
        int type = display.getType();
        if (type == 2) {
            z = true;
        } else if (type == 5 || type == 4) {
            z = mSupportOverlay;
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:7:0x0016, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Bundle hookStartActivityOptions(Context context, Bundle options) {
        if (!enabled()) {
            return options;
        }
        Display display = context.getDisplay();
        if (display == null || (supportHwPCCastFeature(display) ^ 1) != 0 || display.getDisplayId() == -1 || display.getDisplayId() == 0) {
            return options;
        }
        if (enabledInPad() && PKG_DESKTOP_EXPLORER.equals(context.getPackageName()) && (isPcCastMode() ^ 1) != 0) {
            log(TAG, "hookStartActivityOptions not in PC mode and return");
            return options;
        }
        ActivityOptions activityOptions = ActivityOptions.fromBundle(options);
        if (activityOptions == null) {
            activityOptions = ActivityOptions.makeBasic();
        }
        if (activityOptions.getLaunchStackId() == -1 && activityOptions.getLaunchDisplayId() == -1) {
            activityOptions.setLaunchDisplayId(display.getDisplayId());
        }
        return activityOptions.toBundle();
    }

    public static int log(String subTag, String msg) {
        if (!LOG_ENABLED) {
            return 0;
        }
        String tag = TAG;
        if (!TextUtils.isEmpty(subTag)) {
            tag = tag + subTag;
        }
        return Log.i(tag, msg);
    }

    public static Context getDisplayContext(Context context, int displayId) {
        Display targetDisplay = ((DisplayManager) context.getSystemService("display")).getDisplay(displayId);
        if (targetDisplay == null) {
            return null;
        }
        return context.createDisplayContext(targetDisplay);
    }

    public static void bdReport(Context context, int eventID, String eventMsg) {
        HwFrameworkFactory.getHwFlogManager().bdReport(context, eventID, eventMsg);
    }

    public static void showImeStatusIcon(int iconResId, String pkgName) {
        String PCTAG = "HwPCUtils";
        try {
            Log.i(PCTAG, "PCU showImeStatusIcon start");
            IHwPCManager pcManager = getHwPCManager();
            if (pcManager != null) {
                pcManager.showImeStatusIcon(iconResId, pkgName);
            }
            Log.i(PCTAG, "PCU showImeStatusIcon end");
        } catch (RemoteException e) {
            Log.e(PCTAG, "call showImeStatusIcon error");
        }
    }

    public static void hideImeStatusIcon(String pkgName) {
        String PCTAG = "HwPCUtils";
        try {
            Log.i(PCTAG, "PCU hideImeStatusIcon start");
            IHwPCManager pcManager = getHwPCManager();
            if (pcManager != null) {
                pcManager.hideImeStatusIcon(pkgName);
            }
            Log.i(PCTAG, "PCU hideImeStatusIcon end");
        } catch (RemoteException e) {
            Log.e(PCTAG, "call hideImeStatusIcon error");
        }
    }

    public static void setInputMethodList(ArrayList<InputMethodInfo> methodList) {
        sMethodList = methodList;
    }

    public static ArrayList<InputMethodInfo> getInputMethodList() {
        return sMethodList;
    }
}
