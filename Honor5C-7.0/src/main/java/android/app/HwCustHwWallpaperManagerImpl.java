package android.app;

import android.os.SystemProperties;

public class HwCustHwWallpaperManagerImpl extends HwCustHwWallpaperManager {
    public boolean isScrollWallpaper(int max, int min, int width, int height) {
        if (SystemProperties.getBoolean("ro.config.isScrollWallpaper", false) && width == min * 2 && height == max) {
            return true;
        }
        return false;
    }
}
