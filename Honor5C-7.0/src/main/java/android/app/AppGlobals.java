package android.app;

import android.content.pm.IPackageManager;

public class AppGlobals {
    public static Application getInitialApplication() {
        return ActivityThread.currentApplication();
    }

    public static String getInitialPackage() {
        return ActivityThread.currentPackageName();
    }

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
