package ohos.bundlemgr;

import android.app.ActivityThread;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserManager;
import huawei.hiview.HiEvent;
import huawei.hiview.HiView;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundlemgr.freeinstall.ErrorReminder;
import ohos.bundlemgr.webability.WebPackageInfo;
import ohos.bundlemgr.webability.WebPackagesResolver;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.hiviewdfx.HiLogLabel;
import ohos.system.Parameters;

public class BundleMgrAdapter {
    private static final String AMERICA_LANGUAGE_LOCALE = "en";
    private static final HiLogLabel BMS_ADAPTER_LABEL = new HiLogLabel(3, 218108160, TAG);
    private static final String BUNDLE_INSTALL_HAP_PROP = "bundle.installer.hap.name";
    private static final String BUNDLE_INSTALL_PATH_PROP = "bundle.installer.path";
    private static final String CHINA_LANGUAGE_LOCALE = "zh";
    private static final String DOWNLOAD_ROOT_PATH = "/sdcard/Download/";
    private static final String ENGLISH_LOCALE = "en_US";
    private static final int EVENT_ID_INSTALL_FAILED = 951000202;
    private static final int GET_PACKAGE_INFO_FLAGS = 134234125;
    private static final String HIEVENT_KEY_BUNDLE_NAME = "BUNDLE_NAME";
    private static final String HIEVENT_KEY_ERROR_TYPE = "ERROR_TYPE";
    private static final String HONGKONG_CHINESE_LOCALE = "zh_HK";
    private static final String HONGKONG_LOCALE_AREA = "HK";
    private static final Object INSTANCE_LOCK = new Object();
    private static final int JSON_PROFILE_PARSE_FAILED = 6;
    private static final String MAINLAND_CHINESE_LOCALE = "zh_CN";
    private static final String MOCK_FEATURE_HAP = "Music.hap";
    private static final int MSG_INIT_DATA = 1;
    private static final int MSG_INIT_GRS_CLIENT = 2;
    private static final String PAGE_SHELL_SUFFIX = "ShellActivity";
    private static final int RESTART_AFTER_UPGRADE_WAIT_TIME = 500;
    private static final String SIMPLIFIED_SUFFIX = "_#Hans";
    private static final String TAG = "BundleMgrAdapter";
    private static final String TAIWAN_CHINESE_LOCALE = "zh_TW";
    private static final String TAIWAN_LOCALE_AREA = "TW";
    private static final String TIBETAN_LANGUAGE_LOCALE = "bo";
    private static final String TIBETAN_LOCALE = "bo_CN";
    private static final String TRADITIONAL_SUFFIX = "_#Hant";
    private static final String USES_FEATRUE_ZIDANE = "zidane.software.ability";
    private static final String UYGHUR_LANGUAGE_LOCALE = "ug";
    private static final String UYGHUR_LOCALE = "ug_CN";
    private static volatile BundleMgrAdapter instance = null;
    private static volatile PackageInstallerAdapter packageInstallerAdapter = null;
    private final Object INIT_LOCK = new Object();
    private Context bundleMgrContext = null;
    private Handler bundleMgrHandler = null;
    private int currentUserId = 0;
    private InstalledBundleInfo[] installedBundlesOfCurrentUser = null;
    private volatile boolean isDataReady = false;
    private SilentAppMrgAdapter silentAppMrgAdapter = null;
    private final BundleMgrAdapterUtils utils = new BundleMgrAdapterUtils();
    private WebPackagesResolver webPackagesResolver;

    private native void nativeInit();

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeSetUsbDebugState(boolean z);

    static {
        try {
            AppLog.i(BMS_ADAPTER_LABEL, "Load bundle mgr jni so", new Object[0]);
            System.loadLibrary("bundlemgr_jni.z");
        } catch (UnsatisfiedLinkError unused) {
            AppLog.e(BMS_ADAPTER_LABEL, "ERROR: Could not load bundlemgr_jni.z.so ", new Object[0]);
        }
    }

    public static BundleMgrAdapter getInstance() {
        if (instance == null) {
            synchronized (INSTANCE_LOCK) {
                if (instance == null) {
                    instance = new BundleMgrAdapter();
                }
            }
        }
        return instance;
    }

