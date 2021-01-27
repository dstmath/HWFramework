package android.common;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import huawei.android.app.ActivityThreadAdapterEx;

public interface HwActivityThread {
    void changeToSpecialModel(String str);

    boolean decodeBitmapOptEnable();

    boolean doReportRuntime(String str, long j);

    Drawable getCacheDrawableFromAware(int i, Resources resources, int i2, AssetManager assetManager);

    long getHwPreloadStatus();

    boolean getScanOpt();

    void handleHwPreloadStatus(int i);

    void hitDrawableCache(int i);

    boolean initHwArgs(ActivityThreadAdapterEx activityThreadAdapterEx, String[] strArr);

    int isPerfOptEnable(int i);

    void loadAppCyclePatternAsync(AssetManager assetManager, ApplicationInfo applicationInfo, String str);

    void postCacheDrawableToAware(int i, Resources resources, long j, int i2, AssetManager assetManager);

    void reportBindApplicationToAware(Application application, String str);

    void reportLoadUrl();

    void reportWebViewInit(Context context);

    void schedThreadToRtg(int i, boolean z);

    IntentFilter setFilterIdentifier(IntentFilter intentFilter, BroadcastReceiver broadcastReceiver, Context context, String str);

    void setHwPreloadStatus(long j);

    void setNavigationBarColorFromActivityThread(Activity activity, Handler handler, Configuration configuration);
}
