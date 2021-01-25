package android.app;

import android.annotation.UnsupportedAppUsage;
import android.content.pm.IPackageManager;

public class AppGlobals {
    @UnsupportedAppUsage
    public static Application getInitialApplication() {
        return ActivityThread.currentApplication();
    }

    @UnsupportedAppUsage
    public static String getInitialPackage() {
        return ActivityThread.currentPackageName();
    }

    @UnsupportedAppUsage
    public static IPackageManager getPackageManager() {
        return ActivityThread.getPackageManager();
    }

    public static int getIntCoreSetting(String key, int defaultValue) {
        ActivityThread currentActivityThread = ActivityThread.currentActivityThread();
        if (currentActivityThread != null) {
            return currentActivityThread.getIntCoreSetting(key, defaultValue);
        }
        return defaultValue;
    }
}
