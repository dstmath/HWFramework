package android.app;

import android.graphics.Bitmap;
import android.graphics.Rect;

public abstract class AbsWallpaperManagerInner {

    public interface IBlurWallpaperCallback {
        void onBlurWallpaperChanged();
    }

    public Bitmap getBlurBitmap(Rect rect) {
        return null;
    }

    public void setCallback(IBlurWallpaperCallback callback) {
    }
}
