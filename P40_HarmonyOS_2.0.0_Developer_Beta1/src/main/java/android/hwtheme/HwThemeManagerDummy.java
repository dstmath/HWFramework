package android.hwtheme;

import android.app.ActivityManager;
import android.app.Notification;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageParserEx;
import android.content.pm.ResolveInfo;
import android.content.res.AbsResources;
import android.content.res.AbsResourcesImpl;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.IHwConfiguration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.DisplayAdjustments;
import com.android.internal.R;
import java.io.InputStream;

public class HwThemeManagerDummy implements HwThemeManager.IHwThemeManager {
    private static final int STYLE_DATA = 1;

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public boolean isTRingtones(String path) {
        return false;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public boolean isTNotifications(String path) {
        return false;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public boolean isTAlarms(String path) {
        return false;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public void updateConfiguration() {
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public void updateConfiguration(boolean isChangeuser) {
    }

    public Resources getResources(AssetManager assets, DisplayMetrics dm, Configuration config, DisplayAdjustments displayAdjustments, IBinder token) {
        return null;
    }

    public Resources getResources() {
        return null;
    }

    public Resources getResources(boolean isPreloading) {
        return null;
    }

    public Resources getResources(ClassLoader classLoader) {
        return null;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public AbsResourcesImpl getHwResourcesImpl() {
        return null;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public int getHwThemeLauncherIconSize(ActivityManager am, Resources resources) {
        return am.getLauncherLargeIconSize();
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public int getThemeColor(int[] data, int index, TypedValue value, Resources resources, boolean flag) {
        return data[index + 1];
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public void updateIconCache(PackageItemInfo packageItemInfo, String name, String packageName, int icon, int packageIcon) {
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public void removeIconCache(String name, String packageName, int icon, int packageIcon) {
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public void restoreIconCache(String packageName, int icon) {
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public void updateResolveInfoIconCache(ResolveInfo resolveInfo, int icon, String resolvePackageName) {
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public InputStream getDefaultWallpaperIS(Context ctx, int userId) {
        return ctx.getResources().openRawResource(R.drawable.default_wallpaper);
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public boolean installDefaultHwTheme(Context ctx) {
        return false;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public boolean installDefaultHwTheme(Context ctx, int userId) {
        return false;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public boolean installHwTheme(String themePath) {
        return false;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public boolean installHwTheme(String themePath, boolean isSetwallpaper) {
        return false;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public boolean installHwTheme(String themePath, boolean isSetwallpaper, int userId) {
        return false;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public boolean makeIconCache(boolean isClearall) {
        return true;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public boolean saveIconToCache(Bitmap bitmap, String fn, boolean isClearold) {
        return true;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public int getShadowcolor(TypedArray ta, int attr) {
        return 0;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public void setThemeFont() {
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public boolean setThemeFontOnConfigChg(Configuration newConfig) {
        return false;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public boolean shouldUseAdditionalChnFont(String familyName) {
        return false;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public boolean isTargetFamily(String familyName) {
        return false;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public void applyDefaultHwTheme(boolean isCheckState, Context ctx) {
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public void applyDefaultHwTheme(boolean isCheckState, Context ctx, int userId) {
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public void addSimpleUIConfig(PackageParserEx.ActivityEx activity) {
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public void retrieveSimpleUIConfig(ContentResolver cr, Configuration config, int userId) {
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public void updateSimpleUIConfig(ContentResolver cr, Configuration config, int configChanges) {
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public Bitmap getThemeBitmap(Resources res, int id, Rect padding) {
        return null;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public void initForThemeFont(Configuration config) {
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public Bitmap generateBitmap(Context context, Bitmap bm, int width, int height) {
        return bm;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public Drawable getJoinBitmap(Context context, Drawable srcDraw, int backgroudId) {
        return null;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public Drawable getClonedDrawable(Context context, Drawable drawable) {
        return drawable;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public Drawable getHwBadgeDrawable(Notification notification, Context context, Drawable drawable) {
        return null;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public String getDefaultLiveWallpaper(int userId) {
        return null;
    }

    @Override // android.hwtheme.HwThemeManager.IHwThemeManager
    public AbsResources getHwResources() {
        return null;
    }

    public IHwConfiguration getHwConfiguration() {
        return null;
    }
}
