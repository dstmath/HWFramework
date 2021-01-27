package android.app;

import android.content.Context;
import android.graphics.Bitmap;

public class HwCustHwWallpaperManager {
    public boolean isScrollWallpaper(Context context) {
        return false;
    }

    public Bitmap createScrollBitmap(int width, int height, Bitmap bitmap) {
        return bitmap;
    }
}
