package android.hwgallerycache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hwgallerycache.HwGalleryCacheManager;
import android.widget.ImageView;
import java.io.InputStream;

public class HwGalleryCacheManagerDummy implements HwGalleryCacheManager.IHwGalleryCacheManager {
    public Bitmap getGalleryCachedImage(InputStream is, BitmapFactory.Options opts) {
        return null;
    }

    public boolean isGalleryCacheEffect() {
        return false;
    }

    public void recycleCacheInfo(Bitmap.GalleryCacheInfo cache) {
    }

    public boolean revertWechatThumb(ImageView view, Bitmap bm) {
        return false;
    }

    public Bitmap getGalleryCachedVideo(int rowid, long timeModified, BitmapFactory.Options opts) {
        return null;
    }
}
