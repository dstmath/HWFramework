package com.android.server.pm;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAddressNative;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser.Package;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.UserInfo;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.IPowerManager.Stub;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.SettingsEx.Systemex;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Flog;
import android.util.HwSlog;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import android.util.jar.StrictJarFile;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.content.NativeLibraryHelper.Handle;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.UserIcons;
import com.android.internal.util.XmlUtils;
import com.android.server.LocationManagerServiceUtil;
import com.android.server.PPPOEStateMachine;
import com.android.server.UiThread;
import com.android.server.am.HwActivityManagerService;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.pm.auth.HwCertificationManager;
import com.android.server.pm.auth.util.Utils;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.wifipro.WifiProCHRManager;
import com.android.server.wifipro.WifiProCommonDefs;
import com.huawei.android.hwutil.CommandLineUtil;
import com.huawei.cust.HwCustUtils;
import com.huawei.hsm.permission.StubController;
import com.huawei.permission.IHoldService;
import dalvik.system.VMRuntime;
import huawei.android.hwutil.ZipUtil;
import huawei.com.android.server.policy.HwGlobalActionsData;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import libcore.io.IoUtils;
import libcore.io.Streams;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class HwPackageManagerService extends PackageManagerService {
    private static final String ACTION_GET_INSTALLER_PACKAGE_INFO = "com.huawei.android.action.GET_INSTALLER_PACKAGE_INFO";
    private static final String ACTION_GET_PACKAGE_INSTALLATION_INFO = "com.huawei.android.action.GET_PACKAGE_INSTALLATION_INFO";
    private static final boolean ANTIMAL_DEBUG_ON;
    private static final String ANTIMAL_MODULE = "antiMalware";
    private static final String APK_INSTALLFILE = "xml/APKInstallListEMUI5Release.txt";
    private static final String BROADCAST_PERMISSION = "com.android.permission.system_manager_interface";
    private static final String CERT_TYPE_MEDIA = "media";
    private static final String CERT_TYPE_PLATFORM = "platform";
    private static final String CERT_TYPE_SHARED = "shared";
    private static final String CERT_TYPE_TESTKEY = "testkey";
    private static final String CLOUD_APK_CONFIG = "cloud_apk_config.txt";
    private static final String CLOUD_APK_DIR = "/data/system/";
    private static final String CUST_APP_DIR = "cust/app";
    private static final String CUST_DIR = "/data/hw_init/";
    private static final String CUST_SYS_APP_DIR = "system/app";
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_DATA_CUST;
    private static final boolean DEBUG_DEXOPT_OPTIMIZE;
    private static final boolean DEBUG_DEXOPT_SHELL;
    private static final int DEFAULT_PACKAGE_ABI = -1000;
    private static final String DELAPK_INSTALLFILE = "xml/DelAPKInstallListEMUI5Release.txt";
    private static final String DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final String DEXOPT_IN_BOOTUP_APKLIST = "/system/etc/dexopt/dexopt_in_bootup_apklist.cfg";
    private static final String FILE_MULTIWINDOW_WHITELIST = "multiwindow_whitelist_apps.xml";
    private static final String FILE_POLICY_CLASS_NAME = "com.huawei.cust.HwCfgFilePolicy";
    private static final String FLAG_APK_NOSYS = "nosys";
    private static final String FLAG_APK_PRIV = "priv";
    private static final String FLAG_APK_SYS = "sys";
    private static final String GMS_CORE_PATH = "/data/hw_init/system/app/GmsCore";
    private static final String GMS_FWK_PATH = "/data/hw_init/system/app/GoogleServicesFramework";
    private static final String GMS_LOG_PATH = "/data/hw_init/system/app/GoogleLoginService";
    private static final String GOOGLESETUP_PKG = "com.google.android.setupwizard";
    private static final long HOTA_DEXOPT_THRESHOLD = 18000000;
    private static final String HSM_PACKAGE = "com.huawei.systemmanager";
    private static final String HWSETUP_PKG = "com.huawei.hwstartupguide";
    private static final String INSERT_RESULT = "result";
    private static final String INSTALLATION_EXTRA_PACKAGE_INSTALLER_PID = "pkgInstallerPid";
    private static final String INSTALLATION_EXTRA_PACKAGE_INSTALLER_UID = "pkgInstallerUid";
    private static final String INSTALLATION_EXTRA_PACKAGE_INSTALL_RESULT = "pkgInstallResult";
    private static final String INSTALLATION_EXTRA_PACKAGE_META_HASH = "pkgMetaHash";
    private static final String INSTALLATION_EXTRA_PACKAGE_NAME = "pkgName";
    private static final String INSTALLATION_EXTRA_PACKAGE_UPDATE = "pkgUpdate";
    private static final String INSTALLATION_EXTRA_PACKAGE_URI = "pkgUri";
    private static final String INSTALLATION_EXTRA_PACKAGE_VERSION_CODE = "pkgVersionCode";
    private static final String INSTALLATION_EXTRA_PACKAGE_VERSION_NAME = "pkgVersionName";
    private static final int INSTALLER_ADB = 1;
    private static final int INSTALLER_OTHERS = 0;
    private static final String INSTALL_BEGIN = "begin";
    private static final String INSTALL_END = "end";
    private static final int LAST_DONE_VERSION = 10000;
    private static final String[] LIMITED_PACKAGE_NAMES;
    private static final String[] LIMITED_TARGET_PACKAGE_NAMES;
    private static int MAX_PKG = 0;
    private static final int MAX_PREPARE_USER_DATA_TIMES = 2;
    private static final int MAX_THEME_SIZE = 100000000;
    private static final String META_KEY_KEEP_ALIVE = "android.server.pm.KEEP_ALIVE";
    private static final String METHOD_NAME_FOR_FILE = "getCfgFile";
    private static final String NEVER_DEXOPT_APKLIST = "/system/etc/dexopt/never_dexopt_apklist.cfg";
    private static final int OPTIMIZE_FOR_BOOTING = 2;
    private static final int OPTIMIZE_FOR_OTA = 1;
    private static final int OPTIMIZE_FOR_OTHER = 4;
    private static final String PACKAGE_NAME = "pkg";
    private static final String PERINSTALL_FILE_LIST = "preinstalled_files_list.txt";
    private static final String PROPERTY_ANTIMAL_DEBUG = "persist.sys.antimal.debug";
    private static final long QUERY_RECENTLY_USED_THRESHOLD = 604800000;
    private static final String REBOOT_TIMES_WHEN_PREPARE_USER_DATA_KEY = "reboot_times_when_prepare_user_data";
    private static final String SOURCE_PACKAGE_NAME = "src";
    private static final String SYSTEM_APP_DIR = "/system/app";
    private static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    private static final String SYSTEM_SECURE = "ro.secure";
    private static final String TAG = "HwPackageManagerService";
    private static final String TAG_DATA_CUST = "HwPMS_DataCust";
    public static final int TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED = 1008;
    public static final int TRANSACTION_CODE_DELTE_GMS_FROM_UNINSTALLED_DELAPP = 1009;
    public static final int TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST = 1007;
    private static final String XML_ATTRIBUTE_PACKAGE_NAME = "package_name";
    private static final String XML_ELEMENT_APP_FORCED_PORTRAIT_ITEM = "mw_app_forced_portrait";
    private static final String XML_ELEMENT_APP_ITEM = "mw_app";
    private static final String XML_ELEMENT_APP_LIST = "multiwindow_whitelist";
    private static ArrayList<String> mCloudApks = null;
    private static HwCustPackageManagerService mCustPackageManagerService = null;
    private static List<String> mCustStoppedApps = null;
    private static HashMap<String, HashSet<String>> mDelMultiInstallMap = null;
    static final ArrayList<String> mDexoptInBootupApps;
    static final ArrayList<String> mForceNotDexApps;
    private static HwPackageManagerService mHwPackageManagerService = null;
    private static HashSet<String> mInstallSet = null;
    private static HashMap<String, HashSet<String>> mMultiInstallMap = null;
    private static final int mOptimizeForDexopt;
    private static ArrayMap<String, AbiInfo> mPackagesAbi = null;
    private static File mPakcageAbiFilename = null;
    static final StringBuilder mReadMessages;
    private static ArrayList<String> mRemoveablePreInstallApks = null;
    private static File mSystemDir = null;
    private static final int mThreadNum;
    private static String mUninstallApk = null;
    static final ArrayList<String> mUninstalledDelappList;
    private static List<String> preinstalledPackageList = null;
    private static List<String> sMWPortraitWhiteListPkgNames = null;
    private static List<String> sMultiWinWhiteListPkgNames = null;
    public static final int transaction_pmCheckGranted = 1005;
    public static final int transaction_pmCreateThemeFolder = 1003;
    public static final int transaction_pmGetResourcePackageName = 1004;
    public static final int transaction_pmInstallHwTheme = 1002;
    public static final int transaction_sendLimitedPackageBroadcast = 1006;
    public static final int transaction_setEnabledVisitorSetting = 1001;
    private boolean isBlackListExist;
    private BlackListInfo mBlackListInfo;
    private CertCompatSettings mCompatSettings;
    private Object mCust;
    private HwCustHwPackageManagerService mCustHwPms;
    private BlackListInfo mDisableAppListInfo;
    private HashMap<String, BlackListApp> mDisableAppMap;
    private boolean mFoundCertCompatFile;
    Package mGoogleServicePackage;
    private HashSet<String> mGrantedInstalledPkg;
    private boolean mHaveLoadedDexoptInBootUpApkList;
    private boolean mHaveLoadedNeverDexoptApkList;
    private Set<String> mIncompatNotificationList;
    private Set<String> mIncompatiblePkg;
    private final Installer mInstaller;
    ArrayList<UsageStats> mRecentlyUsedApps;
    ArrayList<Package> mSortDexoptApps;
    private boolean needCollectAppInfo;
    private Map<String, String> pkgMetaHash;
    final Comparator<UsageStats> totalTimeInForegroundComparator;

    /* renamed from: com.android.server.pm.HwPackageManagerService.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ long val$currentTime;
        final /* synthetic */ File val$file;
        final /* synthetic */ int val$flags;
        final /* synthetic */ int val$hwFlags;
        final /* synthetic */ int val$scanMode;

        AnonymousClass2(File val$file, int val$flags, int val$scanMode, long val$currentTime, int val$hwFlags) {
            this.val$file = val$file;
            this.val$flags = val$flags;
            this.val$scanMode = val$scanMode;
            this.val$currentTime = val$currentTime;
            this.val$hwFlags = val$hwFlags;
        }

        public void run() {
            try {
                HwPackageManagerService.this.scanPackageLI(this.val$file, this.val$flags, this.val$scanMode, this.val$currentTime, null, this.val$hwFlags);
            } catch (PackageManagerException e) {
                Slog.e(HwPackageManagerService.TAG, "Failed to parse package: " + e.getMessage());
            }
        }
    }

    /* renamed from: com.android.server.pm.HwPackageManagerService.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ boolean val$isSend;
        final /* synthetic */ String val$packageName;

        AnonymousClass4(String val$packageName, boolean val$isSend) {
            this.val$packageName = val$packageName;
            this.val$isSend = val$isSend;
        }

        public void run() {
            HwPackageManagerService.this.updateIncompatibleNotification(this.val$packageName, this.val$isSend);
        }
    }

    /* renamed from: com.android.server.pm.HwPackageManagerService.5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ String val$action;
        final /* synthetic */ Bundle val$extras;
        final /* synthetic */ String val$pkg;
        final /* synthetic */ String val$targetPkg;
        final /* synthetic */ int[] val$userIds;

        AnonymousClass5(int[] val$userIds, String val$action, String val$pkg, Bundle val$extras, String val$targetPkg) {
            this.val$userIds = val$userIds;
            this.val$action = val$action;
            this.val$pkg = val$pkg;
            this.val$extras = val$extras;
            this.val$targetPkg = val$targetPkg;
        }

        public void run() {
            try {
                IActivityManager am = ActivityManagerNative.getDefault();
                if (am != null) {
                    int[] resolvedUserIds;
                    if (this.val$userIds == null) {
                        resolvedUserIds = am.getRunningUserIds();
                    } else {
                        resolvedUserIds = this.val$userIds;
                    }
                    int length = resolvedUserIds.length;
                    for (int i = HwPackageManagerService.mThreadNum; i < length; i += HwPackageManagerService.OPTIMIZE_FOR_OTA) {
                        int id = resolvedUserIds[i];
                        Intent intent = new Intent(this.val$action, this.val$pkg != null ? Uri.fromParts(ControlScope.PACKAGE_ELEMENT_KEY, this.val$pkg, null) : null);
                        if (this.val$extras != null) {
                            intent.putExtras(this.val$extras);
                        }
                        if (this.val$targetPkg != null) {
                            intent.setPackage(this.val$targetPkg);
                        }
                        int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                        if (uid > 0 && UserHandle.getUserId(uid) != id) {
                            intent.putExtra("android.intent.extra.UID", UserHandle.getUid(id, UserHandle.getAppId(uid)));
                        }
                        intent.putExtra("android.intent.extra.user_handle", id);
                        am.broadcastIntent(null, intent, null, null, HwPackageManagerService.mThreadNum, null, null, null, -1, null, HwPackageManagerService.DEBUG_DEXOPT_SHELL, HwPackageManagerService.DEBUG_DEXOPT_SHELL, id);
                    }
                }
            } catch (RemoteException e) {
            }
        }
    }

    public static class AbiInfo {
        int abiCode;
        String name;
        int version;

        AbiInfo(String name, int abiCode, int version) {
            this.name = name;
            this.abiCode = abiCode;
            this.version = version;
        }

        int getAbiCode() {
            return this.abiCode;
        }

        String getName() {
            return this.name;
        }

        int getVersion() {
            return this.version;
        }

        void setVersion(int value) {
            this.version = value;
        }
    }

    static {
        boolean z;
        if (Log.HWINFO) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, OPTIMIZE_FOR_OTHER);
        } else {
            z = DEBUG_DEXOPT_SHELL;
        }
        DEBUG_DEXOPT_OPTIMIZE = z;
        if (Log.HWINFO) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, OPTIMIZE_FOR_OTHER);
        } else {
            z = DEBUG_DEXOPT_SHELL;
        }
        DEBUG_DEXOPT_SHELL = z;
        mUninstallApk = null;
        mUninstalledDelappList = new ArrayList();
        sMultiWinWhiteListPkgNames = new ArrayList();
        mCustStoppedApps = new ArrayList();
        mCustPackageManagerService = (HwCustPackageManagerService) HwCustUtils.createObj(HwCustPackageManagerService.class, new Object[mThreadNum]);
        mHwPackageManagerService = null;
        ANTIMAL_DEBUG_ON = SystemProperties.getInt(PROPERTY_ANTIMAL_DEBUG, mThreadNum) == OPTIMIZE_FOR_OTA ? true : DEBUG_DEXOPT_SHELL;
        mThreadNum = Runtime.getRuntime().availableProcessors() + OPTIMIZE_FOR_OTA;
        sMWPortraitWhiteListPkgNames = new ArrayList();
        mMultiInstallMap = null;
        mDelMultiInstallMap = null;
        MAX_PKG = 100;
        mInstallSet = new HashSet();
        z = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, OPTIMIZE_FOR_OTHER) : DEBUG_DEXOPT_SHELL : true;
        DEBUG_DATA_CUST = z;
        mCloudApks = new ArrayList();
        mRemoveablePreInstallApks = new ArrayList();
        mOptimizeForDexopt = SystemProperties.getInt("ro.config.DexOptForBooting", 7);
        mForceNotDexApps = new ArrayList();
        mDexoptInBootupApps = new ArrayList();
        mPakcageAbiFilename = null;
        mPackagesAbi = null;
        mSystemDir = null;
        mReadMessages = new StringBuilder();
        String[] strArr = new String[OPTIMIZE_FOR_OTHER];
        strArr[mThreadNum] = "com.huawei.android.totemweather";
        strArr[OPTIMIZE_FOR_OTA] = "com.huawei.camera";
        strArr[OPTIMIZE_FOR_BOOTING] = "com.android.calendar";
        strArr[3] = "com.android.soundrecorder";
        LIMITED_PACKAGE_NAMES = strArr;
        strArr = new String[OPTIMIZE_FOR_BOOTING];
        strArr[mThreadNum] = "com.google.android.wearable.app.cn";
        strArr[OPTIMIZE_FOR_OTA] = "com.google.android.wearable.app";
        LIMITED_TARGET_PACKAGE_NAMES = strArr;
        preinstalledPackageList = new ArrayList();
    }

    public static synchronized PackageManagerService getInstance(Context context, Installer installer, boolean factoryTest, boolean onlyCore) {
        synchronized (HwPackageManagerService.class) {
            if (mHwPackageManagerService == null) {
                initCustStoppedApps();
                loadMultiWinWhiteList(context);
                createPackagesAbiFile();
                PackageManagerService hwPackageManagerService = new HwPackageManagerService(context, installer, factoryTest, onlyCore);
                return hwPackageManagerService;
            }
            hwPackageManagerService = mHwPackageManagerService;
            return hwPackageManagerService;
        }
    }

    public HwPackageManagerService(Context context, Installer installer, boolean factoryTest, boolean onlyCore) {
        super(context, installer, factoryTest, onlyCore);
        this.mGrantedInstalledPkg = new HashSet();
        this.mCust = null;
        this.mCustHwPms = (HwCustHwPackageManagerService) HwCustUtils.createObj(HwCustHwPackageManagerService.class, new Object[mThreadNum]);
        this.mHaveLoadedNeverDexoptApkList = DEBUG_DEXOPT_SHELL;
        this.mHaveLoadedDexoptInBootUpApkList = DEBUG_DEXOPT_SHELL;
        this.mRecentlyUsedApps = new ArrayList();
        this.mSortDexoptApps = new ArrayList();
        this.totalTimeInForegroundComparator = new Comparator<UsageStats>() {
            public int compare(UsageStats usageStats1, UsageStats usageStats2) {
                long usageStats1Time = usageStats1.getTotalTimeInForeground();
                long usageStats2Time = usageStats2.getTotalTimeInForeground();
                if (usageStats1Time > usageStats2Time) {
                    return -1;
                }
                if (usageStats1Time < usageStats2Time) {
                    return HwPackageManagerService.OPTIMIZE_FOR_OTA;
                }
                return HwPackageManagerService.mThreadNum;
            }
        };
        this.mBlackListInfo = new BlackListInfo();
        this.mDisableAppListInfo = new BlackListInfo();
        this.mDisableAppMap = new HashMap();
        this.isBlackListExist = DEBUG_DEXOPT_SHELL;
        this.mIncompatiblePkg = new ArraySet();
        this.mIncompatNotificationList = new ArraySet();
        this.pkgMetaHash = new HashMap();
        this.needCollectAppInfo = true;
        this.mInstaller = installer;
        try {
            initBlackList();
        } catch (Exception e) {
            Slog.e(TAG, "initBlackList failed");
        }
    }

    private void setEnabledVisitorSetting(int newState, int flags, String callingPackage, int userId) {
        if (newState == 0 || newState == OPTIMIZE_FOR_OTA || newState == OPTIMIZE_FOR_BOOTING || newState == 3 || newState == OPTIMIZE_FOR_OTHER) {
            boolean sendNow = DEBUG_DEXOPT_SHELL;
            if (callingPackage == null) {
                callingPackage = Integer.toString(Binder.getCallingUid());
            }
            HashMap<String, ArrayList<String>> componentsMap = new HashMap();
            HashMap<String, Integer> pkgMap = new HashMap();
            String pkgNameList = Secure.getString(this.mContext.getContentResolver(), "privacy_app_list");
            if (pkgNameList == null) {
                Slog.e(TAG, " pkgNameList = null ");
                return;
            }
            if (pkgNameList.equals(AppHibernateCst.INVALID_PKG)) {
                Slog.e(TAG, " pkgNameList is null");
                return;
            }
            String[] pkgNameArray;
            int i;
            int length;
            String packageName;
            PackageSetting pkgSetting;
            if (pkgNameList.contains(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                pkgNameArray = pkgNameList.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            } else {
                pkgNameArray = new String[OPTIMIZE_FOR_OTA];
                pkgNameArray[mThreadNum] = pkgNameList;
            }
            for (i = mThreadNum; i < MAX_PKG && pkgNameArray != null; i += OPTIMIZE_FOR_OTA) {
                length = pkgNameArray.length;
                if (i >= r0) {
                    break;
                }
                packageName = pkgNameArray[i];
                String componentName = packageName;
                synchronized (this.mPackages) {
                    pkgSetting = (PackageSetting) this.mSettings.mPackages.get(packageName);
                    if (pkgSetting == null) {
                        pkgMap.put(packageName, Integer.valueOf(OPTIMIZE_FOR_OTA));
                    } else if (pkgSetting.getEnabled(userId) == newState) {
                        pkgMap.put(packageName, Integer.valueOf(OPTIMIZE_FOR_OTA));
                    } else {
                        if (newState == 0 || newState == OPTIMIZE_FOR_OTA) {
                            callingPackage = null;
                        }
                        pkgSetting.setEnabled(newState, userId, callingPackage);
                        pkgMap.put(packageName, Integer.valueOf(mThreadNum));
                        ArrayList<String> components = this.mPendingBroadcasts.get(userId, packageName);
                        boolean newPackage = components == null ? true : DEBUG_DEXOPT_SHELL;
                        if (newPackage) {
                            components = new ArrayList();
                        }
                        if (!components.contains(packageName)) {
                            components.add(packageName);
                        }
                        componentsMap.put(packageName, components);
                        if ((flags & OPTIMIZE_FOR_OTA) == 0) {
                            sendNow = true;
                            this.mPendingBroadcasts.remove(userId, packageName);
                        } else {
                            if (newPackage) {
                                this.mPendingBroadcasts.put(userId, packageName, components);
                            }
                            if (!this.mHandler.hasMessages(OPTIMIZE_FOR_OTA)) {
                                this.mHandler.sendEmptyMessageDelayed(OPTIMIZE_FOR_OTA, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                            }
                        }
                    }
                }
            }
            this.mSettings.writePackageRestrictionsLPr(userId);
            for (i = mThreadNum; i < MAX_PKG && pkgNameArray != null; i += OPTIMIZE_FOR_OTA) {
                length = pkgNameArray.length;
                if (i >= r0) {
                    break;
                }
                packageName = pkgNameArray[i];
                if (((Integer) pkgMap.get(packageName)).intValue() != OPTIMIZE_FOR_OTA) {
                    pkgSetting = (PackageSetting) this.mSettings.mPackages.get(packageName);
                    if (pkgSetting != null && sendNow) {
                        boolean z;
                        int packageUid = UserHandle.getUid(userId, pkgSetting.appId);
                        if ((flags & OPTIMIZE_FOR_OTA) != 0) {
                            z = OPTIMIZE_FOR_OTA;
                        } else {
                            z = mThreadNum;
                        }
                        sendPackageChangedBroadcast(packageName, z, (ArrayList) componentsMap.get(packageName), packageUid);
                    }
                }
            }
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case transaction_setEnabledVisitorSetting /*1001*/:
                Slog.w(TAG, "onTransact");
                data.enforceInterface(DESCRIPTOR);
                setEnabledVisitorSetting(data.readInt(), data.readInt(), null, data.readInt());
                reply.writeNoException();
                return true;
            case transaction_pmInstallHwTheme /*1002*/:
                Slog.w(TAG, "onTransact-pmInstallHwTheme");
                data.enforceInterface(DESCRIPTOR);
                boolean result = pmInstallHwTheme(data.readString(), data.readInt() != 0 ? true : DEBUG_DEXOPT_SHELL, data.readInt());
                reply.writeNoException();
                reply.writeInt(result ? OPTIMIZE_FOR_OTA : mThreadNum);
                return true;
            case transaction_pmCreateThemeFolder /*1003*/:
                Slog.w(TAG, "onTransact-transaction_pmCreateThemeFolder");
                data.enforceInterface(DESCRIPTOR);
                int userId = data.readInt();
                if (userId < 0) {
                    return DEBUG_DEXOPT_SHELL;
                }
                createThemeFolder(userId);
                reply.writeNoException();
                return true;
            case transaction_pmGetResourcePackageName /*1004*/:
                Slog.w(TAG, "onTransact-transaction_pmGetResourcePackageName");
                data.enforceInterface(DESCRIPTOR);
                String packageName = getResourcePackageNameByIcon(data.readString(), data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeString(packageName);
                return true;
            case transaction_pmCheckGranted /*1005*/:
                data.enforceInterface(DESCRIPTOR);
                boolean granted = checkInstallGranted(data.readString());
                reply.writeNoException();
                reply.writeInt(granted ? OPTIMIZE_FOR_OTA : mThreadNum);
                return true;
            case transaction_sendLimitedPackageBroadcast /*1006*/:
                data.enforceInterface(DESCRIPTOR);
                sendLimitedPackageBroadcast(data.readString(), data.readString(), data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null, data.readString(), data.createIntArray());
                reply.writeNoException();
                return true;
            case TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST /*1007*/:
                data.enforceInterface(DESCRIPTOR);
                List<String> list = getPreinstalledApkList();
                reply.writeNoException();
                reply.writeStringList(list);
                return true;
            case TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED /*1008*/:
                data.enforceInterface(DESCRIPTOR);
                boolean isUninstalled = checkGmsCoreUninstalled();
                reply.writeNoException();
                reply.writeInt(isUninstalled ? OPTIMIZE_FOR_OTA : mThreadNum);
                return true;
            case TRANSACTION_CODE_DELTE_GMS_FROM_UNINSTALLED_DELAPP /*1009*/:
                data.enforceInterface(DESCRIPTOR);
                deleteGmsCoreFromUninstalledDelapp();
                reply.writeNoException();
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    private String getResourcePackageNameByIcon(String pkgName, int icon, int userId) {
        String packageName = null;
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            Log.w(TAG, "packageManager is null !");
            return null;
        }
        try {
            return pm.getResourcesForApplicationAsUser(pkgName, userId).getResourcePackageName(icon);
        } catch (NameNotFoundException e) {
            Log.w(TAG, "packageName " + pkgName + ": Resources not found !");
            return null;
        } catch (NotFoundException e2) {
            Log.w(TAG, "packageName " + pkgName + ": ResourcesPackageName not found !");
            return null;
        } catch (RuntimeException e3) {
            Log.w(TAG, "RuntimeException in getResourcePackageNameByIcon !");
            return null;
        } catch (Throwable th) {
            return packageName;
        }
    }

    private boolean pmInstallHwTheme(String themePath, boolean setwallpaper, int userId) {
        if (setwallpaper) {
            rmSysWallpaper();
        }
        File file = null;
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_MEDIA_STORAGE") != 0) {
            throw new SecurityException("Permission Denial: can't install theme from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.WRITE_MEDIA_STORAGE");
        }
        if (themePath != null) {
            file = new File(themePath);
        }
        if (file == null || !file.exists()) {
            Log.w(TAG, "install theme failed, " + themePath + " not found");
            return DEBUG_DEXOPT_SHELL;
        } else if (((int) file.length()) > MAX_THEME_SIZE || ZipUtil.isZipError(themePath)) {
            return DEBUG_DEXOPT_SHELL;
        } else {
            try {
                createThemeFolder(userId);
                if (userId == 0 && isDataSkinExists()) {
                    renameDataSkinFolder(userId);
                } else {
                    createThemeTempFolder();
                    unzipThemePackage(file);
                    unzipCustThemePackage(getCustThemePath(themePath));
                    renameThemeTempFolder(userId);
                    renameKeyguardFile(userId);
                }
                restoreThemeCon(userId);
                deleteInstallFlag();
                if (this.mCustHwPms != null && this.mCustHwPms.isReginalPhoneFeature()) {
                    this.mCustHwPms.changeTheme(themePath, this.mContext);
                }
                return true;
            } catch (Exception e) {
                Log.w(TAG, "install theme failed, ", e);
                deleteThemeTempFolder();
                return DEBUG_DEXOPT_SHELL;
            }
        }
    }

    private void unzipCustThemePackage(File custThemeFile) {
        if (custThemeFile != null && custThemeFile.exists()) {
            unzipThemePackage(custThemeFile);
        }
    }

    private File getCustThemePath(String path) {
        File custDiffFile = null;
        if (path == null) {
            return custDiffFile;
        }
        String[] paths = path.split("/");
        String themeName = paths[paths.length - 1];
        String diffThemePath = SystemProperties.get("ro.config.diff_themes");
        if (!TextUtils.isEmpty(diffThemePath)) {
            return new File(diffThemePath + "/" + themeName);
        }
        try {
            custDiffFile = HwCfgFilePolicy.getCfgFile("themes/diff/" + themeName, mThreadNum);
        } catch (NoClassDefFoundError e) {
            Slog.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        return custDiffFile;
    }

    private String getHwThemePathAsUser(int userId) {
        return HwThemeManager.HWT_PATH_THEME + "/" + userId;
    }

    private boolean isDataSkinExists() {
        File file = new File(HwThemeManager.HWT_PATH_SKIN);
        if (!file.exists()) {
            return DEBUG_DEXOPT_SHELL;
        }
        try {
            return file.getCanonicalPath().equals(new File(file.getParentFile().getCanonicalFile(), file.getName()).getPath());
        } catch (IOException e) {
            return DEBUG_DEXOPT_SHELL;
        }
    }

    private void renameDataSkinFolder(int userId) {
        CommandLineUtil.rm("system", getHwThemePathAsUser(userId));
        CommandLineUtil.mv("system", HwThemeManager.HWT_PATH_SKIN, getHwThemePathAsUser(userId));
        CommandLineUtil.sync("system");
        CommandLineUtil.chmod("system", "0775", getHwThemePathAsUser(userId));
        CommandLineUtil.chown("system", "system", "media_rw", getHwThemePathAsUser(userId));
    }

    private void createFolder(String dir) {
        CommandLineUtil.mkdir("system", dir);
        CommandLineUtil.chmod("system", "0775", dir);
        CommandLineUtil.chown("system", "system", "media_rw", dir);
    }

    private void restoreThemeCon(int userId) {
        File themePath = new File(getHwThemePathAsUser(userId));
        if (themePath.exists() && !SELinux.restoreconRecursive(themePath)) {
            Log.w(TAG, "restoreconRecursive HWT_PATH_SKIN failed!");
        }
    }

    private void createThemeFolder(int userId) {
        createFolder(HwThemeManager.HWT_PATH_SKIN_INSTALL_FLAG);
        createFolder(HwThemeManager.HWT_PATH_THEME);
        createFolder(getHwThemePathAsUser(userId));
    }

    private void createThemeTempFolder() {
        CommandLineUtil.rm("system", "/data/skin.tmp");
        CommandLineUtil.mkdir("system", "/data/skin.tmp");
        CommandLineUtil.chmod("system", "0775", "/data/skin.tmp");
        CommandLineUtil.chown("system", "system", "media_rw", "/data/skin.tmp");
    }

    private void deleteThemeTempFolder() {
        CommandLineUtil.rm("system", "/data/skin.tmp");
        deleteInstallFlag();
    }

    private void deleteInstallFlag() {
        if (new File(HwThemeManager.HWT_PATH_SKIN_INSTALL_FLAG).exists()) {
            CommandLineUtil.rm("system", HwThemeManager.HWT_PATH_SKIN_INSTALL_FLAG);
        }
    }

    private void renameThemeTempFolder(int userId) {
        CommandLineUtil.rm("system", getHwThemePathAsUser(userId));
        CommandLineUtil.mv("system", "/data/skin.tmp", getHwThemePathAsUser(userId));
        CommandLineUtil.sync("system");
    }

    private void unzipThemePackage(File themeFile) {
        ZipUtil.unZipFile(themeFile, "/data/skin.tmp");
        CommandLineUtil.chmod("system", "0775", "/data/skin.tmp/*");
        CommandLineUtil.chown("system", "system", "media_rw", "/data/skin.tmp/*");
        CommandLineUtil.chown("system", "system", "media_rw", "/data/skin.tmp/**/*");
    }

    private void renameKeyguardFile(int userId) {
        if (!new File(getHwThemePathAsUser(userId), "com.android.keyguard").exists() && new File(getHwThemePathAsUser(userId), "com.huawei.android.hwlockscreen").exists()) {
            CommandLineUtil.mv("system", getHwThemePathAsUser(userId) + "/" + "com.huawei.android.hwlockscreen", getHwThemePathAsUser(userId) + "/" + "com.android.keyguard");
        }
    }

    private boolean rmSysWallpaper() {
        if (new File("/data/system/users/0/", "wallpaper").exists()) {
            CommandLineUtil.rm("system", "/data/system/users/0/wallpaper");
            if (this.mCustHwPms != null && this.mCustHwPms.isReginalPhoneFeature()) {
                CommandLineUtil.rm("system", "/data/system/users/0/wallpaper_orig");
            }
        }
        return true;
    }

    private static boolean firstScan() {
        boolean z;
        boolean exists = new File(Environment.getDataDirectory(), "system/packages.xml").exists();
        String str = TAG;
        StringBuilder append = new StringBuilder().append("is first scan?");
        if (exists) {
            z = DEBUG_DEXOPT_SHELL;
        } else {
            z = true;
        }
        Slog.i(str, append.append(z).toString());
        if (exists) {
            return DEBUG_DEXOPT_SHELL;
        }
        return true;
    }

    protected static void initCustStoppedApps() {
        if (firstScan()) {
            Slog.i(TAG, "first boot. init cust stopped apps.");
            File file = null;
            try {
                file = HwCfgFilePolicy.getCfgFile("xml/not_start_firstboot.xml", mThreadNum);
                if (file == null) {
                }
            } catch (NoClassDefFoundError e) {
                Slog.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            } finally {
                file = new File("/data/cust/", "xml/not_start_firstboot.xml");
            }
            try {
                FileReader xmlReader = new FileReader(file);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(xmlReader);
                    XmlUtils.beginDocument(parser, "resources");
                    while (true) {
                        XmlUtils.nextElement(parser);
                        if (parser.getName() == null) {
                            break;
                        } else if (ControlScope.PACKAGE_ELEMENT_KEY.equals(parser.getName()) && MemoryConstant.MEM_POLICY_ACTIONNAME.equals(parser.getAttributeName(mThreadNum))) {
                            String value = parser.getAttributeValue(mThreadNum);
                            if (value == null || AppHibernateCst.INVALID_PKG.equals(value)) {
                                mCustStoppedApps.clear();
                                Slog.e(TAG, "not_start_firstboot.xml bad format.");
                            } else {
                                mCustStoppedApps.add(value);
                                Slog.i(TAG, "cust stopped apps:" + value);
                            }
                        }
                    }
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e2) {
                            Slog.w(TAG, "Got execption when close black_package_name.xml!", e2);
                        }
                    }
                } catch (XmlPullParserException e3) {
                    Slog.w(TAG, "Got execption parsing black_package_name.xml!", e3);
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e22) {
                            Slog.w(TAG, "Got execption when close black_package_name.xml!", e22);
                        }
                    }
                } catch (IOException e222) {
                    Slog.w(TAG, "Got execption parsing black_package_name.xml!", e222);
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e2222) {
                            Slog.w(TAG, "Got execption when close black_package_name.xml!", e2222);
                        }
                    }
                } catch (Throwable th) {
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e22222) {
                            Slog.w(TAG, "Got execption when close black_package_name.xml!", e22222);
                        }
                    }
                }
                return;
            } catch (FileNotFoundException e4) {
                Slog.w(TAG, "There is no file named not_start_firstboot.xml!", e4);
                return;
            }
        }
        Slog.i(TAG, "not first boot. don't init cust stopped apps.");
    }

    public static boolean isCustedCouldStopped(String pkg, boolean block, boolean stopped) {
        boolean contain = mCustStoppedApps.contains(pkg);
        if (contain) {
            if (block) {
                Slog.i(TAG, "blocked broadcast send to system app:" + pkg + ", stopped?" + stopped);
            } else {
                Slog.i(TAG, "a system app is customized not to start at first boot. app:" + pkg);
            }
        }
        return contain;
    }

    protected ResolveInfo hwFindPreferredActivity(Intent intent, String resolvedType, int flags, List<ResolveInfo> query, int priority, boolean always, boolean removeMatches, boolean debug, int userId) {
        int index;
        int index2;
        ResolveInfo info;
        if (intent.hasCategory("android.intent.category.HOME") && query != null && query.size() > OPTIMIZE_FOR_OTA) {
            index = query.size() - 1;
            while (index >= 0) {
                index2 = index - 1;
                info = (ResolveInfo) query.get(index);
                if (info.activityInfo == null || !info.activityInfo.applicationInfo.packageName.equals("com.huawei.android.launcher")) {
                    index = index2;
                } else {
                    HwSlog.v(TAG, "Returning system default Launcher ");
                    return info;
                }
            }
        }
        if (!(intent.getAction() == null || !intent.getAction().equals("android.intent.action.DIAL") || intent.getData() == null || intent.getData().getScheme() == null || !intent.getData().getScheme().equals("tel") || query == null || query.size() <= OPTIMIZE_FOR_OTA)) {
            index = query.size() - 1;
            while (index >= 0) {
                index2 = index - 1;
                info = (ResolveInfo) query.get(index);
                if (info.priority >= 0 && info.activityInfo != null && info.activityInfo.applicationInfo.packageName.equals("com.android.contacts")) {
                    return info;
                }
                index = index2;
            }
        }
        if (!(intent.getAction() == null || !intent.getAction().equals("android.intent.action.VIEW") || intent.getData() == null || intent.getData().getScheme() == null || ((!intent.getData().getScheme().equals("file") && !intent.getData().getScheme().equals("content")) || intent.getType() == null || !intent.getType().startsWith("image/") || query == null || query.size() <= OPTIMIZE_FOR_OTA))) {
            index = query.size() - 1;
            while (index >= 0) {
                index2 = index - 1;
                info = (ResolveInfo) query.get(index);
                if (info.activityInfo != null && info.activityInfo.applicationInfo.packageName.equals("com.android.gallery3d")) {
                    return info;
                }
                index = index2;
            }
        }
        if (!(intent.getAction() == null || !intent.getAction().equals("android.intent.action.VIEW") || intent.getData() == null || intent.getData().getScheme() == null || ((!intent.getData().getScheme().equals("file") && !intent.getData().getScheme().equals("content")) || intent.getType() == null || !intent.getType().startsWith("audio/") || query == null || query.size() <= OPTIMIZE_FOR_OTA))) {
            index = query.size() - 1;
            while (index >= 0) {
                index2 = index - 1;
                info = (ResolveInfo) query.get(index);
                if (info.activityInfo != null && info.activityInfo.applicationInfo.packageName.equals("com.android.mediacenter")) {
                    return info;
                }
                index = index2;
            }
        }
        if (intent.getAction() != null && intent.getAction().equals("android.media.action.IMAGE_CAPTURE") && query != null && query.size() > OPTIMIZE_FOR_OTA) {
            index = query.size() - 1;
            while (index >= 0) {
                index2 = index - 1;
                info = (ResolveInfo) query.get(index);
                if (info.activityInfo != null && info.activityInfo.applicationInfo.packageName.equals("com.huawei.camera")) {
                    return info;
                }
                index = index2;
            }
        }
        if (!(intent.getAction() == null || !intent.getAction().equals("android.intent.action.VIEW") || intent.getData() == null || intent.getData().getScheme() == null || !intent.getData().getScheme().equals("mailto") || query == null || query.size() <= OPTIMIZE_FOR_OTA)) {
            index = query.size() - 1;
            while (index >= 0) {
                index2 = index - 1;
                info = (ResolveInfo) query.get(index);
                if (info.activityInfo != null && info.activityInfo.applicationInfo.packageName.equals("com.android.email")) {
                    return info;
                }
                index = index2;
            }
        }
        return null;
    }

    public void scanCustDir(int scanMode) {
        File mCustAppDir = new File("/data/cust/", "app");
        if (mCustAppDir.exists()) {
            scanDirLI(mCustAppDir, 65, scanMode, 0);
        }
        HwCustPackageManagerService custObj = (HwCustPackageManagerService) HwCustUtils.createObj(HwCustPackageManagerService.class, new Object[mThreadNum]);
        if (custObj != null) {
            custObj.scanCustPrivDir(scanMode, this);
        }
    }

    public void custScanPrivDir(File dir, int parseFlags, int scanFlags, long currentTime, int hwFlags) {
        scanDirLI(dir, parseFlags, scanFlags, currentTime, hwFlags);
    }

    protected void addPreferredActivityInternal(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, boolean always, int userId, String opname) {
        super.addPreferredActivityInternal(filter, match, set, activity, always, userId, opname);
        if (filter.hasCategory("android.intent.category.HOME")) {
            if (Global.getInt(this.mContext.getContentResolver(), "temporary_home_mode", mThreadNum) == OPTIMIZE_FOR_OTA) {
                Slog.i(TAG, "Skip killing last non default home because the new default home is temporary");
                return;
            }
            doKillNondefaultHome(activity.getPackageName(), userId);
        }
    }

    private void doKillNondefaultHome(String defaultHome, int userId) {
        List<ResolveInfo> resolveInfos = queryIntentActivitiesInternal(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT"), null, HwSecDiagnoseConstant.BIT_VERIFYBOOT, userId);
        int sz = resolveInfos.size();
        IActivityManager am = ActivityManagerNative.getDefault();
        for (int i = mThreadNum; i < sz; i += OPTIMIZE_FOR_OTA) {
            ResolveInfo info = (ResolveInfo) resolveInfos.get(i);
            String homePkg = info.activityInfo.packageName;
            Bundle metaData = info.activityInfo.metaData;
            boolean isKeepAlive = DEBUG_DEXOPT_SHELL;
            if (metaData != null) {
                isKeepAlive = metaData.getBoolean(META_KEY_KEEP_ALIVE, DEBUG_DEXOPT_SHELL);
            }
            if (!homePkg.equals(defaultHome)) {
                if (isKeepAlive) {
                    Slog.i(TAG, "Skip killing package : " + homePkg);
                } else if (am != null) {
                    try {
                        am.forceStopPackage(homePkg, userId);
                    } catch (RemoteException e) {
                        Slog.e(TAG, "Failed to kill home package of" + homePkg);
                    } catch (SecurityException e2) {
                        Slog.e(TAG, "Permission Denial, requires FORCE_STOP_PACKAGES");
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean parseCustPreInstallApks(File scanApk) {
        InputStreamReader inStreamReader;
        Throwable th;
        if (scanApk == null) {
            Slog.i(TAG_DATA_CUST, "Invalid input arg (scanApk) null");
            return DEBUG_DEXOPT_SHELL;
        }
        String APK_DIR_TAG = "APK_DIR:";
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        FileInputStream fileInputStream = null;
        boolean result = true;
        try {
            FileInputStream fileInStream = new FileInputStream(scanApk);
            try {
                inStreamReader = new InputStreamReader(fileInStream, "UTF-8");
            } catch (FileNotFoundException e) {
                fileInputStream = fileInStream;
                result = DEBUG_DEXOPT_SHELL;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e2) {
                    }
                }
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (Exception e3) {
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Exception e4) {
                        result = DEBUG_DEXOPT_SHELL;
                        return result;
                    }
                }
                return result;
            } catch (IOException e5) {
                fileInputStream = fileInStream;
                result = DEBUG_DEXOPT_SHELL;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e6) {
                    }
                }
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (Exception e7) {
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Exception e8) {
                        result = DEBUG_DEXOPT_SHELL;
                        return result;
                    }
                }
                return result;
            } catch (Exception e9) {
                fileInputStream = fileInStream;
                result = DEBUG_DEXOPT_SHELL;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e10) {
                    }
                }
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (Exception e11) {
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Exception e12) {
                        result = DEBUG_DEXOPT_SHELL;
                        return result;
                    }
                }
                return result;
            } catch (Throwable th2) {
                th = th2;
                fileInputStream = fileInStream;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e13) {
                    }
                }
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (Exception e14) {
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Exception e15) {
                    }
                }
                throw th;
            }
            try {
                BufferedReader reader = new BufferedReader(inStreamReader);
                while (true) {
                    try {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        line = line.trim();
                        if (DEBUG_DATA_CUST) {
                            Slog.i(TAG_DATA_CUST, "line: " + line.trim());
                        }
                        int startIndex = line.indexOf("APK_DIR:") + "APK_DIR:".length();
                        if (startIndex >= 0) {
                            line = line.substring(startIndex, line.indexOf(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER));
                            line = line.substring(mThreadNum, line.indexOf(","));
                            String pkgPath = line;
                            if (line.endsWith(".apk")) {
                                pkgPath = line.substring(mThreadNum, line.lastIndexOf("/"));
                            }
                            File pkg = new File(pkgPath);
                            if (pkg.exists()) {
                                synchronized (mInstallSet) {
                                    mInstallSet.add(pkg.getAbsolutePath());
                                }
                                if (DEBUG_DATA_CUST) {
                                    Slog.i(TAG_DATA_CUST, "mInstallSet.add: " + pkg.getAbsolutePath());
                                }
                            } else {
                                Slog.w(TAG_DATA_CUST, "ignore (" + pkgPath + ") for not exist.");
                            }
                        }
                    } catch (FileNotFoundException e16) {
                        fileInputStream = fileInStream;
                        inputStreamReader = inStreamReader;
                        bufferedReader = reader;
                    } catch (IOException e17) {
                        fileInputStream = fileInStream;
                        inputStreamReader = inStreamReader;
                        bufferedReader = reader;
                    } catch (Exception e18) {
                        fileInputStream = fileInStream;
                        inputStreamReader = inStreamReader;
                        bufferedReader = reader;
                    } catch (Throwable th3) {
                        th = th3;
                        fileInputStream = fileInStream;
                        inputStreamReader = inStreamReader;
                        bufferedReader = reader;
                    }
                }
            } catch (FileNotFoundException e19) {
                fileInputStream = fileInStream;
                inputStreamReader = inStreamReader;
                result = DEBUG_DEXOPT_SHELL;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return result;
            } catch (IOException e20) {
                fileInputStream = fileInStream;
                inputStreamReader = inStreamReader;
                result = DEBUG_DEXOPT_SHELL;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return result;
            } catch (Exception e21) {
                fileInputStream = fileInStream;
                inputStreamReader = inStreamReader;
                result = DEBUG_DEXOPT_SHELL;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return result;
            } catch (Throwable th4) {
                th = th4;
                fileInputStream = fileInStream;
                inputStreamReader = inStreamReader;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e22) {
            result = DEBUG_DEXOPT_SHELL;
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return result;
        } catch (IOException e23) {
            result = DEBUG_DEXOPT_SHELL;
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return result;
        } catch (Exception e24) {
            result = DEBUG_DEXOPT_SHELL;
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return result;
        } catch (Throwable th5) {
            th = th5;
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
    }

    public void scanHwCustAppDir(int scanMode) {
        if (!parseCustPreInstallApks(new File(CUST_DIR, PERINSTALL_FILE_LIST))) {
            Slog.e(TAG_DATA_CUST, "parse preinstalled_files_list failed. skip all the packages.");
        } else if (mInstallSet != null) {
            if (DEBUG_DATA_CUST) {
                Slog.i(TAG_DATA_CUST, "CUST APK BEGIN");
            }
            scanDirLI(new File(CUST_DIR, CUST_SYS_APP_DIR), 65, scanMode, 0, 167772160);
            scanDirLI(new File(CUST_DIR, CUST_APP_DIR), 65, scanMode, 0, 167772160);
            if (DEBUG_DATA_CUST) {
                Slog.i(TAG_DATA_CUST, "CUST APK END");
            }
        }
    }

    private boolean isDelappInCust(String scanFileString) {
        if (scanFileString == null) {
            Slog.w(TAG_DATA_CUST, "Invalid input arg (scanFileString) null");
            return DEBUG_DEXOPT_SHELL;
        }
        if (DEBUG_DATA_CUST) {
            Slog.i(TAG_DATA_CUST, "isDelapp scanFile: " + scanFileString);
        }
        if (mInstallSet == null || !mInstallSet.contains(scanFileString)) {
            return DEBUG_DEXOPT_SHELL;
        }
        return true;
    }

    public boolean isDelappInCust(PackageSetting ps) {
        if (ps == null || ps.codePath == null) {
            Slog.w(TAG_DATA_CUST, "Invalid input arg (ps) null");
            return DEBUG_DEXOPT_SHELL;
        }
        String codePath = ps.codePath.toString();
        if (mInstallSet != null && mInstallSet.contains(codePath)) {
            return true;
        }
        if (DEBUG_DATA_CUST) {
            Slog.i(TAG_DATA_CUST, "codePath: " + codePath);
        }
        return DEBUG_DEXOPT_SHELL;
    }

    public boolean isCustApkRecorded(File file) {
        if (file == null) {
            Slog.w(TAG_DATA_CUST, "Invalid input arg (file) null");
            return DEBUG_DEXOPT_SHELL;
        }
        Slog.i(TAG_DATA_CUST, " isCustApkRecorded codePath: " + file.getAbsolutePath());
        if (file.isDirectory() && mInstallSet != null && mInstallSet.contains(file.getAbsolutePath())) {
            return true;
        }
        return DEBUG_DEXOPT_SHELL;
    }

    public void scanRemovableAppDir(int scanMode) {
        File[] apps = getRemovableAppDirs();
        for (int i = mThreadNum; i < apps.length; i += OPTIMIZE_FOR_OTA) {
            File app = apps[i];
            if (app != null && app.exists()) {
                scanDirLI(app, 65, scanMode, 0, HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM);
            }
        }
    }

    private File[] getRemovableAppDirs() {
        File mPreRemovableAppDir1 = new File("/data/cust/", "delapp");
        File mPreRemovableAppDir2 = new File("/system/", "delapp");
        File[] fileArr = new File[OPTIMIZE_FOR_BOOTING];
        fileArr[mThreadNum] = mPreRemovableAppDir1;
        fileArr[OPTIMIZE_FOR_OTA] = mPreRemovableAppDir2;
        return fileArr;
    }

    public void recordUninstalledDelapp(String s) {
        IOException e;
        Exception e2;
        Throwable th;
        File file = new File(CLOUD_APK_DIR, "uninstalled_delapp.xml");
        loadUninstalledDelapp(file);
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream stream = new FileOutputStream(file, DEBUG_DEXOPT_SHELL);
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(stream, "utf-8");
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, "values");
                if (s != null) {
                    out.startTag(null, "string");
                    out.attribute(null, MemoryConstant.MEM_POLICY_ACTIONNAME, s);
                    out.endTag(null, "string");
                }
                int N = mUninstalledDelappList.size();
                for (int i = mThreadNum; i < N; i += OPTIMIZE_FOR_OTA) {
                    out.startTag(null, "string");
                    out.attribute(null, MemoryConstant.MEM_POLICY_ACTIONNAME, (String) mUninstalledDelappList.get(i));
                    out.endTag(null, "string");
                }
                out.endTag(null, "values");
                out.endDocument();
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e3) {
                    }
                }
                fileOutputStream = stream;
            } catch (IOException e4) {
                e = e4;
                fileOutputStream = stream;
                Slog.w(TAG, "failed parsing " + file + " " + e);
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e5) {
                    }
                }
            } catch (Exception e6) {
                e2 = e6;
                fileOutputStream = stream;
                try {
                    Slog.w(TAG, "failed parsing " + file + " " + e2);
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e7) {
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e8) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = stream;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (IOException e9) {
            e = e9;
            Slog.w(TAG, "failed parsing " + file + " " + e);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (Exception e10) {
            e2 = e10;
            Slog.w(TAG, "failed parsing " + file + " " + e2);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadUninstalledDelapp(File file) {
        FileNotFoundException e;
        Throwable th;
        XmlPullParserException e2;
        IOException e3;
        mUninstalledDelappList.clear();
        FileInputStream fileInputStream = null;
        try {
            FileInputStream stream = new FileInputStream(file);
            try {
                int type;
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, null);
                do {
                    type = parser.next();
                    if (type == OPTIMIZE_FOR_OTA) {
                        break;
                    }
                } while (type != OPTIMIZE_FOR_BOOTING);
                String tag = parser.getName();
                if ("values".equals(tag)) {
                    type = parser.next();
                    int outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type == OPTIMIZE_FOR_OTA || (type == 3 && parser.getDepth() <= outerDepth)) {
                            if (stream != null) {
                                try {
                                    stream.close();
                                } catch (IOException e4) {
                                }
                            }
                        } else if (!(type == 3 || type == OPTIMIZE_FOR_OTHER || !"string".equals(parser.getName()) || parser.getAttributeValue(mThreadNum) == null)) {
                            mUninstalledDelappList.add(parser.getAttributeValue(mThreadNum));
                        }
                    }
                    if (stream != null) {
                        stream.close();
                    }
                    return;
                }
                throw new XmlPullParserException("Settings do not start with policies tag: found " + tag);
            } catch (FileNotFoundException e5) {
                e = e5;
                fileInputStream = stream;
                try {
                    Slog.w(TAG, "file is not exist " + e);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e6) {
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e7) {
                        }
                    }
                    throw th;
                }
            } catch (XmlPullParserException e8) {
                e2 = e8;
                fileInputStream = stream;
                Slog.w(TAG, "failed parsing " + file + " " + e2);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e9) {
                    }
                }
            } catch (IOException e10) {
                e3 = e10;
                fileInputStream = stream;
                Slog.w(TAG, "failed parsing " + file + " " + e3);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e11) {
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = stream;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e12) {
            e = e12;
            Slog.w(TAG, "file is not exist " + e);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (XmlPullParserException e13) {
            e2 = e13;
            Slog.w(TAG, "failed parsing " + file + " " + e2);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (IOException e14) {
            e3 = e14;
            Slog.w(TAG, "failed parsing " + file + " " + e3);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    private boolean isUninstalledDelapp(String s) {
        int i;
        if (this.mIsPreNUpgrade) {
            Slog.w(TAG, "Compatible Fix for pre-N update verify uninstalled App: " + s);
            File file_ext = new File("/data/data/", "uninstalled_delapp.xml");
            if (file_ext.exists()) {
                loadUninstalledDelapp(file_ext);
                int Num = mUninstalledDelappList.size();
                for (i = mThreadNum; i < Num; i += OPTIMIZE_FOR_OTA) {
                    if (s.equals(mUninstalledDelappList.get(i))) {
                        Slog.w(TAG, "Compatible Fix App Found Deleted in M : " + s);
                        recordUninstalledDelapp(s);
                        return true;
                    }
                }
            }
        }
        File file = new File(CLOUD_APK_DIR, "uninstalled_delapp.xml");
        if (!file.exists()) {
            return DEBUG_DEXOPT_SHELL;
        }
        loadUninstalledDelapp(file);
        int N = mUninstalledDelappList.size();
        for (i = mThreadNum; i < N; i += OPTIMIZE_FOR_OTA) {
            if (s.equals(mUninstalledDelappList.get(i))) {
                return true;
            }
        }
        return DEBUG_DEXOPT_SHELL;
    }

    private boolean isApplicationInstalled(Package pkg) {
        PackageSetting p = (PackageSetting) this.mSettings.mPackages.get(pkg.applicationInfo.packageName);
        if (getApplicationInfo(pkg.applicationInfo.packageName, 8192, UserHandle.getCallingUserId()) == null || p == null) {
            return DEBUG_DEXOPT_SHELL;
        }
        return true;
    }

    public boolean isDelapp(PackageSetting ps) {
        File[] dirs = getRemovableAppDirs();
        String codePath = ps.codePath.toString();
        for (int i = mThreadNum; i < dirs.length; i += OPTIMIZE_FOR_OTA) {
            File dir = dirs[i];
            if (dir != null && dir.exists()) {
                String[] files = dir.list();
                for (int j = mThreadNum; j < files.length; j += OPTIMIZE_FOR_OTA) {
                    File file = new File(dir, files[j]);
                    String[] filesSub = file.list();
                    if (file.getPath().equals(codePath)) {
                        for (int k = mThreadNum; k < filesSub.length; k += OPTIMIZE_FOR_OTA) {
                            if (isPackageFilename(filesSub[k])) {
                                return true;
                            }
                        }
                        continue;
                    }
                }
                continue;
            }
        }
        return DEBUG_DEXOPT_SHELL;
    }

    public boolean isSystemPathApp(PackageSetting ps) {
        File[] dirs = getSystemPathAppDirs();
        String codePath = ps.codePath.toString();
        for (int i = mThreadNum; i < dirs.length; i += OPTIMIZE_FOR_OTA) {
            File dir = dirs[i];
            if (dir != null && dir.exists()) {
                String[] files = dir.list();
                for (int j = mThreadNum; j < files.length; j += OPTIMIZE_FOR_OTA) {
                    File file = new File(dir, files[j]);
                    String[] filesSub = file.list();
                    if (file.getPath().equals(codePath)) {
                        for (int k = mThreadNum; k < filesSub.length; k += OPTIMIZE_FOR_OTA) {
                            if (isPackageFilename(filesSub[k])) {
                                return true;
                            }
                        }
                        continue;
                    }
                }
                continue;
            }
        }
        return DEBUG_DEXOPT_SHELL;
    }

    private File[] getSystemPathAppDirs() {
        File systemPrivAppDir = new File("/system/priv-app");
        File systemAppDir = new File(SYSTEM_APP_DIR);
        File[] fileArr = new File[OPTIMIZE_FOR_BOOTING];
        fileArr[mThreadNum] = systemPrivAppDir;
        fileArr[OPTIMIZE_FOR_OTA] = systemAppDir;
        return fileArr;
    }

    public static boolean isPrivAppInData(File path, String apkListFile) {
        Throwable th;
        BufferedReader bufferedReader = null;
        boolean z = DEBUG_DEXOPT_SHELL;
        try {
            if (mCustPackageManagerService != null && mCustPackageManagerService.isMccMncMatch()) {
                apkListFile = mCustPackageManagerService.getCustomizeAPKListFile(apkListFile, "APKInstallListEMUI5Release.txt", "DelAPKInstallListEMUI5Release.txt", "/data/cust/xml");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(apkListFile), "UTF-8"));
            while (true) {
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    String[] strSplit = line.trim().split(",");
                    if (OPTIMIZE_FOR_BOOTING == strSplit.length && isPackageFilename(strSplit[mThreadNum].trim()) && strSplit[OPTIMIZE_FOR_OTA].trim().equalsIgnoreCase(FLAG_APK_PRIV)) {
                        z = path.getCanonicalPath().startsWith(strSplit[mThreadNum].trim());
                        if (z) {
                            break;
                        }
                    }
                } catch (FileNotFoundException e) {
                    bufferedReader = reader;
                } catch (IOException e2) {
                    bufferedReader = reader;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = reader;
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e3) {
                }
            }
            bufferedReader = reader;
        } catch (FileNotFoundException e4) {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e5) {
                }
            }
            return z;
        } catch (IOException e6) {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e7) {
                }
            }
            return z;
        } catch (Throwable th3) {
            th = th3;
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e8) {
                }
            }
            throw th;
        }
        return z;
    }

    public void scanDataDir(int scanMode) {
    }

    private void getMultiAPKInstallList(List<File> lists, HashMap<String, HashSet<String>> multiInstallMap) {
        if (multiInstallMap != null && lists.size() > 0) {
            for (File list : lists) {
                getAPKInstallList(list, multiInstallMap);
            }
        }
    }

    private void getAPKInstallList(File scanApk, HashMap<String, HashSet<String>> multiInstallMap) {
        Throwable th;
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(scanApk), "UTF-8"));
            while (true) {
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    String[] strSplit = line.trim().split(",");
                    String packagePath = getCustPackagePath(strSplit[mThreadNum]);
                    if (packagePath != null && isPackageFilename(strSplit[mThreadNum].trim())) {
                        if (OPTIMIZE_FOR_BOOTING == strSplit.length) {
                            ((HashSet) multiInstallMap.get(strSplit[OPTIMIZE_FOR_OTA].trim())).add(packagePath.trim());
                        } else if (OPTIMIZE_FOR_OTA == strSplit.length) {
                            ((HashSet) multiInstallMap.get(FLAG_APK_SYS)).add(packagePath.trim());
                        }
                    }
                } catch (FileNotFoundException e) {
                    bufferedReader = reader;
                } catch (IOException e2) {
                    bufferedReader = reader;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = reader;
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e3) {
                    Log.e(TAG, "PackageManagerService.getAPKInstallList error for closing IO");
                }
            }
        } catch (FileNotFoundException e4) {
            try {
                Log.w(TAG, "FileNotFound No such file or directory :" + scanApk.getPath());
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e5) {
                        Log.e(TAG, "PackageManagerService.getAPKInstallList error for closing IO");
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e6) {
                        Log.e(TAG, "PackageManagerService.getAPKInstallList error for closing IO");
                    }
                }
                throw th;
            }
        } catch (IOException e7) {
            Log.e(TAG, "PackageManagerService.getAPKInstallList error for IO");
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e8) {
                    Log.e(TAG, "PackageManagerService.getAPKInstallList error for closing IO");
                }
            }
        }
    }

    private void installAPKforInstallList(HashSet<String> installList, int flags, int scanMode, long currentTime) {
        installAPKforInstallList(installList, flags, scanMode, currentTime, mThreadNum);
    }

    private void installAPKforInstallList(HashSet<String> installList, int flags, int scanMode, long currentTime, int hwFlags) {
        ExecutorService executorService = Executors.newFixedThreadPool(mThreadNum);
        for (String installPath : installList) {
            Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "package install path : " + installPath);
            File file = new File(installPath);
            if (this.mIsPackageScanMultiThread) {
                try {
                    executorService.submit(new AnonymousClass2(file, flags, scanMode, currentTime, hwFlags));
                } catch (Exception e) {
                    this.mIsPackageScanMultiThread = DEBUG_DEXOPT_SHELL;
                }
            }
            if (!this.mIsPackageScanMultiThread) {
                try {
                    scanPackageLI(file, flags, scanMode, currentTime, null, hwFlags);
                } catch (PackageManagerException e2) {
                    Slog.e(TAG, "Failed to parse package: " + e2.getMessage());
                }
            }
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e3) {
        }
    }

    public boolean isUninstallApk(String filePath) {
        if (mUninstallApk != null) {
            return mUninstallApk.contains(filePath);
        }
        return DEBUG_DEXOPT_SHELL;
    }

    public static void setUninstallApk(String string) {
        if (mUninstallApk != null) {
            mUninstallApk += CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER + string;
        } else {
            mUninstallApk = string;
        }
    }

    public static void restoreUninstallApk(String restoreApk) {
        if (mUninstallApk != null && restoreApk != null) {
            String[] restoreApks = Pattern.compile("\\s*|\n|\r|\t").matcher(restoreApk).replaceAll(AppHibernateCst.INVALID_PKG).split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            int length = restoreApks.length;
            for (int i = mThreadNum; i < length; i += OPTIMIZE_FOR_OTA) {
                mUninstallApk = mUninstallApk.replaceAll(restoreApks[i], AppHibernateCst.INVALID_PKG);
            }
        }
    }

    public void getUninstallApk() {
        ArrayList<File> allList = new ArrayList();
        try {
            allList = HwCfgFilePolicy.getCfgFileList("xml/unstall_apk.xml", mThreadNum);
        } catch (NoClassDefFoundError e) {
            Slog.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (allList.size() > 0) {
            for (File list : allList) {
                loadUninstallApps(list);
            }
        }
        try {
            if (!new File(NFC_DEVICE_PATH).exists()) {
                if (this.mAvailableFeatures.containsKey("android.hardware.nfc")) {
                    this.mAvailableFeatures.remove("android.hardware.nfc");
                }
                if (this.mAvailableFeatures.containsKey("android.hardware.nfc.hce")) {
                    this.mAvailableFeatures.remove("android.hardware.nfc.hce");
                }
                if (this.mAvailableFeatures.containsKey("android.hardware.nfc.hcef")) {
                    this.mAvailableFeatures.remove("android.hardware.nfc.hcef");
                }
                if (mUninstallApk == null || AppHibernateCst.INVALID_PKG.equals(mUninstallApk)) {
                    mUninstallApk = "/system/app/NfcNci_45.apk;/system/app/HwNfcTag.apk";
                } else if (!mUninstallApk.contains("/system/app/NfcNci_45.apk")) {
                    mUninstallApk += CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER + "/system/app/NfcNci_45.apk" + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER + "/system/app/HwNfcTag.apk";
                }
            }
        } catch (Exception e2) {
        }
    }

    private void loadUninstallApps(File list) {
        Throwable th;
        File file = list;
        if (getCust() != null) {
            file = getCust().customizeUninstallApk(list);
        }
        if (file.exists()) {
            FileInputStream fileInputStream = null;
            try {
                FileInputStream in = new FileInputStream(file);
                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();
                    xpp.setInput(in, null);
                    for (int eventType = xpp.getEventType(); eventType != OPTIMIZE_FOR_OTA; eventType = xpp.next()) {
                        if (eventType == OPTIMIZE_FOR_BOOTING) {
                            if ("apk".equals(xpp.getName())) {
                                setUninstallApk(xpp.nextText());
                            } else if ("restoreapk".equals(xpp.getName())) {
                                restoreUninstallApk(xpp.nextText());
                            }
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                    }
                } catch (XmlPullParserException e2) {
                    fileInputStream = in;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e3) {
                        }
                    }
                } catch (IOException e4) {
                    fileInputStream = in;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e5) {
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    fileInputStream = in;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e6) {
                        }
                    }
                    throw th;
                }
            } catch (XmlPullParserException e7) {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e8) {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (Throwable th3) {
                th = th3;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        }
    }

    public boolean isDelappInData(PackageSetting ps) {
        if (ps == null || ps.codePath == null) {
            return DEBUG_DEXOPT_SHELL;
        }
        return isDelappInData(ps.codePath.toString());
    }

    public static boolean isPrivAppInCust(File file) {
        HwCustPackageManagerService custObj = (HwCustPackageManagerService) HwCustUtils.createObj(HwCustPackageManagerService.class, new Object[mThreadNum]);
        if (custObj != null) {
            return custObj.isPrivAppInCust(file);
        }
        return DEBUG_DEXOPT_SHELL;
    }

    public void systemReady() {
        super.systemReady();
        if (HwCertificationManager.hasFeature()) {
            if (!HwCertificationManager.isInitialized()) {
                HwCertificationManager.initialize(this.mContext);
            }
            HwCertificationManager.getIntance().systemReady();
        }
        AntiMalPreInstallScanner.getInstance().systemReady();
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                    HwPackageManagerService.this.onRemoveUser(intent.getIntExtra("android.intent.extra.user_handle", -10000));
                }
            }
        }, userFilter);
        if (this.mCustHwPms != null && this.mCustHwPms.isReginalPhoneFeature() && this.mCustHwPms.isCustChange(this.mContext) && !this.mCustHwPms.isThemeChange(this.mContext)) {
            pmInstallHwTheme(Systemex.getString(this.mContext.getContentResolver(), "hw_def_theme"), true, mThreadNum);
            HwThemeManager.updateConfiguration();
        }
        writePreinstalledApkListToFile();
    }

    public void addFlagsForRemovablePreApk(Package pkg, int hwFlags) {
        if ((hwFlags & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) != 0) {
            ApplicationInfo applicationInfo = pkg.applicationInfo;
            applicationInfo.hwFlags |= HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM;
        }
    }

    public boolean needInstallRemovablePreApk(Package pkg, int hwFlags) {
        if ((HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM & hwFlags) == 0 || isApplicationInstalled(pkg) || !isUninstalledDelapp(pkg.packageName)) {
            return true;
        }
        Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "needInstallRemovablePreApk :" + pkg.packageName);
        return DEBUG_DEXOPT_SHELL;
    }

    public void setGMSPackage(Package pkg) {
        if (pkg.packageName.equals("com.google.android.gsf") && isSystemApp(pkg)) {
            this.mGoogleServicePackage = pkg;
        }
    }

    private static boolean isSystemApp(Package pkg) {
        return (pkg.applicationInfo.flags & OPTIMIZE_FOR_OTA) != 0 ? true : DEBUG_DEXOPT_SHELL;
    }

    public boolean getGMSPackagePermission(Package pkg) {
        return (this.mGoogleServicePackage == null || compareSignatures(this.mGoogleServicePackage.mSignatures, pkg.mSignatures) != 0) ? DEBUG_DEXOPT_SHELL : true;
    }

    protected void readCloudApkConfig() {
        FileNotFoundException e;
        Exception e2;
        Throwable th;
        ArrayList<String> cloudApks = mCloudApks;
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(CLOUD_APK_DIR, CLOUD_APK_CONFIG)), Charset.defaultCharset()));
            while (true) {
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    cloudApks.add(line.trim());
                } catch (FileNotFoundException e3) {
                    e = e3;
                    bufferedReader = reader;
                } catch (Exception e4) {
                    e2 = e4;
                    bufferedReader = reader;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = reader;
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e22) {
                    e22.printStackTrace();
                }
            }
        } catch (FileNotFoundException e5) {
            e = e5;
            try {
                e.printStackTrace();
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e222) {
                        e222.printStackTrace();
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e2222) {
                        e2222.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (Exception e6) {
            e2222 = e6;
            e2222.printStackTrace();
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e22222) {
                    e22222.printStackTrace();
                }
            }
        }
    }

    private void writeCloudApkConfig(String packageName) {
        Throwable th;
        ArrayList<String> cloudApks = mCloudApks;
        cloudApks.add(packageName);
        PrintWriter printWriter = null;
        try {
            File file = new File(CLOUD_APK_DIR, CLOUD_APK_CONFIG);
            if (!(file.exists() || file.createNewFile())) {
                Slog.e(TAG, "Create a cloud apk config file failed!");
            }
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.defaultCharset()));
            try {
                int size = cloudApks.size();
                for (int i = mThreadNum; i < size; i += OPTIMIZE_FOR_OTA) {
                    writer.println((String) cloudApks.get(i));
                }
                writer.flush();
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Exception e) {
                    }
                }
                printWriter = writer;
            } catch (FileNotFoundException e2) {
                printWriter = writer;
                if (printWriter != null) {
                    try {
                        printWriter.close();
                    } catch (Exception e3) {
                    }
                }
            } catch (Exception e4) {
                printWriter = writer;
                if (printWriter != null) {
                    try {
                        printWriter.close();
                    } catch (Exception e5) {
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                printWriter = writer;
                if (printWriter != null) {
                    try {
                        printWriter.close();
                    } catch (Exception e6) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            if (printWriter != null) {
                printWriter.close();
            }
        } catch (Exception e8) {
            if (printWriter != null) {
                printWriter.close();
            }
        } catch (Throwable th3) {
            th = th3;
            if (printWriter != null) {
                printWriter.close();
            }
            throw th;
        }
    }

    protected boolean isCloudApk(String packageName) {
        if (mCloudApks.contains(packageName)) {
            return true;
        }
        return DEBUG_DEXOPT_SHELL;
    }

    private boolean isCloudApk(String packageName, String path, boolean cloudSDK) {
        if (path != null && path.contains(SYSTEM_APP_DIR)) {
            return DEBUG_DEXOPT_SHELL;
        }
        if (mCloudApks.contains(packageName)) {
            return true;
        }
        if (!cloudSDK) {
            return DEBUG_DEXOPT_SHELL;
        }
        writeCloudApkConfig(packageName);
        return true;
    }

    protected int appendPkgFlagsForCloudApk(String packageName, String path, int parseFlags, int defaultFlags, Package pkg) {
        return mThreadNum;
    }

    protected int removePkgFlagsForCloudApk(String packageName, String path, int defaultFlags, int hwFlags) {
        int flags = defaultFlags;
        return defaultFlags;
    }

    protected int appendParseFlagsForCloudApk(int pFlags, int defaultFlags) {
        return mThreadNum;
    }

    protected void setUpCustomResolverActivity(Package pkg) {
        synchronized (this.mPackages) {
            super.setUpCustomResolverActivity(pkg);
            if (!TextUtils.isEmpty(HwFrameworkFactory.getHuaweiResolverActivity(this.mContext))) {
                this.mResolveActivity.processName = "system:ui";
                this.mResolveActivity.theme = 16974981;
            }
        }
    }

    private static final boolean isPackageFilename(String name) {
        return name != null ? name.endsWith(".apk") : DEBUG_DEXOPT_SHELL;
    }

    protected void parseInstallerInfo(int uid, String packageUri) {
        int pid = Binder.getCallingPid();
        Bundle extrasInstallerInfo = new Bundle(OPTIMIZE_FOR_OTA);
        extrasInstallerInfo.putInt(INSTALLATION_EXTRA_PACKAGE_INSTALLER_UID, uid);
        extrasInstallerInfo.putInt(INSTALLATION_EXTRA_PACKAGE_INSTALLER_PID, pid);
        extrasInstallerInfo.putString(INSTALLATION_EXTRA_PACKAGE_URI, packageUri);
        Intent intentInformation = new Intent(ACTION_GET_INSTALLER_PACKAGE_INFO);
        intentInformation.putExtras(extrasInstallerInfo);
        intentInformation.setPackage(HSM_PACKAGE);
        intentInformation.setFlags(1073741824);
        long identity = Binder.clearCallingIdentity();
        try {
            this.mContext.sendBroadcast(intentInformation);
            Slog.v(TAG, "installPackageWithVerificationAndEncryption:  uid= " + uid + ", pid=" + pid + ", packageUri= " + packageUri);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    protected void parseInstalledPkgInfo(String pkgUri, String pkgName, String pkgVerName, int pkgVerCode, int resultCode, boolean pkgUpdate) {
        Bundle extrasInfo = new Bundle(OPTIMIZE_FOR_OTA);
        extrasInfo.putString(INSTALLATION_EXTRA_PACKAGE_NAME, pkgName);
        extrasInfo.putInt(INSTALLATION_EXTRA_PACKAGE_VERSION_CODE, pkgVerCode);
        extrasInfo.putString(INSTALLATION_EXTRA_PACKAGE_VERSION_NAME, pkgVerName);
        extrasInfo.putBoolean(INSTALLATION_EXTRA_PACKAGE_UPDATE, pkgUpdate);
        extrasInfo.putInt(INSTALLATION_EXTRA_PACKAGE_INSTALL_RESULT, resultCode);
        extrasInfo.putString(INSTALLATION_EXTRA_PACKAGE_URI, pkgUri);
        String metaHash = (String) this.pkgMetaHash.remove(pkgName);
        extrasInfo.putString(INSTALLATION_EXTRA_PACKAGE_META_HASH, metaHash);
        Intent intentInfo = new Intent(ACTION_GET_PACKAGE_INSTALLATION_INFO);
        intentInfo.putExtras(extrasInfo);
        intentInfo.setFlags(1073741824);
        this.mContext.sendBroadcast(intentInfo, BROADCAST_PERMISSION);
        Slog.v(TAG, "POST_INSTALL:  pkgName = " + pkgName + ", pkgUri = " + pkgUri + ", pkgVerName = " + pkgVerName + ", pkgVerCode = " + pkgVerCode + ", resultCode = " + resultCode + ", pkgUpdate = " + pkgUpdate + ", pkgMetaHash = " + metaHash);
    }

    public boolean containDelPath(String sensePath) {
        return !sensePath.startsWith("/data/cust/delapp") ? sensePath.startsWith("/system/delapp") : true;
    }

    private boolean isDelappInData(String scanFileString) {
        if (!(scanFileString == null || mDelMultiInstallMap == null || mDelMultiInstallMap.isEmpty())) {
            for (Entry<String, HashSet<String>> entry : mDelMultiInstallMap.entrySet()) {
                HashSet<String> hashSet = (HashSet) entry.getValue();
                if (hashSet != null && !hashSet.isEmpty() && hashSet.contains(scanFileString)) {
                    return true;
                }
            }
        }
        return DEBUG_DEXOPT_SHELL;
    }

    public void addUpdatedRemoveableAppFlag(String scanFileString, String packageName) {
        if (containDelPath(scanFileString) || isDelappInData(scanFileString) || isDelappInCust(scanFileString)) {
            synchronized (this.mPackages) {
                mRemoveablePreInstallApks.add(packageName);
                Package p = (Package) this.mPackages.get(packageName);
                if (!(p == null || p.applicationInfo == null)) {
                    ApplicationInfo applicationInfo = p.applicationInfo;
                    applicationInfo.hwFlags &= -33554433;
                    applicationInfo = p.applicationInfo;
                    applicationInfo.hwFlags |= 67108864;
                    this.mPackages.put(p.applicationInfo.packageName, p);
                }
            }
        }
    }

    private static String getCustPackagePath(String readLine) {
        int lastIndex = readLine.lastIndexOf(47);
        if (lastIndex > 0) {
            return readLine.substring(mThreadNum, lastIndex);
        }
        Log.e(TAG, "getAPKInstallList ERROR:  " + readLine);
        return null;
    }

    public boolean needAddUpdatedRemoveableAppFlag(String packageName) {
        if (!mRemoveablePreInstallApks.contains(packageName)) {
            return DEBUG_DEXOPT_SHELL;
        }
        mRemoveablePreInstallApks.remove(packageName);
        return true;
    }

    public void addFlagsForUpdatedRemovablePreApk(Package pkg, int hwFlags) {
        if ((hwFlags & 67108864) != 0) {
            ApplicationInfo applicationInfo = pkg.applicationInfo;
            applicationInfo.hwFlags |= 67108864;
        }
    }

    protected boolean hasOtaUpdate() {
        boolean z = DEBUG_DEXOPT_SHELL;
        try {
            UserInfo userInfo = sUserManager.getUserInfo(mThreadNum);
            if (userInfo == null) {
                return DEBUG_DEXOPT_SHELL;
            }
            Log.i(TAG, "userInfo.lastLoggedInFingerprint : " + userInfo.lastLoggedInFingerprint + ", Build.FINGERPRINT : " + Build.FINGERPRINT);
            if (!Objects.equals(userInfo.lastLoggedInFingerprint, Build.FINGERPRINT)) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            Log.i(TAG, "Exception is " + e);
            return DEBUG_DEXOPT_SHELL;
        }
    }

    private boolean support64BitAbi() {
        int length = Build.SUPPORTED_ABIS.length;
        if (length > OPTIMIZE_FOR_OTA) {
            String instructionSetA = VMRuntime.getInstructionSet(Build.SUPPORTED_ABIS[mThreadNum]);
            String instructionSetB = VMRuntime.getInstructionSet(Build.SUPPORTED_ABIS[OPTIMIZE_FOR_OTA]);
            if (instructionSetA.equals("arm64") || instructionSetB.equals("arm64")) {
                return true;
            }
        } else if (length != OPTIMIZE_FOR_OTA || VMRuntime.getInstructionSet(Build.SUPPORTED_ABIS[mThreadNum]).equals("arm")) {
            return DEBUG_DEXOPT_SHELL;
        }
        return DEBUG_DEXOPT_SHELL;
    }

    protected boolean isOdexMode() {
        boolean support64BitAbi = support64BitAbi();
        File bootArtArm64File = new File("/system/framework/arm64/boot.art");
        File bootArtArmFile = new File("/system/framework/arm/boot.art");
        if (support64BitAbi) {
            if (bootArtArm64File.exists() && bootArtArmFile.exists()) {
                return true;
            }
        } else if (bootArtArmFile.exists()) {
            return true;
        }
        return DEBUG_DEXOPT_SHELL;
    }

    private boolean hasPrunedDalvikCache() {
        if (new File(Environment.getDataDirectory(), "system/.dalvik-cache-pruned").exists()) {
            return true;
        }
        return DEBUG_DEXOPT_SHELL;
    }

    protected boolean notDexOptForBootingSpeedup(boolean adjustCpuAbi) {
        if (adjustCpuAbi) {
            Log.i(TAG, "notDexOptForBootingSpeedup: adjustCpuAbi " + adjustCpuAbi + ", return false");
            return DEBUG_DEXOPT_SHELL;
        }
        boolean isOdexCase = isOdexMode();
        if (DEBUG_DEXOPT_OPTIMIZE) {
            Log.i(TAG, "forceNotDex: isOdexCase " + isOdexCase + ", mSystemReady " + this.mSystemReady + ", mDexOptTotalTime " + this.mPackageDexOptimizer.getDexOptTotalTime() + ", isFirstBoot " + isFirstBoot() + ", hasOtaUpdate " + hasOtaUpdate());
        }
        if (isOdexCase) {
            if (hasOtaUpdate() && !this.mSystemReady && this.mPackageDexOptimizer.getDexOptTotalTime() < HOTA_DEXOPT_THRESHOLD) {
                if (DEBUG_DEXOPT_OPTIMIZE) {
                    Log.i(TAG, "forceNotDex = false: Ota Update & First Boot & withing 3 minutes");
                }
                return DEBUG_DEXOPT_SHELL;
            } else if (hasPrunedDalvikCache() && !isFirstBoot() && !this.mSystemReady && !hasOtaUpdate()) {
                if (DEBUG_DEXOPT_OPTIMIZE) {
                    Log.i(TAG, "forceNotDex = false: Force reboot when booting, pruned dalvik-cache");
                }
                return DEBUG_DEXOPT_SHELL;
            } else if (!((mOptimizeForDexopt & OPTIMIZE_FOR_BOOTING) == 0 || this.mSystemReady)) {
                if (DEBUG_DEXOPT_OPTIMIZE) {
                    Log.i(TAG, "forceNotDex = true: Booting now or outgoing 3 minutes");
                }
                return true;
            }
        }
        if (DEBUG_DEXOPT_OPTIMIZE) {
            Log.i(TAG, "forceNotDex = false: isOdexCase " + isOdexCase + ", mSystemReady " + this.mSystemReady);
        }
        return DEBUG_DEXOPT_SHELL;
    }

    private String codePathEndName(String readLine) {
        int lastIndex = readLine.lastIndexOf(47);
        if (lastIndex > 0) {
            return readLine.substring(lastIndex + OPTIMIZE_FOR_OTA);
        }
        Log.e(TAG, "getAPKInstallList ERROR:  " + readLine);
        return null;
    }

    private void loadAppsList(File file, ArrayList<String> appsList) {
        Throwable th;
        BufferedReader bufferedReader = null;
        if (file != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));
                while (true) {
                    try {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        line = line.trim();
                        appsList.add(line);
                        if (DEBUG_DEXOPT_OPTIMIZE) {
                            Log.i(TAG, "appsList need add " + line);
                        }
                    } catch (FileNotFoundException e) {
                        bufferedReader = reader;
                    } catch (IOException e2) {
                        bufferedReader = reader;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedReader = reader;
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e3) {
                    }
                }
            } catch (FileNotFoundException e4) {
                try {
                    Log.i(TAG, "loadAppsList FileNotFoundException ");
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e5) {
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e6) {
                        }
                    }
                    throw th;
                }
            } catch (IOException e7) {
                Log.i(TAG, "loadAppsList IOException");
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e8) {
                    }
                }
            }
        }
    }

    protected boolean filterForceNotDexApps(Package pkg, boolean adjustCpuAbi) {
        if (adjustCpuAbi) {
            Log.i(TAG, "filterForceNotDexApps: pkg " + pkg + " adjustCpuAbi " + adjustCpuAbi + ", return false");
            return DEBUG_DEXOPT_SHELL;
        }
        ArrayList<File> allList = new ArrayList();
        try {
            allList = HwCfgFilePolicy.getCfgFileList("dexopt/never_dexopt_apklist.cfg", mThreadNum);
        } catch (NoClassDefFoundError e) {
            Slog.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (allList.size() > 0) {
            synchronized (this.mPackages) {
                for (File file : allList) {
                    if (DEBUG_DEXOPT_OPTIMIZE) {
                        Log.i(TAG, "start loadForceNotDexApps file. mHaveLoadedNeverDexoptApkList " + this.mHaveLoadedNeverDexoptApkList);
                    }
                    if (file.exists() && !this.mHaveLoadedNeverDexoptApkList) {
                        if (DEBUG_DEXOPT_OPTIMIZE) {
                            Log.i(TAG, "loadAppsList file.");
                        }
                        this.mHaveLoadedNeverDexoptApkList = true;
                        loadAppsList(file, mForceNotDexApps);
                    } else if (!file.exists()) {
                        Slog.w(TAG, "/system/etc/dexopt/never_dexopt_apklist.cfg file not exists.");
                    } else if (DEBUG_DEXOPT_OPTIMIZE) {
                        Slog.w(TAG, "/system/etc/dexopt/never_dexopt_apklist.cfg file has loaded.");
                    }
                }
            }
        }
        if (DEBUG_DEXOPT_OPTIMIZE) {
            Log.i(TAG, "needed dexopt deferred pkg :" + pkg.packageName);
        }
        if ((pkg.applicationInfo.flags & OPTIMIZE_FOR_OTA) == 0 || (pkg.applicationInfo.flags & HwSecDiagnoseConstant.BIT_VERIFYBOOT) != 0 || mForceNotDexApps == null || !mForceNotDexApps.contains(pkg.packageName)) {
            return DEBUG_DEXOPT_SHELL;
        }
        if (DEBUG_DEXOPT_OPTIMIZE) {
            Log.i(TAG, "Skipping dexopt of " + pkg.packageName);
        }
        return true;
    }

    protected boolean filterDexoptInBootupApps(Package pkg) {
        ArrayList<File> allList = new ArrayList();
        try {
            allList = HwCfgFilePolicy.getCfgFileList("dexopt/dexopt_in_bootup_apklist.cfg", mThreadNum);
        } catch (NoClassDefFoundError e) {
            Slog.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (allList.size() > 0) {
            synchronized (this.mPackages) {
                for (File file : allList) {
                    if (DEBUG_DEXOPT_OPTIMIZE) {
                        Log.i(TAG, "start loadAppsList file. mHaveLoadedDexoptInBootUpApkList " + this.mHaveLoadedDexoptInBootUpApkList);
                    }
                    if (file.exists() && !this.mHaveLoadedDexoptInBootUpApkList) {
                        if (DEBUG_DEXOPT_OPTIMIZE) {
                            Log.i(TAG, "loadAppsList file.");
                        }
                        this.mHaveLoadedDexoptInBootUpApkList = true;
                        loadAppsList(file, mDexoptInBootupApps);
                    } else if (!file.exists()) {
                        Slog.w(TAG, "/system/etc/dexopt/dexopt_in_bootup_apklist.cfg file not exists.");
                    } else if (DEBUG_DEXOPT_OPTIMIZE) {
                        Slog.w(TAG, "/system/etc/dexopt/dexopt_in_bootup_apklist.cfg file has loaded.");
                    }
                }
            }
        }
        if (DEBUG_DEXOPT_OPTIMIZE) {
            Log.i(TAG, "needed dexopt deferred pkg :" + pkg.packageName);
        }
        if (mDexoptInBootupApps == null || !mDexoptInBootupApps.contains(pkg.packageName)) {
            return DEBUG_DEXOPT_SHELL;
        }
        if (DEBUG_DEXOPT_OPTIMIZE) {
            Log.i(TAG, "Need to dexopt " + pkg.packageName + " in bootup.");
        }
        return true;
    }

    protected ArrayList<Package> sortRecentlyUsedApps(Collection<Package> pkgs) {
        Iterator<Package> it;
        Package pkg;
        if (this.mSortDexoptApps != null) {
            this.mSortDexoptApps.clear();
        }
        if (DEBUG_DEXOPT_OPTIMIZE) {
            int size = mThreadNum;
            for (Package pkg2 : pkgs) {
                Log.i(TAG, "before sortRecentlyUsedApps, remaining pkg : " + pkg2.packageName);
                size += OPTIMIZE_FOR_OTA;
            }
            Log.i(TAG, "before sortRecentlyUsedApps, all remaining pkgs.size : " + size);
        }
        long current = Calendar.getInstance().getTimeInMillis();
        long begin = current - QUERY_RECENTLY_USED_THRESHOLD;
        for (Entry entry : ((UsageStatsManager) this.mContext.getSystemService("usagestats")).queryAndAggregateUsageStats(begin, current).entrySet()) {
            UsageStats usageStats = (UsageStats) entry.getValue();
            this.mRecentlyUsedApps.add(usageStats);
        }
        Collections.sort(this.mRecentlyUsedApps, this.totalTimeInForegroundComparator);
        int N = this.mRecentlyUsedApps.size();
        for (int i = mThreadNum; i < N; i += OPTIMIZE_FOR_OTA) {
            UsageStats recentlyUsedApp = (UsageStats) this.mRecentlyUsedApps.get(i);
            if (DEBUG_DEXOPT_OPTIMIZE) {
                Log.i(TAG, "recentlyUsed Apps : " + recentlyUsedApp.mPackageName);
            }
            it = pkgs.iterator();
            while (it.hasNext()) {
                pkg2 = (Package) it.next();
                if (recentlyUsedApp.mPackageName.equals(pkg2.packageName)) {
                    if (DEBUG_DEXOPT_OPTIMIZE) {
                        Log.i(TAG, "Adding recentlyUsedApps : " + pkg2.packageName);
                    }
                    this.mSortDexoptApps.add(pkg2);
                    it.remove();
                }
            }
        }
        for (Package pkg22 : pkgs) {
            if (DEBUG_DEXOPT_OPTIMIZE) {
                Log.i(TAG, "Adding remaining app : " + pkg22.packageName);
            }
            this.mSortDexoptApps.add(pkg22);
        }
        this.mRecentlyUsedApps.clear();
        return this.mSortDexoptApps;
    }

    public boolean isSetupDisabled() {
        return this.mSetupDisabled;
    }

    private boolean needSkipSetupPhase() {
        return BluetoothAddressNative.isLibReady() ? TextUtils.isEmpty(BluetoothAddressNative.getMacAddress()) : DEBUG_DEXOPT_SHELL;
    }

    private boolean isSetupPkg(String pname) {
        return !HWSETUP_PKG.equals(pname) ? GOOGLESETUP_PKG.equals(pname) : true;
    }

    protected boolean skipSetupEnable(String pname) {
        boolean shouldskip = isSetupPkg(pname) ? needSkipSetupPhase() : DEBUG_DEXOPT_SHELL;
        if (shouldskip) {
            Slog.i(TAG, "skipSetupEnable skip pkg: " + pname);
        }
        return shouldskip;
    }

    protected boolean makeSetupDisabled(String pname) {
        if (!isSetupPkg(pname) || this.mSettings.isDisabledSystemPackageLPr(pname) || !needSkipSetupPhase()) {
            return DEBUG_DEXOPT_SHELL;
        }
        this.mSettings.disableSystemPackageLPw(pname);
        this.mSetupDisabled = true;
        Slog.w(TAG, "makeSetupDisabled skip pkg: " + pname);
        return true;
    }

    public synchronized HwCustPackageManagerService getCust() {
        if (this.mCust == null) {
            this.mCust = HwCustUtils.createObj(HwCustPackageManagerService.class, new Object[mThreadNum]);
        }
        return (HwCustPackageManagerService) this.mCust;
    }

    public void filterShellApps(ArrayList<Package> pkgs, LinkedList<Package> sortedPkgs) {
        if (hasOtaUpdate() || hasPrunedDalvikCache()) {
            HwShellAppsHandler handler = new HwShellAppsHandler(this.mInstaller, sUserManager);
            for (Package pkg : pkgs) {
                String shellName = handler.AnalyseShell(pkg);
                if (shellName != null) {
                    if (DEBUG_DEXOPT_SHELL) {
                        Log.i(TAG, "Find a " + shellName + " Shell Pkgs: " + pkg.packageName);
                    }
                    sortedPkgs.add(pkg);
                    handler.ProcessShellApp(pkg);
                }
            }
            pkgs.removeAll(sortedPkgs);
        }
    }

    public static File getCfgFile(String fileName, int type) throws Exception, NoClassDefFoundError {
        Class<?> filePolicyClazz = Class.forName(FILE_POLICY_CLASS_NAME);
        String str = METHOD_NAME_FOR_FILE;
        Class[] clsArr = new Class[OPTIMIZE_FOR_BOOTING];
        clsArr[mThreadNum] = String.class;
        clsArr[OPTIMIZE_FOR_OTA] = Integer.TYPE;
        Method filePolicyMethod = filePolicyClazz.getMethod(str, clsArr);
        Object[] objArr = new Object[OPTIMIZE_FOR_BOOTING];
        objArr[mThreadNum] = fileName;
        objArr[OPTIMIZE_FOR_OTA] = Integer.valueOf(type);
        return (File) filePolicyMethod.invoke(filePolicyClazz, objArr);
    }

    public static File getCustomizedFileName(String xmlName, int flag) {
        File file = null;
        try {
            file = getCfgFile("xml/" + xmlName, flag);
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        } catch (Exception e2) {
            Log.d(TAG, "getCustomizedFileName get layout file exception");
        }
        return file;
    }

    private static void loadMultiWinWhiteList(Context aContext) {
        XmlPullParser xmlParser;
        int xmlEventType;
        String packageName;
        File configFile = getCustomizedFileName(FILE_MULTIWINDOW_WHITELIST, mThreadNum);
        InputStream inputStream = null;
        if (configFile != null) {
            try {
                if (configFile.exists()) {
                    inputStream = new FileInputStream(configFile);
                    if (inputStream != null) {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                Slog.e(TAG, "loadMultiWinWhiteList:- IOE while closing stream", e);
                            }
                        }
                    }
                    xmlParser = Xml.newPullParser();
                    xmlParser.setInput(inputStream, null);
                    for (xmlEventType = xmlParser.next(); xmlEventType != OPTIMIZE_FOR_OTA; xmlEventType = xmlParser.next()) {
                        if (xmlEventType != OPTIMIZE_FOR_BOOTING) {
                            if (XML_ELEMENT_APP_ITEM.equals(xmlParser.getName())) {
                                packageName = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_PACKAGE_NAME);
                                if (packageName != null) {
                                    packageName = packageName.toLowerCase();
                                }
                                sMultiWinWhiteListPkgNames.add(packageName);
                                Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "Multiwindow whitelist package name: [" + packageName + "]");
                            } else if (XML_ELEMENT_APP_FORCED_PORTRAIT_ITEM.equals(xmlParser.getName())) {
                                packageName = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_PACKAGE_NAME);
                                if (packageName != null) {
                                    packageName = packageName.toLowerCase();
                                }
                                sMWPortraitWhiteListPkgNames.add(packageName);
                                Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "Multiwindow portrait whitelist package name: [" + packageName + "]");
                            }
                        } else if (xmlEventType == 3) {
                            continue;
                        } else if (XML_ELEMENT_APP_LIST.equals(xmlParser.getName())) {
                            break;
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e2) {
                            Slog.e(TAG, "loadMultiWinWhiteList:- IOE while closing stream", e2);
                        }
                    }
                    return;
                }
            } catch (FileNotFoundException e3) {
                Log.e(TAG, "loadMultiWinWhiteList", e3);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e22) {
                        Slog.e(TAG, "loadMultiWinWhiteList:- IOE while closing stream", e22);
                    }
                }
            } catch (XmlPullParserException e4) {
                Log.e(TAG, "loadMultiWinWhiteList", e4);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e222) {
                        Slog.e(TAG, "loadMultiWinWhiteList:- IOE while closing stream", e222);
                    }
                }
            } catch (IOException e2222) {
                Log.e(TAG, "loadMultiWinWhiteList", e2222);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e22222) {
                        Slog.e(TAG, "loadMultiWinWhiteList:- IOE while closing stream", e22222);
                    }
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e222222) {
                        Slog.e(TAG, "loadMultiWinWhiteList:- IOE while closing stream", e222222);
                    }
                }
            }
        }
        Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "Multi Window white list taken from default configuration");
        inputStream = aContext.getAssets().open(FILE_MULTIWINDOW_WHITELIST);
        if (inputStream != null) {
            xmlParser = Xml.newPullParser();
            xmlParser.setInput(inputStream, null);
            for (xmlEventType = xmlParser.next(); xmlEventType != OPTIMIZE_FOR_OTA; xmlEventType = xmlParser.next()) {
                if (xmlEventType != OPTIMIZE_FOR_BOOTING) {
                    if (xmlEventType == 3) {
                        continue;
                    } else if (XML_ELEMENT_APP_LIST.equals(xmlParser.getName())) {
                        break;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        return;
                    }
                } else if (XML_ELEMENT_APP_ITEM.equals(xmlParser.getName())) {
                    packageName = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_PACKAGE_NAME);
                    if (packageName != null) {
                        packageName = packageName.toLowerCase();
                    }
                    sMultiWinWhiteListPkgNames.add(packageName);
                    Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "Multiwindow whitelist package name: [" + packageName + "]");
                } else if (XML_ELEMENT_APP_FORCED_PORTRAIT_ITEM.equals(xmlParser.getName())) {
                    packageName = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_PACKAGE_NAME);
                    if (packageName != null) {
                        packageName = packageName.toLowerCase();
                    }
                    sMWPortraitWhiteListPkgNames.add(packageName);
                    Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "Multiwindow portrait whitelist package name: [" + packageName + "]");
                }
            }
            if (inputStream != null) {
                inputStream.close();
            }
            return;
        }
        if (inputStream != null) {
            inputStream.close();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean isInMultiWinWhiteList(String packageName) {
        if (packageName == null || sMultiWinWhiteListPkgNames.size() == 0 || !sMultiWinWhiteListPkgNames.contains(packageName.toLowerCase())) {
            return DEBUG_DEXOPT_SHELL;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean isInMWPortraitWhiteList(String packageName) {
        if (packageName == null || sMWPortraitWhiteListPkgNames.size() == 0 || !sMWPortraitWhiteListPkgNames.contains(packageName.toLowerCase())) {
            return DEBUG_DEXOPT_SHELL;
        }
        return true;
    }

    protected void checkHwCertification(Package pkg, boolean isUpdate) {
        if (!HwCertificationManager.hasFeature()) {
            return;
        }
        if (HwCertificationManager.isSupportHwCertification(pkg)) {
            if (isUpdate || !isContainHwCertification(pkg)) {
                Slog.i(Utils.TAG, "will checkCertificationInner,isUpdate = " + isUpdate);
                hwCertCleanUp(pkg);
                checkCertificationInner(pkg);
            }
            return;
        }
        if (isContainHwCertification(pkg)) {
            hwCertCleanUp(pkg);
        }
    }

    private void checkCertificationInner(Package pkg) {
        HwCertificationManager manager = HwCertificationManager.getIntance();
        if (!(manager == null || manager.checkHwCertification(pkg))) {
            Slog.e(Utils.TAG, "checkHwCertification parse error");
        }
    }

    protected boolean getHwCertificationPermission(boolean allowed, Package pkg, String perm) {
        if (!HwCertificationManager.hasFeature()) {
            return allowed;
        }
        if (!HwCertificationManager.isInitialized()) {
            HwCertificationManager.initialize(this.mContext);
        }
        HwCertificationManager manager = HwCertificationManager.getIntance();
        if (manager == null) {
            return allowed;
        }
        return manager.getHwCertificationPermission(allowed, pkg, perm);
    }

    private void hwCertCleanUp(Package pkg) {
        HwCertificationManager manager = HwCertificationManager.getIntance();
        if (manager != null) {
            manager.cleanUp(pkg);
        }
    }

    protected void hwCertCleanUp() {
        if (HwCertificationManager.hasFeature()) {
            if (!HwCertificationManager.isInitialized()) {
                HwCertificationManager.initialize(this.mContext);
            }
            HwCertificationManager manager = HwCertificationManager.getIntance();
            if (manager != null) {
                manager.cleanUp();
            }
        }
    }

    protected boolean isHwCustHiddenInfoPackage(Package pkgInfo) {
        if (getCust() != null) {
            return getCust().isHwCustHiddenInfoPackage(pkgInfo);
        }
        return DEBUG_DEXOPT_SHELL;
    }

    public void installPackageAsUser(String originPath, IPackageInstallObserver2 observer, int installFlags, String installerPackageName, int userId) {
        if (isParentControlEnabled()) {
            String originPackageInstallerName = getNameForUid(Binder.getCallingUid());
            if (isInstallerValidForParentControl(originPackageInstallerName)) {
                installerPackageName = originPackageInstallerName;
            }
        }
        super.installPackageAsUser(originPath, observer, installFlags, installerPackageName, userId);
    }

    protected boolean isAppInstallAllowed(String installer, String appName) {
        if (isParentControlEnabled() && !isInstallerValidForParentControl(installer) && getPackageInfo(appName, mThreadNum, mThreadNum) == null) {
            return DEBUG_DEXOPT_SHELL;
        }
        return true;
    }

    private boolean isParentControlEnabled() {
        if (Secure.getInt(this.mContext.getContentResolver(), "childmode_status", mThreadNum) == 0 || getPackageInfo("com.huawei.parentcontrol", mThreadNum, mThreadNum) == null) {
            return DEBUG_DEXOPT_SHELL;
        }
        return true;
    }

    private boolean isInstallerValidForParentControl(String installer) {
        String whiteInstallerPackages = Secure.getString(this.mContext.getContentResolver(), "childmode_installer_whitelist");
        if (!(whiteInstallerPackages == null || AppHibernateCst.INVALID_PKG.equals(whiteInstallerPackages.trim()) || installer == null)) {
            String[] installers = whiteInstallerPackages.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            int length = installers.length;
            for (int i = mThreadNum; i < length; i += OPTIMIZE_FOR_OTA) {
                if (installer.equals(installers[i])) {
                    return true;
                }
            }
        }
        return DEBUG_DEXOPT_SHELL;
    }

    public boolean isUnAppInstallAllowed(String originPath) {
        if (mCustPackageManagerService == null || !mCustPackageManagerService.isUnAppInstallAllowed(originPath, this.mContext)) {
            return DEBUG_DEXOPT_SHELL;
        }
        return true;
    }

    private void initBlackList() {
        boolean z;
        BlackListAppsUtils.readBlackList(this.mBlackListInfo);
        synchronized (this.mPackages) {
            BlackListAppsUtils.readDisableAppList(this.mDisableAppListInfo);
            for (BlackListApp app : this.mDisableAppListInfo.mBlackList) {
                this.mDisableAppMap.put(app.mPackageName, app);
            }
        }
        if (this.mBlackListInfo.mBlackList.size() == 0 || this.mBlackListInfo.mVersionCode == -1) {
            z = DEBUG_DEXOPT_SHELL;
        } else {
            z = true;
        }
        this.isBlackListExist = z;
        Set<String> needEnablePackage;
        if (this.isBlackListExist) {
            boolean isCompleteProcess;
            synchronized (this.mPackages) {
                isCompleteProcess = (hasOtaUpdate() || BlackListAppsUtils.isBlackListUpdate(this.mBlackListInfo, this.mDisableAppListInfo)) ? true : validateDisabledAppFile() ? DEBUG_DEXOPT_SHELL : true;
            }
            Slog.d(TAG, "initBlackList start, is completed process: " + isCompleteProcess);
            Set<String> needDisablePackage;
            if (isCompleteProcess) {
                synchronized (this.mPackages) {
                    String pkg;
                    needDisablePackage = new ArraySet();
                    needEnablePackage = new ArraySet();
                    for (BlackListApp app2 : this.mBlackListInfo.mBlackList) {
                        pkg = app2.mPackageName;
                        if (!(needDisablePackage.contains(pkg) || !BlackListAppsUtils.comparePackage((Package) this.mPackages.get(pkg), app2) || needDisablePackage.contains(pkg))) {
                            setPackageDisableFlag(pkg, true);
                            needDisablePackage.add(pkg);
                            this.mDisableAppMap.put(pkg, app2);
                        }
                    }
                    for (String pkg2 : new ArrayList(this.mDisableAppMap.keySet())) {
                        if (!(BlackListAppsUtils.containsApp(this.mBlackListInfo.mBlackList, (BlackListApp) this.mDisableAppMap.get(pkg2)) || needDisablePackage.contains(pkg2))) {
                            if (this.mPackages.get(pkg2) != null) {
                                needEnablePackage.add(pkg2);
                            }
                            this.mDisableAppMap.remove(pkg2);
                        }
                    }
                    enableComponentForAllUser(needEnablePackage, true);
                    enableComponentForAllUser(needDisablePackage, DEBUG_DEXOPT_SHELL);
                    this.mDisableAppListInfo.mBlackList.clear();
                    for (Entry<String, BlackListApp> entry : this.mDisableAppMap.entrySet()) {
                        this.mDisableAppListInfo.mBlackList.add((BlackListApp) entry.getValue());
                    }
                    this.mDisableAppListInfo.mVersionCode = this.mBlackListInfo.mVersionCode;
                    BlackListAppsUtils.writeBlackListToXml(this.mDisableAppListInfo);
                }
            } else {
                needDisablePackage = new ArraySet();
                for (Entry<String, BlackListApp> entry2 : this.mDisableAppMap.entrySet()) {
                    setPackageDisableFlag((String) entry2.getKey(), true);
                    needDisablePackage.add((String) entry2.getKey());
                }
                enableComponentForAllUser(needDisablePackage, DEBUG_DEXOPT_SHELL);
            }
            Slog.d(TAG, "initBlackList end");
            return;
        }
        synchronized (this.mPackages) {
            if (this.mDisableAppMap.size() > 0) {
                Slog.d(TAG, "blacklist not exists, enable all disabled apps");
                needEnablePackage = new ArraySet();
                for (Entry<String, BlackListApp> entry22 : this.mDisableAppMap.entrySet()) {
                    needEnablePackage.add((String) entry22.getKey());
                }
                enableComponentForAllUser(needEnablePackage, true);
                this.mDisableAppMap.clear();
            }
        }
        BlackListAppsUtils.deleteDisableAppListFile();
    }

    private boolean validateDisabledAppFile() {
        if (this.mBlackListInfo.mBlackList.size() == 0) {
            return DEBUG_DEXOPT_SHELL;
        }
        synchronized (this.mPackages) {
            for (Entry<String, BlackListApp> entry : this.mDisableAppMap.entrySet()) {
                if (this.mPackages.get(entry.getKey()) == null) {
                    return DEBUG_DEXOPT_SHELL;
                } else if (!BlackListAppsUtils.containsApp(this.mBlackListInfo.mBlackList, (BlackListApp) entry.getValue())) {
                    return DEBUG_DEXOPT_SHELL;
                }
            }
            return true;
        }
    }

    private void enableComponentForAllUser(Set<String> packages, boolean enable) {
        int[] userIds = sUserManager.getUserIds();
        int length = userIds.length;
        for (int i = mThreadNum; i < length; i += OPTIMIZE_FOR_OTA) {
            int userId = userIds[i];
            if (packages != null && packages.size() > 0) {
                for (String pkg : packages) {
                    enableComponentForPackage(pkg, enable, userId);
                }
            }
        }
    }

    private void setPackageDisableFlag(String packageName, boolean disable) {
        if (!TextUtils.isEmpty(packageName)) {
            Package pkg = (Package) this.mPackages.get(packageName);
            if (pkg != null) {
                ApplicationInfo applicationInfo;
                if (disable) {
                    applicationInfo = pkg.applicationInfo;
                    applicationInfo.hwFlags |= 268435456;
                } else {
                    applicationInfo = pkg.applicationInfo;
                    applicationInfo.hwFlags |= -268435457;
                }
            }
        }
    }

    private void enableComponentForPackage(String packageName, boolean enable, int userId) {
        if (!TextUtils.isEmpty(packageName)) {
            int i;
            int newState = enable ? mThreadNum : OPTIMIZE_FOR_BOOTING;
            PackageInfo packageInfo = getPackageInfo(packageName, 786959, userId);
            if (!(packageInfo == null || packageInfo.receivers == null || packageInfo.receivers.length == 0)) {
                for (i = mThreadNum; i < packageInfo.receivers.length; i += OPTIMIZE_FOR_OTA) {
                    setEnabledComponentInner(new ComponentName(packageName, packageInfo.receivers[i].name), newState, userId);
                }
            }
            if (!(packageInfo == null || packageInfo.services == null || packageInfo.services.length == 0)) {
                for (i = mThreadNum; i < packageInfo.services.length; i += OPTIMIZE_FOR_OTA) {
                    setEnabledComponentInner(new ComponentName(packageName, packageInfo.services[i].name), newState, userId);
                }
            }
            if (!(packageInfo == null || packageInfo.providers == null || packageInfo.providers.length == 0)) {
                for (i = mThreadNum; i < packageInfo.providers.length; i += OPTIMIZE_FOR_OTA) {
                    setEnabledComponentInner(new ComponentName(packageName, packageInfo.providers[i].name), newState, userId);
                }
            }
            if (!(packageInfo == null || packageInfo.activities == null || packageInfo.activities.length == 0)) {
                for (i = mThreadNum; i < packageInfo.activities.length; i += OPTIMIZE_FOR_OTA) {
                    setEnabledComponentInner(new ComponentName(packageName, packageInfo.activities[i].name), newState, userId);
                }
            }
            if (!enable) {
                clearPackagePreferredActivitiesLPw(packageName, userId);
            }
            scheduleWritePackageRestrictionsLocked(userId);
        }
    }

    protected void updatePackageBlackListInfo(String packageName) {
        if (this.isBlackListExist && !TextUtils.isEmpty(packageName)) {
            int[] userIds = sUserManager.getUserIds();
            synchronized (this.mPackages) {
                Package pkgInfo = (Package) this.mPackages.get(packageName);
                boolean needDisable = DEBUG_DEXOPT_SHELL;
                boolean needEnable = DEBUG_DEXOPT_SHELL;
                if (pkgInfo != null) {
                    for (BlackListApp app : this.mBlackListInfo.mBlackList) {
                        if (BlackListAppsUtils.comparePackage(pkgInfo, app)) {
                            setPackageDisableFlag(packageName, true);
                            this.mDisableAppMap.put(packageName, app);
                            needDisable = true;
                            break;
                        }
                    }
                    if (!needDisable && this.mDisableAppMap.containsKey(packageName)) {
                        setPackageDisableFlag(packageName, DEBUG_DEXOPT_SHELL);
                        this.mDisableAppMap.remove(packageName);
                        needEnable = true;
                    }
                } else if (this.mDisableAppMap.containsKey(packageName)) {
                    this.mDisableAppMap.remove(packageName);
                }
                int length = userIds.length;
                for (int i = mThreadNum; i < length; i += OPTIMIZE_FOR_OTA) {
                    int userId = userIds[i];
                    if (needDisable) {
                        enableComponentForPackage(packageName, DEBUG_DEXOPT_SHELL, userId);
                    } else if (needEnable) {
                        enableComponentForPackage(packageName, true, userId);
                    } else {
                        continue;
                    }
                }
                this.mDisableAppListInfo.mBlackList.clear();
                for (Entry<String, BlackListApp> entry : this.mDisableAppMap.entrySet()) {
                    this.mDisableAppListInfo.mBlackList.add((BlackListApp) entry.getValue());
                }
                this.mDisableAppListInfo.mVersionCode = this.mBlackListInfo.mVersionCode;
                BlackListAppsUtils.writeBlackListToXml(this.mDisableAppListInfo);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setEnabledComponentInner(ComponentName componentName, int newState, int userId) {
        if (componentName != null) {
            String packageName = componentName.getPackageName();
            String className = componentName.getClassName();
            if (packageName != null && className != null) {
                synchronized (this.mPackages) {
                    PackageSetting pkgSetting = (PackageSetting) this.mSettings.mPackages.get(packageName);
                    if (pkgSetting == null) {
                        Slog.e(TAG, "setEnabledSetting, can not find pkgSetting, packageName = " + packageName);
                        return;
                    }
                    Package pkg = pkgSetting.pkg;
                    if (pkg == null || !pkg.hasComponentClassName(className)) {
                        Slog.w(TAG, "Failed setComponentEnabledSetting: component class " + className + " does not exist in " + packageName);
                        return;
                    }
                    switch (newState) {
                        case mThreadNum:
                            if (!pkgSetting.restoreComponentLPw(className, userId)) {
                                return;
                            }
                            break;
                        case OPTIMIZE_FOR_BOOTING /*2*/:
                            if (!pkgSetting.disableComponentLPw(className, userId)) {
                                return;
                            }
                            break;
                        default:
                            Slog.e(TAG, "Invalid new component state: " + newState);
                    }
                }
            }
        }
    }

    private void initBlackListForNewUser(int userHandle) {
        if (this.isBlackListExist) {
            synchronized (this.mPackages) {
                for (String pkg : this.mDisableAppMap.keySet()) {
                    enableComponentForPackage(pkg, DEBUG_DEXOPT_SHELL, userHandle);
                }
            }
        }
    }

    void onBeforeUserStartUninitialized(int userId) {
        super.onBeforeUserStartUninitialized(userId);
        initBlackListForNewUser(userId);
    }

    protected void initHwCertificationManager() {
        if (!HwCertificationManager.isInitialized()) {
            HwCertificationManager.initialize(this.mContext);
        }
        HwCertificationManager manager = HwCertificationManager.getIntance();
    }

    protected int getHwCertificateType(Package pkg) {
        if (HwCertificationManager.isSupportHwCertification(pkg)) {
            return HwCertificationManager.getIntance().getHwCertificateType(pkg.packageName);
        }
        return HwCertificationManager.getIntance().getHwCertificateTypeNotMDM();
    }

    protected boolean isContainHwCertification(Package pkg) {
        return HwCertificationManager.getIntance().isContainHwCertification(pkg.packageName);
    }

    private boolean isSystemSecure() {
        if (PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get(SYSTEM_SECURE, PPPOEStateMachine.PHASE_INITIALIZE))) {
            return PPPOEStateMachine.PHASE_DEAD.equals(SystemProperties.get(SYSTEM_DEBUGGABLE, PPPOEStateMachine.PHASE_DEAD));
        }
        return DEBUG_DEXOPT_SHELL;
    }

    public void loadSysWhitelist() {
        AntiMalPreInstallScanner.init(this.mContext, isUpgrade());
        AntiMalPreInstallScanner.getInstance().loadSysWhitelist();
    }

    public void checkIllegalSysApk(Package pkg, int hwFlags) throws PackageManagerException {
        if (checkGmsCoreByInstaller(pkg)) {
            Slog.i(TAG, "checkIllegalSysApk checkGmsCoreByInstaller" + pkg);
            return;
        }
        switch (AntiMalPreInstallScanner.getInstance().checkIllegalSysApk(pkg, hwFlags)) {
            case OPTIMIZE_FOR_OTA /*1*/:
                if (isSystemSecure() || ANTIMAL_DEBUG_ON) {
                    throw new PackageManagerException(-115, "checkIllegalSysApk add illegally!");
                }
            case OPTIMIZE_FOR_BOOTING /*2*/:
                hwFlags |= HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM;
                addFlagsForRemovablePreApk(pkg, hwFlags);
                if (!needInstallRemovablePreApk(pkg, hwFlags)) {
                    throw new PackageManagerException(-115, "checkIllegalSysApk apk changed illegally!");
                }
                break;
        }
    }

    protected void addGrantedInstalledPkg(String pkgName, boolean grant) {
        if (grant) {
            synchronized (this.mGrantedInstalledPkg) {
                Slog.i(TAG, "onReceive() package added:" + pkgName);
                this.mGrantedInstalledPkg.add(pkgName);
            }
        }
    }

    private boolean checkInstallGranted(String pkgName) {
        boolean contains;
        synchronized (this.mGrantedInstalledPkg) {
            contains = this.mGrantedInstalledPkg.contains(pkgName);
        }
        return contains;
    }

    public static boolean isPrivAppNonSystemPartitionDir(File path) {
        if (!(path == null || mMultiInstallMap == null || mDelMultiInstallMap == null)) {
            HashSet<String> privAppHashSet = (HashSet) mMultiInstallMap.get(FLAG_APK_PRIV);
            if (privAppHashSet != null && !privAppHashSet.isEmpty() && privAppHashSet.contains(path.getPath())) {
                return true;
            }
            privAppHashSet = (HashSet) mDelMultiInstallMap.get(FLAG_APK_PRIV);
            if (!(privAppHashSet == null || privAppHashSet.isEmpty() || !privAppHashSet.contains(path.getPath()))) {
                return true;
            }
        }
        return DEBUG_DEXOPT_SHELL;
    }

    private synchronized void addNonSystemPartitionApkToHashMap() {
        int i;
        if (mMultiInstallMap == null) {
            mMultiInstallMap = new HashMap();
            ArrayList<File> allAPKList = getApkInstallFileCfgList(APK_INSTALLFILE);
            if (allAPKList != null) {
                for (i = mThreadNum; i < allAPKList.size(); i += OPTIMIZE_FOR_OTA) {
                    Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "get all apk cfg list -->" + i + " --" + ((File) allAPKList.get(i)).getPath());
                }
                HashSet<String> sysInstallSet = new HashSet();
                HashSet<String> privInstallSet = new HashSet();
                mMultiInstallMap.put(FLAG_APK_SYS, sysInstallSet);
                mMultiInstallMap.put(FLAG_APK_PRIV, privInstallSet);
                getMultiAPKInstallList(allAPKList, mMultiInstallMap);
            }
        }
        if (mDelMultiInstallMap == null) {
            mDelMultiInstallMap = new HashMap();
            ArrayList<File> allDelAPKList = getApkInstallFileCfgList(DELAPK_INSTALLFILE);
            if (allDelAPKList != null) {
                for (i = mThreadNum; i < allDelAPKList.size(); i += OPTIMIZE_FOR_OTA) {
                    Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "get all del apk cfg list -->" + i + " --" + ((File) allDelAPKList.get(i)).getPath());
                }
                sysInstallSet = new HashSet();
                privInstallSet = new HashSet();
                HashSet<String> noSysInstallSet = new HashSet();
                mDelMultiInstallMap.put(FLAG_APK_SYS, sysInstallSet);
                mDelMultiInstallMap.put(FLAG_APK_PRIV, privInstallSet);
                mDelMultiInstallMap.put(FLAG_APK_NOSYS, noSysInstallSet);
                getMultiAPKInstallList(allDelAPKList, mDelMultiInstallMap);
            }
        }
    }

    public void scanNonSystemPartitionDir(int scanMode) {
        if (mMultiInstallMap == null || mDelMultiInstallMap == null) {
            addNonSystemPartitionApkToHashMap();
        }
        addGmsCoreApkToHashMap();
        if (!mMultiInstallMap.isEmpty()) {
            installAPKforInstallList((HashSet) mMultiInstallMap.get(FLAG_APK_SYS), 65, scanMode, 0);
            installAPKforInstallList((HashSet) mMultiInstallMap.get(FLAG_APK_PRIV), 193, scanMode, 0);
        }
        if (!mDelMultiInstallMap.isEmpty()) {
            installAPKforInstallList((HashSet) mDelMultiInstallMap.get(FLAG_APK_SYS), 65, scanMode, 0, HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM);
            installAPKforInstallList((HashSet) mDelMultiInstallMap.get(FLAG_APK_PRIV), 193, scanMode, 0, HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM);
            installAPKforInstallList((HashSet) mDelMultiInstallMap.get(FLAG_APK_NOSYS), mThreadNum, scanMode, 0, HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM);
        }
    }

    private void addGmsCoreApkToHashMap() {
        ((HashSet) mDelMultiInstallMap.get(FLAG_APK_PRIV)).add(GMS_CORE_PATH);
        ((HashSet) mDelMultiInstallMap.get(FLAG_APK_PRIV)).add(GMS_FWK_PATH);
        ((HashSet) mDelMultiInstallMap.get(FLAG_APK_PRIV)).add(GMS_LOG_PATH);
    }

    protected static ArrayList<File> getApkInstallFileCfgList(String apkCfgFile) {
        int i;
        String[] strArr = null;
        ArrayList<File> allApkInstallList = new ArrayList();
        try {
            strArr = HwCfgFilePolicy.getCfgPolicyDir(mThreadNum);
            for (i = mThreadNum; i < strArr.length; i += OPTIMIZE_FOR_OTA) {
                Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "getApkInstallFileCfgList from custpolicy i=" + i + "| " + strArr[i]);
            }
        } catch (NoClassDefFoundError e) {
            Slog.w(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (strArr == null) {
            return null;
        }
        for (i = strArr.length - 1; i >= 0; i--) {
            try {
                String canonicalPath = new File(strArr[i]).getCanonicalPath();
                Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "getApkInstallFileCfgList canonicalPath:" + canonicalPath);
                File rawFileAddToList = adjustmccmncList(canonicalPath, apkCfgFile);
                if (rawFileAddToList != null) {
                    Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "getApkInstallFileCfgList add File :" + rawFileAddToList.getPath());
                    allApkInstallList.add(rawFileAddToList);
                }
                File rawNewFileAddToList = adjustmccmncList(new File("data/hw_init/" + canonicalPath).getCanonicalPath(), apkCfgFile);
                if (rawNewFileAddToList != null) {
                    Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "getApkInstallFileCfgList add data File :" + rawNewFileAddToList.getPath());
                    allApkInstallList.add(rawNewFileAddToList);
                }
            } catch (IOException e2) {
                Slog.e(TAG, "Unable to obtain canonical paths");
            }
        }
        if (allApkInstallList.size() == 0) {
            Log.w(TAG, "No config file found for:" + apkCfgFile);
        }
        return allApkInstallList;
    }

    private static File adjustmccmncList(String canonicalPath, String apkFile) {
        File file = null;
        try {
            File adjustRetFile;
            if (mCustPackageManagerService != null && mCustPackageManagerService.isMccMncMatch()) {
                File mccmncFile = new File(canonicalPath + "/" + joinCustomizeFile(apkFile));
                if (mccmncFile.exists()) {
                    adjustRetFile = new File(mccmncFile.getCanonicalPath());
                    try {
                        Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "adjustRetFile mccmnc :" + adjustRetFile.getPath());
                        return adjustRetFile;
                    } catch (IOException e) {
                        file = adjustRetFile;
                        Slog.e(TAG, "Unable to obtain canonical paths");
                        return file;
                    }
                } else if (!new File(canonicalPath + "/" + apkFile).exists()) {
                    return null;
                } else {
                    adjustRetFile = new File(canonicalPath + "/" + apkFile);
                    Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "adjustRetFile :" + adjustRetFile.getPath());
                    return adjustRetFile;
                }
            } else if (!new File(canonicalPath + "/" + apkFile).exists()) {
                return null;
            } else {
                adjustRetFile = new File(canonicalPath + "/" + apkFile);
                Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "adjustRetFile :" + adjustRetFile.getPath());
                return adjustRetFile;
            }
        } catch (IOException e2) {
            Slog.e(TAG, "Unable to obtain canonical paths");
            return file;
        }
    }

    public static String joinCustomizeFile(String fileName) {
        String joinFileName = fileName;
        String mccmnc = SystemProperties.get("persist.sys.mccmnc", AppHibernateCst.INVALID_PKG);
        if (fileName == null) {
            return joinFileName;
        }
        String[] splitArray = fileName.split("\\.");
        if (splitArray.length == OPTIMIZE_FOR_BOOTING) {
            return splitArray[mThreadNum] + "_" + mccmnc + "." + splitArray[OPTIMIZE_FOR_OTA];
        }
        return joinFileName;
    }

    void onRemoveUser(int userId) {
        if (userId >= OPTIMIZE_FOR_OTA) {
            deleteThemeUserFolder(userId);
        }
    }

    private void deleteThemeUserFolder(int userId) {
        CommandLineUtil.rm("system", getHwThemePathAsUser(userId));
    }

    protected void replaceSignatureIfNeeded(PackageSetting ps, Package pkg, boolean isBootScan, boolean isUpdate) {
        if (pkg != null && this.mCompatSettings != null) {
            if (!isBootScan) {
                synchronized (this.mIncompatiblePkg) {
                    if (this.mIncompatiblePkg.contains(pkg.packageName)) {
                        this.mIncompatiblePkg.remove(pkg.packageName);
                    }
                }
            }
            boolean needReplace = DEBUG_DEXOPT_SHELL;
            String str = null;
            if (isBootScan && ps != null) {
                synchronized (this.mPackages) {
                    Package compatPkg = this.mCompatSettings.getCompatPackage(pkg.packageName);
                    if (compatPkg != null && compatPkg.codePath.equals(ps.codePathString) && compatPkg.timeStamp == ps.timeStamp) {
                        needReplace = true;
                        str = compatPkg.certType;
                    }
                }
            }
            if (!needReplace && HwCertificationManager.isSupportHwCertification(pkg)) {
                switch (getHwCertificateType(pkg)) {
                    case OPTIMIZE_FOR_OTA /*1*/:
                        str = CERT_TYPE_PLATFORM;
                        break;
                    case OPTIMIZE_FOR_BOOTING /*2*/:
                        str = CERT_TYPE_TESTKEY;
                        break;
                    case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                        str = CERT_TYPE_SHARED;
                        break;
                    case OPTIMIZE_FOR_OTHER /*4*/:
                        str = CERT_TYPE_MEDIA;
                        break;
                }
                if (str != null) {
                    needReplace = true;
                    Slog.i(TAG, "system signature compat for hwcert package:" + pkg.packageName);
                }
            }
            boolean isSignedByOldSystemSignature = this.mCompatSettings.isOldSystemSignature(pkg.mSignatures);
            if (!needReplace && isSignedByOldSystemSignature && this.mCompatSettings.isWhiteListedApp(pkg)) {
                str = this.mCompatSettings.getOldSignTpye(pkg.mSignatures);
                needReplace = true;
                Slog.i(TAG, "system signature compat for whitelist package:" + pkg.packageName);
                if (!isBootScan) {
                    Flog.bdReport(this.mContext, CPUFeature.MSG_SET_CPUSETCONFIG_VR, "{package:" + pkg.packageName + ",version:" + pkg.mVersionCode + ",type:" + str + "}");
                }
            }
            if (!needReplace && isSignedByOldSystemSignature && isBootScan && !this.mFoundCertCompatFile && isUpgrade()) {
                str = this.mCompatSettings.getOldSignTpye(pkg.mSignatures);
                needReplace = true;
                Slog.i(TAG, "system signature compat for OTA package:" + pkg.packageName);
            }
            if (needReplace) {
                replaceSignatureInner(ps, pkg, str);
            } else if (isSignedByOldSystemSignature && !isBootScan) {
                synchronized (this.mIncompatiblePkg) {
                    if (!this.mIncompatiblePkg.contains(pkg.packageName)) {
                        this.mIncompatiblePkg.add(pkg.packageName);
                    }
                }
                Slog.i(TAG, "illegal system signature package:" + pkg.packageName);
                Flog.bdReport(this.mContext, WifiProCHRManager.WIFI_PORTAL_AUTH_MSG_COLLECTE, "{package:" + pkg.packageName + ",version:" + pkg.mVersionCode + ",type:" + str + "}");
            }
        }
    }

    private void replaceSignatureInner(PackageSetting ps, Package pkg, String signType) {
        if (signType != null && pkg != null && this.mCompatSettings != null) {
            Signature[] signs = this.mCompatSettings.getNewSign(signType);
            if (signs.length == 0) {
                Slog.e(TAG, "signs init fail");
                return;
            }
            Slog.d(TAG, "CertCompatPackage:" + pkg.packageName);
            setRealSignature(pkg, pkg.mSignatures);
            pkg.mSignatures = signs;
            if (!(ps == null || ps.signatures.mSignatures == null)) {
                ps.signatures.mSignatures = signs;
            }
        }
    }

    protected void initCertCompatSettings() {
        Slog.i(TAG, "init CertCompatSettings");
        this.mCompatSettings = new CertCompatSettings();
        this.mFoundCertCompatFile = this.mCompatSettings.readCertCompatPackages();
    }

    protected void resetSharedUserSignaturesIfNeeded() {
        if (this.mCompatSettings != null && !this.mFoundCertCompatFile && isUpgrade()) {
            for (SharedUserSetting setting : this.mSettings.getAllSharedUsersLPw()) {
                if (this.mCompatSettings.isOldSystemSignature(setting.signatures.mSignatures)) {
                    setting.signatures.mSignatures = null;
                    Slog.i(TAG, "SharedUser:" + setting.name + " signature reset!");
                }
            }
        }
    }

    protected void writeCertCompatPackages(boolean update) {
        if (this.mCompatSettings != null) {
            if (update) {
                for (Package pkg : new ArrayList(this.mCompatSettings.getALLCompatPackages())) {
                    if (!(pkg == null || this.mPackages.containsKey(pkg.packageName))) {
                        this.mCompatSettings.removeCertCompatPackage(pkg.packageName);
                    }
                }
            }
            this.mCompatSettings.writeCertCompatPackages();
        }
    }

    protected void updateCertCompatPackage(Package pkg, PackageSetting ps) {
        if (pkg != null && this.mCompatSettings != null) {
            Signature[] realSign = getRealSignature(pkg);
            if (realSign == null || realSign.length == 0 || ps == null) {
                this.mCompatSettings.removeCertCompatPackage(pkg.applicationInfo.packageName);
            } else {
                this.mCompatSettings.insertCompatPackage(pkg.applicationInfo.packageName, ps);
            }
        }
    }

    protected boolean isSystemSignatureUpdated(Signature[] previous, Signature[] current) {
        if (this.mCompatSettings == null) {
            return DEBUG_DEXOPT_SHELL;
        }
        return this.mCompatSettings.isSystemSignatureUpdated(previous, current);
    }

    protected void sendIncompatibleNotificationIfNeeded(String packageName) {
        synchronized (this.mIncompatiblePkg) {
            boolean update = DEBUG_DEXOPT_SHELL;
            boolean send = DEBUG_DEXOPT_SHELL;
            if (this.mIncompatiblePkg.contains(packageName)) {
                this.mIncompatiblePkg.remove(packageName);
                update = true;
                send = true;
            } else if (this.mIncompatNotificationList.contains(packageName)) {
                update = true;
            }
            if (update) {
                UiThread.getHandler().post(new AnonymousClass4(packageName, send));
            }
        }
    }

    private void updateIncompatibleNotification(String packageName, boolean isSend) {
        int i = mThreadNum;
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                int[] resolvedUserIds = am.getRunningUserIds();
                int length;
                if (isSend) {
                    synchronized (this.mIncompatiblePkg) {
                        this.mIncompatNotificationList.add(packageName);
                    }
                    length = resolvedUserIds.length;
                    while (i < length) {
                        sendIncompatibleNotificationInner(packageName, resolvedUserIds[i]);
                        i += OPTIMIZE_FOR_OTA;
                    }
                }
                boolean cancelAll = DEBUG_DEXOPT_SHELL;
                synchronized (this.mPackages) {
                    boolean isSignedByOldSystemSignature = DEBUG_DEXOPT_SHELL;
                    Package pkgInfo = (Package) this.mPackages.get(packageName);
                    if (!(pkgInfo == null || this.mCompatSettings == null)) {
                        isSignedByOldSystemSignature = this.mCompatSettings.isOldSystemSignature(pkgInfo.mSignatures);
                    }
                    if (pkgInfo == null || !r4) {
                        Slog.d(TAG, "Package removed or update to new system signature version, cancel all incompatible notification.");
                        cancelAll = true;
                    }
                }
                if (cancelAll) {
                    synchronized (this.mIncompatiblePkg) {
                        this.mIncompatNotificationList.remove(packageName);
                    }
                }
                length = resolvedUserIds.length;
                while (i < length) {
                    int id = resolvedUserIds[i];
                    if (cancelAll || !isPackageAvailable(packageName, id)) {
                        cancelIncompatibleNotificationInner(packageName, id);
                    }
                    i += OPTIMIZE_FOR_OTA;
                }
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "RemoteException throw when update Incompatible Notification.");
        }
    }

    private void cancelIncompatibleNotificationInner(String packageName, int userId) {
        NotificationManager nm = (NotificationManager) this.mContext.getSystemService("notification");
        if (nm != null) {
            nm.cancelAsUser(packageName, 33685889, new UserHandle(userId));
            Slog.d(TAG, "cancel incompatible notification for u" + userId + ", packageName:" + packageName);
        }
    }

    private void sendIncompatibleNotificationInner(String packageName, int userId) {
        Slog.d(TAG, "send incompatible notification to u" + userId + ", packageName:" + packageName);
        PackageManager pm = this.mContext.getPackageManager();
        if (pm != null) {
            try {
                ApplicationInfo info = pm.getApplicationInfoAsUser(packageName, mThreadNum, userId);
                Drawable icon = pm.getApplicationIcon(info);
                CharSequence title = pm.getApplicationLabel(info);
                String text = this.mContext.getString(33685889);
                PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, mThreadNum, new Intent("android.intent.action.UNINSTALL_PACKAGE", Uri.parse("package:" + packageName)), mThreadNum, null, new UserHandle(userId));
                if (pi == null) {
                    Slog.w(TAG, "Get PendingIntent fail, package: " + packageName);
                    return;
                }
                Notification notification = new Builder(this.mContext).setLargeIcon(UserIcons.convertToBitmap(icon)).setSmallIcon(17301642).setContentTitle(title).setContentText(text).setContentIntent(pi).setDefaults(OPTIMIZE_FOR_BOOTING).setPriority(OPTIMIZE_FOR_BOOTING).setWhen(System.currentTimeMillis()).setShowWhen(true).setAutoCancel(true).addAction(new Action.Builder(null, this.mContext.getString(33685890), pi).build()).build();
                NotificationManager nm = (NotificationManager) this.mContext.getSystemService("notification");
                if (nm != null) {
                    nm.notifyAsUser(packageName, 33685889, notification, new UserHandle(userId));
                }
            } catch (NameNotFoundException e) {
                Slog.w(TAG, "incompatible package: " + packageName + " not find for u" + userId);
            }
        }
    }

    protected void updateCloneAppList(String removedPackage, boolean replacing, int[] removedUsers) {
        int i = mThreadNum;
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP && !replacing && removedPackage != null) {
            if (removedUsers == null) {
                long callingId = Binder.clearCallingIdentity();
                try {
                    IActivityManager am = ActivityManagerNative.getDefault();
                    if (am != null) {
                        removedUsers = am.getRunningUserIds();
                        Binder.restoreCallingIdentity(callingId);
                    } else {
                        return;
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "error to getRunningUserIds from ams");
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
            if (removedUsers == null || removedUsers.length == 0) {
                Slog.w(TAG, "There is no any valid users");
                return;
            }
            String clonedPackages = Secure.getString(this.mContext.getContentResolver(), "clone_app_list");
            if (clonedPackages == null || clonedPackages.isEmpty()) {
                Slog.w(TAG, "There is no any clone packages");
                return;
            }
            if ((clonedPackages + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER).contains(removedPackage + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                int length = removedUsers.length;
                while (i < length) {
                    int userId = removedUsers[i];
                    if (userId == 0 || userId == -1) {
                        String clonedPackagesAfterRemoved = clonedPackages.replaceAll(removedPackage, AppHibernateCst.INVALID_PKG).replaceAll(";;", CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                        Secure.putString(this.mContext.getContentResolver(), "clone_app_list", clonedPackagesAfterRemoved);
                        Slog.w(TAG, "updateCloneAppList clonedPackages: " + clonedPackages + ", removedPackage: " + removedPackage + ", replacing: " + replacing + ", removedUsers: " + Arrays.toString(removedUsers) + ", clonedPackagesAfterRemoved: " + clonedPackagesAfterRemoved);
                        return;
                    }
                    i += OPTIMIZE_FOR_OTA;
                }
            }
        }
    }

    protected static void createPackagesAbiFile() {
        mPackagesAbi = new ArrayMap();
        try {
            mSystemDir = new File(Environment.getDataDirectory(), "system");
            if (mSystemDir.exists() || mSystemDir.mkdirs()) {
                mPakcageAbiFilename = new File(mSystemDir, "packages-abi.xml");
            } else {
                Slog.i(TAG, "Packages-abi file create error");
            }
        } catch (SecurityException e) {
            Slog.i(TAG, "Packages-abi file SecurityException: " + e.getMessage());
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void readPackagesAbiLPw() {
        IOException e;
        XmlPullParserException e2;
        Throwable th;
        FileInputStream fileInputStream = null;
        if (mPakcageAbiFilename.exists()) {
            try {
                FileInputStream str = new FileInputStream(mPakcageAbiFilename);
                try {
                    int type;
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(str, StandardCharsets.UTF_8.name());
                    do {
                        type = parser.next();
                        if (type == OPTIMIZE_FOR_BOOTING) {
                            break;
                        }
                    } while (type != OPTIMIZE_FOR_OTA);
                    if (type != OPTIMIZE_FOR_BOOTING) {
                        mReadMessages.append("No start tag found in settings file\n");
                        PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager settings");
                        Slog.wtf("PackageManager", "No start tag found in package manager settings");
                        if (str != null) {
                            try {
                                str.close();
                            } catch (IOException e3) {
                                Slog.e("PackageManager", "IO error when closing packages-abi file:" + e3.getMessage());
                            }
                        }
                        return;
                    }
                    int outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type == OPTIMIZE_FOR_OTA || (type == 3 && parser.getDepth() <= outerDepth)) {
                            if (str != null) {
                                try {
                                    str.close();
                                } catch (IOException e32) {
                                    Slog.e("PackageManager", "IO error when closing packages-abi file:" + e32.getMessage());
                                }
                            }
                        } else if (!(type == 3 || type == OPTIMIZE_FOR_OTHER || !parser.getName().equals(ControlScope.PACKAGE_ELEMENT_KEY))) {
                            String name = parser.getAttributeValue(null, MemoryConstant.MEM_POLICY_ACTIONNAME);
                            String abiCode = parser.getAttributeValue(null, "abiCode");
                            String version = parser.getAttributeValue(null, "version");
                            if (TextUtils.isEmpty(name)) {
                                Slog.wtf("PackageManager", "Error in package abi file: pakcage name is null");
                            }
                            int code = -1;
                            if (abiCode != null) {
                                try {
                                    code = Integer.parseInt(abiCode);
                                } catch (NumberFormatException e4) {
                                    Slog.i(TAG, "read package: " + name + " abi code error.");
                                }
                            }
                            int versionCode = mThreadNum;
                            if (!TextUtils.isEmpty(version)) {
                                try {
                                    versionCode = Integer.parseInt(version);
                                } catch (NumberFormatException e5) {
                                    Slog.i(TAG, "read package: " + version + " abi code error.");
                                }
                            }
                            addPackagesAbiLPw(name, code, true, versionCode);
                        }
                    }
                    if (str != null) {
                        str.close();
                    }
                    return;
                } catch (XmlPullParserException e6) {
                    e2 = e6;
                    fileInputStream = str;
                } catch (IOException e7) {
                    e32 = e7;
                    fileInputStream = str;
                } catch (Throwable th2) {
                    th = th2;
                    fileInputStream = str;
                }
            } catch (XmlPullParserException e8) {
                e2 = e8;
                try {
                    Slog.e("PackageManager", "XML parser error:" + e2.getMessage());
                    mPackagesAbi.clear();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e322) {
                            Slog.e("PackageManager", "IO error when closing packages-abi file:" + e322.getMessage());
                        }
                    }
                    return;
                } catch (Throwable th3) {
                    th = th3;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e3222) {
                            Slog.e("PackageManager", "IO error when closing packages-abi file:" + e3222.getMessage());
                        }
                    }
                    throw th;
                }
            } catch (IOException e9) {
                e3222 = e9;
                Slog.e("PackageManager", "Error reading package manager settings file:" + e3222.getMessage());
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e32222) {
                        Slog.e("PackageManager", "IO error when closing packages-abi file:" + e32222.getMessage());
                    }
                }
                return;
            }
        }
        Slog.i(TAG, "PakcageAbiFilename isn't exists");
    }

    protected void writePackagesAbi() {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        IllegalArgumentException e3;
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        if (mPackagesAbi != null) {
            try {
                BufferedOutputStream str;
                FileOutputStream fstr = new FileOutputStream(mPakcageAbiFilename);
                try {
                    str = new BufferedOutputStream(fstr);
                } catch (FileNotFoundException e4) {
                    e = e4;
                    fileOutputStream = fstr;
                    try {
                        Slog.e("PackageManager", "File not found when writing packages-abi file: " + e.getMessage());
                        if (bufferedOutputStream != null) {
                            try {
                                bufferedOutputStream.close();
                            } catch (IOException e22) {
                                PackageManagerService.reportSettingsProblem(6, "Error close writing settings: " + e22);
                                Slog.e("PackageManager", "Error close writing package manager settings" + e22.getMessage());
                            }
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        FileUtils.setPermissions(mPakcageAbiFilename.toString(), 432, -1, -1);
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedOutputStream != null) {
                            try {
                                bufferedOutputStream.close();
                            } catch (IOException e222) {
                                PackageManagerService.reportSettingsProblem(6, "Error close writing settings: " + e222);
                                Slog.e("PackageManager", "Error close writing package manager settings" + e222.getMessage());
                                throw th;
                            }
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        FileUtils.setPermissions(mPakcageAbiFilename.toString(), 432, -1, -1);
                        throw th;
                    }
                } catch (IllegalArgumentException e5) {
                    e3 = e5;
                    fileOutputStream = fstr;
                    Slog.e("PackageManager", "IllegalArgument when writing packages-abi file: " + e3.getMessage());
                    if (bufferedOutputStream != null) {
                        try {
                            bufferedOutputStream.close();
                        } catch (IOException e2222) {
                            PackageManagerService.reportSettingsProblem(6, "Error close writing settings: " + e2222);
                            Slog.e("PackageManager", "Error close writing package manager settings" + e2222.getMessage());
                        }
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    FileUtils.setPermissions(mPakcageAbiFilename.toString(), 432, -1, -1);
                } catch (IOException e6) {
                    e2222 = e6;
                    fileOutputStream = fstr;
                    PackageManagerService.reportSettingsProblem(6, "IOException when writing packages-abi settings: " + e2222);
                    Slog.e("PackageManager", "IOException when writing packages-abi file: " + e2222.getMessage());
                    if (bufferedOutputStream != null) {
                        try {
                            bufferedOutputStream.close();
                        } catch (IOException e22222) {
                            PackageManagerService.reportSettingsProblem(6, "Error close writing settings: " + e22222);
                            Slog.e("PackageManager", "Error close writing package manager settings" + e22222.getMessage());
                        }
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    FileUtils.setPermissions(mPakcageAbiFilename.toString(), 432, -1, -1);
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream = fstr;
                    if (bufferedOutputStream != null) {
                        bufferedOutputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    FileUtils.setPermissions(mPakcageAbiFilename.toString(), 432, -1, -1);
                    throw th;
                }
                try {
                    XmlSerializer serializer = new FastXmlSerializer();
                    serializer.setOutput(str, StandardCharsets.UTF_8.name());
                    serializer.startDocument(null, Boolean.valueOf(true));
                    serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                    serializer.startTag(null, "packages");
                    for (AbiInfo info : mPackagesAbi.values()) {
                        serializer.startTag(null, ControlScope.PACKAGE_ELEMENT_KEY);
                        serializer.attribute(null, MemoryConstant.MEM_POLICY_ACTIONNAME, info.name);
                        serializer.attribute(null, "abiCode", Integer.toString(info.abiCode));
                        serializer.attribute(null, "version", Integer.toString(info.version));
                        serializer.endTag(null, ControlScope.PACKAGE_ELEMENT_KEY);
                    }
                    serializer.endTag(null, "packages");
                    serializer.endDocument();
                    str.flush();
                    FileUtils.sync(fstr);
                    if (str != null) {
                        try {
                            str.close();
                        } catch (IOException e222222) {
                            PackageManagerService.reportSettingsProblem(6, "Error close writing settings: " + e222222);
                            Slog.e("PackageManager", "Error close writing package manager settings" + e222222.getMessage());
                        }
                    }
                    if (fstr != null) {
                        fstr.close();
                    }
                    FileUtils.setPermissions(mPakcageAbiFilename.toString(), 432, -1, -1);
                } catch (FileNotFoundException e7) {
                    e = e7;
                    bufferedOutputStream = str;
                    fileOutputStream = fstr;
                    Slog.e("PackageManager", "File not found when writing packages-abi file: " + e.getMessage());
                    if (bufferedOutputStream != null) {
                        bufferedOutputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    FileUtils.setPermissions(mPakcageAbiFilename.toString(), 432, -1, -1);
                } catch (IllegalArgumentException e8) {
                    e3 = e8;
                    bufferedOutputStream = str;
                    fileOutputStream = fstr;
                    Slog.e("PackageManager", "IllegalArgument when writing packages-abi file: " + e3.getMessage());
                    if (bufferedOutputStream != null) {
                        bufferedOutputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    FileUtils.setPermissions(mPakcageAbiFilename.toString(), 432, -1, -1);
                } catch (IOException e9) {
                    e222222 = e9;
                    bufferedOutputStream = str;
                    fileOutputStream = fstr;
                    PackageManagerService.reportSettingsProblem(6, "IOException when writing packages-abi settings: " + e222222);
                    Slog.e("PackageManager", "IOException when writing packages-abi file: " + e222222.getMessage());
                    if (bufferedOutputStream != null) {
                        bufferedOutputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    FileUtils.setPermissions(mPakcageAbiFilename.toString(), 432, -1, -1);
                } catch (Throwable th4) {
                    th = th4;
                    bufferedOutputStream = str;
                    fileOutputStream = fstr;
                    if (bufferedOutputStream != null) {
                        bufferedOutputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    FileUtils.setPermissions(mPakcageAbiFilename.toString(), 432, -1, -1);
                    throw th;
                }
            } catch (FileNotFoundException e10) {
                e = e10;
                Slog.e("PackageManager", "File not found when writing packages-abi file: " + e.getMessage());
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                FileUtils.setPermissions(mPakcageAbiFilename.toString(), 432, -1, -1);
            } catch (IllegalArgumentException e11) {
                e3 = e11;
                Slog.e("PackageManager", "IllegalArgument when writing packages-abi file: " + e3.getMessage());
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                FileUtils.setPermissions(mPakcageAbiFilename.toString(), 432, -1, -1);
            } catch (IOException e12) {
                e222222 = e12;
                PackageManagerService.reportSettingsProblem(6, "IOException when writing packages-abi settings: " + e222222);
                Slog.e("PackageManager", "IOException when writing packages-abi file: " + e222222.getMessage());
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                FileUtils.setPermissions(mPakcageAbiFilename.toString(), 432, -1, -1);
            }
        }
    }

    protected int getPackagesAbi(String name) {
        int i = DEFAULT_PACKAGE_ABI;
        if (mPackagesAbi == null) {
            return DEFAULT_PACKAGE_ABI;
        }
        AbiInfo info = (AbiInfo) mPackagesAbi.get(name);
        if (info != null) {
            i = info.getAbiCode();
        }
        return i;
    }

    protected int getPackageVersion(String name) {
        int i = mThreadNum;
        if (mPackagesAbi == null) {
            return mThreadNum;
        }
        AbiInfo info = (AbiInfo) mPackagesAbi.get(name);
        if (info != null) {
            i = info.getVersion();
        }
        return i;
    }

    protected void removePackageAbiLPw(String name) {
        if (mPackagesAbi != null) {
            mPackagesAbi.remove(name);
        }
    }

    protected void addPackagesAbiLPw(String name, int code, boolean flag, int versionCode) {
        if (mPackagesAbi != null) {
            AbiInfo info = (AbiInfo) mPackagesAbi.get(name);
            if (info != null) {
                if (flag) {
                    int index = mPackagesAbi.indexOfKey(name);
                    info.setVersion(versionCode);
                    if (index >= 0) {
                        mPackagesAbi.setValueAt(index, info);
                    }
                }
                return;
            }
            mPackagesAbi.put(name, new AbiInfo(name, code, versionCode));
        }
    }

    @FindBugsSuppressWarnings({"UC_USELESS_CONDITION"})
    protected void readLastedAbi(Package pkg, File scanFile, String cpuAbiOverride) throws PackageManagerException {
        setNativeLibraryPaths(pkg);
        int copyRet = mThreadNum;
        AutoCloseable autoCloseable = null;
        try {
            String[] abiList;
            autoCloseable = Handle.create(scanFile);
            if (cpuAbiOverride != null) {
                abiList = new String[OPTIMIZE_FOR_OTA];
                abiList[mThreadNum] = cpuAbiOverride;
            } else {
                abiList = Build.SUPPORTED_ABIS;
            }
            boolean needsRenderScriptOverride = DEBUG_DEXOPT_SHELL;
            if (Build.SUPPORTED_64_BIT_ABIS.length > 0 && cpuAbiOverride == null && NativeLibraryHelper.hasRenderscriptBitcode(autoCloseable)) {
                abiList = Build.SUPPORTED_32_BIT_ABIS;
                needsRenderScriptOverride = true;
            }
            if (this.mSettings != null) {
                copyRet = getPackagesAbi(pkg.packageName);
            }
            if (copyRet >= 0 || copyRet == -114) {
                if (copyRet >= 0) {
                    pkg.applicationInfo.primaryCpuAbi = abiList[copyRet];
                } else if (copyRet == -114 && cpuAbiOverride != null) {
                    pkg.applicationInfo.primaryCpuAbi = cpuAbiOverride;
                } else if (needsRenderScriptOverride) {
                    pkg.applicationInfo.primaryCpuAbi = abiList[mThreadNum];
                }
                setNativeLibraryPaths(pkg);
                return;
            }
            throw new PackageManagerException(-110, "Error unpackaging native libs for app, errorCode=" + copyRet);
        } catch (IOException ioe) {
            Slog.e(TAG, "Unable to get canonical file " + ioe.toString());
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    protected boolean isPackageAbiRestored(String name) {
        boolean z = DEBUG_DEXOPT_SHELL;
        if (mPackagesAbi == null) {
            return DEBUG_DEXOPT_SHELL;
        }
        AbiInfo info = (AbiInfo) mPackagesAbi.get(name);
        if (!(info == null || info.getAbiCode() == DEFAULT_PACKAGE_ABI)) {
            z = true;
        }
        return z;
    }

    protected void deletePackagesAbiFile() {
        if (mPackagesAbi != null) {
            mPackagesAbi.clear();
        }
        try {
            File mSystemDir = new File(Environment.getDataDirectory(), "system");
            if (mSystemDir.exists()) {
                File abiConfigfile = new File(mSystemDir, "packages-abi.xml");
                if (abiConfigfile.exists() && !abiConfigfile.delete()) {
                    Slog.i(TAG, "Packages-abi file delete error.");
                }
            }
        } catch (SecurityException e) {
            Slog.i(TAG, "Packages-abi file SecurityException: " + e.getMessage());
        }
    }

    protected boolean isPackagePathWithNoSysFlag(File filePath) {
        if (!(filePath == null || mDelMultiInstallMap == null)) {
            HashSet<String> delInstallSet = (HashSet) mDelMultiInstallMap.get(FLAG_APK_NOSYS);
            if (!(delInstallSet == null || delInstallSet.isEmpty() || !delInstallSet.contains(filePath.getPath()))) {
                return true;
            }
        }
        return DEBUG_DEXOPT_SHELL;
    }

    private boolean checkLimitePackageBroadcast(String action, String pkg, String targetPkg) {
        String[] callingPkgNames = getPackagesForUid(Binder.getCallingUid());
        if (callingPkgNames == null || callingPkgNames.length <= 0) {
            Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "Android Wear-checkLimitePackageBroadcast: callingPkgNames is empty");
            return DEBUG_DEXOPT_SHELL;
        }
        String callingPkgName = callingPkgNames[mThreadNum];
        Flog.d(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "Android Wear-checkLimitePackageBroadcast: callingPkgName = " + callingPkgName);
        if ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_REMOVED".equals(action)) {
            int i;
            boolean targetPkgExist = DEBUG_DEXOPT_SHELL;
            String[] strArr = LIMITED_TARGET_PACKAGE_NAMES;
            int length = strArr.length;
            for (i = mThreadNum; i < length; i += OPTIMIZE_FOR_OTA) {
                if (strArr[i].equals(targetPkg)) {
                    targetPkgExist = true;
                    break;
                }
            }
            if (targetPkgExist) {
                boolean pkgExist = DEBUG_DEXOPT_SHELL;
                strArr = LIMITED_PACKAGE_NAMES;
                length = strArr.length;
                for (i = mThreadNum; i < length; i += OPTIMIZE_FOR_OTA) {
                    if (strArr[i].equals(pkg)) {
                        pkgExist = true;
                        break;
                    }
                }
                if (!pkgExist) {
                    Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "Android Wear-checkLimitePackageBroadcast: pkg is not permitted");
                    return DEBUG_DEXOPT_SHELL;
                } else if (isSystemApp(getApplicationInfo(callingPkgName, mThreadNum, this.mContext.getUserId()))) {
                    Flog.d(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "Android Wear-checkLimitePackageBroadcast: success");
                    return true;
                } else {
                    Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "Android Wear-checkLimitePackageBroadcast: is not System App.");
                    return DEBUG_DEXOPT_SHELL;
                }
            }
            Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "Android Wear-checkLimitePackageBroadcast: targetPkg is not permitted");
            return DEBUG_DEXOPT_SHELL;
        }
        Flog.i(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "Android Wear-checkLimitePackageBroadcast: action is not permitted");
        return DEBUG_DEXOPT_SHELL;
    }

    private boolean isSystemApp(ApplicationInfo appInfo) {
        boolean bSystemApp = DEBUG_DEXOPT_SHELL;
        if (appInfo != null) {
            bSystemApp = (appInfo.flags & OPTIMIZE_FOR_OTA) != 0 ? true : DEBUG_DEXOPT_SHELL;
        }
        Flog.d(GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX, "Android Wear-checkLimitePackageBroadcast: bSystemApp=" + bSystemApp);
        return bSystemApp;
    }

    private void sendLimitedPackageBroadcast(String action, String pkg, Bundle extras, String targetPkg, int[] userIds) {
        if (checkLimitePackageBroadcast(action, pkg, targetPkg)) {
            this.mHandler.post(new AnonymousClass5(userIds, action, pkg, extras, targetPkg));
            return;
        }
        throw new SecurityException("sendLimitedPackageBroadcast: checkLimitePackageBroadcast failed");
    }

    private boolean checkWhetherToReboot() {
        int times = Secure.getInt(this.mContext.getContentResolver(), REBOOT_TIMES_WHEN_PREPARE_USER_DATA_KEY, mThreadNum);
        if (times < OPTIMIZE_FOR_BOOTING) {
            Slog.i(TAG, "check result is to go to reboot, times:" + times);
            Secure.putInt(this.mContext.getContentResolver(), REBOOT_TIMES_WHEN_PREPARE_USER_DATA_KEY, times + OPTIMIZE_FOR_OTA);
            return true;
        }
        Slog.i(TAG, "check result is to go to eRecovery, times:" + times);
        return DEBUG_DEXOPT_SHELL;
    }

    protected void tryToReboot() {
        try {
            if (checkWhetherToReboot()) {
                Thread.sleep(1000);
                Stub.asInterface(ServiceManager.getService("power")).reboot(DEBUG_DEXOPT_SHELL, "prepare user data failed! try to reboot...", DEBUG_DEXOPT_SHELL);
                return;
            }
            SystemProperties.set("sys.userstorage_block", PPPOEStateMachine.PHASE_INITIALIZE);
        } catch (Exception e) {
            Slog.e(TAG, "try to reboot error, exception:" + e);
            SystemProperties.set("sys.userstorage_block", PPPOEStateMachine.PHASE_INITIALIZE);
        }
    }

    protected void resetRebootTimes() {
        Secure.putInt(this.mContext.getContentResolver(), REBOOT_TIMES_WHEN_PREPARE_USER_DATA_KEY, mThreadNum);
    }

    protected void computeMetaHash(Package pkg) {
        this.pkgMetaHash.put(pkg.packageName, getSHA256(pkg));
    }

    private String getSHA256(Package pkg) {
        Throwable th;
        StrictJarFile strictJarFile = null;
        try {
            StrictJarFile jarFile = new StrictJarFile(pkg.baseCodePath, DEBUG_DEXOPT_SHELL, DEBUG_DEXOPT_SHELL);
            try {
                ZipEntry ze = jarFile.findEntry(Utils.MANIFEST_NAME);
                if (ze != null) {
                    String sha = getSHA(Streams.readFully(jarFile.getInputStream(ze)));
                    if (jarFile != null) {
                        try {
                            jarFile.close();
                        } catch (IOException e) {
                            Log.w(TAG, "close jar file counter exception!");
                        }
                    }
                    return sha;
                }
                Log.d(TAG, "ZipEntry is null");
                if (jarFile != null) {
                    try {
                        jarFile.close();
                    } catch (IOException e2) {
                        Log.w(TAG, "close jar file counter exception!");
                    }
                }
                strictJarFile = jarFile;
                return AppHibernateCst.INVALID_PKG;
            } catch (IOException e3) {
                strictJarFile = jarFile;
                if (strictJarFile != null) {
                    try {
                        strictJarFile.close();
                    } catch (IOException e4) {
                        Log.w(TAG, "close jar file counter exception!");
                    }
                }
                return AppHibernateCst.INVALID_PKG;
            } catch (Throwable th2) {
                th = th2;
                strictJarFile = jarFile;
                if (strictJarFile != null) {
                    try {
                        strictJarFile.close();
                    } catch (IOException e5) {
                        Log.w(TAG, "close jar file counter exception!");
                    }
                }
                throw th;
            }
        } catch (IOException e6) {
            if (strictJarFile != null) {
                strictJarFile.close();
            }
            return AppHibernateCst.INVALID_PKG;
        } catch (Throwable th3) {
            th = th3;
            if (strictJarFile != null) {
                strictJarFile.close();
            }
            throw th;
        }
    }

    private String getSHA(byte[] manifest) {
        StringBuffer output = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(manifest);
            byte[] b = md.digest();
            for (int i = mThreadNum; i < b.length; i += OPTIMIZE_FOR_OTA) {
                String temp = Integer.toHexString(b[i] & com.android.server.display.Utils.MAXINUM_TEMPERATURE);
                if (temp.length() < OPTIMIZE_FOR_BOOTING) {
                    output.append(PPPOEStateMachine.PHASE_DEAD);
                }
                output.append(temp);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "get sha256 failed");
        }
        return output.toString();
    }

    protected void recordInstallAppInfo(String pkgName, long beginTime, int installFlags) {
        long endTime = SystemClock.elapsedRealtime();
        int srcPkg = mThreadNum;
        if ((installFlags & 32) != 0) {
            srcPkg = OPTIMIZE_FOR_OTA;
        }
        insertAppInfo(pkgName, srcPkg, beginTime, endTime);
    }

    public void insertAppInfo(String pkgName, int srcPkg, long beginTime, long endTime) {
        if (TextUtils.isEmpty(pkgName)) {
            Slog.e(TAG, "insertAppInfo pkgName is null");
        } else if (this.needCollectAppInfo) {
            try {
                IHoldService service = StubController.getHoldService();
                if (service == null) {
                    Slog.e(TAG, "insertAppInfo getHoldService is null.");
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString(PACKAGE_NAME, pkgName);
                bundle.putInt(SOURCE_PACKAGE_NAME, srcPkg);
                bundle.putLong(INSTALL_BEGIN, beginTime);
                bundle.putLong(INSTALL_END, endTime);
                Bundle res = service.callHsmService(ANTIMAL_MODULE, bundle);
                Slog.i(TAG, "insertAppInfo pkgName:" + pkgName + " time:" + SystemClock.elapsedRealtime());
                if (res != null && res.getInt(INSERT_RESULT) != 0) {
                    this.needCollectAppInfo = DEBUG_DEXOPT_SHELL;
                }
            } catch (Exception e) {
                Slog.e(TAG, "insertAppInfo EXCEPTION = " + e);
            }
        } else {
            Slog.i(TAG, "AntiMalware is closed");
        }
    }

    protected void addPreinstalledPkgToList(Package scannedPkg) {
        if (scannedPkg != null && scannedPkg.baseCodePath != null) {
            boolean z;
            if (scannedPkg.baseCodePath.startsWith("/data/app/")) {
                z = true;
            } else {
                z = scannedPkg.baseCodePath.startsWith("/data/app-private/");
            }
            if (!z) {
                preinstalledPackageList.add(scannedPkg.packageName);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected List<String> getPreinstalledApkList() {
        FileNotFoundException e;
        Throwable th;
        XmlPullParserException e2;
        IOException e3;
        List<String> preinstalledApkList = new ArrayList();
        File preinstalledApkFile = new File(CLOUD_APK_DIR, "preinstalled_app_list_file.xml");
        FileInputStream fileInputStream = null;
        try {
            FileInputStream stream = new FileInputStream(preinstalledApkFile);
            try {
                int type;
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, null);
                do {
                    type = parser.next();
                    if (type == OPTIMIZE_FOR_OTA) {
                        break;
                    }
                } while (type != OPTIMIZE_FOR_BOOTING);
                String tag = parser.getName();
                if ("values".equals(tag)) {
                    parser.next();
                    int outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type == OPTIMIZE_FOR_OTA || (type == 3 && parser.getDepth() <= outerDepth)) {
                            if (stream != null) {
                                try {
                                    stream.close();
                                } catch (IOException e4) {
                                }
                            }
                        } else if (!(type == 3 || type == OPTIMIZE_FOR_OTHER || !"string".equals(parser.getName()) || parser.getAttributeValue(OPTIMIZE_FOR_OTA) == null)) {
                            preinstalledApkList.add(parser.getAttributeValue(OPTIMIZE_FOR_OTA));
                        }
                    }
                    if (stream != null) {
                        stream.close();
                    }
                    return preinstalledApkList;
                }
                throw new XmlPullParserException("Settings do not start with policies tag: found " + tag);
            } catch (FileNotFoundException e5) {
                e = e5;
                fileInputStream = stream;
                try {
                    Slog.w(TAG, "file is not exist " + e.getMessage());
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e6) {
                        }
                    }
                    return preinstalledApkList;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e7) {
                        }
                    }
                    throw th;
                }
            } catch (XmlPullParserException e8) {
                e2 = e8;
                fileInputStream = stream;
                Slog.w(TAG, "failed parsing " + preinstalledApkFile + " " + e2.getMessage());
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e9) {
                    }
                }
                return preinstalledApkList;
            } catch (IOException e10) {
                e3 = e10;
                fileInputStream = stream;
                Slog.w(TAG, "failed parsing " + preinstalledApkFile + " " + e3.getMessage());
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e11) {
                    }
                }
                return preinstalledApkList;
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = stream;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e12) {
            e = e12;
            Slog.w(TAG, "file is not exist " + e.getMessage());
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return preinstalledApkList;
        } catch (XmlPullParserException e13) {
            e2 = e13;
            Slog.w(TAG, "failed parsing " + preinstalledApkFile + " " + e2.getMessage());
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return preinstalledApkList;
        } catch (IOException e14) {
            e3 = e14;
            Slog.w(TAG, "failed parsing " + preinstalledApkFile + " " + e3.getMessage());
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return preinstalledApkList;
        }
    }

    protected void writePreinstalledApkListToFile() {
        NameNotFoundException e;
        IOException e2;
        Throwable th;
        File preinstalledApkFile = new File(CLOUD_APK_DIR, "preinstalled_app_list_file.xml");
        if (!preinstalledApkFile.exists()) {
            FileOutputStream fileOutputStream = null;
            try {
                if (preinstalledApkFile.createNewFile()) {
                    FileUtils.setPermissions(preinstalledApkFile.getPath(), 416, -1, -1);
                }
                FileOutputStream stream = new FileOutputStream(preinstalledApkFile, DEBUG_DEXOPT_SHELL);
                try {
                    XmlSerializer out = new FastXmlSerializer();
                    out.setOutput(stream, "utf-8");
                    out.startDocument(null, Boolean.valueOf(true));
                    out.startTag(null, "values");
                    int N = preinstalledPackageList.size();
                    PackageManager pm = this.mContext.getPackageManager();
                    for (int i = mThreadNum; i < N; i += OPTIMIZE_FOR_OTA) {
                        String packageName = (String) preinstalledPackageList.get(i);
                        String apkName = pm.getApplicationLabel(pm.getApplicationInfo(packageName, HwSecDiagnoseConstant.BIT_VERIFYBOOT)).toString();
                        out.startTag(null, "string");
                        out.attribute(null, MemoryConstant.MEM_POLICY_ACTIONNAME, packageName);
                        out.attribute(null, "apkName", apkName);
                        out.endTag(null, "string");
                    }
                    out.endTag(null, "values");
                    out.endDocument();
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e3) {
                        }
                    }
                    fileOutputStream = stream;
                } catch (NameNotFoundException e4) {
                    e = e4;
                    fileOutputStream = stream;
                    Slog.w(TAG, "failed parsing " + preinstalledApkFile + " " + e.getMessage());
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e5) {
                        }
                    }
                } catch (IOException e6) {
                    e2 = e6;
                    fileOutputStream = stream;
                    try {
                        Slog.w(TAG, "failed parsing " + preinstalledApkFile + " " + e2.getMessage());
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e7) {
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e8) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream = stream;
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
            } catch (NameNotFoundException e9) {
                e = e9;
                Slog.w(TAG, "failed parsing " + preinstalledApkFile + " " + e.getMessage());
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e10) {
                e2 = e10;
                Slog.w(TAG, "failed parsing " + preinstalledApkFile + " " + e2.getMessage());
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            }
        }
    }

    public boolean checkIllegalGmsCoreApk(Package pkg) {
        if (checkGmsCoreByInstaller(pkg)) {
            Signature[] gmsSignatures = new Signature[OPTIMIZE_FOR_OTA];
            gmsSignatures[mThreadNum] = new Signature("308204433082032ba003020102020900c2e08746644a308d300d06092a864886f70d01010405003074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726f6964301e170d3038303832313233313333345a170d3336303130373233313333345a3074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726f696430820120300d06092a864886f70d01010105000382010d00308201080282010100ab562e00d83ba208ae0a966f124e29da11f2ab56d08f58e2cca91303e9b754d372f640a71b1dcb130967624e4656a7776a92193db2e5bfb724a91e77188b0e6a47a43b33d9609b77183145ccdf7b2e586674c9e1565b1f4c6a5955bff251a63dabf9c55c27222252e875e4f8154a645f897168c0b1bfc612eabf785769bb34aa7984dc7e2ea2764cae8307d8c17154d7ee5f64a51a44a602c249054157dc02cd5f5c0e55fbef8519fbe327f0b1511692c5a06f19d18385f5c4dbc2d6b93f68cc2979c70e18ab93866b3bd5db8999552a0e3b4c99df58fb918bedc182ba35e003c1b4b10dd244a8ee24fffd333872ab5221985edab0fc0d0b145b6aa192858e79020103a381d93081d6301d0603551d0e04160414c77d8cc2211756259a7fd382df6be398e4d786a53081a60603551d2304819e30819b8014c77d8cc2211756259a7fd382df6be398e4d786a5a178a4763074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726f6964820900c2e08746644a308d300c0603551d13040530030101ff300d06092a864886f70d010104050003820101006dd252ceef85302c360aaace939bcff2cca904bb5d7a1661f8ae46b2994204d0ff4a68c7ed1a531ec4595a623ce60763b167297a7ae35712c407f208f0cb109429124d7b106219c084ca3eb3f9ad5fb871ef92269a8be28bf16d44c8d9a08e6cb2f005bb3fe2cb96447e868e731076ad45b33f6009ea19c161e62641aa99271dfd5228c5c587875ddb7f452758d661f6cc0cccb7352e424cc4365c523532f7325137593c4ae341f4db41edda0d0b1071a7c440f0fe9ea01cb627ca674369d084bd2fd911ff06cdbf2cfa10dc0f893ae35762919048c7efc64c7144178342f70581c9de573af55b390dd7fdb9418631895d5f759f30112687ff621410c069308a");
            if (compareSignatures(gmsSignatures, pkg.mSignatures) != 0) {
                Slog.e(TAG, "GmsCore signature not match: " + pkg.packageName);
                return true;
            }
            Slog.d(TAG, "GmsCore signature match");
        }
        return DEBUG_DEXOPT_SHELL;
    }

    private boolean checkGmsCoreByInstaller(Package pkg) {
        if (LocationManagerServiceUtil.GOOGLE_GMS_PROCESS.equals(pkg.applicationInfo.packageName) && "/data/hw_init/system/app/GmsCore/GmsCore.apk".equals(pkg.baseCodePath)) {
            return true;
        }
        if ("com.google.android.gsf".equals(pkg.applicationInfo.packageName) && "/data/hw_init/system/app/GoogleServicesFramework/GoogleServicesFramework.apk".equals(pkg.baseCodePath)) {
            return true;
        }
        if ("com.google.android.gsf.login".equals(pkg.applicationInfo.packageName) && "/data/hw_init/system/app/GoogleLoginService/GoogleLoginService.apk".equals(pkg.baseCodePath)) {
            return true;
        }
        return DEBUG_DEXOPT_SHELL;
    }

    private boolean checkGmsCoreUninstalled() {
        if (isUninstalledDelapp(LocationManagerServiceUtil.GOOGLE_GMS_PROCESS) || isUninstalledDelapp("com.google.android.gsf.login") || isUninstalledDelapp("com.google.android.gsf")) {
            return true;
        }
        return DEBUG_DEXOPT_SHELL;
    }

    private void deleteGmsCoreFromUninstalledDelapp() {
        IOException e;
        Throwable th;
        Exception e2;
        File file = new File(CLOUD_APK_DIR, "uninstalled_delapp.xml");
        loadUninstalledDelapp(file);
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream stream = new FileOutputStream(file, DEBUG_DEXOPT_SHELL);
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(stream, "utf-8");
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, "values");
                int N = mUninstalledDelappList.size();
                int i = mThreadNum;
                while (i < N) {
                    if (LocationManagerServiceUtil.GOOGLE_GMS_PROCESS.equals(mUninstalledDelappList.get(i)) || "com.google.android.gsf.login".equals(mUninstalledDelappList.get(i)) || "com.google.android.gsf".equals(mUninstalledDelappList.get(i))) {
                        Slog.d(TAG, "GmsCore no need write to file");
                    } else {
                        out.startTag(null, "string");
                        out.attribute(null, MemoryConstant.MEM_POLICY_ACTIONNAME, (String) mUninstalledDelappList.get(i));
                        out.endTag(null, "string");
                    }
                    i += OPTIMIZE_FOR_OTA;
                }
                out.endTag(null, "values");
                out.endDocument();
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (IOException e4) {
                e = e4;
                fileOutputStream = stream;
                try {
                    Slog.w(TAG, "failed parsing " + file + " " + e);
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e5) {
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e6) {
                        }
                    }
                    throw th;
                }
            } catch (Exception e7) {
                e2 = e7;
                fileOutputStream = stream;
                Slog.w(TAG, "failed parsing " + file + " " + e2);
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e8) {
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = stream;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (IOException e9) {
            e = e9;
            Slog.w(TAG, "failed parsing " + file + " " + e);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (Exception e10) {
            e2 = e10;
            Slog.w(TAG, "failed parsing " + file + " " + e2);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }
}
