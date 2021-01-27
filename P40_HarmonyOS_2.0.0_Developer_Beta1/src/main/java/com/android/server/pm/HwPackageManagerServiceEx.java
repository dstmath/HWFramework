package com.android.server.pm;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ActivityInfoEx;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.HwPackageParser;
import android.content.pm.IHwPackageParser;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParserEx;
import android.content.pm.PackageParserExUtils;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hardware.display.HwFoldScreenState;
import android.hdm.HwDeviceManagerEx;
import android.hwtheme.HwThemeManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBackupSessionCallback;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.securitydiagnose.HwSecurityDiagnoseManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Base64;
import android.util.Flog;
import android.util.HwSlog;
import android.util.IMonitor;
import android.util.Log;
import android.util.PackageUtilsEx;
import android.util.SplitNotificationUtilsEx;
import android.util.Xml;
import android.widget.Toast;
import com.android.server.HwServiceFactoryEx;
import com.android.server.am.HwActivityManagerServiceEx;
import com.android.server.appactcontrol.AppActUtils;
import com.android.server.appactcontrol.AppActUtilsEx;
import com.android.server.appactcontrol.HwAppActController;
import com.android.server.appprotect.AppProtectControlUtil;
import com.android.server.appprotect.AppProtectUtilEx;
import com.android.server.cust.utils.ForbidShellFuncUtilEx;
import com.android.server.gesture.GestureNavConst;
import com.android.server.notch.DefaultHwNotchScreenWhiteConfig;
import com.android.server.pm.BlackListInfoEx;
import com.android.server.pm.CertCompatSettingsEx;
import com.android.server.pm.CompilerStatsEx;
import com.android.server.pm.InstallerEx;
import com.android.server.pm.PackageManagerServiceEx;
import com.android.server.pm.auth.HwCertificationEx;
import com.android.server.pm.auth.HwCertificationManagerEx;
import com.android.server.pm.dex.DexoptOptionsEx;
import com.android.server.pm.dex.PackageDexUsageEx;
import com.android.server.pm.permission.BasePermissionEx;
import com.android.server.pm.permission.PermissionManagerServiceInternalEx;
import com.android.server.pm.permission.PermissionsStateEx;
import com.android.server.security.securityprofile.ISecurityProfileController;
import com.android.server.wm.utils.HwDisplaySizeUtilEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.ActivityManagerNativeEx;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.app.NotificationManagerExt;
import com.huawei.android.app.PendingIntentEx;
import com.huawei.android.app.SynchronousUserSwitchObserverEx;
import com.huawei.android.common.HwFrameworkFactoryEx;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.IIntentReceiverEx;
import com.huawei.android.content.pm.ApplicationInfoExt;
import com.huawei.android.content.pm.HwHepPackageInfo;
import com.huawei.android.content.pm.HwPresetPackage;
import com.huawei.android.content.pm.IExtServiceProviderEx;
import com.huawei.android.content.pm.IPackageInstallObserver2Ex;
import com.huawei.android.content.pm.IPackageManagerEx;
import com.huawei.android.content.pm.PackageInfoEx;
import com.huawei.android.content.pm.PackageManagerExt;
import com.huawei.android.content.pm.PackageParserEx;
import com.huawei.android.content.pm.ParceledListSliceEx;
import com.huawei.android.content.pm.ResolveInfoEx;
import com.huawei.android.content.pm.SignatureEx;
import com.huawei.android.content.pm.UserInfoExt;
import com.huawei.android.content.pm.VersionedPackageEx;
import com.huawei.android.internal.notification.SystemNotificationChannelsEx;
import com.huawei.android.internal.util.FastXmlSerializerEx;
import com.huawei.android.internal.util.UserIconsEx;
import com.huawei.android.internal.util.XmlUtilsEx;
import com.huawei.android.manufacture.ManufactureNativeUtils;
import com.huawei.android.os.BuildEx;
import com.huawei.android.os.EnvironmentEx;
import com.huawei.android.os.FileUtilsEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.UserManagerExt;
import com.huawei.android.os.ZygoteInitEx;
import com.huawei.android.perf.HwOptPackageParserEx;
import com.huawei.android.util.SlogEx;
import com.huawei.android.view.LayoutParamsExt;
import com.huawei.android.widget.ToastEx;
import com.huawei.hsm.permission.StubController;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import com.huawei.libcore.io.IoUtilsEx;
import com.huawei.permission.IHoldService;
import com.huawei.server.HwBasicPlatformFactory;
import com.huawei.server.UiThreadEx;
import com.huawei.server.security.HwServiceSecurityPartsFactoryEx;
import com.huawei.server.sidetouch.DefaultHwDisplaySideRegionConfig;
import com.huawei.utils.HwPartResourceUtils;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Thread;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class HwPackageManagerServiceEx extends DefaultHwPackageManagerServiceExt {
    private static final String ACCESS_SYSTEM_WHITE_LIST = "com.huawei.permission.ACCESS_SYSTEM_WHITE_LIST";
    private static final String ACTION_GET_PACKAGE_INSTALLATION_INFO = "com.huawei.android.action.GET_PACKAGE_INSTALLATION_INFO";
    private static final int ALL_APPS_USE_SIDE_CLOSE = 0;
    private static final String ALL_APPS_USE_SIDE_MODE = "all_apps_use_side_mode";
    private static final int ALL_APPS_USE_SIDE_OPEN = 1;
    private static final String APK_DELETE_FILE = "/cust/ecota/xml/delete_system_app.txt";
    public static final int APP_FORCE_DARK_USER_SET_FLAG = 128;
    public static final int APP_SIDE_MODE_USER_SET_FLAG = 4;
    private static final int APP_USE_SIDE_MODE_DEFAULT = -1;
    public static final int APP_USE_SIDE_MODE_EXPANDED = 1;
    public static final int APP_USE_SIDE_MODE_UNEXPANDED = 0;
    private static final String BROADCAST_PERMISSION = "com.android.permission.system_manager_interface";
    private static final String BUNDLE_PACKAGE = "package";
    private static final String CERT_TYPE_MEDIA = "media";
    private static final String CERT_TYPE_PLATFORM = "platform";
    private static final String CERT_TYPE_SHARED = "shared";
    private static final String CERT_TYPE_TESTKEY = "testkey";
    private static final int COTA_APP_INSTALLING = -1;
    private static final String DEVICE_POLICIES_XML = "device_policies.xml";
    private static final int DEVICE_TYPE_PC = 1;
    public static final int DPERMISSION_DEFAULT = 0;
    public static final int DPERMISSION_DENY = 2;
    public static final int DPERMISSION_GRANT = 1;
    private static final String ECOTA_VERSION = SystemPropertiesEx.get("ro.product.EcotaVersion", BuildConfig.FLAVOR);
    private static final String FEATURE_SUPPORT = "1";
    private static final String FLAG_APKPATCH_PATH = "/patch_hw/apk/apk_patch.xml";
    private static final String FLAG_APKPATCH_PKGNAME = "pkgname";
    private static final String FLAG_APKPATCH_TAG = "android.huawei.SYSTEM_APP_PATCH";
    private static final String FLAG_APKPATCH_TAGPATCH = "apkpatch";
    private static final String FLAG_VALUE = "value";
    private static final String FREE_FORM_LIST = "freeFormList";
    private static final String HW_LAUNCHER = "com.huawei.android.launcher";
    private static final String HW_PMS_GET_PCASSISTANT_RESULT = "com.huawei.permission.GET_PCASSISTANT_RESULT";
    private static final String HW_PMS_SET_APP_PERMISSION = "huawei.android.permission.SET_CANNOT_UNINSTALLED_PERMISSION";
    private static final String HW_PMS_SET_PCASSISTANT_RESULT = "com.huawei.permission.SET_PCASSISTANT_RESULT";
    public static final String HW_PRODUCT_DIR = "/hw_product";
    private static final String HW_SOUND_RECORDER = "com.android.soundrecorder.upgrade";
    private static final String HW_TV_LAUNCHER = "com.huawei.homevision.launcher";
    private static final String INSTALLATION_EXTRA_INSTALLER_PACKAGE_NAME = "installerPackageName";
    private static final String INSTALLATION_EXTRA_PACKAGE_INSTALL_RESULT = "pkgInstallResult";
    private static final String INSTALLATION_EXTRA_PACKAGE_NAME = "pkgName";
    private static final String INSTALLATION_EXTRA_PACKAGE_UPDATE = "pkgUpdate";
    private static final String INSTALLATION_EXTRA_PACKAGE_URI = "pkgUri";
    private static final String INSTALLATION_EXTRA_PACKAGE_VERSION_CODE = "pkgVersionCode";
    private static final String INSTALLATION_EXTRA_PACKAGE_VERSION_NAME = "pkgVersionName";
    private static final int INVALID_VALUE = -1;
    private static final boolean IS_APP_INSTALL_AS_SYS_ALLOW = SystemPropertiesEx.getBoolean("ro.config.romUpgradeDataReserved", true);
    private static final boolean IS_AUTO_INSTALL_ENABLE = SystemPropertiesEx.getBoolean("hw_mc.pms.recovery_preset_app", false);
    private static final boolean IS_DEBUG = "on".equals(SystemPropertiesEx.get("ro.dbg.pms_log", PROP_DEFAULT_VALUE));
    private static final boolean IS_DEVICE_MAPLE_ENABLED = FEATURE_SUPPORT.equals(SystemPropertiesEx.get("ro.maple.enable", PROP_DEFAULT_VALUE));
    private static final boolean IS_HW_MULTIWINDOW_SUPPORTED = SystemPropertiesEx.getBoolean("ro.config.hw_multiwindow_optimization", false);
    private static final boolean IS_MAPLE_ENABLE_READ_ONLY = FEATURE_SUPPORT.equals(SystemPropertiesEx.get("ro.maple.enable", PROP_DEFAULT_VALUE));
    private static final boolean IS_NOTCH_PROP = (!BuildConfig.FLAVOR.equals(SystemPropertiesEx.get("ro.config.hw_notch_size", BuildConfig.FLAVOR)));
    private static final boolean IS_PREF_HW_LAUNCHER = SystemPropertiesEx.getBoolean("ro.config.pref.hw_launcher", true);
    private static final boolean IS_TV = "tv".equals(SystemPropertiesEx.get("ro.build.characteristics", GestureNavConst.DEVICE_TYPE_DEFAULT));
    private static final String KEY_IS_PROTECTED = "is_protected";
    private static final String KEY_PKG_NAME = "pkg_name";
    private static final int LEGAL_RECORD_NUM = 4;
    private static final Set<MySysAppInfo> MDM_SYSTEM_APPS = new HashSet();
    private static final Set<MySysAppInfo> MDM_SYSTEM_UNDETACHABLE_APPS = new HashSet();
    private static final String META_KEY_KEEP_ALIVE = "android.server.pm.KEEP_ALIVE";
    private static final String METHOD_HSM_INSTALL_UNIFIEDPOWERAPPS = "hsm_install_unifiedpowerapps";
    private static final int MSG_DEL_SOUNDRECORDER = 2;
    public static final int MSG_SET_CURRENT_EMUI_SYS_IMG_VERSION = 1;
    private static final int NORMAL_APP_TYPE = 0;
    static final long OTA_WAIT_DEXOPT_TIME = 480000;
    private static final int PHONE2PC_VERSION = 1;
    private static final String POLICY_CHANGED_INTENT_ACTION = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    public static final String PREINSTALLED_APK_LIST_DIR = "/data/system/";
    public static final String PREINSTALLED_APK_LIST_FILE = "preinstalled_app_list_file.xml";
    private static final int PRIVILEGE_APP_TYPE = 1;
    private static final String PROP_DEFAULT_VALUE = "0";
    private static final String REGIONAL_PHONE_SWITCH = SystemPropertiesEx.get("persist.sys.rpforpms", BuildConfig.FLAVOR);
    private static final int SCAN_AS_PRIVILEGED = 262144;
    public static final int SCAN_AS_SYSTEM = 131072;
    private static final int SCAN_BOOTING = 16;
    private static final int SCAN_FIRST_BOOT_OR_UPGRADE = 8192;
    private static final int SCAN_INITIAL = 512;
    private static final List<String> SCAN_INSTALL_CALLER_PACKAGES = new ArrayList(Arrays.asList("com.huawei.android.launcher", "com.huawei.hiassistant", "com.huawei.search", "com.huawei.tips", "android.uid.phone:1001", "com.huawei.appmarket", "com.huawei.gameassistant"));
    private static final String SEPARATOR = ":";
    private static final String SET_ASPECT_RATIO_PERMISSION = "com.huawei.permission.HW_SET_APPLICATION_ASPECT_RATIO";
    private static final String SKIP_TRIGGER_FREEFORM = "com.huawei.permission.SKIP_TRIGGER_FREEFORM";
    public static final String SYSTEM_APP_DIR = "/system/app";
    public static final String SYSTEM_PRIV_APP_DIR = "/system/priv-app";
    private static final String TAG = "HwPackageManagerServiceEx";
    private static final String TAG_SPECIAL_POLICY_SYS_APP_LIST = "update-sys-app-install-list";
    private static final String TAG_SPECIAL_POLICY_UNDETACHABLE_SYS_APP_LIST = "update-sys-app-undetachable-install-list";
    private static final String TERMINATOR = ";";
    private static final String TME_CUSTOMIZE_SWITCH = SystemPropertiesEx.get("persist.sys.mccmnc", BuildConfig.FLAVOR);
    private static final String UTF_8 = "utf-8";
    static final long WAIT_DEXOPT_TIME = 180000;
    private static List<String> sCustStoppedApps = new ArrayList();
    private static boolean sIsAllAppsUseSideModeOpen = true;
    private static List<String> sPreinstalledPackageList = new ArrayList();
    private DefaultHwNotchScreenWhiteConfig hwNotchScreenWhiteConfig;
    private BlackListInfoEx mBlackListInfo;
    private final HandlerThread mCommonThread;
    private CertCompatSettingsEx mCompatSettings;
    final Context mContext;
    private Object mCust;
    private BlackListInfoEx mDisableAppListInfo;
    private Map<String, BlackListInfoEx.BlackListAppEx> mDisableAppMap;
    private ExtServiceProviderEx mExtServiceProvider;
    private final Object mForceStopLock;
    private final Set<String> mGrantedInstalledPkgs;
    final PackageExHandler mHandler;
    private DefaultHwDisplaySideRegionConfig mHwDisplaySideConfig;
    private HwFileBackupManager mHwFileBackupManager;
    private HwHepApplicationManager mHwHepApplicationManager;
    private HwOptPackageParserEx mHwOptPackageParser;
    private Set<String> mIncompatNotifications;
    private final Set<String> mIncompatiblePkgs;
    private boolean mIsBlackListExist;
    private boolean mIsBootCompleted;
    private boolean mIsDexoptNow;
    private boolean mIsFoundCertCompatFile;
    private AtomicBoolean mIsOpting;
    private boolean mIsSideModeForceStopFinish;
    private HwParallelPackageDexOptimizerEx mParallelPackageDexOptimizer;
    PackageManagerServiceEx mPms;
    private final List<String> mScanInstallApkList;
    private ArrayList<String> mShouldNotUpdateByCotaDataApks;
    private final Object mSpeedOptLock;
    private ArraySet<String> mSpeedOptPkgs;
    private Set<String> mUninstallBlackListPkgNames;
    private long mUserSwitchingTime;

    /* access modifiers changed from: package-private */
    public class PackageExHandler extends Handler {
        PackageExHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                doHandleMessage(msg);
            } catch (Exception e) {
                SlogEx.e(HwPackageManagerServiceEx.TAG, "there is error when PackageExHandler do handle message");
            }
        }

        /* access modifiers changed from: package-private */
        public void doHandleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                PackageParserEx.setCurrentEmuiSysImgVersion(HwPackageManagerServiceEx.deriveEmuiSysImgVersion());
            } else if (i == 2) {
                HwPackageManagerServiceEx.this.deleteSoundercorderIfNeed();
            }
        }
    }

    public HwPackageManagerServiceEx() {
        this.mPms = null;
        this.mIncompatiblePkgs = new ArraySet();
        this.mCommonThread = new HandlerThread("PMSCommonThread");
        this.mGrantedInstalledPkgs = new HashSet();
        this.mScanInstallApkList = new ArrayList();
        this.mSpeedOptLock = new Object();
        this.mIncompatNotifications = new ArraySet();
        this.mUninstallBlackListPkgNames = new HashSet();
        this.mHwFileBackupManager = null;
        this.mHwHepApplicationManager = null;
        this.mIsBootCompleted = false;
        this.mIsDexoptNow = false;
        this.mUserSwitchingTime = 0;
        this.mHwOptPackageParser = null;
        this.mIsOpting = new AtomicBoolean(false);
        this.mSpeedOptPkgs = new ArraySet<>();
        this.mBlackListInfo = new BlackListInfoEx();
        this.mDisableAppListInfo = new BlackListInfoEx();
        this.mDisableAppMap = new HashMap();
        this.mIsBlackListExist = false;
        this.mShouldNotUpdateByCotaDataApks = new ArrayList<>();
        this.mCust = null;
        this.mForceStopLock = new Object();
        this.mIsSideModeForceStopFinish = true;
        this.mContext = null;
        this.mHandler = null;
    }

    public HwPackageManagerServiceEx(PackageManagerServiceEx pms, Context context) {
        this.mPms = null;
        this.mIncompatiblePkgs = new ArraySet();
        this.mCommonThread = new HandlerThread("PMSCommonThread");
        this.mGrantedInstalledPkgs = new HashSet();
        this.mScanInstallApkList = new ArrayList();
        this.mSpeedOptLock = new Object();
        this.mIncompatNotifications = new ArraySet();
        this.mUninstallBlackListPkgNames = new HashSet();
        this.mHwFileBackupManager = null;
        this.mHwHepApplicationManager = null;
        this.mIsBootCompleted = false;
        this.mIsDexoptNow = false;
        this.mUserSwitchingTime = 0;
        this.mHwOptPackageParser = null;
        this.mIsOpting = new AtomicBoolean(false);
        this.mSpeedOptPkgs = new ArraySet<>();
        this.mBlackListInfo = new BlackListInfoEx();
        this.mDisableAppListInfo = new BlackListInfoEx();
        this.mDisableAppMap = new HashMap();
        this.mIsBlackListExist = false;
        this.mShouldNotUpdateByCotaDataApks = new ArrayList<>();
        this.mCust = null;
        this.mForceStopLock = new Object();
        this.mIsSideModeForceStopFinish = true;
        this.mContext = context;
        this.mPms = pms;
        HotInstallEx.setPackageManagerInner(pms, context);
        GunstallUtilEx.initPackageManagerInner(pms, context);
        this.mHwOptPackageParser = HwFrameworkFactoryEx.getHwOptPackageParser();
        this.mHwOptPackageParser.getOptPackages();
        if (!SystemPropertiesEx.getBoolean("ro.config.hwpmsthread.disable", false)) {
            this.mCommonThread.start();
            this.mHandler = new PackageExHandler(this.mCommonThread.getLooper());
        } else {
            this.mHandler = null;
        }
        MspesExUtilEx.getInstance(this).initMspesForbidInstallApps();
        MDM_SYSTEM_APPS.clear();
        MDM_SYSTEM_UNDETACHABLE_APPS.clear();
        readSysInfoFromDevicePolicyXml();
        this.hwNotchScreenWhiteConfig = HwBasicPlatformFactory.loadFactory("com.huawei.server.HwPartBasicPlatformServicesFactory").getHwNotchScreenWhiteConfig();
        this.mHwDisplaySideConfig = HwBasicPlatformFactory.loadFactory("com.huawei.server.sidetouch.HwSideTouchPartFactoryImpl").getHwDisplaySideRegionConfigInstance();
        AppActUtilsEx.setPms(this.mPms);
        AppProtectUtilEx.setPms(this.mPms);
    }

    public boolean isPerfOptEnable(String packageName, int optType) {
        return this.mHwOptPackageParser.isPerfOptEnable(packageName, optType);
    }

    public void checkHwCertification(PackageParserEx.PackageEx pkg, boolean isUpdate) {
        if (pkg != null) {
            PackageParserEx.Package extPkg = new PackageParserEx.Package();
            extPkg.setPackageObject(pkg.getPackage());
            HwServiceSecurityPartsFactoryEx.getInstance().getHwAppAuthManager().checkFileProtect(extPkg);
            if (HwCertificationManagerEx.hasFeature()) {
                if (HwCertificationManagerEx.isSupportHwCertification(pkg)) {
                    boolean isUpgrade = this.mPms.isUpgrade();
                    if (isUpdate || !isContainHwCertification(pkg) || isUpgrade) {
                        checkContainHwCert(pkg);
                    }
                } else if (isContainHwCertification(pkg)) {
                    cleanUpHwCert(pkg);
                }
            }
        }
    }

    private void checkContainHwCert(PackageParserEx.PackageEx pkg) {
        HwCertificationManagerEx manager = HwCertificationManagerEx.getInstance();
        if (manager != null && pkg != null && manager.checkHwCertification(pkg)) {
        }
    }

    public boolean getHwCertPermission(boolean isAllowed, PackageParserEx.PackageEx pkg, String perm) {
        if (isAllowed || !HwCertificationManagerEx.hasFeature()) {
            return isAllowed;
        }
        if (!HwCertificationManagerEx.isInitialized()) {
            HwCertificationManagerEx.initialize(this.mContext);
        }
        HwCertificationManagerEx manager = HwCertificationManagerEx.getInstance();
        if (manager == null) {
            return isAllowed;
        }
        return manager.getHwCertificationPermission(isAllowed, pkg, perm);
    }

    private int getHwCertSignatureVersion(PackageParserEx.PackageEx pkg) {
        HwCertificationManagerEx manager = HwCertificationManagerEx.getInstance();
        if (manager == null || pkg == null) {
            return HwCertificationEx.RESULT_INVALID;
        }
        return manager.getHwCertSignatureVersion(pkg.getPackageName());
    }

    private void cleanUpHwCert(PackageParserEx.PackageEx pkg) {
        HwCertificationManagerEx manager = HwCertificationManagerEx.getInstance();
        if (manager != null) {
            manager.cleanUp(pkg);
        }
    }

    public void cleanUpHwCert() {
        if (HwCertificationManagerEx.hasFeature()) {
            if (!HwCertificationManagerEx.isInitialized()) {
                HwCertificationManagerEx.initialize(this.mContext);
            }
            HwCertificationManagerEx manager = HwCertificationManagerEx.getInstance();
            if (manager != null) {
                manager.cleanUp();
            }
        }
    }

    public void initHwCertificationManager() {
        if (!HwCertificationManagerEx.isInitialized()) {
            HwCertificationManagerEx.initialize(this.mContext);
        }
        HwCertificationManagerEx.getInstance();
    }

    public int getHwCertificateType(PackageParserEx.PackageEx pkg) {
        if (pkg == null || pkg.getPackageName() == null) {
            return -1;
        }
        if (!HwCertificationManagerEx.isSupportHwCertification(pkg)) {
            return HwCertificationManagerEx.getInstance().getHwCertificateTypeNotMdm();
        }
        return HwCertificationManagerEx.getInstance().getHwCertificateType(pkg.getPackageName());
    }

    public boolean isContainHwCertification(PackageParserEx.PackageEx pkg) {
        if (pkg == null) {
            return false;
        }
        return HwCertificationManagerEx.getInstance().isContainHwCertification(pkg.getPackageName());
    }

    public boolean isAllowedSetHomeActivityForAntiMal(PackageInfo packageInfo, int userId) {
        HwSecurityDiagnoseManager sdm = HwSecurityDiagnoseManager.getInstance();
        if (sdm == null || packageInfo == null) {
            return true;
        }
        Bundle params = new Bundle();
        params.putString("pkg", packageInfo.packageName);
        params.putInt("src", HwSecurityDiagnoseManager.AntiMalProtectLauncherType.PMS.ordinal());
        if (sdm.getAntimalProtectionPolicy(HwSecurityDiagnoseManager.AntiMalProtectType.LAUNCHER.ordinal(), params) != 1) {
            return true;
        }
        return false;
    }

    public void updateNotchScreenWhite(String packageName, String flag, int versionCode) {
        if (IS_NOTCH_PROP) {
            this.hwNotchScreenWhiteConfig.updateVersionCodeInNoch(packageName, flag, versionCode);
            if ("removed".equals(flag)) {
                this.hwNotchScreenWhiteConfig.removeAppUseNotchMode(packageName);
            }
        }
    }

    public int getAppUseNotchMode(String packageName) {
        if (!IS_NOTCH_PROP || packageName == null) {
            return -1;
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mPms.getPackagesLockObject()) {
                PackageSettingEx pkgSetting = (PackageSettingEx) this.mPms.getSettings().getPackages().get(packageName);
                if (pkgSetting != null) {
                    if (!pkgSetting.isObjNull()) {
                        int appUseNotchMode = pkgSetting.getAppUseNotchMode();
                        Binder.restoreCallingIdentity(callingId);
                        return appUseNotchMode;
                    }
                }
                return -1;
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void setAppUseNotchMode(String packageName, int mode) {
        if (IS_NOTCH_PROP && packageName != null) {
            int uid = Binder.getCallingUid();
            if (UserHandleEx.getAppId(uid) == 1000 || uid == 0) {
                long callingId = Binder.clearCallingIdentity();
                try {
                    synchronized (this.mPms.getPackagesLockObject()) {
                        PackageSettingEx pkgSetting = (PackageSettingEx) this.mPms.getSettings().getPackages().get(packageName);
                        if (pkgSetting != null) {
                            if (!pkgSetting.isObjNull()) {
                                if (pkgSetting.getAppUseNotchMode() != mode) {
                                    pkgSetting.setAppUseNotchMode(mode);
                                    this.mPms.getSettings().writeLPr();
                                    this.hwNotchScreenWhiteConfig.updateAppUseNotchMode(packageName, mode);
                                }
                                Binder.restoreCallingIdentity(callingId);
                            }
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            } else {
                throw new SecurityException("Only the system can set app use notch mode");
            }
        }
    }

    public int getAppUseSideMode(String packageName) {
        if (packageName == null || !HwDisplaySizeUtilEx.hasSideInScreen()) {
            return -1;
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mPms.getPackagesLockObject()) {
                PackageSettingEx pkgSetting = (PackageSettingEx) this.mPms.getSettings().getPackages().get(packageName);
                if (pkgSetting != null) {
                    if (!pkgSetting.isObjNull()) {
                        int appUseSideMode = pkgSetting.getAppUseSideMode() & -5;
                        Binder.restoreCallingIdentity(callingId);
                        return appUseSideMode;
                    }
                }
                return -1;
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void setAppUseSideMode(String packageName, int mode) {
        if (packageName != null && HwDisplaySizeUtilEx.hasSideInScreen()) {
            int uid = Binder.getCallingUid();
            if (UserHandleEx.getAppId(uid) == 1000 || uid == 0) {
                long callingId = Binder.clearCallingIdentity();
                try {
                    synchronized (this.mPms.getPackagesLockObject()) {
                        PackageSettingEx pkgSetting = (PackageSettingEx) this.mPms.getSettings().getPackages().get(packageName);
                        if (pkgSetting != null) {
                            if (!pkgSetting.isObjNull()) {
                                if ((pkgSetting.getAppUseSideMode() & -5) != mode) {
                                    pkgSetting.setAppUseSideMode(mode | 4);
                                    this.mPms.getSettings().writeLPr();
                                    DefaultHwDisplaySideRegionConfig defaultHwDisplaySideRegionConfig = this.mHwDisplaySideConfig;
                                    boolean z = true;
                                    if (mode != 1) {
                                        z = false;
                                    }
                                    defaultHwDisplaySideRegionConfig.updateExtendApp(packageName, z);
                                }
                                Binder.restoreCallingIdentity(callingId);
                            }
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            } else {
                throw new SecurityException("Only the system can set app use side mode");
            }
        }
    }

    public void updateAppsUseSideWhitelist(ArrayMap<String, String> compressApps, ArrayMap<String, String> extendApps) {
        if (HwDisplaySizeUtilEx.hasSideInScreen()) {
            synchronized (this.mPms.getPackagesLockObject()) {
                updateAppsSideMode(compressApps, 0);
                updateAppsSideMode(extendApps, 1);
                this.mPms.getSettings().writeLPr();
            }
        }
    }

    private void updateAppsSideMode(ArrayMap<String, String> apps, int mode) {
        if (apps == null || apps.size() == 0) {
            SlogEx.d(TAG, "apps is null");
            return;
        }
        for (String packageName : apps.keySet()) {
            PackageSettingEx pkgSetting = (PackageSettingEx) this.mPms.getSettings().getPackages().get(packageName);
            if (pkgSetting == null || pkgSetting.isObjNull()) {
                SlogEx.d(TAG, "pkgSetting is null");
            } else {
                boolean isUserSet = (pkgSetting.getAppUseSideMode() & 4) != 0;
                int realMode = pkgSetting.getAppUseSideMode() & -5;
                SlogEx.d(TAG, "packageName: " + packageName + ", isUserSet: " + isUserSet + ", realMode: " + realMode + ", mode: " + mode);
                if (!isUserSet && realMode != mode) {
                    pkgSetting.setAppUseSideMode(mode);
                }
            }
        }
    }

    public List<String> getAppsUseSideList() {
        ArrayMap<String, PackageSettingEx> packageSettings = this.mPms.getSettings().getPackages();
        List<String> pkgNames = new ArrayList<>();
        for (String pkgname : packageSettings.keySet()) {
            if ((packageSettings.get(pkgname).getAppUseSideMode() & -5) == 1) {
                pkgNames.add(pkgname);
            }
        }
        return pkgNames;
    }

    public void updateUseSideMode(String pkgName, PackageSettingEx ps) {
        if (HwDisplaySizeUtilEx.hasSideInScreen()) {
            if (ps == null || ps.isObjNull()) {
                SlogEx.d(TAG, "updateUseSideMode ps is null for pkgName: " + pkgName);
                return;
            }
            String installVersion = null;
            String whiteListVersion = this.mHwDisplaySideConfig.getAppVersionInWhiteList(pkgName);
            PackageInfoEx pkgInfo = this.mPms.getPackageInfo(pkgName, 16384, 0);
            if (pkgInfo != null) {
                installVersion = pkgInfo.getVersionName();
            }
            if (whiteListVersion == null || this.mHwDisplaySideConfig.compareVersion(installVersion, whiteListVersion) < 0) {
                SlogEx.d(TAG, "updateUseSideMode UNEXPANDED pkgName: " + pkgName);
                ps.setAppUseSideMode(0);
                this.mHwDisplaySideConfig.updateExtendApp(pkgName, false);
                return;
            }
            SlogEx.w(TAG, "updateUseSideMode,pkgName: " + pkgName + ",isAllAppsUseSideModeOpen:" + sIsAllAppsUseSideModeOpen);
            ps.setAppUseSideMode(sIsAllAppsUseSideModeOpen ? 1 : 0);
            this.mHwDisplaySideConfig.updateExtendApp(pkgName, sIsAllAppsUseSideModeOpen);
        }
    }

    public boolean setAllAppsUseSideMode(boolean isUse) {
        if (!HwDisplaySizeUtilEx.hasSideInScreen()) {
            return false;
        }
        int uid = Binder.getCallingUid();
        if (UserHandleEx.getAppId(uid) == 1000 || uid == 0) {
            long callingId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mPms.getPackagesLockObject()) {
                    int mode = isUse ? 1 : 0;
                    ArrayMap<String, PackageSettingEx> packageSettings = this.mPms.getSettings().getPackages();
                    for (String pkgName : packageSettings.keySet()) {
                        PackageSettingEx pkgSetting = packageSettings.get(pkgName);
                        if (pkgSetting != null) {
                            if (!pkgSetting.isObjNull()) {
                                if ((pkgSetting.getAppUseSideMode() & -5) != mode) {
                                    pkgSetting.setAppUseSideMode(mode | 4);
                                    this.mHwDisplaySideConfig.updateExtendApp(pkgName, mode == 1);
                                }
                            }
                        }
                    }
                    this.mPms.getSettings().writeLPr();
                }
                return true;
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        } else {
            throw new SecurityException("Only the system can set app use side mode");
        }
    }

    public boolean setAllAppsUseSideModeAndStopApps(List<String> pkgs, boolean isUse) {
        if (pkgs == null || pkgs.isEmpty()) {
            SlogEx.w(TAG, "setAllAppsUseSideModeAndStopApps, pkgs is empty");
            return true;
        } else if (!HwDisplaySizeUtilEx.hasSideInScreen()) {
            return false;
        } else {
            int uid = Binder.getCallingUid();
            if (UserHandleEx.getAppId(uid) == 1000 || uid == 0) {
                if (Settings.Global.putInt(this.mContext.getContentResolver(), ALL_APPS_USE_SIDE_MODE, isUse ? 1 : 0)) {
                    sIsAllAppsUseSideModeOpen = isUse;
                    SlogEx.w(TAG, "setAllAppsUseSideModeAndStopApps, sIsAllAppsUseSideModeOpen=" + sIsAllAppsUseSideModeOpen);
                }
                long callingId = Binder.clearCallingIdentity();
                try {
                    setPackageSetting(pkgs, isUse);
                    synchronized (this.mForceStopLock) {
                        if (this.mIsSideModeForceStopFinish) {
                            this.mIsSideModeForceStopFinish = false;
                            this.mPms.getPackageHandler().post(new Runnable(pkgs) {
                                /* class com.android.server.pm.$$Lambda$HwPackageManagerServiceEx$KqexTX0NhLb_lHdVo1EaM31HG_E */
                                private final /* synthetic */ List f$1;

                                {
                                    this.f$1 = r2;
                                }

                                @Override // java.lang.Runnable
                                public final void run() {
                                    HwPackageManagerServiceEx.this.lambda$setAllAppsUseSideModeAndStopApps$0$HwPackageManagerServiceEx(this.f$1);
                                }
                            });
                        }
                    }
                    return true;
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            } else {
                throw new SecurityException("Only the system can set app use side mode");
            }
        }
    }

    public /* synthetic */ void lambda$setAllAppsUseSideModeAndStopApps$0$HwPackageManagerServiceEx(List pkgs) {
        long stopBeginTime = SystemClock.elapsedRealtime();
        HwActivityManager.forceStopPackages(pkgs, -1);
        synchronized (this.mForceStopLock) {
            this.mIsSideModeForceStopFinish = true;
        }
        SlogEx.w(TAG, "forceStopPackages cost:" + (SystemClock.elapsedRealtime() - stopBeginTime));
    }

    private void setPackageSetting(List<String> pkgs, boolean isUse) {
        synchronized (this.mPms.getPackagesLockObject()) {
            int mode = isUse ? 1 : 0;
            ArrayMap<String, PackageSettingEx> packageSettings = this.mPms.getSettings().getPackages();
            long settingBeginTime = SystemClock.elapsedRealtime();
            for (String pkgName : pkgs) {
                if (pkgName != null) {
                    PackageSettingEx pkgSetting = packageSettings.get(pkgName);
                    if (pkgSetting != null) {
                        if ((pkgSetting.getAppUseSideMode() & -5) != mode) {
                            pkgSetting.setAppUseSideMode(mode | 4);
                            this.mHwDisplaySideConfig.updateExtendApp(pkgName, mode == 1);
                        }
                    }
                }
            }
            this.mPms.getSettings().writeLPr();
            SlogEx.w(TAG, "writeLPr cost:" + (SystemClock.elapsedRealtime() - settingBeginTime));
        }
    }

    public boolean restoreAllAppsUseSideMode() {
        if (!HwDisplaySizeUtilEx.hasSideInScreen()) {
            return false;
        }
        int uid = Binder.getCallingUid();
        if (UserHandleEx.getAppId(uid) == 1000 || uid == 0) {
            long callingId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mPms.getPackagesLockObject()) {
                    ArrayMap<String, PackageSettingEx> packageSettings = this.mPms.getSettings().getPackages();
                    for (String pkgName : packageSettings.keySet()) {
                        PackageSettingEx pkgSetting = packageSettings.get(pkgName);
                        if (pkgSetting != null) {
                            if (this.mHwDisplaySideConfig.isAppInWhiteList(pkgName)) {
                                pkgSetting.setAppUseSideMode(0);
                                this.mHwDisplaySideConfig.updateExtendApp(pkgName, false);
                            } else {
                                pkgSetting.setAppUseSideMode(1);
                                this.mHwDisplaySideConfig.updateExtendApp(pkgName, true);
                            }
                        }
                    }
                    this.mPms.getSettings().writeLPr();
                }
                return true;
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        } else {
            throw new SecurityException("Only the system can restore app use side mode");
        }
    }

    public boolean isAllAppsUseSideMode(List<String> packages) {
        if (packages == null || !HwDisplaySizeUtilEx.hasSideInScreen()) {
            return false;
        }
        int uid = Binder.getCallingUid();
        if (UserHandleEx.getAppId(uid) == 1000 || uid == 0) {
            long callingId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mPms.getPackagesLockObject()) {
                    for (String pkgName : packages) {
                        PackageSettingEx pkgSetting = (PackageSettingEx) this.mPms.getSettings().getPackages().get(pkgName);
                        if (pkgSetting == null) {
                            Binder.restoreCallingIdentity(callingId);
                            return false;
                        } else if ((pkgSetting.getAppUseSideMode() & -5) == 0) {
                            return false;
                        }
                    }
                    Binder.restoreCallingIdentity(callingId);
                    return true;
                }
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        } else {
            throw new SecurityException("Only the system can read app use side mode");
        }
    }

    private void listenForUserSwitches() {
        try {
            ActivityManagerEx.registerUserSwitchObserver(new SynchronousUserSwitchObserverEx() {
                /* class com.android.server.pm.HwPackageManagerServiceEx.AnonymousClass1 */

                public void onUserSwitching(int newUserId) throws RemoteException {
                    synchronized (HwPackageManagerServiceEx.this.mSpeedOptLock) {
                        HwPackageManagerServiceEx.this.mUserSwitchingTime = SystemClock.elapsedRealtime();
                        SlogEx.d(HwPackageManagerServiceEx.TAG, "onUserSwitching " + HwPackageManagerServiceEx.this.mUserSwitchingTime);
                    }
                }
            }, TAG);
        } catch (RemoteException e) {
            SlogEx.e(TAG, "Failed to listen for user switching event");
        }
    }

    public boolean isApkDexOpt(String targetCompilerFilter) {
        return "speed-profile-opt".equals(targetCompilerFilter);
    }

    public boolean hwPerformDexOptMode(String packageName, boolean isCheckProfiles, boolean isForce, boolean isBootCompleted, String splitName) {
        boolean isSuccess;
        synchronized (this.mSpeedOptLock) {
            if (!this.mIsBootCompleted) {
                this.mIsBootCompleted = FEATURE_SUPPORT.equals(SystemPropertiesEx.get("sys.boot_completed", PROP_DEFAULT_VALUE));
            }
            long elapsedTime = SystemClock.elapsedRealtime();
            if (!this.mIsDexoptNow) {
                this.mIsDexoptNow = elapsedTime > (this.mPms.isUpgrade() ? OTA_WAIT_DEXOPT_TIME : 180000);
            }
            if (this.mUserSwitchingTime != 0 && this.mIsDexoptNow) {
                this.mIsDexoptNow = false;
                if (elapsedTime > this.mUserSwitchingTime) {
                    this.mIsDexoptNow = elapsedTime - this.mUserSwitchingTime > WAIT_DEXOPT_TIME;
                }
            }
            SlogEx.i(TAG, "now " + elapsedTime + " optNow " + this.mIsDexoptNow + " upgrade " + this.mPms.isUpgrade() + " BootCompleted " + this.mIsBootCompleted + " UserSwitching " + this.mUserSwitchingTime);
            if (!this.mIsOpting.get() && this.mIsDexoptNow) {
                if (!this.mIsBootCompleted) {
                }
            }
            this.mSpeedOptPkgs.add(packageName);
            SlogEx.d(TAG, "performDexOptMode add list " + packageName + " size " + this.mSpeedOptPkgs.size());
            return true;
        }
        this.mIsOpting.set(true);
        String pkgName = packageName;
        while (true) {
            isSuccess = this.mPms.performDexOptMode(pkgName, isCheckProfiles, "speed-profile", isForce, isBootCompleted, splitName);
            synchronized (this.mSpeedOptLock) {
                if (this.mSpeedOptPkgs.isEmpty()) {
                    break;
                } else if (!this.mIsDexoptNow) {
                    break;
                } else {
                    pkgName = this.mSpeedOptPkgs.valueAt(0);
                    this.mSpeedOptPkgs.removeAt(0);
                }
            }
        }
        this.mIsOpting.set(false);
        return isSuccess;
    }

    public void setAppCanUninstall(String packageName, boolean isCanUninstall) {
        if (packageName != null) {
            this.mContext.enforceCallingPermission(HW_PMS_SET_APP_PERMISSION, "setAppCanUninstall");
            String callingName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
            if (callingName != null && callingName.equalsIgnoreCase(packageName)) {
                if (isCanUninstall) {
                    this.mUninstallBlackListPkgNames.remove(packageName);
                } else {
                    this.mUninstallBlackListPkgNames.add(packageName);
                }
            }
        }
    }

    public boolean isAllowUninstallApp(String packageName) {
        if (packageName != null && !this.mUninstallBlackListPkgNames.contains(packageName) && !MspesExUtilEx.getInstance(this).isForbidMspesUninstall(packageName)) {
            return true;
        }
        return false;
    }

    private boolean isUserRestricted(int userId, String restrictionKey) {
        if (!UserManagerExt.get(this.mContext).getUserRestrictions(UserHandleEx.of(userId)).getBoolean(restrictionKey, false)) {
            return false;
        }
        SlogEx.w(TAG, "User is restricted: " + restrictionKey);
        return true;
    }

    private int redirectInstallForClone(int userId) {
        if (userId == 0 || !HwActivityManagerServiceEx.IS_SUPPORT_CLONE_APP) {
            return userId;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            UserInfoExt ui = PackageManagerServiceEx.getUserManager().getUserInfo(userId);
            if (ui != null && ui.isClonedProfile()) {
                return ui.getProfileGroupId();
            }
            Binder.restoreCallingIdentity(ident);
            return userId;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void installPackageAsUser(String originPath, IPackageInstallObserver2Ex observer, int insFlags, String installerPackageName, int uid) {
        int installFlags;
        if (originPath != null) {
            int userId = redirectInstallForClone(uid);
            this.mContext.enforceCallingOrSelfPermission("android.permission.INSTALL_PACKAGES", null);
            int callingUid = Binder.getCallingUid();
            this.mPms.getPermissionManager().enforceCrossUserPermission(callingUid, userId, true, true, "installPackageAsUser");
            if (isUserRestricted(userId, "no_install_apps")) {
                if (observer != null) {
                    try {
                        observer.onPackageInstalled(BuildConfig.FLAVOR, -111, (String) null, (Bundle) null);
                    } catch (RemoteException e) {
                        SlogEx.e(TAG, "installPackageAsUser onPackageInstalled failed");
                    }
                }
            } else if (!HwDeviceManagerEx.disallowOp(6)) {
                if (callingUid == 2000 || callingUid == 0) {
                    installFlags = insFlags | 32;
                } else {
                    installFlags = insFlags & -33 & -65;
                }
                if ((installFlags & 256) == 0 || this.mContext.checkCallingOrSelfPermission("android.permission.INSTALL_GRANT_RUNTIME_PERMISSIONS") != -1) {
                    PackageManagerServiceEx.InstallParamsEx params = getInstallParams(originPath, observer, installFlags, installerPackageName, userId, callingUid);
                    Message msg = this.mPms.getPackageHandler().obtainMessage(PackageManagerServiceEx.INIT_COPY);
                    msg.obj = params;
                    this.mPms.getPackageHandler().sendMessage(msg);
                    return;
                }
                throw new SecurityException("You need the android.permission.INSTALL_GRANT_RUNTIME_PERMISSIONS permission to use the PackageManager.INSTALL_GRANT_RUNTIME_PERMISSIONS flag");
            }
        }
    }

    private PackageManagerServiceEx.InstallParamsEx getInstallParams(String originPath, IPackageInstallObserver2Ex observer, int installFlags, String installerPackageName, int userId, int callingUid) {
        UserHandleEx user;
        if ((installFlags & 64) != 0) {
            UserHandleEx user2 = new UserHandleEx();
            user2.setUserHandle(UserHandleEx.ALL);
            user = user2;
        } else {
            user = new UserHandleEx(userId);
        }
        return this.mPms.createInstallParams(PackageManagerServiceEx.OriginInfoEx.fromUntrustedFile(new File(originPath)), (PackageManagerServiceEx.MoveInfoEx) null, observer, installFlags, installerPackageName, (String) null, new PackageManagerServiceEx.VerificationInfoEx((Uri) null, (Uri) null, -1, callingUid), user, (String) null, (String[]) null, PackageParserEx.SigningDetailsEx.unknown(), 0);
    }

    public boolean isPrivilegedPreApp(File scanFile) {
        if (scanFile == null) {
            return false;
        }
        return HwPreAppManager.getInstance(this).isPrivilegedPreApp(scanFile);
    }

    public boolean isSystemPreApp(File scanFile) {
        if (scanFile == null) {
            return false;
        }
        return HwPreAppManager.getInstance(this).isSystemPreApp(scanFile);
    }

    public void readPersistentConfig() {
        HwPersistentAppManager.readPersistentConfig();
    }

    public void resolvePersistentFlagForPackage(int oldFlags, PackageParserEx.PackageEx pkg) {
        HwPersistentAppManager.resolvePersistentFlagForPackage(oldFlags, pkg);
    }

    public boolean isPersistentUpdatable(PackageParserEx.PackageEx pkg) {
        return HwPersistentAppManager.isPersistentUpdatable(pkg);
    }

    public void systemReady() {
        checkAndEnableWebview();
        CertCompatSettingsEx certCompatSettingsEx = this.mCompatSettings;
        if (certCompatSettingsEx != null) {
            certCompatSettingsEx.systemReady();
        }
        if (HwCertificationManagerEx.hasFeature()) {
            if (!HwCertificationManagerEx.isInitialized()) {
                HwCertificationManagerEx.initialize(this.mContext);
            }
            HwCertificationManagerEx.getInstance().systemReady();
        }
        try {
            initPackageBlackList();
        } catch (Exception e) {
            SlogEx.e(TAG, "initBlackList failed");
        }
        setCurrentEmuiSysImgVersion();
        listenForUserSwitches();
        deleteSoundrecorder();
        boolean z = true;
        if (SystemPropertiesEx.getBoolean("ro.config.hw_mg_copyright", true)) {
            HwThemeInstaller.getInstance(this.mContext).createMagazineFolder();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(POLICY_CHANGED_INTENT_ACTION);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.pm.HwPackageManagerServiceEx.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                Bundle bundle;
                String policyName = intent.getStringExtra("policy_name");
                SlogEx.d(HwPackageManagerServiceEx.TAG, "devicepolicy.action.POLICY_CHANGED policyName:" + policyName);
                if ((HwPackageManagerServiceEx.TAG_SPECIAL_POLICY_UNDETACHABLE_SYS_APP_LIST.equals(policyName) || HwPackageManagerServiceEx.TAG_SPECIAL_POLICY_SYS_APP_LIST.equals(policyName)) && (bundle = intent.getExtras()) != null) {
                    String value = bundle.getString(HwPackageManagerServiceEx.FLAG_VALUE);
                    SlogEx.d(HwPackageManagerServiceEx.TAG, "value:" + value);
                    HwPackageManagerServiceEx.this.updateSysAppInfoList(value, policyName);
                }
            }
        }, intentFilter, "android.permission.MANAGE_PROFILE_AND_DEVICE_OWNERS", null);
        new Handler().postDelayed(new Runnable() {
            /* class com.android.server.pm.HwPackageManagerServiceEx.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                new HwPileApplicationManager(HwPackageManagerServiceEx.this.mContext).startInstallPileApk();
            }
        }, 1000);
        Thread ecotaNormalInstallThread = new Thread("ecotaNormalInstallThread") {
            /* class com.android.server.pm.HwPackageManagerServiceEx.AnonymousClass4 */

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                new HwEcotaCopyInstallManager(HwPackageManagerServiceEx.this.mContext).startEcotaCopyInstall();
            }
        };
        ecotaNormalInstallThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            /* class com.android.server.pm.HwPackageManagerServiceEx.AnonymousClass5 */

            @Override // java.lang.Thread.UncaughtExceptionHandler
            public void uncaughtException(Thread t, Throwable e) {
                SlogEx.e(HwPackageManagerServiceEx.TAG, t.getName() + " : " + e.getMessage());
            }
        });
        ecotaNormalInstallThread.start();
        if (Settings.Global.getInt(this.mContext.getContentResolver(), ALL_APPS_USE_SIDE_MODE, 1) != 1) {
            z = false;
        }
        sIsAllAppsUseSideModeOpen = z;
        SlogEx.w(TAG, "isAllAppsUseSideModeOpen:" + sIsAllAppsUseSideModeOpen);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSysAppInfoList(String currentValue, String policyName) {
        SlogEx.d(TAG, "updateSysAppInfoList currentValue:" + currentValue);
        if (!TextUtils.isEmpty(currentValue) && !TextUtils.isEmpty(policyName)) {
            if (TAG_SPECIAL_POLICY_UNDETACHABLE_SYS_APP_LIST.equals(policyName)) {
                MDM_SYSTEM_UNDETACHABLE_APPS.clear();
                for (String sysPkg : currentValue.split(TERMINATOR)) {
                    String[] sysPkgInfoStrings = sysPkg.split(SEPARATOR);
                    if (sysPkgInfoStrings.length == 4) {
                        MDM_SYSTEM_UNDETACHABLE_APPS.add(new MySysAppInfo(sysPkgInfoStrings[0], sysPkgInfoStrings[1], sysPkgInfoStrings[2], sysPkgInfoStrings[3]));
                    } else {
                        return;
                    }
                }
            } else if (TAG_SPECIAL_POLICY_SYS_APP_LIST.equals(policyName)) {
                MDM_SYSTEM_APPS.clear();
                for (String sysPkg2 : currentValue.split(TERMINATOR)) {
                    String[] sysPkgInfoStrings2 = sysPkg2.split(SEPARATOR);
                    if (sysPkgInfoStrings2.length == 4) {
                        MDM_SYSTEM_APPS.add(new MySysAppInfo(sysPkgInfoStrings2[0], sysPkgInfoStrings2[1], sysPkgInfoStrings2[2], sysPkgInfoStrings2[3]));
                    } else {
                        return;
                    }
                }
            }
        }
    }

    public int adjustScanFlagForApk(PackageParserEx.PackageEx pkg, int scanFlags) {
        if (pkg == null) {
            return 0;
        }
        int result = scanFlags;
        if (IS_APP_INSTALL_AS_SYS_ALLOW && isValidPackage(pkg)) {
            MySysAppInfo target = getRecordFromCache(MDM_SYSTEM_APPS, pkg.getPackageName(), sha256(PackageParserExUtils.getSignatures(pkg.getSigningDetailsEx())[0].toByteArray()));
            if (target == null) {
                target = getRecordFromCache(MDM_SYSTEM_UNDETACHABLE_APPS, pkg.getPackageName(), sha256(PackageParserExUtils.getSignatures(pkg.getSigningDetailsEx())[0].toByteArray()));
            }
            if (target != null) {
                if (target.getPrivileged()) {
                    SlogEx.d(TAG, "add privileged for " + pkg.getPackageName());
                    result = PackageManagerServiceEx.SCAN_AS_PRIVILEGED | result | PackageManagerServiceEx.SCAN_AS_SYSTEM;
                } else {
                    SlogEx.d(TAG, "add sytem for " + pkg.getPackageName());
                    result = PackageManagerServiceEx.SCAN_AS_SYSTEM | result;
                }
                if (!target.getUndetachable()) {
                    SlogEx.d(TAG, "add del for " + pkg.getPackageName());
                    ApplicationInfoExt.orHwFlags(pkg.getApplicationInfo(), 33554432);
                }
            }
        }
        return result;
    }

    public boolean isSystemAppGrantByMdm(PackageParserEx.PackageEx pkg) {
        if (IS_APP_INSTALL_AS_SYS_ALLOW && isValidPackage(pkg)) {
            MySysAppInfo target = getRecordFromCache(MDM_SYSTEM_APPS, pkg.getPackageName(), sha256(PackageParserExUtils.getSignatures(pkg.getSigningDetailsEx())[0].toByteArray()));
            if (target == null) {
                target = getRecordFromCache(MDM_SYSTEM_UNDETACHABLE_APPS, pkg.getPackageName(), sha256(PackageParserExUtils.getSignatures(pkg.getSigningDetailsEx())[0].toByteArray()));
            }
            if (target != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isSystemAppGrantByMdm(String pkgName) {
        if (!IS_APP_INSTALL_AS_SYS_ALLOW || TextUtils.isEmpty(pkgName)) {
            return false;
        }
        MySysAppInfo target = getRecordFromCache(MDM_SYSTEM_APPS, pkgName, null);
        if (target == null) {
            target = getRecordFromCache(MDM_SYSTEM_UNDETACHABLE_APPS, pkgName, null);
        }
        if (target != null) {
            return true;
        }
        return false;
    }

    public void updateDozeList(String packageName, boolean isProtect) {
        if (!TextUtils.isEmpty(packageName)) {
            Bundle bundle = new Bundle();
            bundle.putString(KEY_PKG_NAME, packageName);
            bundle.putInt(KEY_IS_PROTECTED, isProtect ? 1 : 0);
            try {
                IHoldService service = StubController.getHoldService();
                if (service == null) {
                    SlogEx.e(TAG, "hsm_install_unifiedpowerapps service is null!");
                } else {
                    service.callHsmService(METHOD_HSM_INSTALL_UNIFIEDPOWERAPPS, bundle);
                }
            } catch (RemoteException e) {
                SlogEx.e(TAG, "unable to reach HSM service");
            } catch (Exception e2) {
                SlogEx.e(TAG, "failed to call HSM service ");
            }
        }
    }

    private boolean isValidPackage(PackageParserEx.PackageEx pkg) {
        if ((MDM_SYSTEM_APPS.isEmpty() && MDM_SYSTEM_UNDETACHABLE_APPS.isEmpty()) || pkg == null || TextUtils.isEmpty(pkg.getPackageName()) || pkg.getSigningDetailsEx() == null || PackageParserExUtils.getSignatures(pkg.getSigningDetailsEx()) == null || PackageParserExUtils.getSignatures(pkg.getSigningDetailsEx()).length < 1 || PackageParserExUtils.getSignatures(pkg.getSigningDetailsEx())[0] == null) {
            return false;
        }
        return true;
    }

    private String sha256(byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
            mDigest.update(data);
            return bytesToString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private String bytesToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] chars = new char[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int byteValue = bytes[j] & 255;
            chars[j * 2] = hexChars[byteValue >>> 4];
            chars[(j * 2) + 1] = hexChars[byteValue & 15];
        }
        return new String(chars).toUpperCase(Locale.ENGLISH);
    }

    private MySysAppInfo getRecordFromCache(Set<MySysAppInfo> cacheApps, String pkgName, String pkgSignature) {
        if (cacheApps == null || cacheApps.isEmpty() || pkgName == null) {
            return null;
        }
        for (MySysAppInfo app : cacheApps) {
            if (pkgName.equalsIgnoreCase(app.getPkgName()) && (pkgSignature == null || pkgSignature.equalsIgnoreCase(app.getPkgSignature()))) {
                return app;
            }
        }
        return null;
    }

    private void readSysInfoFromDevicePolicyXml() {
        XmlPullParser parser;
        int type;
        FileInputStream inputStr = null;
        File sysPackageFile = new File(EnvironmentEx.getDataSystemDirectory(), DEVICE_POLICIES_XML);
        if (sysPackageFile.exists()) {
            try {
                FileInputStream inputStr2 = new FileInputStream(sysPackageFile);
                parser = Xml.newPullParser();
                parser.setInput(inputStr2, null);
                if (type != 2) {
                    SlogEx.e(TAG, "No start tag found in package file");
                    try {
                        inputStr2.close();
                        return;
                    } catch (IOException e) {
                        SlogEx.e(TAG, "Unable to close the inputStr");
                        return;
                    }
                } else {
                    int outerDepth = parser.getDepth();
                    while (true) {
                        int type2 = parser.next();
                        if (type2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                            try {
                                inputStr2.close();
                                return;
                            } catch (IOException e2) {
                                SlogEx.e(TAG, "Unable to close the inputStr");
                                return;
                            }
                        } else if (!(type2 == 3 || type2 == 4)) {
                            parsePolicyFile(parser);
                        }
                    }
                }
            } catch (FileNotFoundException e3) {
                SlogEx.e(TAG, "Unable to read device_policies");
                if (0 != 0) {
                    inputStr.close();
                    return;
                }
                return;
            } catch (XmlPullParserException e4) {
                SlogEx.e(TAG, "XmlPullParserException when try to parse device_policies");
                if (0 != 0) {
                    inputStr.close();
                    return;
                }
                return;
            } catch (IOException e5) {
                SlogEx.e(TAG, "IOException when try to parse device_policies");
                if (0 != 0) {
                    inputStr.close();
                    return;
                }
                return;
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        inputStr.close();
                    } catch (IOException e6) {
                        SlogEx.e(TAG, "Unable to close the inputStr");
                    }
                }
                throw th;
            }
            while (true) {
                type = parser.next();
                if (type == 2 || type == 1) {
                    break;
                }
            }
        }
    }

    private void parsePolicyFile(XmlPullParser parser) {
        String tagName = parser.getName();
        char c = 2;
        int i = 4;
        char c2 = 1;
        char c3 = 0;
        if (TAG_SPECIAL_POLICY_SYS_APP_LIST.equals(tagName)) {
            String pkgInfo = XmlUtilsEx.readStringAttribute(parser, FLAG_VALUE);
            if (!TextUtils.isEmpty(pkgInfo)) {
                String[] split = pkgInfo.split(TERMINATOR);
                int length = split.length;
                int i2 = 0;
                while (i2 < length) {
                    String[] sysPkgInfoStrings = split[i2].split(SEPARATOR);
                    if (sysPkgInfoStrings.length == 4) {
                        MDM_SYSTEM_APPS.add(new MySysAppInfo(sysPkgInfoStrings[0].intern(), sysPkgInfoStrings[c2], sysPkgInfoStrings[c], sysPkgInfoStrings[3]));
                    }
                    i2++;
                    c = 2;
                    c2 = 1;
                }
                SlogEx.d(TAG, "read " + pkgInfo);
            }
        } else if (TAG_SPECIAL_POLICY_UNDETACHABLE_SYS_APP_LIST.equals(tagName)) {
            String pkgInfo2 = XmlUtilsEx.readStringAttribute(parser, FLAG_VALUE);
            if (!TextUtils.isEmpty(pkgInfo2)) {
                String[] split2 = pkgInfo2.split(TERMINATOR);
                int length2 = split2.length;
                int i3 = 0;
                while (i3 < length2) {
                    String[] sysPkgInfoStrings2 = split2[i3].split(SEPARATOR);
                    if (sysPkgInfoStrings2.length == i) {
                        MDM_SYSTEM_UNDETACHABLE_APPS.add(new MySysAppInfo(sysPkgInfoStrings2[c3].intern(), sysPkgInfoStrings2[1], sysPkgInfoStrings2[2], sysPkgInfoStrings2[3]));
                    }
                    i3++;
                    i = 4;
                    c3 = 0;
                }
                SlogEx.d(TAG, "read " + pkgInfo2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasOtaUpdate() {
        try {
            UserInfoExt userInfo = PackageManagerServiceEx.getUserManager().getUserInfo(0);
            if (userInfo == null) {
                return false;
            }
            Log.i(TAG, "userInfo.lastLoggedInFingerprint : " + userInfo.getLastLoggedInFingerprint() + ", Build.FINGERPRINT : " + Build.FINGERPRINT + "userInfo.lastLoggedInFingerprintEx : " + userInfo.getLastLoggedInFingerprintEx() + ", Build.FINGERPRINTEX : " + BuildEx.FINGERPRINTEX);
            if (!Objects.equals(userInfo.getLastLoggedInFingerprint(), Build.FINGERPRINT) || !Objects.equals(userInfo.getLastLoggedInFingerprintEx(), BuildEx.FINGERPRINTEX)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "hasOtaUpdate catch Exception");
            return false;
        }
    }

    public boolean isMDMDisallowedInstallPackage(PackageParserEx.PackageEx pkg, PackageManagerServiceEx.PackageInstalledInfoEx res) {
        if (pkg == null) {
            return false;
        }
        if (!ApplicationInfoExt.isSystemApp(pkg.getApplicationInfo()) && HwDeviceManagerEx.disallowOp(7, pkg.getPackageName())) {
            UiThreadEx.getHandler().post(new Runnable() {
                /* class com.android.server.pm.HwPackageManagerServiceEx.AnonymousClass6 */

                @Override // java.lang.Runnable
                public void run() {
                    Toast toast = Toast.makeText(HwPackageManagerServiceEx.this.mContext, HwPackageManagerServiceEx.this.mContext.getString(HwPartResourceUtils.getResourceId("mdm_toast_prohibit_install_app")), 0);
                    ToastEx.setWindowParamsPrivateFlags(toast, LayoutParamsExt.getOrPrivateFlags(ToastEx.getWindowParams(toast), (int) HwPackageManagerServiceEx.SCAN_BOOTING));
                    toast.show();
                }
            });
            if (res != null) {
                res.setError(-110, "app is not in the installpackage_whitelist");
            }
            return true;
        } else if (ApplicationInfoExt.isSystemApp(pkg.getApplicationInfo()) || !HwDeviceManagerEx.disallowOp(19, pkg.getPackageName())) {
            return false;
        } else {
            final String pkgName = getApplicationLabel(this.mContext, pkg);
            new Handler().postDelayed(new Runnable() {
                /* class com.android.server.pm.HwPackageManagerServiceEx.AnonymousClass7 */

                @Override // java.lang.Runnable
                public void run() {
                    Toast.makeText(HwPackageManagerServiceEx.this.mContext, HwPackageManagerServiceEx.this.mContext.getResources().getString(33685933, pkgName), 0).show();
                }
            }, 500);
            if (res != null) {
                res.setError(-110, "app is in the installpackage_blacklist");
            }
            return true;
        }
    }

    public static String getApplicationLabel(Context context, PackageParserEx.PackageEx pkg) {
        if (context == null || pkg == null) {
            return BuildConfig.FLAVOR;
        }
        PackageManager pm = context.getPackageManager();
        String displayName = pkg.getPackageName();
        if (pm != null) {
            return String.valueOf(pm.getApplicationLabel(pkg.getApplicationInfo()));
        }
        return displayName;
    }

    public ResolveInfoEx hwFindPreferredActivityEx(Intent intent, List<ResolveInfoEx> query) {
        if (intent == null || query == null) {
            return null;
        }
        boolean isQueryValid = query.size() > 1;
        if (intent.hasCategory("android.intent.category.HOME") && isQueryValid) {
            return getLauncherResolveInfo(query);
        }
        boolean isActinDial = intent.getAction() != null && "android.intent.action.DIAL".equals(intent.getAction());
        boolean isDataSchemeTel = (intent.getData() == null || intent.getData().getScheme() == null || !"tel".equals(intent.getData().getScheme())) ? false : true;
        if (isActinDial && isDataSchemeTel && isQueryValid) {
            return getSpecificPreferredActivity(query, true, HwThemeInstaller.HWT_OLD_CONTACT);
        }
        boolean isActionView = intent.getAction() != null && "android.intent.action.VIEW".equals(intent.getAction());
        boolean isIntentDataFile = (intent.getData() == null || intent.getData().getScheme() == null || (!"file".equals(intent.getData().getScheme()) && !"content".equals(intent.getData().getScheme()))) ? false : true;
        boolean isIntentTypeImage = intent.getType() != null && intent.getType().startsWith("image/");
        if (isActionView && isIntentDataFile && isIntentTypeImage && isQueryValid) {
            return getImageResolveInfo(query);
        }
        boolean isIntentTypeAudio = intent.getType() != null && intent.getType().startsWith("audio/");
        if (isActionView && isQueryValid && isIntentDataFile && isIntentTypeAudio) {
            return getAudioResolveInfo(query);
        }
        if ((intent.getAction() != null && "android.media.action.IMAGE_CAPTURE".equals(intent.getAction())) && isQueryValid) {
            return getSpecificPreferredActivity(query, false, "com.huawei.camera");
        }
        boolean isIntentDataMail = (intent.getData() == null || intent.getData().getScheme() == null || !"mailto".equals(intent.getData().getScheme())) ? false : true;
        if (isActionView && isQueryValid && isIntentDataMail) {
            return getMailResolveInfo(intent, query);
        }
        boolean isDefaultPreferredActivityChanged = this.mPms.getIsDefaultPreferredActivityChangedInner();
        boolean isDefaultGoogleCalendar = this.mPms.getIsDefaultGoogleCalendarInner();
        StringBuilder sb = new StringBuilder();
        sb.append("!isDefaultPreferredActivityChanged = ");
        sb.append(!isDefaultPreferredActivityChanged);
        sb.append(" ,isDefaultGoogleCalendar = ");
        sb.append(isDefaultGoogleCalendar);
        Log.i(TAG, sb.toString());
        if (!isDefaultPreferredActivityChanged && isDefaultGoogleCalendar && isCalendarType(intent) && isQueryValid) {
            return getSpecificPreferredActivity(query, true, "com.google.android.calendar");
        }
        boolean isIntentDataMarket = intent.getData() != null && intent.getData().toString().startsWith("market://details") && intent.getData().getScheme() != null && "market".equals(intent.getData().getScheme());
        if (!isActionView || !isQueryValid || !isIntentDataMarket) {
            return null;
        }
        return getMarketResolveInfo(query);
    }

    private ResolveInfoEx getLauncherResolveInfo(List<ResolveInfoEx> query) {
        if (!isInWhiteListLaunchers(query)) {
            SlogEx.i(TAG, "not in whiteList launchers, return null");
            return null;
        }
        int index = query.size() - 1;
        HwCustPackageManagerServiceEx custPackageManagerService = this.mPms.getHwPMSCustPackageManagerService();
        while (index >= 0) {
            int index2 = index - 1;
            ResolveInfoEx info = query.get(index);
            String defaultLauncher = IS_TV ? HW_TV_LAUNCHER : "com.huawei.android.launcher";
            if (!(custPackageManagerService == null || info.getActivityInfo() == null)) {
                String custDefaultLauncher = custPackageManagerService.getCustDefaultLauncher(this.mContext, info.getActivityInfo().getApplicationInfo().packageName);
                if (!TextUtils.isEmpty(custDefaultLauncher)) {
                    defaultLauncher = custDefaultLauncher;
                }
            }
            if (info.getActivityInfo() == null || info.getActivityInfo().getApplicationInfo() == null || !defaultLauncher.equals(info.getActivityInfo().getApplicationInfo().packageName)) {
                index = index2;
            } else {
                HwSlog.v(TAG, "Returning system default Launcher ");
                return info;
            }
        }
        return null;
    }

    private boolean isInWhiteListLaunchers(List<ResolveInfoEx> query) {
        if (IS_PREF_HW_LAUNCHER) {
            return true;
        }
        List<String> whiteListLaunchers = new ArrayList<>(3);
        whiteListLaunchers.add("com.huawei.android.launcher");
        whiteListLaunchers.add("com.huawei.kidsmode");
        whiteListLaunchers.add("com.android.settings");
        int num = query.size() - 1;
        while (num >= 0) {
            int num2 = num - 1;
            ResolveInfoEx info = query.get(num);
            if (info.getActivityInfo() == null || whiteListLaunchers.contains(info.getActivityInfo().getApplicationInfo().packageName)) {
                num = num2;
            } else {
                HwSlog.v(TAG, "return default Launcher null");
                return false;
            }
        }
        return true;
    }

    private ResolveInfoEx getImageResolveInfo(List<ResolveInfoEx> query) {
        ResolveInfoEx info = getSpecificPreferredActivity(query, false, "com.android.gallery3d");
        if (info == null) {
            return getSpecificPreferredActivity(query, false, "com.huawei.photos");
        }
        return info;
    }

    private ResolveInfoEx getAudioResolveInfo(List<ResolveInfoEx> query) {
        ResolveInfoEx info = getSpecificPreferredActivity(query, false, "com.android.mediacenter");
        if (info == null) {
            return getSpecificPreferredActivity(query, false, "com.huawei.music");
        }
        return info;
    }

    private ResolveInfoEx getMailResolveInfo(Intent intent, List<ResolveInfoEx> query) {
        if (intent == null) {
            return null;
        }
        if (intent.getCategories() != null && intent.getCategories().size() > 0) {
            return null;
        }
        ResolveInfoEx info = getSpecificPreferredActivity(query, false, "com.android.email");
        if (info == null) {
            return getSpecificPreferredActivity(query, false, "com.huawei.email");
        }
        return info;
    }

    private ResolveInfoEx getMarketResolveInfo(List<ResolveInfoEx> query) {
        String defaultAppMarket = Settings.Global.getString(this.mContext.getContentResolver(), "default_appmarket");
        Log.i(TAG, "find default appmarket is : " + defaultAppMarket);
        return getSpecificPreferredActivity(query, false, defaultAppMarket);
    }

    private ResolveInfoEx getSpecificPreferredActivity(List<ResolveInfoEx> query, boolean isCheckPriorityFlag, String specificPackageName) {
        if (query == null || specificPackageName == null) {
            return null;
        }
        int index = query.size() - 1;
        while (index >= 0) {
            int index2 = index - 1;
            ResolveInfoEx info = query.get(index);
            boolean isSpecificPackageName = (info.getActivityInfo() == null || info.getActivityInfo().getApplicationInfo() == null || !specificPackageName.equals(info.getActivityInfo().getApplicationInfo().packageName)) ? false : true;
            if ((!isCheckPriorityFlag || info.getPriority() >= 0) && isSpecificPackageName) {
                return info;
            }
            index = index2;
        }
        return null;
    }

    private boolean isCalendarType(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            Log.e(TAG, "isCalendarType, intent or action is null, return false");
            return false;
        }
        String action = intent.getAction();
        Uri data = intent.getData();
        String type = intent.getType();
        if (("android.intent.action.EDIT".equals(action) || "android.intent.action.INSERT".equals(action) || "android.intent.action.VIEW".equals(action)) && "vnd.android.cursor.item/event".equals(type)) {
            return true;
        }
        if (("android.intent.action.EDIT".equals(action) || "android.intent.action.INSERT".equals(action)) && "vnd.android.cursor.dir/event".equals(type)) {
            return true;
        }
        if (!(data == null || data.getPath() == null)) {
            if ("android.intent.action.VIEW".equals(action) && ("http".equals(data.getScheme()) || "https".equals(data.getScheme())) && "www.google.com".equals(data.getHost()) && (data.getPath().startsWith("/calendar/event") || (data.getPath().startsWith("/calendar/hosted") && data.getPath().endsWith("/event")))) {
                return true;
            }
        }
        if ("android.intent.action.VIEW".equals(action) && "text/calendar".equals(type)) {
            return true;
        }
        if ("android.intent.action.VIEW".equals(action) && "time/epoch".equals(type)) {
            return true;
        }
        if ("android.intent.action.VIEW".equals(action) && (data != null && ("com.android.calendar".equals(data.getHost()) || "com.huawei.calendar".equals(data.getHost()))) && "content".equals(data.getScheme())) {
            return true;
        }
        return false;
    }

    private int startBackupSession(IBackupSessionCallback callback) {
        getHwFileBackupManager();
        SlogEx.i(TAG, "application bind call startBackupSession");
        if (!checkBackupSessionCaller()) {
            return -2;
        }
        return this.mHwFileBackupManager.startBackupSession(callback);
    }

    private int executeBackupTask(int sessionId, String taskCmd) {
        getHwFileBackupManager();
        SlogEx.i(TAG, "bind call executeBackupTask on session:" + sessionId);
        if (!checkBackupSessionCaller()) {
            return -2;
        }
        return this.mHwFileBackupManager.executeBackupTask(sessionId, this.mHwFileBackupManager.prepareBackupTaskCmd(taskCmd, this.mPms.getPackagesLock()));
    }

    private int finishBackupSession(int sessionId) {
        getHwFileBackupManager();
        SlogEx.i(TAG, "bind call finishBackupSession sessionId:" + sessionId);
        if (!checkBackupSessionCaller()) {
            return -2;
        }
        return this.mHwFileBackupManager.finishBackupSession(sessionId);
    }

    private boolean checkBackupSessionCaller() {
        getHwFileBackupManager();
        int callingUid = Binder.getCallingUid();
        if (callingUid == 1001 || callingUid == 5513) {
            return true;
        }
        String pkgName = this.mPms.getNameForUidInner(callingUid);
        if (!this.mHwFileBackupManager.checkBackupPackageName(pkgName) || !isPlatformSignatureApp(pkgName)) {
            return false;
        }
        return true;
    }

    public boolean isPlatformSignatureApp(String pkgName) {
        boolean isPlatformSignApp = false;
        if (pkgName == null) {
            return false;
        }
        if (this.mPms.checkSignaturesInner(PackageManagerServiceEx.PLATFORM_PACKAGE_NAME, pkgName) == 0) {
            isPlatformSignApp = true;
        }
        if (!isPlatformSignApp) {
            SlogEx.d(TAG, "is not platform signature app, pkgName is " + pkgName);
        }
        return isPlatformSignApp;
    }

    private void getHwFileBackupManager() {
        if (this.mHwFileBackupManager == null) {
            this.mHwFileBackupManager = HwFileBackupManager.getInstance(this.mPms.getInstallerInner());
        }
    }

    private void getAPKInstallList(List<File> apkInstallLists, HashMap<String, HashSet<String>> multiInstallMap) {
        HwPreAppManager.getInstance(this).getMultiApkInstallList(apkInstallLists, multiInstallMap);
    }

    private void installAPKforInstallList(HashSet<String> installList, int flags, int scanMode, long currentTime) {
        installAPKforInstallList(installList, flags, scanMode, currentTime, 0);
    }

    public void installAPKforInstallList(HashSet<String> installList, int parseFlags, int scanFlags, long currentTime, int hwFlags) {
        if (installList != null && installList.size() != 0) {
            if (this.mPms.getCotaFlagInner()) {
                this.mPms.setHwPMSCotaApksInstallStatus(-1);
            }
            int fileSize = installList.size();
            File[] files = new File[fileSize];
            Iterator<String> it = installList.iterator();
            int index = 0;
            while (it.hasNext()) {
                String installPath = it.next();
                File file = new File(installPath);
                if (index < fileSize) {
                    files[index] = file;
                    Flog.i(205, "add package install path : " + installPath);
                    index++;
                } else {
                    SlogEx.w(TAG, "faile to add package install path : " + installPath + "fileSize:" + fileSize + ",index:" + index);
                }
            }
            this.mPms.scanPackageFilesLIInner(files, parseFlags, scanFlags, currentTime, hwFlags);
        }
    }

    public boolean isDelappInData(PackageSettingEx ps) {
        if (ps == null || ps.isObjNull()) {
            return false;
        }
        return HwPreAppManager.getInstance(this).isDelappInData(ps);
    }

    public boolean isUninstallApk(String filePath) {
        if (filePath == null) {
            return false;
        }
        return HwForbidUninstallManager.getInstance(this).isUninstallApk(filePath);
    }

    public void getUninstallApk() {
        long startTime = HwPackageManagerServiceUtilsEx.hwTimingsBegin();
        HwForbidUninstallManager.getInstance(this).getUninstallApk();
        HwPackageManagerServiceUtilsEx.hwTimingsEnd(TAG, "getUninstallApk", startTime);
    }

    public synchronized HwCustPackageManagerServiceEx getCust() {
        if (this.mCust == null) {
            this.mCust = new HwCustPackageManagerServiceEx();
        }
        if (!(this.mCust instanceof HwCustPackageManagerServiceEx)) {
            return null;
        }
        return (HwCustPackageManagerServiceEx) this.mCust;
    }

    public boolean isHwCustHiddenInfoPackage(PackageParserEx.PackageEx pkgInfo) {
        if (pkgInfo == null || getCust() == null) {
            return false;
        }
        return getCust().isHwCustHiddenInfoPackage(pkgInfo);
    }

    public void setUpCustomResolverActivity(PackageParserEx.PackageEx pkg) {
        synchronized (this.mPms.getPackagesLockObject()) {
            ActivityInfoEx mResolveActivity = this.mPms.getResolveActivityInner();
            this.mPms.setUpCustomResolverActivityInner(pkg);
            if (!TextUtils.isEmpty(HwFrameworkFactoryEx.getHuaweiResolverActivity(this.mContext))) {
                mResolveActivity.setProcessName("system:ui");
                mResolveActivity.setTheme(HwPartResourceUtils.getResourceId("Theme_Holo_Dialog_Alert"));
            }
        }
    }

    public static boolean firstScan() {
        boolean isExist = new File(Environment.getDataDirectory(), "system/packages.xml").exists();
        StringBuilder sb = new StringBuilder();
        sb.append("is first scan?");
        sb.append(!isExist);
        SlogEx.i(TAG, sb.toString());
        return !isExist;
    }

    public int getStartBackupSession(IBackupSessionCallback callback) {
        return startBackupSession(callback);
    }

    public int getExecuteBackupTask(int sessionId, String taskCmd) {
        return executeBackupTask(sessionId, taskCmd);
    }

    public int getFinishBackupSession(int sessionId) {
        return finishBackupSession(sessionId);
    }

    public void getAPKInstallListForHwPMS(List<File> apkInstallLists, HashMap<String, HashSet<String>> multiInstallMap) {
        getAPKInstallList(apkInstallLists, multiInstallMap);
    }

    public void installAPKforInstallListForHwPMS(HashSet<String> installList, int flags, int scanMode, long currentTime) {
        installAPKforInstallList(installList, flags, scanMode, currentTime);
    }

    public void installAPKforInstallListForHwPMS(HashSet<String> installList, int parseFlags, int scanFlags, long currentTime, int hwFlags) {
        installAPKforInstallList(installList, parseFlags, scanFlags, currentTime, hwFlags);
    }

    private void initPackageBlackList() {
        boolean isCompleteProcess;
        BlackListAppsUtilsEx.readBlackList(this.mBlackListInfo);
        synchronized (this.mPms.getPackagesLockObject()) {
            BlackListAppsUtilsEx.readDisableAppList(this.mDisableAppListInfo);
            Iterator it = this.mDisableAppListInfo.getBlacklistApps().iterator();
            while (it.hasNext()) {
                BlackListInfoEx.BlackListAppEx app = (BlackListInfoEx.BlackListAppEx) it.next();
                this.mDisableAppMap.put(app.getPackageName(), app);
            }
        }
        this.mIsBlackListExist = (this.mBlackListInfo.getBlacklistApps().size() == 0 || this.mBlackListInfo.getVersionCode() == -1) ? false : true;
        if (!this.mIsBlackListExist) {
            deleteDisableAppListFile();
            return;
        }
        synchronized (this.mPms.getPackagesLockObject()) {
            if (!this.mPms.isUpgrade() && !BlackListAppsUtilsEx.isBlackListUpdate(this.mBlackListInfo, this.mDisableAppListInfo)) {
                if (validateDisabledAppFile()) {
                    isCompleteProcess = false;
                }
            }
            isCompleteProcess = true;
        }
        SlogEx.i(TAG, "initBlackList start, is completed process: " + isCompleteProcess);
        if (isCompleteProcess) {
            synchronized (this.mPms.getPackagesLockObject()) {
                Set<String> needDisablePackages = getNeedDisablePackages();
                enableComponentForAllUser(getNeedEnablePackages(needDisablePackages), true);
                enableComponentForAllUser(needDisablePackages, false);
                this.mDisableAppListInfo.getBlacklistApps().clear();
                for (Map.Entry<String, BlackListInfoEx.BlackListAppEx> entry : this.mDisableAppMap.entrySet()) {
                    this.mDisableAppListInfo.getBlacklistApps().add(entry.getValue());
                }
                this.mDisableAppListInfo.setVersionCode(this.mBlackListInfo.getVersionCode());
                BlackListAppsUtilsEx.writeBlackListToXml(this.mDisableAppListInfo);
            }
        } else {
            synchronized (this.mPms.getPackagesLockObject()) {
                Set<String> needDisablePackages2 = new ArraySet<>();
                for (Map.Entry<String, BlackListInfoEx.BlackListAppEx> entry2 : this.mDisableAppMap.entrySet()) {
                    setPackageDisableFlag(entry2.getKey(), true);
                    needDisablePackages2.add(entry2.getKey());
                }
                enableComponentForAllUser(needDisablePackages2, false);
            }
        }
        SlogEx.i(TAG, "initBlackList end");
    }

    private void deleteDisableAppListFile() {
        synchronized (this.mPms.getPackagesLockObject()) {
            if (this.mDisableAppMap.size() > 0) {
                SlogEx.i(TAG, "blacklist not exists, enable all disabled apps");
                Set<String> needEnablePackages = new ArraySet<>();
                for (Map.Entry<String, BlackListInfoEx.BlackListAppEx> entry : this.mDisableAppMap.entrySet()) {
                    needEnablePackages.add(entry.getKey());
                }
                enableComponentForAllUser(needEnablePackages, true);
                this.mDisableAppMap.clear();
            }
        }
        BlackListAppsUtilsEx.deleteDisableAppListFile();
    }

    private Set<String> getNeedDisablePackages() {
        Set<String> needDisablePackages = new ArraySet<>();
        Iterator it = this.mBlackListInfo.getBlacklistApps().iterator();
        while (it.hasNext()) {
            BlackListInfoEx.BlackListAppEx app = (BlackListInfoEx.BlackListAppEx) it.next();
            String pkg = app.getPackageName();
            if (!needDisablePackages.contains(pkg) && BlackListAppsUtilsEx.comparePackage((PackageParserEx.PackageEx) this.mPms.getPackagesLock().get(pkg), app) && !needDisablePackages.contains(pkg)) {
                setPackageDisableFlag(pkg, true);
                needDisablePackages.add(pkg);
                this.mDisableAppMap.put(pkg, app);
            }
        }
        return needDisablePackages;
    }

    private Set<String> getNeedEnablePackages(Set<String> needDisablePackages) {
        Set<String> needEnablePackages = new ArraySet<>();
        for (String pkg : new ArrayList<>(this.mDisableAppMap.keySet())) {
            if (!BlackListAppsUtilsEx.containsApp(this.mBlackListInfo.getBlacklistApps(), this.mDisableAppMap.get(pkg)) && !needDisablePackages.contains(pkg)) {
                if (this.mPms.getPackagesLock().get(pkg) != null) {
                    needEnablePackages.add(pkg);
                }
                this.mDisableAppMap.remove(pkg);
            }
        }
        return needEnablePackages;
    }

    private boolean validateDisabledAppFile() {
        if (this.mBlackListInfo.getBlacklistApps().size() == 0) {
            return false;
        }
        synchronized (this.mPms.getPackagesLockObject()) {
            for (Map.Entry<String, BlackListInfoEx.BlackListAppEx> entry : this.mDisableAppMap.entrySet()) {
                if (this.mPms.getPackagesLock().get(entry.getKey()) == null) {
                    return false;
                }
                if (!BlackListAppsUtilsEx.containsApp(this.mBlackListInfo.getBlacklistApps(), entry.getValue())) {
                    return false;
                }
            }
            return true;
        }
    }

    private void enableComponentForAllUser(Set<String> packages, boolean isEnable) {
        int[] userIds = new UserManagerServiceEx().getUserIds();
        for (int userId : userIds) {
            if (packages != null && packages.size() > 0) {
                for (String pkg : packages) {
                    enableComponentForPackage(pkg, isEnable, userId);
                }
            }
        }
    }

    private void setPackageDisableFlag(String packageName, boolean isDisable) {
        PackageParserEx.PackageEx pkg;
        if (TextUtils.isEmpty(packageName) || (pkg = (PackageParserEx.PackageEx) this.mPms.getPackagesLock().get(packageName)) == null) {
            return;
        }
        if (isDisable) {
            ApplicationInfoExt.orHwFlags(pkg.getApplicationInfo(), 268435456);
        } else {
            ApplicationInfoExt.orHwFlags(pkg.getApplicationInfo(), -268435457);
        }
    }

    private void enableComponentForPackage(String packageName, boolean isEnable, int userId) {
        if (!TextUtils.isEmpty(packageName)) {
            int newState = isEnable ? 0 : 2;
            try {
                PackageInfo packageInfo = IPackageManagerEx.getPackageInfo(packageName, 786959, userId);
                if (!(packageInfo == null || packageInfo.receivers == null || packageInfo.receivers.length == 0)) {
                    for (ActivityInfo receiverInfo : packageInfo.receivers) {
                        setEnabledComponentInner(new ComponentName(packageName, receiverInfo.name), newState, userId);
                    }
                }
                if (!(packageInfo == null || packageInfo.services == null || packageInfo.services.length == 0)) {
                    for (ServiceInfo serviceInfo : packageInfo.services) {
                        setEnabledComponentInner(new ComponentName(packageName, serviceInfo.name), newState, userId);
                    }
                }
                if (!(packageInfo == null || packageInfo.providers == null || packageInfo.providers.length == 0)) {
                    for (ProviderInfo providerInfo : packageInfo.providers) {
                        setEnabledComponentInner(new ComponentName(packageName, providerInfo.name), newState, userId);
                    }
                }
                if (!(packageInfo == null || packageInfo.activities == null || packageInfo.activities.length == 0)) {
                    for (ActivityInfo activityInfo : packageInfo.activities) {
                        setEnabledComponentInner(new ComponentName(packageName, activityInfo.name), newState, userId);
                    }
                }
            } catch (RemoteException e) {
                SlogEx.e(TAG, "BlackList: Get packageInfo fail, name=" + packageName);
            }
        }
    }

    public void updatePackageBlackListInfo(String packageName) {
        if (this.mIsBlackListExist && !TextUtils.isEmpty(packageName)) {
            try {
                synchronized (this.mPms.getPackagesLockObject()) {
                    PackageParserEx.PackageEx pkgInfo = (PackageParserEx.PackageEx) this.mPms.getPackagesLock().get(packageName);
                    boolean isNeedDisable = false;
                    boolean isNeedEnable = false;
                    if (pkgInfo != null) {
                        Iterator it = this.mBlackListInfo.getBlacklistApps().iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                break;
                            }
                            BlackListInfoEx.BlackListAppEx app = (BlackListInfoEx.BlackListAppEx) it.next();
                            if (BlackListAppsUtilsEx.comparePackage(pkgInfo, app)) {
                                setPackageDisableFlag(packageName, true);
                                this.mDisableAppMap.put(packageName, app);
                                isNeedDisable = true;
                                break;
                            }
                        }
                        if (!isNeedDisable && this.mDisableAppMap.containsKey(packageName)) {
                            setPackageDisableFlag(packageName, false);
                            this.mDisableAppMap.remove(packageName);
                            isNeedEnable = true;
                        }
                    } else if (this.mDisableAppMap.containsKey(packageName)) {
                        this.mDisableAppMap.remove(packageName);
                    }
                    enableComponentForPackages(isNeedDisable, isNeedEnable, packageName);
                    this.mDisableAppListInfo.getBlacklistApps().clear();
                    for (Map.Entry<String, BlackListInfoEx.BlackListAppEx> entry : this.mDisableAppMap.entrySet()) {
                        this.mDisableAppListInfo.getBlacklistApps().add(entry.getValue());
                    }
                    this.mDisableAppListInfo.setVersionCode(this.mBlackListInfo.getVersionCode());
                    BlackListAppsUtilsEx.writeBlackListToXml(this.mDisableAppListInfo);
                }
            } catch (Exception e) {
                SlogEx.e(TAG, "update BlackList info failed");
            }
        }
    }

    private void enableComponentForPackages(boolean isNeedDisable, boolean isNeedEnable, String packageName) {
        int[] userIds = new UserManagerServiceEx().getUserIds();
        for (int userId : userIds) {
            if (isNeedDisable) {
                enableComponentForPackage(packageName, false, userId);
            } else if (isNeedEnable) {
                enableComponentForPackage(packageName, true, userId);
            }
        }
    }

    private void setEnabledComponentInner(ComponentName componentName, int newState, int userId) {
        if (componentName != null) {
            String packageName = componentName.getPackageName();
            String className = componentName.getClassName();
            if (packageName != null && className != null) {
                synchronized (this.mPms.getPackagesLockObject()) {
                    PackageSettingEx pkgSetting = (PackageSettingEx) this.mPms.getSettings().getPackages().get(packageName);
                    if (pkgSetting != null) {
                        if (!pkgSetting.isObjNull()) {
                            PackageParserEx.PackageEx pkg = pkgSetting.getPkg();
                            if (pkg != null) {
                                if (pkg.hasComponentClassName(className)) {
                                    if (newState != 0) {
                                        if (newState != 2) {
                                            SlogEx.e(TAG, "Invalid new component state: " + newState);
                                            return;
                                        } else if (!pkgSetting.disableComponentLPw(className, userId)) {
                                            return;
                                        }
                                    } else if (!pkgSetting.restoreComponentLPw(className, userId)) {
                                        return;
                                    }
                                    return;
                                }
                            }
                            SlogEx.w(TAG, "Failed setComponentEnabledSetting: component class " + className + " does not exist in " + packageName);
                            return;
                        }
                    }
                    SlogEx.e(TAG, "setEnabledSetting, can not find pkgSetting, packageName = " + packageName);
                }
            }
        }
    }

    private void initBlackListForNewUser(int userHandle) {
        if (this.mIsBlackListExist) {
            synchronized (this.mPms.getPackagesLockObject()) {
                for (String pkg : this.mDisableAppMap.keySet()) {
                    enableComponentForPackage(pkg, false, userHandle);
                }
            }
        }
    }

    public void onNewUserCreated(int userId) {
        initBlackListForNewUser(userId);
    }

    public void replaceSignatureIfNeeded(PackageSettingEx ps, PackageParserEx.PackageEx pkg, boolean isBootScan, boolean isUpdate) {
        String packageSignType;
        boolean isNeedReplace;
        if (pkg != null && this.mCompatSettings != null) {
            if (!isBootScan) {
                removeIncompatiblePkg(pkg.getPackageName());
            }
            boolean isNeedReplace2 = false;
            String packageSignType2 = null;
            boolean isSignedByOldSystemSignature = this.mCompatSettings.isOldSystemSignature(PackageParserExUtils.getSignatures(pkg.getSigningDetailsEx()));
            if (isBootScan && !ps.isObjNull()) {
                synchronized (this.mPms.getPackagesLockObject()) {
                    CertCompatSettingsEx.PackageEx compatPkg = this.mCompatSettings.getCompatPackage(pkg.getPackageName());
                    if (compatPkg.getCodePath() != null && compatPkg.getCodePath().equals(ps.getCodePathString()) && compatPkg.getTimeStamp() == ps.getTimeStamp()) {
                        isNeedReplace2 = isNeedReplaceForCompatPackage(pkg, compatPkg.getCertType(), isSignedByOldSystemSignature);
                        packageSignType2 = isNeedReplace2 ? compatPkg.getCertType() : null;
                    }
                }
            }
            if (!isNeedReplace2 && HwCertificationManagerEx.isSupportHwCertification(pkg) && isContainHwCertification(pkg) && (packageSignType2 = getPackageSignType(pkg)) != null) {
                isNeedReplace2 = isNeedReplaceForHwCard(pkg, isBootScan, packageSignType2);
            }
            if (!isNeedReplace2) {
                ReplaceSignatureInfo replaceInfo = isNeedReplaceForWhiteList(pkg, isBootScan, isSignedByOldSystemSignature);
                if (replaceInfo.isNeedReplace()) {
                    isNeedReplace2 = true;
                    packageSignType2 = replaceInfo.getPackageSignType();
                }
            }
            boolean isUpdateSignedWhenUpgrade = false;
            boolean isUpgrdeNotFoundCertCompatFile = !this.mIsFoundCertCompatFile || this.mCompatSettings.isUpgrade();
            if (isSignedByOldSystemSignature && isBootScan && this.mPms.isUpgrade()) {
                isUpdateSignedWhenUpgrade = true;
            }
            if (isNeedReplace2 || !isUpdateSignedWhenUpgrade || !isUpgrdeNotFoundCertCompatFile || this.mCompatSettings.isIncompatPackage(pkg)) {
                isNeedReplace = isNeedReplace2;
                packageSignType = packageSignType2;
            } else {
                String packageSignType3 = this.mCompatSettings.getOldSignTpye(PackageParserExUtils.getSignatures(pkg.getSigningDetailsEx()));
                SlogEx.i(TAG, "CertCompat: system signature compat for OTA package:" + pkg.getPackageName());
                isNeedReplace = true;
                packageSignType = packageSignType3;
            }
            replaceSignature(isNeedReplace, ps, pkg, packageSignType, isBootScan);
        }
    }

    private void removeIncompatiblePkg(String packageName) {
        if (packageName != null) {
            synchronized (this.mIncompatiblePkgs) {
                if (this.mIncompatiblePkgs.contains(packageName)) {
                    this.mIncompatiblePkgs.remove(packageName);
                }
            }
        }
    }

    private boolean isNeedReplaceForCompatPackage(PackageParserEx.PackageEx pkg, String packageSignType, boolean isSignedByOldSystemSignature) {
        if (this.mCompatSettings.isIncompatPackage(pkg)) {
            SlogEx.i(TAG, "CertCompat: remove incompat package:" + pkg.getPackageName() + ",type:" + packageSignType);
            return false;
        } else if (!isSignedByOldSystemSignature && !isContainHwCertification(pkg)) {
            SlogEx.i(TAG, "CertCompat: remove normal package:" + pkg.getPackageName() + ",type:" + packageSignType);
            return false;
        } else if (!this.mCompatSettings.isUpgrade()) {
            return true;
        } else {
            SlogEx.i(TAG, "CertCompat: system signature compat for OTA package:" + pkg.getPackageName() + ",type:" + packageSignType);
            return true;
        }
    }

    private String getPackageSignType(PackageParserEx.PackageEx pkg) {
        int resultCode = getHwCertificateType(pkg);
        if (resultCode == HwCertificationEx.RESULT_PLATFORM) {
            return CERT_TYPE_PLATFORM;
        }
        if (resultCode == HwCertificationEx.RESULT_MEDIA) {
            return CERT_TYPE_MEDIA;
        }
        if (resultCode == HwCertificationEx.RESULT_SHARED) {
            return CERT_TYPE_SHARED;
        }
        if (resultCode == HwCertificationEx.RESULT_TESTKEY) {
            return CERT_TYPE_TESTKEY;
        }
        return null;
    }

    private boolean isNeedReplaceForHwCard(PackageParserEx.PackageEx pkg, boolean isBootScan, String packageSignType) {
        int certVersion = getHwCertSignatureVersion(pkg);
        boolean isUpgradeAndBootScan = isBootScan && this.mCompatSettings.isUpgrade() && this.mPms.isUpgrade();
        if (certVersion == HwCertificationEx.HWCERT_SIGNATURE_VERSION_3 || ((this.mCompatSettings.isCompatAllLegacyPackages() && certVersion == HwCertificationEx.HWCERT_SIGNATURE_VERSION_2) || this.mCompatSettings.isWhiteListedApp(pkg, isBootScan) || isUpgradeAndBootScan)) {
            SlogEx.i(TAG, "CertCompat: system signature compat for hwcert package:" + pkg.getPackageName() + ",type:" + packageSignType + ",certVersion:" + certVersion);
            return true;
        }
        SlogEx.i(TAG, "CertCompat: illegal system signature compat for hwcert package:" + pkg.getPackageName() + ",type:" + packageSignType + ",certVersion:" + certVersion);
        return false;
    }

    /* access modifiers changed from: private */
    public class ReplaceSignatureInfo {
        private boolean isNeedReplace;
        private String packageSignType;

        public ReplaceSignatureInfo(boolean isNeedReplace2, String packageSignType2) {
            this.isNeedReplace = isNeedReplace2;
            this.packageSignType = packageSignType2;
        }

        public boolean isNeedReplace() {
            return this.isNeedReplace;
        }

        public void setNeedReplace(boolean needReplace) {
            this.isNeedReplace = needReplace;
        }

        public String getPackageSignType() {
            return this.packageSignType;
        }

        public void setPackageSignType(String packageSignType2) {
            this.packageSignType = packageSignType2;
        }
    }

    private ReplaceSignatureInfo isNeedReplaceForWhiteList(PackageParserEx.PackageEx pkg, boolean isBootScan, boolean isSignedByOldSystemSignature) {
        ReplaceSignatureInfo replaceInfo = new ReplaceSignatureInfo(false, null);
        if (this.mCompatSettings.isSystemSignatureForWhiteList(PackageParserExUtils.getSignatures(pkg.getSigningDetailsEx())) && !this.mCompatSettings.isIncompatPackage(pkg) && ((isSignedByOldSystemSignature && this.mCompatSettings.isCompatAllLegacyPackages()) || this.mCompatSettings.isWhiteListedApp(pkg, isBootScan))) {
            replaceInfo.setPackageSignType(this.mCompatSettings.getSignTpyeForWhiteList(PackageParserExUtils.getSignatures(pkg.getSigningDetailsEx())));
            replaceInfo.setNeedReplace(true);
            SlogEx.i(TAG, "CertCompat: system signature compat for whitelist package:" + pkg.getPackageName() + ",type:" + replaceInfo.getPackageSignType());
            if (!isBootScan) {
                Context context = this.mContext;
                Flog.bdReport(context, 125, "{package:" + pkg.getPackageName() + ",version:" + pkg.getVersionCode() + ",type:" + replaceInfo.getPackageSignType() + "}");
            }
        }
        return replaceInfo;
    }

    private void replaceSignature(boolean isNeedReplace, PackageSettingEx ps, PackageParserEx.PackageEx pkg, String packageSignType, boolean isBootScan) {
        if (isNeedReplace) {
            replaceSignatureInner(ps, pkg, packageSignType);
        } else if (this.mCompatSettings.isLegacySignature(PackageParserExUtils.getSignatures(pkg.getSigningDetailsEx())) && !isBootScan) {
            synchronized (this.mIncompatiblePkgs) {
                if (!this.mIncompatiblePkgs.contains(pkg.getPackageName()) && !this.mCompatSettings.isIncompatPackage(pkg)) {
                    this.mIncompatiblePkgs.add(pkg.getPackageName());
                }
            }
            String pkgSignType = this.mCompatSettings.getOldSignTpye(PackageParserExUtils.getSignatures(pkg.getSigningDetailsEx()));
            if (pkgSignType != null) {
                SlogEx.i(TAG, "CertCompat: illegal system signature package:" + pkg.getPackageName() + ",type:" + pkgSignType);
                Context context = this.mContext;
                Flog.bdReport(context, 124, "{package:" + pkg.getPackageName() + ",version:" + pkg.getVersionCode() + ",type:" + pkgSignType + "}");
                return;
            }
            SlogEx.i(TAG, "CertCompat: Legacy system signature package:" + pkg.getPackageName());
        }
    }

    private void replaceSignatureInner(PackageSettingEx ps, PackageParserEx.PackageEx pkg, String signType) {
        CertCompatSettingsEx certCompatSettingsEx;
        if (signType != null && pkg != null && (certCompatSettingsEx = this.mCompatSettings) != null) {
            SignatureEx[] signs = certCompatSettingsEx.getNewSign(signType);
            if (signs.length == 0) {
                SlogEx.e(TAG, "CertCompat: signs init fail");
                return;
            }
            PackageParserEx.SigningDetailsEx newSignDetails = createNewSigningDetails(pkg.getSigningDetailsEx(), signs);
            this.mPms.setRealSigningDetails(pkg, pkg.getSigningDetailsEx());
            pkg.setSigningDetailsEx(newSignDetails);
            if (ps.getSignatures() != null && ps.getSignatures().getSigningDetails().hasSignatures()) {
                ps.getSignatures().setSigningDetails(pkg.getSigningDetailsEx());
            }
            SlogEx.d(TAG, "CertCompat: CertCompatPackage:" + pkg.getPackageName());
        }
    }

    private PackageParserEx.SigningDetailsEx createNewSigningDetails(PackageParserEx.SigningDetailsEx orig, SignatureEx[] newSigns) {
        return PackageParserExUtils.newSigningDetails(newSigns, orig.getSignatureSchemeVersion(), orig.getPublicKeys(), PackageParserExUtils.getPastSigningCertificates(orig));
    }

    public void initCertCompatSettings() {
        SlogEx.i(TAG, "CertCompat: init CertCompatSettings");
        this.mCompatSettings = new CertCompatSettingsEx();
        this.mIsFoundCertCompatFile = this.mCompatSettings.readCertCompatPackages();
    }

    @SuppressLint({"PreferForInArrayList"})
    public void writeCertCompatPackages(boolean isUpdate) {
        CertCompatSettingsEx certCompatSettingsEx = this.mCompatSettings;
        if (certCompatSettingsEx != null) {
            if (isUpdate) {
                Iterator<CertCompatSettingsEx.PackageEx> it = new ArrayList<>(certCompatSettingsEx.getALLCompatPackages()).iterator();
                while (it.hasNext()) {
                    CertCompatSettingsEx.PackageEx pkg = it.next();
                    if (pkg != null && !this.mPms.getPackagesLock().containsKey(pkg.getPackageName())) {
                        this.mCompatSettings.removeCertCompatPackage(pkg.getPackageName());
                    }
                }
            }
            this.mCompatSettings.writeCertCompatPackages();
        }
    }

    public void updateCertCompatPackage(PackageParserEx.PackageEx pkg, PackageSettingEx ps) {
        if (pkg != null && this.mCompatSettings != null) {
            SignatureEx[] realSigns = this.mPms.getRealSignature(pkg);
            if (realSigns == null || realSigns.length == 0 || ps.isObjNull()) {
                this.mCompatSettings.removeCertCompatPackage(pkg.getApplicationInfo().packageName);
            } else {
                this.mCompatSettings.insertCompatPackage(pkg.getApplicationInfo().packageName, ps);
            }
        }
    }

    public boolean isSystemSignatureUpdatedEx(SignatureEx[] previous, SignatureEx[] current) {
        CertCompatSettingsEx certCompatSettingsEx = this.mCompatSettings;
        if (certCompatSettingsEx == null) {
            return false;
        }
        return certCompatSettingsEx.isSystemSignatureUpdated(previous, current);
    }

    public void sendIncompatibleNotificationIfNeeded(final String packageName) {
        if (packageName != null) {
            synchronized (this.mIncompatiblePkgs) {
                boolean isUpdate = false;
                final boolean isShouldSend = false;
                if (this.mIncompatiblePkgs.contains(packageName)) {
                    this.mIncompatiblePkgs.remove(packageName);
                    isUpdate = true;
                    isShouldSend = true;
                } else if (this.mIncompatNotifications.contains(packageName)) {
                    isUpdate = true;
                }
                if (isUpdate) {
                    UiThreadEx.getHandler().post(new Runnable() {
                        /* class com.android.server.pm.HwPackageManagerServiceEx.AnonymousClass8 */

                        @Override // java.lang.Runnable
                        public void run() {
                            HwPackageManagerServiceEx.this.updateIncompatibleNotification(packageName, isShouldSend);
                        }
                    });
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateIncompatibleNotification(String packageName, boolean isSend) {
        try {
            int[] resolvedUserIds = ActivityManagerNativeEx.getRunningUserIds();
            int i = 0;
            if (isSend) {
                synchronized (this.mIncompatiblePkgs) {
                    this.mIncompatNotifications.add(packageName);
                }
                int length = resolvedUserIds.length;
                while (i < length) {
                    sendIncompatibleNotificationInner(packageName, resolvedUserIds[i]);
                    i++;
                }
                return;
            }
            boolean isCancelAll = false;
            synchronized (this.mPms.getPackagesLockObject()) {
                boolean isLegacySignature = false;
                PackageParserEx.PackageEx pkgInfo = (PackageParserEx.PackageEx) this.mPms.getPackagesLock().get(packageName);
                if (!(pkgInfo == null || this.mCompatSettings == null)) {
                    isLegacySignature = this.mCompatSettings.isLegacySignature(PackageParserExUtils.getSignatures(pkgInfo.getSigningDetailsEx()));
                }
                if (pkgInfo == null || !isLegacySignature) {
                    SlogEx.d(TAG, "CertCompat: Package removed or update to new system signature version, cancel all incompatible notification.");
                    isCancelAll = true;
                }
            }
            if (isCancelAll) {
                synchronized (this.mIncompatiblePkgs) {
                    this.mIncompatNotifications.remove(packageName);
                }
            }
            int length2 = resolvedUserIds.length;
            while (i < length2) {
                int id = resolvedUserIds[i];
                if (isCancelAll || !IPackageManagerEx.isPackageAvailable(packageName, id)) {
                    cancelIncompatibleNotificationInner(packageName, id);
                }
                i++;
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "CertCompat: RemoteException throw when update Incompatible Notification.");
        }
    }

    private void cancelIncompatibleNotificationInner(String packageName, int userId) {
        NotificationManager nm = (NotificationManager) this.mContext.getSystemService("notification");
        if (nm != null) {
            NotificationManagerExt.cancelAsUser(nm, packageName, HwPartResourceUtils.getResourceId("package_incompatible"), UserHandleEx.getUserHandle(userId));
            SlogEx.d(TAG, "CertCompat: cancel incompatible notification for u" + userId + ", packageName:" + packageName);
        }
    }

    private void sendIncompatibleNotificationInner(String packageName, int userId) {
        SlogEx.d(TAG, "CertCompat: send incompatible notification to u" + userId + ", packageName:" + packageName);
        PackageManager pm = this.mContext.getPackageManager();
        if (pm != null) {
            try {
                ApplicationInfo info = PackageManagerExt.getApplicationInfoAsUser(pm, packageName, 0, userId);
                Drawable icon = pm.getApplicationIcon(info);
                CharSequence title = pm.getApplicationLabel(info);
                String text = this.mContext.getString(33685898);
                PendingIntent pi = PendingIntentEx.getActivityAsUser(this.mContext, 0, new Intent("android.intent.action.UNINSTALL_PACKAGE", Uri.parse("package:" + packageName)), 0, (Bundle) null, UserHandleEx.getUserHandle(userId));
                if (pi == null) {
                    SlogEx.w(TAG, "CertCompat: Get PendingIntent fail, package: " + packageName);
                    return;
                }
                Notification notification = new Notification.Builder(this.mContext, SystemNotificationChannelsEx.ALERTS).setLargeIcon(UserIconsEx.convertToBitmap(icon)).setSmallIcon(HwPartResourceUtils.getResourceId("stat_sys_warning")).setContentTitle(title).setContentText(text).setContentIntent(pi).setDefaults(2).setPriority(2).setWhen(System.currentTimeMillis()).setShowWhen(true).setAutoCancel(true).addAction(new Notification.Action.Builder((Icon) null, this.mContext.getString(HwPartResourceUtils.getResourceId("package_uninstall")), pi).build()).build();
                NotificationManager nm = (NotificationManager) this.mContext.getSystemService("notification");
                if (nm != null) {
                    NotificationManagerExt.notifyAsUser(nm, packageName, 33685898, notification, UserHandleEx.getUserHandle(userId));
                }
            } catch (PackageManager.NameNotFoundException e) {
                SlogEx.w(TAG, "CertCompat: incompatible package: " + packageName + " not find for u" + userId);
            }
        }
    }

    public void recordInstallAppInfo(String pkgName, long beginTime, int installFlags) {
        int srcPkg;
        long endTime = SystemClock.elapsedRealtime();
        if ((installFlags & 32) != 0) {
            srcPkg = 1;
        } else {
            srcPkg = 0;
        }
        insertAppInfo(pkgName, srcPkg, beginTime, endTime);
    }

    private void insertAppInfo(String pkgName, int srcPkg, long beginTime, long endTime) {
        if (TextUtils.isEmpty(pkgName)) {
            SlogEx.e(TAG, "insertAppInfo pkgName is null");
            return;
        }
        HwSecurityDiagnoseManager sdm = HwSecurityDiagnoseManager.getInstance();
        if (sdm == null) {
            SlogEx.e(TAG, "insertAppInfo error, sdm is null");
            return;
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putString("pkg", pkgName);
            bundle.putInt("src", srcPkg);
            bundle.putLong("begintime", beginTime);
            bundle.putLong("endtime", endTime);
            if (!sdm.setMalData(HwSecurityDiagnoseManager.AntiMalDataSrcType.PMS.ordinal(), bundle)) {
                SlogEx.w(TAG, "insertAppInfo, failed to pass the filling information");
            }
        } catch (Exception e) {
            SlogEx.e(TAG, "insertAppInfo EXCEPTION");
        }
    }

    private void setCurrentEmuiSysImgVersion() {
        PackageExHandler packageExHandler = this.mHandler;
        if (packageExHandler != null) {
            packageExHandler.sendEmptyMessage(1);
        }
    }

    private void deleteSoundrecorder() {
        PackageExHandler packageExHandler = this.mHandler;
        if (packageExHandler != null) {
            packageExHandler.sendEmptyMessage(2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deleteSoundercorderIfNeed() {
        Log.i(TAG, "begin uninstall soundrecorder");
        if (PROP_DEFAULT_VALUE.equals(SystemPropertiesEx.get("persist.sys.uninstallapk", PROP_DEFAULT_VALUE))) {
            this.mPms.deletePackageInner(HW_SOUND_RECORDER, -1, 0, 2);
            Log.i(TAG, "uninstall soundrecorder ...");
            SystemPropertiesEx.set("persist.sys.uninstallapk", FEATURE_SUPPORT);
        }
    }

    /* access modifiers changed from: private */
    public static int deriveEmuiSysImgVersion() {
        try {
            String str = ManufactureNativeUtils.getVersionInfo(3);
            SlogEx.d(TAG, "deriveEmuiSysImgVersion, version info is " + str);
            if (TextUtils.isEmpty(str)) {
                return 0;
            }
            String ret = BuildConfig.FLAVOR;
            Matcher matcher = Pattern.compile("(\\d+\\.){3}\\d+").matcher(str);
            if (matcher.find()) {
                ret = matcher.group().trim();
            }
            SlogEx.d(TAG, "deriveEmuiSysImgVersion,find:" + ret);
            if (TextUtils.isEmpty(ret)) {
                return 0;
            }
            int version = Integer.parseInt(ret.replace(".", BuildConfig.FLAVOR));
            SlogEx.d(TAG, "deriveEmuiSysImgVersion,version:" + version);
            return version;
        } catch (NumberFormatException e) {
            SlogEx.w(TAG, "deriveEmuiSysImgVersion number format error");
            return 0;
        } catch (Exception e2) {
            SlogEx.w(TAG, "deriveEmuiSysImgVersion error");
            return 0;
        }
    }

    public boolean setApplicationAspectRatio(String packageName, String aspectName, float aspectRatio) {
        if (packageName == null || aspectName == null) {
            return false;
        }
        if (UserHandleEx.getAppId(Binder.getCallingUid()) != 1000) {
            this.mContext.enforceCallingPermission(SET_ASPECT_RATIO_PERMISSION, "Permission Denied for setApplicationAspectRatio!");
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mPms.getPackagesLockObject()) {
                PackageSettingEx pkgSetting = (PackageSettingEx) this.mPms.getSettings().getPackages().get(packageName);
                if (pkgSetting != null) {
                    if (!pkgSetting.isObjNull()) {
                        if (pkgSetting.getAspectRatio(aspectName) != aspectRatio) {
                            pkgSetting.setAspectRatio(aspectName, aspectRatio);
                            this.mPms.getSettings().writeLPr();
                            Binder.restoreCallingIdentity(callingId);
                            return true;
                        }
                        Binder.restoreCallingIdentity(callingId);
                        return false;
                    }
                }
                return false;
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public float getApplicationAspectRatio(String packageName, String aspectName) {
        long callingId;
        if (packageName == null || aspectName == null) {
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        callingId = Binder.clearCallingIdentity();
        try {
            PackageSettingEx pkgSetting = (PackageSettingEx) this.mPms.getSettings().getPackages().get(packageName);
            if (pkgSetting != null) {
                if (!pkgSetting.isObjNull()) {
                    float aspectRatio = pkgSetting.getAspectRatio(aspectName);
                    Binder.restoreCallingIdentity(callingId);
                    return aspectRatio;
                }
            }
            Binder.restoreCallingIdentity(callingId);
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        } catch (ArrayIndexOutOfBoundsException e) {
            SlogEx.w(TAG, "get " + aspectName + " index out of bounds! packageName :" + packageName);
        } catch (Exception e2) {
            SlogEx.w(TAG, "get " + aspectName + " other exception! packageName :" + packageName);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
        Binder.restoreCallingIdentity(callingId);
        return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    }

    public void addPreinstalledPkgToList(PackageParserEx.PackageEx scannedPkg) {
        if (scannedPkg != null && scannedPkg.getCodePath() != null && !scannedPkg.getCodePath().startsWith("/data/app/") && !scannedPkg.getCodePath().startsWith("/data/app-private/")) {
            sPreinstalledPackageList.add(scannedPkg.getPackageName());
        }
    }

    public List<String> getPreinstalledApkList() {
        XmlPullParser parser;
        List<String> preinstalledApkList = new ArrayList<>();
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(new File(PREINSTALLED_APK_LIST_DIR, PREINSTALLED_APK_LIST_FILE));
            parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            String tag = parser.getName();
            if ("values".equals(tag)) {
                parser.next();
                int outerDepth = parser.getDepth();
                while (true) {
                    int type = parser.next();
                    if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                        try {
                            stream2.close();
                            break;
                        } catch (IOException e) {
                            SlogEx.e(TAG, "close pre-installed apk file stream error.");
                        }
                    } else if (type != 3) {
                        if (type != 4) {
                            if ("string".equals(parser.getName()) && parser.getAttributeValue(1) != null) {
                                preinstalledApkList.add(parser.getAttributeValue(1));
                            }
                        }
                    }
                }
                return preinstalledApkList;
            }
            throw new XmlPullParserException("Settings do not start with policies tag: found " + tag);
        } catch (FileNotFoundException e2) {
            SlogEx.e(TAG, "pre-installed apk file is not exist.");
            if (0 != 0) {
                stream.close();
            }
        } catch (XmlPullParserException e3) {
            SlogEx.e(TAG, "XmlPullParserException. parsing pre-installed apk file.");
            if (0 != 0) {
                stream.close();
            }
        } catch (IOException e4) {
            SlogEx.e(TAG, "IOException. parsing pre-installed apk file.");
            if (0 != 0) {
                stream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e5) {
                    SlogEx.e(TAG, "close pre-installed apk file stream error.");
                }
            }
            throw th;
        }
        while (true) {
            int type2 = parser.next();
            if (type2 == 1 || type2 == 2) {
                break;
            }
        }
    }

    public void writePreinstalledApkListToFile() {
        File preinstalledApkFile = new File(PREINSTALLED_APK_LIST_DIR, PREINSTALLED_APK_LIST_FILE);
        if (!preinstalledApkFile.exists()) {
            FileOutputStream stream = null;
            try {
                if (preinstalledApkFile.createNewFile()) {
                    FileUtilsEx.setPermissions(preinstalledApkFile.getPath(), 416, -1, -1);
                }
                FileOutputStream stream2 = new FileOutputStream(preinstalledApkFile, false);
                FastXmlSerializerEx out = new FastXmlSerializerEx();
                out.setOutput(stream2, UTF_8);
                out.startDocument((String) null, true);
                out.startTag((String) null, "values");
                PackageManager pm = this.mContext.getPackageManager();
                for (String packageName : sPreinstalledPackageList) {
                    String apkName = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 67108864)).toString();
                    out.startTag((String) null, "string");
                    out.attribute((String) null, "name", packageName);
                    out.attribute((String) null, "apkName", apkName);
                    out.endTag((String) null, "string");
                }
                out.endTag((String) null, "values");
                out.endDocument();
                try {
                    stream2.close();
                } catch (IOException e) {
                    SlogEx.e(TAG, "close pre-installed apk file stream error.");
                }
            } catch (PackageManager.NameNotFoundException e2) {
                SlogEx.e(TAG, "Can not fount pre-installed apk file.");
                if (0 != 0) {
                    stream.close();
                }
            } catch (IOException e3) {
                SlogEx.e(TAG, "IOException. Failed parsing pre-installed apk file.");
                if (0 != 0) {
                    stream.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e4) {
                        SlogEx.e(TAG, "close pre-installed apk file stream error.");
                    }
                }
                throw th;
            }
        }
    }

    public void createPublicityFile() {
        if ("CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", BuildConfig.FLAVOR))) {
            ParceledListSliceEx slice = this.mPms.getInstalledApplications(0, 0);
            if (this.mPms.isUpgrade()) {
                PackagePublicityUtils.deletePublicityFile();
            }
            PackagePublicityUtils.writeAllPakcagePublicityInfoIntoFile(this.mContext, slice);
        }
    }

    public List<String> getHwPublicityAppList() {
        return PackagePublicityUtils.getHwPublicityAppList(this.mContext);
    }

    public ParcelFileDescriptor getHwPublicityAppParcelFileDescriptor() {
        return PackagePublicityUtils.getHwPublicityAppParcelFileDescriptor();
    }

    public void initCustStoppedAppsEx() {
        initCustStoppedApps();
    }

    protected static void initCustStoppedApps() {
        File file;
        if (!firstScan()) {
            SlogEx.i(TAG, "not first boot. don't init cust stopped apps.");
            return;
        }
        SlogEx.i(TAG, "first boot. init cust stopped apps.");
        File file2 = null;
        try {
            file2 = HwCfgFilePolicy.getCfgFile("xml/not_start_firstboot.xml", 0);
            if (file2 == null) {
                file = new File(HwDelAppManager.CUST_PRE_DEL_DIR, "xml/not_start_firstboot.xml");
                file2 = file;
            }
        } catch (NoClassDefFoundError e) {
            SlogEx.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            if (0 == 0) {
                file = new File(HwDelAppManager.CUST_PRE_DEL_DIR, "xml/not_start_firstboot.xml");
            }
        } catch (Throwable th) {
            if (0 == 0) {
                new File(HwDelAppManager.CUST_PRE_DEL_DIR, "xml/not_start_firstboot.xml");
            }
            throw th;
        }
        parseCustStoppedApps(file2);
    }

    private static void parseCustStoppedApps(File file) {
        try {
            FileReader xmlReader = new FileReader(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(xmlReader);
                XmlUtilsEx.beginDocument(parser, "resources");
                while (true) {
                    XmlUtilsEx.nextElement(parser);
                    if (parser.getName() == null) {
                        try {
                            xmlReader.close();
                            return;
                        } catch (IOException e) {
                            SlogEx.e(TAG, "Got IOException when close black_package_name.xml!");
                            return;
                        }
                    } else if (BUNDLE_PACKAGE.equals(parser.getName())) {
                        String value = parser.getAttributeValue(null, "name");
                        if (!TextUtils.isEmpty(value)) {
                            sCustStoppedApps.add(value.intern());
                            SlogEx.i(TAG, "cust stopped apps:" + value);
                        } else {
                            sCustStoppedApps.clear();
                            SlogEx.e(TAG, "not_start_firstboot.xml bad format.");
                        }
                    }
                }
            } catch (XmlPullParserException e2) {
                SlogEx.e(TAG, "Got execption parsing black_package_name.xml!");
                xmlReader.close();
            } catch (IOException e3) {
                SlogEx.e(TAG, "Got IOException parsing black_package_name.xml!");
                xmlReader.close();
            } catch (Throwable th) {
                try {
                    xmlReader.close();
                } catch (IOException e4) {
                    SlogEx.e(TAG, "Got IOException when close black_package_name.xml!");
                }
                throw th;
            }
        } catch (FileNotFoundException e5) {
            SlogEx.w(TAG, "There is no file named not_start_firstboot.xml!");
        }
    }

    public boolean isCustedCouldStoppedEx(String pkgName, boolean isBlock, boolean isStopped) {
        return isCustedCouldStopped(pkgName, isBlock, isStopped);
    }

    public static boolean isCustedCouldStopped(String pkgName, boolean isBlock, boolean isStopped) {
        if (pkgName == null) {
            return false;
        }
        boolean isContain = sCustStoppedApps.contains(pkgName);
        if (isContain) {
            if (isBlock) {
                SlogEx.i(TAG, "blocked broadcast send to system app:" + pkgName + ", stopped:" + isStopped);
            } else {
                SlogEx.i(TAG, "a system app is customized not to start at first boot. app:" + pkgName);
            }
        }
        return isContain;
    }

    public void scanRemovableAppDir(int scanMode) {
        long startTime = HwPackageManagerServiceUtilsEx.hwTimingsBegin();
        HwDelAppManager.getInstance(this).scanRemovableAppDir(scanMode);
        HwPackageManagerServiceUtilsEx.hwTimingsEnd(TAG, "scanRemovableAppDir", startTime);
    }

    public boolean needInstallRemovablePreApk(PackageParserEx.PackageEx pkg, int hwFlags) {
        return HwUninstalledAppManager.getInstance(this, this).needInstallRemovablePreApk(pkg, hwFlags);
    }

    public boolean isDelapp(PackageSettingEx ps) {
        if (ps == null || ps.isObjNull()) {
            return false;
        }
        return HwDelAppManager.getInstance(this).isDelapp(ps);
    }

    public boolean isReservePersistentApp(PackageSettingEx ps) {
        if (ps == null || ps.getCodePath() == null) {
            return false;
        }
        String codePath = ps.getCodePath().toString();
        for (String path : new String[]{SYSTEM_PRIV_APP_DIR, SYSTEM_APP_DIR, HW_PRODUCT_DIR}) {
            if (codePath.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    public void addFlagsForRemovablePreApk(PackageParserEx.PackageEx pkg, int hwFlags) {
        HwPackageManagerServiceUtilsEx.addFlagsForRemovablePreApk(pkg, hwFlags);
    }

    public boolean isDisallowUninstallApk(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (HwDeviceManagerEx.disallowOp(5, packageName)) {
            Flog.i(209, "Not removing package " + packageName + ": is disallowed!");
            return true;
        }
        try {
            String disallowUninstallPkgList = Settings.Secure.getString(this.mContext.getContentResolver(), "enterprise_disallow_uninstall_apklist");
            SlogEx.w(TAG, "isEnterpriseDisallowUninstallApk disallowUninstallPkgList : " + disallowUninstallPkgList);
            if (!TextUtils.isEmpty(disallowUninstallPkgList)) {
                for (String pkg : disallowUninstallPkgList.split(TERMINATOR)) {
                    if (packageName.equals(pkg)) {
                        SlogEx.i(TAG, packageName + " is in the enterprise Disallow UninstallApk blacklist!");
                        Flog.i(209, "Not removing package " + packageName + ": is disallowed!");
                        return true;
                    }
                }
            }
            return false;
        } catch (IllegalStateException e) {
            SlogEx.e(TAG, "get disallow uninstall pkg list IllegalStateException : " + e.getMessage());
            return false;
        } catch (Exception e2) {
            SlogEx.e(TAG, "isDisallowUninstallApk, get disallow uninstall list failed");
            return false;
        }
    }

    public boolean isInMultiWinWhiteList(String packageName) {
        if (packageName != null && !IS_HW_MULTIWINDOW_SUPPORTED && MultiWinWhiteListManager.getInstance().isInMultiWinWhiteList(packageName)) {
            return true;
        }
        return false;
    }

    public boolean isInMWPortraitWhiteList(String packageName) {
        if (packageName == null) {
            return false;
        }
        return MultiWinWhiteListManager.getInstance().isInMWPortraitWhiteList(packageName);
    }

    public String getResourcePackageNameByIcon(String pkgName, int icon, int userId) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            Log.w(TAG, "packageManager is null !");
            return null;
        }
        try {
            return PackageManagerExt.getResourcesForApplicationAsUser(pm, pkgName, userId).getResourcePackageName(icon);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "packageName " + pkgName + ": Resources not found !");
            return null;
        } catch (Resources.NotFoundException e2) {
            Log.w(TAG, "packageName " + pkgName + ": ResourcesPackageName not found !");
            return null;
        } catch (RuntimeException e3) {
            Log.w(TAG, "RuntimeException in getResourcePackageNameByIcon !");
            return null;
        }
    }

    public List<String> getOldDataBackup() {
        return HwUninstalledAppManager.getInstance(this, this).getOldDataBackup();
    }

    private boolean assertScanInstallApkLocked(String packageName, String apkFile, int userId) {
        if (this.mScanInstallApkList.contains(apkFile)) {
            SlogEx.w(TAG, "Scan install , the apk file " + apkFile + " is already in scanning. Skipping duplicate.");
            return false;
        }
        Map<String, String> mUninstalledMap = getUninstalledMap();
        if ((mUninstalledMap == null || !mUninstalledMap.containsValue(apkFile)) && (!IS_AUTO_INSTALL_ENABLE || !isScanInstallApk(apkFile))) {
            SlogEx.w(TAG, "Scan install , the apk file " + apkFile + " is not a uninstalled system app's codePath. Skipping.");
            return false;
        }
        PackageParserEx.PackageEx pkg = parsePackage(apkFile);
        if (!(pkg == null || pkg.providers == null)) {
            try {
                this.mPms.assertProvidersNotDefined(pkg);
            } catch (PackageManagerExceptionEx e) {
                SlogEx.w(TAG, "Scan install, " + e.getMessage());
                return false;
            }
        }
        if (userId == -1) {
            SlogEx.i(TAG, "Scan install for all users!");
            return true;
        }
        PackageSettingEx psTemp = this.mPms.getSettings().getPackageLPr(packageName);
        if (psTemp.isObjNull() || !psTemp.getInstalled(userId)) {
            return true;
        }
        SlogEx.w(TAG, "Scan install , " + packageName + " is already installed in user " + userId + " . Skipping scan " + apkFile);
        return false;
    }

    private PackageParserEx.PackageEx parsePackage(String apkFile) {
        try {
            return new android.content.pm.PackageParserEx().parsePackage(new File(apkFile), 0, true, 0);
        } catch (PackageParserEx.PackageParserExceptionEx e) {
            SlogEx.w(TAG, "Scan install, parse " + apkFile + " to get package name failed!" + e.getMessage());
            return null;
        }
    }

    private boolean checkScanInstallCaller() {
        int callingUid = Binder.getCallingUid();
        if (callingUid == 1000) {
            return true;
        }
        return SCAN_INSTALL_CALLER_PACKAGES.contains(this.mPms.getNameForUidInner(callingUid));
    }

    public boolean scanInstallApk(String apkFile) {
        if (apkFile == null) {
            return false;
        }
        return scanInstallApk(null, apkFile, UserHandleEx.getUserId(Binder.getCallingUid()));
    }

    public boolean scanInstallApk(String packageName, String apkFile, int userId) {
        if (!checkScanInstallCaller()) {
            SlogEx.w(TAG, "Scan install ,check caller failed!");
            return false;
        } else if (apkFile == null) {
            SlogEx.d(TAG, "Illegal install apk file:" + apkFile);
            return false;
        } else {
            String pkgName = packageName;
            if (TextUtils.isEmpty(packageName)) {
                PackageParserEx.PackageEx pkg = parsePackage(apkFile);
                if (pkg == null || pkg.getPackage() == null) {
                    SlogEx.w(TAG, "Scan install, get package name failed, pkg is null!");
                    return false;
                }
                pkgName = pkg.getPackageName();
            }
            synchronized (this.mScanInstallApkList) {
                if (!assertScanInstallApkLocked(pkgName, apkFile, userId)) {
                    return false;
                }
                this.mScanInstallApkList.add(apkFile);
                SlogEx.i(TAG, "Scan install , add to list:" + apkFile);
            }
            int curId = UserHandleEx.getUserId(Binder.getCallingUid());
            boolean isSuccess = false;
            long token = Binder.clearCallingIdentity();
            try {
                PackageSettingEx ps = this.mPms.getSettings().getPackageLPr(pkgName);
                if (ps.isObjNull() || !ps.isAnyInstalled(PackageManagerServiceEx.getUserManager().getUserIds())) {
                    isSuccess = scanPackageLI(apkFile, pkgName, userId, curId);
                } else {
                    isSuccess = checkPackageCodePath(ps, pkgName, apkFile, userId, packageName);
                }
                if (IS_AUTO_INSTALL_ENABLE && isSuccess && isScanInstallApk(apkFile)) {
                    HwForbidUninstallManager.getInstance(this).writeRemoveUnstallApks(pkgName, apkFile);
                }
                Binder.restoreCallingIdentity(token);
                synchronized (this.mScanInstallApkList) {
                    if (this.mScanInstallApkList.remove(apkFile)) {
                        SlogEx.i(TAG, "Scan install , remove from list:" + apkFile);
                    }
                }
            } catch (Exception e) {
                try {
                    SlogEx.e(TAG, "Scan install " + apkFile + " failed!");
                    Binder.restoreCallingIdentity(token);
                    synchronized (this.mScanInstallApkList) {
                        if (this.mScanInstallApkList.remove(apkFile)) {
                            SlogEx.i(TAG, "Scan install , remove from list:" + apkFile);
                        }
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    synchronized (this.mScanInstallApkList) {
                        if (this.mScanInstallApkList.remove(apkFile)) {
                            SlogEx.i(TAG, "Scan install , remove from list:" + apkFile);
                        }
                        throw th;
                    }
                }
            }
            return isSuccess;
        }
    }

    private boolean checkPackageCodePath(PackageSettingEx ps, String pkgName, String apkFile, int userId, String packageName) {
        boolean isSuccess = false;
        if (apkFile.equals(ps.getCodePathString()) || packageName != null) {
            boolean z = true;
            int[] installUsers = userId == -1 ? ps.queryInstalledUsers(PackageManagerServiceEx.getUserManager().getUserIds(), false) : new int[]{userId};
            int length = installUsers.length;
            int result = 1;
            int result2 = 0;
            while (true) {
                if (result2 >= length) {
                    break;
                }
                int installUser = installUsers[result2];
                if (installUser == 0 || !this.mPms.getUserManagerInternalInner().isClonedProfile(installUser)) {
                    result = this.mPms.installExistingPackageAsUserInternalInner(pkgName, installUser, 4194304, 0);
                    if (result != 1) {
                        SlogEx.w(TAG, "Scan install failed for user " + installUser + ", installExistingPackageAsUser:" + apkFile);
                        break;
                    }
                } else {
                    SlogEx.d(TAG, "Scan install, skipping cloned user " + installUser + "!");
                }
                result2++;
            }
            if (result != 1) {
                z = false;
            }
            isSuccess = z;
            PackageSettingEx psTemp = this.mPms.getSettings().getPackageLPr(pkgName);
            if (isSuccess && !psTemp.isObjNull() && psTemp.queryInstalledUsers(PackageManagerServiceEx.getUserManager().getUserIds(), false).length == 0) {
                removeFromUninstalledDelapp(pkgName);
            }
            SlogEx.d(TAG, "Scan install , installExistingPackageAsUser:" + apkFile + " isSuccess:" + isSuccess);
        } else {
            SlogEx.w(TAG, "Scan install ," + pkgName + " installed by other user from " + ps.getCodePathString());
        }
        return isSuccess;
    }

    private boolean scanPackageLI(String apkFile, String pkgName, int userId, int curId) {
        int scanFlags;
        int parseFlags;
        PackageManagerExceptionEx e;
        boolean isSuccess = false;
        File scanFile = new File(apkFile);
        if (HwPackageManagerServiceUtilsEx.isNoSystemPreApp(apkFile)) {
            scanFlags = 139792 & -131073;
            parseFlags = 0;
        } else if (HwPreAppManager.getInstance(this).isPrivilegedPreApp(apkFile)) {
            scanFlags = 139792 | SCAN_AS_PRIVILEGED;
            parseFlags = SCAN_BOOTING;
        } else {
            scanFlags = 139792;
            parseFlags = SCAN_BOOTING;
        }
        try {
            try {
                PackageParserEx.PackageEx pp = this.mPms.scanPackageLIInner(scanFile, parseFlags, scanFlags, 0, new UserHandleEx(curId), 1107296256);
                isSuccess = pp != null;
                if (isSuccess) {
                    try {
                        this.mPms.setWhitelistedRestrictedPermissionsInner(pkgName, pp.requestedPermissions(), 2, userId);
                    } catch (PackageManagerExceptionEx e2) {
                        e = e2;
                        SlogEx.e(TAG, "Scan install, failed to parse package: " + e.getMessage());
                        return isSuccess;
                    }
                }
                SlogEx.d(TAG, "Scan install , restore from :" + apkFile + " isSuccess:" + isSuccess);
            } catch (PackageManagerExceptionEx e3) {
                e = e3;
                SlogEx.e(TAG, "Scan install, failed to parse package: " + e.getMessage());
                return isSuccess;
            }
        } catch (PackageManagerExceptionEx e4) {
            e = e4;
            SlogEx.e(TAG, "Scan install, failed to parse package: " + e.getMessage());
            return isSuccess;
        }
        return isSuccess;
    }

    public List<String> getScanInstallList() {
        return HwUninstalledAppManager.getInstance(this, this).getScanInstallList();
    }

    public void doPostScanInstall(PackageParserEx.PackageEx pkg, UserHandleEx user, boolean isNewInstall, int hwFlags, PackageParserEx.PackageEx scannedPkg) {
        if (pkg != null) {
            int userId = user.getIdentifier();
            if (needSetPermissionFlag() && scannedPkg != null && scannedPkg.getPackage() != null && HwPreAppManager.getInstance(this).isAppNonSystemPartitionDir(pkg.getCodePath())) {
                this.mPms.setWhitelistedRestrictedPermissionsInner(pkg.getPackageName(), pkg.requestedPermissions(), 2, userId);
            }
            if (!(scannedPkg == null || scannedPkg.getPackage() == null)) {
                HotInstallEx.recordAutoInstallPkg(pkg);
            }
            if ((hwFlags & 1073741824) != 0 && isPreRemovableApp(pkg.getCodePath())) {
                PackageManagerServiceEx.PackageInstalledInfoEx res = new PackageManagerServiceEx.PackageInstalledInfoEx();
                res.setReturnCode(1);
                res.setUid(-1);
                if (isNewInstall) {
                    res.setOrigUsers(new int[]{userId});
                } else {
                    res.setOrigUsers(this.mPms.getSettings().getPackageLPr(pkg.getPackageName()).queryInstalledUsers(PackageManagerServiceEx.getUserManager().getUserIds(), true));
                }
                res.setPkg((PackageParserEx.PackageEx) null);
                res.setRemovedInfo((PackageManagerServiceEx.PackageRemovedInfoEx) null);
                try {
                    this.mPms.updateSharedLibrariesLPrInner(pkg, (PackageParserEx.PackageEx) null);
                } catch (PackageManagerExceptionEx e) {
                    SlogEx.e(TAG, "updateSharedLibrariesLPr failed: " + e.getMessage());
                }
                this.mPms.updateSettingsLIInner(pkg, "com.huawei.android.launcher", PackageManagerServiceEx.getUserManager().getUserIds(), res, user, 4);
                this.mPms.prepareAppDataAfterInstallLIFInner(pkg);
                Bundle extras = new Bundle();
                extras.putInt("android.intent.extra.UID", pkg.getApplicationInfo() != null ? pkg.getApplicationInfo().uid : 0);
                this.mPms.sendPackageBroadcastInner("android.intent.action.PACKAGE_ADDED", pkg.getPackageName(), extras, 0, (String) null, (IIntentReceiverEx) null, new int[]{userId}, (int[]) null);
                removeFromUninstalledDelapp(pkg.getPackageName(), this.mPms.getSettings().getPackageLPr(pkg.getPackageName()));
                SlogEx.d(TAG, "Scan install done for package:" + pkg.getPackageName());
            }
        }
    }

    private void removeFromUninstalledDelapp(String packageName, PackageSettingEx psTemp) {
        if (psTemp.isObjNull()) {
            SlogEx.i(TAG, " the PackageSettingEx of " + packageName + " is null.");
            return;
        }
        int[] installedUsers = psTemp.queryInstalledUsers(PackageManagerServiceEx.getUserManager().getUserIds(), false);
        int countClonedUser = 0;
        for (int installedUser : installedUsers) {
            if (this.mPms.getUserManagerInternalInner().isClonedProfile(installedUser)) {
                countClonedUser++;
                SlogEx.d(TAG, installedUser + " skiped, it is cloned user when install package:" + packageName);
            }
        }
        if (installedUsers.length == 0 || installedUsers.length == countClonedUser) {
            removeFromUninstalledDelapp(packageName);
        }
    }

    private void removeFromUninstalledDelapp(String apkName) {
        HwUninstalledAppManager.getInstance(this, this).removeFromUninstalledDelapp(apkName);
    }

    private boolean needSetPermissionFlag() {
        if (this.mPms.getSystemReadyInner()) {
            return true;
        }
        if (this.mPms.isUpgrade()) {
            return false;
        }
        if (TextUtils.isEmpty(REGIONAL_PHONE_SWITCH) && TextUtils.isEmpty(TME_CUSTOMIZE_SWITCH) && TextUtils.isEmpty(ECOTA_VERSION)) {
            return false;
        }
        return true;
    }

    public void recordUninstalledDelapp(String packageName, String path) {
        HwUninstalledAppManager.getInstance(this, this).recordUninstalledDelapp(packageName, path);
    }

    public void readPreInstallApkList() {
        HwPreAppManager.getInstance(this).readPreInstallApkList();
    }

    public boolean isPreRemovableApp(String codePath) {
        if (codePath == null) {
            return false;
        }
        return HwPreAppManager.getInstance(this).isPreRemovableApp(codePath);
    }

    public void parseInstalledPkgInfo(String pkgUri, String pkgName, String pkgVerName, int pkgVerCode, int resultCode, boolean isPkgUpdated) {
        String installedPath = BuildConfig.FLAVOR;
        String installerPackageName = BuildConfig.FLAVOR;
        if (!(pkgUri == null || pkgUri.length() == 0)) {
            int splitIndex = pkgUri.indexOf(TERMINATOR);
            if (splitIndex >= 0) {
                installedPath = pkgUri.substring(0, splitIndex);
                installerPackageName = pkgUri.substring(splitIndex + 1);
            } else {
                installerPackageName = pkgUri;
            }
        }
        Bundle extrasInfo = new Bundle(1);
        extrasInfo.putString(INSTALLATION_EXTRA_PACKAGE_NAME, pkgName);
        extrasInfo.putInt(INSTALLATION_EXTRA_PACKAGE_VERSION_CODE, pkgVerCode);
        extrasInfo.putString(INSTALLATION_EXTRA_PACKAGE_VERSION_NAME, pkgVerName);
        extrasInfo.putBoolean(INSTALLATION_EXTRA_PACKAGE_UPDATE, isPkgUpdated);
        extrasInfo.putInt(INSTALLATION_EXTRA_PACKAGE_INSTALL_RESULT, resultCode);
        extrasInfo.putString(INSTALLATION_EXTRA_PACKAGE_URI, installedPath);
        extrasInfo.putString(INSTALLATION_EXTRA_INSTALLER_PACKAGE_NAME, installerPackageName);
        Intent intentInfo = new Intent(ACTION_GET_PACKAGE_INSTALLATION_INFO);
        intentInfo.putExtras(extrasInfo);
        intentInfo.setFlags(1073741824);
        this.mContext.sendBroadcast(intentInfo, BROADCAST_PERMISSION);
        SlogEx.v(TAG, "POST_INSTALL: pkgName = " + pkgName + ", pkgUri = " + pkgUri + ", pkgInstalledPath = " + installedPath + ", pkgInstallerPackageName = " + installerPackageName + ", pkgVerName = " + pkgVerName + ", pkgVerCode = " + pkgVerCode + ", resultCode = " + resultCode + ", pkgUpdate = " + isPkgUpdated);
    }

    public boolean containDelPath(String sensePath) {
        if (sensePath == null) {
            return false;
        }
        return HwDelAppManager.getInstance(this).containDelPath(sensePath);
    }

    public void addUpdatedRemoveableAppFlag(String scanFile, String packageName) {
        HwPreAppManager.getInstance(this).addUpdatedRemoveableAppFlag(scanFile, packageName);
    }

    public boolean needAddUpdatedRemoveableAppFlag(String packageName) {
        if (packageName == null) {
            return false;
        }
        return HwPreAppManager.getInstance(this).needAddUpdatedRemoveableAppFlag(packageName);
    }

    public boolean isUnAppInstallAllowed(String originPath) {
        HwCustPackageManagerServiceEx mCustPackageManagerService;
        if (originPath == null || (mCustPackageManagerService = this.mPms.getHwPMSCustPackageManagerService()) == null || !mCustPackageManagerService.isUnAppInstallAllowed(originPath, this.mContext)) {
            return false;
        }
        return true;
    }

    public boolean isPrivAppNonSystemPartitionDir(File path) {
        if (path == null) {
            return false;
        }
        return HwPreAppManager.getInstance(this).isPrivAppNonSystemPartitionDir(path);
    }

    public void scanNonSystemPartitionDir(int scanMode) {
        long startTime = HwPackageManagerServiceUtilsEx.hwTimingsBegin();
        HwPreAppManager.getInstance(this).scanNonSystemPartitionDir(scanMode);
        HwPackageManagerServiceUtilsEx.hwTimingsEnd(TAG, "scanNonSystemPartitionDir", startTime);
    }

    public void scanNoSysAppInNonSystemPartitionDir(int scanMode) {
        long startTime = HwPackageManagerServiceUtilsEx.hwTimingsBegin();
        HwPreAppManager.getInstance(this).scanNoSysAppInNonSystemPartitionDir(scanMode);
        HwPackageManagerServiceUtilsEx.hwTimingsEnd(TAG, "scanNoSysAppInNonSystemPartitionDir", startTime);
    }

    public void setHdbKey(String key) {
        HwAdbManagerEx.setHdbKey(key);
    }

    public void loadCorrectUninstallDelapp() {
        HwUninstalledAppManager.getInstance(this, this).loadCorrectUninstallDelapp();
    }

    public void addUninstallDataToCache(String packageName, String codePath) {
        HwUninstalledAppManager.getInstance(this, this).addUninstallDataToCache(packageName, codePath);
    }

    public boolean checkUninstalledSystemApp(PackageParserEx.PackageEx pkg, PackageManagerServiceEx.InstallArgsEx args, PackageManagerServiceEx.PackageInstalledInfoEx res) throws PackageManagerExceptionEx {
        return HwPreAppManager.getInstance(this).checkUninstalledSystemApp(pkg, args, res);
    }

    public PackageManagerServiceEx getIPmsInner() {
        return this.mPms;
    }

    public Map<String, String> getUninstalledMap() {
        return HwUninstalledAppManager.getInstance(this, this).getUninstalledMap();
    }

    public boolean pmInstallHwTheme(String themePath, boolean isSetWallpaper, int userId) {
        return HwThemeInstaller.getInstance(this.mContext).pmInstallHwTheme(themePath, isSetWallpaper, userId);
    }

    public void onUserRemoved(int userId) {
        HwThemeInstaller.getInstance(this.mContext).onUserRemoved(userId);
    }

    public boolean isAppInstallAllowed(String appName) {
        return !isInMspesForbidInstallPackageList(appName);
    }

    private void checkAndEnableWebview() {
        if (this.mPms.isUpgrade() && this.mPms != null) {
            boolean isChina = "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", BuildConfig.FLAVOR));
            try {
                int state = this.mPms.getApplicationEnabledSetting("com.google.android.webview", ContextEx.getUserId(this.mContext));
                boolean isEnabled = true;
                if (state != 1) {
                    isEnabled = false;
                }
                SlogEx.i(TAG, "WebViewGoogle state = " + state + " version is china = " + isChina);
                if (!isEnabled && isChina) {
                    SlogEx.i(TAG, "current WebViewGoogle disable, enable it");
                    this.mPms.setApplicationEnabledSetting("com.google.android.webview", 1, 0, ContextEx.getUserId(this.mContext), ContextEx.getOpPackageName(this.mContext));
                }
            } catch (Exception e) {
                SlogEx.w(TAG, "checkAndEnableWebview, enable WebViewGoogle exception");
            }
        }
    }

    public boolean verifyPackageSecurityPolicy(String packageName, File baseApkFile) {
        ISecurityProfileController spc = HwServiceFactoryEx.getSecurityProfileController();
        if (spc == null || spc.verifyPackage(packageName, baseApkFile)) {
            return true;
        }
        SlogEx.e(TAG, "Security policy verification failed");
        return false;
    }

    public void reportEventStream(int eventId, String message) {
        if (eventId != HwPackageManagerServiceUtilsEx.EVENT_UNINSTALLED_APPLICATION && !TextUtils.isEmpty(message)) {
            IMonitor.EventStream eventStream = IMonitor.openEventStream(eventId);
            if (eventStream != null) {
                eventStream.setParam("message", message);
                IMonitor.sendEvent(eventStream);
            }
            IMonitor.closeEventStream(eventStream);
        }
    }

    public void deleteExistsIfNeeded() {
    }

    public ArrayList<String> getDataApkShouldNotUpdateByCota() {
        return this.mShouldNotUpdateByCotaDataApks;
    }

    /* access modifiers changed from: private */
    public class MySysAppInfo {
        private boolean isPrivileged;
        private boolean isUndetachable;
        private String pkgName;
        private String pkgSignature;

        MySysAppInfo(String name, String sign, String priv, String del) {
            this.pkgName = name;
            this.pkgSignature = sign;
            if ("true".equals(priv)) {
                this.isPrivileged = true;
            } else {
                this.isPrivileged = false;
            }
            if ("true".equals(del)) {
                this.isUndetachable = true;
            } else {
                this.isUndetachable = false;
            }
        }

        /* access modifiers changed from: package-private */
        public String getPkgName() {
            return this.pkgName;
        }

        /* access modifiers changed from: package-private */
        public void setPkgName(String value) {
            this.pkgName = value;
        }

        /* access modifiers changed from: package-private */
        public String getPkgSignature() {
            return this.pkgSignature;
        }

        /* access modifiers changed from: package-private */
        public void setPkgSignature(String value) {
            this.pkgSignature = value;
        }

        /* access modifiers changed from: package-private */
        public boolean getPrivileged() {
            return this.isPrivileged;
        }

        /* access modifiers changed from: package-private */
        public void setPrivileged(boolean isPvg) {
            this.isPrivileged = isPvg;
        }

        /* access modifiers changed from: package-private */
        public boolean getUndetachable() {
            return this.isUndetachable;
        }

        /* access modifiers changed from: package-private */
        public void setUndetachable(boolean isUndetachabled) {
            this.isUndetachable = isUndetachabled;
        }
    }

    public String readMspesFile(String fileName) {
        return MspesExUtilEx.getInstance(this).readMspesFile(fileName);
    }

    public boolean writeMspesFile(String fileName, String content) {
        return MspesExUtilEx.getInstance(this).writeMspesFile(fileName, content);
    }

    public String getMspesOEMConfig() {
        return MspesExUtilEx.getInstance(this).getMspesOEMConfig();
    }

    public int updateMspesOEMConfig(String src) {
        return MspesExUtilEx.getInstance(this).updateMspesOEMConfig(src);
    }

    public boolean isInMspesForbidInstallPackageList(String pkgName) {
        return MspesExUtilEx.getInstance(this).isInMspesForbidInstallPackageList(pkgName);
    }

    public void preSendPackageBroadcast(String action, String pkg, String targetPkg) {
        HwServiceSecurityPartsFactoryEx.getInstance().getHwAppAuthManager().preSendPackageBroadcast(action, pkg, targetPkg);
    }

    public List<String> getSystemWhiteList(String type) {
        if (type == null) {
            return Collections.emptyList();
        }
        this.mContext.enforceCallingOrSelfPermission(ACCESS_SYSTEM_WHITE_LIST, "Permission Denied for getSystemWhiteList!");
        if (FREE_FORM_LIST.equals(type)) {
            return SplitNotificationUtilsEx.getInstance(this.mContext).getListPkgName(3);
        }
        return Collections.emptyList();
    }

    public boolean shouldSkipTriggerFreeform(String pkgName, int userId) {
        this.mContext.enforceCallingOrSelfPermission(SKIP_TRIGGER_FREEFORM, "Permission Denied for shouldSkipTriggerFreeform!");
        return SplitNotificationUtilsEx.getInstance(this.mContext).shouldSkipTriggerFreeform(pkgName, userId);
    }

    public int getPrivilegeAppType(String pkgName) {
        return checkInstallGranted(pkgName) ? 1 : 0;
    }

    public void addGrantedInstalledPkg(PackageParserEx.PackageEx pkg, boolean isGranted) {
        if (pkg == null) {
            SlogEx.i(TAG, "addGrantedInstalledPkg package is null");
            return;
        }
        String pkgName = pkg.getPackageName();
        if (TextUtils.isEmpty(pkgName)) {
            SlogEx.i(TAG, "addGrantedInstalledPkg packageName is null");
        } else if (!isGranted) {
            SlogEx.i(TAG, "addGrantedInstalledPkg not granted: " + pkgName);
        } else {
            synchronized (this.mGrantedInstalledPkgs) {
                SlogEx.i(TAG, "addGrantedInstalledPkg package added: " + pkgName);
                this.mGrantedInstalledPkgs.add(pkgName);
            }
        }
    }

    private boolean checkInstallGranted(String pkgName) {
        boolean contains;
        synchronized (this.mGrantedInstalledPkgs) {
            contains = this.mGrantedInstalledPkgs.contains(pkgName);
        }
        return contains;
    }

    public void clearPreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        boolean isActivityMatch;
        int callingUid = Binder.getCallingUid();
        SlogEx.d(TAG, "clearPreferredActivity " + activity + " for user " + userId + " from uid " + callingUid);
        this.mPms.getPermissionManager().enforceCrossUserPermission(callingUid, userId, true, false, "clear preferred activity");
        if (this.mContext.checkCallingOrSelfPermission("android.permission.SET_PREFERRED_APPLICATIONS") != 0) {
            synchronized (this.mPms.getPackagesLockObject()) {
                if (this.mPms.getUidTargetSdkVersionLockedLPrEx(callingUid) < 8) {
                    SlogEx.w(TAG, "Ignoring clearPreferredActivity() from uid " + Binder.getCallingUid());
                    return;
                }
            }
            this.mContext.enforceCallingOrSelfPermission("android.permission.SET_PREFERRED_APPLICATIONS", null);
        }
        if (filter != null && filter.countCategories() == 0) {
            filter.addCategory("android.intent.category.DEFAULT");
        }
        synchronized (this.mPms.getPackagesLockObject()) {
            PreferredIntentResolverEx pir = (PreferredIntentResolverEx) this.mPms.getSettings().getPreferredActivities().get(userId);
            if (pir != null) {
                ArrayList<PreferredActivityEx> existingEntries = getExistingEntries(pir, filter);
                if (existingEntries != null) {
                    if (!existingEntries.isEmpty()) {
                        for (int i = existingEntries.size() - 1; i >= 0; i--) {
                            PreferredActivityEx pa = existingEntries.get(i);
                            boolean isPrefMatch = false;
                            if (activity != null) {
                                if (!activity.equals(pa.getPref().getComponent())) {
                                    isActivityMatch = false;
                                    if (match == 0 || pa.getPref().getMatch() == (match & 268369920)) {
                                        isPrefMatch = true;
                                    }
                                    if (pa.getPref().getAlways() && isActivityMatch && isPrefMatch) {
                                        pir.removeFilter(pa);
                                    }
                                }
                            }
                            isActivityMatch = true;
                            isPrefMatch = true;
                            pir.removeFilter(pa);
                        }
                        this.mPms.writePackageRestrictions(userId);
                        this.mPms.sendPreferredActivityChangedBroadcast(userId);
                    }
                }
            }
        }
    }

    private ArrayList<PreferredActivityEx> getExistingEntries(PreferredIntentResolverEx pir, IntentFilter filter) {
        ArrayList<PreferredActivityEx> existingEntries = new ArrayList<>();
        for (IntentFilter intentFilter : rebuildFilter(filter)) {
            ArrayList<PreferredActivityEx> tmpEntries = pir.findFilters(intentFilter);
            if (tmpEntries != null) {
                existingEntries.addAll(tmpEntries);
            }
        }
        return existingEntries;
    }

    private List<IntentFilter> rebuildFilter(IntentFilter filter) {
        IntentFilter tmpFilter;
        Iterator<String> iter;
        List<IntentFilter> filterList = new ArrayList<>();
        filterList.add(filter);
        if (filter.countDataSchemes() == 0) {
            IntentFilter contentFilter = new IntentFilter(filter);
            contentFilter.addDataScheme("content");
            filterList.add(contentFilter);
            IntentFilter fileFilter = new IntentFilter(filter);
            fileFilter.addDataScheme("file");
            filterList.add(fileFilter);
            return filterList;
        } else if (filter.countDataSchemes() != 1 || ((!filter.hasDataScheme("content") && !filter.hasDataScheme("file")) || (iter = (tmpFilter = new IntentFilter(filter)).schemesIterator()) == null)) {
            return filterList;
        } else {
            while (iter.hasNext()) {
                if (!TextUtils.isEmpty(iter.next())) {
                    iter.remove();
                }
            }
            filterList.add(tmpFilter);
            return filterList;
        }
    }

    public void registerExtServiceProvider(IExtServiceProviderEx extServiceProvider, Intent filter) {
        if (this.mExtServiceProvider == null) {
            this.mExtServiceProvider = new ExtServiceProviderEx();
        }
        this.mExtServiceProvider.registerExtServiceProvider(extServiceProvider, filter);
    }

    public void unregisterExtServiceProvider(IExtServiceProviderEx extServiceProvider) {
        ExtServiceProviderEx extServiceProviderEx = this.mExtServiceProvider;
        if (extServiceProviderEx != null) {
            extServiceProviderEx.unregisterExtServiceProvider(extServiceProvider);
        }
    }

    public ResolveInfoEx[] queryExtServiceEx(String action, String packageName) {
        if (this.mExtServiceProvider == null) {
            this.mExtServiceProvider = new ExtServiceProviderEx();
        }
        return this.mExtServiceProvider.queryExtService(action, packageName);
    }

    public boolean isInValidApkPatchFile(File file, int parseFlags) {
        boolean isInValidApk = false;
        if (file == null) {
            return false;
        }
        try {
            PackageParserEx.PackageEx ppkg = new android.content.pm.PackageParserEx().parsePackage(file, parseFlags);
            if (ppkg.getAppMetaData() == null || !ppkg.getAppMetaData().getBoolean(FLAG_APKPATCH_TAG, false)) {
                return false;
            }
            if (!checkAllowtoInstallPatchApk(ppkg.getPackageName())) {
                isInValidApk = true;
            }
            SlogEx.i(TAG, "isInvalidApkPatchFile with file " + isInValidApk);
            return isInValidApk;
        } catch (PackageParserEx.PackageParserExceptionEx e) {
            SlogEx.w(TAG, "failed to parse " + file);
            return false;
        }
    }

    public boolean isInValidApkPatchPkg(PackageParserEx.PackageEx pkg) {
        if (pkg == null || TextUtils.isEmpty(pkg.getPackageName()) || pkg.getAppMetaData() == null || !pkg.getAppMetaData().getBoolean(FLAG_APKPATCH_TAG, false)) {
            return false;
        }
        boolean isInValidApk = !checkAllowtoInstallPatchApk(pkg.getPackageName());
        SlogEx.i(TAG, "isInvalidApkPatch with pkg " + isInValidApk);
        return isInValidApk;
    }

    private boolean checkAllowtoInstallPatchApk(String srcPkgName) {
        File fileForParse = new File(FLAG_APKPATCH_PATH);
        InputStream is = null;
        boolean isLegalPatchPkg = false;
        if (TextUtils.isEmpty(srcPkgName) || !fileForParse.exists()) {
            return false;
        }
        try {
            is = new FileInputStream(fileForParse);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(is, null);
            XmlUtilsEx.beginDocument(parser, FLAG_APKPATCH_TAGPATCH);
            while (true) {
                XmlUtilsEx.nextElement(parser);
                String element = parser.getName();
                if (element != null) {
                    String packageName = XmlUtilsEx.readStringAttribute(parser, FLAG_VALUE);
                    if (FLAG_APKPATCH_PKGNAME.equals(element) && srcPkgName.equalsIgnoreCase(packageName)) {
                        SlogEx.d(TAG, "this is legal apk");
                        isLegalPatchPkg = true;
                        break;
                    }
                    isLegalPatchPkg = false;
                } else {
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            SlogEx.e(TAG, "patch info parse fail");
        } catch (IOException | XmlPullParserException e2) {
            SlogEx.e(TAG, "patch info parse fail & io fail");
        } catch (Throwable th) {
            IoUtilsEx.closeQuietly((AutoCloseable) null);
            throw th;
        }
        IoUtilsEx.closeQuietly(is);
        return isLegalPatchPkg;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0043  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00ca  */
    public boolean setForceDarkSetting(List<String> packageNames, int forceDarkMode) {
        List<String> pkgNames;
        boolean isChanged;
        int uid = Binder.getCallingUid();
        if (UserHandleEx.getAppId(uid) == 1000 || uid == 0) {
            long callingId = Binder.clearCallingIdentity();
            boolean isSetAll = false;
            try {
                synchronized (this.mPms.getPackagesLockObject()) {
                    if (packageNames != null) {
                        if (packageNames.size() != 0) {
                            pkgNames = packageNames;
                            isChanged = false;
                            for (String packageName : pkgNames) {
                                PackageSettingEx pkgSetting = (PackageSettingEx) this.mPms.getSettings().getPackages().get(packageName);
                                if (!(pkgSetting == null || pkgSetting.getPkg() == null)) {
                                    if (pkgSetting.getPkg().getApplicationInfo() != null) {
                                        int realMode = pkgSetting.getForceDarkMode() & -129;
                                        if (IS_DEBUG) {
                                            SlogEx.d(TAG, "setForceDarkSetting packageName: " + packageName + ", realMode: " + realMode + ", mode: " + forceDarkMode);
                                        }
                                        if (realMode != forceDarkMode) {
                                            isChanged = true;
                                            pkgSetting.setForceDarkMode(forceDarkMode | APP_FORCE_DARK_USER_SET_FLAG);
                                            ApplicationInfoExt.setForceDarkMode(pkgSetting.getPkg().getApplicationInfo(), forceDarkMode);
                                        }
                                    }
                                }
                                SlogEx.d(TAG, "setForceDarkSetting pkgSetting is null for packageName: " + packageName);
                            }
                            if (isChanged) {
                                this.mPms.getSettings().writeLPr();
                            }
                        }
                    }
                    pkgNames = getAllSupportForceDarkApps();
                    isSetAll = true;
                    isChanged = false;
                    while (r9.hasNext()) {
                    }
                    if (isChanged) {
                    }
                }
                updateOrStopPackage(isSetAll, pkgNames);
                return true;
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        } else {
            throw new SecurityException("Only the system can set app force dark mode");
        }
    }

    public int getForceDarkSetting(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return 2;
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            String pkgName = packageName;
            if (packageName.endsWith(HwForceDarkModeConfigEx.FORCE_DARK_PKGNAME_BLACK_LIST_SUFFIX)) {
                pkgName = pkgName.substring(0, pkgName.toLowerCase(Locale.ROOT).lastIndexOf(HwForceDarkModeConfigEx.FORCE_DARK_PKGNAME_BLACK_LIST_SUFFIX));
                if (HwForceDarkModeConfigEx.checkForceDark3rdBlackListFromAppTypeRecoManager(pkgName)) {
                    return HwForceDarkModeConfigEx.FORCE_DARK_IN_3RD_BLACK_LIST;
                }
            }
            synchronized (this.mPms.getPackagesLockObject()) {
                PackageSettingEx pkgSetting = (PackageSettingEx) this.mPms.getSettings().getPackages().get(pkgName);
                if (pkgSetting != null) {
                    if (!pkgSetting.isObjNull()) {
                        int forceDarkMode = pkgSetting.getForceDarkMode() & -129;
                        Binder.restoreCallingIdentity(callingId);
                        return forceDarkMode;
                    }
                }
                SlogEx.d(TAG, "getForceDarkSetting pkgSetting is null for packageName: " + pkgName);
                Binder.restoreCallingIdentity(callingId);
                return 2;
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void updateForceDarkMode(String pkgName, PackageSettingEx ps) {
        if (ps == null || ps.isObjNull() || pkgName == null) {
            SlogEx.d(TAG, "updateForceDarkMode ps is null for pkgName: " + pkgName);
            return;
        }
        ps.setForceDarkMode(HwForceDarkModeConfigEx.getForceDarkModeFromAppTypeRecoManager(pkgName, ps));
    }

    private List<String> getAllSupportForceDarkApps() {
        List<String> packageNames = new ArrayList<>();
        for (String pkgName : this.mPms.getSettings().getPackages().keySet()) {
            if (getForceDarkSetting(pkgName) != 2) {
                packageNames.add(pkgName);
            }
        }
        return packageNames;
    }

    private void updateOrStopPackage(boolean isSetAll, List<String> packageNames) {
        if (isSetAll) {
            updateConfiguration();
            return;
        }
        for (String packageName : packageNames) {
            forceStopPackage(packageName);
        }
    }

    private void forceStopPackage(String packageName) {
        try {
            ActivityManagerNativeEx.forceStopPackage(packageName, -1);
        } catch (RemoteException e) {
            SlogEx.e(TAG, "Failed to force stop package of " + packageName + " for RemoteException.");
        } catch (SecurityException e2) {
            SlogEx.e(TAG, "forceStopPackage permission denial, requires FORCE_STOP_PACKAGES");
        }
    }

    private void updateConfiguration() {
        HwThemeManager.IHwThemeManager manager = HwFrameworkFactoryEx.getThemeManagerInstance();
        if (manager != null) {
            manager.updateConfiguration();
        } else {
            SlogEx.e(TAG, "updateConfiguration manager is null");
        }
    }

    public void updateWhitelistByHot() {
        new WhitelistUpdateThread(this.mContext, "/data/cota/para/xml/HwExtDisplay/fold", "hw_tahiti_app_aspect_list.xml", new HotUpdateRunnable(HwPackageParser.getTahitiAppAspectList(), HwPackageParser.getTahitiAppVersionCodeList())).start();
    }

    private class HotUpdateRunnable implements Runnable {
        private static final double COMPARISON = 1.0E-8d;
        Map<String, Float> mAppAspects = null;
        Map<String, Integer> mAppVersionCodes = null;

        protected HotUpdateRunnable(Map<String, Float> appAspects, Map<String, Integer> appVersionCodes) {
            this.mAppAspects = appAspects;
            this.mAppVersionCodes = appVersionCodes;
        }

        @Override // java.lang.Runnable
        public void run() {
            HwPackageParser.initTahitiAppAspectList();
            Map<String, Float> appAspectsAfterUpdate = HwPackageParser.getTahitiAppAspectList();
            Map<String, Integer> appVersionCodesAfterUpdate = HwPackageParser.getTahitiAppVersionCodeList();
            Set<String> pkgs = new HashSet<>();
            pkgs.addAll(this.mAppAspects.keySet());
            pkgs.addAll(appAspectsAfterUpdate.keySet());
            new android.content.pm.PackageParserEx();
            List<String> pkgsToStops = getPkgsToStops(getPkgsChanges(pkgs, appAspectsAfterUpdate, appVersionCodesAfterUpdate), appAspectsAfterUpdate, appVersionCodesAfterUpdate);
            boolean isShouldUpdate = false;
            if (pkgsToStops != null && pkgsToStops.size() > 0) {
                isShouldUpdate = true;
            }
            if (isShouldUpdate) {
                synchronized (HwPackageManagerServiceEx.this.getIPmsInner().getPackagesLock()) {
                    HwPackageManagerServiceEx.this.getIPmsInner().getSettings().writeLPr();
                }
                if (!HwPackageParser.getIsNeedBootUpdate()) {
                    HwActivityManager.forceStopPackages(pkgsToStops, -1);
                }
            }
            SlogEx.i(HwPackageManagerServiceEx.TAG, "hot update finish!");
        }

        private Set<String> getPkgsChanges(Set<String> pkgs, Map<String, Float> appAspectsAfterUpdate, Map<String, Integer> appVersionCodesAfterUpdate) {
            Set<String> pkgsChanges = new HashSet<>();
            for (String pkg : pkgs) {
                if (!TextUtils.isEmpty(pkg)) {
                    Float before = this.mAppAspects.get(pkg);
                    Float after = appAspectsAfterUpdate.get(pkg);
                    int newCode = 0;
                    int oldCode = 0;
                    if (appVersionCodesAfterUpdate != null && appVersionCodesAfterUpdate.containsKey(pkg)) {
                        newCode = appVersionCodesAfterUpdate.get(pkg).intValue();
                    }
                    Map<String, Integer> map = this.mAppVersionCodes;
                    if (map != null && map.containsKey(pkg)) {
                        oldCode = this.mAppVersionCodes.get(pkg).intValue();
                    }
                    if (before == null || after == null || ((double) Math.abs(before.floatValue() - after.floatValue())) >= COMPARISON || newCode != oldCode) {
                        SlogEx.i(HwPackageManagerServiceEx.TAG, "hot update changed pkg: " + pkg);
                        pkgsChanges.add(pkg);
                    }
                }
            }
            return pkgsChanges;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:65:0x0196, code lost:
            r2 = r20;
            r0 = r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:69:0x01b7, code lost:
            r2 = r20;
            r0 = r16;
         */
        private List<String> getPkgsToStops(Set<String> pkgsChanges, Map<String, Float> appAspectsAfterUpdate, Map<String, Integer> appVersionCodesAfterUpdate) {
            Iterator<String> it;
            boolean isUserSetPkg;
            Float after;
            int newCode;
            Map<String, Integer> map = appVersionCodesAfterUpdate;
            List<String> pkgsToStops = new ArrayList<>();
            Iterator<String> it2 = pkgsChanges.iterator();
            while (it2.hasNext()) {
                String pkg = it2.next();
                if (!TextUtils.isEmpty(pkg)) {
                    synchronized (HwPackageManagerServiceEx.this.getIPmsInner().getPackagesLock()) {
                        try {
                            PackageSettingEx pkgSetting = (PackageSettingEx) HwPackageManagerServiceEx.this.getIPmsInner().getSettings().getPackages().get(pkg);
                            if (pkgSetting == null) {
                                it = it2;
                            } else if (pkgSetting.isObjNull()) {
                                it = it2;
                            } else {
                                Float pkgSettingSet = Float.valueOf(pkgSetting.getAspectRatio("minAspectRatio"));
                                Float before = this.mAppAspects.get(pkg);
                                int oldCode = 0;
                                if (this.mAppVersionCodes != null && this.mAppVersionCodes.containsKey(pkg)) {
                                    oldCode = this.mAppVersionCodes.get(pkg).intValue();
                                }
                                if (before != null && isAspectValid(pkgSetting.getPkg().getVersionCode(), oldCode)) {
                                    if (((double) Math.abs(before.floatValue() - pkgSettingSet.floatValue())) < COMPARISON) {
                                        isUserSetPkg = false;
                                        if ((pkgSettingSet.floatValue() != GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || !isUserSetPkg) && ApplicationInfoExt.canChangeAspectRatio(pkgSetting.getPkg().getApplicationInfo(), "minAspectRatio")) {
                                            after = appAspectsAfterUpdate.get(pkg);
                                            newCode = 0;
                                            if (map != null && map.containsKey(pkg)) {
                                                newCode = map.get(pkg).intValue();
                                            }
                                            int currentCode = pkgSetting.getPkg().getVersionCode();
                                            SlogEx.i(HwPackageManagerServiceEx.TAG, "hot update pkg: " + pkg + "code: " + currentCode + " old minAspect: " + before + " code: " + oldCode + " new minAspect: " + after + " code: " + newCode);
                                            if ((before != null || !isAspectValid(currentCode, oldCode)) && after != null && isAspectValid(currentCode, newCode)) {
                                                pkgSetting.setAspectRatio("minAspectRatio", after.floatValue());
                                            } else if (before != null && isAspectValid(currentCode, oldCode) && (after == null || !isAspectValid(currentCode, newCode))) {
                                                setPkgAspectRatio(pkgSetting);
                                            } else if (before == null || !isAspectValid(currentCode, oldCode) || after == null || !isAspectValid(currentCode, newCode)) {
                                                SlogEx.i(HwPackageManagerServiceEx.TAG, "hot update pkg: do nothing!");
                                            } else {
                                                pkgSetting.setAspectRatio("minAspectRatio", after.floatValue());
                                            }
                                            pkgsToStops.add(pkg);
                                        } else {
                                            SlogEx.i(HwPackageManagerServiceEx.TAG, "hot update skip " + pkg + " for reason user set or can not change");
                                        }
                                    }
                                }
                                isUserSetPkg = true;
                                if (pkgSettingSet.floatValue() != GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                                }
                                after = appAspectsAfterUpdate.get(pkg);
                                newCode = 0;
                                newCode = map.get(pkg).intValue();
                                int currentCode2 = pkgSetting.getPkg().getVersionCode();
                                SlogEx.i(HwPackageManagerServiceEx.TAG, "hot update pkg: " + pkg + "code: " + currentCode2 + " old minAspect: " + before + " code: " + oldCode + " new minAspect: " + after + " code: " + newCode);
                                if (before != null) {
                                }
                                pkgSetting.setAspectRatio("minAspectRatio", after.floatValue());
                                pkgsToStops.add(pkg);
                            }
                            SlogEx.i(HwPackageManagerServiceEx.TAG, "hot update but not installed package " + pkg);
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                }
            }
            return pkgsToStops;
        }

        private void setPkgAspectRatio(PackageSettingEx pkgSetting) {
            Float tempAspect = Float.valueOf(HwFoldScreenState.getScreenFoldFullRatio());
            Iterator it = pkgSetting.getPkg().getActivities().iterator();
            while (true) {
                if (it.hasNext()) {
                    if (!((PackageParserEx.ActivityEx) it.next()).isResizeable()) {
                        tempAspect = Float.valueOf(1.3333334f);
                        break;
                    }
                } else {
                    break;
                }
            }
            pkgSetting.setAspectRatio("minAspectRatio", tempAspect.floatValue());
        }

        private boolean isAspectValid(int apkCode, int maxVaildCode) {
            if (maxVaildCode == 0 || apkCode < maxVaildCode) {
                return true;
            }
            return false;
        }
    }

    private static class WhitelistUpdateThread extends Thread {
        Context mContext = null;
        String mFileName = null;
        String mFilePath = null;
        Runnable mRunnable = null;

        protected WhitelistUpdateThread(Context context, String filePath, String fileName, Runnable runnable) {
            super("config update thread");
            this.mContext = context;
            this.mFilePath = filePath;
            this.mFileName = fileName;
            this.mRunnable = runnable;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Runnable runnable;
            if (!TextUtils.isEmpty(this.mFilePath) && !TextUtils.isEmpty(this.mFileName)) {
                FileInputStream inputStream = null;
                try {
                    File file = new File(this.mFilePath, this.mFileName);
                    if (file.exists()) {
                        inputStream = new FileInputStream(file);
                    }
                    File targetFileTemp = createFileForWrite(this.mFileName);
                    if (!(targetFileTemp == null || inputStream == null)) {
                        parseConfigsToTargetFile(targetFileTemp, inputStream);
                    }
                    closeInputStream(inputStream);
                    runnable = this.mRunnable;
                    if (runnable == null) {
                        return;
                    }
                } catch (FileNotFoundException e) {
                    Log.e(HwPackageManagerServiceEx.TAG, "Can not found whitelist");
                    closeInputStream(null);
                    runnable = this.mRunnable;
                    if (runnable == null) {
                        return;
                    }
                } catch (Throwable th) {
                    closeInputStream(null);
                    Runnable runnable2 = this.mRunnable;
                    if (runnable2 != null) {
                        runnable2.run();
                    }
                    throw th;
                }
                runnable.run();
            }
        }

        private static File createFileForWrite(String fileName) {
            File file = new File(EnvironmentEx.getDataSystemDirectory(), fileName);
            if (!file.exists() || file.delete()) {
                try {
                    if (!file.createNewFile()) {
                        Log.e(HwPackageManagerServiceEx.TAG, "createFileForWrite createNewFile error!");
                        return null;
                    }
                    file.setReadable(true, false);
                    return file;
                } catch (IOException e) {
                    Log.e(HwPackageManagerServiceEx.TAG, "createFileForWrite IoException.");
                    return null;
                }
            } else {
                Log.e(HwPackageManagerServiceEx.TAG, "delete file error!");
                return null;
            }
        }

        private void parseConfigsToTargetFile(File targetFile, FileInputStream inputStream) {
            BufferedReader reader = null;
            FileOutputStream outputStream = null;
            InputStreamReader inputStreamReader = null;
            StringBuilder targetStringBuilder = new StringBuilder();
            boolean isRecordStarted = true;
            try {
                inputStreamReader = new InputStreamReader(inputStream, HwPackageManagerServiceEx.UTF_8);
                reader = new BufferedReader(inputStreamReader);
                while (true) {
                    String tempLineString = reader.readLine();
                    if (tempLineString == null) {
                        break;
                    }
                    String tempLineString2 = tempLineString.trim();
                    if (isRecordStarted) {
                        targetStringBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                        isRecordStarted = false;
                    } else {
                        targetStringBuilder.append(System.lineSeparator());
                        targetStringBuilder.append(tempLineString2);
                    }
                }
                outputStream = new FileOutputStream(targetFile);
                byte[] outputStrings = targetStringBuilder.toString().getBytes(HwPackageManagerServiceEx.UTF_8);
                outputStream.write(outputStrings, 0, outputStrings.length);
            } catch (IOException e) {
                deleteAbnormalXml(targetFile);
                Log.e(HwPackageManagerServiceEx.TAG, "parseConfigsToTargetFile IOException");
            } catch (RuntimeException e2) {
                deleteAbnormalXml(targetFile);
                Log.e(HwPackageManagerServiceEx.TAG, "parseConfigsToTargetFile RuntimeException");
            } catch (Throwable th) {
                closeBufferedReader(null);
                closeInputStreamReader(null);
                closeFileOutputStream(null);
                throw th;
            }
            closeBufferedReader(reader);
            closeInputStreamReader(inputStreamReader);
            closeFileOutputStream(outputStream);
        }

        private void deleteAbnormalXml(File file) {
            if (file.exists() && !file.delete()) {
                Log.e(HwPackageManagerServiceEx.TAG, "delete abnormal xml error!");
            }
        }

        private void closeBufferedReader(BufferedReader bufferedReader) {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(HwPackageManagerServiceEx.TAG, "closeBufferedReader error!");
                }
            }
        }

        private void closeInputStreamReader(InputStreamReader inputStreamReader) {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    Log.e(HwPackageManagerServiceEx.TAG, "closeInputStreamReader error!");
                }
            }
        }

        private void closeInputStream(InputStream inputStream) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(HwPackageManagerServiceEx.TAG, "closeInputStream error!");
                }
            }
        }

        private void closeFileOutputStream(FileOutputStream fileOutputStream) {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.e(HwPackageManagerServiceEx.TAG, "closeFileOutputStream error!");
                }
            }
        }
    }

    public void restoreHwLauncherMode(int[] allUserHandles) {
        SlogEx.i(TAG, "restore hw launcher mode");
        if (allUserHandles != null) {
            List<ComponentName> componentList = getHwLauncherHomeActivities();
            if (componentList.size() != 0) {
                for (int userId : allUserHandles) {
                    for (ComponentName component : componentList) {
                        this.mPms.setComponentEnabledSetting(component, 2, 0, userId);
                    }
                    String hwLauncherMode = getHwLauncherMode(userId);
                    SlogEx.i(TAG, "hwLauncherMode: " + hwLauncherMode + ", userId: " + userId);
                    this.mPms.setComponentEnabledSetting(new ComponentName("com.huawei.android.launcher", hwLauncherMode), 1, 0, userId);
                }
            }
        }
    }

    public void revokePermissionsFromApp(String pkgName, List<String> permissionList) {
        SlogEx.d(TAG, "revokePermissionsFromApp " + pkgName + " from uid " + Binder.getCallingUid());
        if (pkgName == null || permissionList == null || permissionList.size() == 0) {
            SlogEx.e(TAG, "pkgName is null or permissionList is null");
            return;
        }
        PackageManagerServiceEx packageManagerServiceEx = this.mPms;
        if (packageManagerServiceEx == null || packageManagerServiceEx.getPermissionCallbackEx() == null) {
            SlogEx.e(TAG, "mPermissionCallback is null");
        } else if (this.mPms.getSettings() != null && this.mPms.getSettings().getPackages() != null) {
            PackageSettingEx ps = (PackageSettingEx) this.mPms.getSettings().getPackages().get(pkgName);
            if (ps == null || ps.isObjNull()) {
                SlogEx.e(TAG, "ps is null");
                return;
            }
            PermissionsStateEx permissionsState = ps.getPermissionsState();
            PermissionManagerServiceInternalEx permissionManager = new PermissionManagerServiceInternalEx();
            for (String permName : permissionList) {
                if (permissionsState.hasInstallPermission(permName)) {
                    BasePermissionEx bp = permissionManager.getPermissionTEMP(permName);
                    if (bp == null) {
                        SlogEx.i(TAG, "Unknown permission " + permName + " in package " + pkgName);
                    } else {
                        permissionsState.revokeInstallPermission(bp);
                    }
                }
            }
            this.mPms.getPermissionCallbackEx().onInstallPermissionRevoked();
        }
    }

    private List<ComponentName> getHwLauncherHomeActivities() {
        List<ComponentName> componentList = new ArrayList<>();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setPackage("com.huawei.android.launcher");
        List<ResolveInfoEx> resolveInfoList = this.mPms.queryIntentActivitiesInternal(intent, (String) null, 786432, 0);
        if (!(resolveInfoList == null || resolveInfoList.size() == 0)) {
            for (ResolveInfoEx resolveInfo : resolveInfoList) {
                componentList.add(new ComponentName(resolveInfo.getActivityInfo().getPackageName(), resolveInfo.getActivityInfo().getName()));
            }
        }
        return componentList;
    }

    private String getHwLauncherMode(int userId) {
        if (SettingsEx.Systemex.getIntForUser(this.mContext.getContentResolver(), GestureNavConst.SIMPLE_MODE_DB_KEY, 0, userId) == 1) {
            return "com.huawei.android.launcher.newsimpleui.NewSimpleLauncher";
        }
        if (SettingsEx.Systemex.getIntForUser(this.mContext.getContentResolver(), "Simple mode", 0, userId) == 1) {
            return "com.huawei.android.launcher.simpleui.SimpleUILauncher";
        }
        if (SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), "launcher_record", 0, userId) == 4) {
            return "com.huawei.android.launcher.drawer.DrawerLauncher";
        }
        return "com.huawei.android.launcher.unihome.UniHomeLauncher";
    }

    public ComponentName getMdmDefaultLauncherEx(List<ResolveInfoEx> resolveInfos) {
        if (resolveInfos == null || resolveInfos.size() == 0) {
            return null;
        }
        ComponentName componentName = null;
        Bundle bundle = new HwDevicePolicyManagerEx().getCachedPolicyForFwk((ComponentName) null, "set-default-launcher", (Bundle) null);
        if (bundle != null) {
            componentName = ComponentName.unflattenFromString(bundle.getString(FLAG_VALUE));
            SlogEx.i(TAG, "componentName = " + componentName);
        }
        if (componentName == null) {
            return null;
        }
        String packageName = componentName.getPackageName();
        SlogEx.i(TAG, "packageName = " + packageName);
        for (ResolveInfoEx resolveInfo : resolveInfos) {
            if (resolveInfo.getActivityInfo() != null && TextUtils.equals(resolveInfo.getActivityInfo().getPackageName(), packageName)) {
                SlogEx.i(TAG, "return mdm default launcher componentname");
                return componentName;
            }
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0043, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0048, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0049, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004c, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x004f, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0054, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0055, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0058, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x005b, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0060, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0061, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0064, code lost:
        throw r4;
     */
    public ArrayList<String> getDelPackageList() {
        ArrayList<String> delPackageNameList = new ArrayList<>();
        File apkDeleteFile = new File(APK_DELETE_FILE);
        if (!apkDeleteFile.exists()) {
            return delPackageNameList;
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(apkDeleteFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    reader.close();
                    inputStreamReader.close();
                    fileInputStream.close();
                    break;
                }
                delPackageNameList.add(line.trim());
            }
        } catch (IOException e) {
            Log.e(TAG, "PackageManagerService.getDelPackageList error for IO");
        }
        return delPackageNameList;
    }

    public void putPreferredActivityInPcMode(int userId, IntentFilter filter, PreferredActivityEx preferredActivity) {
        HwResolverManagerEx.putPreferredActivityInPcMode(userId, filter, preferredActivity);
    }

    public boolean isFindPreferredActivityInCache(Intent intent, String resolvedType, int userId) {
        if (intent == null) {
            SlogEx.d(TAG, "isFindPreferredActivityInCache: intent is null.");
            return false;
        } else if (!HwResolverManager.getInstance().isMultiScreenCollaborationEnabled(this.mContext, (Intent) null) || !checkResolvedType(intent, resolvedType)) {
            return false;
        } else {
            boolean isSuccessOpen = HwResolverManager.getInstance().getOpenFileResult(intent) == 0;
            boolean isOnlyOnce = HwResolverManager.getInstance().isOnlyOncePreferredActivity(intent, resolvedType, userId);
            if (isSuccessOpen || isOnlyOnce) {
                return true;
            }
            return false;
        }
    }

    private boolean checkResolvedType(Intent intent, String resolvedType) {
        Uri data = intent.getData();
        if (resolvedType != null) {
            return true;
        }
        if (data == null) {
            return false;
        }
        if ("http".equals(data.getScheme()) || "https".equals(data.getScheme())) {
            return true;
        }
        return false;
    }

    public ResolveInfoEx findPreferredActivityInCacheEx(Intent intent, String resolvedType, int flags, List<ResolveInfoEx> query, int userId) {
        if (intent == null) {
            SlogEx.d(TAG, "findPreferredActivityInCache: intent is null.");
            return null;
        }
        String data = intent.getDataString();
        if (!"image/*".equals(resolvedType) || data == null || !data.startsWith("content://media/external_primary/")) {
            HwResolverManagerEx.preChooseBestActivity(intent, query, resolvedType, userId);
            return HwResolverManagerEx.findPreferredActivityInCache(this.mPms, intent, resolvedType, flags, query, userId);
        }
        SlogEx.d(TAG, "findPreferredActivityInCache: Record the screen or a touch pass.");
        return hwFindPreferredActivityEx(intent, query);
    }

    public boolean isMultiScreenCollaborationEnabled(Intent intent) {
        return HwResolverManager.getInstance().isMultiScreenCollaborationEnabled(this.mContext, intent);
    }

    public void filterResolveInfoEx(Intent intent, String resolvedType, List<ResolveInfoEx> resolveInfoList) {
        HwResolverManagerEx.filterResolveInfo(this.mContext, intent, UserHandleEx.getUserId(Binder.getCallingUid()), resolvedType, resolveInfoList);
    }

    public void setVersionMatchFlag(int deviceType, int version, boolean isMatchSuccess) {
        this.mContext.enforceCallingPermission(HW_PMS_SET_PCASSISTANT_RESULT, "Permission Denied for setVersionMatchFlag.");
        HwResolverManager.getInstance().setVersionMatchFlag(deviceType, version, isMatchSuccess);
    }

    public boolean getVersionMatchFlag(int deviceType, int version) {
        this.mContext.enforceCallingOrSelfPermission(HW_PMS_GET_PCASSISTANT_RESULT, "Permission Denied for getVersionMatchFlag.");
        return HwResolverManager.getInstance().getVersionMatchFlag(deviceType, version);
    }

    public void setOpenFileResult(Intent intent, int retCode) {
        this.mContext.enforceCallingPermission(HW_PMS_SET_PCASSISTANT_RESULT, "Permission Denied for setOpenFileResult.");
        HwResolverManager.getInstance().setOpenFileResult(intent, retCode);
    }

    public int getOpenFileResult(Intent intent) {
        this.mContext.enforceCallingOrSelfPermission(HW_PMS_GET_PCASSISTANT_RESULT, "Permission Denied for getOpenFileResult.");
        return HwResolverManager.getInstance().getOpenFileResult(intent);
    }

    private int checkSinglePermissionGrant(BasePermissionEx basePermission, String[] signingDetails, int protectionLevel) {
        if (basePermission.isNormal()) {
            return 1;
        }
        if (basePermission.isRuntime()) {
            return protectionLevel == 1 ? 0 : 2;
        }
        if (!basePermission.isSignature()) {
            return 2;
        }
        SignatureEx[] tmpSignings = basePermission.getSignatures();
        List<String> localHashes = new ArrayList<>();
        for (SignatureEx signature : tmpSignings) {
            localHashes.add(Base64.encodeToString(PackageUtilsEx.computeSha256DigestBytes(signature.toByteArray()), 0).trim());
        }
        if (signingDetails.length != tmpSignings.length) {
            return 2;
        }
        for (String signature2 : signingDetails) {
            if (!localHashes.contains(signature2)) {
                return 2;
            }
        }
        return 1;
    }

    private Bundle canGrantDPermission(String packageName, String[] permissionNames, String[] signingDetails, int[] protectionLevels) {
        Bundle resultBundle = new Bundle();
        resultBundle.putString(BUNDLE_PACKAGE, packageName);
        int[] resultPermissions = new int[permissionNames.length];
        for (int i = 0; i < permissionNames.length; i++) {
            BasePermissionEx basePermission = this.mPms.getPermissionManager().getPermissionTEMP(permissionNames[i]);
            if (basePermission == null) {
                resultPermissions[i] = 2;
                SlogEx.i(TAG, "canGrantDPermission, deny packageName " + packageName);
            } else {
                resultPermissions[i] = checkSinglePermissionGrant(basePermission, signingDetails, protectionLevels[i]);
            }
        }
        resultBundle.putIntArray("results", resultPermissions);
        return resultBundle;
    }

    private boolean isCanGrantDPermissionsCaller() {
        if (Binder.getCallingUid() == 1000) {
            return true;
        }
        return false;
    }

    public Bundle[] canGrantDPermissions(Bundle[] bundles) {
        Bundle[] invalidBundles = new Bundle[0];
        if (!isCanGrantDPermissionsCaller()) {
            SlogEx.e(TAG, "invalid invoke");
            return invalidBundles;
        } else if (bundles == null || bundles.length == 0 || bundles.length > 64) {
            SlogEx.e(TAG, "bundle invalid");
            return invalidBundles;
        } else {
            Bundle[] resultBundles = new Bundle[bundles.length];
            for (int i = 0; i < bundles.length; i++) {
                if (bundles[i] == null) {
                    SlogEx.e(TAG, "bundle[ " + i + " ] is null");
                    return invalidBundles;
                }
                String packageName = bundles[i].getString(BUNDLE_PACKAGE, BuildConfig.FLAVOR);
                String[] permissionNames = bundles[i].getStringArray("permission");
                String[] signingDigests = bundles[i].getStringArray("sign");
                int[] protectionLevels = bundles[i].getIntArray("protectionLevel");
                if (TextUtils.isEmpty(packageName) || permissionNames == null || signingDigests == null || protectionLevels == null) {
                    SlogEx.e(TAG, "packageName:" + packageName + " [ " + i + " ] exist null element");
                    return invalidBundles;
                } else if (permissionNames.length == 0 || permissionNames.length > 1024) {
                    SlogEx.e(TAG, "length of permissionNames is " + permissionNames.length);
                    return invalidBundles;
                } else if (signingDigests.length == 0 || signingDigests.length > 1024) {
                    SlogEx.e(TAG, "length of signingDigests is " + signingDigests.length);
                    return invalidBundles;
                } else if (protectionLevels.length == 0 || protectionLevels.length != permissionNames.length) {
                    SlogEx.e(TAG, "length of permissionNames is " + permissionNames.length + " protectionLevels.length " + protectionLevels.length);
                    return invalidBundles;
                } else {
                    resultBundles[i] = canGrantDPermission(packageName, permissionNames, signingDigests, protectionLevels);
                }
            }
            return resultBundles;
        }
    }

    public Optional<HwRenamedPackagePolicyEx> generateRenamedPackagePolicyLocked(PackageParserEx.PackageEx pkg) {
        Optional<HwRenamedPackagePolicyEx> renamedPackagePolicyOptional = HwRenamedPackagePolicyManagerEx.generateRenamedPackagePolicyLocked(pkg, this.mPms);
        if (!renamedPackagePolicyOptional.isPresent()) {
            return Optional.empty();
        }
        HwRenamedPackagePolicyEx renamedPackagePolicy = renamedPackagePolicyOptional.get();
        if (HwRenamedPackagePolicyManagerEx.addRenamedPackagePolicy(renamedPackagePolicy)) {
            return Optional.of(renamedPackagePolicy);
        }
        return Optional.empty();
    }

    public Map<String, String> getHwRenamedPackages(int flags) {
        List<HwRenamedPackagePolicyEx> renamedPackagePolicyList = HwRenamedPackagePolicyManagerEx.getRenamedPackagePolicy(flags);
        Map<String, String> renamedPackages = new HashMap<>();
        if (renamedPackagePolicyList == null || renamedPackagePolicyList.isEmpty()) {
            return renamedPackages;
        }
        for (HwRenamedPackagePolicyEx renamedPackagePolicy : renamedPackagePolicyList) {
            renamedPackages.put(renamedPackagePolicy.getOriginalPackageName(), renamedPackagePolicy.getNewPackageName());
        }
        return renamedPackages;
    }

    public Optional<HwRenamedPackagePolicyEx> getRenamedPackagePolicyByOriginalNameEx(String originalPackageName) {
        return HwRenamedPackagePolicyManagerEx.getRenamedPackagePolicyByOriginalName(originalPackageName);
    }

    public boolean migrateDataForRenamedPackageLocked(PackageParserEx.PackageEx pkg, int userId, int flags) {
        return HwRenamedPackagePolicyManagerEx.migrateDataForRenamedPackageLocked(pkg, userId, flags, this.mPms);
    }

    public boolean isOldPackageNameCanNotInstall(String packageName) {
        if (packageName == null) {
            return false;
        }
        Map<String, String> renamedPackages = new HashMap<>();
        renamedPackages.putAll(new HashMap<>(getHwRenamedPackages(HwRenamedPackagePolicyEx.EXCLUSIVE_INSTALL)));
        renamedPackages.putAll(new HashMap<>(HwUninstalledAppManager.getInstance(this, this).getUninstalledRenamedPackagesMaps()));
        if (renamedPackages.size() != 0 && renamedPackages.containsKey(packageName)) {
            return true;
        }
        return false;
    }

    public boolean migrateAppUninstalledState(String packageName) {
        if (packageName == null || !isNeedMigrateUninstallStatus(packageName)) {
            return false;
        }
        String oldPkgName = HwRenamedPackagePolicyManagerEx.getRenamedPackagePolicyByNewPackageName(packageName).get().getOriginalPackageName();
        PackageSettingEx oldPkgSetting = (PackageSettingEx) this.mPms.getSettings().getPackages().get(oldPkgName);
        if (oldPkgSetting == null || oldPkgSetting.isObjNull()) {
            this.mPms.deletePackageVersioned(new VersionedPackageEx(packageName, -1), new PackageManagerExt.LegacyPackageDeleteObserverEx((Object) null).getBinder(), 0, 2);
            SlogEx.i(TAG, "migrate uninstalled state, newPkgName: " + packageName + ", oldPkgName: " + oldPkgName + " for all users");
            HwUninstalledAppManager.getInstance(this, this).removeFromUninstalledDelapp(oldPkgName);
            return true;
        }
        for (UserInfoExt user : PackageManagerServiceEx.getUserManager().getUsers(false)) {
            if (!oldPkgSetting.getInstalled(user.getUserId())) {
                SlogEx.i(TAG, "migrate uninstalled state, newPkgName: " + packageName + ", oldPkgName: " + oldPkgName + ", userid: " + user.getUserId());
                this.mPms.setSystemAppInstallState(packageName, false, user.getUserId());
            }
        }
        HwUninstalledAppManager.getInstance(this, this).removeFromUninstalledDelapp(oldPkgName);
        return true;
    }

    private boolean isNeedMigrateUninstallStatus(String packageName) {
        Optional<HwRenamedPackagePolicyEx> policyOptional = HwRenamedPackagePolicyManagerEx.getRenamedPackagePolicyByNewPackageName(packageName);
        if (!policyOptional.isPresent()) {
            return false;
        }
        if (HwUninstalledAppManager.getInstance(this, this).getUninstalledMap().get(policyOptional.get().getOriginalPackageName()) != null) {
            SlogEx.i(TAG, "original package name app is uninstalled, need migrate the uninstall state for " + packageName);
            return true;
        }
        SlogEx.i(TAG, "original package name app is installed, don't need migrate the uninstall state for " + packageName);
        return false;
    }

    public List<ApplicationInfo> getClusterApplications(int flags, int clusterMask, boolean isOnlyDisabled, int userId) {
        if (IS_DEBUG) {
            SlogEx.d(TAG, "getClusterApplications flags:" + flags + ",clusterMask:" + clusterMask + ",isOnlyDisabled:" + isOnlyDisabled + ",userId:" + userId);
        }
        boolean isQueryPlugin = false;
        boolean isQueryBundle = (clusterMask & 1) != 0;
        if ((clusterMask & 2) != 0) {
            isQueryPlugin = true;
        }
        if (isQueryBundle || isQueryPlugin) {
            synchronized (this.mPms.getPackagesLockObject()) {
                ParceledListSliceEx applicationInfos = getAllApplicationList(flags, isOnlyDisabled, userId);
                if (applicationInfos == null) {
                    return Collections.emptyList();
                }
                List<ApplicationInfo> resultList = new ArrayList<>();
                for (ApplicationInfo applicationInfo : applicationInfos.getList()) {
                    if (!isFilterQueryClusterApplication(applicationInfo, isQueryBundle, isQueryPlugin)) {
                        resultList.add(applicationInfo);
                    }
                }
                if (IS_DEBUG) {
                    for (ApplicationInfo app : resultList) {
                        SlogEx.i(TAG, "getClusterApplications result:" + app.packageName + ",splitSize:" + app.splitNames.length);
                    }
                }
                return new ArrayList(resultList);
            }
        }
        SlogEx.e(TAG, "Invalid clusterMask!");
        return Collections.emptyList();
    }

    private ParceledListSliceEx getAllApplicationList(int flags, boolean isOnlyDisabled, int userId) {
        if (!isOnlyDisabled) {
            return this.mPms.getInstalledApplications(flags, userId);
        }
        List<ApplicationInfo> appInfos = new ArrayList<>();
        for (PackageSettingEx ps : this.mPms.getSettings().getDisabledSysPackages().values()) {
            PackageParserEx.PackageEx pkg = ps.getPkg();
            if (pkg != null) {
                ApplicationInfo app = PackageParserExUtils.generateApplicationInfo(pkg.getApplicationInfo(), flags, ps.readUserState(userId), userId);
                if (HwPackageManagerUtils.isDynamicApplication(app)) {
                    app.packageName = pkg.getStaticSharedLibName() != null ? pkg.getManifestPackageName() : pkg.getPackageName();
                    appInfos.add(app);
                }
            }
        }
        return new ParceledListSliceEx(appInfos);
    }

    private boolean isFilterQueryClusterApplication(ApplicationInfo app, boolean isQueryBundle, boolean isQueryPlugin) {
        boolean isFilter = true;
        if ((isQueryBundle && HwPackageManagerUtils.isBundleApplication(app)) || (isQueryPlugin && HwPackageManagerUtils.isSplitApplication(app))) {
            isFilter = false;
        }
        if (IS_DEBUG) {
            SlogEx.d(TAG, "getClusterApplications " + app.packageName + " isFilter:" + isFilter);
        }
        return isFilter;
    }

    public int installHepApp(File stageDir) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.INSTALL_PACKAGES", null);
        if (stageDir == null) {
            return -1;
        }
        if (this.mHwHepApplicationManager == null) {
            this.mHwHepApplicationManager = new HwHepApplicationManager(this.mContext);
        }
        return this.mHwHepApplicationManager.installHepInStageDir(stageDir);
    }

    public List<HwHepPackageInfo> getInstalledHep(int flags) {
        if (this.mHwHepApplicationManager == null) {
            this.mHwHepApplicationManager = new HwHepApplicationManager(this.mContext);
        }
        return this.mHwHepApplicationManager.getInstalledHep(flags);
    }

    public HwPresetPackage getPresetPackage(String packageName) {
        return HwPreAppManager.getInstance(this).getPresetPackage(packageName);
    }

    public int uninstallHep(String packageName, int flags) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DELETE_PACKAGES", null);
        if (packageName == null) {
            return -1;
        }
        if (this.mHwHepApplicationManager == null) {
            this.mHwHepApplicationManager = new HwHepApplicationManager(this.mContext);
        }
        return this.mHwHepApplicationManager.uninstallHep(packageName, flags);
    }

    public int getDisplayChangeAppRestartConfig(int type, String pkgName) {
        IHwPackageParser hwPackageParser;
        if (pkgName == null || (hwPackageParser = HwFrameworkFactoryEx.getHwPackageParser()) == null) {
            return -1;
        }
        return hwPackageParser.getDisplayChangeAppRestartConfig(type, pkgName);
    }

    public String obtainMapleClassPathByPkg(PackageParserEx.PackageEx pkg) {
        if (pkg == null || pkg.getAppMetaData() == null) {
            SlogEx.i(TAG, "[DCP] -> fail to obtain maple class path due to null pointer of pkg");
            return null;
        }
        String mapleClassPath = concatMapleClassPath(pkg);
        if (mapleClassPath == null) {
            SlogEx.i(TAG, "[DCP] -> no maple class path found");
        }
        return mapleClassPath;
    }

    public void callGenMplCacheAtPmsInstaller(String baseCodePath, int sharedGid, int level, String mapleClassPath) {
        try {
            this.mPms.getInstallerInner().generateMplCache(baseCodePath, sharedGid, level, mapleClassPath);
        } catch (InstallerEx.InstallerExceptionEx e) {
            SlogEx.e(TAG, "[DCP] -> fail to generate maple cache for " + baseCodePath);
        }
    }

    public boolean isMygoteEnabled() {
        return IS_DEVICE_MAPLE_ENABLED && !FEATURE_SUPPORT.equals(SystemPropertiesEx.get("persist.mygote.disable", PROP_DEFAULT_VALUE));
    }

    private void collectMapleClassPath(List<String> resultList, List<String> inputList) {
        if (!(resultList == null || inputList == null)) {
            for (String tmp : inputList) {
                if (!resultList.contains(tmp)) {
                    resultList.add(tmp);
                }
            }
        }
    }

    private String concatMapleClassPath(PackageParserEx.PackageEx pkg) {
        if (pkg == null || pkg.getBaseCodePath() == null) {
            return null;
        }
        StringBuilder retVal = new StringBuilder();
        List<String> resultList = new ArrayList<>();
        collectMapleClassPath(resultList, ZygoteInitEx.getNonBootClasspathList());
        collectMapleClassPath(resultList, pkg.getUsesLibraries());
        collectMapleClassPath(resultList, pkg.getUsesOptionalLibraries());
        Iterator<String> it = resultList.iterator();
        while (it.hasNext()) {
            retVal.append("/system/lib64/libmaple" + it.next() + ".so:");
        }
        retVal.append(pkg.getBaseCodePath() + "!/maple/arm64/mapleclasses.so");
        return retVal.toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0048  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004b A[SYNTHETIC] */
    public List<PackageParserEx.PackageEx> obtainMaplePkgsToGenCacheEx() {
        boolean isMaplized;
        List<PackageParserEx.PackageEx> pkgs = new ArrayList<>();
        synchronized (this.mPms.getPackagesLockObject()) {
            for (PackageParserEx.PackageEx pkg : this.mPms.getPackagesLock().values()) {
                if ((ApplicationInfoExt.getHwFlags(pkg.getApplicationInfo()) & 16777216) == 0) {
                    if ((ApplicationInfoExt.getHwFlags(pkg.getApplicationInfo()) & 8388608) == 0) {
                        isMaplized = false;
                        if (!isMaplized) {
                            pkgs.add(pkg);
                        }
                    }
                }
                isMaplized = true;
                if (!isMaplized) {
                }
            }
        }
        return pkgs;
    }

    public void clearMplCacheLIF(PackageParserEx.PackageEx pkg, int userId, int flags) {
        InstallerEx.InstallerExceptionEx e;
        if (pkg == null) {
            SlogEx.wtf(TAG, "Package was null!", new Throwable());
            return;
        }
        PackageSettingEx ps = this.mPms.getPackageSettingByPackageName(pkg.getPackageName());
        int[] resolvedUserIds = userId == -1 ? PackageManagerServiceEx.getUserManager().getUserIds() : new int[]{userId};
        for (int realUserId : resolvedUserIds) {
            long ceDataInode = ps != null ? ps.getCeDataInode(realUserId) : 0;
            try {
                SlogEx.i(TAG, "[DCP] clearMplCacheLIF: pkg = " + pkg);
                try {
                    this.mPms.getInstallerInner().clearMplCache(pkg.getVolumeUuid(), pkg.getPackageName(), realUserId, flags, ceDataInode);
                } catch (InstallerEx.InstallerExceptionEx e2) {
                    e = e2;
                }
            } catch (InstallerEx.InstallerExceptionEx e3) {
                e = e3;
                SlogEx.e(TAG, String.valueOf(e));
            }
        }
    }

    public boolean isMplPackage(PackageParserEx.PackageEx pkg) {
        if (pkg == null) {
            return false;
        }
        if ((ApplicationInfoExt.getHwFlags(pkg.getApplicationInfo()) & 16777216) == 0 && (ApplicationInfoExt.getHwFlags(pkg.getApplicationInfo()) & 8388608) == 0) {
            return false;
        }
        return true;
    }

    public int getCacheLevelForMapleApp(PackageParserEx.PackageEx pkg) {
        int cacheLevel = -1;
        if (pkg == null || pkg.getAppMetaData() == null) {
            SlogEx.i(TAG, "[DCP] -> fail to get cache level for maple cache due to null pointer");
            return -1;
        }
        String cacheLevelStr = pkg.getAppMetaData().getString("com.huawei.maple.pre.generatecache");
        if (cacheLevelStr == null) {
            SlogEx.i(TAG, "[DCP] -> failed, cacheLevelStr is null");
            return -1;
        }
        String cacheLevelStr2 = cacheLevelStr.trim();
        if ("true".equals(cacheLevelStr2) || "3".equals(cacheLevelStr2)) {
            cacheLevel = 3;
        }
        if (FEATURE_SUPPORT.equals(cacheLevelStr2)) {
            cacheLevel = 1;
        }
        if ("7".equals(cacheLevelStr2)) {
            cacheLevel = 7;
        }
        if (cacheLevel == -1) {
            SlogEx.i(TAG, "[DCP] -> invalid value of cacheLevel");
        }
        return cacheLevel;
    }

    public boolean isNeedForbidShellFunc(String packageName) {
        return ForbidShellFuncUtilEx.isNeedForbidShellFunc(packageName);
    }

    public boolean resolvePreferredActivity(IntentFilter filter, int match, ComponentName[] sets, ComponentName activity, int userId) {
        return new HwPreferredActivityManagerEx(this.mContext, this.mPms.getSettings(), this.mPms).resolvePreferredActivity(filter, match, sets, activity, userId);
    }

    public void rebuildPreferredActivity(int userId) {
        new HwPreferredActivityManagerEx(this.mContext, this.mPms.getSettings(), this.mPms).rebuildPreferredActivity(userId);
    }

    public void rebuildApkBindFile() {
        new HwBindApkFileManager(this.mPms).rebuildApkBindFile();
    }

    public void initParallelPackageDexOptimizer(Context context, PackageDexOptimizerEx packageDexOptimizer) {
        this.mParallelPackageDexOptimizer = new HwParallelPackageDexOptimizerEx(context, packageDexOptimizer);
    }

    public void parallelPerformDexOpt(PackageParserEx.PackageEx pkg, String[] instructionSets, CompilerStatsEx.PackageStatsEx packageStats, PackageDexUsageEx.PackageUseInfoEx packageUseInfo, DexoptOptionsEx options) {
        HwParallelPackageDexOptimizerEx hwParallelPackageDexOptimizerEx = this.mParallelPackageDexOptimizer;
        if (hwParallelPackageDexOptimizerEx != null) {
            hwParallelPackageDexOptimizerEx.submit(pkg, instructionSets, packageStats, packageUseInfo, options);
        }
    }

    public boolean removeMatchedPreferredActivity(Intent intent, PreferredIntentResolverEx preferredIntentResolver, PreferredActivityEx preferredActivity) {
        return new HwPreferredActivityManagerEx(this.mContext, this.mPms.getSettings(), this.mPms).removeMatchedPreferredActivity(intent, preferredIntentResolver, preferredActivity);
    }

    public HwGunstallSwitchState updateGunstallState() {
        return GunstallUtilEx.updateGunstallState();
    }

    public boolean forbidGMSUpgrade(PackageParserEx.PackageEx pkg, PackageParserEx.PackageEx oldPackage, int callingSessionUid, HwGunstallSwitchState hwGSwitchState) {
        return GunstallUtilEx.forbidGMSUpgrade(pkg, oldPackage, callingSessionUid, hwGSwitchState);
    }

    public boolean isScanInstallApk(String codePath) {
        return HwForbidUninstallManager.getInstance(this).isScanInstallApk(codePath);
    }

    public void loadRemoveUnstallApks() {
        long startTime = HwPackageManagerServiceUtilsEx.hwTimingsBegin();
        HwForbidUninstallManager.getInstance(this).loadRemoveUnstallApks();
        HwPackageManagerServiceUtilsEx.hwTimingsEnd(TAG, "loadRemoveUnstallApks", startTime);
    }

    public boolean isRemoveUnstallApk(File file) {
        return HwForbidUninstallManager.getInstance(this).isRemoveUnstallApk(file);
    }

    public FeatureInfo[] getHwSystemAvailableFeatures() {
        return HwSystemFeatureManager.getInstance().getHwSystemAvailableFeatures();
    }

    public boolean hasHwSystemFeature(String featureName, int version) {
        return HwSystemFeatureManager.getInstance().hasHwSystemFeature(featureName, version);
    }

    public void addWaitDexOptPackage(String packageName) {
        HwCompensateDexOptManager.getInstance().addWaitDexOptPackage(packageName);
    }

    public void fixMdmRuntimePermission(String packageName, String permName, int flag) {
        HwMdmFixPermission.getInstance().fixMdmRuntimePermission(packageName, permName, this, flag);
    }

    public boolean isMdmFix(String packageName) {
        return HwMdmFixPermission.getInstance().isMdmFix(packageName);
    }

    public boolean isNeedForbidAppAct(String scenes, String pkgName, String className, HashMap<String, String> extra) {
        return HwAppActController.getInstance().isNeedForbidAppAct(scenes, pkgName, className, extra);
    }

    public void recordPreasApp(String pkgName, String appPath) {
        AppActUtils.recordPreasApp(pkgName, appPath);
    }

    public boolean isInDelAppList(String packageName) {
        return this.mPms.getSettings().isInDelAppList(packageName);
    }

    public boolean isNeedForbidHarmfulAppDisableApp(String callingPackageName, String targetPackageName) {
        return AppProtectControlUtil.getInstance().isNeedForbidHarmfulAppDisableApp(callingPackageName, targetPackageName);
    }

    public boolean isNeedForbidHarmfulAppUpdateApp(String packageName, String updateSource) {
        return AppProtectControlUtil.getInstance().isNeedForbidHarmfulAppUpdateApp(packageName, updateSource);
    }

    public boolean isNeedForbidHarmfulAppSlientDeleteApp(String deletePackageName) {
        return AppProtectControlUtil.getInstance().isNeedForbidHarmfulAppSlientDeleteApp(deletePackageName);
    }
}
