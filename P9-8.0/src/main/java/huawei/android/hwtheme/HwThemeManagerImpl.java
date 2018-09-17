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
import android.content.om.IOverlayManager.Stub;
import android.content.om.OverlayInfo;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageParser.Activity;
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
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.hwtheme.HwThemeManager.IHwThemeManager;
import android.os.Environment;
import android.os.FreezeScreenScene;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.DisplayAdjustments;
import android.view.WindowManager;
import com.huawei.android.hwutil.CommandLineUtil;
import com.huawei.hsm.permission.StubController;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.android.hwutil.IconCache;
import huawei.android.hwutil.IconCache.CacheEntry;
import huawei.android.provider.HwSettings;
import huawei.com.android.internal.widget.HwLockPatternUtils;
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

public class HwThemeManagerImpl implements IHwThemeManager {
    private static final String ALARM_CLASS_POSTFIX = "_alarm.";
    static final String BLURRED_WALLPAPER = "/blurwallpaper";
    private static final String CALENDAR_CLASS_POSTFIX = "_calendar.";
    static final String CURRENT_HOMEWALLPAPER_NAME = "home_wallpaper_0.jpg";
    static final String CURRENT_HOMEWALLPAPER_NAME_PNG = "home_wallpaper_0.png";
    static final String CUST_THEME_NAME = "hw_def_theme";
    static final String CUST_WALLPAPER = "/data/cust/wallpaper/wallpaper1.jpg";
    static final String CUST_WALLPAPER_DIR = "/data/cust/wallpaper/";
    static final String CUST_WALLPAPER_FILE_NAME = "wallpaper1.jpg";
    static final boolean DEBUG = false;
    private static final String DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final ComponentName DOCOMOHOME_COMPONENT = new ComponentName("com.nttdocomo.android.dhome", "com.nttdocomo.android.dhome.HomeActivity");
    private static final ComponentName DRAWERHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.drawer.DrawerLauncher");
    private static final String EMAIL_CLASS_POSTFIX = "_email.";
    private static final int HWTHEME_DISABLED = 0;
    private static final String HWTHEME_GET_NOTIFICATION_INFO = "com.huawei.hwtheme.permission.GET_NOTIFICATION_INFO";
    private static final String IPACKAGE_MANAGER_DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final boolean IS_COTA_FEATURE = SystemProperties.getBoolean("ro.config.hw_cota", false);
    private static final boolean IS_REGIONAL_PHONE_FEATURE = SystemProperties.getBoolean("ro.config.region_phone_feature", false);
    private static final boolean IS_SHOW_DCMUI = SystemProperties.getBoolean("ro.config.hw_show_dcmui", false);
    private static final boolean IS_SUPPORT_CLONE_APP = SystemProperties.getBoolean("ro.config.hw_support_clone_app", false);
    private static final String KEY_DISPLAY_MODE = "display_mode";
    public static final String LIVEWALLPAPER_FILE = "livepaper.xml";
    private static final String MESSAGE_CLASS_POSTFIX = "_message.";
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
    private static final String SYSTEM_APP_HWEXT = "androidhwext";
    static final String TAG = "HwThemeManagerImpl";
    private static final String THEME_FONTS_BASE_PATH = "/data/skin/fonts/";
    private static final ComponentName UNIHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.unihome.UniHomeLauncher");
    static final String WALLPAPER_INFO = "/wallpaper_info.xml";
    private static ResolveInfoUtils resolveInfoUtils = ((ResolveInfoUtils) EasyInvokeFactory.getInvokeUtils(ResolveInfoUtils.class));
    private static boolean sIsDefaultThemeOk = true;
    private static final boolean sIsHwtFlipFontOn = (SystemProperties.getInt("ro.config.hwtheme", 0) != 0);
    private static final int transaction_pmGetResourcePackageName = 1004;
    private final IHwConfiguration lastHwConfig = initConfigurationEx();
    private Locale lastLocale = Locale.getDefault();
    private List<ComponentState> mDisablelaunchers = new ArrayList();
    private Map<Integer, Integer> mLauncherMap = new HashMap();
    private Object mLockForClone = new Object();
    private IPackageManager mPackageManagerService;
    private HashMap<String, String> mPackageNameMap = new HashMap();
    private CacheEntry mTempRemovedEntry;

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
        IHwConfiguration tmpExtraConfig = null;
        try {
            return (IHwConfiguration) Class.forName("android.content.res.ConfigurationEx").newInstance();
        } catch (Exception e) {
            Log.e("Configuration", "reflection exception is " + e);
            return tmpExtraConfig;
        }
    }

    public void initForThemeFont(Configuration config) {
        ((ConfigurationEx) this.lastHwConfig).hwtheme = ((ConfigurationEx) config.extraConfig).hwtheme;
        if (config.locale != null) {
            this.lastLocale = (Locale) config.locale.clone();
        }
    }

    public HwThemeManagerImpl() {
        initLauncherComponent();
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x004e A:{SYNTHETIC, Splitter: B:26:0x004e} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003c A:{SYNTHETIC, Splitter: B:19:0x003c} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0057 A:{SYNTHETIC, Splitter: B:31:0x0057} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setThemeWallpaper(String fn, Context ctx) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        File file = new File(fn);
        if (file.exists()) {
            WallpaperManager wpm = (WallpaperManager) ctx.getSystemService("wallpaper");
            InputStream ips = null;
            try {
                InputStream ips2 = new FileInputStream(file);
                try {
                    wpm.setStream(ips2);
                    if (ips2 != null) {
                        try {
                            ips2.close();
                        } catch (Exception e3) {
                        }
                    }
                    ips = ips2;
                } catch (FileNotFoundException e4) {
                    e = e4;
                    ips = ips2;
                    Log.w(TAG, "pwm setwallpaper not found err:", e);
                    if (ips != null) {
                        try {
                            ips.close();
                        } catch (Exception e5) {
                        }
                    }
                    return;
                } catch (IOException e6) {
                    e2 = e6;
                    ips = ips2;
                    try {
                        Log.w(TAG, "pwm setwallpaper io err:", e2);
                        if (ips != null) {
                            try {
                                ips.close();
                            } catch (Exception e7) {
                            }
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (ips != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    ips = ips2;
                    if (ips != null) {
                        try {
                            ips.close();
                        } catch (Exception e8) {
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e9) {
                e = e9;
                Log.w(TAG, "pwm setwallpaper not found err:", e);
                if (ips != null) {
                }
                return;
            } catch (IOException e10) {
                e2 = e10;
                Log.w(TAG, "pwm setwallpaper io err:", e2);
                if (ips != null) {
                }
                return;
            }
            return;
        }
        Log.w(TAG, "pwm setwallpaper stopped");
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
        return pmInstallHwTheme(1002, "pmInstallHwTheme", themePath, setwallpaper, userId);
    }

    public boolean installDefaultHwTheme(Context ctx) {
        return installDefaultHwTheme(ctx, UserHandle.myUserId());
    }

    public boolean installDefaultHwTheme(Context ctx, int userId) {
        if (isDhomeThemeAdaptBackcolor()) {
            return installDefaultDhomeTheme(ctx);
        }
        String defname = getDefaultHwThemePack(ctx);
        Log.w(TAG, "the default theme: " + defname);
        if (!isFileExists(defname)) {
            return false;
        }
        pmCreateThemeFolder(HwLockPatternUtils.transaction_setActiveVisitorPasswordState, "pmCreateThemeFolder", null, false, userId);
        return installHwTheme(defname, false, userId);
    }

    public boolean makeIconCache(boolean clearall) {
        return true;
    }

    public boolean saveIconToCache(Bitmap bitmap, String fn, boolean clearold) {
        return true;
    }

    private String getDefaultHwThemePack(Context ctx) {
        String colors_themes = System.getString(ctx.getContentResolver(), "colors_themes");
        if (TextUtils.isEmpty(colors_themes)) {
            Log.w(TAG, "colors and themes is empty!");
            return System.getString(ctx.getContentResolver(), CUST_THEME_NAME);
        }
        String[] colorsAndThemes = colors_themes.split(";");
        HashMap<String, String> mHwColorThemes = new HashMap();
        for (String str : colorsAndThemes) {
            String[] color_theme = str.split(",");
            if (color_theme.length != 2 || (color_theme[0].isEmpty() ^ 1) == 0) {
                Log.w(TAG, "invalid color and theme : " + str);
            } else {
                mHwColorThemes.put(color_theme[0].toLowerCase(Locale.US), color_theme[1]);
            }
        }
        if (mHwColorThemes.isEmpty()) {
            Log.w(TAG, "has no valid color-theme!");
            return System.getString(ctx.getContentResolver(), CUST_THEME_NAME);
        }
        String mColor = SystemProperties.get("ro.config.devicecolor");
        String mBackColor = SystemProperties.get("ro.config.backcolor");
        if (mColor == null) {
            return System.getString(ctx.getContentResolver(), CUST_THEME_NAME);
        }
        String hwThemePath = (String) mHwColorThemes.get(mColor.toLowerCase(Locale.US));
        if (isFileExists(hwThemePath)) {
            System.putString(ctx.getContentResolver(), CUST_THEME_NAME, hwThemePath);
            Log.w(TAG, "The TP color: " + mColor + ", Theme path: " + hwThemePath);
            return hwThemePath;
        }
        if (hwThemePath == null && (TextUtils.isEmpty(mBackColor) ^ 1) != 0) {
            String mGroupColor = mColor + "+" + mBackColor;
            hwThemePath = (String) mHwColorThemes.get(mGroupColor.toLowerCase(Locale.US));
            if (isFileExists(hwThemePath)) {
                System.putString(ctx.getContentResolver(), CUST_THEME_NAME, hwThemePath);
                Log.w(TAG, "The group color: " + mGroupColor + ", Theme path: " + hwThemePath);
                return hwThemePath;
            }
        }
        return System.getString(ctx.getContentResolver(), CUST_THEME_NAME);
    }

    public boolean isDhomeThemeAdaptBackcolor() {
        return SystemProperties.getBoolean("ro.config.hw_dhome_theme", false);
    }

    private String getDhomeThemeName(Context ctx) {
        String DOCOMO_COLOR_THEMES = "dcm_color_themes";
        String DOCOMO_DEFAULT_THEME = "dcm_default_theme";
        String DOCOMO_DEFAULT_THEME_NAME = System.getString(ctx.getContentResolver(), "dcm_default_theme");
        String colorThemes = System.getString(ctx.getContentResolver(), "dcm_color_themes");
        String IS_DOCOMO_MULTI_THEMES = System.getString(ctx.getContentResolver(), "dcm_multi_themes");
        if (IS_DOCOMO_MULTI_THEMES != null && IS_DOCOMO_MULTI_THEMES.equals("true")) {
            colorThemes = System.getString(ctx.getContentResolver(), "dcm_color_multi_themes");
        }
        if (TextUtils.isEmpty(colorThemes)) {
            Log.w(TAG, "dcm colors and themes is empty!");
            return DOCOMO_DEFAULT_THEME_NAME;
        }
        String[] colors_themes = colorThemes.split(";");
        HashMap<String, String> dHomeColorThemes = new HashMap();
        for (String str : colors_themes) {
            String[] color_theme = str.split(",");
            if (color_theme.length != 2 || (color_theme[0].isEmpty() ^ 1) == 0) {
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
        String dcmThemeName = (String) dHomeColorThemes.get(mBackColor.toLowerCase(Locale.US));
        if (dcmThemeName == null || dcmThemeName.isEmpty()) {
            Log.w(TAG, "has no theme adapt to backcolor,use default docomo theme");
            return DOCOMO_DEFAULT_THEME_NAME;
        }
        System.putString(ctx.getContentResolver(), "dcm_default_theme", dcmThemeName);
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
        String path = System.getString(ctx.getContentResolver(), themePath);
        if (!TextUtils.isEmpty(path)) {
            def = path;
        }
        return def;
    }

    public boolean installDefaultDhomeTheme(Context ctx) {
        String DOCOMO_ALL_THEMES_DIR = Environment.getDataDirectory() + "/kisekae";
        String DOCOMO_PREFABRICATE_THEMES_DIR = getDhomeThemePath(ctx, "dcm_theme_path", "/cust/docomo/jp/themes/");
        String DOCOMO_DEFAULT_THEME_PATH = DOCOMO_ALL_THEMES_DIR + "/kisekae0.kin";
        String deviceThemeName = getDhomeThemeName(ctx);
        String originThemePath = DOCOMO_PREFABRICATE_THEMES_DIR + deviceThemeName;
        File file = new File(DOCOMO_ALL_THEMES_DIR);
        File originThemeFile = new File(originThemePath);
        if (UserHandle.getCallingUserId() != 0) {
            return false;
        }
        String hwThemeFileName = PATH_DATASKIN_WALLPAPER + UserHandle.myUserId();
        File hwThemePath = new File(hwThemeFileName);
        if (!(hwThemeFileName.contains("..") || (hwThemePath.exists() ^ 1) == 0)) {
            if (!hwThemePath.mkdirs()) {
                return false;
            }
            giveRWToPath(PATH_DATASKIN_WALLPAPER);
        }
        if (file.exists() || file.mkdir()) {
            giveRWToPath(DOCOMO_ALL_THEMES_DIR);
            String IS_DOCOMO_MULTI_THEMES = System.getString(ctx.getContentResolver(), "dcm_multi_themes");
            if (IS_DOCOMO_MULTI_THEMES == null || !IS_DOCOMO_MULTI_THEMES.equals("true")) {
                if (originThemeFile.exists()) {
                    linkTheme(originThemePath, DOCOMO_DEFAULT_THEME_PATH);
                }
            } else if (!TextUtils.isEmpty(deviceThemeName)) {
                String[] deviceThemeNames = deviceThemeName.split(":");
                int linkThemeTag = 0;
                for (String str : deviceThemeNames) {
                    String originMultiThemePath = DOCOMO_PREFABRICATE_THEMES_DIR + str;
                    file = new File(originMultiThemePath);
                    if (file.exists()) {
                        int linkThemeTag2 = linkThemeTag + 1;
                        linkTheme(originMultiThemePath, DOCOMO_ALL_THEMES_DIR + "/kisekae" + linkThemeTag + ".kin");
                        linkThemeTag = linkThemeTag2;
                    }
                }
            }
            if (DOCOMO_DEFAULT_THEME_PATH == null || (isFileExists(DOCOMO_DEFAULT_THEME_PATH) ^ 1) != 0) {
                Log.w(TAG, DOCOMO_DEFAULT_THEME_PATH + " not exist");
                return false;
            }
            saveDhomeThemeInfo(originThemeFile, DOCOMO_DEFAULT_THEME_PATH);
            return true;
        }
        Log.w(TAG, "mkdir /data/kisekae fail !!!");
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x00f5 A:{SYNTHETIC, Splitter: B:50:0x00f5} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00e3 A:{SYNTHETIC, Splitter: B:44:0x00e3} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00b7 A:{SYNTHETIC, Splitter: B:36:0x00b7} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveDhomeThemeInfo(File originThemeFile, String defaultThemePath) {
        Throwable th;
        if (originThemeFile == null || TextUtils.isEmpty(defaultThemePath)) {
            Log.e(TAG, "saveDhomeThemeInfo :: origin theme file or default theme path invalid.");
            return;
        }
        Properties properties = new Properties();
        properties.put("default_theme_path", defaultThemePath);
        String themeName = originThemeFile.getName();
        String str = "current_docomo_theme_type";
        Object obj = themeName.contains("kisekae_Cute.kin") ? "silver" : themeName.contains("village.kin") ? "gold" : "unknow";
        properties.put(str, obj);
        properties.put("current_docomo_theme_name", themeName);
        str = "deleted_docomo_themes";
        obj = themeName.contains("kisekae_Cute.kin") ? "village.kin" : themeName.contains("village.kin") ? "kisekae_Cute.kin" : "unknow";
        properties.put(str, obj);
        FileOutputStream fos = null;
        String deviceThemeInfoPath = defaultThemePath.substring(0, defaultThemePath.lastIndexOf(47)) + "/DeviceThemeInfo.properties";
        try {
            FileOutputStream fos2 = new FileOutputStream(deviceThemeInfoPath);
            try {
                properties.store(fos2, null);
                giveRWToPath(deviceThemeInfoPath);
                if (fos2 != null) {
                    try {
                        fos2.close();
                    } catch (IOException e) {
                        Log.w(TAG, "FileOutputStream close failed!");
                    }
                }
                fos = fos2;
            } catch (FileNotFoundException e2) {
                fos = fos2;
                Log.w(TAG, deviceThemeInfoPath + " not found");
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e3) {
                        Log.w(TAG, "FileOutputStream close failed!");
                    }
                }
            } catch (IOException e4) {
                fos = fos2;
                try {
                    Log.w(TAG, "DeviceThemeInfo.properties store failed!");
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e5) {
                            Log.w(TAG, "FileOutputStream close failed!");
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e6) {
                            Log.w(TAG, "FileOutputStream close failed!");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fos = fos2;
                if (fos != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            Log.w(TAG, deviceThemeInfoPath + " not found");
            if (fos != null) {
            }
        } catch (IOException e8) {
            Log.w(TAG, "DeviceThemeInfo.properties store failed!");
            if (fos != null) {
            }
        }
    }

    public void updateConfiguration(boolean changeuser) {
        try {
            boolean z;
            Configuration curConfig = new Configuration();
            curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            curConfig.extraConfig.setConfigItem(1, curConfig.extraConfig.getConfigItem(1) + 1);
            ActivityManagerNative.getDefault().updateConfiguration(curConfig);
            if (!HwPCUtils.enabled() || ActivityThread.currentActivityThread() == null) {
                z = false;
            } else {
                z = HwPCUtils.isValidExtDisplayId(ActivityThread.currentActivityThread().getDisplayId());
            }
            if (!z && !changeuser) {
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
        IOverlayManager overlayManager = Stub.asInterface(ServiceManager.getService("overlay"));
        if (overlayManager != null) {
            List frameworkhwextinfos = null;
            try {
                frameworkhwextinfos = overlayManager.getOverlayInfosForTarget(SYSTEM_APP_HWEXT, newUserId);
            } catch (RemoteException e) {
                Log.e(TAG, "fail get fwk overlayinfos");
            }
            int size = frameworkhwextinfos != null ? frameworkhwextinfos.size() : 0;
            for (int i = 0; i < size; i++) {
                String packageName = ((OverlayInfo) frameworkhwextinfos.get(i)).packageName;
                if (!TextUtils.isEmpty(packageName)) {
                    boolean isDarkTheme = HwThemeManager.isDeepDarkTheme();
                    boolean isHonorProduct = HwThemeManager.isHonorProduct();
                    if (!(isDarkTheme || newUserId == 0)) {
                        try {
                            overlayManager.setEnabled(packageName, false, 0);
                        } catch (RemoteException e2) {
                            Log.e(TAG, "fail set enable the primary user");
                        }
                    }
                    if (isDarkTheme || (isHonorProduct ^ 1) == 0) {
                        if (isDarkTheme && packageName.contains("dark")) {
                            try {
                                overlayManager.setEnabledExclusive(packageName, true, newUserId);
                                if (newUserId != 0) {
                                    overlayManager.setEnabledExclusive(packageName, true, 0);
                                }
                            } catch (RemoteException e3) {
                                Log.e(TAG, "fail set dark account access");
                            }
                        }
                        if (!isDarkTheme && isHonorProduct && packageName.contains("honor")) {
                            try {
                                overlayManager.setEnabledExclusive(packageName, true, newUserId);
                                if (newUserId != 0) {
                                    overlayManager.setEnabledExclusive(packageName, true, 0);
                                }
                            } catch (RemoteException e4) {
                                Log.e(TAG, "fail set honor account access");
                            }
                        }
                    } else {
                        try {
                            overlayManager.setEnabled(packageName, false, newUserId);
                        } catch (RemoteException e5) {
                            Log.e(TAG, "fail set false account access");
                        }
                    }
                }
            }
            Log.i(TAG, "HwThemeManagerImpl#updateOverlaysThems end");
        }
    }

    public void updateConfiguration() {
        updateConfiguration(false);
    }

    public Resources getResources(AssetManager assets, DisplayMetrics dm, Configuration config, DisplayAdjustments displayAdjustments, IBinder token) {
        return new HwResources(assets, dm, config, displayAdjustments, token);
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
                    try {
                        wallpaperManager.setStream(is2);
                        return is2;
                    } catch (IOException e) {
                        is = is2;
                    }
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
        } catch (IOException e2) {
        }
        if (is == null) {
            is = context.getResources().openRawResource(17302115);
        }
        return is;
    }

    public void updateIconCache(PackageItemInfo packageItemInfo, String name, String packageName, int icon, int packageIcon) {
        int i = 0;
        String resPackageName = packageName;
        String idAndPackageName = (icon != 0 ? icon : packageIcon) + "#" + packageName;
        if (!IconCache.contains(idAndPackageName)) {
            String tmpName = name != null ? name : packageName;
            String lc = tmpName != null ? tmpName.toLowerCase() : null;
            if (lc != null && lc.indexOf("shortcut") < 0 && lc.indexOf(".cts") < 0) {
                CacheEntry ce = new CacheEntry();
                ce.name = tmpName;
                if (icon != 0) {
                    i = 1;
                }
                ce.type = i;
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
        int id = icon != 0 ? icon : packageIcon;
        String idAndPackageName = id + "#" + getResourcePackageName(1004, "getResourcesPackageName", packageName, id);
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
        if (!flag || value.resourceId == 0) {
            return data[index + 1];
        }
        return resources.getColor(value.resourceId);
    }

    public int getHwThemeLauncherIconSize(ActivityManager am, Resources resources) {
        Resources res = resources;
        int configIconSize = SystemProperties.getInt("ro.config.app_big_icon_size", -1);
        int multiResSize = SystemProperties.getInt("persist.sys.res.icon_size", -1);
        if (configIconSize > 0 && multiResSize > 0) {
            configIconSize = multiResSize;
        }
        return configIconSize == -1 ? (int) resources.getDimension(34472064) : configIconSize;
    }

    public boolean isTRingtones(String path) {
        return path.indexOf(RINGTONE_CLASS_POSTFIX) > 0;
    }

    public boolean isTNotifications(String path) {
        if (path.indexOf(NOTIFI_CLASS_POSTFIX) > 0 || path.indexOf(CALENDAR_CLASS_POSTFIX) > 0 || path.indexOf(EMAIL_CLASS_POSTFIX) > 0 || path.indexOf(MESSAGE_CLASS_POSTFIX) > 0) {
            return true;
        }
        return false;
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
            str = (String) this.mPackageNameMap.get(packageName);
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
        if (!skinExist || ((checkState && (sIsDefaultThemeOk ^ 1) != 0) || installFlagExist)) {
            setIsDefaultThemeOk(installDefaultHwTheme(mContext, userId));
        } else if (isSupportThemeRestore() && isCustChange(mContext) && (isThemeChange(mContext) ^ 1) != 0) {
            if (new File(PATH_DATA_USERS + userId, WALLPAPER_INFO).exists()) {
                CommandLineUtil.rm("system", PATH_DATA_USERS + userId + WALLPAPER_INFO);
            }
            if (new File(PATH_DATA_USERS + userId, BLURRED_WALLPAPER).exists()) {
                CommandLineUtil.rm("system", PATH_DATA_USERS + userId + BLURRED_WALLPAPER);
            }
            installCustomerTheme(mContext, userId);
            HwThemeManager.updateConfiguration();
        }
    }

    private static void setIsDefaultThemeOk(boolean isOk) {
        sIsDefaultThemeOk = isOk;
    }

    public void addSimpleUIConfig(Activity activity) {
        if (activity.metaData.getBoolean("simpleuimode", false)) {
            ActivityInfo activityInfo = activity.info;
            activityInfo.configChanges |= StubController.PERMISSION_SMSLOG_WRITE;
        }
    }

    private IPackageManager getPackageManager() {
        if (this.mPackageManagerService == null) {
            this.mPackageManagerService = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        }
        return this.mPackageManagerService;
    }

    private boolean pmCreateThemeFolder(int code, String transactName, String paramName, boolean paramValue, int userId) {
        IBinder pmsBinder = getPackageManager().asBinder();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (pmsBinder != null) {
            try {
                data.writeInterfaceToken("huawei.com.android.server.IPackageManager");
                data.writeInt(userId);
                pmsBinder.transact(code, data, reply, 0);
                reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                reply.recycle();
                data.recycle();
            }
        }
        reply.recycle();
        data.recycle();
        return false;
    }

    private boolean pmInstallHwTheme(int code, String transactName, String paramName, boolean paramValue, int userId) {
        int i = 0;
        IBinder pmsBinder = getPackageManager().asBinder();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean result = false;
        if (pmsBinder != null) {
            try {
                data.writeInterfaceToken("huawei.com.android.server.IPackageManager");
                data.writeString(paramName);
                if (paramValue) {
                    i = 1;
                }
                data.writeInt(i);
                data.writeInt(userId);
                pmsBinder.transact(code, data, reply, 0);
                reply.readException();
                result = reply.readInt() != 0;
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                reply.recycle();
                data.recycle();
            }
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    private void initLauncherComponent() {
        this.mDisablelaunchers.clear();
        this.mLauncherMap.clear();
        if (IS_SHOW_DCMUI) {
            this.mDisablelaunchers.add(new ComponentState(DOCOMOHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(UNIHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(SIMPLELAUNCHERHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(SIMPLEHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(DRAWERHOME_COMPONENT));
        } else {
            this.mDisablelaunchers.add(new ComponentState(UNIHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(SIMPLEHOME_COMPONENT));
            this.mDisablelaunchers.add(new ComponentState(DRAWERHOME_COMPONENT));
        }
        this.mLauncherMap.put(Integer.valueOf(0), Integer.valueOf(0));
        this.mLauncherMap.put(Integer.valueOf(1), Integer.valueOf(0));
        this.mLauncherMap.put(Integer.valueOf(2), Integer.valueOf(1));
        this.mLauncherMap.put(Integer.valueOf(4), Integer.valueOf(2));
    }

    private void enableLaunchers(int type, int userId) {
        int disableSize = this.mDisablelaunchers.size();
        int i = 0;
        while (i < disableSize) {
            ComponentState componentstate = (ComponentState) this.mDisablelaunchers.get(i);
            componentstate.mSetState = i == type ? 1 : 2;
            componentstate.setComponentEnable(userId);
            i++;
        }
    }

    public void retrieveSimpleUIConfig(ContentResolver cr, Configuration config, int userId) {
        int simpleuiVal = System.getIntForUser(cr, HwSettings.System.SIMPLEUI_MODE, 0, ActivityManager.getCurrentUser());
        int launcherConfig = SystemProperties.getInt("ro.config.hw_simpleui_enable", 0);
        Log.d(TAG, "updateSimpleUIConfig simpleuiVal =" + simpleuiVal + ",launcherConfig=" + launcherConfig);
        if (simpleuiVal == 0 || (IS_REGIONAL_PHONE_FEATURE && Secure.getInt(cr, "user_setup_complete", 0) != 1)) {
            simpleuiVal = launcherConfig == 0 ? 1 : launcherConfig;
        }
        config.extraConfig.setConfigItem(2, simpleuiVal);
        try {
            enableLaunchers(((Integer) this.mLauncherMap.get(Integer.valueOf(simpleuiVal))).intValue(), userId);
        } catch (Exception e) {
            Log.d(TAG, "retrieveSimpleUIConfig enableLaunchers e=" + e);
        }
    }

    public void updateSimpleUIConfig(ContentResolver cr, Configuration config, int configChanges) {
        if ((StubController.PERMISSION_SMSLOG_WRITE & configChanges) != 0) {
            Log.w(TAG, "updateSimpleUIConfig SIMPLEUI_MODE putIntForUser =" + config.extraConfig.getConfigItem(2));
            System.putIntForUser(cr, HwSettings.System.SIMPLEUI_MODE, config.extraConfig.getConfigItem(2), ActivityManager.getCurrentUser());
        }
    }

    public Bitmap getThemeBitmap(Resources res, int id, Rect padding) {
        TypedValue outValue = new TypedValue();
        res.getValue(id, outValue, true);
        return res.getThemeBitmap(outValue, id, padding);
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
        Locale locale = null;
        if (sIsHwtFlipFontOn) {
            boolean isHwThemeChanged = false;
            if (!(this.lastHwConfig == null || newConfig == null)) {
                if ((StubController.PERMISSION_CALLLOG_WRITE & this.lastHwConfig.updateFrom(newConfig.extraConfig)) != 0) {
                    isHwThemeChanged = true;
                }
            }
            boolean isLocaleChanged = false;
            if (!(newConfig == null || newConfig.locale == null || (this.lastLocale != null && (this.lastLocale.equals(newConfig.locale) ^ 1) == 0))) {
                if (newConfig.locale != null) {
                    locale = (Locale) newConfig.locale.clone();
                }
                this.lastLocale = locale;
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
        return familyName != null ? familyName.equals("chnfzxh") : false;
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
        if (bm == null) {
            return null;
        }
        WindowManager wm = (WindowManager) context.getSystemService(FreezeScreenScene.WINDOW_PARAM);
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = wm.getDefaultDisplay();
        display.getMetrics(metrics);
        bm.setDensity(metrics.noncompatDensityDpi);
        Point size = new Point();
        display.getRealSize(size);
        int max = size.x > size.y ? size.x : size.y;
        int min = size.x < size.y ? size.x : size.y;
        if (width <= 0 || height <= 0 || ((bm.getWidth() == width && bm.getHeight() == height) || ((bm.getWidth() == min && bm.getHeight() == max) || ((bm.getWidth() == max && bm.getHeight() == max) || (bm.getWidth() == max && bm.getHeight() == min))))) {
            return bm;
        }
        try {
            Bitmap newbm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            if (newbm == null) {
                Log.w(TAG, "Can't generate default bitmap, newbm = null");
                return bm;
            }
            newbm.setDensity(metrics.noncompatDensityDpi);
            Canvas c = new Canvas(newbm);
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
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
            c.drawBitmap(bm, null, targetRect, paint);
            bm.recycle();
            return newbm;
        } catch (OutOfMemoryError e) {
            Log.w(TAG, "Can't generate default bitmap", e);
            return bm;
        }
    }

    private String getResourcePackageName(int code, String transactName, String packageName, int icon) {
        String name = getResourcePackageNameFromMap(packageName);
        if (name != null) {
            return name;
        }
        IBinder resBinder = getPackageManager().asBinder();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String result = null;
        if (resBinder != null) {
            try {
                data.writeInterfaceToken("huawei.com.android.server.IPackageManager");
                data.writeString(packageName);
                data.writeInt(icon);
                data.writeInt(UserHandle.myUserId());
                resBinder.transact(code, data, reply, 0);
                reply.readException();
                result = reply.readString();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                reply.recycle();
                data.recycle();
            }
        }
        reply.recycle();
        data.recycle();
        if (!(result == null || (result.equals("") ^ 1) == 0)) {
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

    public Drawable getClonedDrawable(Context context, Drawable drawable) {
        if (!IS_SUPPORT_CLONE_APP) {
            return drawable;
        }
        Drawable clone = context.getResources().getDrawable(33751231);
        if (clone == null) {
            return drawable;
        }
        if (drawable == null) {
            clone.setColorFilter(context.getColor(33882311), Mode.SRC_IN);
            clone.setBounds(0, 0, clone.getIntrinsicWidth(), clone.getIntrinsicHeight());
            return clone;
        }
        int cloneWidth;
        int cloneHeight;
        BitmapDrawable mergedDrawable;
        Float markBgRatio = Float.valueOf(Float.parseFloat(context.getResources().getString(34472193)));
        Float markBgPercentage = Float.valueOf(Float.parseFloat(context.getResources().getString(34472194)));
        int srcWidth = drawable.getIntrinsicWidth();
        int srcHeight = drawable.getIntrinsicHeight();
        int tempSrcWidth = (int) (((float) srcWidth) * markBgPercentage.floatValue());
        int tempSrcHeight = (int) (((float) srcHeight) * markBgPercentage.floatValue());
        int markBgRadius = tempSrcWidth > tempSrcHeight ? tempSrcHeight : tempSrcWidth;
        if (clone.getIntrinsicWidth() >= clone.getIntrinsicHeight()) {
            cloneWidth = (int) (((float) markBgRadius) / markBgRatio.floatValue());
            cloneHeight = (int) (((((double) cloneWidth) * 1.0d) * ((double) clone.getIntrinsicHeight())) / ((double) clone.getIntrinsicWidth()));
        } else {
            cloneHeight = (int) (((float) markBgRadius) / markBgRatio.floatValue());
            cloneWidth = (int) (((((double) cloneHeight) * 1.0d) * ((double) clone.getIntrinsicWidth())) / ((double) clone.getIntrinsicHeight()));
        }
        synchronized (this.mLockForClone) {
            Bitmap newBitMap = Bitmap.createBitmap(srcWidth, srcWidth, Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitMap);
            drawable.setBounds(0, 0, srcWidth, srcWidth);
            drawable.draw(canvas);
            Paint mPaint = new Paint();
            mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_OVER));
            mPaint.setAntiAlias(true);
            canvas.save();
            mPaint.setColor(context.getColor(33882311));
            canvas.translate((float) (srcWidth - markBgRadius), (float) (srcHeight - markBgRadius));
            canvas.drawCircle(((float) markBgRadius) / 2.0f, ((float) markBgRadius) / 2.0f, ((float) markBgRadius) / 2.0f, mPaint);
            canvas.translate(((float) (markBgRadius - cloneWidth)) / 2.0f, ((float) (markBgRadius - cloneHeight)) / 2.0f);
            canvas.scale((((float) cloneWidth) * 1.0f) / ((float) clone.getIntrinsicWidth()), (((float) cloneHeight) * 1.0f) / ((float) clone.getIntrinsicHeight()));
            clone.setBounds(0, 0, clone.getIntrinsicWidth(), clone.getIntrinsicHeight());
            clone.draw(canvas);
            canvas.restore();
            mPaint.setXfermode(null);
            mergedDrawable = new BitmapDrawable(context.getResources(), newBitMap);
            if (drawable instanceof BitmapDrawable) {
                mergedDrawable.setTargetDensity(((BitmapDrawable) drawable).getBitmap().getDensity());
            }
        }
        return mergedDrawable;
    }

    public Drawable getHwBadgeDrawable(Notification notification, Context context, Drawable drawable) {
        if (!notification.extras.getBoolean("com.huawei.isIntentProtectedApp")) {
            return null;
        }
        Drawable trustSpace = context.getResources().getDrawable(33751237);
        trustSpace.setColorFilter(context.getColor(33882311), Mode.SRC_IN);
        trustSpace.setBounds(0, 0, trustSpace.getIntrinsicWidth(), trustSpace.getIntrinsicHeight());
        return trustSpace;
    }

    public String getDefaultLiveWallpaper(int userId) {
        Document document = null;
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        String livepaperpath = "/data/themes//" + userId;
        try {
            document = builderFactory.newDocumentBuilder().parse(new File(livepaperpath, LIVEWALLPAPER_FILE));
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
            if (itemNode.getNodeType() == (short) 1) {
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
        if (isFileExists(defname)) {
            return installHwTheme(defname, true, userId);
        }
        return false;
    }

    public static boolean isSupportThemeRestore() {
        return !IS_REGIONAL_PHONE_FEATURE ? IS_COTA_FEATURE : true;
    }

    public boolean isCustChange(Context context) {
        try {
            if (IS_REGIONAL_PHONE_FEATURE) {
                String originalVendorCountry = Secure.getString(context.getContentResolver(), "vendor_country");
                String currentVendorCountry = SystemProperties.get("ro.hw.custPath", "");
                if (originalVendorCountry == null) {
                    Secure.putString(context.getContentResolver(), "vendor_country", currentVendorCountry);
                    return false;
                } else if (!originalVendorCountry.equals(currentVendorCountry)) {
                    Secure.putString(context.getContentResolver(), "vendor_country", currentVendorCountry);
                    return true;
                }
            } else if (IS_COTA_FEATURE) {
                String originalCustVersion = Secure.getString(context.getContentResolver(), "custCDVersion");
                String custCVersion = SystemProperties.get("ro.product.CustCVersion", "");
                String custDVersion = SystemProperties.get("ro.product.CustDVersion", "");
                String originalCotaVersion = Secure.getString(context.getContentResolver(), "cotaVersion");
                String cotaVersion = SystemProperties.get("ro.product.CotaVersion", "");
                if (originalCustVersion == null && originalCotaVersion == null) {
                    Secure.putString(context.getContentResolver(), "custCDVersion", custCVersion + custDVersion);
                    Secure.putString(context.getContentResolver(), "cotaVersion", cotaVersion);
                    return false;
                } else if (!(custCVersion + custDVersion).equals(originalCustVersion)) {
                    Secure.putString(context.getContentResolver(), "custCDVersion", custCVersion + custDVersion);
                    return true;
                } else if (!cotaVersion.equals(originalCotaVersion)) {
                    Secure.putString(context.getContentResolver(), "cotaVersion", cotaVersion);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "check cust Exception e : " + e);
        }
        return false;
    }

    public static boolean isThemeChange(Context context) {
        return "true".equals(Secure.getString(context.getContentResolver(), "isUserChangeTheme"));
    }
}
