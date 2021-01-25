package android.hwtheme;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.Notification;
import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageParserEx;
import android.content.pm.ResolveInfo;
import android.content.res.AbsResources;
import android.content.res.AbsResourcesImpl;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import com.android.internal.telephony.PhoneConstants;
import com.huawei.android.hwutil.CommandLineUtil;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HwThemeManager {
    public static final boolean DEBUG_UI_PROGRAM = SystemProperties.getBoolean("persist.deep.theme.debug_ui_kit", false);
    public static final String DEFAULT_HOME_WALLPAPER = "home_wallpaper_0";
    public static final String DIR_WALLPAPER = "/data/skin/wallpaper/";
    public static final String FRAMEWORKHWEXT_TAG = "frameworkhwext";
    public static final String HONOR_TAG = "honor";
    public static final String HWT_FPATH_SYSWP = "/data/system/users/0/wallpaper";
    public static final String HWT_GROUP_MEDIARW = "media_rw";
    public static final String HWT_MODE_ALL = "0775";
    public static final String HWT_NEW_KEYGUARD = "com.android.keyguard";
    public static final String HWT_OLD_KEYGUARD = "com.huawei.android.hwlockscreen";
    public static final String HWT_PATH_SKIN = (Environment.getDataDirectory() + "/skin");
    public static final String HWT_PATH_SKINALL = "/data/skin*";
    public static final String HWT_PATH_SKIN_INSTALL_FLAG = (Environment.getDataDirectory() + "/skin_if");
    public static final String HWT_PATH_SYSWP = "/data/system/users/0/";
    public static final String HWT_PATH_TEMP_SKIN = "/data/skin.tmp";
    public static final String HWT_PATH_THEME = (Environment.getDataDirectory() + "/themes");
    public static final String HWT_USER_ROOT = "root";
    public static final String HWT_USER_SYSTEM = "system";
    public static final boolean IS_THEME_INSULATE = SystemProperties.getBoolean("ro.config.hw_themeInsulate", false);
    private static final String LOCK_RES = "LockRes";
    public static final int NO_SUPPORT_DEEP_THEME = 0;
    public static final String PRODUCT_BRAND = SystemProperties.get("ro.product.brand");
    public static final int SUPPORT_HONOR = 16;
    private static boolean SUPPORT_LOCK_DPI = SystemProperties.getBoolean("ro.config.auto_display_mode", true);
    public static final String SYS_WALLPAPER = "wallpaper";
    private static final String TAG = HwThemeManager.class.getSimpleName();
    public static final String TMP_WALLPAPAER = "/data/skin.tmp/wallpaper/";
    private static volatile IHwThemeManager sInstance = null;
    private static Map<String, Bundle> sMultidpiInfos = new HashMap();
    private static Object sMultidpiInfosLock = new Object();

    public interface IHwThemeManager {
        void addSimpleUIConfig(PackageParserEx.ActivityEx activityEx);

        void applyDefaultHwTheme(boolean z, Context context);

        void applyDefaultHwTheme(boolean z, Context context, int i);

        Bitmap generateBitmap(Context context, Bitmap bitmap, int i, int i2);

        Drawable getClonedDrawable(Context context, Drawable drawable);

        String getDefaultLiveWallpaper(int i);

        InputStream getDefaultWallpaperIS(Context context, int i);

        Drawable getHwBadgeDrawable(Notification notification, Context context, Drawable drawable);

        AbsResources getHwResources();

        AbsResourcesImpl getHwResourcesImpl();

        int getHwThemeLauncherIconSize(ActivityManager activityManager, Resources resources);

        Drawable getJoinBitmap(Context context, Drawable drawable, int i);

        int getShadowcolor(TypedArray typedArray, int i);

        Bitmap getThemeBitmap(Resources resources, int i, Rect rect);

        int getThemeColor(int[] iArr, int i, TypedValue typedValue, Resources resources, boolean z);

        void initForThemeFont(Configuration configuration);

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
        IHwThemeManagerFactory obj;
        if (sInstance == null) {
            synchronized (HwThemeManager.class) {
                if (sInstance == null && (obj = HwFrameworkFactory.getHwThemeManagerFactory()) != null) {
                    sInstance = obj.getThemeManagerInstance();
                }
            }
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

    public static class HwThemeManagerFileFilter implements FileFilter {
        @Override // java.io.FileFilter
        public boolean accept(File pathname) {
            if (!pathname.isFile() || pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".xml")) {
                return false;
            }
            return true;
        }
    }

    public static void updateConfiguration() {
        getImplObject().updateConfiguration();
    }

    public static void updateConfiguration(boolean isChangeuser) {
        getImplObject().updateConfiguration(isChangeuser);
    }

    public static AbsResourcesImpl getHwResourcesImpl() {
        return getImplObject().getHwResourcesImpl();
    }

    public static AbsResources getHwResources() {
        return getImplObject().getHwResources();
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

    public static boolean installHwTheme(String themePath, boolean isSetwallpaper) {
        return getImplObject().installHwTheme(themePath, isSetwallpaper);
    }

    public static boolean installHwTheme(String themePath, boolean isSetwallpaper, int userId) {
        return getImplObject().installHwTheme(themePath, isSetwallpaper, userId);
    }

    public static boolean makeIconCache(boolean isClearall) {
        return getImplObject().makeIconCache(isClearall);
    }

    public static boolean saveIconToCache(Bitmap bitmap, String fn, boolean isClearold) {
        return getImplObject().saveIconToCache(bitmap, fn, isClearold);
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

    public static void applyDefaultHwTheme(boolean isCheckState, Context ctx) {
        getImplObject().applyDefaultHwTheme(isCheckState, ctx);
    }

    public static void applyDefaultHwTheme(boolean isCheckState, Context ctx, int userId) {
        getImplObject().applyDefaultHwTheme(isCheckState, ctx, userId);
    }

    public static void addSimpleUIConfig(PackageParserEx.ActivityEx activity) {
        getImplObject().addSimpleUIConfig(activity);
    }

    public static void retrieveSimpleUIConfig(ContentResolver cr, Configuration config, int userId) {
        getImplObject().retrieveSimpleUIConfig(cr, config, userId);
    }

    public static void updateSimpleUIConfig(ContentResolver cr, Configuration config, int configChanges) {
        getImplObject().updateSimpleUIConfig(cr, config, configChanges);
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

    public static boolean isHonorProduct() {
        return HONOR_TAG.equalsIgnoreCase(PRODUCT_BRAND) && !IS_THEME_INSULATE;
    }

    public static boolean isHonorBrand() {
        return HONOR_TAG.equalsIgnoreCase(PRODUCT_BRAND);
    }

    public static Bundle getPreMultidpiInfo(String packageName) {
        Bundle dpiInfo;
        ApplicationInfo tmpInfo;
        if (!SUPPORT_LOCK_DPI || packageName == null || "android".equals(packageName) || "androidhwext".equals(packageName)) {
            return null;
        }
        synchronized (sMultidpiInfosLock) {
            dpiInfo = sMultidpiInfos.get(packageName);
        }
        if (dpiInfo != null) {
            return dpiInfo;
        }
        Bundle dpiInfo2 = new Bundle();
        ApplicationInfo tmpInfo2 = null;
        try {
            IPackageManager pm = AppGlobals.getPackageManager();
            if (pm != null) {
                tmpInfo2 = pm.getApplicationInfo(packageName, 128, UserHandle.getUserId(Process.myUid()));
            }
            tmpInfo = tmpInfo2;
        } catch (RemoteException e) {
            String str = TAG;
            Log.w(str, "getApplicationInfo for " + packageName + ", Exception:" + e.getMessage());
            tmpInfo = null;
        }
        if (tmpInfo != null) {
            boolean needLockRes = (tmpInfo.flags & 1) != 0;
            Bundle metaData = tmpInfo.metaData;
            if (metaData != null) {
                if (PhoneConstants.APN_TYPE_DEFAULT.equalsIgnoreCase(metaData.getString("support_display_mode"))) {
                    dpiInfo2.putBoolean("LockDpi", true);
                }
                String msg = metaData.getString("huawei_support_lock_res");
                if (TextUtils.isEmpty(msg)) {
                    msg = metaData.getString("support_lock_res");
                }
                if ("lock".equalsIgnoreCase(msg)) {
                    dpiInfo2.putBoolean(LOCK_RES, true);
                } else if ("no_lock".equalsIgnoreCase(msg)) {
                    dpiInfo2.putBoolean(LOCK_RES, false);
                } else {
                    dpiInfo2.putBoolean(LOCK_RES, needLockRes);
                }
            } else {
                dpiInfo2.putBoolean(LOCK_RES, needLockRes);
            }
        }
        synchronized (sMultidpiInfosLock) {
            sMultidpiInfos.put(packageName, dpiInfo2);
        }
        return dpiInfo2;
    }

    public static void initZygoteResource(Resources res) {
        if (UserHandle.getAppId(Process.myUid()) < 1000) {
            Log.i(TAG, "init zygote theme resources");
            res.getImpl().getHwResourcesImpl().initResource();
        }
    }

    public static ArrayList<String> getDataSkinThemePackages() {
        ArrayList<String> themePackages = new ArrayList<>();
        File themePath = new File(HWT_PATH_SKIN);
        if (themePath.exists()) {
            File[] files = themePath.listFiles(new HwThemeManagerFileFilter());
            if (files == null) {
                return null;
            }
            for (File f : files) {
                if (f.getName() == null || (!f.getName().contains("launcher") && !f.getName().contains("deskclock") && !f.getName().contains("aod") && !f.getName().contains("totemweather"))) {
                    themePackages.add(f.getName());
                }
            }
        }
        return themePackages;
    }
}
