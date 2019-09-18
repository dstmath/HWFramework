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
import android.os.SystemProperties;
import android.util.Log;
import android.util.LruCache;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AwareBitmapCacher implements IAwareBitmapCacher {
    private static final String BITMAP_CACHER_SIZE = "persist.sys.iaware.size.BitmapDeocodeCache";
    private static final String BITMAP_CACHER_SWITCH = "persist.sys.iaware.switch.BitmapDeocodeCache";
    private static final int INIT_FIAILED_STATUS = -2;
    private static final int MAX_BITMAP_POINT = 1000;
    private static final int MAX_BITMAP_SIZE = 2000000;
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
    private static final int UNIT_K2BYTES = 10;
    private static final String groupBG = "background";
    private static volatile AwareBitmapCacher mInstance = new AwareBitmapCacher();
    private static final String matcher = "cpuset";
    private Application mApplication;
    private int mBitampCacherSize = -1;
    private boolean mBitmapCacherSwitch = false;
    private LruCache<String, Bitmap> mLruCache;
    /* access modifiers changed from: private */
    public MyHandler mMyHandler;
    private String mProcessName;
    private ReentrantReadWriteLock.ReadLock mReadLock;
    private ReentrantReadWriteLock mReadWriteLock;
    private ReentrantReadWriteLock.WriteLock mWriteLock;

    public static class FileContent {
        public static void close(BufferedReader br) {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Log.e(AwareBitmapCacher.TAG, "close exception!");
                }
            }
        }

        public static void close(InputStreamReader isr) {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    Log.e(AwareBitmapCacher.TAG, "close exception!");
                }
            }
        }

        public static void close(FileInputStream fis) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(AwareBitmapCacher.TAG, "close exception!");
                }
            }
        }
    }

    class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        MyActivityLifecycleCallbacks() {
        }

        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        public void onActivityStarted(Activity activity) {
        }

        public void onActivityResumed(Activity activity) {
            if (AwareBitmapCacher.this.mMyHandler != null) {
                AwareBitmapCacher.this.mMyHandler.removeMessages(1002);
            }
        }

        public void onActivityPaused(Activity activity) {
            if (AwareBitmapCacher.this.mMyHandler != null) {
                AwareBitmapCacher.this.mMyHandler.sendEmptyMessageDelayed(1001, 5000);
            }
        }

        public void onActivityStopped(Activity activity) {
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        public void onActivityDestroyed(Activity activity) {
        }
    }

    private class MyHandler extends Handler {
        public MyHandler() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                Log.e(AwareBitmapCacher.TAG, "null == msg");
                return;
            }
            switch (msg.what) {
                case 1000:
                    AwareBitmapCacher.this.handleInit();
                    break;
                case 1001:
                    removeMessages(1002);
                    AwareBitmapCacher.this.handleCheckBgAndRelease();
                    break;
                case 1002:
                    AwareBitmapCacher.this.handleReleaseCache();
                    break;
            }
        }
    }

    public static IAwareBitmapCacher getDefault() {
        return mInstance;
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

    /* Debug info: failed to restart local var, previous not found, register: 6 */
    /* access modifiers changed from: private */
    public void handleInit() {
        try {
            if (this.mBitampCacherSize != -1) {
                Log.d(TAG, "handleInit reinit pid=" + Process.myPid());
                if (this.mBitampCacherSize > 0) {
                    return;
                }
            } else if (Process.myUid() % MUTIUSER_ADD_UID <= 1000) {
                Log.d(TAG, "handleInit system app disable uid=" + Process.myUid());
                if (this.mBitampCacherSize > 0) {
                    return;
                }
            } else if (this.mProcessName == null) {
                Log.e(TAG, "handleInit disable mProcessName=null");
                if (this.mBitampCacherSize > 0) {
                    return;
                }
            } else {
                if (!this.mProcessName.contains("com.android.") && !this.mProcessName.contains("com.huawei.") && !this.mProcessName.contains("com.google.") && !this.mProcessName.contains("android.process.")) {
                    if (!this.mProcessName.contains(":")) {
                        this.mBitmapCacherSwitch = SystemProperties.getBoolean(BITMAP_CACHER_SWITCH, false);
                        if (!this.mBitmapCacherSwitch) {
                            Log.d(TAG, "handleInit switch not opened pid=" + Process.myPid());
                            if (this.mBitampCacherSize > 0) {
                                return;
                            }
                        } else {
                            this.mBitampCacherSize = SystemProperties.getInt(BITMAP_CACHER_SIZE, 0);
                            if (this.mBitampCacherSize > 0) {
                                this.mBitampCacherSize <<= 10;
                                registerActivityCallback();
                                this.mReadWriteLock = new ReentrantReadWriteLock();
                                this.mReadLock = this.mReadWriteLock.readLock();
                                this.mWriteLock = this.mReadWriteLock.writeLock();
                                initCache(this.mBitampCacherSize);
                                return;
                            }
                        }
                    }
                }
                Log.d(TAG, "handleInit disable " + this.mProcessName);
                if (this.mBitampCacherSize > 0) {
                    return;
                }
            }
        } catch (Throwable th) {
            if (this.mBitampCacherSize > 0) {
                throw th;
            }
        }
        this.mBitampCacherSize = -2;
        this.mApplication = null;
        this.mProcessName = null;
        this.mMyHandler = null;
    }

    private void registerActivityCallback() {
        if (this.mApplication != null) {
            this.mApplication.registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
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
                        bm.incReference();
                    }
                }
                this.mLruCache.remove(pathName);
                Log.i(TAG, "getCachedBitmap remove for isRecycled @pathName=" + pathName);
                return null;
            }
            this.mReadLock.unlock();
            return bm;
        } finally {
            this.mReadLock.unlock();
        }
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
                                if (this.mMyHandler != null) {
                                    this.mMyHandler.removeMessages(1002);
                                    this.mMyHandler.sendEmptyMessageDelayed(1002, 60000);
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

    public Bitmap getCachedBitmap(Resources res, int id) {
        return getCachedBitmap(getString(res, id));
    }

    public void cacheBitmap(Resources res, int id, Bitmap bitmap, BitmapFactory.Options opts) {
        cacheBitmap(getString(res, id), bitmap, opts);
    }

    /* access modifiers changed from: private */
    public void handleReleaseCache() {
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
    public void handleCheckBgAndRelease() {
        int myPid = Process.myPid();
        File tmpFile = new File("/proc/" + myPid + "/cgroup");
        if (!tmpFile.exists() || !tmpFile.canRead()) {
            Log.e(TAG, "handleCheckBgAndRelease can't access:" + pathName);
            return;
        }
        boolean procIsBg = true;
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader reader = null;
        try {
            fis = new FileInputStream(tmpFile);
            isr = new InputStreamReader(fis, "UTF-8");
            reader = new BufferedReader(isr);
            while (true) {
                String str = reader.readLine();
                if (str == null) {
                    break;
                } else if (str.contains(matcher)) {
                    procIsBg = str.contains(groupBG);
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException!");
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "FileNotFoundException!");
        } catch (UnsupportedEncodingException e3) {
            Log.e(TAG, "UnsupportedEncodingException!");
        } catch (IOException e4) {
            Log.e(TAG, "IOException!");
        } catch (Throwable th) {
            FileContent.close((BufferedReader) null);
            FileContent.close((InputStreamReader) null);
            FileContent.close((FileInputStream) null);
            throw th;
        }
        FileContent.close(reader);
        FileContent.close(isr);
        FileContent.close(fis);
        if (procIsBg) {
            handleReleaseCache();
        }
    }
}
