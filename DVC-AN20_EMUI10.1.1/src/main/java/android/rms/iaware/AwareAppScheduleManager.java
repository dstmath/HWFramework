package android.rms.iaware;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.rms.iaware.scenerecog.HwAppSceneImpl;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.WebView;
import com.huawei.uikit.effect.BuildConfig;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONException;
import org.json.JSONObject;

public class AwareAppScheduleManager {
    private static final String APP_FAMILYLINKMANAGER = "com.google.android.apps.kids.familylinkmanager";
    private static final String APP_VERSION = "AppVersion";
    private static final String ART64_PATH = "/oat/arm64/base.art";
    private static final String ART_PATH = "/oat/arm/base.art";
    private static final String BASE_APK = "/base.apk";
    private static final int CAMERA_SCAN_SCENE_ID = 30101;
    private static final String DATA_APP = "/data/app/";
    private static final String FILE_NAME_DECODEBITMAP = "hw_cached_resid.list";
    private static final int FINISH_HANDLER_DELAY_TIME = 20000;
    private static final int FINISH_LEARNED_DELAY_TIME = 5000;
    private static final String FRAMEWORK_RES_RESOURCE = "/system/framework/framework-res.apk";
    private static final String LEARNING_DATA = "aware_learning_data";
    private static final int LENGTH_POLICY = 8;
    private static final String LOAD_URL = "feature_1";
    private static final int MAX_CACHED_SIZE = 256;
    private static final int MAX_RESID_COUNT = 20;
    private static final int MSG_FINISH_HANDLER = 3;
    private static final int MSG_FINISH_LEARNED = 2;
    private static final int MSG_READ_FROM_DISK = 1;
    private static final int MSG_UPDATE_WEB_VIEW_LEARNED = 4;
    private static final long OPT_VALID_TIME = 120000;
    private static final String PRIMARY_PROF = "primary.prof";
    private static final String SPLIT_NAME = "/";
    private static final int STATUS_FINISH_HANDLER = 3;
    private static final int STATUS_FINISH_LEARN = 2;
    private static final int STATUS_INIT = 0;
    private static final int STATUS_INIT_SUCCESS = 1;
    private static final String SYSTEM_VERSION = "SystemVersion";
    private static final String TAG = "AwareAppScheduleManager";
    private static final String TEMP_ART64_PATH = "/oat/arm64/temp.art";
    private static final String TEMP_ART_PATH = "/oat/arm/temp.art";
    private static final String TEMP_ODEX64_PATH = "/oat/arm64/temp.odex";
    private static final String TEMP_ODEX_PATH = "/oat/arm/temp.odex";
    private static final int TRY_MAX_TIMES = 25;
    private static final int UPDATE_LEARNED_DELAY_TIME = 15000;
    private static final String VDEX64_PATH = "/oat/arm64/base.vdex";
    private static final String VDEX_PATH = "/oat/arm/base.vdex";
    private static AwareAppScheduleManager sInstance;
    private ApplicationInfo mAppInfo;
    private int mAppVersionCode = 0;
    private File mCacheDir;
    private final Map<Integer, Boolean> mCacheDrawableResIds = new HashMap();
    private int mCameraScanRecogVersion = 0;
    private boolean mCameraScanSwitch = false;
    private boolean mClickViewSpeedUpSwitch = false;
    private Context mContext;
    private Object mDecodeBitmapFileLock = new Object();
    private int mDecodeTime = 10000000;
    private boolean mDisableGlInit = false;
    private AtomicBoolean mDrawableCacheFeature = new AtomicBoolean(false);
    private final SparseArray<Drawable> mDrawableCaches = new SparseArray<>();
    private boolean mHadInitWebView = false;
    private boolean mHadLoadUrl = false;
    private volatile AppScheduleHandler mHandler = null;
    private HandlerThread mHandlerThread;
    private AtomicBoolean mHasNewResId = new AtomicBoolean(false);
    private boolean mIsMainProcess = true;
    private Object mLearningDataLock = new Object();
    private AtomicInteger mLoadDrawableResId = new AtomicInteger(0);
    private String mPackageName;
    private int mPid;
    private boolean mPreloadWebViewSwitch = false;
    private AtomicBoolean mResultShouldReplaced = new AtomicBoolean(false);
    private long mStartTime = 0;
    private AtomicInteger mStatus = new AtomicInteger(0);
    private String mSystemVersion = BuildConfig.FLAVOR;
    private boolean mWillLoadUrl = false;

