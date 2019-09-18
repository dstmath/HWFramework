package android.common;

import android.app.Activity;
import android.app.Application;
import android.app.LoadedApk;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;

public interface HwActivityThread {
    void changeToSpecialModel(String str);

    boolean decodeBitmapOptEnable();

    boolean doReportRuntime(String str, long j);

    Drawable getCacheDrawableFromAware(int i, Resources resources, int i2, AssetManager assetManager);

    String getWechatScanActivity();

    boolean getWechatScanOpt();

    void hitDrawableCache(int i);

    boolean isLiteSysLoadEnable();

    int isPerfOptEnable(int i);

    boolean isScene(int i);

    void loadAppCyclePatternAsync(LoadedApk loadedApk, ApplicationInfo applicationInfo, String str);

    void postCacheDrawableToAware(int i, Resources resources, long j, int i2, AssetManager assetManager);

    void reportBindApplicationToAware(Application application, String str);

    void reportLoadUrl();

    void reportWebViewInit(Context context);

    void setNavigationBarColorFromActivityThread(Activity activity, Handler handler, Configuration configuration);
}
