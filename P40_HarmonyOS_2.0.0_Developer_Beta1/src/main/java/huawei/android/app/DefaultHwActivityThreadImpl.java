package huawei.android.app;

import android.app.Activity;
import android.app.Application;
import android.common.HwActivityThread;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DefaultHwActivityThreadImpl implements HwActivityThread {
    private static DefaultHwActivityThreadImpl sInstance;

    @HwSystemApi
    public static synchronized DefaultHwActivityThreadImpl getDefault() {
        DefaultHwActivityThreadImpl defaultHwActivityThreadImpl;
        synchronized (DefaultHwActivityThreadImpl.class) {
            if (sInstance == null) {
                sInstance = new DefaultHwActivityThreadImpl();
            }
            defaultHwActivityThreadImpl = sInstance;
        }
        return defaultHwActivityThreadImpl;
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public void changeToSpecialModel(String pkgName) {
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public int isPerfOptEnable(int optTypeId) {
        return 0;
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public boolean decodeBitmapOptEnable() {
        return false;
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public void reportBindApplicationToAware(Application app, String processName) {
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public Drawable getCacheDrawableFromAware(int resId, Resources wrapper, int cookie, AssetManager asset) {
        return null;
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public void postCacheDrawableToAware(int resId, Resources wrapper, long time, int cookie, AssetManager asset) {
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public void hitDrawableCache(int resId) {
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public boolean getScanOpt() {
        return false;
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public void setNavigationBarColorFromActivityThread(Activity activity, Handler handle, Configuration configuration) {
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public IntentFilter setFilterIdentifier(IntentFilter filter, BroadcastReceiver receiver, Context context, String basePackageName) {
        return null;
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public void reportWebViewInit(Context context) {
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public void reportLoadUrl() {
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public boolean initHwArgs(ActivityThreadAdapterEx thread, String[] args) {
        return false;
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public void loadAppCyclePatternAsync(AssetManager asset, ApplicationInfo appInfo, String processName) {
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public boolean doReportRuntime(String procName, long startTime) {
        return true;
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public void schedThreadToRtg(int tid, boolean enable) {
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public void setHwPreloadStatus(long preloadStatus) {
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public long getHwPreloadStatus() {
        return 0;
    }

    @Override // android.common.HwActivityThread
    @HwSystemApi
    public void handleHwPreloadStatus(int opType) {
    }
}
