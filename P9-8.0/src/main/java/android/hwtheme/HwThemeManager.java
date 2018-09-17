package android.hwtheme;

import android.app.ActivityManager;
import android.app.Notification;
import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageParser.Activity;
import android.content.pm.ResolveInfo;
import android.content.res.AbsResourcesImpl;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.IHwConfiguration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ProxyInfo;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.DisplayAdjustments;
import com.huawei.android.hwutil.CommandLineUtil;
import java.io.InputStream;

public class HwThemeManager {
    public static final String DARK_TAG = "dark";
    public static final String DEFAULT_WALLPAPER = "unlock_wallpaper_0.jpg";
    public static final String DIR_UNLOCK = "/data/skin/unlock/";
    public static final String DIR_WALLPAPER = "/data/skin/wallpaper/";
    public static final int HAS_DIALER_IMAGE = 1;
    public static final String HONOR_TAG = "honor";
    public static final String HWT_FPATH_NEW_KEYGUARD = "/data/skin/com.android.keyguard";
    public static final String HWT_FPATH_OLD_KEYGUARD = "/data/skin/com.huawei.android.hwlockscreen";
    public static final String HWT_FPATH_SYSWP = "/data/system/users/0/wallpaper";
    public static final String HWT_GROUP_MEDIARW = "media_rw";
    public static final String HWT_MODE_ALL = "0775";
    public static final String HWT_NEW_KEYGUARD = "com.android.keyguard";
    public static final String HWT_OLD_KEYGUARD = "com.huawei.android.hwlockscreen";
    public static final String HWT_PATH_COLOR_BLACK = "/system/themes/Taste.hwt";
    public static final String HWT_PATH_COLOR_PINK = "/system/themes/Elegant.hwt";
    public static final String HWT_PATH_COLOR_WHITE = "/system/themes/Pure.hwt";
    public static final String HWT_PATH_ICONCACHE = "/data/skin/iconcache/";
    public static final String HWT_PATH_SKIN = (Environment.getDataDirectory() + "/skin");
    public static final String HWT_PATH_SKINALL = "/data/skin*";
    public static final String HWT_PATH_SKIN_DEC = "/data/skin/description.xml";
    public static final String HWT_PATH_SKIN_INSTALL_FLAG = (Environment.getDataDirectory() + "/skin_if");
    public static final String HWT_PATH_SYSWP = "/data/system/users/0/";
    public static final String HWT_PATH_TEMP_SKIN = "/data/skin.tmp";
    public static final String HWT_PATH_TEMP_SKIN_L1ALL = "/data/skin.tmp/*";
    public static final String HWT_PATH_TEMP_SKIN_L2ALL = "/data/skin.tmp/**/*";
    public static final String HWT_PATH_THEME = (Environment.getDataDirectory() + "/themes");
    public static final String HWT_USER_ROOT = "root";
    public static final String HWT_USER_SYSTEM = "system";
    public static final boolean IS_THEME_INSULATE = SystemProperties.getBoolean("ro.config.hw_themeInsulate", false);
    public static final String OVERLAY_THEME = "persist.deep.theme_";
    public static final String PRODUCT_BRAND = SystemProperties.get("ro.product.brand");
    public static final String SYS_WALLPAPER = "wallpaper";
    private static final String TAG = HwThemeManager.class.getSimpleName();
    public static final String TAG_ITEM = "item";
    public static final String TAG_LAYOUT = "layout";
    public static final String TAG_STYLE = "style";
    public static final String TAG_WALLPAPER = "wallpaper";
    private static String mOverLayThemePath = (OVERLAY_THEME + UserHandle.myUserId());
    private static String mOverLayThemeType = ProxyInfo.LOCAL_EXCL_LIST;
    private static IHwThemeManager sInstance = null;

    public interface IHwThemeManager {
        void addSimpleUIConfig(Activity activity);

        void applyDefaultHwTheme(boolean z, Context context);

        void applyDefaultHwTheme(boolean z, Context context, int i);

        Bitmap generateBitmap(Context context, Bitmap bitmap, int i, int i2);

        Drawable getClonedDrawable(Context context, Drawable drawable);

        String getDefaultLiveWallpaper(int i);

        InputStream getDefaultWallpaperIS(Context context, int i);

        Drawable getHwBadgeDrawable(Notification notification, Context context, Drawable drawable);

        AbsResourcesImpl getHwResourcesImpl();

        int getHwThemeLauncherIconSize(ActivityManager activityManager, Resources resources);

        Drawable getJoinBitmap(Context context, Drawable drawable, int i);

        Resources getResources();

        Resources getResources(AssetManager assetManager, DisplayMetrics displayMetrics, Configuration configuration, DisplayAdjustments displayAdjustments, IBinder iBinder);

        Resources getResources(ClassLoader classLoader);

        Resources getResources(boolean z);

