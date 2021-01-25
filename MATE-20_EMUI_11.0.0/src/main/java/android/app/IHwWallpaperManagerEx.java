package android.app;

import android.graphics.Bitmap;
import android.graphics.Rect;

public interface IHwWallpaperManagerEx {
    Bitmap createDefaultWallpaperBitmap(Bitmap bitmap);

    Bitmap getBlurBitmap(Rect rect);

    void setWallpaperStartingPoints(int[] iArr);
}
