package com.huawei.android.app;

import android.app.ActivityManager;
import android.app.WindowConfiguration;
import android.content.res.Configuration;
import android.graphics.Rect;

public class WindowConfigurationEx {
    public static final int ACTIVITY_TYPE_ASSISTANT = 4;
    public static final int ACTIVITY_TYPE_HOME = 2;
    public static final int ACTIVITY_TYPE_RECENTS = 3;
    public static final int ACTIVITY_TYPE_STANDARD = 1;
    public static final int ACTIVITY_TYPE_UNDEFINED = 0;
    public static final int HW_MULTI_WINDOWING_MODE_FREEFORM = 102;
    public static final int HW_MULTI_WINDOWING_MODE_MAGIC = 103;
    public static final int HW_MULTI_WINDOWING_MODE_PRIMARY = 100;
    public static final int HW_MULTI_WINDOWING_MODE_SECONDARY = 101;
    public static final int WINDOWING_MODE_FREEFORM = 5;
    public static final int WINDOWING_MODE_FULLSCREEN = 1;
    public static final int WINDOWING_MODE_PINNED = 2;
    public static final int WINDOWING_MODE_SPLIT_SCREEN_PRIMARY = 3;
    public static final int WINDOWING_MODE_SPLIT_SCREEN_SECONDARY = 4;
    public static final int WINDOWING_MODE_UNDEFINED = 0;
    private WindowConfiguration mWindowConfiguration;

    public WindowConfiguration getWindowConfiguration() {
        return this.mWindowConfiguration;
    }

    public void setWindowConfiguration(WindowConfiguration windowConfiguration) {
        this.mWindowConfiguration = windowConfiguration;
    }

    public void setWindowingMode(int windowingMode) {
        this.mWindowConfiguration.setWindowingMode(windowingMode);
    }

    public void setBounds(Rect rect) {
        this.mWindowConfiguration.setBounds(rect);
    }

    public void setAppBounds(Rect rect) {
        this.mWindowConfiguration.setAppBounds(rect);
    }

    public static boolean isHwFreeFormWindowingMode(int windowmode) {
        return WindowConfiguration.isHwFreeFormWindowingMode(windowmode);
    }

    public static boolean isHwMultiStackWindowingMode(int windowmode) {
        return WindowConfiguration.isHwMultiStackWindowingMode(windowmode);
    }

    public Rect getBounds() {
        return this.mWindowConfiguration.getBounds();
    }

    public static int getActivityType(ActivityManager.RunningTaskInfo info) {
        return info.configuration.windowConfiguration.getActivityType();
    }

    public static boolean isHwSplitScreenWindowingMode(int windowingMode) {
        return WindowConfiguration.isHwSplitScreenWindowingMode(windowingMode);
    }

    public static boolean isSplitScreenWindowingMode(int windowingMode) {
        return WindowConfiguration.isSplitScreenWindowingMode(windowingMode);
    }

    public static void setMagicWindowRotation(Configuration configuration, int rotation) {
        if (configuration != null && configuration.windowConfiguration != null && rotation >= 0 && rotation <= 3) {
            configuration.windowConfiguration.setRotation(rotation);
        }
    }
}