        int getShadowcolor(TypedArray typedArray, int i);

        Bitmap getThemeBitmap(Resources resources, int i);

        Bitmap getThemeBitmap(Resources resources, int i, Rect rect);

        int getThemeColor(int[] iArr, int i, TypedValue typedValue, Resources resources, boolean z);

        void initForThemeFont(Configuration configuration);

        IHwConfiguration initHwConfiguration();

        boolean installDefaultHwTheme(Context context);

        boolean installDefaultHwTheme(Context context, int i);

        boolean installHwTheme(String str);

        boolean installHwTheme(String str, boolean z);

        boolean installHwTheme(String str, boolean z, int i);

        boolean isTAlarms(String str);

        boolean isTNotifications(String str);

        boolean isTRingtones(String str);

        boolean isTargetFamily(String str);

        boolean makeIconCache(boolean z);

        void removeIconCache(String str, String str2, int i, int i2);

        void restoreIconCache(String str, int i);

        void retrieveSimpleUIConfig(ContentResolver contentResolver, Configuration configuration, int i);

        boolean saveIconToCache(Bitmap bitmap, String str, boolean z);

        void setThemeFont();

        boolean setThemeFontOnConfigChg(Configuration configuration);

        boolean shouldUseAdditionalChnFont(String str);

        void updateConfiguration();

        void updateConfiguration(boolean z);

        void updateIconCache(PackageItemInfo packageItemInfo, String str, String str2, int i, int i2);

        void updateResolveInfoIconCache(ResolveInfo resolveInfo, int i, String str);

        void updateSimpleUIConfig(ContentResolver contentResolver, Configuration configuration, int i);
    }

    public static class IconType {
        public static final int ACTIVITY = 1;
        public static final int APPLICATION = 0;
    }

    private static IHwThemeManager getImplObject() {
        if (sInstance != null) {
            return sInstance;
        }
        IHwThemeManager instance = null;
        IHwThemeManagerFactory obj = HwFrameworkFactory.getHwThemeManagerFactory();
        if (obj != null) {
            instance = obj.getThemeManagerInstance();
        }
        if (instance != null) {
            sInstance = instance;
        } else {
            Log.w(TAG, "can't get impl object from vendor, use default implemention");
            sInstance = new HwThemeManagerDummy();
        }
        return sInstance;
    }

    public static String getDefaultLiveWallpaper(int userId) {
        return getImplObject().getDefaultLiveWallpaper(userId);
    }

    public static boolean isTRingtones(String path) {
        return getImplObject().isTRingtones(path);
    }

    public static boolean isTNotifications(String path) {
        return getImplObject().isTNotifications(path);
    }

    public static boolean isTAlarms(String path) {
        return getImplObject().isTAlarms(path);
    }

    public static void updateConfiguration() {
        getImplObject().updateConfiguration();
    }

    public static Resources getResources(AssetManager assets, DisplayMetrics dm, Configuration config, DisplayAdjustments displayAdjustments, IBinder token) {
        return getImplObject().getResources(assets, dm, config, displayAdjustments, token);
    }

    public static Resources getResources() {
        return getImplObject().getResources();
    }

    public static Resources getResources(boolean preloading) {
        return getImplObject().getResources(preloading);
    }

    public static Resources getResources(ClassLoader classLoader) {
        return getImplObject().getResources(classLoader);
    }

    public static AbsResourcesImpl getHwResourcesImpl() {
        return getImplObject().getHwResourcesImpl();
    }

    public static int getHwThemeLauncherIconSize(ActivityManager am, Resources resources) {
        return getImplObject().getHwThemeLauncherIconSize(am, resources);
    }

    public static int getThemeColor(int[] data, int index, TypedValue value, Resources resources, boolean flag) {
        return getImplObject().getThemeColor(data, index, value, resources, flag);
    }

    public static void updateIconCache(PackageItemInfo packageItemInfo, String name, String packageName, int icon, int packageIcon) {
        getImplObject().updateIconCache(packageItemInfo, name, packageName, icon, packageIcon);
    }

    public static void updateResolveInfoIconCache(ResolveInfo resolveInfo, int icon, String resolvePackageName) {
        getImplObject().updateResolveInfoIconCache(resolveInfo, icon, resolvePackageName);
    }

    public static void removeIconCache(String name, String packageName, int icon, int packageIcon) {
        getImplObject().removeIconCache(name, packageName, icon, packageIcon);
    }

    public static void restoreIconCache(String packageName, int icon) {
        getImplObject().restoreIconCache(packageName, icon);
    }

    public static InputStream getDefaultWallpaperIS(Context ctx, int userId) {
        return getImplObject().getDefaultWallpaperIS(ctx, userId);
    }

    public static boolean installDefaultHwTheme(Context ctx) {
        return getImplObject().installDefaultHwTheme(ctx);
    }

