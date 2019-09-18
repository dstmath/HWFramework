package android.hwgallerycache;

import android.common.HwFrameworkFactory;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Process;
import android.util.Log;
import android.widget.ImageView;
import java.io.InputStream;

public class HwGalleryCacheManager {
    private static final String TAG = "HwGalleryCacheManager";
    private static IHwGalleryCacheManager sInstance = null;

    public interface IHwGalleryCacheManager {
        Bitmap getGalleryCachedImage(InputStream inputStream, BitmapFactory.Options options);

        Bitmap getGalleryCachedVideo(int i, long j, BitmapFactory.Options options);

        boolean isGalleryCacheEffect();

        void recycleCacheInfo(Bitmap.GalleryCacheInfo galleryCacheInfo);

        boolean revertWechatThumb(ImageView imageView, Bitmap bitmap);
    }

    private static synchronized IHwGalleryCacheManager getImplObject() {
        synchronized (HwGalleryCacheManager.class) {
            if (sInstance != null) {
                IHwGalleryCacheManager iHwGalleryCacheManager = sInstance;
                return iHwGalleryCacheManager;
            } else if (Process.myPpid() == 1) {
                return null;
            } else {
                IHwGalleryCacheManager instance = null;
                IHwGalleryCacheManagerFactory obj = HwFrameworkFactory.getHwGalleryCacheManagerFactory();
                if (obj != null) {
                    instance = obj.getGalleryCacheManagerInstance();
                }
                if (instance != null) {
                    sInstance = instance;
                } else {
                    Log.w(TAG, "can't get impl object from vendor, use default implemention");
                    sInstance = new HwGalleryCacheManagerDummy();
                }
                IHwGalleryCacheManager iHwGalleryCacheManager2 = sInstance;
                return iHwGalleryCacheManager2;
            }
        }
    }

    public static Bitmap getGalleryCachedImage(InputStream is, BitmapFactory.Options opts) {
        if (getImplObject() != null) {
            return getImplObject().getGalleryCachedImage(is, opts);
        }
        return null;
    }

    public static boolean isGalleryCacheEffect() {
        if (getImplObject() != null) {
            return getImplObject().isGalleryCacheEffect();
        }
        return false;
    }

    public static void recycleCacheInfo(Bitmap.GalleryCacheInfo cache) {
        if (getImplObject() != null) {
            getImplObject().recycleCacheInfo(cache);
        }
    }

    public static boolean revertWechatThumb(ImageView view, Bitmap bm) {
        if (getImplObject() != null) {
            return getImplObject().revertWechatThumb(view, bm);
        }
        return false;
    }

    public static Bitmap getGalleryCachedVideo(int rowid, long timeModified, BitmapFactory.Options opts) {
        if (getImplObject() != null) {
            return getImplObject().getGalleryCachedVideo(rowid, timeModified, opts);
        }
        return null;
    }
}
