package android.app;

import android.content.Context;
import android.provider.Settings;

public class HwCustHwWallpaperManagerImpl extends HwCustHwWallpaperManager {
    public boolean isScrollWallpaper(int max, int min, int width, int height, Context context) {
        if ("true".equals(Settings.System.getString(context.getContentResolver(), "isScrollWallpaper")) && width == 2 * min && height == max) {
            return true;
        }
        return false;
    }
}
