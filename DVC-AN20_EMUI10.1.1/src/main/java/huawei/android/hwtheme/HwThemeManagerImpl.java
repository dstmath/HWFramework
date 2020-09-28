package huawei.android.hwtheme;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageParserEx;
import android.content.pm.ResolveInfo;
import android.content.pm.ResolveInfoUtils;
import android.content.res.AbsResources;
import android.content.res.AbsResourcesImpl;
import android.content.res.Configuration;
import android.content.res.HwResource;
import android.content.res.HwResourcesImpl;
import android.content.res.IHwConfiguration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.Environment;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.content.om.IOverlayManagerEx;
import com.huawei.android.content.om.OverlayInfoEx;
import com.huawei.android.content.pm.HwPackageManager;
import com.huawei.android.content.pm.IPackageManagerEx;
import com.huawei.android.content.res.ConfigurationAdapter;
import com.huawei.android.content.res.ResourcesImplAdapter;
import com.huawei.android.graphics.CanvasEx;
import com.huawei.android.hwutil.CommandLineUtil;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.util.DisplayMetricsEx;
import com.huawei.bd.Reporter;
import com.huawei.hwpartbasicplatform.BuildConfig;
import com.huawei.utils.HwPartResourceUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.android.hwutil.IconCache;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class HwThemeManagerImpl implements HwThemeManager.IHwThemeManager {
    private static final String ALARM_CLASS_POSTFIX = "_alarm.";
    static final String BLURRED_WALLPAPER = "/blurwallpaper";
    private static final String CALENDAR_CLASS_POSTFIX = "_calendar.";
    static final String CURRENT_HOMEWALLPAPER_NAME = "home_wallpaper_0.jpg";
    static final String CURRENT_HOMEWALLPAPER_NAME_PNG = "home_wallpaper_0.png";
    private static final int CUST_HOME_SCREEN_OFF = 0;
    private static final int CUST_HOME_SCREEN_ON = 1;
    static final String CUST_THEME_NAME = "hw_def_theme";
    static final String CUST_WALLPAPER = "/data/cust/wallpaper/wallpaper1.jpg";
    static final String CUST_WALLPAPER_DIR = "/data/cust/wallpaper/";
    static final String CUST_WALLPAPER_FILE_NAME = "wallpaper1.jpg";
    private static final String DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final ComponentName DOCOMOHOME_COMPONENT = new ComponentName("com.nttdocomo.android.dhome", "com.nttdocomo.android.dhome.HomeActivity");
    private static final ComponentName DRAWERHOME_COMPONENT = new ComponentName(LAUNCHER_PACKAGE, "com.huawei.android.launcher.drawer.DrawerLauncher");
    private static final String ECOTA_BACKUP_DIR = "/ecota_backup";
    private static final String ECOTA_FONT_PATH = "/cust/ecota/themes/fonts/";
    private static final String ECOTA_UNLOCK_WALLPAPER_FILE = "/cust/ecota/themes/wallpaper/unlock_wallpaper_0.jpg";
    private static final String ECOTA_UNLOCK_WALLPAPER_FILE_PNG = "/cust/ecota/themes/wallpaper/unlock_wallpaper_0.png";
    private static final String ECOTA_VERSION = "EcotaVersion";
    private static final String ECOTA_WALLPAPER_PATH = "/cust/ecota/themes/wallpaper/";
    private static final String EMAIL_CLASS_POSTFIX = "_email.";
    private static final String EXTRA_HW_IS_INTENT_PROTECTED_APP = "com.huawei.isIntentProtectedApp";
    private static final String FONT_DIR = "/fonts";
    private static final String FWK_HONOR_TAG = "com.android.frameworkhwext.honor";
    private static final String HASH_TAG = "#";
    private static final String HAS_CHANGE_FONT = "hasChangeFont";
    private static final String HAS_CHANGE_WALLPAPER = "hasChangeWallpaper";
    private static final String HWTHEME_GET_NOTIFICATION_INFO = "com.huawei.hwtheme.permission.GET_NOTIFICATION_INFO";
    private static final boolean IS_COTA_FEATURE = SystemPropertiesEx.getBoolean("ro.config.hw_cota", false);
    static final boolean IS_DEBUG = false;
    private static final boolean IS_HOTA_RESTORE_THEME = SystemPropertiesEx.getBoolean("ro.config.hw_hotaRestoreTheme", false);
    private static final boolean IS_REGIONAL_PHONE_FEATURE = SystemPropertiesEx.getBoolean("ro.config.region_phone_feature", false);
    private static final boolean IS_SHOW_CUSTUI_DEFAULT = SystemPropertiesEx.getBoolean("ro.config.show_custui_default", false);
    private static final boolean IS_SHOW_CUST_HOME_SCREEN = (SystemPropertiesEx.getInt("ro.config.show_cust_homescreen", 0) == 1);
    private static final boolean IS_SUPPORT_CLONE_APP = SystemPropertiesEx.getBoolean("ro.config.hw_support_clone_app", false);
    private static final String KEY_DISPLAY_MODE = "display_mode";
    private static final String LAUNCHER_PACKAGE = "com.huawei.android.launcher";
    public static final String LIVEWALLPAPER_FILE = "livepaper.xml";
    private static final String MESSAGE_CLASS_POSTFIX = "_message.";
    private static final ComponentName NEWSIMPLEHOME_COMPONENT = new ComponentName(LAUNCHER_PACKAGE, "com.huawei.android.launcher.newsimpleui.NewSimpleLauncher");
    public static final String NODE_LIVEWALLPAPER_CLASS = "classname";
    public static final String NODE_LIVEWALLPAPER_PACKAGE = "pkgname";
    private static final String NOTIFI_CLASS_POSTFIX = "_notification.";
    static final String PATH_DATASKIN_WALLPAPER = "/data/themes/";
    static final String PATH_DATA_USERS = "/data/system/users/";
    private static final String PROP_WALLPAPER = "ro.config.wallpaper";
    private static final String RINGTONE_CLASS_POSTFIX = "_ringtone.";
    private static final ComponentName SIMPLEHOME_COMPONENT = new ComponentName(LAUNCHER_PACKAGE, "com.huawei.android.launcher.simpleui.SimpleUILauncher");
    private static final ComponentName SIMPLELAUNCHERHOME_COMPONENT = new ComponentName("com.huawei.android.simplelauncher", "com.huawei.android.simplelauncher.unihome.UniHomeLauncher");
    private static final String SLASH = "/";
    private static final int STYLE_DATA = 1;
    private static final String SYSTEM_APP_HWEXT = "androidhwext";
    static final String TAG = "HwThemeManagerImpl";
    private static final String THEME_FONTS_BASE_PATH = "/data/skin/fonts/";
    private static final String TRUE = "true";
    private static final ComponentName UNIHOME_COMPONENT = new ComponentName(LAUNCHER_PACKAGE, "com.huawei.android.launcher.unihome.UniHomeLauncher");
    private static final String WALLPAPER_DIR = "/wallpaper";
    static final String WALLPAPER_INFO = "/wallpaper_info.xml";
    private static boolean mIsDefaultThemeOk = true;
    private static ResolveInfoUtils resolveInfoUtils = EasyInvokeFactory.getInvokeUtils(ResolveInfoUtils.class);
    private List<ComponentState> mDisablelaunchers = new ArrayList();
    private String mFontName = "DroidSansChinese.ttf";
    private final IHwConfiguration mLastHwConfig = initHwConfiguration();
    private Locale mLastLocale = Locale.getDefault();
    private Map<Integer, Integer> mLauncherMap = new HashMap();
    private final Object mLockForClone = new Object();
    private Map<String, String> mPackageNameMap = new HashMap();
    private IconCache.CacheEntry mTempRemovedEntry;

    public HwThemeManagerImpl() {
        initLauncherComponent();
    }

    public final IHwConfiguration initHwConfiguration() {
        try {
            return (IHwConfiguration) Class.forName("android.content.res.HwConfiguration").newInstance();
        } catch (ClassNotFoundException e) {
            Log.e("Configuration", "init HwConfiguration error");
            return null;
        } catch (Exception e2) {
            Log.e("Configuration", "reflection exception");
            return null;
        }
    }

    public void initForThemeFont(Configuration config) {
        this.mLastHwConfig.hwtheme = ConfigurationAdapter.getExtraConfig(config).hwtheme;
        if (config.locale != null) {
            this.mLastLocale = (Locale) config.locale.clone();
        }
    }

    private void setThemeWallpaper(String fileName, Context context) {
        File file = new File(fileName);
        if (!file.exists()) {
            Log.w(TAG, "pwm setwallpaper stopped");
            return;
        }
        WallpaperManager wpm = (WallpaperManager) context.getSystemService("wallpaper");
        InputStream ips = null;
        try {
            ips = new FileInputStream(file);
            wpm.setStream(ips);
            try {
                ips.close();
            } catch (IOException e) {
                Log.w(TAG, "close wallpaper file error");
            }
        } catch (FileNotFoundException e2) {
            Log.w(TAG, "pwm setwallpaper not found err");
            if (ips != null) {
                ips.close();
            }
        } catch (IOException e3) {
            Log.w(TAG, "pwm setwallpaper io err:");
            if (ips != null) {
                ips.close();
            }
        } catch (Throwable th) {
            if (ips != null) {
                try {
                    ips.close();
                } catch (IOException e4) {
                    Log.w(TAG, "close wallpaper file error");
                }
            }
            throw th;
        }
    }

    private boolean isFileExists(String filename) {
        if (filename == null) {
            return false;
        }
        return new File(filename).exists();
    }

    public void setTheme(String themePath) {
    }

    public boolean installHwTheme(String themePath) {
        return installHwTheme(themePath, false, UserHandleEx.myUserId());
    }

    public boolean installHwTheme(String themePath, boolean isSetwallpaper) {
        return installHwTheme(themePath, isSetwallpaper, UserHandleEx.myUserId());
    }

    public boolean installHwTheme(String themePath, boolean isSetwallpaper, int userId) {
        return HwPackageManager.pmInstallHwTheme(themePath, isSetwallpaper, userId);
    }

    public boolean installDefaultHwTheme(Context ctx) {
        return installDefaultHwTheme(ctx, UserHandleEx.myUserId());
    }

    public boolean installDefaultHwTheme(Context ctx, int userId) {
        if (isDhomeThemeAdaptBackcolor()) {
            installDefaultDhomeTheme(ctx);
        }
        String defaultName = getDefaultHwThemePack(ctx);
        Log.w(TAG, "the default theme: " + defaultName);
        return installHwTheme(defaultName, false, userId);
    }

    public boolean makeIconCache(boolean isClearall) {
        return true;
    }

    public boolean saveIconToCache(Bitmap bitmap, String fn, boolean isClearold) {
        return true;
    }

    private Map<String, String> getColorsAndThemes(String colorsThemes) {
        String[] colorsAndThemes = colorsThemes.split(";");
        Map<String, String> colorsThemesMap = new HashMap<>(colorsAndThemes.length);
        for (String str : colorsAndThemes) {
            String[] colorThemes = str.split(",");
            if (colorThemes.length != 2 || colorThemes[0].isEmpty()) {
                Log.w(TAG, "invalid color and theme : " + str);
            } else {
                colorsThemesMap.put(colorThemes[0].toLowerCase(Locale.US), colorThemes[1]);
            }
        }
        return colorsThemesMap;
    }

    private String getDefaultHwThemePack(Context ctx) {
        String colorsThemes = Settings.System.getString(ctx.getContentResolver(), "colors_themes");
        if (TextUtils.isEmpty(colorsThemes)) {
            Log.w(TAG, "colors and themes is empty!");
            return Settings.System.getString(ctx.getContentResolver(), CUST_THEME_NAME);
        }
        Map<String, String> hwColorThemes = getColorsAndThemes(colorsThemes);
        if (hwColorThemes.isEmpty()) {
            Log.w(TAG, "has no valid color-theme!");
            return Settings.System.getString(ctx.getContentResolver(), CUST_THEME_NAME);
        }
        String color = SystemPropertiesEx.get("ro.config.devicecolor");
        if (color == null) {
            return Settings.System.getString(ctx.getContentResolver(), CUST_THEME_NAME);
        }
        String hwThemePath = hwColorThemes.get(color.toLowerCase(Locale.US));
        if (isFileExists(hwThemePath)) {
            Settings.System.putString(ctx.getContentResolver(), CUST_THEME_NAME, hwThemePath);
            Log.w(TAG, "The TP color: " + color + ", Theme path: " + hwThemePath);
            return hwThemePath;
        }
        String backColor = SystemPropertiesEx.get("ro.config.backcolor");
        if (hwThemePath == null && !TextUtils.isEmpty(backColor)) {
            String groupColor = color + "+" + backColor;
            String hwThemePath2 = hwColorThemes.get(groupColor.toLowerCase(Locale.US));
            if (isFileExists(hwThemePath2)) {
                Settings.System.putString(ctx.getContentResolver(), CUST_THEME_NAME, hwThemePath2);
                Log.w(TAG, "The group color: " + groupColor + ", Theme path: " + hwThemePath2);
                return hwThemePath2;
            }
        }
        return Settings.System.getString(ctx.getContentResolver(), CUST_THEME_NAME);
    }

    public boolean isDhomeThemeAdaptBackcolor() {
        return SystemPropertiesEx.getBoolean("ro.config.hw_dhome_theme", false);
    }

    private String getDhomeThemeName(Context ctx) {
        String docomoDefaultThemeName = Settings.System.getString(ctx.getContentResolver(), "cust_default_theme");
        String colorsThemes = Settings.System.getString(ctx.getContentResolver(), "cust_color_themes");
        String isDocomoMultiThemes = Settings.System.getString(ctx.getContentResolver(), "cust_multi_themes");
        if (isDocomoMultiThemes != null && TRUE.equals(isDocomoMultiThemes)) {
            colorsThemes = Settings.System.getString(ctx.getContentResolver(), "cust_color_multi_themes");
        }
        if (TextUtils.isEmpty(colorsThemes)) {
            Log.w(TAG, "dcm colors and themes is empty!");
            return docomoDefaultThemeName;
        }
        Map<String, String> docomoHomeColorThemes = getColorsAndThemes(colorsThemes);
        if (docomoHomeColorThemes.isEmpty()) {
            Log.w(TAG, "has no valid dcm color_theme!");
            return docomoDefaultThemeName;
        }
        String backColor = SystemPropertiesEx.get("ro.config.backcolor");
        if (backColor == null || backColor.isEmpty()) {
            Log.w(TAG, "has no backcolor property,use default docomo theme");
            return docomoDefaultThemeName;
        }
        String dcmThemeName = docomoHomeColorThemes.get(backColor.toLowerCase(Locale.US));
        if (dcmThemeName == null || dcmThemeName.isEmpty()) {
            Log.w(TAG, "has no theme adapt to backcolor,use default docomo theme");
            return docomoDefaultThemeName;
        }
        Settings.System.putString(ctx.getContentResolver(), "cust_default_theme", dcmThemeName);
        Log.w(TAG, "get docomo default theme OK,dcmThemePath:" + dcmThemeName);
        return dcmThemeName;
    }

    private void linkTheme(String origin, String target) {
        CommandLineUtil.link("system", origin, target);
    }

    private void setAccessRight(String path) {
        CommandLineUtil.chmod("system", "0775", path);
        CommandLineUtil.chown("system", "system", "media_rw", path);
    }

    private String getDhomeThemePath(Context ctx, String themePath, String def) {
        if (ctx == null || TextUtils.isEmpty(themePath)) {
            return def;
        }
        String path = Settings.System.getString(ctx.getContentResolver(), themePath);
        return TextUtils.isEmpty(path) ? def : path;
    }

    public boolean installDefaultDhomeTheme(Context ctx) {
        String docomoAllThemesDir = Environment.getDataDirectory() + "/kisekae";
        File themefolderDir = new File(docomoAllThemesDir);
        if (UserHandleEx.getCallingUserId() != 0) {
            return false;
        }
        String hwThemeFileName = PATH_DATASKIN_WALLPAPER + UserHandleEx.myUserId();
        File hwThemePath = new File(hwThemeFileName);
        if (!hwThemeFileName.contains("..") && !hwThemePath.exists()) {
            if (!hwThemePath.mkdirs()) {
                return false;
            }
            setAccessRight(PATH_DATASKIN_WALLPAPER);
        }
        if (themefolderDir.exists() || themefolderDir.mkdir()) {
            setAccessRight(docomoAllThemesDir);
            return linkAndSaveTheme(ctx, docomoAllThemesDir);
        }
        Log.w(TAG, "mkdir /data/kisekae fail !!!");
        return false;
    }

    private boolean linkAndSaveTheme(Context ctx, String docomoAllThemesDir) {
        File originThemeFile;
        String str = docomoAllThemesDir;
        String docomoPrefabricateThemesDir = getDhomeThemePath(ctx, "cust_theme_path", "/cust/docomo/jp/themes/");
        String docomoDefaultThemePath = str + "/kisekae0.kin";
        String deviceThemeName = getDhomeThemeName(ctx);
        String originThemePath = docomoPrefabricateThemesDir + deviceThemeName;
        File originThemeFile2 = new File(originThemePath);
        String isDocomoMultiThemes = Settings.System.getString(ctx.getContentResolver(), "cust_multi_themes");
        if (isDocomoMultiThemes == null || !TRUE.equals(isDocomoMultiThemes)) {
            if (originThemeFile2.exists()) {
                linkTheme(originThemePath, docomoDefaultThemePath);
            }
            originThemeFile = originThemeFile2;
        } else {
            String[] deviceThemeNames = !TextUtils.isEmpty(deviceThemeName) ? deviceThemeName.split(":") : new String[0];
            int linkThemeTag = 0;
            int length = deviceThemeNames.length;
            originThemeFile = originThemeFile2;
            int i = 0;
            while (i < length) {
                String originMultiThemePath = docomoPrefabricateThemesDir + deviceThemeNames[i];
                originThemeFile = new File(originMultiThemePath);
                if (originThemeFile.exists()) {
                    linkTheme(originMultiThemePath, str + "/kisekae" + linkThemeTag + ".kin");
                    linkThemeTag++;
                }
                i++;
                str = docomoAllThemesDir;
            }
        }
        if (!isFileExists(docomoDefaultThemePath)) {
            Log.w(TAG, docomoDefaultThemePath + " not exist");
            return false;
        }
        saveDhomeThemeInfo(originThemeFile, docomoDefaultThemePath);
        return true;
    }

    private void saveDhomeThemeInfo(File originThemeFile, String defaultThemePath) {
        String str;
        if (originThemeFile == null || TextUtils.isEmpty(defaultThemePath)) {
            Log.e(TAG, "saveDhomeThemeInfo :: origin theme file or default theme path invalid.");
            return;
        }
        Properties properties = new Properties();
        properties.put("default_theme_path", defaultThemePath);
        String themeName = originThemeFile.getName();
        String str2 = "kisekae_Cute.kin";
        if (themeName.contains(str2)) {
            str = "silver";
        } else {
            str = themeName.contains("village.kin") ? "gold" : "unknow";
        }
        properties.put("current_docomo_theme_type", str);
        properties.put("current_docomo_theme_name", themeName);
        if (themeName.contains(str2)) {
            str2 = "village.kin";
        } else if (!themeName.contains("village.kin")) {
            str2 = "unknow";
        }
        properties.put("deleted_docomo_themes", str2);
        FileOutputStream fos = null;
        String deviceThemeInfoPath = defaultThemePath.substring(0, defaultThemePath.lastIndexOf(47)) + "/DeviceThemeInfo.properties";
        try {
            fos = new FileOutputStream(deviceThemeInfoPath);
            properties.store(fos, (String) null);
            setAccessRight(deviceThemeInfoPath);
            try {
                fos.close();
            } catch (IOException e) {
                Log.w(TAG, "FileOutputStream close failed!");
            }
        } catch (FileNotFoundException e2) {
            Log.w(TAG, deviceThemeInfoPath + " not found");
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e3) {
            Log.w(TAG, "DeviceThemeInfo.properties store failed!");
            if (fos != null) {
                fos.close();
            }
        } catch (Throwable th) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e4) {
                    Log.w(TAG, "FileOutputStream close failed!");
                }
            }
            throw th;
        }
    }

    public void updateConfiguration() {
        updateConfiguration(false);
    }

    public void updateConfiguration(boolean isChangeUser) {
        try {
            Configuration curConfig = new Configuration();
            ConfigurationAdapter.getExtraConfig(curConfig).setConfigItem(1, ConfigurationAdapter.getExtraConfig(ActivityManagerEx.getConfiguration()).getConfigItem(1) + 1);
            ActivityManagerEx.updateConfiguration(curConfig);
            if ((!HwPCUtils.enabled() || ActivityThreadEx.currentActivityThread() == null || !HwPCUtils.isValidExtDisplayId(ActivityThreadEx.currentActivityThread().getDisplayId())) && !isChangeUser) {
                updateOverlaysThems();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "updateConfiguration occurs exception e.msg = " + e.getMessage());
        }
    }

    private void updateOverlaysThems() {
        int newUserId = UserHandleEx.myUserId();
        IOverlayManagerEx overlayManager = IOverlayManagerEx.getInstance(ServiceManagerEx.getService("overlay"));
        if (overlayManager != null) {
            List<OverlayInfoEx> frameworkhwextinfos = null;
            try {
                frameworkhwextinfos = overlayManager.getOverlayInfosForTarget(SYSTEM_APP_HWEXT, newUserId);
            } catch (RemoteException e) {
                Log.e(TAG, "fail get fwk overlayinfos");
            }
            if (frameworkhwextinfos != null) {
                for (OverlayInfoEx info : frameworkhwextinfos) {
                    String packageName = info.getPackageName();
                    if (!TextUtils.isEmpty(packageName)) {
                        boolean isHonorProduct = HwThemeManager.isHonorProduct();
                        if (FWK_HONOR_TAG.equals(packageName) && isHonorProduct) {
                            try {
                                overlayManager.setEnabledExclusive(packageName, true, newUserId);
                            } catch (RemoteException e2) {
                                Log.e(TAG, "fail set " + packageName + " account access");
                            }
                        }
                    }
                }
                Log.i(TAG, "HwThemeManagerImpl#updateOverlaysThems end");
            }
        }
    }

    public AbsResourcesImpl getHwResourcesImpl() {
        return new HwResourcesImpl();
    }

    public AbsResources getHwResources() {
        return new HwResource();
    }

    public InputStream getDefaultWallpaperIS(Context context, int userId) {
        InputStream is = null;
        WallpaperManager wallpaperManager = (WallpaperManager) context.getSystemService("wallpaper");
        try {
            String path = SystemPropertiesEx.get(PROP_WALLPAPER);
            if (!TextUtils.isEmpty(path)) {
                File googleCustWallpaperFile = new File(path);
                if (googleCustWallpaperFile.exists()) {
                    InputStream is2 = new FileInputStream(googleCustWallpaperFile);
                    wallpaperManager.setStream(is2);
                    return is2;
                }
            }
            if (new File(CUST_WALLPAPER_DIR, CUST_WALLPAPER_FILE_NAME).exists()) {
                is = new FileInputStream(CUST_WALLPAPER);
            } else {
                File file = new File(PATH_DATASKIN_WALLPAPER + userId + "/wallpaper/", CURRENT_HOMEWALLPAPER_NAME);
                if (!file.exists()) {
                    file = new File(PATH_DATASKIN_WALLPAPER + userId + "/wallpaper/", CURRENT_HOMEWALLPAPER_NAME_PNG);
                }
                if (file.exists()) {
                    is = new FileInputStream(file);
                }
            }
        } catch (IOException e) {
        }
        if (is == null) {
            return context.getResources().openRawResource(HwPartResourceUtils.getResourceId("default_wallpaper"));
        }
        return is;
    }

    public void updateIconCache(PackageItemInfo packageItemInfo, String name, String packageName, int icon, int packageIcon) {
        String idAndPackageName = (icon != 0 ? icon : packageIcon) + HASH_TAG + packageName;
        if (!IconCache.contains(idAndPackageName)) {
            String tmpName = name != null ? name : packageName;
            String lc = tmpName != null ? tmpName.toLowerCase(Locale.ROOT) : null;
            if (lc != null && lc.indexOf("shortcut") < 0 && lc.indexOf(".cts") < 0) {
                IconCache.CacheEntry ce = new IconCache.CacheEntry();
                ce.name = tmpName;
                ce.type = icon != 0 ? 1 : 0;
                IconCache.add(idAndPackageName, ce);
            }
        }
    }

    public void updateResolveInfoIconCache(ResolveInfo resolveInfo, int icon, String resolvePackageName) {
        if (icon != 0 && resolveInfo != null) {
            if (resolvePackageName != null) {
                updateIconCache(null, null, resolvePackageName, icon, 0);
                return;
            }
            ComponentInfo ci = resolveInfoUtils.getComponentInfo(resolveInfo);
            if (ci != null) {
                updateIconCache(ci, ci.name, ci.packageName, icon, 0);
            }
        }
    }

    public void removeIconCache(String name, String packageName, int icon, int packageIcon) {
        int id = icon != 0 ? icon : packageIcon;
        String idAndPackageName = id + HASH_TAG + getResourcePackageName(packageName, id);
        if (IconCache.contains(idAndPackageName)) {
            this.mTempRemovedEntry = IconCache.get(idAndPackageName);
            IconCache.remove(idAndPackageName);
        }
    }

    public void restoreIconCache(String packageName, int icon) {
        if (this.mTempRemovedEntry != null) {
            IconCache.add(icon + HASH_TAG + packageName, this.mTempRemovedEntry);
            this.mTempRemovedEntry = null;
        }
    }

    public int getThemeColor(int[] data, int index, TypedValue value, Resources resources, boolean isFlag) {
        if (!isFlag || value.resourceId == 0) {
            return data[index + 1];
        }
        try {
            return resources.getColor(value.resourceId);
        } catch (Resources.NotFoundException e) {
            return data[index + 1];
        }
    }

    public int getHwThemeLauncherIconSize(ActivityManager am, Resources resources) {
        int configIconSize = SystemPropertiesEx.getInt("ro.config.app_big_icon_size", -1);
        int multiResSize = SystemPropertiesEx.getInt("persist.sys.res.icon_size", -1);
        if (configIconSize > 0 && multiResSize > 0) {
            configIconSize = multiResSize;
        }
        if (configIconSize == -1) {
            return (int) resources.getDimension(34472064);
        }
        return configIconSize;
    }

    public boolean isTRingtones(String path) {
        return path.indexOf(RINGTONE_CLASS_POSTFIX) > 0;
    }

    public boolean isTNotifications(String path) {
        return path.indexOf(NOTIFI_CLASS_POSTFIX) > 0 || path.indexOf(CALENDAR_CLASS_POSTFIX) > 0 || path.indexOf(EMAIL_CLASS_POSTFIX) > 0 || path.indexOf(MESSAGE_CLASS_POSTFIX) > 0;
    }

    public boolean isTAlarms(String path) {
        return path.indexOf(ALARM_CLASS_POSTFIX) > 0;
    }

    private String getResourcePackageNameFromMap(String packageName) {
        String str;
        synchronized (this.mPackageNameMap) {
            str = this.mPackageNameMap.get(packageName);
        }
        return str;
    }

    private void addResourcePackageName(String oldPkgName, String newPkgName) {
        synchronized (this.mPackageNameMap) {
            this.mPackageNameMap.put(oldPkgName, newPkgName);
        }
    }

    public int getShadowcolor(TypedArray a, int attr) {
        return a.getColor(attr, 0);
    }

    public void applyDefaultHwTheme(boolean isCheckState, Context context) {
        applyDefaultHwTheme(isCheckState, context, UserHandleEx.myUserId());
    }

    public void applyDefaultHwTheme(boolean isCheckState, Context context, int userId) {
        boolean isSkinExist = new File(HwThemeManager.HWT_PATH_THEME + SLASH + userId).exists();
        boolean installFlagExist = isFileExists(HwThemeManager.HWT_PATH_SKIN_INSTALL_FLAG);
        boolean isSet = !isSkinExist || (isCheckState && !mIsDefaultThemeOk) || installFlagExist;
        boolean isThemeInvalid = false;
        if (isSkinExist && !installFlagExist && isCheckState) {
            isThemeInvalid = isThemeInvalid(userId);
        }
        if (isSet || isThemeInvalid) {
            setIsDefaultThemeOk(installDefaultHwTheme(context, userId));
            isHotaRestoreThemeOrFirstBoot(context);
        } else if ((isSupportThemeRestore() && isCustChange(context) && !isThemeChange(context)) || isHotaRestoreThemeOrFirstBoot(context)) {
            deleteWallpaperInfoFile(userId);
            installCustomerTheme(context, userId);
            HwThemeManager.updateConfiguration();
        } else if (isEcotaVersion(context)) {
            if (!isUserChangeWallpaper(context)) {
                customizeEcotaWallpaper(context, userId);
            }
            if (!isUserChangeFont(context)) {
                customizeEcotaFont(context, userId);
            }
        } else if (isClearEcota(context)) {
            if (!isUserChangeWallpaper(context)) {
                Log.i(TAG, "clear Ecota wallpaper");
                deleteWallpaperInfoFile(userId);
                deleteSystemWallpaper(userId);
                restoreDefaultWallpaper(userId);
            }
            if (!isUserChangeFont(context)) {
                Log.i(TAG, "clear Ecota font");
                deleteEcotaFont(userId);
            }
        }
    }

    private static void setIsDefaultThemeOk(boolean isOk) {
        mIsDefaultThemeOk = isOk;
    }

    public void addSimpleUIConfig(PackageParserEx.ActivityEx activity) {
        if (activity.getMetaData().getBoolean("simpleuimode", false)) {
            activity.getActivityInfo().configChanges |= 65536;
        }
    }

    /* access modifiers changed from: private */
    public static class ComponentState {
        int mSetState = 2;
        ComponentName mlauncherComponent;

        ComponentState(ComponentName name) {
            this.mlauncherComponent = name;
        }

        public void setComponentEnable(int userId) {
            Log.d(HwThemeManagerImpl.TAG, "updateSimpleUIConfig mlauncherComponent =" + this.mlauncherComponent + ",mSetState=" + this.mSetState);
            try {
                IPackageManagerEx.setComponentEnabledSetting(this.mlauncherComponent, this.mSetState, 1, userId);
            } catch (IllegalArgumentException e) {
                Log.e(HwThemeManagerImpl.TAG, "setComponentEnabledSetting error");
            } catch (Exception e2) {
                Log.e(HwThemeManagerImpl.TAG, "setComponentEnabledSetting error");
            }
        }
    }

    private void initLauncherComponent() {
        this.mDisablelaunchers.clear();
        this.mLauncherMap.clear();
        if (IS_SHOW_CUST_HOME_SCREEN) {
            this.mDisablelaunchers.add(new ComponentState(DOCOMOHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(UNIHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(SIMPLELAUNCHERHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(SIMPLEHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(DRAWERHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(NEWSIMPLEHOME_COMPONENT));
        } else if (IS_SHOW_CUSTUI_DEFAULT) {
            this.mDisablelaunchers.add(new ComponentState(DOCOMOHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(SIMPLEHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(DRAWERHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(UNIHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(NEWSIMPLEHOME_COMPONENT));
        } else {
            this.mDisablelaunchers.add(new ComponentState(UNIHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(SIMPLEHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(DRAWERHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(NEWSIMPLEHOME_COMPONENT));
        }
        this.mLauncherMap.put(0, 0);
        this.mLauncherMap.put(1, 0);
        this.mLauncherMap.put(2, 1);
        this.mLauncherMap.put(4, 2);
        this.mLauncherMap.put(5, 3);
    }

    private void enableLaunchers(int type, int userId) {
        int disableSize = this.mDisablelaunchers.size();
        int i = 0;
        while (i < disableSize) {
            ComponentState componentstate = this.mDisablelaunchers.get(i);
            componentstate.mSetState = i == type ? 1 : 2;
            componentstate.setComponentEnable(userId);
            i++;
        }
    }

    public void retrieveSimpleUIConfig(ContentResolver cr, Configuration config, int userId) {
        int simpleuiVal = SettingsEx.System.getIntForUser(cr, SettingsEx.System.SIMPLEUI_MODE, 0, ActivityManagerEx.getCurrentUser());
        int launcherConfig = Settings.System.getInt(cr, "hw_launcher_desktop_mode", 0);
        Log.d(TAG, "updateSimpleUIConfig simpleuiVal =" + simpleuiVal + ",launcherConfig=" + launcherConfig);
        int i = 1;
        if (simpleuiVal == 0 || (IS_REGIONAL_PHONE_FEATURE && 1 != Settings.Secure.getInt(cr, SettingsEx.Secure.getUserSetupComplete(), 0))) {
            if (launcherConfig != 0) {
                i = launcherConfig;
            }
            simpleuiVal = i;
        }
        ConfigurationAdapter.getExtraConfig(config).setConfigItem(2, simpleuiVal);
        enableLaunchers(this.mLauncherMap.get(Integer.valueOf(simpleuiVal)).intValue(), userId);
    }

    public void updateSimpleUIConfig(ContentResolver cr, Configuration config, int configChanges) {
        if ((65536 & configChanges) != 0) {
            Log.w(TAG, "updateSimpleUIConfig SIMPLEUI_MODE putIntForUser =" + ConfigurationAdapter.getExtraConfig(config).getConfigItem(2));
            SettingsEx.System.putIntForUser(cr, SettingsEx.System.SIMPLEUI_MODE, ConfigurationAdapter.getExtraConfig(config).getConfigItem(2), ActivityManagerEx.getCurrentUser());
        }
    }

    public Bitmap getThemeBitmap(Resources res, int id, Rect padding) {
        TypedValue outValue = new TypedValue();
        res.getValue(id, outValue, true);
        if (outValue.string == null) {
            return null;
        }
        String file = outValue.string.toString();
        if (file.endsWith(".png") || file.endsWith(".jpg")) {
            return ResourcesImplAdapter.getHwResourcesImpl(res).getThemeBitmap(res, outValue, id, padding);
        }
        return null;
    }

    private static boolean isThemeFontExist() {
        File fontdir = new File(THEME_FONTS_BASE_PATH, BuildConfig.FLAVOR);
        if (!fontdir.exists() || fontdir.list() == null || fontdir.list().length == 0) {
            return false;
        }
        return true;
    }

    public void setThemeFont() {
        CanvasEx.freeCaches();
        CanvasEx.freeTextLayoutCaches();
    }

    public boolean setThemeFontOnConfigChg(Configuration newConfig) {
        Locale locale;
        boolean isHwThemeChanged = false;
        if (!(this.mLastHwConfig == null || newConfig == null)) {
            if ((32768 & this.mLastHwConfig.updateFrom(ConfigurationAdapter.getExtraConfig(newConfig))) != 0) {
                isHwThemeChanged = true;
            }
        }
        boolean isLocaleChanged = false;
        if (!(newConfig == null || newConfig.locale == null || ((locale = this.mLastLocale) != null && locale.equals(newConfig.locale)))) {
            this.mLastLocale = newConfig.locale != null ? (Locale) newConfig.locale.clone() : null;
            isLocaleChanged = true;
        }
        if (!isHwThemeChanged && !isLocaleChanged) {
            return false;
        }
        setThemeFont();
        return true;
    }

    public boolean isTargetFamily(String familyName) {
        return familyName != null && "chnfzxh".equals(familyName);
    }

    public boolean shouldUseAdditionalChnFont(String familyName) {
        if (!isTargetFamily(familyName)) {
            return false;
        }
        String curLang = Locale.getDefault().getLanguage();
        if (isThemeFontExist()) {
            return false;
        }
        if (curLang.contains("zh") || curLang.contains("en")) {
            return true;
        }
        return false;
    }

    public Bitmap generateBitmap(Context context, Bitmap bm, int width, int height) {
        if (bm == null) {
            return null;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        display.getMetrics(metrics);
        bm.setDensity(DisplayMetricsEx.getNoncompatDensityDpi(metrics));
        Point size = new Point();
        display.getRealSize(size);
        int max = size.x > size.y ? size.x : size.y;
        int min = size.x < size.y ? size.x : size.y;
        if (width > 0 && height > 0) {
            if (bm.getWidth() != width || bm.getHeight() != height) {
                if ((bm.getWidth() != min || bm.getHeight() != max) && (bm.getWidth() != max || bm.getHeight() != max)) {
                    if (bm.getWidth() != max || bm.getHeight() != min) {
                        try {
                            Bitmap newbm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                            if (newbm == null) {
                                Log.w(TAG, "Can't generate default bitmap, newbm = null");
                                return bm;
                            }
                            newbm.setDensity(DisplayMetricsEx.getNoncompatDensityDpi(metrics));
                            try {
                                Rect targetRect = generateTargetRect(bm, width, height);
                                Paint paint = new Paint();
                                paint.setFilterBitmap(true);
                                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                                new Canvas(newbm).drawBitmap(bm, (Rect) null, targetRect, paint);
                                bm.recycle();
                                return newbm;
                            } catch (OutOfMemoryError e) {
                                Log.w(TAG, "Can't generate default bitmap");
                                return bm;
                            }
                        } catch (OutOfMemoryError e2) {
                            Log.w(TAG, "Can't generate default bitmap");
                            return bm;
                        }
                    }
                }
            }
        }
        return bm;
    }

    private Rect generateTargetRect(Bitmap bm, int width, int height) {
        Rect targetRect = new Rect();
        targetRect.right = bm.getWidth();
        targetRect.bottom = bm.getHeight();
        int deltaw = width - targetRect.right;
        int deltah = height - targetRect.bottom;
        if (deltaw > 0 || deltah > 0) {
            float tempWidth = ((float) width) / ((float) targetRect.right);
            float tempHeight = ((float) height) / ((float) targetRect.bottom);
            float scale = tempWidth > tempHeight ? tempWidth : tempHeight;
            targetRect.right = (int) (((float) targetRect.right) * scale);
            targetRect.bottom = (int) (((float) targetRect.bottom) * scale);
            deltaw = width - targetRect.right;
            deltah = height - targetRect.bottom;
        }
        targetRect.offset(deltaw / 2, deltah / 2);
        return targetRect;
    }

    private String getResourcePackageName(String packageName, int icon) {
        String name = getResourcePackageNameFromMap(packageName);
        if (name != null) {
            return name;
        }
        String result = HwPackageManager.getResourcePackageNameByIcon(packageName, icon, UserHandleEx.myUserId());
        if (result != null && !BuildConfig.FLAVOR.equals(result)) {
            addResourcePackageName(packageName, result);
        }
        return result;
    }

    public Drawable getJoinBitmap(Context context, Drawable srcDraw, int backgroundId) {
        context.enforceCallingOrSelfPermission(HWTHEME_GET_NOTIFICATION_INFO, "getJoinBitmap");
        Resources r = context.getResources();
        if (r != null) {
            return ResourcesImplAdapter.getHwResourcesImpl(r).getJoinBitmap(srcDraw, backgroundId);
        }
        return null;
    }

    public Drawable getClonedDrawable(Context context, Drawable drawable) {
        Drawable clone;
        if (!IS_SUPPORT_CLONE_APP || (clone = context.getResources().getDrawable(33751231)) == null) {
            return drawable;
        }
        if (drawable != null) {
            return getClonedDrawable(context, drawable, clone);
        }
        clone.setColorFilter(context.getColor(33882311), PorterDuff.Mode.SRC_IN);
        clone.setBounds(0, 0, clone.getIntrinsicWidth(), clone.getIntrinsicHeight());
        return clone;
    }

    private Drawable getClonedDrawable(Context context, Drawable drawable, Drawable clone) {
        int cloneHeight;
        int cloneHeight2;
        float markBgRatio = Float.parseFloat(context.getResources().getString(34472193));
        float markBgPercentage = Float.parseFloat(context.getResources().getString(34472194));
        int srcWidth = drawable.getIntrinsicWidth();
        int srcHeight = drawable.getIntrinsicHeight();
        int tempSrcWidth = (int) (((float) srcWidth) * markBgPercentage);
        int tempSrcHeight = (int) (((float) srcHeight) * markBgPercentage);
        int markBgRadius = tempSrcWidth > tempSrcHeight ? tempSrcHeight : tempSrcWidth;
        if (clone.getIntrinsicWidth() >= clone.getIntrinsicHeight()) {
            int cloneWidth = (int) (((float) markBgRadius) / markBgRatio);
            cloneHeight = (int) (((((double) cloneWidth) * 1.0d) * ((double) clone.getIntrinsicHeight())) / ((double) clone.getIntrinsicWidth()));
            cloneHeight2 = cloneWidth;
        } else {
            int cloneHeight3 = (int) (((float) markBgRadius) / markBgRatio);
            cloneHeight = cloneHeight3;
            cloneHeight2 = (int) (((((double) cloneHeight3) * 1.0d) * ((double) clone.getIntrinsicWidth())) / ((double) clone.getIntrinsicHeight()));
        }
        synchronized (this.mLockForClone) {
            try {
                Bitmap newBitMap = Bitmap.createBitmap(srcWidth, srcWidth, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(newBitMap);
                drawable.setBounds(0, 0, srcWidth, srcWidth);
                drawable.draw(canvas);
                Paint mPaint = new Paint();
                try {
                    mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
                    mPaint.setAntiAlias(true);
                    canvas.save();
                    try {
                        mPaint.setColor(context.getColor(33882311));
                        canvas.translate((float) (srcWidth - markBgRadius), (float) (srcHeight - markBgRadius));
                        canvas.drawCircle(((float) markBgRadius) / 2.0f, ((float) markBgRadius) / 2.0f, ((float) markBgRadius) / 2.0f, mPaint);
                        canvas.translate(((float) (markBgRadius - cloneHeight2)) / 2.0f, ((float) (markBgRadius - cloneHeight)) / 2.0f);
                        canvas.scale((((float) cloneHeight2) * 1.0f) / ((float) clone.getIntrinsicWidth()), (((float) cloneHeight) * 1.0f) / ((float) clone.getIntrinsicHeight()));
                        clone.setBounds(0, 0, clone.getIntrinsicWidth(), clone.getIntrinsicHeight());
                        clone.draw(canvas);
                        canvas.restore();
                        mPaint.setXfermode(null);
                        BitmapDrawable mergedDrawable = new BitmapDrawable(context.getResources(), newBitMap);
                        if (drawable instanceof BitmapDrawable) {
                            mergedDrawable.setTargetDensity(((BitmapDrawable) drawable).getBitmap().getDensity());
                        }
                        return mergedDrawable;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public Drawable getHwBadgeDrawable(Notification notification, Context context, Drawable drawable) {
        Drawable trustSpace;
        boolean isNotificationValid = true;
        boolean isContextValid = context != null;
        if (notification == null || notification.extras == null) {
            isNotificationValid = false;
        }
        if (!isContextValid || !isNotificationValid || !notification.extras.getBoolean(EXTRA_HW_IS_INTENT_PROTECTED_APP) || (trustSpace = context.getResources().getDrawable(33751237)) == null) {
            return null;
        }
        trustSpace.setColorFilter(context.getColor(33882311), PorterDuff.Mode.SRC_IN);
        trustSpace.setBounds(0, 0, trustSpace.getIntrinsicWidth(), trustSpace.getIntrinsicHeight());
        return trustSpace;
    }

    public String getDefaultLiveWallpaper(int userId) {
        Element rootElement;
        Document document = null;
        String livepaperpath = "/data/themes//" + userId;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(livepaperpath, LIVEWALLPAPER_FILE));
        } catch (ParserConfigurationException e) {
            Log.e(TAG, " ParserConfigurationException " + livepaperpath);
        } catch (SAXException e2) {
            Log.e(TAG, "SAXException " + livepaperpath);
        } catch (IOException e3) {
            Log.e(TAG, "IOException " + livepaperpath);
        } catch (Exception e4) {
            Log.e(TAG, "Exception " + livepaperpath);
        }
        if (document == null || (rootElement = document.getDocumentElement()) == null) {
            return null;
        }
        LivewallpaperXmlInfo livewallpaperInfo = new LivewallpaperXmlInfo();
        NodeList itemNodes = rootElement.getChildNodes();
        int length = itemNodes.getLength();
        for (int i = 0; i < length; i++) {
            Node itemNode = itemNodes.item(i);
            if (itemNode.getNodeType() == 1) {
                if (NODE_LIVEWALLPAPER_PACKAGE.equals(itemNode.getNodeName())) {
                    livewallpaperInfo.setPackageName(itemNode.getTextContent());
                } else if (NODE_LIVEWALLPAPER_CLASS.equals(itemNode.getNodeName())) {
                    livewallpaperInfo.setClassName(itemNode.getTextContent());
                }
            }
        }
        StringBuffer livewallpaperStr = new StringBuffer();
        livewallpaperStr.append(livewallpaperInfo.getPackageName());
        livewallpaperStr.append(SLASH);
        livewallpaperStr.append(livewallpaperInfo.getClassName());
        Log.e(TAG, "HwThemeManager#getDefaultLiveWallpaper livewallpaperStr =" + livewallpaperStr.toString());
        return livewallpaperStr.toString();
    }

    private static class LivewallpaperXmlInfo {
        private String mClassName;
        private String mPackageName;

        private LivewallpaperXmlInfo() {
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public void setPackageName(String packageName) {
            this.mPackageName = packageName;
        }

        public String getClassName() {
            return this.mClassName;
        }

        public void setClassName(String className) {
            this.mClassName = className;
        }
    }

    public boolean installCustomerTheme(Context ctx, int userId) {
        String defname = getDefaultHwThemePack(ctx);
        Log.w(TAG, "the new default theme: " + defname);
        if (!isFileExists(defname)) {
            return false;
        }
        return installHwTheme(defname, true, userId);
    }

    public static boolean isSupportThemeRestore() {
        return IS_REGIONAL_PHONE_FEATURE || IS_COTA_FEATURE;
    }

    public boolean isCustChange(Context context) {
        try {
            if (IS_REGIONAL_PHONE_FEATURE) {
                String originalVendorCountry = Settings.Secure.getString(context.getContentResolver(), "vendor_country");
                String currentVendorCountry = SystemPropertiesEx.get("ro.hw.custPath", BuildConfig.FLAVOR);
                if (originalVendorCountry == null) {
                    Settings.Secure.putString(context.getContentResolver(), "vendor_country", currentVendorCountry);
                    return false;
                } else if (!originalVendorCountry.equals(currentVendorCountry)) {
                    Settings.Secure.putString(context.getContentResolver(), "vendor_country", currentVendorCountry);
                    return true;
                }
            } else if (IS_COTA_FEATURE) {
                String originalCotaVersion = Settings.Secure.getString(context.getContentResolver(), "cotaVersion");
                String cotaVersion = SystemPropertiesEx.get("ro.product.CotaVersion", BuildConfig.FLAVOR);
                if (originalCotaVersion == null) {
                    Settings.Secure.putString(context.getContentResolver(), "cotaVersion", cotaVersion);
                    return false;
                } else if (!cotaVersion.equals(originalCotaVersion)) {
                    Settings.Secure.putString(context.getContentResolver(), "cotaVersion", cotaVersion);
                    return true;
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "check cust Exception");
        } catch (Exception e2) {
            Log.e(TAG, "check cust Exception");
        }
        return false;
    }

    public static boolean isThemeChange(Context context) {
        return TRUE.equals(Settings.Secure.getString(context.getContentResolver(), "isUserChangeTheme"));
    }

    private boolean isThemeInvalid(int userId) {
        long startTime = SystemClock.elapsedRealtime();
        if (isFileInvalid((HwThemeManager.HWT_PATH_THEME + SLASH + userId) + SLASH + "wallpaper" + SLASH + CURRENT_HOMEWALLPAPER_NAME)) {
            Log.d(TAG, "Theme is not valid");
            return true;
        }
        Log.d(TAG, "check theme took " + (SystemClock.elapsedRealtime() - startTime));
        return false;
    }

    private boolean isFileInvalid(String fileName) {
        if (fileName == null || !isFileExists(fileName)) {
            return false;
        }
        boolean isInValid = false;
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = new FileInputStream(fileName);
            byte[] buffers = new byte[Reporter.MAX_CONTENT_SIZE];
            while (fis2.read(buffers) != -1) {
                int length = buffers.length;
                int i = 0;
                while (true) {
                    if (i < length) {
                        if (buffers[i] != 0) {
                            try {
                                fis2.close();
                            } catch (IOException e) {
                                Log.w(TAG, "isFileInvalid IOException ");
                            }
                            return false;
                        }
                        i++;
                    }
                }
            }
            isInValid = true;
            try {
                fis2.close();
            } catch (IOException e2) {
                Log.w(TAG, "isFileInvalid IOException ");
            }
        } catch (IOException e3) {
            Log.w(TAG, "isFileInvalid Exception ");
            if (0 != 0) {
                fis.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e4) {
                    Log.w(TAG, "isFileInvalid IOException ");
                }
            }
            throw th;
        }
        if (isInValid) {
            return true;
        }
        return false;
    }

    private boolean isHotaRestoreThemeOrFirstBoot(Context context) {
        if (!IS_HOTA_RESTORE_THEME) {
            return false;
        }
        try {
            String originalVersion = Settings.Secure.getString(context.getContentResolver(), "custVersion");
            String buildVersion = SystemPropertiesEx.get("ro.build.display.id", BuildConfig.FLAVOR);
            String custCVersion = SystemPropertiesEx.get("ro.product.CustCVersion", BuildConfig.FLAVOR);
            String custDVersion = SystemPropertiesEx.get("ro.product.CustDVersion", BuildConfig.FLAVOR);
            if ((buildVersion + custCVersion + custDVersion).equals(originalVersion)) {
                return false;
            }
            ContentResolver contentResolver = context.getContentResolver();
            Settings.Secure.putString(contentResolver, "custVersion", buildVersion + custCVersion + custDVersion);
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "check cust Exception");
            return false;
        } catch (Exception e2) {
            Log.e(TAG, "check cust Exception");
            return false;
        }
    }

    private boolean isEcotaVersion(Context context) {
        String currentEcotaVersion = SystemPropertiesEx.get("ro.product.EcotaVersion", BuildConfig.FLAVOR);
        if (TextUtils.isEmpty(currentEcotaVersion) || currentEcotaVersion.equals(Settings.Secure.getString(context.getContentResolver(), ECOTA_VERSION))) {
            return false;
        }
        Settings.Secure.putString(context.getContentResolver(), ECOTA_VERSION, currentEcotaVersion);
        return true;
    }

    private boolean isEcotaHomeWallpaperExist() {
        return new File("/cust/ecota/themes/wallpaper/home_wallpaper_0.jpg").exists() || new File("/cust/ecota/themes/wallpaper/home_wallpaper_0.png").exists();
    }

    private boolean isEcotaUnlockWallpaperExist() {
        return new File(ECOTA_UNLOCK_WALLPAPER_FILE).exists() || new File(ECOTA_UNLOCK_WALLPAPER_FILE_PNG).exists();
    }

    private void deleteWallpaperInfoFile(int userId) {
        if (new File(PATH_DATA_USERS + userId, WALLPAPER_INFO).exists()) {
            CommandLineUtil.rm("system", PATH_DATA_USERS + userId + WALLPAPER_INFO);
        }
        if (new File(PATH_DATA_USERS + userId, BLURRED_WALLPAPER).exists()) {
            CommandLineUtil.rm("system", PATH_DATA_USERS + userId + BLURRED_WALLPAPER);
        }
    }

    private void deleteSystemWallpaper(int userId) {
        if (new File("/data/system/users/0/", "wallpaper").exists()) {
            CommandLineUtil.rm("system", "/data/system/users/0/wallpaper");
            CommandLineUtil.rm("system", "/data/system/users/0/wallpaper_orig");
        }
        String liveWallpaperPath = PATH_DATASKIN_WALLPAPER + userId + File.separator + LIVEWALLPAPER_FILE;
        if (new File(liveWallpaperPath).exists()) {
            CommandLineUtil.rm("system", liveWallpaperPath);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0049, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004e, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004f, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0052, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0055, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005a, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005b, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005e, code lost:
        throw r3;
     */
    private void copyFile(File oldFile, String newPath) {
        if (oldFile.exists()) {
            try {
                FileInputStream input = new FileInputStream(oldFile);
                FileOutputStream output = new FileOutputStream(newPath + File.separator + oldFile.getName());
                byte[] byteData = new byte[Reporter.MAX_CONTENT_SIZE];
                while (true) {
                    int len = input.read(byteData);
                    if (len != -1) {
                        output.write(byteData, 0, len);
                    } else {
                        output.flush();
                        output.close();
                        input.close();
                        return;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "copyFile IOException");
            } catch (Exception e2) {
                Log.e(TAG, "copyFile Exception");
            }
        }
    }

    private boolean isUserChangeWallpaper(Context context) {
        return TRUE.equals(Settings.Secure.getString(context.getContentResolver(), "isUserChangeWallpaper"));
    }

    private boolean isClearEcota(Context context) {
        if (TextUtils.isEmpty(Settings.Secure.getString(context.getContentResolver(), ECOTA_VERSION)) || !TextUtils.isEmpty(SystemPropertiesEx.get("ro.product.EcotaVersion", BuildConfig.FLAVOR))) {
            return false;
        }
        Settings.Secure.putString(context.getContentResolver(), ECOTA_VERSION, BuildConfig.FLAVOR);
        Log.i(TAG, "isClearEcota == true");
        return true;
    }

    private void restoreDefaultWallpaper(int userId) {
        CommandLineUtil.copyFolder("system", HwThemeManager.HWT_PATH_THEME + File.separator + userId + ECOTA_BACKUP_DIR + WALLPAPER_DIR, HwThemeManager.HWT_PATH_THEME + File.separator + userId + WALLPAPER_DIR);
        File file = new File(HwThemeManager.HWT_PATH_THEME + File.separator + userId + ECOTA_BACKUP_DIR + File.separator + LIVEWALLPAPER_FILE);
        StringBuilder sb = new StringBuilder();
        sb.append(HwThemeManager.HWT_PATH_THEME);
        sb.append(File.separator);
        sb.append(userId);
        copyFile(file, sb.toString());
        CommandLineUtil.chmod("system", "0775", HwThemeManager.HWT_PATH_THEME + File.separator + userId);
    }

    private void backupDefaultWallpaper(int userId) {
        String backupThemePath = HwThemeManager.HWT_PATH_THEME + File.separator + userId + ECOTA_BACKUP_DIR;
        File backupThemeFile = new File(backupThemePath);
        if (!backupThemeFile.exists()) {
            boolean isMkdirsEcotaBackupSuccess = backupThemeFile.mkdirs();
            boolean isMkdirsWallpaperSuccess = new File(backupThemePath + WALLPAPER_DIR).mkdirs();
            if (!isMkdirsEcotaBackupSuccess || !isMkdirsWallpaperSuccess) {
                Log.w(TAG, "file mkdirs failed");
                return;
            }
        }
        CommandLineUtil.copyFolder("system", HwThemeManager.HWT_PATH_THEME + File.separator + userId, backupThemePath);
        CommandLineUtil.chmod("system", "0775", backupThemePath);
    }

    private void customizeEcotaWallpaper(Context context, int userId) {
        if (isEcotaHomeWallpaperExist() && isEcotaUnlockWallpaperExist()) {
            if (TextUtils.isEmpty(Settings.Secure.getString(context.getContentResolver(), HAS_CHANGE_WALLPAPER))) {
                backupDefaultWallpaper(userId);
            }
            Log.i(TAG, "Ecota change wallpaper");
            Settings.Secure.putString(context.getContentResolver(), HAS_CHANGE_WALLPAPER, TRUE);
            deleteWallpaperInfoFile(userId);
            deleteSystemWallpaper(userId);
            CommandLineUtil.copyFolder("system", ECOTA_WALLPAPER_PATH, HwThemeManager.HWT_PATH_THEME + File.separator + userId + WALLPAPER_DIR);
            CommandLineUtil.chmod("system", "0775", HwThemeManager.HWT_PATH_THEME + File.separator + userId + WALLPAPER_DIR);
            Settings.Global.putInt(context.getContentResolver(), "enable_magazinelock_feature", 0);
        } else if (TRUE.equals(Settings.Secure.getString(context.getContentResolver(), HAS_CHANGE_WALLPAPER))) {
            Log.i(TAG, "Ecota delete wallpaper");
            Settings.Secure.putString(context.getContentResolver(), HAS_CHANGE_WALLPAPER, "false");
            deleteWallpaperInfoFile(userId);
            deleteSystemWallpaper(userId);
            restoreDefaultWallpaper(userId);
        }
    }

    private boolean isUserChangeFont(Context context) {
        return TRUE.equals(Settings.Secure.getString(context.getContentResolver(), "isUserChangeFont"));
    }

    private boolean isEcotaFontExist() {
        return new File(ECOTA_FONT_PATH + this.mFontName).exists();
    }

    private void customizeEcotaFont(Context context, int userId) {
        if (isEcotaFontExist()) {
            Log.i(TAG, "Ecota change font");
            Settings.Secure.putString(context.getContentResolver(), HAS_CHANGE_FONT, TRUE);
            String fontFolderDir = HwThemeManager.HWT_PATH_THEME + File.separator + userId + FONT_DIR;
            if (new File(fontFolderDir).exists() || new File(fontFolderDir).mkdir()) {
                CommandLineUtil.copyFolder("system", ECOTA_FONT_PATH, fontFolderDir);
                CommandLineUtil.chmod("system", "0775", fontFolderDir);
            }
        } else if (TRUE.equals(Settings.Secure.getString(context.getContentResolver(), HAS_CHANGE_FONT))) {
            Log.i(TAG, "Ecota delete font");
            Settings.Secure.putString(context.getContentResolver(), HAS_CHANGE_FONT, "false");
            deleteEcotaFont(userId);
        } else {
            Log.i(TAG, "Ecota doesn't exist font");
        }
    }

    private void deleteEcotaFont(int userId) {
        if (new File(HwThemeManager.HWT_PATH_THEME + File.separator + userId + FONT_DIR).exists()) {
            CommandLineUtil.rm("system", HwThemeManager.HWT_PATH_THEME + File.separator + userId + FONT_DIR);
        }
    }
}
