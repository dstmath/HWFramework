package huawei.android.hwtheme;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.Notification;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.content.pm.ResolveInfoUtils;
import android.content.res.AbsResourcesImpl;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.ConfigurationEx;
import android.content.res.HwResources;
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
import android.os.FreezeScreenScene;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.DisplayAdjustments;
import android.view.WindowManager;
import com.huawei.android.content.pm.HwPackageManager;
import com.huawei.android.hwutil.CommandLineUtil;
import com.huawei.hsm.permission.StubController;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.android.hwutil.FileUtil;
import huawei.android.hwutil.IconCache;
import huawei.android.provider.HwSettings;
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
    static final boolean DEBUG = false;
    private static final String DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final ComponentName DOCOMOHOME_COMPONENT = new ComponentName("com.nttdocomo.android.dhome", "com.nttdocomo.android.dhome.HomeActivity");
    private static final ComponentName DRAWERHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.drawer.DrawerLauncher");
    private static final String ECOTA_UNLOCK_WALLPAPER_FILE = "/cust/ecota/themes/wallpaper/unlock_wallpaper_0.jpg";
    private static final String ECOTA_UNLOCK_WALLPAPER_FILE_PNG = "/cust/ecota/themes/wallpaper/unlock_wallpaper_0.png";
    private static final String ECOTA_WALLPAPER_PATH = "/cust/ecota/themes/wallpaper/";
    private static final String EMAIL_CLASS_POSTFIX = "_email.";
    private static final String FWK_HONOR_TAG = "com.android.frameworkhwext.honor";
    private static final String FWK_NOVA_TAG = "com.android.frameworkhwext.nova";
    private static final int HWTHEME_DISABLED = 0;
    private static final String HWTHEME_GET_NOTIFICATION_INFO = "com.huawei.hwtheme.permission.GET_NOTIFICATION_INFO";
    private static final String IPACKAGE_MANAGER_DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final boolean IS_COTA_FEATURE = SystemProperties.getBoolean("ro.config.hw_cota", false);
    private static final boolean IS_HOTA_RESTORE_THEME = SystemProperties.getBoolean("ro.config.hw_hotaRestoreTheme", false);
    private static final boolean IS_REGIONAL_PHONE_FEATURE = SystemProperties.getBoolean("ro.config.region_phone_feature", false);
    private static final boolean IS_SHOW_CUSTUI_DEFAULT = SystemProperties.getBoolean("ro.config.show_custui_default", false);
    private static final boolean IS_SHOW_CUST_HOME_SCREEN = (SystemProperties.getInt("ro.config.show_cust_homescreen", 0) == 1);
    private static final boolean IS_SUPPORT_CLONE_APP = SystemProperties.getBoolean("ro.config.hw_support_clone_app", false);
    private static final String KEY_DISPLAY_MODE = "display_mode";
    public static final String LIVEWALLPAPER_FILE = "livepaper.xml";
    private static final String MESSAGE_CLASS_POSTFIX = "_message.";
    private static final ComponentName NEWSIMPLEHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.newsimpleui.NewSimpleLauncher");
    public static final String NODE_LIVEWALLPAPER_CLASS = "classname";
    public static final String NODE_LIVEWALLPAPER_PACKAGE = "pkgname";
    private static final String NOTIFI_CLASS_POSTFIX = "_notification.";
    static final String PATH_DATASKIN_WALLPAPER = "/data/themes/";
    static final String PATH_DATA_USERS = "/data/system/users/";
    private static final String PROP_WALLPAPER = "ro.config.wallpaper";
    private static final String RINGTONE_CLASS_POSTFIX = "_ringtone.";
    private static final ComponentName SIMPLEHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.simpleui.SimpleUILauncher");
    private static final ComponentName SIMPLELAUNCHERHOME_COMPONENT = new ComponentName("com.huawei.android.simplelauncher", "com.huawei.android.simplelauncher.unihome.UniHomeLauncher");
    private static final int STYLE_DATA = 1;
    private static final String SYSTEM_APP = "android";
    private static final String SYSTEM_APP_HWEXT = "androidhwext";
    static final String TAG = "HwThemeManagerImpl";
    private static final String THEME_FONTS_BASE_PATH = "/data/skin/fonts/";
    private static final ComponentName UNIHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.unihome.UniHomeLauncher");
    static final String WALLPAPER_INFO = "/wallpaper_info.xml";
    private static ResolveInfoUtils resolveInfoUtils = ((ResolveInfoUtils) EasyInvokeFactory.getInvokeUtils(ResolveInfoUtils.class));
    private static boolean sIsDefaultThemeOk = true;
    private static final boolean sIsHwtFlipFontOn = (SystemProperties.getInt("ro.config.hwtheme", 0) != 0);
    private final IHwConfiguration lastHwConfig = initConfigurationEx();
    private Locale lastLocale = Locale.getDefault();
    private List<ComponentState> mDisablelaunchers = new ArrayList();
    private Map<Integer, Integer> mLauncherMap = new HashMap();
    private Object mLockForClone = new Object();
    private IPackageManager mPackageManagerService;
    private HashMap<String, String> mPackageNameMap = new HashMap<>();
    private IconCache.CacheEntry mTempRemovedEntry;

    private static class ComponentState {
        int mSetState = 2;
        ComponentName mlauncherComponent;

        public ComponentState(ComponentName name) {
            this.mlauncherComponent = name;
        }

        public void setComponentEnable(int userId) {
            Log.d(HwThemeManagerImpl.TAG, "updateSimpleUIConfig mlauncherComponent =" + this.mlauncherComponent + ",mSetState=" + this.mSetState);
            try {
                AppGlobals.getPackageManager().setComponentEnabledSetting(this.mlauncherComponent, this.mSetState, 1, userId);
            } catch (Exception e) {
                Log.e(HwThemeManagerImpl.TAG, "setComponentEnabledSetting  because e: " + e);
            }
        }
    }

    public static class LivewallpaperXmlInfo {
        public String mClassName;
        public String mPackageName;
    }

    public IHwConfiguration initConfigurationEx() {
        try {
            return Class.forName("android.content.res.ConfigurationEx").newInstance();
        } catch (Exception e) {
            Log.e("Configuration", "reflection exception is " + e);
            return null;
        }
    }

    public void initForThemeFont(Configuration config) {
        this.lastHwConfig.hwtheme = config.extraConfig.hwtheme;
        if (config.locale != null) {
            this.lastLocale = (Locale) config.locale.clone();
        }
    }

    public HwThemeManagerImpl() {
        initLauncherComponent();
    }

    private void setThemeWallpaper(String fn, Context ctx) {
        File file = new File(fn);
        if (!file.exists()) {
            Log.w(TAG, "pwm setwallpaper stopped");
            return;
        }
        WallpaperManager wpm = (WallpaperManager) ctx.getSystemService("wallpaper");
        InputStream ips = null;
        try {
            ips = new FileInputStream(file);
            wpm.setStream(ips);
            try {
                ips.close();
            } catch (Exception e) {
            }
        } catch (FileNotFoundException e2) {
            Log.w(TAG, "pwm setwallpaper not found err:", e2);
            if (ips != null) {
                ips.close();
            }
        } catch (IOException e3) {
            Log.w(TAG, "pwm setwallpaper io err:", e3);
            if (ips != null) {
                ips.close();
            }
        } catch (Throwable th) {
            if (ips != null) {
                try {
                    ips.close();
                } catch (Exception e4) {
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

    public void setTheme(String theme_path) {
    }

    public boolean installHwTheme(String themePath) {
        return installHwTheme(themePath, false, UserHandle.myUserId());
    }

    public boolean installHwTheme(String themePath, boolean setwallpaper) {
        return installHwTheme(themePath, setwallpaper, UserHandle.myUserId());
    }

    public boolean installHwTheme(String themePath, boolean setwallpaper, int userId) {
        return HwPackageManager.pmInstallHwTheme(themePath, setwallpaper, userId);
    }

    public boolean installDefaultHwTheme(Context ctx) {
        return installDefaultHwTheme(ctx, UserHandle.myUserId());
    }

    public boolean installDefaultHwTheme(Context ctx, int userId) {
        if (isDhomeThemeAdaptBackcolor()) {
            installDefaultDhomeTheme(ctx);
        }
        String defname = getDefaultHwThemePack(ctx);
        Log.w(TAG, "the default theme: " + defname);
        return installHwTheme(defname, false, userId);
    }

    public boolean makeIconCache(boolean clearall) {
        return true;
    }

    public boolean saveIconToCache(Bitmap bitmap, String fn, boolean clearold) {
        return true;
    }

    private String getDefaultHwThemePack(Context ctx) {
        String colors_themes = Settings.System.getString(ctx.getContentResolver(), "colors_themes");
        if (TextUtils.isEmpty(colors_themes)) {
            Log.w(TAG, "colors and themes is empty!");
            return Settings.System.getString(ctx.getContentResolver(), CUST_THEME_NAME);
        }
        String[] colorsAndThemes = colors_themes.split(";");
        HashMap<String, String> mHwColorThemes = new HashMap<>();
        for (String str : colorsAndThemes) {
            String[] color_theme = str.split(",");
            if (color_theme.length != 2 || color_theme[0].isEmpty()) {
                Log.w(TAG, "invalid color and theme : " + str);
            } else {
                mHwColorThemes.put(color_theme[0].toLowerCase(Locale.US), color_theme[1]);
            }
        }
        if (mHwColorThemes.isEmpty()) {
            Log.w(TAG, "has no valid color-theme!");
            return Settings.System.getString(ctx.getContentResolver(), CUST_THEME_NAME);
        }
        String mColor = SystemProperties.get("ro.config.devicecolor");
        String mBackColor = SystemProperties.get("ro.config.backcolor");
        if (mColor == null) {
            return Settings.System.getString(ctx.getContentResolver(), CUST_THEME_NAME);
        }
        String hwThemePath = mHwColorThemes.get(mColor.toLowerCase(Locale.US));
        if (isFileExists(hwThemePath)) {
            Settings.System.putString(ctx.getContentResolver(), CUST_THEME_NAME, hwThemePath);
            Log.w(TAG, "The TP color: " + mColor + ", Theme path: " + hwThemePath);
            return hwThemePath;
        }
        if (hwThemePath == null && !TextUtils.isEmpty(mBackColor)) {
            String hwThemePath2 = mHwColorThemes.get((mColor + "+" + mBackColor).toLowerCase(Locale.US));
            if (isFileExists(hwThemePath2)) {
                Settings.System.putString(ctx.getContentResolver(), CUST_THEME_NAME, hwThemePath2);
                Log.w(TAG, "The group color: " + mGroupColor + ", Theme path: " + hwThemePath2);
                return hwThemePath2;
            }
        }
        return Settings.System.getString(ctx.getContentResolver(), CUST_THEME_NAME);
    }

    public boolean isDhomeThemeAdaptBackcolor() {
        return SystemProperties.getBoolean("ro.config.hw_dhome_theme", false);
    }

    private String getDhomeThemeName(Context ctx) {
        String DOCOMO_DEFAULT_THEME_NAME = Settings.System.getString(ctx.getContentResolver(), "dcm_default_theme");
        String colorThemes = Settings.System.getString(ctx.getContentResolver(), "dcm_color_themes");
        String IS_DOCOMO_MULTI_THEMES = Settings.System.getString(ctx.getContentResolver(), "cust_multi_themes");
        if (IS_DOCOMO_MULTI_THEMES != null && IS_DOCOMO_MULTI_THEMES.equals("true")) {
            colorThemes = Settings.System.getString(ctx.getContentResolver(), "cust_color_multi_themes");
        }
        if (TextUtils.isEmpty(colorThemes)) {
            Log.w(TAG, "dcm colors and themes is empty!");
            return DOCOMO_DEFAULT_THEME_NAME;
        }
        String[] colors_themes = colorThemes.split(";");
        HashMap<String, String> dHomeColorThemes = new HashMap<>();
        for (String str : colors_themes) {
            String[] color_theme = str.split(",");
            if (color_theme.length != 2 || color_theme[0].isEmpty()) {
                Log.w(TAG, "invalid dcm color and theme : " + str);
            } else {
                dHomeColorThemes.put(color_theme[0].toLowerCase(Locale.US), color_theme[1]);
            }
        }
        if (dHomeColorThemes.isEmpty()) {
            Log.w(TAG, "has no valid dcm color_theme!");
            return DOCOMO_DEFAULT_THEME_NAME;
        }
        String mBackColor = SystemProperties.get("ro.config.backcolor");
        if (mBackColor == null || mBackColor.isEmpty()) {
            Log.w(TAG, "has no backcolor property,use default docomo theme");
            return DOCOMO_DEFAULT_THEME_NAME;
        }
        String dcmThemeName = dHomeColorThemes.get(mBackColor.toLowerCase(Locale.US));
        if (dcmThemeName == null || dcmThemeName.isEmpty()) {
            Log.w(TAG, "has no theme adapt to backcolor,use default docomo theme");
            return DOCOMO_DEFAULT_THEME_NAME;
        }
        Settings.System.putString(ctx.getContentResolver(), "dcm_default_theme", dcmThemeName);
        Log.w(TAG, "get docomo default theme OK,dcmThemePath:" + dcmThemeName);
        return dcmThemeName;
    }

    private void linkTheme(String origin, String target) {
        CommandLineUtil.link("system", origin, target);
    }

    private void giveRWToPath(String path) {
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
        String DOCOMO_ALL_THEMES_DIR;
        String DOCOMO_ALL_THEMES_DIR2 = Environment.getDataDirectory() + "/kisekae";
        String DOCOMO_PREFABRICATE_THEMES_DIR = getDhomeThemePath(ctx, "dcm_theme_path", "/cust/docomo/jp/themes/");
        String DOCOMO_DEFAULT_THEME_PATH = DOCOMO_ALL_THEMES_DIR2 + "/kisekae0.kin";
        String deviceThemeName = getDhomeThemeName(ctx);
        String originThemePath = DOCOMO_PREFABRICATE_THEMES_DIR + deviceThemeName;
        File themefolderDir = new File(DOCOMO_ALL_THEMES_DIR2);
        File originThemeFile = new File(originThemePath);
        if (UserHandle.getCallingUserId() != 0) {
            return false;
        }
        String hwThemeFileName = PATH_DATASKIN_WALLPAPER + UserHandle.myUserId();
        File hwThemePath = new File(hwThemeFileName);
        if (!hwThemeFileName.contains("..") && !hwThemePath.exists()) {
            if (!hwThemePath.mkdirs()) {
                return false;
            }
            giveRWToPath(PATH_DATASKIN_WALLPAPER);
        }
        if (themefolderDir.exists() || themefolderDir.mkdir()) {
            giveRWToPath(DOCOMO_ALL_THEMES_DIR2);
            String IS_DOCOMO_MULTI_THEMES = Settings.System.getString(ctx.getContentResolver(), "cust_multi_themes");
            if (IS_DOCOMO_MULTI_THEMES == null || !IS_DOCOMO_MULTI_THEMES.equals("true")) {
                String str = DOCOMO_PREFABRICATE_THEMES_DIR;
                if (originThemeFile.exists()) {
                    linkTheme(originThemePath, DOCOMO_DEFAULT_THEME_PATH);
                }
            } else if (!TextUtils.isEmpty(deviceThemeName)) {
                String[] deviceThemeNames = deviceThemeName.split(":");
                File originThemeFile2 = originThemeFile;
                int i = 0;
                int linkThemeTag = 0;
                while (i < deviceThemeNames.length) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(DOCOMO_PREFABRICATE_THEMES_DIR);
                    String DOCOMO_PREFABRICATE_THEMES_DIR2 = DOCOMO_PREFABRICATE_THEMES_DIR;
                    sb.append(deviceThemeNames[i]);
                    String originMultiThemePath = sb.toString();
                    File originThemeFile3 = new File(originMultiThemePath);
                    if (originThemeFile3.exists()) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(DOCOMO_ALL_THEMES_DIR2);
                        DOCOMO_ALL_THEMES_DIR = DOCOMO_ALL_THEMES_DIR2;
                        sb2.append("/kisekae");
                        sb2.append(linkThemeTag);
                        sb2.append(".kin");
                        String DOCOMO_MULTI_THEME_PATH = sb2.toString();
                        linkTheme(originMultiThemePath, DOCOMO_MULTI_THEME_PATH);
                        linkThemeTag++;
                        String str2 = DOCOMO_MULTI_THEME_PATH;
                    } else {
                        DOCOMO_ALL_THEMES_DIR = DOCOMO_ALL_THEMES_DIR2;
                    }
                    i++;
                    originThemeFile2 = originThemeFile3;
                    DOCOMO_PREFABRICATE_THEMES_DIR = DOCOMO_PREFABRICATE_THEMES_DIR2;
                    DOCOMO_ALL_THEMES_DIR2 = DOCOMO_ALL_THEMES_DIR;
                    Context context = ctx;
                }
                String str3 = DOCOMO_PREFABRICATE_THEMES_DIR;
                originThemeFile = originThemeFile2;
            } else {
                String str4 = DOCOMO_PREFABRICATE_THEMES_DIR;
            }
            if (DOCOMO_DEFAULT_THEME_PATH == null || !isFileExists(DOCOMO_DEFAULT_THEME_PATH)) {
                Log.w(TAG, DOCOMO_DEFAULT_THEME_PATH + " not exist");
                return false;
            }
            saveDhomeThemeInfo(originThemeFile, DOCOMO_DEFAULT_THEME_PATH);
            return true;
        }
        Log.w(TAG, "mkdir /data/kisekae fail !!!");
        return false;
    }

    private void saveDhomeThemeInfo(File originThemeFile, String defaultThemePath) {
        if (originThemeFile == null || TextUtils.isEmpty(defaultThemePath)) {
            Log.e(TAG, "saveDhomeThemeInfo :: origin theme file or default theme path invalid.");
            return;
        }
        Properties properties = new Properties();
        properties.put("default_theme_path", defaultThemePath);
        String themeName = originThemeFile.getName();
        properties.put("current_docomo_theme_type", themeName.contains("kisekae_Cute.kin") ? "silver" : themeName.contains("village.kin") ? "gold" : "unknow");
        properties.put("current_docomo_theme_name", themeName);
        properties.put("deleted_docomo_themes", themeName.contains("kisekae_Cute.kin") ? "village.kin" : themeName.contains("village.kin") ? "kisekae_Cute.kin" : "unknow");
        FileOutputStream fos = null;
        String deviceThemeInfoPath = defaultThemePath.substring(0, defaultThemePath.lastIndexOf(47)) + "/DeviceThemeInfo.properties";
        try {
            fos = new FileOutputStream(deviceThemeInfoPath);
            properties.store(fos, null);
            giveRWToPath(deviceThemeInfoPath);
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

    public void updateConfiguration(boolean changeuser) {
        try {
            Configuration curConfig = new Configuration();
            curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            curConfig.extraConfig.setConfigItem(1, curConfig.extraConfig.getConfigItem(1) + 1);
            ActivityManagerNative.getDefault().updateConfiguration(curConfig);
            if ((!HwPCUtils.enabled() || ActivityThread.currentActivityThread() == null || !HwPCUtils.isValidExtDisplayId(ActivityThread.currentActivityThread().getDisplayId())) && !changeuser) {
                updateOverlaysThems();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "updateConfiguration occurs exception e.msg = " + e.getMessage());
        }
    }

    private void updateOverlaysThems() {
        int newUserId = UserHandle.myUserId();
        String themePath = "persist.deep.theme_" + newUserId;
        HwThemeManager.setOverLayThemePath(themePath);
        HwThemeManager.setOverLayThemeType(SystemProperties.get(themePath));
        IOverlayManager overlayManager = IOverlayManager.Stub.asInterface(ServiceManager.getService("overlay"));
        if (overlayManager != null) {
            List<OverlayInfo> frameworkhwextinfos = null;
            List<OverlayInfo> frameworkinfos = null;
            try {
                frameworkhwextinfos = overlayManager.getOverlayInfosForTarget(SYSTEM_APP_HWEXT, newUserId);
                frameworkinfos = overlayManager.getOverlayInfosForTarget("android", newUserId);
            } catch (RemoteException e) {
                Log.e(TAG, "fail get fwk overlayinfos");
            }
            int size = 0;
            if (frameworkhwextinfos != null) {
                size = frameworkhwextinfos.size();
                if (frameworkinfos != null) {
                    frameworkhwextinfos.addAll(frameworkinfos);
                    size = frameworkhwextinfos.size();
                }
            }
            int size2 = size;
            int i = 0;
            while (true) {
                int i2 = i;
                if (i2 < size2) {
                    String packageName = null;
                    if (frameworkhwextinfos != null) {
                        packageName = frameworkhwextinfos.get(i2).packageName;
                    }
                    String packageName2 = packageName;
                    if (!TextUtils.isEmpty(packageName2)) {
                        boolean isDarkTheme = HwThemeManager.isDeepDarkTheme();
                        boolean isHonorProduct = HwThemeManager.isHonorProduct();
                        if ((!FWK_NOVA_TAG.equals(packageName2) || !HwThemeManager.isNovaProduct()) && (!FWK_HONOR_TAG.equals(packageName2) || !isHonorProduct)) {
                            if (!isDarkTheme && newUserId != 0 && packageName2.contains("frameworkhwext")) {
                                try {
                                    overlayManager.setEnabled(packageName2, false, 0);
                                } catch (RemoteException e2) {
                                    RemoteException remoteException = e2;
                                    Log.e(TAG, "fail set enable the primary user");
                                }
                            }
                            if (isDarkTheme || isHonorProduct || !packageName2.contains("frameworkhwext")) {
                                if (isDarkTheme && packageName2.contains("dark")) {
                                    try {
                                        overlayManager.setEnabledExclusive(packageName2, true, newUserId);
                                        if (newUserId != 0) {
                                            overlayManager.setEnabledExclusive(packageName2, true, 0);
                                        }
                                    } catch (RemoteException e3) {
                                        Log.e(TAG, "fail set dark account access");
                                    }
                                }
                                if (!isDarkTheme && isHonorProduct && packageName2.contains("honor")) {
                                    try {
                                        overlayManager.setEnabledExclusive(packageName2, true, newUserId);
                                        if (newUserId != 0) {
                                            overlayManager.setEnabledExclusive(packageName2, true, 0);
                                        }
                                    } catch (RemoteException e4) {
                                        Log.e(TAG, "fail set honor account access");
                                    }
                                }
                            } else {
                                try {
                                    overlayManager.setEnabled(packageName2, false, newUserId);
                                } catch (RemoteException e5) {
                                    RemoteException remoteException2 = e5;
                                    Log.e(TAG, "fail set false account access");
                                }
                            }
                        } else {
                            try {
                                overlayManager.setEnabledExclusive(packageName2, true, newUserId);
                            } catch (RemoteException e6) {
                                RemoteException remoteException3 = e6;
                                Log.e(TAG, "fail set " + packageName2 + " account access");
                            }
                        }
                    }
                    i = i2 + 1;
                } else {
                    Log.i(TAG, "HwThemeManagerImpl#updateOverlaysThems end");
                    return;
                }
            }
        }
    }

    public void updateConfiguration() {
        updateConfiguration(false);
    }

    public Resources getResources(AssetManager assets, DisplayMetrics dm, Configuration config, DisplayAdjustments displayAdjustments, IBinder token) {
        HwResources hwResources = new HwResources(assets, dm, config, displayAdjustments, token);
        return hwResources;
    }

    public Resources getResources() {
        return new HwResources();
    }

    public Resources getResources(boolean system) {
        return new HwResources(system);
    }

    public Resources getResources(ClassLoader classLoader) {
        return new HwResources(classLoader);
    }

    public AbsResourcesImpl getHwResourcesImpl() {
        return new HwResourcesImpl();
    }

    public InputStream getDefaultWallpaperIS(Context context, int userId) {
        InputStream is = null;
        WallpaperManager wallpaperManager = (WallpaperManager) context.getSystemService("wallpaper");
        try {
            String path = SystemProperties.get(PROP_WALLPAPER);
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
            is = context.getResources().openRawResource(17302116);
        }
        return is;
    }

    public void updateIconCache(PackageItemInfo packageItemInfo, String name, String packageName, int icon, int packageIcon) {
        String resPackageName;
        int id = icon != 0 ? icon : packageIcon;
        String idAndPackageName = id + "#" + resPackageName;
        if (!IconCache.contains(idAndPackageName)) {
            String tmpName = name != null ? name : resPackageName;
            String lc = tmpName != null ? tmpName.toLowerCase() : null;
            if (lc != null && lc.indexOf("shortcut") < 0 && lc.indexOf(".cts") < 0) {
                IconCache.CacheEntry ce = new IconCache.CacheEntry();
                ce.name = tmpName;
                ce.type = icon != 0 ? 1 : 0;
                IconCache.add(idAndPackageName, ce);
            }
        }
    }

    public void updateResolveInfoIconCache(ResolveInfo resolveInfo, int icon, String resolvePackageName) {
        if (!(icon == 0 || resolveInfo == null)) {
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
        String resPackageName = getResourcePackageName(packageName, icon != 0 ? icon : packageIcon);
        String idAndPackageName = id + "#" + resPackageName;
        if (IconCache.contains(idAndPackageName)) {
            this.mTempRemovedEntry = IconCache.get(idAndPackageName);
            IconCache.remove(idAndPackageName);
        }
    }

    public void restoreIconCache(String packageName, int icon) {
        if (this.mTempRemovedEntry != null) {
            IconCache.add(icon + "#" + packageName, this.mTempRemovedEntry);
            this.mTempRemovedEntry = null;
        }
    }

    public int getThemeColor(int[] data, int index, TypedValue value, Resources resources, boolean flag) {
        TypedValue mValue = value;
        if (!flag || mValue.resourceId == 0) {
            return data[index + 1];
        }
        try {
            return resources.getColor(mValue.resourceId);
        } catch (Resources.NotFoundException e) {
            return data[index + 1];
        }
    }

    public int getHwThemeLauncherIconSize(ActivityManager am, Resources resources) {
        Resources res = resources;
        int configIconSize = SystemProperties.getInt("ro.config.app_big_icon_size", -1);
        int multiResSize = SystemProperties.getInt("persist.sys.res.icon_size", -1);
        if (configIconSize > 0 && multiResSize > 0) {
            configIconSize = multiResSize;
        }
        return configIconSize == -1 ? (int) res.getDimension(34472064) : configIconSize;
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

    public IHwConfiguration initHwConfiguration() {
        return new ConfigurationEx();
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

    public void applyDefaultHwTheme(boolean checkState, Context mContext) {
        applyDefaultHwTheme(checkState, mContext, UserHandle.myUserId());
    }

    public void applyDefaultHwTheme(boolean checkState, Context mContext, int userId) {
        boolean skinExist = new File(HwThemeManager.HWT_PATH_THEME + "/" + userId).exists();
        boolean installFlagExist = isFileExists(HwThemeManager.HWT_PATH_SKIN_INSTALL_FLAG);
        boolean isSet = !skinExist || (checkState && !sIsDefaultThemeOk) || installFlagExist;
        boolean isThemeInvalid = false;
        if (skinExist && !installFlagExist && checkState) {
            isThemeInvalid = isThemeInvalid(userId);
        }
        if (isSet || isThemeInvalid) {
            setIsDefaultThemeOk(installDefaultHwTheme(mContext, userId));
            isHotaRestoreThemeOrFirstBoot(mContext);
        } else if ((isSupportThemeRestore() && isCustChange(mContext) && !isThemeChange(mContext)) || isHotaRestoreThemeOrFirstBoot(mContext)) {
            deleteWallpaperInfoFile(userId);
            installCustomerTheme(mContext, userId);
            HwThemeManager.updateConfiguration();
        } else if (isEcotaChangeTheme(mContext)) {
            deleteWallpaperInfoFile(userId);
            deleteSystemWallpaper(userId);
            copyFolder(ECOTA_WALLPAPER_PATH, HwThemeManager.HWT_PATH_THEME + "/" + userId + "/wallpaper");
        }
    }

    private static void setIsDefaultThemeOk(boolean isOk) {
        sIsDefaultThemeOk = isOk;
    }

    public void addSimpleUIConfig(PackageParser.Activity activity) {
        if (activity.metaData.getBoolean("simpleuimode", false)) {
            activity.info.configChanges |= StubController.PERMISSION_SMSLOG_WRITE;
        }
    }

    private IPackageManager getPackageManager() {
        if (this.mPackageManagerService == null) {
            this.mPackageManagerService = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        }
        return this.mPackageManagerService;
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
        int simpleuiVal = Settings.System.getIntForUser(cr, HwSettings.System.SIMPLEUI_MODE, 0, ActivityManager.getCurrentUser());
        int launcherConfig = Settings.System.getInt(cr, "hw_launcher_desktop_mode", 0);
        Log.d(TAG, "updateSimpleUIConfig simpleuiVal =" + simpleuiVal + ",launcherConfig=" + launcherConfig);
        int i = 1;
        if (simpleuiVal == 0 || (IS_REGIONAL_PHONE_FEATURE && Settings.Secure.getInt(cr, "user_setup_complete", 0) != 1)) {
            if (launcherConfig != 0) {
                i = launcherConfig;
            }
            simpleuiVal = i;
        }
        config.extraConfig.setConfigItem(2, simpleuiVal);
        try {
            enableLaunchers(this.mLauncherMap.get(Integer.valueOf(simpleuiVal)).intValue(), userId);
        } catch (Exception e) {
            Log.d(TAG, "retrieveSimpleUIConfig enableLaunchers e=" + e);
        }
    }

    public void updateSimpleUIConfig(ContentResolver cr, Configuration config, int configChanges) {
        if ((65536 & configChanges) != 0) {
            Log.w(TAG, "updateSimpleUIConfig SIMPLEUI_MODE putIntForUser =" + config.extraConfig.getConfigItem(2));
            Settings.System.putIntForUser(cr, HwSettings.System.SIMPLEUI_MODE, config.extraConfig.getConfigItem(2), ActivityManager.getCurrentUser());
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
            return res.getThemeBitmap(outValue, id, padding);
        }
        return null;
    }

    public Bitmap getThemeBitmap(Resources res, int id) {
        return getThemeBitmap(res, id, null);
    }

    private static boolean isNoThemeFont() {
        File fontdir = new File(THEME_FONTS_BASE_PATH, "");
        if (!fontdir.exists() || fontdir.list() == null || fontdir.list().length == 0) {
            return true;
        }
        return false;
    }

    public void setThemeFont() {
        if (sIsHwtFlipFontOn) {
            Canvas.freeCaches();
            Canvas.freeTextLayoutCaches();
        }
    }

    public boolean setThemeFontOnConfigChg(Configuration newConfig) {
        if (sIsHwtFlipFontOn) {
            boolean isHwThemeChanged = false;
            if (!(this.lastHwConfig == null || newConfig == null)) {
                if ((32768 & this.lastHwConfig.updateFrom(newConfig.extraConfig)) != 0) {
                    isHwThemeChanged = true;
                }
            }
            boolean isLocaleChanged = false;
            if (!(newConfig == null || newConfig.locale == null || (this.lastLocale != null && this.lastLocale.equals(newConfig.locale)))) {
                this.lastLocale = newConfig.locale != null ? (Locale) newConfig.locale.clone() : null;
                isLocaleChanged = true;
            }
            if (isHwThemeChanged || isLocaleChanged) {
                setThemeFont();
                return true;
            }
        }
        return false;
    }

    public boolean isTargetFamily(String familyName) {
        return familyName != null && familyName.equals("chnfzxh");
    }

    public boolean shouldUseAdditionalChnFont(String familyName) {
        if (isTargetFamily(familyName)) {
            String curLang = Locale.getDefault().getLanguage();
            if (isNoThemeFont() && (curLang.contains("zh") || curLang.contains("en"))) {
                return true;
            }
        }
        return false;
    }

    public Bitmap generateBitmap(Context context, Bitmap bm, int width, int height) {
        Bitmap bitmap = bm;
        int i = width;
        int i2 = height;
        if (bitmap == null) {
            return null;
        }
        WindowManager wm = (WindowManager) context.getSystemService(FreezeScreenScene.WINDOW_PARAM);
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = wm.getDefaultDisplay();
        display.getMetrics(metrics);
        bitmap.setDensity(metrics.noncompatDensityDpi);
        Point size = new Point();
        display.getRealSize(size);
        int max = size.x > size.y ? size.x : size.y;
        int min = size.x < size.y ? size.x : size.y;
        if (i <= 0 || i2 <= 0) {
        } else if ((bm.getWidth() == i && bm.getHeight() == i2) || ((bm.getWidth() == min && bm.getHeight() == max) || ((bm.getWidth() == max && bm.getHeight() == max) || (bm.getWidth() == max && bm.getHeight() == min)))) {
            WindowManager windowManager = wm;
        } else {
            try {
                Bitmap newbm = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
                if (newbm == null) {
                    try {
                        Log.w(TAG, "Can't generate default bitmap, newbm = null");
                        return bitmap;
                    } catch (OutOfMemoryError e) {
                        e = e;
                        WindowManager windowManager2 = wm;
                        Log.w(TAG, "Can't generate default bitmap", e);
                        return bitmap;
                    }
                } else {
                    newbm.setDensity(metrics.noncompatDensityDpi);
                    Canvas c = new Canvas(newbm);
                    Rect targetRect = new Rect();
                    targetRect.right = bm.getWidth();
                    targetRect.bottom = bm.getHeight();
                    int deltaw = i - targetRect.right;
                    int deltah = i2 - targetRect.bottom;
                    if (deltaw > 0 || deltah > 0) {
                        WindowManager windowManager3 = wm;
                        try {
                            float tempWidth = ((float) i) / ((float) targetRect.right);
                            float tempHeight = ((float) i2) / ((float) targetRect.bottom);
                            float scale = tempWidth > tempHeight ? tempWidth : tempHeight;
                            float f = tempWidth;
                            targetRect.right = (int) (((float) targetRect.right) * scale);
                            targetRect.bottom = (int) (((float) targetRect.bottom) * scale);
                            deltaw = i - targetRect.right;
                            deltah = i2 - targetRect.bottom;
                        } catch (OutOfMemoryError e2) {
                            e = e2;
                            Log.w(TAG, "Can't generate default bitmap", e);
                            return bitmap;
                        }
                    } else {
                        WindowManager windowManager4 = wm;
                    }
                    targetRect.offset(deltaw / 2, deltah / 2);
                    Paint paint = new Paint();
                    paint.setFilterBitmap(true);
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                    c.drawBitmap(bitmap, null, targetRect, paint);
                    bm.recycle();
                    return newbm;
                }
            } catch (OutOfMemoryError e3) {
                e = e3;
                WindowManager windowManager5 = wm;
                Log.w(TAG, "Can't generate default bitmap", e);
                return bitmap;
            }
        }
        return bitmap;
    }

    private String getResourcePackageName(String packageName, int icon) {
        String name = getResourcePackageNameFromMap(packageName);
        if (name != null) {
            return name;
        }
        String result = HwPackageManager.getResourcePackageNameByIcon(packageName, icon, UserHandle.myUserId());
        if (result != null && !result.equals("")) {
            addResourcePackageName(packageName, result);
        }
        return result;
    }

    public Drawable getJoinBitmap(Context context, Drawable srcDraw, int backgroundId) {
        context.enforceCallingOrSelfPermission(HWTHEME_GET_NOTIFICATION_INFO, "getJoinBitmap");
        Resources r = context.getResources();
        if (r != null) {
            return r.getImpl().getHwResourcesImpl().getJoinBitmap(srcDraw, backgroundId);
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:0x015b, code lost:
        return r6;
     */
    public Drawable getClonedDrawable(Context context, Drawable drawable) {
        int cloneHeight;
        int cloneWidth;
        Paint mPaint;
        Context context2 = context;
        Drawable drawable2 = drawable;
        if (!IS_SUPPORT_CLONE_APP) {
            return drawable2;
        }
        Drawable clone = context.getResources().getDrawable(33751231);
        if (clone == null) {
            return drawable2;
        }
        if (drawable2 == null) {
            clone.setColorFilter(context2.getColor(33882311), PorterDuff.Mode.SRC_IN);
            clone.setBounds(0, 0, clone.getIntrinsicWidth(), clone.getIntrinsicHeight());
            return clone;
        }
        Float markBgRatio = Float.valueOf(Float.parseFloat(context.getResources().getString(34472193)));
        Float markBgPercentage = Float.valueOf(Float.parseFloat(context.getResources().getString(34472194)));
        int srcWidth = drawable.getIntrinsicWidth();
        int srcHeight = drawable.getIntrinsicHeight();
        int tempSrcWidth = (int) (((float) srcWidth) * markBgPercentage.floatValue());
        int tempSrcHeight = (int) (((float) srcHeight) * markBgPercentage.floatValue());
        int markBgRadius = tempSrcWidth > tempSrcHeight ? tempSrcHeight : tempSrcWidth;
        if (clone.getIntrinsicWidth() >= clone.getIntrinsicHeight()) {
            cloneWidth = (int) (((float) markBgRadius) / markBgRatio.floatValue());
            cloneHeight = (int) (((1.0d * ((double) cloneWidth)) * ((double) clone.getIntrinsicHeight())) / ((double) clone.getIntrinsicWidth()));
        } else {
            cloneHeight = (int) (((float) markBgRadius) / markBgRatio.floatValue());
            cloneWidth = (int) (((1.0d * ((double) cloneHeight)) * ((double) clone.getIntrinsicWidth())) / ((double) clone.getIntrinsicHeight()));
        }
        synchronized (this.mLockForClone) {
            try {
                Bitmap newBitMap = Bitmap.createBitmap(srcWidth, srcWidth, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(newBitMap);
                Float f = markBgRatio;
                try {
                    drawable2.setBounds(0, 0, srcWidth, srcWidth);
                    drawable2.draw(canvas);
                    mPaint = new Paint();
                    Float f2 = markBgPercentage;
                } catch (Throwable th) {
                    th = th;
                    Float f3 = markBgPercentage;
                    int i = srcWidth;
                    int i2 = srcHeight;
                    int i3 = tempSrcWidth;
                    throw th;
                }
                try {
                    int i4 = tempSrcWidth;
                } catch (Throwable th2) {
                    th = th2;
                    int i5 = srcWidth;
                    int i6 = srcHeight;
                    int i7 = tempSrcWidth;
                    throw th;
                }
                try {
                    mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
                    mPaint.setAntiAlias(true);
                    canvas.save();
                    mPaint.setColor(context2.getColor(33882311));
                    canvas.translate((float) (srcWidth - markBgRadius), (float) (srcHeight - markBgRadius));
                    int i8 = srcWidth;
                    int i9 = srcHeight;
                    canvas.drawCircle(((float) markBgRadius) / 2.0f, ((float) markBgRadius) / 2.0f, ((float) markBgRadius) / 2.0f, mPaint);
                    canvas.translate(((float) (markBgRadius - cloneWidth)) / 2.0f, ((float) (markBgRadius - cloneHeight)) / 2.0f);
                    canvas.scale((((float) cloneWidth) * 1.0f) / ((float) clone.getIntrinsicWidth()), (((float) cloneHeight) * 1.0f) / ((float) clone.getIntrinsicHeight()));
                    clone.setBounds(0, 0, clone.getIntrinsicWidth(), clone.getIntrinsicHeight());
                    clone.draw(canvas);
                    canvas.restore();
                    mPaint.setXfermode(null);
                    BitmapDrawable mergedDrawable = new BitmapDrawable(context.getResources(), newBitMap);
                    if (drawable2 instanceof BitmapDrawable) {
                        mergedDrawable.setTargetDensity(((BitmapDrawable) drawable2).getBitmap().getDensity());
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                Float f4 = markBgRatio;
                Float f5 = markBgPercentage;
                int i10 = srcWidth;
                int i11 = srcHeight;
                int i12 = tempSrcWidth;
                throw th;
            }
        }
    }

    public Drawable getHwBadgeDrawable(Notification notification, Context context, Drawable drawable) {
        if (!notification.extras.getBoolean("com.huawei.isIntentProtectedApp")) {
            return null;
        }
        Drawable trustSpace = context.getResources().getDrawable(33751237);
        trustSpace.setColorFilter(context.getColor(33882311), PorterDuff.Mode.SRC_IN);
        trustSpace.setBounds(0, 0, trustSpace.getIntrinsicWidth(), trustSpace.getIntrinsicHeight());
        return trustSpace;
    }

    public String getDefaultLiveWallpaper(int userId) {
        Document document = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("/data/themes//" + userId, LIVEWALLPAPER_FILE));
        } catch (ParserConfigurationException e) {
            Log.e(TAG, " ParserConfigurationException " + livepaperpath);
        } catch (SAXException e2) {
            Log.e(TAG, "SAXException " + livepaperpath);
        } catch (IOException e3) {
            Log.e(TAG, "IOException " + livepaperpath);
        } catch (Exception e4) {
            Log.e(TAG, "Exception " + livepaperpath);
        }
        if (document == null) {
            return null;
        }
        Element rootElement = document.getDocumentElement();
        if (rootElement == null) {
            return null;
        }
        LivewallpaperXmlInfo livewallpaperInfo = new LivewallpaperXmlInfo();
        NodeList itemNodes = rootElement.getChildNodes();
        int length = itemNodes.getLength();
        for (int i = 0; i < length; i++) {
            Node itemNode = itemNodes.item(i);
            if (itemNode.getNodeType() == 1) {
                if (NODE_LIVEWALLPAPER_PACKAGE.equals(itemNode.getNodeName())) {
                    livewallpaperInfo.mPackageName = itemNode.getTextContent();
                } else if (NODE_LIVEWALLPAPER_CLASS.equals(itemNode.getNodeName())) {
                    livewallpaperInfo.mClassName = itemNode.getTextContent();
                }
            }
        }
        StringBuffer livewallpaperStr = new StringBuffer();
        livewallpaperStr.append(livewallpaperInfo.mPackageName);
        livewallpaperStr.append("/");
        livewallpaperStr.append(livewallpaperInfo.mClassName);
        Log.e(TAG, "HwThemeManager#getDefaultLiveWallpaper livewallpaperStr =" + livewallpaperStr.toString());
        return livewallpaperStr.toString();
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
                String currentVendorCountry = SystemProperties.get("ro.hw.custPath", "");
                if (originalVendorCountry == null) {
                    Settings.Secure.putString(context.getContentResolver(), "vendor_country", currentVendorCountry);
                    return false;
                } else if (!originalVendorCountry.equals(currentVendorCountry)) {
                    Settings.Secure.putString(context.getContentResolver(), "vendor_country", currentVendorCountry);
                    return true;
                }
            } else if (IS_COTA_FEATURE) {
                String originalCotaVersion = Settings.Secure.getString(context.getContentResolver(), "cotaVersion");
                String cotaVersion = SystemProperties.get("ro.product.CotaVersion", "");
                if (originalCotaVersion == null) {
                    Settings.Secure.putString(context.getContentResolver(), "cotaVersion", cotaVersion);
                    return false;
                } else if (!cotaVersion.equals(originalCotaVersion)) {
                    Settings.Secure.putString(context.getContentResolver(), "cotaVersion", cotaVersion);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "check cust Exception e : " + e);
        }
        return false;
    }

    public static boolean isThemeChange(Context context) {
        return "true".equals(Settings.Secure.getString(context.getContentResolver(), "isUserChangeTheme"));
    }

    private boolean isThemeInvalid(int userId) {
        long startTime = SystemClock.elapsedRealtime();
        String themePath = HwThemeManager.HWT_PATH_THEME + "/" + userId;
        String defaultWallpaperFileName = themePath + "/wallpaper/home_wallpaper_0.jpg";
        if (!FileUtil.isFileExists(defaultWallpaperFileName) || !FileUtil.isFileContentAllZero(defaultWallpaperFileName)) {
            Log.d(TAG, "check theme took " + (SystemClock.elapsedRealtime() - startTime));
            return false;
        }
        Log.d(TAG, "Theme is not valid");
        return true;
    }

    private boolean isHotaRestoreThemeOrFirstBoot(Context context) {
        if (IS_HOTA_RESTORE_THEME) {
            try {
                String originalVersion = Settings.Secure.getString(context.getContentResolver(), "custVersion");
                String buildVersion = SystemProperties.get("ro.build.display.id", "");
                String custCVersion = SystemProperties.get("ro.product.CustCVersion", "");
                String custDVersion = SystemProperties.get("ro.product.CustDVersion", "");
                if (!(buildVersion + custCVersion + custDVersion).equals(originalVersion)) {
                    ContentResolver contentResolver = context.getContentResolver();
                    Settings.Secure.putString(contentResolver, "custVersion", buildVersion + custCVersion + custDVersion);
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "check cust Exception e : " + e);
            }
        }
        return false;
    }

    private boolean isEcotaChangeTheme(Context context) {
        if (TextUtils.isEmpty(SystemProperties.get("ro.product.EcotaVersion", "")) || isThemeChange(context)) {
            return false;
        }
        if (isEcotaHomeWallpaperExist() || isEcotaUnlockWallpaperExist()) {
            try {
                Settings.Secure.putString(context.getContentResolver(), "isUserChangeTheme", "true");
                Log.i(TAG, "isEcotaChangeTheme == true");
                return true;
            } catch (SecurityException e) {
                Log.e(TAG, "isEcotaChangeTheme SecurityException");
            } catch (Exception e2) {
                Log.e(TAG, "isEcotaChangeTheme Exception");
            }
        }
        return false;
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
        String liveWallpaperPath = PATH_DATASKIN_WALLPAPER + userId + "/" + LIVEWALLPAPER_FILE;
        if (new File(liveWallpaperPath).exists()) {
            CommandLineUtil.rm("system", liveWallpaperPath);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0074, code lost:
        r8 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0075, code lost:
        r9 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0079, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x007a, code lost:
        r12 = r9;
        r9 = r8;
        r8 = r12;
     */
    private void copyFolder(String oldPath, String newPath) {
        FileInputStream input;
        FileOutputStream output;
        Throwable th;
        Throwable th2;
        String[] files = new File(oldPath).list();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File tempFile = new File(oldPath + files[i]);
                if (tempFile.isFile()) {
                    try {
                        input = new FileInputStream(tempFile);
                        output = new FileOutputStream(newPath + "/" + tempFile.getName().toString());
                        byte[] byteData = new byte[5120];
                        while (true) {
                            int read = input.read(byteData);
                            int len = read;
                            if (read == -1) {
                                break;
                            }
                            output.write(byteData, 0, len);
                        }
                        output.flush();
                        $closeResource(null, output);
                        $closeResource(null, input);
                    } catch (IOException e) {
                        Log.e(TAG, "copyFolder IOException");
                    } catch (Exception e2) {
                        Log.e(TAG, "copyFolder Exception");
                    } catch (Throwable th3) {
                        $closeResource(r6, input);
                        throw th3;
                    }
                }
            }
            return;
        }
        return;
        $closeResource(th, output);
        throw th2;
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }
}