    public static boolean installDefaultHwTheme(Context ctx, int userId) {
        return getImplObject().installDefaultHwTheme(ctx, userId);
    }

    public static boolean installHwTheme(String themePath) {
        return getImplObject().installHwTheme(themePath);
    }

    public static boolean installHwTheme(String themePath, boolean setwallpaper) {
        return getImplObject().installHwTheme(themePath, setwallpaper);
    }

    public static boolean installHwTheme(String themePath, boolean setwallpaper, int userId) {
        return getImplObject().installHwTheme(themePath, setwallpaper, userId);
    }

    public static boolean makeIconCache(boolean clearall) {
        return getImplObject().makeIconCache(clearall);
    }

    public static boolean saveIconToCache(Bitmap bitmap, String fn, boolean clearold) {
        return getImplObject().saveIconToCache(bitmap, fn, clearold);
    }

    public static IHwConfiguration initHwConfiguration() {
        return getImplObject().initHwConfiguration();
    }

    public static int getShadowcolor(TypedArray a, int attr) {
        return getImplObject().getShadowcolor(a, attr);
    }

    public static void setThemeFont() {
        getImplObject().setThemeFont();
    }

    public static boolean setThemeFontOnConfigChg(Configuration newConfig) {
        return getImplObject().setThemeFontOnConfigChg(newConfig);
    }

    public static boolean shouldUseAdditionalChnFont(String familyName) {
        return getImplObject().shouldUseAdditionalChnFont(familyName);
    }

    public static boolean isTargetFamily(String familyName) {
        return getImplObject().isTargetFamily(familyName);
    }

    public static void applyDefaultHwTheme(boolean checkState, Context ctx) {
        getImplObject().applyDefaultHwTheme(checkState, ctx);
    }

    public static void applyDefaultHwTheme(boolean checkState, Context ctx, int userId) {
        getImplObject().applyDefaultHwTheme(checkState, ctx, userId);
    }

    public static void addSimpleUIConfig(Activity activity) {
        getImplObject().addSimpleUIConfig(activity);
    }

    public static void retrieveSimpleUIConfig(ContentResolver cr, Configuration config, int userId) {
        getImplObject().retrieveSimpleUIConfig(cr, config, userId);
    }

    public static void updateSimpleUIConfig(ContentResolver cr, Configuration config, int configChanges) {
        getImplObject().updateSimpleUIConfig(cr, config, configChanges);
    }

    public static Bitmap getThemeBitmap(Resources res, int id) {
        return getImplObject().getThemeBitmap(res, id);
    }

    public static Bitmap getThemeBitmap(Resources res, int id, Rect padding) {
        return getImplObject().getThemeBitmap(res, id, padding);
    }

    public static void initForThemeFont(Configuration config) {
        getImplObject().initForThemeFont(config);
    }

    public static Bitmap generateBitmap(Context context, Bitmap bm, int width, int height) {
        return getImplObject().generateBitmap(context, bm, width, height);
    }

    public static void linkDataSkinDirAsUser(int userId) {
        CommandLineUtil.unlink(HWT_PATH_SKIN);
        CommandLineUtil.link("system", HWT_PATH_THEME + "/" + userId, HWT_PATH_SKIN);
    }

    public static Drawable getJoinBitmap(Context context, Drawable srcDraw, int backgroudId) {
        return getImplObject().getJoinBitmap(context, srcDraw, backgroudId);
    }

    public static Drawable getClonedDrawable(Context context, Drawable drawable) {
        return getImplObject().getClonedDrawable(context, drawable);
    }

    public static Drawable getHwBadgeDrawable(Notification notification, Context context, Drawable drawable) {
        return getImplObject().getHwBadgeDrawable(notification, context, drawable);
    }

    public static void updateConfiguration(boolean changeuser) {
        getImplObject().updateConfiguration(changeuser);
    }

    public static void setOverLayThemePath(String overLayThemePath) {
        synchronized (HwThemeManager.class) {
            mOverLayThemePath = overLayThemePath;
        }
    }

    public static void setOverLayThemeType(String overLayThemeType) {
        synchronized (HwThemeManager.class) {
            mOverLayThemeType = overLayThemeType;
        }
    }

    public static String getOverLayThemeType() {
        return mOverLayThemeType;
    }

    public static boolean isDeepDarkTheme(int userId) {
        setOverLayThemePath(OVERLAY_THEME + userId);
        setOverLayThemeType(SystemProperties.get(mOverLayThemePath));
        return isDeepDarkTheme();
    }

    public static boolean isDeepDarkTheme() {
        return DARK_TAG.equals(mOverLayThemeType);
    }

    public static boolean isHonorProduct() {
        return HONOR_TAG.equalsIgnoreCase(PRODUCT_BRAND) ? IS_THEME_INSULATE ^ 1 : false;
    }
}
