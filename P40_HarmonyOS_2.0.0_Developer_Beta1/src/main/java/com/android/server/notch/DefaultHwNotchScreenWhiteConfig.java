package com.android.server.notch;

import android.content.Context;
import android.view.WindowManager;
import com.android.server.policy.WindowManagerPolicyEx;
import java.util.List;

public class DefaultHwNotchScreenWhiteConfig {
    public static final String DISPLAY_NOTCH_STATUS = "display_notch_status";
    public static final int NOTCH_MODE_ALWAYS = 1;
    public static final int NOTCH_MODE_NEVER = 2;
    private static DefaultHwNotchScreenWhiteConfig defaultHwNotchScreenWhiteConfig;

    public static DefaultHwNotchScreenWhiteConfig getInstance() {
        if (defaultHwNotchScreenWhiteConfig == null) {
            defaultHwNotchScreenWhiteConfig = new DefaultHwNotchScreenWhiteConfig();
        }
        return defaultHwNotchScreenWhiteConfig;
    }

    public boolean notchSupportWindow(WindowManager.LayoutParams attrs) {
        return false;
    }

    public void updateWhiteListData() {
    }

    public void updateVersionCodeInNoch(String packageName, String flag, int updateVersionCode) {
    }

    public boolean isNotchAppInfo(WindowManagerPolicyEx.WindowStateEx win) {
        return false;
    }

    public boolean isNoneNotchAppInfo(WindowManagerPolicyEx.WindowStateEx win) {
        return false;
    }

    public boolean isNotchAppHideInfo(WindowManagerPolicyEx.WindowStateEx win) {
        return false;
    }

    public boolean isNoneNotchAppHideInfo(WindowManagerPolicyEx.WindowStateEx win) {
        return false;
    }

    public boolean isNoneNotchAppWithStatusbarInfo(WindowManagerPolicyEx.WindowStateEx win) {
        return false;
    }

    public boolean isSystemAppInfo(String systemInfo) {
        return false;
    }

    public void registerNotchSwitchListener(Context context, NotchSwitchListener listener) {
    }

    public void unregisterNotchSwitchListener(Context context, NotchSwitchListener listener) {
    }

    public void updateWhitelistByHot(Context context, String fileName) {
    }

    public List<String> getNotchSystemApps() {
        return null;
    }

    public void removeAppUseNotchMode(String packageName) {
    }

    public void updateAppUseNotchMode(String packageName, int mode) {
    }

    public int getAppUseNotchMode(String packageName) {
        return 0;
    }

    public void setNotchSwitchStatus(boolean isNotchSwitchOpen) {
    }
}
