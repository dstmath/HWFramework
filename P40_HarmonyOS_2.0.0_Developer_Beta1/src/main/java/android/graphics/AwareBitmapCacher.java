package android.graphics;

import android.app.Activity;
import android.app.Application;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.util.LruCache;
import com.huawei.android.graphics.BitmapExt;
import com.huawei.android.os.SystemPropertiesEx;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AwareBitmapCacher extends DefaultAwareBitmapCacherImpl {
    private static final String BITMAP_CACHER_SIZE = "persist.sys.iaware.size.BitmapDeocodeCache";
    private static final String BITMAP_CACHER_SWITCH = "persist.sys.iaware.switch.BitmapDeocodeCache";
    private static final int INIT_FIAILED_STATUS = -2;
    private static final String MATCH_BG = "background";
    private static final String MATCH_CPUSET = "cpuset";
    private static final int MAX_BITMAP_SIZE = 3000000;
    private static final int MSG_BITMAP_CACHER_INIT = 1000;
    private static final int MSG_BITMAP_CACHER_RELEASE = 1002;
    private static final int MSG_CHECK_IS_BG_AND_RELEASE = 1001;
    private static final int MUTIUSER_ADD_UID = 100000;
    private static final int SYSTEM_UID = 1000;
    private static final String TAG = "AwareBitmapCacher";
    private static final int TIME_DELAY_BITMAP_CACHER_INIT = 5000;
    private static final int TIME_DELAY_CACHE_RELEASE = 60000;
    private static final int TIME_DELAY_CHECK_RELEASE = 5000;
    private static final int UNINITED_STATUS = -1;
    private static final int UNIT_KB = 10;
    private static volatile AwareBitmapCacher sInstance = new AwareBitmapCacher();
    private Application mApplication;
    private int mBitampCacherSize = -1;
    private LruCache<String, Bitmap> mLruCache;
    private MyHandler mMyHandler;
    private String mProcessName;
    private ReentrantReadWriteLock.ReadLock mReadLock;
    private ReentrantReadWriteLock mReadWriteLock;
    private ReentrantReadWriteLock.WriteLock mWriteLock;

    public static IAwareBitmapCacher getDefault() {
        return sInstance;
    }

    public void init(String processName, Application app) {
        if (app != null && processName != null) {
            Log.i(TAG, "init processName:" + processName + " pid=" + Process.myPid() + " uid=" + Process.myUid());
            this.mApplication = app;
            this.mProcessName = processName;
            this.mMyHandler = new MyHandler();
            this.mMyHandler.sendEmptyMessageDelayed(1000, 5000);
        }
    }

    private void initCache(int cacheSize) {
        Log.i(TAG, "init lrucache size: " + cacheSize + " pid=" + Process.myPid());
        if (cacheSize > 0) {
            this.mWriteLock.lock();
            try {
                this.mLruCache = new LruCache<String, Bitmap>(cacheSize) {
                    /* class android.graphics.AwareBitmapCacher.AnonymousClass1 */

                    /* access modifiers changed from: protected */
                    public int sizeOf(String key, Bitmap value) {
                        return value.getByteCount();
                    }
                };
            } finally {
                this.mWriteLock.unlock();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleInit() {
        boolean isError = false;
        try {
            if (this.mBitampCacherSize != -1) {
                Log.d(TAG, "handleInit reinit pid=" + Process.myPid());
                if (this.mBitampCacherSize > 0) {
                }
            } else if (isInvalidProcss()) {
                if (this.mBitampCacherSize > 0) {
                }
            } else if (!SystemPropertiesEx.getBoolean(BITMAP_CACHER_SWITCH, false)) {
                Log.d(TAG, "handleInit switch not opened pid=" + Process.myPid());
                if (this.mBitampCacherSize > 0) {
                }
            } else {
                this.mBitampCacherSize = SystemPropertiesEx.getInt(BITMAP_CACHER_SIZE, 0);
                if (this.mBitampCacherSize <= 0) {
                    isError = true;
                }
                if (isError) {
                    this.mBitampCacherSize = -2;
                    this.mApplication = null;
                    this.mProcessName = null;
                    this.mMyHandler = null;
                    return;
                }
                this.mBitampCacherSize <<= 10;
                registerActivityCallback();
                this.mReadWriteLock = new ReentrantReadWriteLock();
                this.mReadLock = this.mReadWriteLock.readLock();
                this.mWriteLock = this.mReadWriteLock.writeLock();
                initCache(this.mBitampCacherSize);
            }
        } finally {
            if (this.mBitampCacherSize <= 0) {
            }
        }
    }

    private boolean isInvalidProcss() {
        if (Process.myUid() % MUTIUSER_ADD_UID > 1000 && this.mProcessName != null && !isInvalidProcName()) {
            return false;
        }
        return true;
    }

    private boolean isInvalidProcName() {
        if (this.mProcessName.contains("com.android.") || this.mProcessName.contains("com.huawei.") || this.mProcessName.contains("com.google.") || this.mProcessName.contains("android.process.") || this.mProcessName.contains(":")) {
            return true;
        }
        return false;
    }

    private void registerActivityCallback() {
        Application application = this.mApplication;
        if (application != null) {
            application.registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
        }
    }

    public Bitmap getCachedBitmap(String pathName) {
        if (this.mLruCache == null) {
            return null;
        }
        if (pathName == null) {
            Log.e(TAG, "getCachedBitmap pathName null");
            return null;
        }
        this.mReadLock.lock();
        try {
            Bitmap bm = this.mLruCache.get(pathName);
            if (bm != null) {
                if (!bm.isRecycled()) {
                    if (bm.getByteCount() > 0) {
                        BitmapExt.incReference(bm);
                    }
                }
                this.mLruCache.remove(pathName);
                return null;
            }
            this.mReadLock.unlock();
            return bm;
        } finally {
            this.mReadLock.unlock();
        }
    }

    public Bitmap getCachedBitmap(Resources res, int id) {
        return getCachedBitmap(getString(res, id));
    }

    public void cacheBitmap(String pathName, Bitmap bitmap, BitmapFactory.Options opts) {
        if (this.mLruCache != null) {
            if (pathName == null) {
                Log.e(TAG, "cacheBitmap pathName null");
                return;
            }
            this.mWriteLock.lock();
            if (bitmap != null) {
                try {
                    if (!bitmap.isRecycled()) {
                        int bmByteCount = bitmap.getByteCount();
                        if (bmByteCount < MAX_BITMAP_SIZE) {
                            if (bmByteCount > 0) {
                                this.mLruCache.put(pathName, bitmap);
                                this.mWriteLock.unlock();
                                MyHandler myHandler = this.mMyHandler;
                                if (myHandler != null) {
                                    myHandler.removeMessages(MSG_BITMAP_CACHER_RELEASE);
                                    this.mMyHandler.sendEmptyMessageDelayed(MSG_BITMAP_CACHER_RELEASE, 60000);
                                    return;
                                }
                                return;
                            }
                        }
                        return;
                    }
                } finally {
                    this.mWriteLock.unlock();
                }
            }
            Log.i(TAG, "cacheBitmap bitmap null");
            this.mWriteLock.unlock();
        }
    }

    public void cacheBitmap(Resources res, int id, Bitmap bitmap, BitmapFactory.Options opts) {
        cacheBitmap(getString(res, id), bitmap, opts);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReleaseCache() {
        if (this.mLruCache != null) {
            Log.i(TAG, "handleReleaseCache: pid=" + Process.myPid());
            initCache(this.mBitampCacherSize);
        }
    }

    private String getString(Resources res, int id) {
        if (res == null) {
            return null;
        }
        return res.toString() + id;
    }

    /* access modifiers changed from: private */
    public class MyHandler extends Handler {
        public MyHandler() {
            super(Looper.getMainLooper());
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                Log.e(AwareBitmapCacher.TAG, "null msg");
                return;
            }
            switch (msg.what) {
                case 1000:
                    AwareBitmapCacher.this.handleInit();
                    return;
                case AwareBitmapCacher.MSG_CHECK_IS_BG_AND_RELEASE /* 1001 */:
                    removeMessages(AwareBitmapCacher.MSG_BITMAP_CACHER_RELEASE);
                    AwareBitmapCacher.this.handleCheckBgAndRelease();
                    return;
                case AwareBitmapCacher.MSG_BITMAP_CACHER_RELEASE /* 1002 */:
                    AwareBitmapCacher.this.handleReleaseCache();
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCheckBgAndRelease() {
        String pathName = "/proc/" + Process.myPid() + "/cgroup";
        File tmpFile = new File(pathName);
        if (!tmpFile.exists() || !tmpFile.canRead()) {
            Log.e(TAG, "handleCheckBgAndRelease can't access:" + pathName);
            return;
        }
        boolean isProcBg = true;
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader reader = null;
        try {
            fis = new FileInputStream(tmpFile);
            isr = new InputStreamReader(fis, "UTF-8");
            reader = new BufferedReader(isr);
            while (true) {
                String str = reader.readLine();
                if (str != null) {
                    if (str.contains(MATCH_CPUSET)) {
                        isProcBg = str.contains(MATCH_BG);
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (FileNotFoundException | UnsupportedEncodingException | NumberFormatException e) {
            Log.e(TAG, "exception!");
        } catch (IOException e2) {
            Log.e(TAG, "exception!");
        } catch (Throwable th) {
            FileContent.close(null);
            FileContent.close(null);
            FileContent.close(null);
            throw th;
        }
        FileContent.close(reader);
        FileContent.close(isr);
        FileContent.close(fis);
        if (isProcBg) {
            handleReleaseCache();
        }
    }

    /* access modifiers changed from: package-private */
    public class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        MyActivityLifecycleCallbacks() {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityStarted(Activity activity) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityResumed(Activity activity) {
            if (AwareBitmapCacher.this.mMyHandler != null) {
                AwareBitmapCacher.this.mMyHandler.removeMessages(AwareBitmapCacher.MSG_BITMAP_CACHER_RELEASE);
            }
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityPaused(Activity activity) {
            if (AwareBitmapCacher.this.mMyHandler != null) {
                AwareBitmapCacher.this.mMyHandler.sendEmptyMessageDelayed(AwareBitmapCacher.MSG_CHECK_IS_BG_AND_RELEASE, 5000);
            }
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityStopped(Activity activity) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityDestroyed(Activity activity) {
        }
    }

    /* access modifiers changed from: private */
    public static class FileContent {
        private FileContent() {
        }

        public static void close(Closeable io) {
            if (io != null) {
                try {
                    io.close();
                } catch (IOException e) {
                    Log.e(AwareBitmapCacher.TAG, "close exception!");
                }
            }
        }
    }
}
