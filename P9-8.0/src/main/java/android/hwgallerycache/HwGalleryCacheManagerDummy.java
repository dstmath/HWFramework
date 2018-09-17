package android.hwgallerycache;

import android.graphics.Bitmap;
import android.graphics.Bitmap.GalleryCacheInfo;
import android.graphics.BitmapFactory.Options;
import android.hwgallerycache.HwGalleryCacheManager.IHwGalleryCacheManager;
import android.widget.ImageView;
import java.io.InputStream;

public class HwGalleryCacheManagerDummy implements IHwGalleryCacheManager {
    public Bitmap getGalleryCachedImage(InputStream is, Options opts) {
        return null;
    }

    public boolean isGalleryCacheEffect() {
        return false;
    }

    public void recycleCacheInfo(GalleryCacheInfo cache) {
    }

    public boolean revertWechatThumb(ImageView view, Bitmap bm) {
        return false;
    }

    public Bitmap getGalleryCachedVideo(int rowid, long timeModified, Options opts) {
        return null;
    }
}
