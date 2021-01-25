package com.android.systemui.shared.system;

import android.content.Context;
import android.content.res.Resources;
import android.view.ViewConfiguration;
import com.android.internal.policy.ScreenDecorationsUtils;
import com.android.server.location.HwLogRecordManager;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.StringJoiner;

public class QuickStepContract {
    public static final String KEY_EXTRA_INPUT_MONITOR = "extra_input_monitor";
    public static final String KEY_EXTRA_SUPPORTS_WINDOW_CORNERS = "extra_supports_window_corners";
    public static final String KEY_EXTRA_SYSUI_PROXY = "extra_sysui_proxy";
    public static final String KEY_EXTRA_WINDOW_CORNER_RADIUS = "extra_window_corner_radius";
    public static final String NAV_BAR_MODE_2BUTTON_OVERLAY = "com.android.internal.systemui.navbar.twobutton";
    public static final String NAV_BAR_MODE_3BUTTON_OVERLAY = "com.android.internal.systemui.navbar.threebutton";
    public static final String NAV_BAR_MODE_GESTURAL_OVERLAY = "com.android.internal.systemui.navbar.gestural";
    public static final float QUICKSTEP_TOUCH_SLOP_RATIO = 3.0f;
    public static final int SYSUI_STATE_A11Y_BUTTON_CLICKABLE = 16;
    public static final int SYSUI_STATE_A11Y_BUTTON_LONG_CLICKABLE = 32;
    public static final int SYSUI_STATE_BOUNCER_SHOWING = 8;
    public static final int SYSUI_STATE_HOME_DISABLED = 256;
    public static final int SYSUI_STATE_NAV_BAR_HIDDEN = 2;
    public static final int SYSUI_STATE_NOTIFICATION_PANEL_EXPANDED = 4;
    public static final int SYSUI_STATE_OVERVIEW_DISABLED = 128;
    public static final int SYSUI_STATE_SCREEN_PINNING = 1;
    public static final int SYSUI_STATE_STATUS_BAR_KEYGUARD_SHOWING = 64;
    public static final int SYSUI_STATE_STATUS_BAR_KEYGUARD_SHOWING_OCCLUDED = 512;

    @Retention(RetentionPolicy.SOURCE)
    public @interface SystemUiStateFlags {
    }

    public static String getSystemUiStateString(int flags) {
        StringJoiner str = new StringJoiner(HwLogRecordManager.VERTICAL_SEPARATE);
        String str2 = "";
        str.add((flags & 1) != 0 ? "screen_pinned" : str2);
        str.add((flags & 128) != 0 ? "overview_disabled" : str2);
        str.add((flags & 256) != 0 ? "home_disabled" : str2);
        str.add((flags & 2) != 0 ? "navbar_hidden" : str2);
        str.add((flags & 4) != 0 ? "notif_visible" : str2);
        str.add((flags & 64) != 0 ? "keygrd_visible" : str2);
        str.add((flags & 512) != 0 ? "keygrd_occluded" : str2);
        str.add((flags & 8) != 0 ? "bouncer_visible" : str2);
        str.add((flags & 16) != 0 ? "a11y_click" : str2);
        if ((flags & 32) != 0) {
            str2 = "a11y_long_click";
        }
        str.add(str2);
        return str.toString();
    }

    public static final float getQuickStepTouchSlopPx(Context context) {
        return ((float) ViewConfiguration.get(context).getScaledTouchSlop()) * 3.0f;
    }

    public static int getQuickStepDragSlopPx() {
        return convertDpToPixel(10.0f);
    }

    public static int getQuickStepTouchSlopPx() {
        return convertDpToPixel(24.0f);
    }

    public static int getQuickScrubTouchSlopPx() {
        return convertDpToPixel(24.0f);
    }

    private static int convertDpToPixel(float dp) {
        return (int) (Resources.getSystem().getDisplayMetrics().density * dp);
    }

    public static boolean isAssistantGestureDisabled(int sysuiStateFlags) {
        if ((sysuiStateFlags & 11) != 0) {
            return true;
        }
        if ((sysuiStateFlags & 4) == 0 || (sysuiStateFlags & 64) != 0) {
            return false;
        }
        return true;
    }

    public static boolean isBackGestureDisabled(int sysuiStateFlags) {
        if ((sysuiStateFlags & 8) == 0 && (sysuiStateFlags & 71) != 0) {
            return true;
        }
        return false;
    }

    public static boolean isGesturalMode(int mode) {
        return mode == 2;
    }

    public static boolean isSwipeUpMode(int mode) {
        return mode == 1;
    }

    public static boolean isLegacyMode(int mode) {
        return mode == 0;
    }

    public static float getWindowCornerRadius(Resources resources) {
        return ScreenDecorationsUtils.getWindowCornerRadius(resources);
    }

    public static boolean supportsRoundedCornersOnWindows(Resources resources) {
        return ScreenDecorationsUtils.supportsRoundedCornersOnWindows(resources);
    }
}
