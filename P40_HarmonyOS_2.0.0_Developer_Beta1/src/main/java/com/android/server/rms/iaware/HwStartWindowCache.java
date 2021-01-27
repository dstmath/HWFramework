package com.android.server.rms.iaware;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.drawable.LayerDrawableEx;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import android.view.View;
import android.view.WindowManager;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.huawei.android.os.HandlerEx;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HwStartWindowCache {
    private static final long DELAY_UPDATE_TOPN = 2000;
    private static final Object LOCK = new Object();
    private static final long MINUTE_TO_MILLISECONDS = 60000;
    private static final int MSG_CLEAR_ALL_CACHE = 2;
    private static final int MSG_UPDATE_TOPN = 1;
    private static final String TAG = "HwStartWindowCache";
    private static volatile HwStartWindowCache sInstance = null;
    private long mCacheSum = 0;
    private boolean mFeatureSwitch = false;
    private Handler mHandler;
    private long mInvalidTimeMs = 0;
    private int mLastNightMode = 0;
    private final ArraySet<String> mMostFreqApk = new ArraySet<>();
    private final HashMap<String, StartWindowInfo> mStartWindowCache = new HashMap<>();
    private int mTopN = 0;

    /* access modifiers changed from: private */
    public class StartWindowInfo {
        protected long mCacheSize;
        protected WindowManager.LayoutParams mParams;
        protected long mUpdateTime = SystemClock.elapsedRealtime();
        protected View mView;

        public StartWindowInfo(View startView, WindowManager.LayoutParams params, long cacheSize) {
            this.mView = startView;
            this.mParams = params;
            this.mCacheSize = cacheSize;
        }

        public void update() {
            this.mUpdateTime = SystemClock.elapsedRealtime();
            AwareLog.d(HwStartWindowCache.TAG, "cache update time");
        }

        public boolean isInvalid() {
            if (HwStartWindowCache.this.mInvalidTimeMs > 0 && SystemClock.elapsedRealtime() - this.mUpdateTime >= HwStartWindowCache.this.mInvalidTimeMs) {
                return true;
            }
            return false;
        }

        public long getCacheSize() {
            return this.mCacheSize;
        }
    }

    private class StartWindowCacheHandler extends HandlerEx {
        public StartWindowCacheHandler(Looper looper) {
            super(looper, (Handler.Callback) null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                HwStartWindowCache.this.updateTopN();
            } else if (i == 2) {
                HwStartWindowCache.this.clearAllCache();
            }
        }
    }

    private HwStartWindowCache() {
    }

    public static HwStartWindowCache getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new HwStartWindowCache();
                }
            }
        }
        return sInstance;
    }

    private boolean isTopNumApk(String pkg) {
        boolean isTopN;
        synchronized (this.mMostFreqApk) {
            isTopN = this.mMostFreqApk.contains(pkg);
        }
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.sendEmptyMessageDelayed(1, DELAY_UPDATE_TOPN);
        }
        return isTopN;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTopN() {
        List<String> mostFreqPkg;
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit != null && (mostFreqPkg = habit.getMostFrequentUsedApp(this.mTopN, 0)) != null) {
            synchronized (this.mMostFreqApk) {
                this.mMostFreqApk.clear();
                this.mMostFreqApk.addAll(mostFreqPkg);
            }
            removeColdCache();
        }
    }

    private void removeColdCache() {
        synchronized (this.mStartWindowCache) {
            Iterator<Map.Entry<String, StartWindowInfo>> iter = this.mStartWindowCache.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, StartWindowInfo> entry = iter.next();
                String pkgName = entry.getKey();
                StartWindowInfo value = entry.getValue();
                long cacheSize = value.getCacheSize();
                if (value.isInvalid()) {
                    this.mCacheSum -= cacheSize;
                    iter.remove();
                    AwareLog.d(TAG, "remove " + pkgName + " (out of time)");
                } else if (!this.mMostFreqApk.contains(pkgName)) {
                    this.mCacheSum -= cacheSize;
                    iter.remove();
                    AwareLog.d(TAG, "remove " + pkgName + " (not topN)");
                }
            }
            AwareLog.d(TAG, "cache updated = " + this.mStartWindowCache);
        }
    }

    public View tryAddViewFromCache(String packageName, IBinder appToken, Configuration config) {
        StartWindowInfo info;
        if (!this.mFeatureSwitch || config == null || !isHomeStatus()) {
            return null;
        }
        if (this.mLastNightMode != config.uiMode) {
            clearAllCache();
            this.mLastNightMode = config.uiMode;
            return null;
        }
        synchronized (this.mStartWindowCache) {
            info = this.mStartWindowCache.get(packageName);
        }
        if (info == null) {
            return null;
        }
        Context context = info.mView.getContext();
        if (context == null) {
            AwareLog.d(TAG, "context == null, do not addView for " + packageName);
            return null;
        }
        WindowManager wm = (WindowManager) context.getSystemService(WindowManager.class);
        if (wm == null) {
            AwareLog.d(TAG, "window manager == null, do not addView for " + packageName);
            return null;
        }
        info.update();
        info.mParams.token = appToken;
        wm.addView(info.mView, info.mParams);
        return info.mView;
    }

    public void putViewToCache(String packageName, View startView, WindowManager.LayoutParams params) {
        if (this.mFeatureSwitch && startView != null && params != null && isHomeStatus() && isTopNumApk(packageName)) {
            long cacheSize = (long) getStartViewCacheSize(startView);
            long totalSize = this.mCacheSum + cacheSize;
            if ((cacheSize >> 20) > 25 || (totalSize >> 20) > 150) {
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(TAG, "drop cache: " + packageName + " cacheSize: " + cacheSize + " totalSize: " + totalSize);
                }
            } else if (isSingleLaunchPackage(packageName, startView)) {
                synchronized (this.mStartWindowCache) {
                    this.mCacheSum += cacheSize;
                    this.mStartWindowCache.put(packageName, new StartWindowInfo(startView, params, cacheSize));
                }
            }
        }
    }

    public void clearCacheWhenUninstall(String packageName) {
        if (this.mFeatureSwitch && packageName != null) {
            synchronized (this.mStartWindowCache) {
                StartWindowInfo cacheInfo = this.mStartWindowCache.remove(packageName);
                if (cacheInfo != null) {
                    this.mCacheSum -= cacheInfo.getCacheSize();
                }
            }
            AwareLog.d(TAG, "remove " + packageName + "(uninstall)");
        }
    }

    public void init(int topN, int invalidTimeMinute) {
        if (topN > 0) {
            this.mTopN = topN;
            if (invalidTimeMinute > 0) {
                this.mInvalidTimeMs = ((long) invalidTimeMinute) * 60000;
            }
            this.mFeatureSwitch = true;
            AwareLog.d(TAG, "Feature enabled ! topN:" + topN + ", invalidTimeMs:" + this.mInvalidTimeMs);
        }
    }

    public void deinit() {
        this.mTopN = 0;
        this.mFeatureSwitch = false;
        AwareLog.d(TAG, "Feature disabled !");
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [android.os.Handler, com.android.server.rms.iaware.HwStartWindowCache$StartWindowCacheHandler] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void setHandler(Handler handler) {
        if (handler == null) {
            AwareLog.w(TAG, "handler from rms is null , cache not working!");
        } else {
            this.mHandler = new StartWindowCacheHandler(handler.getLooper());
        }
    }

    public void notifyMemCritical() {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.sendEmptyMessage(2);
            AwareLog.d(TAG, "mem critical clean all cache !");
        }
    }

    public void notifyResolutionChange() {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.sendEmptyMessage(2);
            AwareLog.d(TAG, "resolution change clean all cache !");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearAllCache() {
        this.mCacheSum = 0;
        synchronized (this.mStartWindowCache) {
            this.mStartWindowCache.clear();
        }
    }

    private boolean isHomeStatus() {
        return AwareAppAssociate.getInstance().isForeGroundApp(AwareAppAssociate.getInstance().getCurHomeProcessUid());
    }

    private int getStartViewCacheSize(View cacheView) {
        if (cacheView == null) {
            return 0;
        }
        try {
            return new LayerDrawableEx().getCacheSize(cacheView.getBackground());
        } catch (RuntimeException e) {
            AwareLog.d(TAG, "getStartViewCacheSize RuntimeException");
            return 0;
        }
    }

    public String dump() {
        int count;
        StringBuilder sb = new StringBuilder();
        sb.append("----StartWindowCache Meminfo--------------[in bytes]");
        sb.append(System.lineSeparator());
        long totalSize = 0;
        synchronized (this.mStartWindowCache) {
            count = this.mStartWindowCache.size();
            try {
                for (Map.Entry<String, StartWindowInfo> entry : this.mStartWindowCache.entrySet()) {
                    sb.append("viewName:" + entry.getKey());
                    sb.append(System.lineSeparator());
                    StartWindowInfo info = entry.getValue();
                    if (!(info == null || info.mView == null)) {
                        if (info.mView.getBackground() != null) {
                            int cacheSize = new LayerDrawableEx().getCacheSize(info.mView.getBackground());
                            totalSize += (long) cacheSize;
                            sb.append("cacheSize:" + cacheSize);
                            sb.append(System.lineSeparator());
                        }
                    }
                }
            } catch (RuntimeException e) {
                AwareLog.d(TAG, "dump RuntimeException");
            }
        }
        sb.append("TotalSize: " + totalSize);
        sb.append(System.lineSeparator());
        sb.append("SumCache: " + this.mCacheSum);
        sb.append(System.lineSeparator());
        sb.append("-------------------------------------cache-hit----" + count);
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    private boolean isSingleLaunchPackage(String packageName, View startView) {
        PackageManager packageManager;
        Context context = startView.getContext();
        if (context == null || (packageManager = context.getPackageManager()) == null) {
            return false;
        }
        Intent intentToResolve = new Intent("android.intent.action.MAIN");
        intentToResolve.addCategory("android.intent.category.INFO");
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = packageManager.queryIntentActivities(intentToResolve, 0);
        if (ris == null || ris.size() <= 0) {
            intentToResolve.removeCategory("android.intent.category.INFO");
            intentToResolve.addCategory("android.intent.category.LAUNCHER");
            ris = packageManager.queryIntentActivities(intentToResolve, 0);
        }
        if (ris == null || ris.size() > 1) {
            return false;
        }
        return true;
    }
}
