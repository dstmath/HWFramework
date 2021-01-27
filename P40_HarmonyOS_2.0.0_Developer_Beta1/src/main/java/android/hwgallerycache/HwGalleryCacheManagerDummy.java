package android.hwgallerycache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hwgallerycache.HwGalleryCacheManager;
import android.widget.ImageView;
import java.io.InputStream;

public class HwGalleryCacheManagerDummy implements HwGalleryCacheManager.IHwGalleryCacheManager {
    public static HwGalleryCacheManager.IHwGalleryCacheManager getDefault() {
        return new HwGalleryCacheManagerDummy();
    }

    @Override // android.hwgallerycache.HwGalleryCacheManager.IHwGalleryCacheManager
    public Bitmap getGalleryCachedImage(InputStream is, BitmapFactory.Options opts) {
        return null;
    }

    @Override // android.hwgallerycache.HwGalleryCacheManager.IHwGalleryCacheManager
    public boolean isGalleryCacheEffect() {
        return false;
    }

    @Override // android.hwgallerycache.HwGalleryCacheManager.IHwGalleryCacheManager
    public void recycleCacheInfo(Bitmap.GalleryCacheInfo cache) {
    }

    @Override // android.hwgallerycache.HwGalleryCacheManager.IHwGalleryCacheManager
    public boolean revertWechatThumb(ImageView view, Bitmap bm) {
        return false;
    }

    @Override // android.hwgallerycache.HwGalleryCacheManager.IHwGalleryCacheManager
    public Bitmap getGalleryCachedVideo(int rowid, long timeModified, BitmapFactory.Options opts) {
        return null;
    }
}