    public static synchronized AwareAppScheduleManager getInstance() {
        AwareAppScheduleManager awareAppScheduleManager;
        synchronized (AwareAppScheduleManager.class) {
            if (sInstance == null) {
                sInstance = new AwareAppScheduleManager();
            }
            awareAppScheduleManager = sInstance;
        }
        return awareAppScheduleManager;
    }

    private AwareAppScheduleManager() {
    }

    private boolean hasBaseApk(ApplicationInfo appInfo) {
        return appInfo.sourceDir.contains(BASE_APK);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasArt(ApplicationInfo appInfo) {
        int index;
        if (appInfo == null || (index = appInfo.sourceDir.lastIndexOf(SPLIT_NAME)) == -1) {
            return false;
        }
        String appPath = appInfo.sourceDir.substring(0, index);
        File artFile = new File(appPath, ART_PATH);
        File art64File = new File(appPath, ART64_PATH);
        File artTempFile = new File(appPath, TEMP_ART_PATH);
        File art64TempFile = new File(appPath, TEMP_ART64_PATH);
        File odexTempFile = new File(appPath, TEMP_ODEX_PATH);
        File odex64TempFile = new File(appPath, TEMP_ODEX64_PATH);
        if (artFile.exists() || art64File.exists() || artTempFile.exists() || art64TempFile.exists() || odexTempFile.exists() || odex64TempFile.exists()) {
            return true;
        }
        return false;
    }

    private boolean IsValidTimeScope(File apkFile, String appPath, String cmpPath) {
        File cmpFile = new File(appPath, cmpPath);
        if (!cmpFile.exists()) {
            return false;
        }
        long apkTime = apkFile.lastModified();
        long cmpTime = cmpFile.lastModified();
        if ((cmpTime > apkTime ? cmpTime - apkTime : 0) <= OPT_VALID_TIME) {
            return true;
        }
        return false;
    }

    private boolean hasValidVdex(ApplicationInfo appInfo) {
        int index;
        boolean need_compile = false;
        if (appInfo == null || (index = appInfo.sourceDir.lastIndexOf(SPLIT_NAME)) == -1) {
            return false;
        }
        String appPath = appInfo.sourceDir.substring(0, index);
        File apkFile = new File(appPath, BASE_APK);
        if (!apkFile.exists()) {
            return false;
        }
        if (IsValidTimeScope(apkFile, appPath, VDEX_PATH) || IsValidTimeScope(apkFile, appPath, VDEX64_PATH)) {
            need_compile = true;
        }
        return need_compile;
    }

    private boolean isInvalidPkgName(String packageName) {
        if (APP_FAMILYLINKMANAGER.equals(packageName)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasValidProfile(ApplicationInfo appInfo) {
        File profileFile = new File(Environment.getDataProfilesDePackageDirectory(UserHandle.myUserId(), appInfo.packageName), PRIMARY_PROF);
        AwareLog.d(TAG, "profile path " + profileFile.getPath() + ", length " + profileFile.length());
        return profileFile.exists() && profileFile.isFile() && profileFile.length() > 0;
    }

    private boolean isDataApp(ApplicationInfo appInfo) {
        return appInfo.sourceDir.contains(DATA_APP);
    }

    private boolean needNotCompile(ApplicationInfo appInfo) {
        return appInfo.sourceDir == null || appInfo.packageName == null || isInvalidPkgName(appInfo.packageName) || !isDataApp(appInfo) || !hasBaseApk(appInfo) || hasArt(appInfo) || !hasValidVdex(appInfo);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void compileArt(final ApplicationInfo appInfo) {
        if (!SystemProperties.getBoolean("persist.sys.aware.compile.enable", true)) {
            AwareLog.i(TAG, "packageName " + appInfo.packageName + " compile feature is closed. setprop persist.sys.aware.compile.enable true or false.");
        } else if (needNotCompile(appInfo)) {
            AwareLog.d(TAG, "packageName " + appInfo.packageName + ", source dir " + appInfo.sourceDir);
        } else {
            new Thread("queued-work-looper-fast-compile") {
                /* class android.rms.iaware.AwareAppScheduleManager.AnonymousClass1 */

                public void run() {
                    try {
                        AwareLog.d(AwareAppScheduleManager.TAG, appInfo.packageName + ", Tread fast compile try begin");
                        Thread.currentThread();
                        Thread.sleep(5000);
                        int i = 25;
                        while (true) {
                            if (i <= 0) {
                                break;
                            }
                            AwareLog.d(AwareAppScheduleManager.TAG, "try " + ((25 - i) + 1) + ", Tread wait profile");
                            Thread.currentThread();
                            Thread.sleep(2000);
                            if (AwareAppScheduleManager.this.hasArt(appInfo)) {
                                AwareLog.d(AwareAppScheduleManager.TAG, "Tread artFile or art64File exists");
                                break;
                            } else if (AwareAppScheduleManager.this.hasValidProfile(appInfo)) {
                                AwareLog.i(AwareAppScheduleManager.TAG, "try " + ((25 - i) + 1) + ", has valid profile, begin to compile.");
                                AwareLog.i(AwareAppScheduleManager.TAG, "result " + ActivityThread.getPackageManager().performDexOptMode(appInfo.packageName, true, "speed-profile-opt", false, true, (String) null) + ", Thread fast-compile end");
                                break;
                            } else {
                                i--;
                            }
                        }
                    } catch (RemoteException e) {
                        AwareLog.w(AwareAppScheduleManager.TAG, "fast_compile_thread synchronized process failed!");
                    } catch (InterruptedException e2) {
                        AwareLog.w(AwareAppScheduleManager.TAG, "fast_compile_thread interrupted");
                    }
                    AwareLog.d(AwareAppScheduleManager.TAG, appInfo.packageName + ", Thread fast compile try end");
                }
            }.start();
        }
    }

    public void init(String processName, Application app) {
        if (processName != null && app != null) {
            this.mContext = app.getBaseContext();
            Context context = this.mContext;
            if (context != null) {
                this.mAppInfo = context.getApplicationInfo();
                ApplicationInfo applicationInfo = this.mAppInfo;
                if (applicationInfo == null || applicationInfo.uid < 10000) {
                    AwareLog.d(TAG, "special uid caller");
                } else if (!isSystemUnRemovablePkg(this.mAppInfo)) {
                    this.mPackageName = this.mContext.getPackageName();
                    if (!processName.equals(this.mPackageName)) {
                        AwareLog.d(TAG, "not main process, processName:" + processName + ", mPackageName:" + this.mPackageName);
                        this.mIsMainProcess = false;
                    }
                    if (this.mStatus.get() > 0) {
                        AwareLog.w(TAG, "has enabled");
                        return;
                    }
                    this.mStartTime = System.currentTimeMillis();
                    IAwareSdk.asyncReportDataWithCallback(3030, this.mPackageName, new AppScheduleSDKCallback(), this.mStartTime);
                    this.mAppVersionCode = this.mAppInfo.versionCode;
                    this.mPid = Process.myPid();
                }
            }
        }
    }

    private boolean isSystemUnRemovablePkg(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & 1) != 0 && (applicationInfo.hwFlags & 100663296) == 0;
    }

    public Drawable getCacheDrawableFromAware(int resId, Resources wrapper, int cookie, AssetManager asset) {
        Drawable dr;
        AtomicBoolean atomicBoolean = this.mDrawableCacheFeature;
        if (atomicBoolean == null || !atomicBoolean.get() || this.mResultShouldReplaced.get() || wrapper == null || this.mStatus.get() == 0 || this.mStatus.get() > 2 || this.mLoadDrawableResId.get() == resId) {
            return null;
        }
        synchronized (this.mDrawableCaches) {
            dr = this.mDrawableCaches.get(resId);
        }
        if (dr != null) {
            if (!Resources.class.getName().equals(wrapper.getClass().getName())) {
                AwareLog.d(TAG, "get cache drawable wrapper is not Resource, resId: " + resId);
                return null;
            } else if (!checkCookie(resId, cookie, asset)) {
                return null;
            } else {
                synchronized (this.mCacheDrawableResIds) {
                    if (!this.mCacheDrawableResIds.get(Integer.valueOf(resId)).booleanValue()) {
                        this.mCacheDrawableResIds.put(Integer.valueOf(resId), true);
                    }
                }
                AwareLog.i(TAG, "get cache drawable from aware success resId = " + resId + ", packagename = " + this.mPackageName);
            }
        }
        return dr;
    }

    private boolean checkCookie(int resId, int cookie, AssetManager asset) {
        String cookieName;
        if (asset == null || cookie == 0 || asset.getApkAssets() == null || asset.getApkAssets().length < cookie || (cookieName = asset.getApkAssets()[cookie - 1].getAssetPath()) == null) {
            return false;
        }
        if (cookieName.equals(this.mAppInfo.sourceDir) || cookieName.equals(FRAMEWORK_RES_RESOURCE)) {
            return true;
        }
        AwareLog.d(TAG, "cache drawable cookieName is not main package, resId: " + resId + ", cookieName:" + cookieName);
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00c0, code lost:
        if (r5.mHasNewResId.get() != false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00c2, code lost:
        r5.mHasNewResId.set(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        return;
     */
    public void postCacheDrawableToAware(int resId, Resources wrapper, long time, int cookie, AssetManager asset) {
        AtomicBoolean atomicBoolean = this.mDrawableCacheFeature;
        if (atomicBoolean != null && atomicBoolean.get() && time >= ((long) this.mDecodeTime) && this.mStatus.get() == 1 && wrapper != null) {
            if (Process.myTid() != this.mPid) {
                AwareLog.d(TAG, "postCacheDrawableToAware not UiThread");
            } else if (this.mLoadDrawableResId.get() != resId) {
                if (!Resources.class.getName().equals(wrapper.getClass().getName())) {
                    AwareLog.d(TAG, "post cache drawable wrapper is not Resource, resId: " + resId + ", wrapper:" + wrapper);
                } else if (checkCookie(resId, cookie, asset)) {
                    Log.i(TAG, "post cache drawable res id to aware, resId = " + resId + ", packagename = " + this.mPackageName + ", cost time = " + time);
                    synchronized (this.mCacheDrawableResIds) {
                        if (!this.mCacheDrawableResIds.containsKey(Integer.valueOf(resId))) {
                            this.mCacheDrawableResIds.put(Integer.valueOf(resId), true);
                        }
                    }
                }
            }
        }
    }

    public void hitDrawableCache(int resId) {
        AtomicBoolean atomicBoolean = this.mDrawableCacheFeature;
        if (atomicBoolean != null && atomicBoolean.get() && this.mStatus.get() != 0 && this.mStatus.get() <= 2) {
            synchronized (this.mCacheDrawableResIds) {
                if (this.mCacheDrawableResIds.containsKey(Integer.valueOf(resId))) {
                    AwareLog.d(TAG, "get cache drawable from cachedDrawable, resid: " + resId);
                    if (!this.mCacheDrawableResIds.get(Integer.valueOf(resId)).booleanValue()) {
                        this.mCacheDrawableResIds.put(Integer.valueOf(resId), true);
                    }
                }
            }
        }
    }

    public boolean getClickViewSpeedUpStatus() {
        return this.mClickViewSpeedUpSwitch;
    }

    public boolean getScanOpt() {
        boolean scanOpt = false;
        if (this.mCameraScanSwitch) {
            scanOpt = HwAppSceneImpl.getDefault().isMatchSceneId(ActivityThread.currentPackageName(), ActivityThread.currentActivityName(), CAMERA_SCAN_SCENE_ID, this.mCameraScanRecogVersion);
        }
        AwareLog.d(TAG, "scanOptEnable=" + this.mCameraScanSwitch + ",isScanOpt=" + scanOpt);
        return scanOpt;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0098, code lost:
        if (r1 != r4) goto L_0x00a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x009a, code lost:
        android.rms.iaware.AwareLog.d(android.rms.iaware.AwareAppScheduleManager.TAG, "hit rate is 0, clear learn result before and do not need to write");
        deleteFile(r7.mDecodeBitmapFileLock, android.rms.iaware.AwareAppScheduleManager.FILE_NAME_DECODEBITMAP);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00a8, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00a9, code lost:
        if (r1 != 0) goto L_0x00bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00b1, code lost:
        if (r7.mHasNewResId.get() != false) goto L_0x00bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00b3, code lost:
        android.rms.iaware.AwareLog.d(android.rms.iaware.AwareAppScheduleManager.TAG, "the learn result does not change, do not need to write");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00ba, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00bb, code lost:
        writeToFile(r7.mDecodeBitmapFileLock, android.rms.iaware.AwareAppScheduleManager.FILE_NAME_DECODEBITMAP, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00c2, code lost:
        return;
     */
    private void writeDrawable() {
        AwareLog.i(TAG, "begin write cache data to disk, packageName = " + this.mPackageName);
        int notHitCount = 0;
        synchronized (this.mCacheDrawableResIds) {
            int size = this.mCacheDrawableResIds.size();
            if (size >= 1) {
                if (size <= 20) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.mSystemVersion);
                    sb.append("\n");
                    sb.append(this.mAppVersionCode);
                    for (Map.Entry<Integer, Boolean> entry : this.mCacheDrawableResIds.entrySet()) {
                        if (entry.getValue().booleanValue()) {
                            sb.append("\n");
                            sb.append(entry.getKey());
                        } else {
                            notHitCount++;
                        }
                    }
                    AwareLog.d(TAG, "write cache resid to disk, packageName = " + this.mPackageName + ", line: " + this.mCacheDrawableResIds);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readFromDisk() {
        generateDrawable();
    }

    private void generateDrawable() {
        if (this.mDrawableCacheFeature.get()) {
            List<String> resIds = readFromFile(this.mDecodeBitmapFileLock, FILE_NAME_DECODEBITMAP, 256);
            AwareLog.d(TAG, "read cache resid from disk, packageName = " + this.mPackageName + ", line: " + resIds);
            int size = resIds.size();
            if (size < 3) {
                if (size != 0) {
                    AwareLog.w(TAG, "read cached resid size not right , its:" + size);
                }
                this.mResultShouldReplaced.set(true);
                deleteFile(this.mDecodeBitmapFileLock, FILE_NAME_DECODEBITMAP);
                return;
            }
            try {
                if (!this.mSystemVersion.equals(resIds.get(0))) {
                    AwareLog.w(TAG, "system version has changed");
                    this.mResultShouldReplaced.set(true);
                    deleteFile(this.mDecodeBitmapFileLock, FILE_NAME_DECODEBITMAP);
                } else if (Integer.parseInt(resIds.get(1)) != this.mAppVersionCode) {
                    AwareLog.d(TAG, this.mPackageName + " version has changed ");
                    this.mResultShouldReplaced.set(true);
                    deleteFile(this.mDecodeBitmapFileLock, FILE_NAME_DECODEBITMAP);
                } else {
                    int resId = 0;
                    int size2 = 22;
                    if (size <= 22) {
                        size2 = size;
                    }
                    for (int i = 2; i < size2; i++) {
                        try {
                            resId = Integer.parseInt(resIds.get(i));
                            synchronized (this.mCacheDrawableResIds) {
                                if (!this.mCacheDrawableResIds.containsKey(Integer.valueOf(resId))) {
                                    this.mCacheDrawableResIds.put(Integer.valueOf(resId), false);
                                }
                            }
                            this.mLoadDrawableResId.set(resId);
                            Drawable dr = this.mContext.getDrawable(resId);
                            this.mLoadDrawableResId.set(0);
                            if (dr != null) {
                                synchronized (this.mDrawableCaches) {
                                    this.mDrawableCaches.put(resId, dr);
                                }
                            } else {
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            AwareLog.w(TAG, "create drawable parse resId error:" + resId);
                            deleteFile(this.mDecodeBitmapFileLock, FILE_NAME_DECODEBITMAP);
                        } catch (Resources.NotFoundException e2) {
                            AwareLog.d(TAG, "create drawable not found error:" + resId);
                            deleteFile(this.mDecodeBitmapFileLock, FILE_NAME_DECODEBITMAP);
                        } catch (RuntimeException e3) {
                            AwareLog.d(TAG, "create drawable failed:" + resId);
                            deleteFile(this.mDecodeBitmapFileLock, FILE_NAME_DECODEBITMAP);
                            return;
                        }
                    }
                }
            } catch (NumberFormatException e4) {
                AwareLog.w(TAG, this.mPackageName + " versioncode parse error ");
                this.mResultShouldReplaced.set(true);
                deleteFile(this.mDecodeBitmapFileLock, FILE_NAME_DECODEBITMAP);
            }
        }
    }

    private List<String> readFromFile(Object lock, String filename, int maxsize) {
        List<String> resIds;
        synchronized (lock) {
            resIds = new AtomicFileUtils(new File(this.mCacheDir, filename)).readFileLines(maxsize);
        }
        return resIds;
    }

    private void writeToFile(Object lock, String filename, StringBuilder sb) {
        synchronized (lock) {
            new AtomicFileUtils(new File(this.mCacheDir, filename)).writeFileLine(sb);
        }
    }

    private void deleteFile(Object lock, String filename) {
        synchronized (lock) {
            new AtomicFileUtils(new File(this.mCacheDir, filename)).deleteFile();
        }
    }

    /* access modifiers changed from: private */
    public final class AppScheduleHandler extends Handler {
        public AppScheduleHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                AwareAppScheduleManager.this.readFromDisk();
                AwareAppScheduleManager.this.handleReadWebViewLearning();
            } else if (i == 2) {
                AwareAppScheduleManager.this.finishLearn();
            } else if (i == 3) {
                AwareAppScheduleManager.this.finishHandler();
            } else if (i == 4) {
                AwareAppScheduleManager.this.handleUpdateWebViewLearning();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finishHandler() {
        AwareLog.d(TAG, "finishHandler");
        this.mStatus.set(3);
        this.mHandler = null;
        writeDrawable();
        HandlerThread handlerThread = this.mHandlerThread;
        if (handlerThread != null) {
            handlerThread.quit();
            this.mHandlerThread = null;
        }
        synchronized (this.mDrawableCaches) {
            this.mDrawableCaches.clear();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finishLearn() {
        AwareLog.d(TAG, "finishLearn");
        this.mStatus.set(2);
    }

    private class AppScheduleSDKCallback extends Binder implements IInterface {
        private static final String SDK_CALLBACK_DESCRIPTOR = "android.rms.iaware.AppScheduleSDKCallback";
        private static final int TRANSACTION_initAppSchedulePolicy = 1;

        public AppScheduleSDKCallback() {
            attachInterface(this, SDK_CALLBACK_DESCRIPTOR);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (System.currentTimeMillis() - AwareAppScheduleManager.this.mStartTime > 5000) {
                AwareLog.w(AwareAppScheduleManager.TAG, "init policy is later!");
                return false;
            } else if (code != 1) {
                return super.onTransact(code, data, reply, flags);
            } else {
                try {
                    data.enforceInterface(SDK_CALLBACK_DESCRIPTOR);
                    int[] policy = new int[8];
                    data.readIntArray(policy);
                    AwareAppScheduleManager.this.mCameraScanRecogVersion = data.readInt();
                    if (policy.length != 8) {
                        AwareLog.e(AwareAppScheduleManager.TAG, "policy is error");
                        return false;
                    }
                    initAppSchedulePolicy(policy);
                    return true;
                } catch (SecurityException e) {
                    AwareLog.e(AwareAppScheduleManager.TAG, "enforceInterface SDK_CALLBACK_DESCRIPTOR failed");
                    return false;
                }
            }
        }

        public IBinder asBinder() {
            return this;
        }

        private void initAppSchedulePolicy(int[] policy) {
            boolean learningOpen = false;
            if (policy[0] == 0) {
                AwareAppScheduleManager.this.mStatus.set(0);
                AwareLog.d(AwareAppScheduleManager.TAG, "init: failed cause feature is disabled process:" + AwareAppScheduleManager.this.mPackageName);
                return;
            }
            AwareAppScheduleManager awareAppScheduleManager = AwareAppScheduleManager.this;
            awareAppScheduleManager.mCacheDir = awareAppScheduleManager.mContext.getFilesDir();
            AwareAppScheduleManager.this.mDrawableCacheFeature.set(AwareAppScheduleManager.this.mIsMainProcess && 1 == policy[1]);
            AwareAppScheduleManager.this.mDecodeTime = policy[2];
            AwareAppScheduleManager.this.mCameraScanSwitch = 1 == policy[3];
            AwareAppScheduleManager awareAppScheduleManager2 = AwareAppScheduleManager.this;
            awareAppScheduleManager2.mPreloadWebViewSwitch = awareAppScheduleManager2.mIsMainProcess && 1 == policy[4];
            AwareAppScheduleManager.this.mClickViewSpeedUpSwitch = 1 == policy[5];
            AwareAppScheduleManager.this.mStatus.set(1);
            String hwSystemVersion = SystemProperties.get("ro.huawei.build.version.incremental", BuildConfig.FLAVOR);
            AwareAppScheduleManager.this.mSystemVersion = hwSystemVersion.isEmpty() ? Build.VERSION.INCREMENTAL : hwSystemVersion;
            if (AwareAppScheduleManager.this.mDrawableCacheFeature.get() || AwareAppScheduleManager.this.mPreloadWebViewSwitch) {
                learningOpen = true;
            }
            if (learningOpen && AwareAppScheduleManager.this.mHandlerThread == null) {
                AwareAppScheduleManager.this.mHandlerThread = new HandlerThread("queued-work-looper-schedule-handler", 10);
                AwareAppScheduleManager.this.mHandlerThread.start();
                AwareAppScheduleManager awareAppScheduleManager3 = AwareAppScheduleManager.this;
                awareAppScheduleManager3.mHandler = new AppScheduleHandler(awareAppScheduleManager3.mHandlerThread.getLooper());
                AwareAppScheduleManager.this.mHandler.sendMessage(AwareAppScheduleManager.this.mHandler.obtainMessage(1));
                AwareAppScheduleManager.this.mHandler.sendMessageDelayed(AwareAppScheduleManager.this.mHandler.obtainMessage(2), 5000);
                AwareAppScheduleManager.this.mHandler.sendEmptyMessageDelayed(4, 15000);
                AwareAppScheduleManager.this.mHandler.sendMessageDelayed(AwareAppScheduleManager.this.mHandler.obtainMessage(3), 20000);
            }
            AwareAppScheduleManager awareAppScheduleManager4 = AwareAppScheduleManager.this;
            awareAppScheduleManager4.compileArt(awareAppScheduleManager4.mAppInfo);
            AwareLog.i(AwareAppScheduleManager.TAG, "init: success process:" + AwareAppScheduleManager.this.mPackageName);
        }
    }

    public void reportWebViewInit(Context context) {
        if (!this.mDisableGlInit) {
            if (!this.mPreloadWebViewSwitch) {
                this.mHadInitWebView = true;
                return;
            }
            if (!this.mHadInitWebView && this.mWillLoadUrl && context != null) {
                Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler(context) {
                    /* class android.rms.iaware.$$Lambda$AwareAppScheduleManager$PntR8acH0vxKV0oG8bcjPluhFBY */
                    private final /* synthetic */ Context f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final boolean queueIdle() {
                        return AwareAppScheduleManager.this.lambda$reportWebViewInit$0$AwareAppScheduleManager(this.f$1);
                    }
                });
            }
            this.mHadInitWebView = true;
        }
    }

    public /* synthetic */ boolean lambda$reportWebViewInit$0$AwareAppScheduleManager(Context context) {
        if (this.mHadLoadUrl) {
            return false;
        }
        Log.d(TAG, "webViewOpt, openGL init: " + this.mPackageName);
        initWebView(context);
        return false;
    }

    public void reportLoadUrl() {
        if (!this.mDisableGlInit) {
            this.mHadLoadUrl = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateWebViewLearning() {
        if (this.mPreloadWebViewSwitch && this.mContext != null && this.mHadLoadUrl && !this.mWillLoadUrl) {
            Log.d(TAG, "webViewOpt, app loadUrl, save data");
            try {
                JSONObject learningData = readLearningFile();
                if (learningData == null) {
                    learningData = new JSONObject();
                }
                learningData.put(SYSTEM_VERSION, this.mSystemVersion);
                learningData.put(APP_VERSION, this.mAppVersionCode);
                learningData.put(LOAD_URL, true);
                writeToFile(this.mLearningDataLock, LEARNING_DATA, new StringBuilder(learningData.toString()));
            } catch (JSONException e) {
                Log.d(TAG, "webViewOpt, JSONException");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReadWebViewLearning() {
        JSONObject learningData;
        if (this.mPreloadWebViewSwitch && (learningData = readLearningFile()) != null && learningData.length() != 0) {
            if (!learningData.optString(SYSTEM_VERSION, BuildConfig.FLAVOR).equals(this.mSystemVersion) || learningData.optInt(APP_VERSION, -1) != this.mAppVersionCode) {
                Log.d(TAG, "webViewOpt, system or app version update, clearData");
                deleteFile(this.mLearningDataLock, LEARNING_DATA);
                return;
            }
            this.mWillLoadUrl = learningData.optBoolean(LOAD_URL, false);
            if (this.mWillLoadUrl) {
                postNewWebViewToUi();
            }
        }
    }

    private boolean checkNewWebViewSafe() {
        try {
            WebView.setDataDirectorySuffix(BuildConfig.FLAVOR + File.separatorChar);
            Log.d(TAG, "webViewOpt, set success");
            return false;
        } catch (IllegalStateException e) {
            Log.d(TAG, "webViewOpt, now safe ");
            return true;
        } catch (IllegalArgumentException e2) {
            Log.d(TAG, "webViewOpt, not safe ");
            return false;
        }
    }

    private JSONObject readLearningFile() {
        List<String> data = readFromFile(this.mLearningDataLock, LEARNING_DATA, 256);
        if (data.size() <= 0) {
            return null;
        }
        try {
            return new JSONObject(data.get(0));
        } catch (JSONException e) {
            return null;
        }
    }

    private void postNewWebViewToUi() {
        Looper mainLooper;
        MessageQueue mainQueue;
        Context context = this.mContext;
        if (context != null && (mainLooper = context.getMainLooper()) != null && (mainQueue = mainLooper.getQueue()) != null) {
            mainQueue.addIdleHandler(new MessageQueue.IdleHandler() {
                /* class android.rms.iaware.$$Lambda$AwareAppScheduleManager$TKAQZenujDzuEaxFjlKyhFel3Ns */

                public final boolean queueIdle() {
                    return AwareAppScheduleManager.this.lambda$postNewWebViewToUi$1$AwareAppScheduleManager();
                }
            });
        }
    }

    public /* synthetic */ boolean lambda$postNewWebViewToUi$1$AwareAppScheduleManager() {
        if (!this.mHadInitWebView && checkNewWebViewSafe()) {
            this.mDisableGlInit = true;
            Log.d(TAG, "webViewOpt, webView init: " + this.mPackageName);
            initWebView(this.mContext);
            this.mDisableGlInit = false;
        }
        return false;
    }

    private void initWebView(Context context) {
        if (Process.myTid() == Process.myPid()) {
            new WebView(context).loadData(BuildConfig.FLAVOR, "text/html", "utf-8");
        }
    }
}
