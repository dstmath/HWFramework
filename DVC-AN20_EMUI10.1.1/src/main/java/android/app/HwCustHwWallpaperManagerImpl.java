package android.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.provider.Settings;

public class HwCustHwWallpaperManagerImpl extends HwCustHwWallpaperManager {
    public boolean isScrollWallpaper(Context context) {
        if (context == null || !"true".equals(Settings.System.getString(context.getContentResolver(), "isScrollWallpaper"))) {
            return false;
        }
        Settings.System.putInt(context.getContentResolver(), "is_scroll", 1);
        return true;
    }

    public Bitmap createScrollBitmap(int width, int height, Bitmap bitmap) {
        if (height <= width || width == 0) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        float scale = ((float) height) / ((float) width);
        matrix.setScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, (height - width) / 2, width, width, matrix, true);
    }
}
