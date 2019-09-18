package com.huawei.android.util;

import android.content.Context;
import android.content.res.HwPCMultiWindowCompatibility;
import android.os.Bundle;
import android.util.HwPCUtils;

public class HwPCUtilsEx {
    public static final String ACTION_CHANGE_PC_WALLPAPER = "com.huawei.pcfilemanager.change_wallpaper";
    public static final int FORCED_PC_DISPLAY_DENSITY_160 = 1001;
    public static final int FORCED_PC_DISPLAY_DENSITY_240 = 1002;
    public static final int FORCED_PC_DISPLAY_DENSITY_DEFAULT_240 = 1004;
    public static final int FORCED_PC_DISPLAY_DENSITY_GET = 1000;
    public static final int FORCED_PC_DISPLAY_DENSITY_OTHER = 1003;
    public static final int FORCED_PC_DISPLAY_SIZE_GET_OVERSCAN_MODE = 1005;
    public static final int FORCED_PC_DISPLAY_SIZE_OVERSCAN_MODE = 1006;
    public static final int FORCED_PC_DISPLAY_SIZE_UNOVERSCAN1_MODE = 1008;
    public static final int FORCED_PC_DISPLAY_SIZE_UNOVERSCAN_MODE = 1007;
    public static final String PKG_DESKTOP_EXPLORER = "com.huawei.desktop.explorer";
    public static final String PKG_DESKTOP_SYSTEMUI = "com.huawei.desktop.systemui";
    public static final int REPORT_CLICK_HOME = 10000;
    public static final int REPORT_CLICK_KEYBOARD = 10006;
    public static final int REPORT_CLICK_MUTETILE = 10007;
    public static final int REPORT_CLICK_SEARCH = 10012;
    public static final int REPORT_CURRENT_TASK_SIZE = 10017;
    public static final int REPORT_ENTER_HELP_PAGE = 10019;
    public static final int REPORT_ENTER_HELP_PAGE_CONNECT = 10020;
    public static final int REPORT_ENTER_HELP_PAGE_GUIDE = 10021;
    public static final int REPORT_ENTER_MY_COMPUTER = 10022;
    public static final int REPORT_LASE_PEN_ERASE_PEN = 10031;
    public static final int REPORT_LASE_PEN_SELECT_LASERS = 10029;
    public static final int REPORT_LASE_PEN_SELECT_MOUSE = 10028;
    public static final int REPORT_LASE_PEN_START_DRAW = 10030;
    public static final int REPORT_PINED_APP = 10013;
    public static final int REPORT_SKIP_GUIDE = 10018;
    public static final int REPORT_START_APP_AT_PC = 10015;
    public static final int REPORT_START_APP_FROM_DOCK = 10014;
    public static final int REPORT_STOP_APP_AT_PC = 10016;
    public static final int REPORT_TOUCHPAD_ON_CREATE = 10024;
    public static final int REPORT_TOUCHPAD_ON_STOP = 10025;
    public static final int REPORT_TOUCHPAD_SHOW_INPUT = 10023;

    public enum ProjectionMode {
        DESKTOP_MODE,
        PHONE_MODE
    }

    public static final boolean isValidExtDisplayId(int displayId) {
        return HwPCUtils.isValidExtDisplayId(displayId);
    }

    public static final boolean isValidExtDisplayId(Context context) {
        return HwPCUtils.isValidExtDisplayId(context);
    }

    public static final int getTypeLauncherLike() {
        return 2103;
    }

    public static final int getTypeBootProgress() {
        return 2021;
    }

    public static final boolean isPcCastMode() {
        return HwPCUtils.isPcCastMode();
    }

    public static void bdReport(Context context, int eventID, String eventMsg) {
        HwPCUtils.bdReport(context, eventID, eventMsg);
    }

    public static Bundle hookStartActivityOptions(Context context, Bundle options) {
        return HwPCUtils.hookStartActivityOptions(context, options);
    }

    public static final int getTypeLightDraw() {
        return 2104;
    }

    public static final String getLightDrawViewName() {
        return "com.huawei.systemui.mk.lighterdrawer.LighterDrawView";
    }

    public static boolean isPcDynamicStack(int stackId) {
        return HwPCUtils.isPcDynamicStack(stackId);
    }

    public static boolean isFullscreenable(int windowState) {
        return HwPCMultiWindowCompatibility.isFullscreenable(windowState);
    }

    public static boolean isLayoutFullscreen(int windowState) {
        return HwPCMultiWindowCompatibility.isLayoutFullscreen(windowState);
    }

    public static boolean isVideoCouldOnlyFullscreen(int windowState) {
        return HwPCMultiWindowCompatibility.isVideoCouldOnlyFullscreen(windowState);
    }

    public static boolean isShowTopbar(int windowState) {
        return HwPCMultiWindowCompatibility.isShowTopbar(windowState);
    }

    public static void ignoreInjectEventForFreeMouse(boolean ignore) {
        HwPCUtils.ignoreInjectEventForFreeMouse(ignore);
    }
}
