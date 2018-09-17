package android.hwtheme;

import android.app.ActivityManager;
import android.app.Notification;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageParser.Activity;
import android.content.pm.ResolveInfo;
import android.content.res.AbsResourcesImpl;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.HwConfigurationDummy;
import android.content.res.IHwConfiguration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager.IHwThemeManager;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.DisplayAdjustments;
import java.io.InputStream;

public class HwThemeManagerDummy implements IHwThemeManager {
    private static final int STYLE_DATA = 1;

    public boolean isTRingtones(String path) {
        return false;
    }

    public boolean isTNotifications(String path) {
        return false;
    }

    public boolean isTAlarms(String path) {
        return false;
    }

    public void updateConfiguration() {
    }

    public Resources getResources(AssetManager assets, DisplayMetrics dm, Configuration config, DisplayAdjustments displayAdjustments, IBinder token) {
        return null;
    }

    public Resources getResources() {
        return null;
    }

    public Resources getResources(boolean preloading) {
        return null;
    }

    public Resources getResources(ClassLoader classLoader) {
        return null;
    }

    public AbsResourcesImpl getHwResourcesImpl() {
        return null;
    }

    public int getHwThemeLauncherIconSize(ActivityManager am, Resources resources) {
        return am.getLauncherLargeIconSize();
    }

    public int getThemeColor(int[] data, int index, TypedValue value, Resources resources, boolean flag) {
        return data[index + 1];
    }

    public void updateIconCache(PackageItemInfo packageItemInfo, String name, String packageName, int icon, int packageIcon) {
    }

    public void removeIconCache(String name, String packageName, int icon, int packageIcon) {
    }

    public void restoreIconCache(String packageName, int icon) {
    }

    public void updateResolveInfoIconCache(ResolveInfo resolveInfo, int icon, String resolvePackageName) {
    }

    public InputStream getDefaultWallpaperIS(Context ctx, int userId) {
        return ctx.getResources().openRawResource(17302115);
    }

    public boolean installDefaultHwTheme(Context ctx) {
        return false;
    }

    public boolean installDefaultHwTheme(Context ctx, int userId) {
        return false;
    }

    public boolean installHwTheme(String themePath) {
        return false;
    }

    public boolean installHwTheme(String themePath, boolean setwallpaper) {
        return false;
    }

    public boolean installHwTheme(String themePath, boolean setwallpaper, int userId) {
        return false;
    }

    public boolean makeIconCache(boolean clearall) {
        return true;
    }

    public boolean saveIconToCache(Bitmap bitmap, String fn, boolean clearold) {
        return true;
    }

    public IHwConfiguration initHwConfiguration() {
        return new HwConfigurationDummy();
    }

    public int getShadowcolor(TypedArray a, int attr) {
        return 0;
    }

    public void setThemeFont() {
    }

    public boolean setThemeFontOnConfigChg(Configuration newConfig) {
        return false;
    }

    public boolean shouldUseAdditionalChnFont(String familyName) {
        return false;
    }

    public boolean isTargetFamily(String familyName) {
        return false;
    }

    public void applyDefaultHwTheme(boolean checkState, Context ctx) {
    }

    public void applyDefaultHwTheme(boolean checkState, Context ctx, int userId) {
    }

    public void addSimpleUIConfig(Activity activity) {
    }

    public void retrieveSimpleUIConfig(ContentResolver cr, Configuration config, int userId) {
    }

    public void updateSimpleUIConfig(ContentResolver cr, Configuration config, int configChanges) {
    }

    public Bitmap getThemeBitmap(Resources res, int id) {
        return null;
    }

    public Bitmap getThemeBitmap(Resources res, int id, Rect padding) {
        return null;
    }

    public void initForThemeFont(Configuration config) {
    }

    public Bitmap generateBitmap(Context context, Bitmap bm, int width, int height) {
        return bm;
    }

    public Drawable getJoinBitmap(Context context, Drawable srcDraw, int backgroudId) {
        return null;
    }

    public Drawable getClonedDrawable(Context context, Drawable drawable) {
        return drawable;
    }

    public Drawable getHwBadgeDrawable(Notification notification, Context context, Drawable drawable) {
        return null;
    }

    public void updateConfiguration(boolean changeuser) {
    }

    public String getDefaultLiveWallpaper(int userId) {
        return null;
    }
}