    private BundleMgrAdapter() {
        AppLog.i(BMS_ADAPTER_LABEL, TAG, new Object[0]);
    }

    public void init(Context context) {
        AppLog.i(BMS_ADAPTER_LABEL, "init begin", new Object[0]);
        if (context == null) {
            AppLog.e(BMS_ADAPTER_LABEL, "init failed, context is null", new Object[0]);
            return;
        }
        this.bundleMgrContext = context;
        this.webPackagesResolver = new WebPackagesResolver(context);
        setPackageInstallerAdapter(new PackageInstallerAdapter(context));
        this.silentAppMrgAdapter = new SilentAppMrgAdapter(context);
        nativeInit();
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        if (looper == null) {
            AppLog.e(BMS_ADAPTER_LABEL, "looper is null", new Object[0]);
            return;
        }
        this.bundleMgrHandler = new Handler(looper) {
            /* class ohos.bundlemgr.BundleMgrAdapter.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int i = message.what;
                if (i == 1) {
                    BundleMgrAdapter.this.initPreZidanePackageInfos();
                } else if (i != 2) {
                    AppLog.w(BundleMgrAdapter.BMS_ADAPTER_LABEL, "handleMessage, unknown msg : %{public}s", Integer.valueOf(message.what));
                } else {
                    BundleMgrAdapter.initGrsClient();
                }
                super.handleMessage(message);
            }
        };
        this.bundleMgrHandler.sendEmptyMessage(1);
        this.bundleMgrHandler.sendEmptyMessage(2);
        registerUsbStateReceiver(this.bundleMgrContext);
        AppLog.i(BMS_ADAPTER_LABEL, "init end", new Object[0]);
    }

    public WebPackageInfo[] getPackageInfosFromHbs(String str) {
        WebPackageInfo[] webPackageInfoArr = new WebPackageInfo[0];
        WebPackagesResolver webPackagesResolver2 = this.webPackagesResolver;
        if (webPackagesResolver2 == null) {
            return webPackageInfoArr;
        }
        List<WebPackageInfo> packages = webPackagesResolver2.getPackages(str);
        if (packages.isEmpty()) {
            return webPackageInfoArr;
        }
        return (WebPackageInfo[]) packages.toArray(new WebPackageInfo[packages.size()]);
    }

    private static void setPackageInstallerAdapter(PackageInstallerAdapter packageInstallerAdapter2) {
        packageInstallerAdapter = packageInstallerAdapter2;
    }

    private InstalledBundleInfo[] getInstalledBundlesFromPms(int i) {
        AppLog.i(BMS_ADAPTER_LABEL, "getInstalledBundlesFromPms", new Object[0]);
        InstalledBundleInfo[] installedBundleInfoArr = new InstalledBundleInfo[0];
        PackageManager packageManager = this.utils.getPackageManager(this.bundleMgrContext);
        if (packageManager == null) {
            AppLog.e(BMS_ADAPTER_LABEL, "getInstalledBundlesFromPms failed, pm is null", new Object[0]);
            return installedBundleInfoArr;
        }
        ArrayList arrayList = new ArrayList();
        List<PackageInfo> installedPackagesAsUser = packageManager.getInstalledPackagesAsUser(GET_PACKAGE_INFO_FLAGS, i);
        if (installedPackagesAsUser == null) {
            AppLog.e(BMS_ADAPTER_LABEL, "getInstalledBundlesFromPms failed, pkgInfoList is null", new Object[0]);
            return installedBundleInfoArr;
        }
        for (PackageInfo packageInfo : installedPackagesAsUser) {
            InstalledBundleInfo convertPackageInfo2BundleInfo = this.utils.convertPackageInfo2BundleInfo(packageInfo, i);
            if (convertPackageInfo2BundleInfo != null) {
                arrayList.add(convertPackageInfo2BundleInfo);
                AppLog.d(BMS_ADAPTER_LABEL, "add name = %{public}s", packageInfo.packageName);
            }
        }
        int size = arrayList.size();
        AppLog.i(BMS_ADAPTER_LABEL, "userId : %{private}d, installedBundles size : %{public}d", Integer.valueOf(i), Integer.valueOf(size));
        return size > 0 ? (InstalledBundleInfo[]) arrayList.toArray(new InstalledBundleInfo[size]) : installedBundleInfoArr;
    }

    public void initPreZidanePackageInfos() {
        Context context = this.bundleMgrContext;
        if (context == null) {
            AppLog.e(BMS_ADAPTER_LABEL, "initPreZidanePackageInfos failed, bundleMgrContext is null", new Object[0]);
            return;
        }
        UserManager userManager = this.utils.getUserManager(context);
        synchronized (this.INIT_LOCK) {
            if (userManager != null) {
                this.currentUserId = userManager.getUserHandle();
            }
            this.installedBundlesOfCurrentUser = getInstalledBundlesFromPms(this.currentUserId);
            this.isDataReady = true;
        }
    }

    public InstalledBundleInfo[] getInstalledBundles(int i) {
        synchronized (this.INIT_LOCK) {
            if (!this.isDataReady || i != this.currentUserId) {
                return getInstalledBundlesFromPms(i);
            }
            this.isDataReady = false;
            InstalledBundleInfo[] installedBundleInfoArr = this.installedBundlesOfCurrentUser;
            this.installedBundlesOfCurrentUser = null;
            return installedBundleInfoArr;
        }
    }

    private InstalledBundleInfo getBundleInfoFromPms(String str, int i) {
        PackageManager packageManager = this.utils.getPackageManager(this.bundleMgrContext);
        if (packageManager == null) {
            AppLog.e(BMS_ADAPTER_LABEL, "getBundleInfoFromPms failed, pm is null", new Object[0]);
            return null;
        }
        try {
            PackageInfo packageInfoAsUser = packageManager.getPackageInfoAsUser(str, GET_PACKAGE_INFO_FLAGS, i);
            if (packageInfoAsUser != null) {
                return this.utils.convertPackageInfo2BundleInfo(packageInfoAsUser, i);
            }
            return null;
        } catch (PackageManager.NameNotFoundException e) {
            AppLog.w(BMS_ADAPTER_LABEL, "getPackageInfoAsUser failed, error : %{public}s", e.getMessage());
            return null;
        }
    }

    public String[] checkAndUninstall(String[] strArr) {
        SilentAppMrgAdapter silentAppMrgAdapter2 = this.silentAppMrgAdapter;
        if (silentAppMrgAdapter2 != null) {
            return silentAppMrgAdapter2.checkAndUninstall(strArr);
        }
        AppLog.i(BMS_ADAPTER_LABEL, "silentAppMrgAdapter init did not finish", new Object[0]);
        return new String[0];
    }

    public static InstalledBundleInfo[] getInstalledbundles(int i) {
        AppLog.i(BMS_ADAPTER_LABEL, "getInstalledbundles", new Object[0]);
        return getInstance().getInstalledBundles(i);
    }

    public static WebPackageInfo[] getInstalledWebAbilities(String str) {
        AppLog.i(BMS_ADAPTER_LABEL, "getInstalledWebAbilities", new Object[0]);
        return getInstance().getPackageInfosFromHbs(str);
    }

    public static InstalledBundleInfo getBundleInfo(String str, int i) {
        if (str == null || str.isEmpty()) {
            AppLog.e(BMS_ADAPTER_LABEL, "bundle name is null or empty", new Object[0]);
            return null;
        }
        AppLog.d(BMS_ADAPTER_LABEL, "getbundleInfo, name : %{public}s", str);
        return getInstance().getBundleInfoFromPms(str, i);
    }

    public static String getHagBaseUrl() {
        return GrsServiceUtils.getInstance().getHagBaseUrl();
    }

    public static String downloadApp(String str) {
        AppLog.i(BMS_ADAPTER_LABEL, "begin to download app, bundleName is %{private}s", str);
        String str2 = Parameters.get(BUNDLE_INSTALL_PATH_PROP, DOWNLOAD_ROOT_PATH);
        return str2 + str;
    }

    public static String downloadHap(String str, String str2, String str3) {
        AppLog.i(BMS_ADAPTER_LABEL, "begin to download hap, downloadUrl is %{private}s", str);
        return Parameters.get(BUNDLE_INSTALL_HAP_PROP, "/sdcard/Download/Music.hap");
    }

    private static Context getAppContext() {
        Application currentApplication = ActivityThread.currentApplication();
        if (currentApplication == null) {
            AppLog.e(BMS_ADAPTER_LABEL, "get current application failed", new Object[0]);
            return null;
        }
        Context applicationContext = currentApplication.getApplicationContext();
        if (applicationContext != null) {
            return applicationContext;
        }
        AppLog.e(BMS_ADAPTER_LABEL, "get application context failed", new Object[0]);
        return null;
    }

    public static void showErrorMessage(int i) {
        Context appContext = getAppContext();
        if (appContext != null) {
            new ErrorReminder(appContext).showErrorMessage(i);
        }
    }

    public static boolean startAbilityWithParam(String str) {
        AppLog.d(BMS_ADAPTER_LABEL, "startAbilityWithParam param = %{private}s", str);
        if (str == null || str.isEmpty()) {
            AppLog.e(BMS_ADAPTER_LABEL, "startAbilityWithParam failed, param is null or empty", new Object[0]);
            return false;
        }
        try {
            final Intent parseUri = Intent.parseUri(str, 0);
            parseUri.addFlags(268435456);
            BundleMgrAdapter instance2 = getInstance();
            if (instance2.bundleMgrContext == null) {
                AppLog.e(BMS_ADAPTER_LABEL, "startAbilityWithParam failed, bundleMgrContext is null", new Object[0]);
                return false;
            }
            Thread thread = new Thread(new Runnable() {
                /* class ohos.bundlemgr.BundleMgrAdapter.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        Thread.sleep(500);
                        BundleMgrAdapter.this.bundleMgrContext.startActivity(parseUri);
                    } catch (InterruptedException e) {
                        AppLog.e(BundleMgrAdapter.TAG, "Thread.sleep exception:" + e);
                    } catch (ActivityNotFoundException unused) {
                        AppLog.e(BundleMgrAdapter.BMS_ADAPTER_LABEL, "startAbilityWithParam failed, ability not found", new Object[0]);
                    }
                }
            });
            thread.setName("bmsThread");
            thread.setUncaughtExceptionHandler($$Lambda$BundleMgrAdapter$QIYKWrHtLNRnhj26XwNPuGJOl9o.INSTANCE);
            thread.start();
            return true;
        } catch (URISyntaxException unused) {
            AppLog.e(BMS_ADAPTER_LABEL, "startAbilityWithParam failed, param is invalid", new Object[0]);
            return false;
        }
    }

    public static String getLocale() {
        Locale locale;
        Context appContext = getAppContext();
        if (appContext == null) {
            locale = Locale.getDefault();
        } else if (Build.VERSION.SDK_INT >= 24) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = appContext.getResources().getConfiguration().locale;
        }
        String str = MAINLAND_CHINESE_LOCALE;
        if (locale == null) {
            return str;
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if ((language == null || language.isEmpty()) && (country == null || country.isEmpty())) {
            return str;
        }
        if (CHINA_LANGUAGE_LOCALE.equals(language)) {
            if (locale.toString().endsWith(SIMPLIFIED_SUFFIX)) {
                return str;
            }
            if (locale.toString().endsWith(TRADITIONAL_SUFFIX)) {
                boolean equals = HONGKONG_LOCALE_AREA.equals(country);
                str = HONGKONG_CHINESE_LOCALE;
                if (equals) {
                    return str;
                }
                if (TAIWAN_LOCALE_AREA.equals(country)) {
                    return TAIWAN_CHINESE_LOCALE;
                }
            }
            return str;
        } else if (AMERICA_LANGUAGE_LOCALE.equals(language)) {
            return ENGLISH_LOCALE;
        } else {
            if (TIBETAN_LANGUAGE_LOCALE.equals(language)) {
                return TIBETAN_LOCALE;
            }
            if (UYGHUR_LANGUAGE_LOCALE.equals(language)) {
                return UYGHUR_LOCALE;
            }
            return locale.toString();
        }
    }

    public static PackageInstalledStatus installShellApk(InstallShellInfo installShellInfo) {
        if (packageInstallerAdapter == null) {
            AppLog.i(BMS_ADAPTER_LABEL, "PackageInstallerAdapter init did not finish", new Object[0]);
            return null;
        } else if (installShellInfo == null) {
            AppLog.i(BMS_ADAPTER_LABEL, "Shell apk install failure: Params shellApkPaths is null", new Object[0]);
            return null;
        } else {
            AppLog.d(BMS_ADAPTER_LABEL, "installShellApk userId = %{private}d, entryHap = %{public}s", Integer.valueOf(installShellInfo.getUserId()), installShellInfo.getEntryHap());
            AppLog.d(BMS_ADAPTER_LABEL, "start install shellAPK %{private}s, and startTime is %{private}d ms", installShellInfo.getPackageName(), Long.valueOf(SystemClock.uptimeMillis()));
            PackageInstalledStatus installShellApkByPms = packageInstallerAdapter.installShellApkByPms(installShellInfo);
            AppLog.d(BMS_ADAPTER_LABEL, "finish install shellAPK %{private}s, and endTime is %{private}d ms", installShellInfo.getPackageName(), Long.valueOf(SystemClock.uptimeMillis()));
            return installShellApkByPms;
        }
    }

    public static PackageInstalledStatus uninstallShellApk(String str, int i, boolean z) {
        if (packageInstallerAdapter == null) {
            AppLog.i(BMS_ADAPTER_LABEL, "PackageInstallerAdapter init did not finish", new Object[0]);
            return null;
        }
        AppLog.d(BMS_ADAPTER_LABEL, "uninstallShellApk packageName = %{private}s, userId = %{private}d", str, Integer.valueOf(i));
        AppLog.d(BMS_ADAPTER_LABEL, "start uninstall shellAPK %{private}s, and startTime is %{private}dms", str, Long.valueOf(SystemClock.uptimeMillis()));
        PackageInstalledStatus uninstallShellApkByPms = packageInstallerAdapter.uninstallShellApkByPms(str, i, z);
        AppLog.d(BMS_ADAPTER_LABEL, "finish uninstall shellAPK %{private}s, and endTime is %{private}dms", str, Long.valueOf(SystemClock.uptimeMillis()));
        return uninstallShellApkByPms;
    }

    public static String[] checkAndUninstallSilentApps(String[] strArr) {
        return getInstance().checkAndUninstall(strArr);
    }

    public static boolean startShortcut(ShortcutIntent[] shortcutIntentArr) {
        AppLog.i(BMS_ADAPTER_LABEL, "startShortcut start", new Object[0]);
        ArrayList arrayList = new ArrayList();
        for (ShortcutIntent shortcutIntent : shortcutIntentArr) {
            if (shortcutIntent == null) {
                AppLog.w(BMS_ADAPTER_LABEL, "startShortcut failed, intentInfo is null", new Object[0]);
                return false;
            }
            AppLog.w(BMS_ADAPTER_LABEL, "startShortcut targetBundle: %{private}s", shortcutIntent.getTargetBundle());
            String targetClass = shortcutIntent.getTargetClass();
            if (shortcutIntent.getTargetClass().startsWith(".")) {
                targetClass = shortcutIntent.getTargetBundle() + shortcutIntent.getTargetClass();
            }
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(shortcutIntent.getTargetBundle(), targetClass));
            intent.addFlags(268435456);
            arrayList.add(intent);
        }
        return innerStartShortcut(arrayList);
    }

    private static boolean innerStartShortcut(List<Intent> list) {
        Intent[] intentArr = (Intent[]) list.toArray(new Intent[0]);
        try {
            BundleMgrAdapter instance2 = getInstance();
            if (instance2.bundleMgrContext == null) {
                AppLog.w(BMS_ADAPTER_LABEL, "startShortcut failed, bundleMgrContext is null", new Object[0]);
                return false;
            }
            instance2.bundleMgrContext.startActivities(intentArr);
            AppLog.i(BMS_ADAPTER_LABEL, "startShortcut success", new Object[0]);
            return true;
        } catch (ActivityNotFoundException unused) {
            AppLog.w(BMS_ADAPTER_LABEL, "startShortcut failed due to ability not found", new Object[0]);
            return false;
        }
    }

    public boolean setApplicationSettingByBms(String str, int i) {
        IPackageManager asInterface = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (asInterface == null) {
            AppLog.w(BMS_ADAPTER_LABEL, "setApplicationSettingByBms failed, packageManager is null", new Object[0]);
            return false;
        } else if (this.bundleMgrContext == null) {
            AppLog.w(BMS_ADAPTER_LABEL, "setApplicationSettingByBms failed, bundleMgrContext is null", new Object[0]);
            return false;
        } else if (str == null || str.isEmpty()) {
            AppLog.w(BMS_ADAPTER_LABEL, "setApplicationSettingByBms failed, packageName is null", new Object[0]);
            return false;
        } else {
            if (str.contains(PsuedoNames.PSEUDONAME_ROOT)) {
                str = str + "ShellActivity";
            }
            ComponentName unflattenFromString = ComponentName.unflattenFromString(str);
            try {
                List<UserInfo> userInfos = this.utils.getUserInfos(this.bundleMgrContext);
                if (userInfos == null) {
                    AppLog.w(BMS_ADAPTER_LABEL, "setApplicationSettingByBms failed, userList is null", new Object[0]);
                    return false;
                }
                for (UserInfo userInfo : userInfos) {
                    if (unflattenFromString != null) {
                        asInterface.setComponentEnabledSetting(unflattenFromString, i, 0, userInfo.id);
                    } else {
                        asInterface.setApplicationEnabledSetting(str, i, 0, userInfo.id, this.bundleMgrContext.getOpPackageName());
                    }
                }
                return true;
            } catch (RemoteException unused) {
                AppLog.w(BMS_ADAPTER_LABEL, "setApplicationDisableByBms exception.", new Object[0]);
                return false;
            }
        }
    }

    private void registerUsbStateReceiver(Context context) {
        UsbStateBroadcastReceiver usbStateBroadcastReceiver = new UsbStateBroadcastReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.hardware.usb.action.USB_STATE");
        context.registerReceiver(usbStateBroadcastReceiver, intentFilter);
    }

    public static boolean setApplicationSetting(String str, int i) {
        AppLog.i(BMS_ADAPTER_LABEL, "setApplicationDisable packageName:%{public}s, state:%{public}d", str, Integer.valueOf(i));
        return getInstance().setApplicationSettingByBms(str, i);
    }

    public static class BundleMgrAdapterUtils {
        private BundleMgrAdapterUtils() {
        }

        public InstalledBundleInfo convertPackageInfo2BundleInfo(PackageInfo packageInfo, int i) {
            boolean z;
            if (packageInfo == null) {
                AppLog.e(BundleMgrAdapter.BMS_ADAPTER_LABEL, "convertPackageInfo2BundleInfo failed, pkgInfo is null", new Object[0]);
                return null;
            }
            if (packageInfo.reqFeatures != null) {
                FeatureInfo[] featureInfoArr = packageInfo.reqFeatures;
                int length = featureInfoArr.length;
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        break;
                    } else if (BundleMgrAdapter.USES_FEATRUE_ZIDANE.equals(featureInfoArr[i2].name)) {
                        z = true;
                        break;
                    } else {
                        i2++;
                    }
                }
            }
            z = false;
            if (!z) {
                return null;
            }
            if (packageInfo.applicationInfo == null) {
                AppLog.e(BundleMgrAdapter.BMS_ADAPTER_LABEL, "applicationInfo is null, pkg name:%{public}s", packageInfo.packageName);
                return null;
            } else if (packageInfo.signingInfo == null) {
                AppLog.e(BundleMgrAdapter.BMS_ADAPTER_LABEL, "signingInfo is null, pkg name:%{public}s", packageInfo.packageName);
                return null;
            } else {
                String generateAppId = PackageManagerAdapter.generateAppId(packageInfo);
                if (generateAppId.isEmpty()) {
                    AppLog.w(BundleMgrAdapter.BMS_ADAPTER_LABEL, "appId is empty, pkg name:%{public}s", packageInfo.packageName);
                }
                InstalledBundleInfo installedBundleInfo = new InstalledBundleInfo(packageInfo, generateAppId, i);
                AppLog.d(BundleMgrAdapter.BMS_ADAPTER_LABEL, "convertPackageInfo2BundleInfo, pkgName = %{public}s, userId = %{private}d", packageInfo.packageName, Integer.valueOf(i));
                return installedBundleInfo;
            }
        }

        private static void sendEvent() {
            HiView.report(new HiEvent((int) BundleMgrAdapter.EVENT_ID_INSTALL_FAILED).putString(BundleMgrAdapter.HIEVENT_KEY_BUNDLE_NAME, "").putInt(BundleMgrAdapter.HIEVENT_KEY_ERROR_TYPE, 6));
        }

        public List<UserInfo> getUserInfos(Context context) {
            if (context == null) {
                AppLog.e(BundleMgrAdapter.BMS_ADAPTER_LABEL, "getUserInfos failed, context is null", new Object[0]);
                return null;
            }
            Object systemService = context.getSystemService("user");
            if (systemService == null) {
                AppLog.e(BundleMgrAdapter.BMS_ADAPTER_LABEL, "getUserInfos failed, object is null", new Object[0]);
                return null;
            } else if (systemService instanceof UserManager) {
                return ((UserManager) systemService).getUsers();
            } else {
                AppLog.e(BundleMgrAdapter.BMS_ADAPTER_LABEL, "getUserInfos failed, object is not UserManager", new Object[0]);
                return null;
            }
        }

        public UserManager getUserManager(Context context) {
            if (context == null) {
                AppLog.e(BundleMgrAdapter.BMS_ADAPTER_LABEL, "getUserInfos failed, context is null", new Object[0]);
                return null;
            }
            Object systemService = context.getSystemService("user");
            if (systemService == null) {
                AppLog.e(BundleMgrAdapter.BMS_ADAPTER_LABEL, "getUserInfos failed, object is null", new Object[0]);
                return null;
            } else if (systemService instanceof UserManager) {
                return (UserManager) systemService;
            } else {
                AppLog.e(BundleMgrAdapter.BMS_ADAPTER_LABEL, "getUserInfos failed, object is not UserManager", new Object[0]);
                return null;
            }
        }

        public PackageManager getPackageManager(Context context) {
            if (context == null) {
                AppLog.e(BundleMgrAdapter.BMS_ADAPTER_LABEL, "getPackageManager failed, context is null", new Object[0]);
                return null;
            }
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                return packageManager;
            }
            AppLog.e(BundleMgrAdapter.BMS_ADAPTER_LABEL, "getPackageManager failed, pm is null", new Object[0]);
            return null;
        }

        public void closeStream(InputStream inputStream) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    AppLog.e("closeStream exception: %{private}", e.getMessage());
                }
            }
        }
    }

    public static class UsbStateBroadcastReceiver extends BroadcastReceiver {
        BundleMgrAdapter bundleMgrAdapter;

        public UsbStateBroadcastReceiver(BundleMgrAdapter bundleMgrAdapter2) {
            this.bundleMgrAdapter = bundleMgrAdapter2;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                boolean booleanExtra = intent.getBooleanExtra("connected", false);
                AppLog.i("Usb state broadcast receiver isConnect:%{public}d", Integer.valueOf(booleanExtra ? 1 : 0));
                BundleMgrAdapter bundleMgrAdapter2 = this.bundleMgrAdapter;
                if (bundleMgrAdapter2 != null) {
                    bundleMgrAdapter2.nativeSetUsbDebugState(booleanExtra);
                }
            }
        }
    }

    public static void initGrsClient() {
        BundleMgrAdapter instance2 = getInstance();
        if (instance2.bundleMgrContext == null) {
            AppLog.w(BMS_ADAPTER_LABEL, "startShortcut failed, bundleMgrContext is null", new Object[0]);
        } else {
            GrsServiceUtils.getInstance().initGrsClient(instance2.bundleMgrContext);
        }
    }
}
