package android.graphics;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DefaultAwareBitmapCacherImpl implements IAwareBitmapCacher {
    private static final Object SLOCK = new Object();
    private static DefaultAwareBitmapCacherImpl sInstance;

    public static IAwareBitmapCacher getDefault() {
        DefaultAwareBitmapCacherImpl defaultAwareBitmapCacherImpl;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new DefaultAwareBitmapCacherImpl();
            }
            defaultAwareBitmapCacherImpl = sInstance;
        }
        return defaultAwareBitmapCacherImpl;
    }

    @Override // android.graphics.IAwareBitmapCacher
    public void init(String processName, Application app) {
    }

    @Override // android.graphics.IAwareBitmapCacher
    public Bitmap getCachedBitmap(String pathName) {
        return null;
    }

    @Override // android.graphics.IAwareBitmapCacher
    public Bitmap getCachedBitmap(Resources res, int id) {
        return null;
    }

    @Override // android.graphics.IAwareBitmapCacher
    public void cacheBitmap(String pathName, Bitmap bitmap, BitmapFactory.Options opts) {
    }

    @Override // android.graphics.IAwareBitmapCacher
    public void cacheBitmap(Resources res, int id, Bitmap bitmap, BitmapFactory.Options opts) {
    }
}
