package com.android.systemui.shared.system;

import android.util.StatsLog;

public class StatsLogCompat {
    public static void write(int action, int srcState, int dstState, byte[] extension, boolean swipeUpEnabled) {
        StatsLog.write(19, action, srcState, dstState, extension, swipeUpEnabled);
    }

    public static void write(int action, int colorPackageHash, int fontPackageHash, int shapePackageHash, int clockPackageHash, int launcherGrid, int wallpaperCategoryHash, int wallpaperIdHash, int colorPreference, int locationPreference) {
        StatsLog.write(179, action, colorPackageHash, fontPackageHash, shapePackageHash, clockPackageHash, launcherGrid, wallpaperCategoryHash, wallpaperIdHash, colorPreference, locationPreference);
    }
}
