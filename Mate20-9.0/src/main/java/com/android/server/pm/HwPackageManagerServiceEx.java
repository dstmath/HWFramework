package com.android.server.pm;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SynchronousUserSwitchObserver;
import android.bluetooth.BluetoothAddressNative;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ParceledListSlice;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.content.pm.UserInfo;
import android.content.pm.VersionedPackage;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.biometrics.fingerprint.V2_1.RequestStatus;
import android.hdm.HwDeviceManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBackupSessionCallback;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.perf.HwOptPackageParser;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Flog;
import android.util.HwSlog;
import android.util.IMonitor;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import android.widget.Toast;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.UserIcons;
import com.android.internal.util.XmlUtils;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.HwServiceFactory;
import com.android.server.UiThread;
import com.android.server.gesture.GestureNavConst;
import com.android.server.notch.HwNotchScreenWhiteConfig;
import com.android.server.pfw.autostartup.comm.XmlConst;
import com.android.server.pm.BlackListInfo;
import com.android.server.pm.CertCompatSettings;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.auth.HwCertificationManager;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.antimal.HwAntiMalStatus;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.security.securityprofile.ISecurityProfileController;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.manufacture.ManufactureNativeUtils;
import com.huawei.cust.HwCustUtils;
import com.huawei.permission.HwSystemManager;
import huawei.com.android.server.security.fileprotect.HwAppAuthManager;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class HwPackageManagerServiceEx implements IHwPackageManagerServiceEx, IHwPackageManagerServiceExInner {
    private static final String ACTION_GET_INSTALLER_PACKAGE_INFO = "com.huawei.android.action.GET_INSTALLER_PACKAGE_INFO";
    private static final String ACTION_GET_PACKAGE_INSTALLATION_INFO = "com.huawei.android.action.GET_PACKAGE_INSTALLATION_INFO";
    private static final boolean ANTIMAL_DEBUG_ON = (SystemProperties.getInt(PROPERTY_ANTIMAL_DEBUG, 0) == 1);
    private static final String ANTIMAL_MODULE = "antiMalware";
    private static final boolean APP_INSTALL_AS_SYS_ALLOW = SystemProperties.getBoolean("ro.config.romUpgradeDataReserved", true);
    private static final String BROADCAST_PERMISSION = "com.android.permission.system_manager_interface";
    private static final String CERT_TYPE_MEDIA = "media";
    private static final String CERT_TYPE_PLATFORM = "platform";
    private static final String CERT_TYPE_SHARED = "shared";
    private static final String CERT_TYPE_TESTKEY = "testkey";
    private static final String COTA_APK_XML_PATH = "xml/APKInstallListEMUI5Release.txt";
    private static final int COTA_APP_INSTALLING = -1;
    private static final String COTA_DEL_APK_XML_PATH = "xml/DelAPKInstallListEMUI5Release.txt";
    private static final boolean DEBUG = SystemProperties.get("ro.dbg.pms_log", "0").equals(XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_);
    private static final boolean DEBUG_DEXOPT_SHELL;
    private static final String DEVICE_POLICIES_XML = "device_policies.xml";
    private static final String FLAG_APKPATCH_PATH = "/patch_hw/apk/apk_patch.xml";
    private static final String FLAG_APKPATCH_PKGNAME = "pkgname";
    private static final String FLAG_APKPATCH_TAG = "android.huawei.SYSTEM_APP_PATCH";
    private static final String FLAG_APKPATCH_TAGPATCH = "apkpatch";
    private static final String FLAG_APKPATCH_TAGVALUE = "value";
    private static final String GOOGLESETUP_PKG = "com.google.android.setupwizard";
    private static final String HWSETUP_PKG = "com.huawei.hwstartupguide";
    private static final String HW_PMS_SET_APP_PERMISSION = "huawei.android.permission.SET_CANNOT_UNINSTALLED_PERMISSION";
    private static final String HW_SOUND_RECORDER = "com.android.soundrecorder.upgrade";
    private static final String INSERT_RESULT = "result";
    private static final String INSTALLATION_EXTRA_INSTALLER_PACKAGE_NAME = "installerPackageName";
    private static final String INSTALLATION_EXTRA_PACKAGE_INSTALLER_PID = "pkgInstallerPid";
    private static final String INSTALLATION_EXTRA_PACKAGE_INSTALLER_UID = "pkgInstallerUid";
    private static final String INSTALLATION_EXTRA_PACKAGE_INSTALL_RESULT = "pkgInstallResult";
    private static final String INSTALLATION_EXTRA_PACKAGE_NAME = "pkgName";
    private static final String INSTALLATION_EXTRA_PACKAGE_UPDATE = "pkgUpdate";
    private static final String INSTALLATION_EXTRA_PACKAGE_URI = "pkgUri";
    private static final String INSTALLATION_EXTRA_PACKAGE_VERSION_CODE = "pkgVersionCode";
    private static final String INSTALLATION_EXTRA_PACKAGE_VERSION_NAME = "pkgVersionName";
    private static final int INSTALLER_ADB = 1;
    private static final int INSTALLER_OTHERS = 0;
    private static final String INSTALL_BEGIN = "begin";
    private static final String INSTALL_END = "end";
    private static final String[] INSTALL_SAFEMODE_LIST = {"jp.co.omronsoft.iwnnime.ml"};
    private static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", "").equals(""));
    private static final int LEGAL_RECORD_NUM = 4;
    private static final Set<MySysAppInfo> MDM_SYSTEM_APPS = new HashSet();
    private static final Set<MySysAppInfo> MDM_SYSTEM_UNDETACHABLE_APPS = new HashSet();
    private static final String META_KEY_KEEP_ALIVE = "android.server.pm.KEEP_ALIVE";
    private static final String MIDDLEWARE_LIMITED_DPC_PKGS = "com.huawei.mdm.dpc";
    private static final int MSG_DEL_SOUNDRECORDER = 2;
    public static final int MSG_SET_CURRENT_EMUI_SYS_IMG_VERSION = 1;
    static final long OTA_WAIT_DEXOPT_TIME = 480000;
    private static final String PACKAGE_CACHE_DIR = "/data/system/package_cache/1/";
    private static final String PACKAGE_NAME = "pkg";
    private static final String POLICY_CHANGED_INTENT_ACTION = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    public static final String PREINSTALLED_APK_LIST_DIR = "/data/system/";
    public static final String PREINSTALLED_APK_LIST_FILE = "preinstalled_app_list_file.xml";
    private static final String PROPERTY_ANTIMAL_DEBUG = "persist.sys.antimal.debug";
    private static final int SCAN_AS_PRIVILEGED = 262144;
    public static final int SCAN_AS_SYSTEM = 131072;
    private static final int SCAN_BOOTING = 16;
    private static final int SCAN_FIRST_BOOT_OR_UPGRADE = 8192;
    private static final int SCAN_INITIAL = 512;
    private static final ArrayList<String> SCAN_INSTALL_CALLER_PACKAGES = new ArrayList<>(Arrays.asList(new String[]{GestureNavConst.DEFAULT_LAUNCHER_PACKAGE, "com.huawei.hiassistant", "com.huawei.search", "com.huawei.tips"}));
    private static final String SEPARATOR = ":";
    private static final String SIMPLE_COTA_APK_XML_PATH = "/data/cota/live_update/work/xml/APKInstallListEMUI5Release.txt";
    private static final String SIMPLE_COTA_DEL_APK_XML_PATH = "/data/cota/live_update/work/xml/DelAPKInstallListEMUI5Release.txt";
    private static final String SOURCE_PACKAGE_NAME = "src";
    public static final String SYSTEM_APP_DIR = "/system/app";
    private static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    private static final String SYSTEM_FRAMEWORK_DIR = "/system/framework/";
    public static final String SYSTEM_PRIV_APP_DIR = "/system/priv-app";
    private static final String SYSTEM_SECURE = "ro.secure";
    static final String TAG = "HwPackageManagerServiceEx";
    private static final String TAG_SPECIAL_POLICY_SYS_APP_LIST = "update-sys-app-install-list";
    private static final String TAG_SPECIAL_POLICY_UNDETACHABLE_SYS_APP_LIST = "update-sys-app-undetachable-install-list";
    private static final String TERMINATOR = ";";
    static final long WAIT_DEXOPT_TIME = 180000;
    private static List<String> mCustStoppedApps = new ArrayList();
    private static List<String> preinstalledPackageList = new ArrayList();
    private boolean isBlackListExist = false;
    private BlackListInfo mBlackListInfo = new BlackListInfo();
    private boolean mBootCompleted = false;
    private final HandlerThread mCommonThread = new HandlerThread("PMSCommonThread");
    private CertCompatSettings mCompatSettings;
    final Context mContext;
    private Object mCust = null;
    private ArrayList<String> mDataApkShouldNotUpdateByCota = new ArrayList<>();
    private boolean mDexoptNow = false;
    private BlackListInfo mDisableAppListInfo = new BlackListInfo();
    private HashMap<String, BlackListInfo.BlackListApp> mDisableAppMap = new HashMap<>();
    private boolean mFoundCertCompatFile;
    final PackageExHandler mHandler;
    private HwAntiMalStatus mHwAntiMalStatus = null;
    private HwFastAppManager mHwFastAppManager = null;
    private HwFileBackupManager mHwFileBackupManager = null;
    private HwOptPackageParser mHwOptPackageParser = null;
    IHwPackageManagerInner mIPmsInner = null;
    private Set<String> mIncompatNotificationList = new ArraySet();
    private Set<String> mIncompatiblePkg = new ArraySet();
    private AtomicBoolean mIsOpting = new AtomicBoolean(false);
    private ArrayList<String> mScanInstallApkList = new ArrayList<>();
    boolean mSetupDisabled = false;
    final Object mSpeedOptLock = new Object();
    private ArraySet<String> mSpeedOptPkgs = new ArraySet<>();
    private HashSet<String> mUninstallBlackListPkgNames = new HashSet<>();
    /* access modifiers changed from: private */
    public long mUserSwitchingTime = 0;
    private boolean needCollectAppInfo = true;

    private class MyCallback implements PackageParser.Callback {
        private MyCallback() {
        }

        public boolean hasFeature(String feature) {
            return false;
        }

        public String[] getOverlayPaths(String targetPackageName, String targetPath) {
            return null;
        }

        public String[] getOverlayApks(String targetPackageName) {
            return null;
        }
    }

    private class MySysAppInfo {
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
        public void setPrivileged(boolean value) {
            this.isPrivileged = value;
        }

        /* access modifiers changed from: package-private */
        public boolean getUndetachable() {
            return this.isUndetachable;
        }

        /* access modifiers changed from: package-private */
        public void setUndetachable(boolean value) {
            this.isUndetachable = value;
        }
    }

    class PackageExHandler extends Handler {
        PackageExHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            doHandleMessage(msg);
        }

        /* access modifiers changed from: package-private */
        public void doHandleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    PackageParser.setCurrentEmuiSysImgVersion(HwPackageManagerServiceEx.deriveEmuiSysImgVersion());
                    return;
                case 2:
                    HwPackageManagerServiceEx.this.deleteSoundercorderIfNeed();
                    return;
                default:
                    return;
            }
        }
    }

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        DEBUG_DEXOPT_SHELL = z;
    }

    public HwPackageManagerServiceEx(IHwPackageManagerInner pms, Context context) {
        this.mIPmsInner = pms;
        this.mContext = context;
        this.mHwOptPackageParser = HwFrameworkFactory.getHwOptPackageParser();
        this.mHwOptPackageParser.getOptPackages();
        this.mHwAntiMalStatus = new HwAntiMalStatus(this.mContext);
        this.mCommonThread.start();
        this.mHwFastAppManager = new HwFastAppManager(this.mContext, new Handler(this.mCommonThread.getLooper()));
        this.mHandler = new PackageExHandler(this.mCommonThread.getLooper());
        MspesExUtil.getInstance(this).initMspesForbidInstallApps();
        MDM_SYSTEM_APPS.clear();
        MDM_SYSTEM_UNDETACHABLE_APPS.clear();
        readSysInfoFromDevicePolicyXml();
    }

    public boolean isPerfOptEnable(String packageName, int optType) {
        return this.mHwOptPackageParser.isPerfOptEnable(packageName, optType);
    }

    public void checkHwCertification(PackageParser.Package pkg, boolean isUpdate) {
        HwAppAuthManager.getInstance().checkFileProtect(pkg);
        if (HwCertificationManager.hasFeature()) {
            if (!HwCertificationManager.isSupportHwCertification(pkg)) {
                if (isContainHwCertification(pkg)) {
                    cleanUpHwCert(pkg);
                }
                return;
            }
            boolean isUpgrade = this.mIPmsInner.isUpgrade();
            if (isUpdate || !isContainHwCertification(pkg) || isUpgrade) {
                checkContainHwCert(pkg);
            }
        }
    }

    private void checkContainHwCert(PackageParser.Package pkg) {
        HwCertificationManager manager = HwCertificationManager.getIntance();
        if (manager == null || manager.checkHwCertification(pkg)) {
        }
    }

    public boolean getHwCertPermission(boolean allowed, PackageParser.Package pkg, String perm) {
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

    private void cleanUpHwCert(PackageParser.Package pkg) {
        HwCertificationManager manager = HwCertificationManager.getIntance();
        if (manager != null) {
            manager.cleanUp(pkg);
        }
    }

    public void cleanUpHwCert() {
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

    public void initHwCertificationManager() {
        if (!HwCertificationManager.isInitialized()) {
            HwCertificationManager.initialize(this.mContext);
        }
        HwCertificationManager intance = HwCertificationManager.getIntance();
    }

    public int getHwCertificateType(PackageParser.Package pkg) {
        if (!HwCertificationManager.isSupportHwCertification(pkg)) {
            return HwCertificationManager.getIntance().getHwCertificateTypeNotMDM();
        }
        return HwCertificationManager.getIntance().getHwCertificateType(pkg.packageName);
    }

    public boolean isContainHwCertification(PackageParser.Package pkg) {
        return HwCertificationManager.getIntance().isContainHwCertification(pkg.packageName);
    }

    public boolean isAllowedSetHomeActivityForAntiMal(PackageInfo pi, int userId) {
        if (this.mHwAntiMalStatus == null) {
            this.mHwAntiMalStatus = new HwAntiMalStatus(this.mContext);
        }
        return this.mHwAntiMalStatus.isAllowedSetHomeActivityForAntiMal(pi, userId);
    }

    public boolean isAllowedToBeDisabled(String packageName) {
        if (this.mHwAntiMalStatus == null) {
            this.mHwAntiMalStatus = new HwAntiMalStatus(this.mContext);
        }
        return this.mHwAntiMalStatus.isAllowedToBeDisabled(packageName);
    }

    public void updateNochScreenWhite(String packageName, String flag, int versionCode) {
        if (IS_NOTCH_PROP) {
            HwNotchScreenWhiteConfig.getInstance().updateVersionCodeInNoch(packageName, flag, versionCode);
            if ("removed".equals(flag)) {
                HwNotchScreenWhiteConfig.getInstance().removeAppUseNotchMode(packageName);
            }
        }
    }

    public int getAppUseNotchMode(String packageName) {
        if (!IS_NOTCH_PROP) {
            return -1;
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIPmsInner.getPackagesLock()) {
                PackageSetting pkgSetting = (PackageSetting) this.mIPmsInner.getSettings().mPackages.get(packageName);
                if (pkgSetting == null) {
                    Binder.restoreCallingIdentity(callingId);
                    return -1;
                }
                int appUseNotchMode = pkgSetting.getAppUseNotchMode();
                Binder.restoreCallingIdentity(callingId);
                return appUseNotchMode;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0055, code lost:
        r3 = r4;
        android.os.Binder.restoreCallingIdentity(r1);
     */
    public void setAppUseNotchMode(String packageName, int mode) {
        if (IS_NOTCH_PROP) {
            int uid = Binder.getCallingUid();
            if (UserHandle.getAppId(uid) == 1000 || uid == 0) {
                long callingId = Binder.clearCallingIdentity();
                try {
                    synchronized (this.mIPmsInner.getPackagesLock()) {
                        PackageSetting pkgSetting = (PackageSetting) this.mIPmsInner.getSettings().mPackages.get(packageName);
                        if (pkgSetting == null) {
                            Binder.restoreCallingIdentity(callingId);
                        } else if (pkgSetting.getAppUseNotchMode() != mode) {
                            pkgSetting.setAppUseNotchMode(mode);
                            this.mIPmsInner.getSettings().writeLPr();
                            HwNotchScreenWhiteConfig.getInstance().updateAppUseNotchMode(packageName, mode);
                        }
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                    throw th;
                }
            } else {
                throw new SecurityException("Only the system can set app use notch mode");
            }
        }
    }

    private void listenForUserSwitches() {
        try {
            ActivityManager.getService().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                public void onUserSwitching(int newUserId) throws RemoteException {
                    synchronized (HwPackageManagerServiceEx.this.mSpeedOptLock) {
                        long unused = HwPackageManagerServiceEx.this.mUserSwitchingTime = SystemClock.elapsedRealtime();
                        Slog.d(HwPackageManagerServiceEx.TAG, "onUserSwitching " + HwPackageManagerServiceEx.this.mUserSwitchingTime);
                    }
                }
            }, TAG);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to listen for user switching event");
        }
    }

    public boolean isApkDexOpt(String targetCompilerFilter) {
        return "speed-profile-opt".equals(targetCompilerFilter);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00b5, code lost:
        r1.mIsOpting.set(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00bd, code lost:
        r6 = r1.mIPmsInner.performDexOptMode(r2, r20, "speed-profile", r22, r23, r24);
        r7 = r1.mSpeedOptLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00cf, code lost:
        monitor-enter(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00d6, code lost:
        if (r1.mSpeedOptPkgs.isEmpty() != false) goto L_0x00ee;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00da, code lost:
        if (r1.mDexoptNow != false) goto L_0x00dd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00dd, code lost:
        r2 = r1.mSpeedOptPkgs.valueAt(0);
        r1.mSpeedOptPkgs.removeAt(0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00eb, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00ec, code lost:
        r3 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00ee, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00ef, code lost:
        r1.mIsOpting.set(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00f4, code lost:
        return r6;
     */
    public boolean hwPerformDexOptMode(String packageName, boolean checkProfiles, String targetCompilerFilter, boolean force, boolean bootComplete, String splitName) {
        String packageName2 = packageName;
        synchronized (this.mSpeedOptLock) {
            if (!this.mBootCompleted) {
                this.mBootCompleted = SystemProperties.get("sys.boot_completed", "0").equals("1");
            }
            long elapsedTime = SystemClock.elapsedRealtime();
            if (!this.mDexoptNow) {
                this.mDexoptNow = elapsedTime > (this.mIPmsInner.isUpgrade() ? OTA_WAIT_DEXOPT_TIME : 180000);
            }
            if (this.mUserSwitchingTime != 0 && this.mDexoptNow) {
                this.mDexoptNow = false;
                if (elapsedTime > this.mUserSwitchingTime) {
                    this.mDexoptNow = elapsedTime - this.mUserSwitchingTime > WAIT_DEXOPT_TIME;
                }
            }
            Slog.i(TAG, "now " + elapsedTime + " optNow " + this.mDexoptNow + " upgrade " + this.mIPmsInner.isUpgrade() + " BootCompleted " + this.mBootCompleted + " UserSwitching " + this.mUserSwitchingTime);
            if (!this.mIsOpting.get() && this.mDexoptNow) {
                if (!this.mBootCompleted) {
                }
            }
            this.mSpeedOptPkgs.add(packageName2);
            Slog.d(TAG, "performDexOptMode add list " + packageName2 + " size " + this.mSpeedOptPkgs.size());
            return true;
        }
    }

    public void setAppCanUninstall(String packageName, boolean canUninstall) {
        this.mContext.enforceCallingPermission(HW_PMS_SET_APP_PERMISSION, "setAppCanUninstall");
        String callingName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (callingName != null && callingName.equalsIgnoreCase(packageName)) {
            if (canUninstall) {
                this.mUninstallBlackListPkgNames.remove(packageName);
            } else {
                this.mUninstallBlackListPkgNames.add(packageName);
            }
        }
    }

    public boolean isAllowUninstallApp(String packageName) {
        return !this.mUninstallBlackListPkgNames.contains(packageName) && !MspesExUtil.getInstance(this).isForbidMspesUninstall(packageName);
    }

    public boolean isDisallowedInstallApk(PackageParser.Package pkg) {
        boolean z = false;
        if (pkg == null || TextUtils.isEmpty(pkg.packageName) || pkg.mSigningDetails == null || !MIDDLEWARE_LIMITED_DPC_PKGS.equals(pkg.packageName)) {
            return false;
        }
        if (PackageManagerServiceUtils.compareSignatures(new Signature[]{new Signature(HwUtils.SYSTEM_SIGN_STR)}, pkg.mSigningDetails.signatures) != 0) {
            z = true;
        }
        return z;
    }

    private boolean isUserRestricted(int userId, String restrictionKey) {
        if (!UserManager.get(this.mContext).getUserRestrictions(UserHandle.of(userId)).getBoolean(restrictionKey, false)) {
            return false;
        }
        Slog.w(TAG, "User is restricted: " + restrictionKey);
        return true;
    }

    public void installPackageAsUser(String originPath, IPackageInstallObserver2 observer, int installFlags, String installerPackageName, int userId) {
        int installFlags2;
        UserHandle userHandle;
        IPackageInstallObserver2 iPackageInstallObserver2 = observer;
        int i = userId;
        this.mContext.enforceCallingOrSelfPermission("android.permission.INSTALL_PACKAGES", null);
        int callingUid = Binder.getCallingUid();
        this.mIPmsInner.getPermissionManager().enforceCrossUserPermission(callingUid, i, true, true, "installPackageAsUser");
        if (isUserRestricted(i, "no_install_apps")) {
            if (iPackageInstallObserver2 != null) {
                try {
                    iPackageInstallObserver2.onPackageInstalled("", -111, null, null);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to install package as user");
                }
            }
        } else if (!HwDeviceManager.disallowOp(6)) {
            if (callingUid == 2000 || callingUid == 0) {
                installFlags2 = installFlags | 32;
            } else {
                installFlags2 = installFlags & -33 & -65;
            }
            if ((installFlags2 & 64) != 0) {
                userHandle = UserHandle.ALL;
            } else {
                userHandle = new UserHandle(i);
            }
            UserHandle user = userHandle;
            if ((installFlags2 & 256) != 0 && this.mContext.checkCallingOrSelfPermission("android.permission.INSTALL_GRANT_RUNTIME_PERMISSIONS") == -1) {
                throw new SecurityException("You need the android.permission.INSTALL_GRANT_RUNTIME_PERMISSIONS permission to use the PackageManager.INSTALL_GRANT_RUNTIME_PERMISSIONS flag");
            } else if ((installFlags2 & 1) == 0 && (installFlags2 & 8) == 0) {
                File originFile = new File(originPath);
                PackageManagerService.OriginInfo origin = PackageManagerService.OriginInfo.fromUntrustedFile(originFile);
                Message msg = this.mIPmsInner.getPackageHandler().obtainMessage(5);
                File file = originFile;
                int i2 = callingUid;
                Message msg2 = msg;
                msg2.obj = this.mIPmsInner.createInstallParams(origin, null, iPackageInstallObserver2, installFlags2, installerPackageName, null, new PackageManagerService.VerificationInfo(null, null, -1, callingUid), user, null, null, PackageParser.SigningDetails.UNKNOWN, 0);
                this.mIPmsInner.getPackageHandler().sendMessage(msg2);
            } else {
                throw new IllegalArgumentException("New installs into ASEC containers no longer supported");
            }
        }
    }

    public boolean isPrivilegedPreApp(File scanFile) {
        return HwPreAppManager.getInstance(this).isPrivilegedPreApp(scanFile);
    }

    public boolean isSystemPreApp(File scanFile) {
        return HwPreAppManager.getInstance(this).isSystemPreApp(scanFile);
    }

    public void readPersistentConfig() {
        HwPersistentAppManager.readPersistentConfig();
    }

    public void resolvePersistentFlagForPackage(int oldFlags, PackageParser.Package pkg) {
        HwPersistentAppManager.resolvePersistentFlagForPackage(oldFlags, pkg);
    }

    public boolean isPersistentUpdatable(PackageParser.Package pkg) {
        return HwPersistentAppManager.isPersistentUpdatable(pkg);
    }

    public PackageInfo handlePackageNotFound(String packageName, int flag, int callingUid) {
        if (this.mHwFastAppManager != null) {
            return this.mHwFastAppManager.getPacakgeInfoForFastApp(packageName, flag, callingUid);
        }
        return null;
    }

    public void systemReady() {
        if (this.mHwFastAppManager != null) {
            this.mHwFastAppManager.systemReady();
        }
        if (HwCertificationManager.hasFeature()) {
            if (!HwCertificationManager.isInitialized()) {
                HwCertificationManager.initialize(this.mContext);
            }
            HwCertificationManager.getIntance().systemReady();
        }
        setCurrentEmuiSysImgVersion();
        try {
            initPackageBlackList();
        } catch (Exception e) {
            Slog.e(TAG, "initBlackList failed");
        }
        deleteSoundrecorder();
        listenForUserSwitches();
        if (SystemProperties.getBoolean("ro.config.hw_mg_copyright", true)) {
            HwThemeInstaller.getInstance(this.mContext).createMagazineFolder();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.huawei.devicepolicy.action.POLICY_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String policyName = intent.getStringExtra("policy_name");
                Slog.d(HwPackageManagerServiceEx.TAG, "devicepolicy.action.POLICY_CHANGED policyName:" + policyName);
                if (HwPackageManagerServiceEx.TAG_SPECIAL_POLICY_UNDETACHABLE_SYS_APP_LIST.equals(policyName) || HwPackageManagerServiceEx.TAG_SPECIAL_POLICY_SYS_APP_LIST.equals(policyName)) {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        String value = bundle.getString("value");
                        Slog.d(HwPackageManagerServiceEx.TAG, "value:" + value);
                        HwPackageManagerServiceEx.this.updateSysAppInfoList(value, policyName);
                    }
                }
            }
        }, intentFilter, "android.permission.MANAGE_PROFILE_AND_DEVICE_OWNERS", null);
    }

    /* access modifiers changed from: private */
    public void updateSysAppInfoList(String currentValue, String policyName) {
        String str = currentValue;
        String str2 = policyName;
        Slog.d(TAG, "updateSysAppInfoList currentValue:" + str);
        if (!TextUtils.isEmpty(currentValue)) {
            if (TAG_SPECIAL_POLICY_UNDETACHABLE_SYS_APP_LIST.equals(str2)) {
                MDM_SYSTEM_UNDETACHABLE_APPS.clear();
                String[] split = str.split(";");
                int length = split.length;
                int i = 0;
                while (i < length) {
                    String[] infoList = split[i].split(SEPARATOR);
                    if (infoList.length == 4) {
                        MySysAppInfo mySysAppInfo = new MySysAppInfo(infoList[0], infoList[1], infoList[2], infoList[3]);
                        MDM_SYSTEM_UNDETACHABLE_APPS.add(mySysAppInfo);
                        i++;
                    } else {
                        return;
                    }
                }
            } else if (TAG_SPECIAL_POLICY_SYS_APP_LIST.equals(str2)) {
                MDM_SYSTEM_APPS.clear();
                String[] split2 = str.split(";");
                int length2 = split2.length;
                int i2 = 0;
                while (i2 < length2) {
                    String[] infoList2 = split2[i2].split(SEPARATOR);
                    if (infoList2.length == 4) {
                        MySysAppInfo mySysAppInfo2 = new MySysAppInfo(infoList2[0], infoList2[1], infoList2[2], infoList2[3]);
                        MDM_SYSTEM_APPS.add(mySysAppInfo2);
                        i2++;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    public int adjustScanFlagForApk(PackageParser.Package pkg, int scanFlags) {
        int result = scanFlags;
        if (APP_INSTALL_AS_SYS_ALLOW && isValidPackage(pkg)) {
            MySysAppInfo target = getRecordFromCache(MDM_SYSTEM_APPS, pkg.packageName, sha256(pkg.mSigningDetails.signatures[0].toByteArray()));
            if (target == null) {
                target = getRecordFromCache(MDM_SYSTEM_UNDETACHABLE_APPS, pkg.packageName, sha256(pkg.mSigningDetails.signatures[0].toByteArray()));
            }
            if (target != null) {
                if (target.getPrivileged()) {
                    Slog.d(TAG, "add privileged for " + pkg.packageName);
                    result = result | 262144 | 131072;
                } else {
                    Slog.d(TAG, "add sytem for " + pkg.packageName);
                    result |= 131072;
                }
                if (!target.getUndetachable()) {
                    Slog.d(TAG, "add del for " + pkg.packageName);
                    ApplicationInfo applicationInfo = pkg.applicationInfo;
                    applicationInfo.hwFlags = applicationInfo.hwFlags | 33554432;
                }
            }
        }
        return result;
    }

    public boolean isSystemAppGrantByMdm(PackageParser.Package pkg) {
        if (APP_INSTALL_AS_SYS_ALLOW && isValidPackage(pkg)) {
            MySysAppInfo target = getRecordFromCache(MDM_SYSTEM_APPS, pkg.packageName, sha256(pkg.mSigningDetails.signatures[0].toByteArray()));
            if (target == null) {
                target = getRecordFromCache(MDM_SYSTEM_UNDETACHABLE_APPS, pkg.packageName, sha256(pkg.mSigningDetails.signatures[0].toByteArray()));
            }
            if (target != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isSystemAppGrantByMdm(String pkgName) {
        if (APP_INSTALL_AS_SYS_ALLOW && !TextUtils.isEmpty(pkgName)) {
            MySysAppInfo target = getRecordFromCache(MDM_SYSTEM_APPS, pkgName, null);
            if (target == null) {
                target = getRecordFromCache(MDM_SYSTEM_UNDETACHABLE_APPS, pkgName, null);
            }
            if (target != null) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidPackage(PackageParser.Package pkg) {
        if (pkg == null || TextUtils.isEmpty(pkg.packageName) || pkg.mSigningDetails == null || pkg.mSigningDetails.signatures == null || pkg.mSigningDetails.signatures.length <= 0) {
            return false;
        }
        return true;
    }

    private String sha256(byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            return bytesToString(md.digest());
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
        if (cacheApps != null && !cacheApps.isEmpty()) {
            Iterator<MySysAppInfo> it = cacheApps.iterator();
            while (it.hasNext()) {
                MySysAppInfo app = it.next();
                if (app.getPkgName().equalsIgnoreCase(pkgName) && (pkgSignature == null || app.getPkgSignature().equalsIgnoreCase(pkgSignature))) {
                    return app;
                }
            }
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x002f A[Catch:{ FileNotFoundException -> 0x008e, XmlPullParserException -> 0x0080, IOException -> 0x0072, all -> 0x0070 }] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0044 A[SYNTHETIC, Splitter:B:16:0x0044] */
    private void readSysInfoFromDevicePolicyXml() {
        int type;
        FileInputStream str = null;
        File sysPackageFile = new File(Environment.getDataSystemDirectory(), DEVICE_POLICIES_XML);
        if (sysPackageFile.exists()) {
            try {
                str = new FileInputStream(sysPackageFile);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(str, null);
                while (true) {
                    int next = parser.next();
                    type = next;
                    if (next == 2 || type == 1) {
                        if (type == 2) {
                            Slog.e(TAG, "No start tag found in package file");
                            try {
                                str.close();
                            } catch (IOException e) {
                                Slog.e(TAG, "Unable to close the str");
                            }
                            return;
                        }
                        int outerDepth = parser.getDepth();
                        while (true) {
                            int next2 = parser.next();
                            int type2 = next2;
                            if (next2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                                try {
                                    break;
                                } catch (IOException e2) {
                                    Slog.e(TAG, "Unable to close the str");
                                }
                            } else if (type2 != 3) {
                                if (type2 != 4) {
                                    parsePolicyFile(parser);
                                }
                            }
                        }
                        return;
                    }
                }
                if (type == 2) {
                }
            } catch (FileNotFoundException e3) {
                Slog.e(TAG, "FileNotFoundException when try to parse device_policies");
                if (str != null) {
                    str.close();
                }
            } catch (XmlPullParserException e4) {
                Slog.e(TAG, "XmlPullParserException when try to parse device_policies");
                if (str != null) {
                    str.close();
                }
            } catch (IOException e5) {
                Slog.e(TAG, "IOException when try to parse device_policies");
                if (str != null) {
                    str.close();
                }
            } catch (Throwable th) {
                if (str != null) {
                    try {
                        str.close();
                    } catch (IOException e6) {
                        Slog.e(TAG, "Unable to close the str");
                    }
                }
                throw th;
            }
        }
    }

    private void parsePolicyFile(XmlPullParser parser) {
        XmlPullParser xmlPullParser = parser;
        String tagName = parser.getName();
        if (TAG_SPECIAL_POLICY_SYS_APP_LIST.equals(tagName)) {
            String pkgInfoList = XmlUtils.readStringAttribute(xmlPullParser, "value");
            if (!TextUtils.isEmpty(pkgInfoList)) {
                for (String sysPkg : pkgInfoList.split(";")) {
                    String[] infoList = sysPkg.split(SEPARATOR);
                    if (infoList.length == 4) {
                        MySysAppInfo app = new MySysAppInfo(infoList[0], infoList[1], infoList[2], infoList[3]);
                        MDM_SYSTEM_APPS.add(app);
                    }
                }
                Slog.d(TAG, "read " + pkgInfoList);
            }
        } else if (TAG_SPECIAL_POLICY_UNDETACHABLE_SYS_APP_LIST.equals(tagName)) {
            String pkgInfoList2 = XmlUtils.readStringAttribute(xmlPullParser, "value");
            if (!TextUtils.isEmpty(pkgInfoList2)) {
                for (String sysPkg2 : pkgInfoList2.split(";")) {
                    String[] infoList2 = sysPkg2.split(SEPARATOR);
                    if (infoList2.length == 4) {
                        MySysAppInfo app2 = new MySysAppInfo(infoList2[0], infoList2[1], infoList2[2], infoList2[3]);
                        MDM_SYSTEM_UNDETACHABLE_APPS.add(app2);
                    }
                }
                Slog.d(TAG, "read " + pkgInfoList2);
            }
        }
    }

    public void handleActivityInfoNotFound(int flags, Intent intent, int callingUid, List<ResolveInfo> list) {
        if (this.mHwFastAppManager != null) {
            this.mHwFastAppManager.updateActivityInfo(flags, intent, callingUid, list);
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasOtaUpdate() {
        try {
            UserInfo userInfo = PackageManagerService.sUserManager.getUserInfo(0);
            if (userInfo == null) {
                return false;
            }
            Log.i(TAG, "userInfo.lastLoggedInFingerprint : " + userInfo.lastLoggedInFingerprint + ", Build.FINGERPRINT : " + Build.FINGERPRINT);
            return !Objects.equals(userInfo.lastLoggedInFingerprint, Build.FINGERPRINT);
        } catch (Exception e) {
            Log.i(TAG, "Exception is " + e);
            return false;
        }
    }

    public void filterShellApps(ArrayList<PackageParser.Package> pkgs, LinkedList<PackageParser.Package> sortedPkgs) {
        Installer mInstaller = this.mIPmsInner.getInstallerInner();
        if ((!hasOtaUpdate() || this.mIPmsInner.isFirstBootInner()) && !hasPrunedDalvikCache()) {
            Slog.i(TAG, "Do not filt shell Apps! not OTA case.");
            return;
        }
        HwShellAppsHandler handler = new HwShellAppsHandler(mInstaller, PackageManagerService.sUserManager);
        Iterator<PackageParser.Package> it = pkgs.iterator();
        while (it.hasNext()) {
            PackageParser.Package pkg = it.next();
            String shellName = handler.analyseShell(pkg);
            if (shellName != null) {
                if (DEBUG_DEXOPT_SHELL) {
                    Log.i(TAG, "Find a " + shellName + " Shell Pkgs: " + pkg.packageName);
                }
                sortedPkgs.add(pkg);
                handler.processShellApp(pkg);
            }
        }
        pkgs.removeAll(sortedPkgs);
    }

    private boolean hasPrunedDalvikCache() {
        if (new File(Environment.getDataDirectory(), "system/.dalvik-cache-pruned").exists()) {
            return true;
        }
        return false;
    }

    public boolean isMDMDisallowedInstallPackage(PackageParser.Package pkg, PackageManagerService.PackageInstalledInfo res) {
        if (!HwDeviceManager.disallowOp(19, pkg.packageName)) {
            return false;
        }
        final String pkgName1 = PackageManagerService.getCallingAppName(this.mContext, pkg);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Toast.makeText(HwPackageManagerServiceEx.this.mContext, HwPackageManagerServiceEx.this.mContext.getResources().getString(33685933, new Object[]{pkgName1}), 0).show();
            }
        }, 500);
        res.setError(RequestStatus.SYS_ETIMEDOUT, "app is in the installpackage_blacklist");
        return true;
    }

    public void hwAddRequirementForDefaultHome(IntentFilter filter, ComponentName activity, int userId) {
        if (filter.hasCategory("android.intent.category.HOME")) {
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "temporary_home_mode", 0) == 1) {
                Slog.i(TAG, "Skip killing last non default home because the new default home is temporary");
                return;
            }
            doKillNondefaultHome(activity.getPackageName(), userId);
        }
    }

    private void doKillNondefaultHome(String defaultHome, int userId) {
        List<ResolveInfo> resolveInfos = this.mIPmsInner.queryIntentActivitiesInternalInner(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT"), null, 128, userId);
        IActivityManager am = ActivityManagerNative.getDefault();
        for (ResolveInfo info : resolveInfos) {
            String homePkg = info.activityInfo.packageName;
            Bundle metaData = info.activityInfo.metaData;
            boolean isKeepAlive = false;
            if (metaData != null) {
                isKeepAlive = metaData.getBoolean(META_KEY_KEEP_ALIVE, false);
            }
            if (!homePkg.equals(defaultHome)) {
                if (isKeepAlive) {
                    Slog.i(TAG, "Skip killing package : " + homePkg);
                } else if (2000 == Binder.getCallingUid() || this.mIPmsInner.getNameForUidInner(Binder.getCallingUid()) == null) {
                    Slog.i(TAG, "Skip killing package when calling from thread whose pgkname is null: " + homePkg);
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

    public ResolveInfo hwFindPreferredActivity(Intent intent, String resolvedType, int flags, List<ResolveInfo> query, int priority, boolean always, boolean removeMatches, boolean debug, int userId) {
        List<ResolveInfo> list = query;
        boolean isDefaultPreferredActivityChanged = this.mIPmsInner.getIsDefaultPreferredActivityChangedInner();
        HwCustPackageManagerService custPackageManagerService = this.mIPmsInner.getHwPMSCustPackageManagerService();
        boolean isDefaultGoogleCalendar = this.mIPmsInner.getIsDefaultGoogleCalendarInner();
        Intent intent2 = intent;
        if (intent2.hasCategory("android.intent.category.HOME") && list != null && query.size() > 1) {
            if (!SystemProperties.getBoolean("ro.config.pref.hw_launcher", true)) {
                int num = query.size() - 1;
                List<String> whiteListLauncher = new ArrayList<>();
                whiteListLauncher.add(GestureNavConst.DEFAULT_LAUNCHER_PACKAGE);
                whiteListLauncher.add("com.huawei.kidsmode");
                whiteListLauncher.add("com.android.settings");
                while (num >= 0) {
                    int num2 = num - 1;
                    ResolveInfo info = list.get(num);
                    if (info.activityInfo == null || whiteListLauncher.contains(info.activityInfo.applicationInfo.packageName)) {
                        num = num2;
                    } else {
                        HwSlog.v(TAG, "return default Launcher null");
                        return null;
                    }
                }
            }
            int index = query.size() - 1;
            while (index >= 0) {
                int index2 = index - 1;
                ResolveInfo info2 = list.get(index);
                String defaultLauncher = GestureNavConst.DEFAULT_LAUNCHER_PACKAGE;
                if (!(custPackageManagerService == null || info2.activityInfo == null)) {
                    String custDefaultLauncher = custPackageManagerService.getCustDefaultLauncher(this.mContext, info2.activityInfo.applicationInfo.packageName);
                    if (!TextUtils.isEmpty(custDefaultLauncher)) {
                        defaultLauncher = custDefaultLauncher;
                    }
                }
                if (info2.activityInfo == null || !info2.activityInfo.applicationInfo.packageName.equals(defaultLauncher)) {
                    index = index2;
                } else {
                    HwSlog.v(TAG, "Returning system default Launcher ");
                    return info2;
                }
            }
        }
        if (intent2.getAction() != null && intent2.getAction().equals("android.intent.action.DIAL") && intent2.getData() != null && intent2.getData().getScheme() != null && intent2.getData().getScheme().equals("tel") && list != null && query.size() > 1) {
            return getSpecificPreferredActivity(list, true, "com.android.contacts");
        }
        if (intent2.getAction() != null && intent2.getAction().equals("android.intent.action.VIEW") && intent2.getData() != null && intent2.getData().getScheme() != null && ((intent2.getData().getScheme().equals("file") || intent2.getData().getScheme().equals("content")) && intent2.getType() != null && intent2.getType().startsWith("image/") && list != null && query.size() > 1)) {
            return getSpecificPreferredActivity(list, false, "com.android.gallery3d");
        }
        if (intent2.getAction() != null && intent2.getAction().equals("android.intent.action.VIEW") && intent2.getData() != null && intent2.getData().getScheme() != null && ((intent2.getData().getScheme().equals("file") || intent2.getData().getScheme().equals("content")) && intent2.getType() != null && intent2.getType().startsWith("audio/") && list != null && query.size() > 1)) {
            return getSpecificPreferredActivity(list, false, "com.android.mediacenter");
        }
        if (intent2.getAction() != null && intent2.getAction().equals("android.media.action.IMAGE_CAPTURE") && list != null && query.size() > 1) {
            return getSpecificPreferredActivity(list, false, MemoryConstant.CAMERA_PACKAGE_NAME);
        }
        if (intent2.getAction() == null || !intent2.getAction().equals("android.intent.action.VIEW") || intent2.getData() == null || intent2.getData().getScheme() == null || !intent2.getData().getScheme().equals("mailto") || list == null || query.size() <= 1) {
            Log.i(TAG, "!isDefaultPreferredActivityChanged= " + (!isDefaultPreferredActivityChanged) + " ,isDefaultGoogleCalendar= " + isDefaultGoogleCalendar);
            if (!isDefaultPreferredActivityChanged && isDefaultGoogleCalendar && isCalendarType(intent2) && list != null && query.size() > 1) {
                return getSpecificPreferredActivity(list, true, "com.google.android.calendar");
            }
            if (intent2.getAction() == null || !intent2.getAction().equals("android.intent.action.VIEW") || intent2.getData() == null || !intent2.getData().toString().startsWith("market://details") || intent2.getData().getScheme() == null || !intent2.getData().getScheme().equals("market") || list == null || query.size() <= 1) {
                return null;
            }
            String defaultAppMarket = Settings.Global.getString(this.mContext.getContentResolver(), "default_appmarket");
            if (TextUtils.isEmpty(defaultAppMarket)) {
                defaultAppMarket = SystemProperties.get("ro.config.default_appmarket", "");
            }
            Log.i(TAG, "find default appmarket is : " + defaultAppMarket);
            return getSpecificPreferredActivity(list, false, defaultAppMarket);
        } else if (intent2.getCategories() == null || intent2.getCategories().size() <= 0) {
            return getSpecificPreferredActivity(list, false, "com.android.email");
        } else {
            return null;
        }
    }

    private ResolveInfo getSpecificPreferredActivity(List<ResolveInfo> query, boolean checkPriorityFlag, String specificPackageName) {
        int index = query.size() - 1;
        while (index >= 0) {
            int index2 = index - 1;
            ResolveInfo info = query.get(index);
            if ((!checkPriorityFlag || info.priority >= 0) && info.activityInfo != null && info.activityInfo.applicationInfo.packageName.equals(specificPackageName)) {
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
        if ("android.intent.action.VIEW".equals(action) && data != null && ("http".equals(data.getScheme()) || HwNetworkPropertyChecker.NetworkCheckerThread.TYPE_HTTPS.equals(data.getScheme())) && "www.google.com".equals(data.getHost()) && data.getPath() != null && (data.getPath().startsWith("/calendar/event") || (data.getPath().startsWith("/calendar/hosted") && data.getPath().endsWith("/event")))) {
            return true;
        }
        if ("android.intent.action.VIEW".equals(action) && "text/calendar".equals(type)) {
            return true;
        }
        if ("android.intent.action.VIEW".equals(action) && "time/epoch".equals(type)) {
            return true;
        }
        if ("android.intent.action.VIEW".equals(action) && data != null && "content".equals(data.getScheme()) && "com.android.calendar".equals(data.getHost())) {
            return true;
        }
        return false;
    }

    private static boolean firstScan() {
        boolean exists = new File(Environment.getDataDirectory(), "system/packages.xml").exists();
        Slog.i(TAG, "is first scan?" + (!exists));
        return !exists;
    }

    private int startBackupSession(IBackupSessionCallback callback) {
        getHwFileBackupManager();
        Slog.i(TAG, "application bind call startBackupSession");
        if (!checkBackupSessionCaller()) {
            return -2;
        }
        return this.mHwFileBackupManager.startBackupSession(callback);
    }

    private int executeBackupTask(int sessionId, String taskCmd) {
        getHwFileBackupManager();
        Slog.i(TAG, "bind call executeBackupTask on session:" + sessionId);
        if (!checkBackupSessionCaller()) {
            return -2;
        }
        return this.mHwFileBackupManager.executeBackupTask(sessionId, this.mHwFileBackupManager.prepareBackupTaskCmd(taskCmd, this.mIPmsInner.getPackagesLock()));
    }

    private int finishBackupSession(int sessionId) {
        getHwFileBackupManager();
        Slog.i(TAG, "bind call finishBackupSession sessionId:" + sessionId);
        if (!checkBackupSessionCaller()) {
            return -2;
        }
        return this.mHwFileBackupManager.finishBackupSession(sessionId);
    }

    private boolean checkBackupSessionCaller() {
        getHwFileBackupManager();
        int callingUid = Binder.getCallingUid();
        boolean z = true;
        if (callingUid == 1001) {
            return true;
        }
        String pkgName = this.mIPmsInner.getNameForUidInner(callingUid);
        if (!this.mHwFileBackupManager.checkBackupPackageName(pkgName) || !isPlatformSignatureApp(pkgName)) {
            z = false;
        }
        return z;
    }

    public boolean isPlatformSignatureApp(String pkgName) {
        boolean result = this.mIPmsInner.checkSignaturesInner("android", pkgName) == 0;
        if (!result) {
            Slog.d(TAG, "is not platform signature app, pkgName is " + pkgName);
        }
        return result;
    }

    private void getHwFileBackupManager() {
        if (this.mHwFileBackupManager == null) {
            this.mHwFileBackupManager = HwFileBackupManager.getInstance(this.mIPmsInner.getInstallerInner());
        }
    }

    private void getAPKInstallList(List<File> lists, HashMap<String, HashSet<String>> multiInstallMap) {
        HwPreAppManager.getInstance(this).getMultiAPKInstallList(lists, multiInstallMap);
    }

    private void installAPKforInstallList(HashSet<String> installList, int flags, int scanMode, long currentTime) {
        installAPKforInstallList(installList, flags, scanMode, currentTime, 0);
    }

    public void installAPKforInstallList(HashSet<String> installList, int parseFlags, int scanFlags, long currentTime, int hwFlags) {
        if (installList != null && installList.size() != 0) {
            if (this.mIPmsInner.getCotaFlagInner()) {
                this.mIPmsInner.setHwPMSCotaApksInstallStatus(-1);
            }
            int fileSize = installList.size();
            File[] files = new File[fileSize];
            Iterator<String> it = installList.iterator();
            int i = 0;
            while (it.hasNext() != 0) {
                File file = new File(it.next());
                if (i < fileSize) {
                    files[i] = file;
                    Flog.i(205, "add package install path : " + installPath);
                    i++;
                } else {
                    Slog.w(TAG, "faile to add package install path : " + installPath + "fileSize:" + fileSize + ",i:" + i);
                }
            }
            this.mIPmsInner.scanPackageFilesLIInner(files, parseFlags, scanFlags, currentTime, hwFlags);
        }
    }

    public boolean isDelappInData(PackageSetting ps) {
        return HwPreAppManager.getInstance(this).isDelappInData(ps);
    }

    public boolean isUninstallApk(String filePath) {
        return HwForbidUninstallManager.getInstance(this).isUninstallApk(filePath);
    }

    public void getUninstallApk() {
        HwForbidUninstallManager.getInstance(this).getUninstallApk();
    }

    public synchronized HwCustPackageManagerService getCust() {
        if (this.mCust == null) {
            this.mCust = HwCustUtils.createObj(HwCustPackageManagerService.class, new Object[0]);
        }
        return (HwCustPackageManagerService) this.mCust;
    }

    public boolean isHwCustHiddenInfoPackage(PackageParser.Package pkgInfo) {
        if (getCust() != null) {
            return getCust().isHwCustHiddenInfoPackage(pkgInfo);
        }
        return false;
    }

    public boolean isSetupDisabled() {
        return this.mSetupDisabled;
    }

    private boolean needSkipSetupPhase() {
        return BluetoothAddressNative.isLibReady() && TextUtils.isEmpty(BluetoothAddressNative.getMacAddress());
    }

    private boolean isSetupPkg(String pname) {
        return HWSETUP_PKG.equals(pname) || GOOGLESETUP_PKG.equals(pname);
    }

    public boolean skipSetupEnable(String pname) {
        boolean shouldSkip = isSetupPkg(pname) && needSkipSetupPhase();
        if (shouldSkip) {
            Slog.i(TAG, "skipSetupEnable skip pkg: " + pname);
        }
        return shouldSkip;
    }

    public boolean makeSetupDisabled(String pname) {
        if (!isSetupPkg(pname) || this.mIPmsInner.getSettings().isDisabledSystemPackageLPr(pname) || !needSkipSetupPhase()) {
            return false;
        }
        this.mIPmsInner.getSettings().disableSystemPackageLPw(pname);
        this.mSetupDisabled = true;
        Slog.w(TAG, "makeSetupDisabled skip pkg: " + pname);
        return true;
    }

    public void setUpCustomResolverActivity(PackageParser.Package pkg) {
        synchronized (this.mIPmsInner.getPackagesLock()) {
            ActivityInfo mResolveActivity = this.mIPmsInner.getResolveActivityInner();
            this.mIPmsInner.setUpCustomResolverActivityInner(pkg);
            if (!TextUtils.isEmpty(HwFrameworkFactory.getHuaweiResolverActivity(this.mContext))) {
                mResolveActivity.processName = "system:ui";
                mResolveActivity.theme = 16974813;
            }
        }
    }

    public static boolean firstScanForHwPMS() {
        return firstScan();
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

    public void getAPKInstallListForHwPMS(List<File> lists, HashMap<String, HashSet<String>> multiInstallMap) {
        getAPKInstallList(lists, multiInstallMap);
    }

    public void installAPKforInstallListForHwPMS(HashSet<String> installList, int flags, int scanMode, long currentTime) {
        installAPKforInstallList(installList, flags, scanMode, currentTime);
    }

    public void installAPKforInstallListForHwPMS(HashSet<String> installList, int parseFlags, int scanFlags, long currentTime, int hwFlags) {
        installAPKforInstallList(installList, parseFlags, scanFlags, currentTime, hwFlags);
    }

    private void setCurrentEmuiSysImgVersion() {
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(1);
        }
    }

    private void deleteSoundrecorder() {
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(2);
        }
    }

    /* access modifiers changed from: private */
    public void deleteSoundercorderIfNeed() {
        Log.i(TAG, "begin uninstall soundrecorder");
        if ("0".equals(SystemProperties.get("persist.sys.uninstallapk", "0"))) {
            try {
                this.mIPmsInner.deletePackageInner(HW_SOUND_RECORDER, -1, 0, 2);
                Log.i(TAG, "uninstall soundrecorder ...");
            } catch (Exception e) {
                Log.e(TAG, "uninstall soundrecorder error");
            }
            SystemProperties.set("persist.sys.uninstallapk", "1");
        }
    }

    /* access modifiers changed from: private */
    public static int deriveEmuiSysImgVersion() {
        try {
            String str = ManufactureNativeUtils.getVersionInfo(3);
            Slog.d(TAG, "deriveEmuiSysImgVersion, version info is " + str);
            if (TextUtils.isEmpty(str)) {
                return 0;
            }
            String ret = "";
            Matcher matcher = Pattern.compile("(\\d+\\.){3}\\d+").matcher(str);
            if (matcher.find()) {
                ret = matcher.group().trim();
            }
            Slog.d(TAG, "deriveEmuiSysImgVersion,find:" + ret);
            if (TextUtils.isEmpty(ret)) {
                return 0;
            }
            int version = Integer.parseInt(ret.replace(".", ""));
            Slog.d(TAG, "deriveEmuiSysImgVersion,version:" + version);
            return version;
        } catch (Exception e) {
            Slog.w(TAG, "deriveEmuiSysImgVersion error:" + e.getMessage());
            return 0;
        }
    }

    private void initPackageBlackList() {
        boolean isCompleteProcess;
        BlackListAppsUtils.readBlackList(this.mBlackListInfo);
        synchronized (this.mIPmsInner.getPackagesLock()) {
            BlackListAppsUtils.readDisableAppList(this.mDisableAppListInfo);
            Iterator<BlackListInfo.BlackListApp> it = this.mDisableAppListInfo.mBlackList.iterator();
            while (it.hasNext()) {
                BlackListInfo.BlackListApp app = it.next();
                this.mDisableAppMap.put(app.mPackageName, app);
            }
        }
        this.isBlackListExist = (this.mBlackListInfo.mBlackList.size() == 0 || this.mBlackListInfo.mVersionCode == -1) ? false : true;
        if (!this.isBlackListExist) {
            synchronized (this.mIPmsInner.getPackagesLock()) {
                if (this.mDisableAppMap.size() > 0) {
                    Slog.i(TAG, "blacklist not exists, enable all disabled apps");
                    Set<String> needEnablePackage = new ArraySet<>();
                    for (Map.Entry<String, BlackListInfo.BlackListApp> entry : this.mDisableAppMap.entrySet()) {
                        needEnablePackage.add(entry.getKey());
                    }
                    enableComponentForAllUser(needEnablePackage, true);
                    this.mDisableAppMap.clear();
                }
            }
            BlackListAppsUtils.deleteDisableAppListFile();
            return;
        }
        synchronized (this.mIPmsInner.getPackagesLock()) {
            if (!this.mIPmsInner.isUpgrade() && !BlackListAppsUtils.isBlackListUpdate(this.mBlackListInfo, this.mDisableAppListInfo)) {
                if (validateDisabledAppFile()) {
                    isCompleteProcess = false;
                }
            }
            isCompleteProcess = true;
        }
        Slog.i(TAG, "initBlackList start, is completed process: " + isCompleteProcess);
        if (isCompleteProcess) {
            synchronized (this.mIPmsInner.getPackagesLock()) {
                Set<String> needDisablePackage = new ArraySet<>();
                Set<String> needEnablePackage2 = new ArraySet<>();
                Iterator<BlackListInfo.BlackListApp> it2 = this.mBlackListInfo.mBlackList.iterator();
                while (it2.hasNext()) {
                    BlackListInfo.BlackListApp app2 = it2.next();
                    String pkg = app2.mPackageName;
                    if (!needDisablePackage.contains(pkg)) {
                        if (BlackListAppsUtils.comparePackage((PackageParser.Package) this.mIPmsInner.getPackagesLock().get(pkg), app2) && !needDisablePackage.contains(pkg)) {
                            setPackageDisableFlag(pkg, true);
                            needDisablePackage.add(pkg);
                            this.mDisableAppMap.put(pkg, app2);
                        }
                    }
                }
                for (String pkg2 : new ArrayList<>(this.mDisableAppMap.keySet())) {
                    if (!BlackListAppsUtils.containsApp(this.mBlackListInfo.mBlackList, this.mDisableAppMap.get(pkg2)) && !needDisablePackage.contains(pkg2)) {
                        if (this.mIPmsInner.getPackagesLock().get(pkg2) != null) {
                            needEnablePackage2.add(pkg2);
                        }
                        this.mDisableAppMap.remove(pkg2);
                    }
                }
                enableComponentForAllUser(needEnablePackage2, true);
                enableComponentForAllUser(needDisablePackage, false);
                this.mDisableAppListInfo.mBlackList.clear();
                for (Map.Entry<String, BlackListInfo.BlackListApp> entry2 : this.mDisableAppMap.entrySet()) {
                    this.mDisableAppListInfo.mBlackList.add(entry2.getValue());
                }
                this.mDisableAppListInfo.mVersionCode = this.mBlackListInfo.mVersionCode;
                BlackListAppsUtils.writeBlackListToXml(this.mDisableAppListInfo);
            }
        } else {
            Set<String> needDisablePackage2 = new ArraySet<>();
            for (Map.Entry<String, BlackListInfo.BlackListApp> entry3 : this.mDisableAppMap.entrySet()) {
                setPackageDisableFlag(entry3.getKey(), true);
                needDisablePackage2.add(entry3.getKey());
            }
            enableComponentForAllUser(needDisablePackage2, false);
        }
        Slog.i(TAG, "initBlackList end");
    }

    private boolean validateDisabledAppFile() {
        if (this.mBlackListInfo.mBlackList.size() == 0) {
            return false;
        }
        synchronized (this.mIPmsInner.getPackagesLock()) {
            for (Map.Entry<String, BlackListInfo.BlackListApp> entry : this.mDisableAppMap.entrySet()) {
                if (this.mIPmsInner.getPackagesLock().get(entry.getKey()) == null) {
                    return false;
                }
                if (!BlackListAppsUtils.containsApp(this.mBlackListInfo.mBlackList, entry.getValue())) {
                    return false;
                }
            }
            return true;
        }
    }

    private void enableComponentForAllUser(Set<String> packages, boolean enable) {
        for (int userId : UserManagerService.getInstance().getUserIds()) {
            if (packages != null && packages.size() > 0) {
                for (String pkg : packages) {
                    enableComponentForPackage(pkg, enable, userId);
                }
            }
        }
    }

    private void setPackageDisableFlag(String packageName, boolean disable) {
        if (!TextUtils.isEmpty(packageName)) {
            PackageParser.Package pkg = (PackageParser.Package) this.mIPmsInner.getPackagesLock().get(packageName);
            if (pkg != null) {
                if (disable) {
                    pkg.applicationInfo.hwFlags |= 268435456;
                } else {
                    pkg.applicationInfo.hwFlags |= -268435457;
                }
            }
        }
    }

    private void enableComponentForPackage(String packageName, boolean enable, int userId) {
        if (!TextUtils.isEmpty(packageName)) {
            int newState = enable ? 0 : 2;
            try {
                PackageInfo packageInfo = AppGlobals.getPackageManager().getPackageInfo(packageName, 786959, userId);
                if (!(packageInfo == null || packageInfo.receivers == null || packageInfo.receivers.length == 0)) {
                    for (ActivityInfo activityInfo : packageInfo.receivers) {
                        setEnabledComponentInner(new ComponentName(packageName, activityInfo.name), newState, userId);
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
                    for (ActivityInfo activityInfo2 : packageInfo.activities) {
                        setEnabledComponentInner(new ComponentName(packageName, activityInfo2.name), newState, userId);
                    }
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "BlackList: Get packageInfo fail, name=" + packageName);
            }
        }
    }

    public void updatePackageBlackListInfo(String packageName) {
        if (this.isBlackListExist && !TextUtils.isEmpty(packageName)) {
            int[] userIds = UserManagerService.getInstance().getUserIds();
            synchronized (this.mIPmsInner.getPackagesLock()) {
                PackageParser.Package pkgInfo = (PackageParser.Package) this.mIPmsInner.getPackagesLock().get(packageName);
                boolean needDisable = false;
                boolean needEnable = false;
                if (pkgInfo != null) {
                    Iterator<BlackListInfo.BlackListApp> it = this.mBlackListInfo.mBlackList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        BlackListInfo.BlackListApp app = it.next();
                        if (BlackListAppsUtils.comparePackage(pkgInfo, app)) {
                            setPackageDisableFlag(packageName, true);
                            this.mDisableAppMap.put(packageName, app);
                            needDisable = true;
                            break;
                        }
                    }
                    if (!needDisable && this.mDisableAppMap.containsKey(packageName)) {
                        setPackageDisableFlag(packageName, false);
                        this.mDisableAppMap.remove(packageName);
                        needEnable = true;
                    }
                } else if (this.mDisableAppMap.containsKey(packageName)) {
                    this.mDisableAppMap.remove(packageName);
                }
                for (int userId : userIds) {
                    if (needDisable) {
                        enableComponentForPackage(packageName, false, userId);
                    } else if (needEnable) {
                        enableComponentForPackage(packageName, true, userId);
                    }
                }
                this.mDisableAppListInfo.mBlackList.clear();
                for (Map.Entry<String, BlackListInfo.BlackListApp> entry : this.mDisableAppMap.entrySet()) {
                    this.mDisableAppListInfo.mBlackList.add(entry.getValue());
                }
                this.mDisableAppListInfo.mVersionCode = this.mBlackListInfo.mVersionCode;
                BlackListAppsUtils.writeBlackListToXml(this.mDisableAppListInfo);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x007a, code lost:
        return;
     */
    private void setEnabledComponentInner(ComponentName componentName, int newState, int userId) {
        if (componentName != null) {
            String packageName = componentName.getPackageName();
            String className = componentName.getClassName();
            if (packageName != null && className != null) {
                synchronized (this.mIPmsInner.getPackagesLock()) {
                    PackageSetting pkgSetting = (PackageSetting) this.mIPmsInner.getSettings().mPackages.get(packageName);
                    if (pkgSetting == null) {
                        Slog.e(TAG, "setEnabledSetting, can not find pkgSetting, packageName = " + packageName);
                        return;
                    }
                    PackageParser.Package pkg = pkgSetting.pkg;
                    if (pkg != null) {
                        if (pkg.hasComponentClassName(className)) {
                            if (newState != 0) {
                                if (newState != 2) {
                                    Slog.e(TAG, "Invalid new component state: " + newState);
                                    return;
                                } else if (!pkgSetting.disableComponentLPw(className, userId)) {
                                    return;
                                }
                            } else if (!pkgSetting.restoreComponentLPw(className, userId)) {
                                return;
                            }
                        }
                    }
                    Slog.w(TAG, "Failed setComponentEnabledSetting: component class " + className + " does not exist in " + packageName);
                }
            }
        }
    }

    private void initBlackListForNewUser(int userHandle) {
        if (this.isBlackListExist) {
            synchronized (this.mIPmsInner.getPackagesLock()) {
                for (String pkg : this.mDisableAppMap.keySet()) {
                    enableComponentForPackage(pkg, false, userHandle);
                }
            }
        }
    }

    public void onNewUserCreated(int userId) {
        initBlackListForNewUser(userId);
    }

    public void replaceSignatureIfNeeded(PackageSetting ps, PackageParser.Package pkg, boolean isBootScan, boolean isUpdate) {
        if (pkg != null && this.mCompatSettings != null) {
            if (!isBootScan) {
                synchronized (this.mIncompatiblePkg) {
                    if (this.mIncompatiblePkg.contains(pkg.packageName)) {
                        this.mIncompatiblePkg.remove(pkg.packageName);
                    }
                }
            }
            boolean needReplace = false;
            String packageSignType = null;
            if (isBootScan && ps != null) {
                synchronized (this.mIPmsInner.getPackagesLock()) {
                    CertCompatSettings.Package compatPkg = this.mCompatSettings.getCompatPackage(pkg.packageName);
                    if (compatPkg != null && compatPkg.codePath.equals(ps.codePathString) && compatPkg.timeStamp == ps.timeStamp) {
                        needReplace = true;
                        packageSignType = compatPkg.certType;
                    }
                }
            }
            if (!needReplace && HwCertificationManager.isSupportHwCertification(pkg)) {
                switch (getHwCertificateType(pkg)) {
                    case 1:
                        packageSignType = "platform";
                        break;
                    case 2:
                        packageSignType = "testkey";
                        break;
                    case 3:
                        packageSignType = "shared";
                        break;
                    case 4:
                        packageSignType = "media";
                        break;
                }
                if (packageSignType != null) {
                    needReplace = true;
                    Slog.i(TAG, "CertCompat: system signature compat for hwcert package:" + pkg.packageName + ",type:" + packageSignType);
                }
            }
            boolean isSignedByOldSystemSignature = this.mCompatSettings.isOldSystemSignature(pkg.mSigningDetails.signatures);
            if (!needReplace && isSignedByOldSystemSignature && this.mCompatSettings.isWhiteListedApp(pkg)) {
                packageSignType = this.mCompatSettings.getOldSignTpye(pkg.mSigningDetails.signatures);
                needReplace = true;
                Slog.i(TAG, "CertCompat: system signature compat for whitelist package:" + pkg.packageName + ",type:" + packageSignType);
                if (!isBootScan) {
                    Context context = this.mContext;
                    Flog.bdReport(context, CPUFeature.MSG_SET_CPUSETCONFIG_VR, "{package:" + pkg.packageName + ", version:" + pkg.mVersionCode + ", type:" + packageSignType + "}");
                }
            }
            if (!needReplace && isSignedByOldSystemSignature && isBootScan && !this.mFoundCertCompatFile && this.mIPmsInner.isUpgrade()) {
                packageSignType = this.mCompatSettings.getOldSignTpye(pkg.mSigningDetails.signatures);
                needReplace = true;
                Slog.i(TAG, "CertCompat: system signature compat for OTA package:" + pkg.packageName);
            }
            if (needReplace) {
                replaceSignatureInner(ps, pkg, packageSignType);
            } else if (isSignedByOldSystemSignature && !isBootScan) {
                synchronized (this.mIncompatiblePkg) {
                    if (!this.mIncompatiblePkg.contains(pkg.packageName)) {
                        this.mIncompatiblePkg.add(pkg.packageName);
                    }
                }
                String packageSignType2 = this.mCompatSettings.getOldSignTpye(pkg.mSigningDetails.signatures);
                Slog.i(TAG, "CertCompat: illegal system signature package:" + pkg.packageName + ",type:" + packageSignType2);
                Context context2 = this.mContext;
                Flog.bdReport(context2, 124, "{package:" + pkg.packageName + ",version:" + pkg.mVersionCode + ",type:" + packageSignType2 + "}");
            }
        }
    }

    private void replaceSignatureInner(PackageSetting ps, PackageParser.Package pkg, String signType) {
        if (signType != null && pkg != null && this.mCompatSettings != null) {
            Signature[] signs = this.mCompatSettings.getNewSign(signType);
            if (signs.length == 0) {
                Slog.e(TAG, "CertCompat: signs init fail");
                return;
            }
            PackageParser.SigningDetails newSignDetails = createNewSigningDetails(pkg.mSigningDetails, signs);
            this.mIPmsInner.setRealSigningDetails(pkg, pkg.mSigningDetails);
            pkg.mSigningDetails = newSignDetails;
            if (ps != null && ps.signatures.mSigningDetails.hasSignatures()) {
                ps.signatures.mSigningDetails = pkg.mSigningDetails;
            }
            Slog.d(TAG, "CertCompat: CertCompatPackage:" + pkg.packageName);
        }
    }

    private PackageParser.SigningDetails createNewSigningDetails(PackageParser.SigningDetails orig, Signature[] newSigns) {
        PackageParser.SigningDetails signingDetails = new PackageParser.SigningDetails(newSigns, orig.signatureSchemeVersion, orig.publicKeys, orig.pastSigningCertificates, orig.pastSigningCertificatesFlags);
        return signingDetails;
    }

    public void initCertCompatSettings() {
        Slog.i(TAG, "CertCompat: init CertCompatSettings");
        this.mCompatSettings = new CertCompatSettings();
        this.mFoundCertCompatFile = this.mCompatSettings.readCertCompatPackages();
    }

    public void resetSharedUserSignaturesIfNeeded() {
        if (this.mCompatSettings != null && !this.mFoundCertCompatFile && this.mIPmsInner.isUpgrade()) {
            for (SharedUserSetting setting : this.mIPmsInner.getSettings().getAllSharedUsersLPw()) {
                if (this.mCompatSettings.isOldSystemSignature(setting.signatures.mSigningDetails.signatures)) {
                    setting.signatures.mSigningDetails = PackageParser.SigningDetails.UNKNOWN;
                    Slog.i(TAG, "CertCompat: SharedUser:" + setting.name + " signature reset!");
                }
            }
        }
    }

    @SuppressLint({"PreferForInArrayList"})
    public void writeCertCompatPackages(boolean update) {
        if (this.mCompatSettings != null) {
            if (update) {
                Iterator<CertCompatSettings.Package> it = new ArrayList<>(this.mCompatSettings.getALLCompatPackages()).iterator();
                while (it.hasNext()) {
                    CertCompatSettings.Package pkg = it.next();
                    if (pkg != null && !this.mIPmsInner.getPackagesLock().containsKey(pkg.packageName)) {
                        this.mCompatSettings.removeCertCompatPackage(pkg.packageName);
                    }
                }
            }
            this.mCompatSettings.writeCertCompatPackages();
        }
    }

    public void updateCertCompatPackage(PackageParser.Package pkg, PackageSetting ps) {
        if (pkg != null && this.mCompatSettings != null) {
            Signature[] realSign = this.mIPmsInner.getRealSignature(pkg);
            if (realSign == null || realSign.length == 0 || ps == null) {
                this.mCompatSettings.removeCertCompatPackage(pkg.applicationInfo.packageName);
            } else {
                this.mCompatSettings.insertCompatPackage(pkg.applicationInfo.packageName, ps);
            }
        }
    }

    public boolean isSystemSignatureUpdated(Signature[] previous, Signature[] current) {
        if (this.mCompatSettings == null) {
            return false;
        }
        return this.mCompatSettings.isSystemSignatureUpdated(previous, current);
    }

    public void sendIncompatibleNotificationIfNeeded(final String packageName) {
        synchronized (this.mIncompatiblePkg) {
            boolean update = false;
            boolean send = false;
            if (this.mIncompatiblePkg.contains(packageName)) {
                this.mIncompatiblePkg.remove(packageName);
                update = true;
                send = true;
            } else if (this.mIncompatNotificationList.contains(packageName)) {
                update = true;
            }
            if (update) {
                final boolean isSend = send;
                UiThread.getHandler().post(new Runnable() {
                    public void run() {
                        HwPackageManagerServiceEx.this.updateIncompatibleNotification(packageName, isSend);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateIncompatibleNotification(String packageName, boolean isSend) {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                int[] resolvedUserIds = am.getRunningUserIds();
                int i = 0;
                if (isSend) {
                    synchronized (this.mIncompatiblePkg) {
                        this.mIncompatNotificationList.add(packageName);
                    }
                    int length = resolvedUserIds.length;
                    while (i < length) {
                        sendIncompatibleNotificationInner(packageName, resolvedUserIds[i]);
                        i++;
                    }
                }
                boolean cancelAll = false;
                synchronized (this.mIPmsInner.getPackagesLock()) {
                    boolean isSignedByOldSystemSignature = false;
                    PackageParser.Package pkgInfo = (PackageParser.Package) this.mIPmsInner.getPackagesLock().get(packageName);
                    if (!(pkgInfo == null || this.mCompatSettings == null)) {
                        isSignedByOldSystemSignature = this.mCompatSettings.isOldSystemSignature(pkgInfo.mSigningDetails.signatures);
                    }
                    if (pkgInfo == null || !isSignedByOldSystemSignature) {
                        Slog.d(TAG, "CertCompat: Package removed or update to new system signature version, cancel all incompatible notification.");
                        cancelAll = true;
                    }
                }
                if (cancelAll) {
                    synchronized (this.mIncompatiblePkg) {
                        this.mIncompatNotificationList.remove(packageName);
                    }
                }
                int length2 = resolvedUserIds.length;
                while (i < length2) {
                    int id = resolvedUserIds[i];
                    if (cancelAll || !AppGlobals.getPackageManager().isPackageAvailable(packageName, id)) {
                        cancelIncompatibleNotificationInner(packageName, id);
                    }
                    i++;
                }
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "CertCompat: RemoteException throw when update Incompatible Notification.");
        }
    }

    private void cancelIncompatibleNotificationInner(String packageName, int userId) {
        NotificationManager nm = (NotificationManager) this.mContext.getSystemService("notification");
        if (nm != null) {
            nm.cancelAsUser(packageName, 33685898, new UserHandle(userId));
            Slog.d(TAG, "CertCompat: cancel incompatible notification for u" + userId + ", packageName:" + packageName);
        }
    }

    private void sendIncompatibleNotificationInner(String packageName, int userId) {
        String str = packageName;
        int i = userId;
        Slog.d(TAG, "CertCompat: send incompatible notification to u" + i + ", packageName:" + str);
        PackageManager pm = this.mContext.getPackageManager();
        if (pm != null) {
            try {
                ApplicationInfo info = pm.getApplicationInfoAsUser(str, 0, i);
                Drawable icon = pm.getApplicationIcon(info);
                CharSequence title = pm.getApplicationLabel(info);
                String text = this.mContext.getString(33685898);
                PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, new Intent("android.intent.action.UNINSTALL_PACKAGE", Uri.parse("package:" + str)), 0, null, new UserHandle(i));
                if (pi == null) {
                    Slog.w(TAG, "CertCompat: Get PendingIntent fail, package: " + str);
                    return;
                }
                Notification notification = new Notification.Builder(this.mContext, SystemNotificationChannels.ALERTS).setLargeIcon(UserIcons.convertToBitmap(icon)).setSmallIcon(17301642).setContentTitle(title).setContentText(text).setContentIntent(pi).setDefaults(2).setPriority(2).setWhen(System.currentTimeMillis()).setShowWhen(true).setAutoCancel(true).addAction(new Notification.Action.Builder(null, this.mContext.getString(33685899), pi).build()).build();
                NotificationManager nm = (NotificationManager) this.mContext.getSystemService("notification");
                if (nm != null) {
                    nm.notifyAsUser(str, 33685898, notification, new UserHandle(i));
                }
            } catch (PackageManager.NameNotFoundException e) {
                PackageManager.NameNotFoundException nameNotFoundException = e;
                Slog.w(TAG, "CertCompat: incompatible package: " + str + " not find for u" + i);
            }
        }
    }

    public void recordInstallAppInfo(String pkgName, long beginTime, int installFlags) {
        long endTime = SystemClock.elapsedRealtime();
        int srcPkg = 0;
        if ((installFlags & 32) != 0) {
            srcPkg = 1;
        }
        insertAppInfo(pkgName, srcPkg, beginTime, endTime);
    }

    private void insertAppInfo(String pkgName, int srcPkg, long beginTime, long endTime) {
        if (TextUtils.isEmpty(pkgName)) {
            Slog.e(TAG, "insertAppInfo pkgName is null");
        } else if (!this.needCollectAppInfo) {
            Slog.i(TAG, "AntiMalware is closed");
        } else {
            try {
                Bundle bundle = new Bundle();
                bundle.putString("pkg", pkgName);
                bundle.putInt(SOURCE_PACKAGE_NAME, srcPkg);
                bundle.putLong(INSTALL_BEGIN, beginTime);
                bundle.putLong(INSTALL_END, endTime);
                Bundle res = HwSystemManager.callHsmService(ANTIMAL_MODULE, bundle);
                Slog.i(TAG, "insertAppInfo pkgName:" + pkgName + " time:" + SystemClock.elapsedRealtime());
                if (res != null && res.getInt(INSERT_RESULT) != 0) {
                    this.needCollectAppInfo = false;
                }
            } catch (Exception e) {
                Slog.e(TAG, "insertAppInfo EXCEPTION = " + e);
            }
        }
    }

    public boolean setApplicationMaxAspectRatio(String packageName, float ar) {
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mIPmsInner.getPackagesLock()) {
                PackageSetting pkgSetting = (PackageSetting) this.mIPmsInner.getSettings().mPackages.get(packageName);
                if (pkgSetting == null) {
                    Binder.restoreCallingIdentity(callingId);
                    return false;
                } else if (pkgSetting.getMaxAspectRatio() != ar) {
                    pkgSetting.setMaxAspectRatio(ar);
                    this.mIPmsInner.getSettings().writeLPr();
                    Binder.restoreCallingIdentity(callingId);
                    return true;
                } else {
                    Binder.restoreCallingIdentity(callingId);
                    return false;
                }
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
    }

    public float getApplicationMaxAspectRatio(String packageName) {
        int uid = Binder.getCallingUid();
        if (UserHandle.getAppId(uid) == 1000 || uid == 0) {
            long callingId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mIPmsInner.getPackagesLock()) {
                    PackageSetting pkgSetting = (PackageSetting) this.mIPmsInner.getSettings().mPackages.get(packageName);
                    if (pkgSetting == null) {
                        Binder.restoreCallingIdentity(callingId);
                        return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                    }
                    float maxAspectRatio = pkgSetting.getMaxAspectRatio();
                    Binder.restoreCallingIdentity(callingId);
                    return maxAspectRatio;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(callingId);
                throw th;
            }
        } else {
            throw new SecurityException("Only the system can get application max ratio");
        }
    }

    public boolean setApplicationAspectRatio(String packageName, String aspectName, float ar) {
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000) {
            long callingId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mIPmsInner.getPackagesLock()) {
                    PackageSetting pkgSetting = (PackageSetting) this.mIPmsInner.getSettings().mPackages.get(packageName);
                    if (pkgSetting == null) {
                        Binder.restoreCallingIdentity(callingId);
                        return false;
                    } else if (pkgSetting.getAspectRatio(aspectName) != ar) {
                        pkgSetting.setAspectRatio(aspectName, ar);
                        this.mIPmsInner.getSettings().writeLPr();
                        Binder.restoreCallingIdentity(callingId);
                        return true;
                    } else {
                        Binder.restoreCallingIdentity(callingId);
                        return false;
                    }
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(callingId);
                throw th;
            }
        } else {
            throw new SecurityException("Only the system can get application aspect ratio");
        }
    }

    public float getApplicationAspectRatio(String packageName, String aspectName) {
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000) {
            long callingId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mIPmsInner.getPackagesLock()) {
                    PackageSetting pkgSetting = (PackageSetting) this.mIPmsInner.getSettings().mPackages.get(packageName);
                    if (pkgSetting == null) {
                        Binder.restoreCallingIdentity(callingId);
                        return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                    }
                    float aspectRatio = pkgSetting.getAspectRatio(aspectName);
                    Binder.restoreCallingIdentity(callingId);
                    return aspectRatio;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(callingId);
                throw th;
            }
        } else {
            throw new SecurityException("Only the system can get application aspect ratio");
        }
    }

    private boolean isSystemSecure() {
        return "1".equals(SystemProperties.get(SYSTEM_SECURE, "1")) && "0".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0"));
    }

    public void loadSysWhitelist() {
        AntiMalPreInstallScanner.init(this.mContext, this.mIPmsInner.isUpgrade());
        AntiMalPreInstallScanner.getInstance().loadSysWhitelist();
    }

    public void checkIllegalSysApk(PackageParser.Package pkg, int hwFlags) throws PackageManagerException {
        switch (AntiMalPreInstallScanner.getInstance().checkIllegalSysApk(pkg, hwFlags)) {
            case 1:
                if (isSystemSecure() || ANTIMAL_DEBUG_ON) {
                    throw new PackageManagerException(-115, "checkIllegalSysApk add illegally!");
                }
                return;
            case 2:
                int hwFlags2 = hwFlags | 33554432;
                addFlagsForRemovablePreApk(pkg, hwFlags2);
                if (!needInstallRemovablePreApk(pkg, hwFlags2)) {
                    throw new PackageManagerException(-115, "checkIllegalSysApk apk changed illegally!");
                }
                return;
            default:
                Slog.i(TAG, "Other types of aplication " + pkg);
                return;
        }
    }

    public void addPreinstalledPkgToList(PackageParser.Package scannedPkg) {
        if (scannedPkg != null && scannedPkg.baseCodePath != null && !scannedPkg.baseCodePath.startsWith("/data/app/") && !scannedPkg.baseCodePath.startsWith("/data/app-private/")) {
            preinstalledPackageList.add(scannedPkg.packageName);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r3.close();
     */
    public List<String> getPreinstalledApkList() {
        List<String> preinstalledApkList = new ArrayList<>();
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(new File(PREINSTALLED_APK_LIST_DIR, PREINSTALLED_APK_LIST_FILE));
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            while (true) {
                int next = parser.next();
                int type = next;
                if (next == 1 || type == 2) {
                } else {
                    Slog.d(TAG, "getPreinstalledApkList");
                }
            }
            if ("values".equals(parser.getName())) {
                parser.next();
                int outerDepth = parser.getDepth();
                while (true) {
                    int next2 = parser.next();
                    int type2 = next2;
                    if (next2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                        try {
                            break;
                        } catch (IOException e) {
                            Slog.e(TAG, "getPreinstalledApkList, failed to close fileinputstream");
                        }
                    } else if (type2 != 3) {
                        if (type2 != 4) {
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
            Slog.w(TAG, "file is not exist " + e2.getMessage());
            if (stream != null) {
                stream.close();
            }
        } catch (XmlPullParserException e3) {
            Slog.w(TAG, "failed parsing " + preinstalledApkFile + " " + e3.getMessage());
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e4) {
            Slog.w(TAG, "failed parsing " + preinstalledApkFile + " " + e4.getMessage());
            if (stream != null) {
                stream.close();
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e5) {
                    Slog.e(TAG, "getPreinstalledApkList, failed to close fileinputstream");
                }
            }
            throw th;
        }
    }

    public void writePreinstalledApkListToFile() {
        File preinstalledApkFile = new File(PREINSTALLED_APK_LIST_DIR, PREINSTALLED_APK_LIST_FILE);
        if (!preinstalledApkFile.exists()) {
            FileOutputStream stream = null;
            try {
                if (preinstalledApkFile.createNewFile()) {
                    FileUtils.setPermissions(preinstalledApkFile.getPath(), HwUtils.SET_PERMISSIONS_MODE, -1, -1);
                }
                FileOutputStream stream2 = new FileOutputStream(preinstalledApkFile, false);
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(stream2, "utf-8");
                out.startDocument(null, true);
                out.startTag(null, "values");
                int pkgSize = preinstalledPackageList.size();
                PackageManager pm = this.mContext.getPackageManager();
                for (int i = 0; i < pkgSize; i++) {
                    String packageName = preinstalledPackageList.get(i);
                    out.startTag(null, "string");
                    out.attribute(null, "name", packageName);
                    out.attribute(null, HwSecDiagnoseConstant.MALAPP_APK_NAME, pm.getApplicationLabel(pm.getApplicationInfo(packageName, 67108864)).toString());
                    out.endTag(null, "string");
                }
                out.endTag(null, "values");
                out.endDocument();
                try {
                    stream2.close();
                } catch (IOException e) {
                    Slog.e(TAG, "writePreinstalledApkListToFile, failed to close fileoutputstream");
                }
            } catch (PackageManager.NameNotFoundException e2) {
                Slog.w(TAG, "failed parsing " + preinstalledApkFile + " " + e2.getMessage());
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e3) {
                Slog.w(TAG, "failed parsing " + preinstalledApkFile + " " + e3.getMessage());
                if (stream != null) {
                    stream.close();
                }
            } catch (Throwable th) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e4) {
                        Slog.e(TAG, "writePreinstalledApkListToFile, failed to close fileoutputstream");
                    }
                }
                throw th;
            }
        }
    }

    public void createPublicityFile() {
        AntiMalPreInstallScanner.init(this.mContext, this.mIPmsInner.isUpgrade());
        if ("CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""))) {
            ParceledListSlice<ApplicationInfo> slice = this.mIPmsInner.getInstalledApplications(0, 0);
            if (this.mIPmsInner.isUpgrade()) {
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

    protected static void initCustStoppedApps() {
        File file;
        if (!firstScanForHwPMS()) {
            Slog.i(TAG, "not first boot. don't init cust stopped apps.");
            return;
        }
        Slog.i(TAG, "first boot. init cust stopped apps.");
        File file2 = null;
        try {
            file2 = HwCfgFilePolicy.getCfgFile("xml/not_start_firstboot.xml", 0);
            if (file2 == null) {
                file = new File(HwDelAppManager.CUST_PRE_DEL_DIR, "xml/not_start_firstboot.xml");
                file2 = file;
            }
        } catch (NoClassDefFoundError e) {
            Slog.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
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
                XmlUtils.beginDocument(parser, "resources");
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (parser.getName() == null) {
                        try {
                            break;
                        } catch (IOException e) {
                            Slog.w(TAG, "Got execption when close black_package_name.xml!", e);
                        }
                    } else if ("package".equals(parser.getName()) && "name".equals(parser.getAttributeName(0))) {
                        String value = parser.getAttributeValue(0);
                        if (value == null || "".equals(value)) {
                            mCustStoppedApps.clear();
                            Slog.e(TAG, "not_start_firstboot.xml bad format.");
                        } else {
                            mCustStoppedApps.add(value);
                            Slog.i(TAG, "cust stopped apps:" + value);
                        }
                    }
                }
                xmlReader.close();
            } catch (XmlPullParserException e2) {
                Slog.w(TAG, "Got execption parsing black_package_name.xml!", e2);
                xmlReader.close();
            } catch (IOException e3) {
                Slog.w(TAG, "Got execption parsing black_package_name.xml!", e3);
                xmlReader.close();
            } catch (Throwable th) {
                try {
                    xmlReader.close();
                } catch (IOException e4) {
                    Slog.w(TAG, "Got execption when close black_package_name.xml!", e4);
                }
                throw th;
            }
        } catch (FileNotFoundException e5) {
            Slog.w(TAG, "There is no file named not_start_firstboot.xml!", e5);
        }
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

    public void scanRemovableAppDir(int scanMode) {
        HwDelAppManager.getInstance(this).scanRemovableAppDir(scanMode);
    }

    public boolean needInstallRemovablePreApk(PackageParser.Package pkg, int hwFlags) {
        return HwUninstalledAppManager.getInstance(this).needInstallRemovablePreApk(pkg, hwFlags);
    }

    public boolean isDelapp(PackageSetting ps) {
        return HwDelAppManager.getInstance(this).isDelapp(ps);
    }

    private File[] getSystemPathAppDirs() {
        return new File[]{new File(SYSTEM_PRIV_APP_DIR), new File(SYSTEM_APP_DIR)};
    }

    public boolean isSystemPathApp(PackageSetting ps) {
        if (ps.codePath == null) {
            return false;
        }
        String codePath = ps.codePath.toString();
        if (codePath.startsWith(SYSTEM_FRAMEWORK_DIR) || HwPackageManagerUtils.isHaveApkFile(getSystemPathAppDirs(), codePath)) {
            return true;
        }
        if ((ps.pkgFlags & 1) == 0) {
            return false;
        }
        for (String installSafeMode : INSTALL_SAFEMODE_LIST) {
            if (installSafeMode.equals(ps.name)) {
                return true;
            }
        }
        return false;
    }

    public void addFlagsForRemovablePreApk(PackageParser.Package pkg, int hwFlags) {
        HwPackageManagerServiceUtils.addFlagsForRemovablePreApk(pkg, hwFlags);
    }

    public boolean isDisallowUninstallApk(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        String disallowUninstallPkgList = Settings.Secure.getString(this.mContext.getContentResolver(), "enterprise_disallow_uninstall_apklist");
        Slog.w(TAG, "isEnterpriseDisallowUninstallApk disallowUninstallPkgList : " + disallowUninstallPkgList);
        if (!TextUtils.isEmpty(disallowUninstallPkgList)) {
            for (String pkg : disallowUninstallPkgList.split(";")) {
                if (packageName.equals(pkg)) {
                    Slog.i(TAG, packageName + " is in the enterprise Disallow UninstallApk blacklist!");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInMultiWinWhiteList(String packageName) {
        return MultiWinWhiteListManager.getInstance().isInMultiWinWhiteList(packageName);
    }

    public boolean isInMWPortraitWhiteList(String packageName) {
        return MultiWinWhiteListManager.getInstance().isInMWPortraitWhiteList(packageName);
    }

    public String getResourcePackageNameByIcon(String pkgName, int icon, int userId) {
        String packageName;
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            Log.w(TAG, "packageManager is null !");
            return null;
        }
        try {
            packageName = pm.getResourcesForApplicationAsUser(pkgName, userId).getResourcePackageName(icon);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "packageName " + pkgName + ": Resources not found !");
            packageName = null;
        } catch (Resources.NotFoundException e2) {
            Log.w(TAG, "packageName " + pkgName + ": ResourcesPackageName not found !");
            packageName = null;
        } catch (RuntimeException e3) {
            Log.w(TAG, "RuntimeException in getResourcePackageNameByIcon !");
            packageName = null;
        }
        return packageName;
    }

    public List<String> getOldDataBackup() {
        return HwUninstalledAppManager.getInstance(this).getOldDataBackup();
    }

    private boolean assertScanInstallApkLocked(String packageName, String apkFile, int userId) {
        if (this.mScanInstallApkList.contains(apkFile)) {
            Slog.w(TAG, "Scan install , the apk file " + apkFile + " is already in scanning.  Skipping duplicate.");
            return false;
        }
        Map<String, String> uninstalledMap = getUninstalledMap();
        if (uninstalledMap == null || !uninstalledMap.containsValue(apkFile)) {
            Slog.w(TAG, "Scan install , the apk file " + apkFile + " is not a uninstalled system app's codePath.  Skipping.");
            return false;
        } else if (userId == -1) {
            Slog.i(TAG, "Scan install for all users!");
            return true;
        } else {
            PackageSetting psTemp = this.mIPmsInner.getSettings().getPackageLPr(packageName);
            if (psTemp == null || !psTemp.getInstalled(userId)) {
                return true;
            }
            Slog.w(TAG, "Scan install , " + packageName + " is already installed in user " + userId + " .  Skipping scan " + apkFile);
            return false;
        }
    }

    private boolean checkScanInstallCaller() {
        int callingUid = Binder.getCallingUid();
        if (callingUid == 1000) {
            return true;
        }
        return SCAN_INSTALL_CALLER_PACKAGES.contains(this.mIPmsInner.getNameForUidInner(callingUid));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x025c, code lost:
        monitor-enter(r1.mScanInstallApkList);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x0263, code lost:
        if (r1.mScanInstallApkList.remove(r2) != false) goto L_0x0265;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x0265, code lost:
        android.util.Slog.i(TAG, "Scan install , remove from list:" + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x027c, code lost:
        r4 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x0284, code lost:
        monitor-enter(r1.mScanInstallApkList);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x028b, code lost:
        if (r1.mScanInstallApkList.remove(r2) != false) goto L_0x028d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x028d, code lost:
        android.util.Slog.i(TAG, "Scan install , remove from list:" + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x02a4, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0091, code lost:
        r7 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r8 = r1.mIPmsInner.getSettings().getPackageLPr(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x009d, code lost:
        if (r8 == null) goto L_0x018d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a9, code lost:
        if (r8.isAnyInstalled(com.android.server.pm.PackageManagerService.sUserManager.getUserIds()) == false) goto L_0x018d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b1, code lost:
        if (r2.equals(r8.codePathString) != false) goto L_0x00d8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00b3, code lost:
        if (r22 == null) goto L_0x00b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00b6, code lost:
        android.util.Slog.w(TAG, "Scan install ," + r5 + " installed by other user from " + r8.codePathString);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00d9, code lost:
        if (r3 != -1) goto L_0x00e6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00db, code lost:
        r0 = r8.queryInstalledUsers(com.android.server.pm.PackageManagerService.sUserManager.getUserIds(), false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00e6, code lost:
        r0 = new int[]{r3};
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00ea, code lost:
        r11 = r0.length;
        r12 = 1;
        r10 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00ee, code lost:
        if (r10 >= r11) goto L_0x0149;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00f0, code lost:
        r13 = r0[r10];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00f2, code lost:
        if (r13 == 0) goto L_0x011c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00fe, code lost:
        if (r1.mIPmsInner.getUserManagerInternalInner().isClonedProfile(r13) == false) goto L_0x011c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0100, code lost:
        android.util.Slog.d(TAG, "Scan install, skipping cloned user " + r13 + "!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x011c, code lost:
        r12 = r1.mIPmsInner.installExistingPackageAsUserInternalInner(r5, r13, 0, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0124, code lost:
        if (1 == r12) goto L_0x0145;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0126, code lost:
        android.util.Slog.w(TAG, "Scan install failed for user " + r13 + ", installExistingPackageAsUser:" + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0145, code lost:
        r10 = r10 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0149, code lost:
        r6 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x014a, code lost:
        if (1 != r12) goto L_0x014d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x014d, code lost:
        r6 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x014e, code lost:
        r7 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x014f, code lost:
        if (r7 == false) goto L_0x016d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0151, code lost:
        r6 = r1.mIPmsInner.getSettings().getPackageLPr(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x015b, code lost:
        if (r6 == null) goto L_0x016d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0168, code lost:
        if (r6.queryInstalledUsers(com.android.server.pm.PackageManagerService.sUserManager.getUserIds(), false).length != 0) goto L_0x016d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x016a, code lost:
        removeFromUninstalledDelapp(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x016d, code lost:
        android.util.Slog.d(TAG, "Scan install , installExistingPackageAsUser:" + r2 + " success:" + r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x018d, code lost:
        r14 = new java.io.File(r2);
        r0 = 139792;
        r10 = 16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x019b, code lost:
        if (isNoSystemPreApp(r2) == false) goto L_0x01a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x019d, code lost:
        r10 = 0;
        r0 = 139792 & -131073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x01ac, code lost:
        if (com.android.server.pm.HwPreAppManager.getInstance(r21).isPrivilegedPreApp(r2) == false) goto L_0x01a2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x01ae, code lost:
        r0 = 139792 | 262144;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x01ce, code lost:
        if (r1.mIPmsInner.scanPackageLIInner(r14, r10, r0, 0, new android.os.UserHandle(android.os.UserHandle.getUserId(android.os.Binder.getCallingUid())), 1107296256) == null) goto L_0x01d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x01d0, code lost:
        r4 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x01d2, code lost:
        r7 = r4;
        android.util.Slog.d(TAG, "Scan install , restore from :" + r2 + " success:" + r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01f2, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:?, code lost:
        android.util.Slog.e(TAG, "Scan install, failed to parse package: " + r0.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0234, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0237, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:?, code lost:
        android.util.Slog.e(TAG, "Scan install " + r2 + " failed!" + r0.getMessage());
     */
    public boolean scanInstallApk(String packageName, String apkFile, int userId) {
        boolean success;
        String str = apkFile;
        int i = userId;
        boolean z = false;
        if (!checkScanInstallCaller()) {
            Slog.w(TAG, "Scan install ,check caller failed!");
            return false;
        } else if (str == null || !isPreRemovableApp(str)) {
            Slog.d(TAG, "Illegal install apk file:" + str);
            return false;
        } else {
            String pkgName = packageName;
            if (TextUtils.isEmpty(packageName)) {
                PackageParser.Package pkg = null;
                try {
                    pkg = new PackageParser().parsePackage(new File(str), 0, true, 0);
                } catch (PackageParser.PackageParserException e) {
                    Slog.w(TAG, "Scan install ,parse " + str + " to get package name failed!" + e.getMessage());
                }
                if (pkg == null) {
                    Slog.w(TAG, "Scan install ,get package name failed, pkg is null!");
                    return false;
                }
                pkgName = pkg.packageName;
            }
            synchronized (this.mScanInstallApkList) {
                if (!assertScanInstallApkLocked(pkgName, str, i)) {
                    return false;
                }
                this.mScanInstallApkList.add(str);
                Slog.i(TAG, "Scan install , add to list:" + str);
            }
        }
        boolean success2 = success;
        synchronized (this.mScanInstallApkList) {
            if (this.mScanInstallApkList.remove(str)) {
                Slog.i(TAG, "Scan install , remove from list:" + str);
            }
        }
        return success2;
        return success2;
    }

    public boolean scanInstallApk(String apkFile) {
        return scanInstallApk(null, apkFile, UserHandle.getUserId(Binder.getCallingUid()));
    }

    public List<String> getScanInstallList() {
        return HwUninstalledAppManager.getInstance(this).getScanInstallList();
    }

    public void doPostScanInstall(PackageParser.Package pkg, UserHandle user, boolean isNewInstall, int hwFlags) {
        PackageParser.Package packageR = pkg;
        if ((hwFlags & 1073741824) != 0 && isPreRemovableApp(packageR.codePath)) {
            PackageManagerService.PackageInstalledInfo res = new PackageManagerService.PackageInstalledInfo();
            res.setReturnCode(1);
            res.uid = -1;
            if (isNewInstall) {
                res.origUsers = new int[]{user.getIdentifier()};
            } else {
                res.origUsers = this.mIPmsInner.getSettings().getPackageLPr(packageR.packageName).queryInstalledUsers(PackageManagerService.sUserManager.getUserIds(), true);
            }
            res.pkg = null;
            res.removedInfo = null;
            this.mIPmsInner.updateSettingsLIInner(packageR, GestureNavConst.DEFAULT_LAUNCHER_PACKAGE, PackageManagerService.sUserManager.getUserIds(), res, user, 4);
            this.mIPmsInner.prepareAppDataAfterInstallLIFInner(packageR);
            Bundle extras = new Bundle();
            extras.putInt("android.intent.extra.UID", packageR.applicationInfo != null ? packageR.applicationInfo.uid : 0);
            this.mIPmsInner.sendPackageBroadcastInner("android.intent.action.PACKAGE_ADDED", packageR.packageName, extras, 0, null, null, new int[]{user.getIdentifier()}, null);
            PackageSetting psTemp = this.mIPmsInner.getSettings().getPackageLPr(packageR.packageName);
            if (psTemp != null) {
                int[] iUser = psTemp.queryInstalledUsers(PackageManagerService.sUserManager.getUserIds(), false);
                int countClonedUser = 0;
                for (int installedUser : iUser) {
                    if (this.mIPmsInner.getUserManagerInternalInner().isClonedProfile(installedUser)) {
                        countClonedUser++;
                        Slog.d(TAG, installedUser + " skiped, it is cloned user when install package:" + packageR.packageName);
                    }
                }
                if (iUser.length == 0 || iUser.length == countClonedUser) {
                    removeFromUninstalledDelapp(packageR.packageName);
                }
            }
            Slog.d(TAG, "Scan install done for package:" + packageR.packageName);
        }
    }

    private void removeFromUninstalledDelapp(String s) {
        HwUninstalledAppManager.getInstance(this).removeFromUninstalledDelapp(s);
    }

    public void recordUninstalledDelapp(String s, String path) {
        HwUninstalledAppManager.getInstance(this).recordUninstalledDelapp(s, path);
    }

    public void readPreInstallApkList() {
        HwPreAppManager.getInstance(this).readPreInstallApkList();
    }

    private boolean isNoSystemPreApp(String codePath) {
        return HwPackageManagerServiceUtils.isNoSystemPreApp(codePath);
    }

    public boolean isPreRemovableApp(String codePath) {
        return HwPreAppManager.getInstance(this).isPreRemovableApp(codePath);
    }

    public void parseInstalledPkgInfo(String pkgUri, String pkgName, String pkgVerName, int pkgVerCode, int resultCode, boolean pkgUpdate) {
        String installedPath = "";
        String installerPackageName = "";
        if (!(pkgUri == null || pkgUri.length() == 0)) {
            int splitIndex = pkgUri.indexOf(";");
            if (splitIndex >= 0) {
                installedPath = pkgUri.substring(0, splitIndex);
                installerPackageName = pkgUri.substring(splitIndex + 1);
            } else {
                installerPackageName = pkgUri;
            }
        }
        Bundle extrasInfo = new Bundle(1);
        extrasInfo.putString("pkgName", pkgName);
        extrasInfo.putInt(INSTALLATION_EXTRA_PACKAGE_VERSION_CODE, pkgVerCode);
        extrasInfo.putString(INSTALLATION_EXTRA_PACKAGE_VERSION_NAME, pkgVerName);
        extrasInfo.putBoolean(INSTALLATION_EXTRA_PACKAGE_UPDATE, pkgUpdate);
        extrasInfo.putInt(INSTALLATION_EXTRA_PACKAGE_INSTALL_RESULT, resultCode);
        extrasInfo.putString(INSTALLATION_EXTRA_PACKAGE_URI, installedPath);
        extrasInfo.putString(INSTALLATION_EXTRA_INSTALLER_PACKAGE_NAME, installerPackageName);
        Intent intentInfo = new Intent(ACTION_GET_PACKAGE_INSTALLATION_INFO);
        intentInfo.putExtras(extrasInfo);
        intentInfo.setFlags(1073741824);
        this.mContext.sendBroadcast(intentInfo, BROADCAST_PERMISSION);
        Slog.v(TAG, "POST_INSTALL:  pkgName = " + pkgName + ", pkgUri = " + pkgUri + ", pkgInstalledPath = " + installedPath + ", pkgInstallerPackageName = " + installerPackageName + ", pkgVerName = " + pkgVerName + ", pkgVerCode = " + pkgVerCode + ", resultCode = " + resultCode + ", pkgUpdate = " + pkgUpdate);
    }

    public boolean containDelPath(String sensePath) {
        return HwDelAppManager.getInstance(this).containDelPath(sensePath);
    }

    public void addUpdatedRemoveableAppFlag(String scanFileString, String packageName) {
        HwPreAppManager.getInstance(this).addUpdatedRemoveableAppFlag(scanFileString, packageName);
    }

    public boolean needAddUpdatedRemoveableAppFlag(String packageName) {
        return HwPreAppManager.getInstance(this).needAddUpdatedRemoveableAppFlag(packageName);
    }

    public boolean isUnAppInstallAllowed(String originPath) {
        HwCustPackageManagerService custPackageManagerService = this.mIPmsInner.getHwPMSCustPackageManagerService();
        if (custPackageManagerService == null || !custPackageManagerService.isUnAppInstallAllowed(originPath, this.mContext)) {
            return false;
        }
        return true;
    }

    public boolean isPrivAppNonSystemPartitionDir(File path) {
        return HwPreAppManager.getInstance(this).isPrivAppNonSystemPartitionDir(path);
    }

    public void scanNonSystemPartitionDir(int scanMode) {
        HwPreAppManager.getInstance(this).scanNonSystemPartitionDir(scanMode);
    }

    public void setHdbKey(String key) {
        HwAdbManager.setHdbKey(key);
    }

    public void loadCorrectUninstallDelapp() {
        HwUninstalledAppManager.getInstance(this).loadCorrectUninstallDelapp();
    }

    public void addUnisntallDataToCache(String packageName, String codePath) {
        HwUninstalledAppManager.getInstance(this).addUnisntallDataToCache(packageName, codePath);
    }

    public boolean checkUninstalledSystemApp(PackageParser.Package pkg, PackageManagerService.InstallArgs args, PackageManagerService.PackageInstalledInfo res) throws PackageManagerException {
        return HwPreAppManager.getInstance(this).checkUninstalledSystemApp(pkg, args, res);
    }

    public IHwPackageManagerInner getIPmsInner() {
        return this.mIPmsInner;
    }

    public Map<String, String> getUninstalledMap() {
        return HwUninstalledAppManager.getInstance(this).getUninstalledMap();
    }

    public boolean pmInstallHwTheme(String themePath, boolean setwallpaper, int userId) {
        return HwThemeInstaller.getInstance(this.mContext).pmInstallHwTheme(themePath, setwallpaper, userId);
    }

    public void onUserRemoved(int userId) {
        HwThemeInstaller.getInstance(this.mContext).onUserRemoved(userId);
    }

    public UserHandle getHwUserHandle(UserHandle user) {
        return HwCloneAppController.getInstance(this).getHwUserHandle(user);
    }

    public boolean isAppInstallAllowed(String installer, String appName) {
        return HwParentControlUtils.getInstance(this).isAppInstallAllowed(installer, appName) && !isInMspesForbidInstallPackageList(appName);
    }

    public int updateFlags(int flags, int userId) {
        return HwCloneAppController.getInstance(this).updateFlags(flags, userId);
    }

    public static Set<String> getSupportCloneApps() {
        return HwCloneAppController.getSupportCloneApps();
    }

    public boolean checkPermissionGranted(String permName, int userId) {
        return HwCloneAppController.getInstance(this).checkPermissionGranted(permName, userId);
    }

    public boolean checkUidPermissionGranted(String permName, int uid) {
        return HwCloneAppController.getInstance(this).checkUidPermissionGranted(permName, uid);
    }

    public void deleteNonRequiredAppsForClone(int clonedProfileUserId, boolean isFirstCreat) {
        HwCloneAppController.getInstance(this).deleteNonRequiredAppsForClone(clonedProfileUserId, isFirstCreat);
    }

    public void restoreAppDataForClone(String pkgName, int parentUserId, int clonedProfileUserId) {
        HwCloneAppController.getInstance(this).restoreAppDataForClone(pkgName, parentUserId, clonedProfileUserId);
    }

    public static boolean isSupportCloneAppInCust(String packageName) {
        return HwCloneAppController.isSupportCloneAppInCust(packageName);
    }

    public void deleteClonedProfileIfNeed(int[] removedUsers) {
        HwCloneAppController.getInstance(this).deleteClonedProfileIfNeed(removedUsers);
    }

    public static void initCloneAppsFromCust() {
        HwCloneAppController.initCloneAppsFromCust();
    }

    public void preSendPackageBroadcast(String action, String pkg, Bundle extras, int flags, String targetPkg, IIntentReceiver finishedReceiver, int[] userIds, int[] instantUserIds) {
        String str = action;
        String str2 = pkg;
        HwCloneAppController.getInstance(this).preSendPackageBroadcast(str, str2, extras, flags, targetPkg, finishedReceiver, userIds, instantUserIds);
        HwAppAuthManager.getInstance().preSendPackageBroadcast(str, str2, targetPkg);
    }

    public void preInstallExistingPackageAsUser(String packageName, int userId, int installFlags, int installReason) {
        HwCloneAppController.getInstance(this).preInstallExistingPackageAsUser(packageName, userId, installFlags, installReason);
    }

    public ActivityInfo getActivityInfo(ComponentName component, int flags, int userId) {
        return HwCloneAppController.getInstance(this).getActivityInfo(component, flags, userId);
    }

    public List<ResolveInfo> queryIntentActivitiesInternal(Intent intent, String resolvedType, int flags, int filterCallingUid, int userId, boolean resolveForStart, boolean allowDynamicSplits) {
        return HwCloneAppController.getInstance(this).queryIntentActivitiesInternal(intent, resolvedType, flags, filterCallingUid, userId, resolveForStart, allowDynamicSplits);
    }

    public void deletePackageVersioned(VersionedPackage versionedPackage, IPackageDeleteObserver2 observer, int userId, int deleteFlags) {
        HwCloneAppController.getInstance(this).deletePackageVersioned(versionedPackage, observer, userId, deleteFlags);
    }

    public boolean isPackageAvailable(String packageName, int userId) {
        return HwCloneAppController.getInstance(this).isPackageAvailable(packageName, userId);
    }

    public void deleteNonSupportedAppsForClone() {
        HwCloneAppController.getInstance(this).deleteNonSupportedAppsForClone();
    }

    public Context getContextInner() {
        return this.mContext;
    }

    public boolean verifyPackageSecurityPolicy(String packageName, File path) {
        if (packageName == null || path == null) {
            Slog.e(TAG, "verifyPackageSecurityPolicy illegal params");
            return false;
        }
        String baseApkPath = path.getAbsolutePath() + "/base.apk";
        ISecurityProfileController spc = HwServiceFactory.getSecurityProfileController();
        if (spc == null || spc.verifyPackage(packageName, baseApkPath)) {
            return true;
        }
        Slog.e(TAG, "Security policy verification failed");
        return false;
    }

    public boolean isInValidApkPatchFile(File file, int parseFlags) {
        boolean bInValidapk = false;
        if (file == null) {
            return false;
        }
        try {
            PackageParser.Package ppkg = new PackageParser().parsePackage(file, parseFlags);
            if (ppkg.mAppMetaData != null && ppkg.mAppMetaData.getBoolean(FLAG_APKPATCH_TAG, false)) {
                bInValidapk = !checkAllowtoInstallPatchApk(ppkg.packageName);
                Slog.i(TAG, "-----isInvalidApkPatch-----" + bInValidapk);
            }
        } catch (PackageParser.PackageParserException e) {
            Slog.w(TAG, "failed to parse " + file, e);
        }
        return bInValidapk;
    }

    public boolean isInValidApkPatchPkg(PackageParser.Package pkg) {
        boolean bInValidapk = false;
        if (pkg == null || TextUtils.isEmpty(pkg.packageName)) {
            return false;
        }
        if (pkg.mAppMetaData != null && pkg.mAppMetaData.getBoolean(FLAG_APKPATCH_TAG, false)) {
            bInValidapk = !checkAllowtoInstallPatchApk(pkg.packageName);
            Slog.i(TAG, "-----isInvalidApkPatch-----2-----" + bInValidapk);
        }
        return bInValidapk;
    }

    private boolean checkAllowtoInstallPatchApk(String srcpkgName) {
        File fileForParse = new File(FLAG_APKPATCH_PATH);
        InputStream is = null;
        boolean bIsLegalPatchPkg = false;
        if (TextUtils.isEmpty(srcpkgName) || !fileForParse.exists()) {
            return false;
        }
        try {
            is = new FileInputStream(fileForParse);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(is, null);
            XmlUtils.beginDocument(parser, FLAG_APKPATCH_TAGPATCH);
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element != null) {
                    if ("pkgname".equals(element) && srcpkgName.equalsIgnoreCase(XmlUtils.readStringAttribute(parser, "value"))) {
                        Slog.d(TAG, "this is legal apk");
                        bIsLegalPatchPkg = true;
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "patch info parse fail");
        } catch (IOException | XmlPullParserException e2) {
            Slog.e(TAG, "patch info parse fail & io fail ");
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
            throw th;
        }
        IoUtils.closeQuietly(is);
        return bIsLegalPatchPkg;
    }

    public void reportEventStream(int eventId, String message) {
        if (907400027 != eventId && !TextUtils.isEmpty(message)) {
            IMonitor.EventStream eventStream = IMonitor.openEventStream(eventId);
            if (eventStream != null) {
                eventStream.setParam("message", message);
                IMonitor.sendEvent(eventStream);
            }
            IMonitor.closeEventStream(eventStream);
        }
    }

    public String readMspesFile(String fileName) {
        return MspesExUtil.getInstance(this).readMspesFile(fileName);
    }

    public boolean writeMspesFile(String fileName, String content) {
        return MspesExUtil.getInstance(this).writeMspesFile(fileName, content);
    }

    public String getMspesOEMConfig() {
        return MspesExUtil.getInstance(this).getMspesOEMConfig();
    }

    public int updateMspesOEMConfig(String src) {
        return MspesExUtil.getInstance(this).updateMspesOEMConfig(src);
    }

    public boolean isInMspesForbidInstallPackageList(String pkg) {
        return MspesExUtil.getInstance(this).isInMspesForbidInstallPackageList(pkg);
    }

    public boolean hasSystemFeatureDelegate(String name, int version) {
        if (name != null && (name.equals("android.hardware.audio.low_latency") || name.equals("android.hardware.audio.pro"))) {
            AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
            if (audioManager != null) {
                String result = audioManager.getParameters("queryLowlatencyUid=" + Binder.getCallingUid());
                Slog.d(TAG, "ignore hasSystemFeature parameter: " + parameter + ", result " + result);
                return true;
            }
        }
        return true;
    }

    public void deleteExistsIfNeedForHwPMS() {
        ArrayList<ArrayList<File>> list;
        boolean z;
        ArrayList<ArrayList<File>> list2 = getCotaApkInstallXMLPath();
        int i = 0;
        boolean z2 = true;
        File[] files = getCotaApkInstallXMLFile((HashSet) getAllCotaApkPath(list2.get(0), list2.get(1)));
        int i2 = 0;
        while (i2 < files.length) {
            try {
                Log.i(TAG, "deleteExistsIfNeed files " + i2 + "  " + files[i2]);
                PackageParser pp = new PackageParser();
                pp.setCallback(new MyCallback());
                PackageParser.Package pkg = pp.parsePackage(files[i2], 16, z2, i);
                if (pkg != null) {
                    String pkgName = pkg.packageName;
                    PackageParser.Package oldPkg = (PackageParser.Package) this.mIPmsInner.getPackagesLock().get(pkgName);
                    if (oldPkg != null) {
                        String oldCodePath = oldPkg.codePath;
                        String oldApkName = new File(oldCodePath).getName();
                        PackageSetting ps = (PackageSetting) this.mIPmsInner.getSettings().mPackages.get(pkgName);
                        StringBuilder sb = new StringBuilder();
                        list = list2;
                        try {
                            sb.append("parsePackage  pkg= ");
                            sb.append(pkgName);
                            sb.append(" ,oldCodePath = ");
                            sb.append(oldCodePath);
                            sb.append(" ,oldApkName = ");
                            sb.append(oldApkName);
                            sb.append(" ,ps = ");
                            sb.append(ps);
                            Log.i(TAG, sb.toString());
                            if (this.mIPmsInner.getPackagesLock().containsKey(pkgName)) {
                                if (ps != null) {
                                    if (oldCodePath.startsWith("/data/app")) {
                                        this.mDataApkShouldNotUpdateByCota.add(files[i2].getCanonicalPath());
                                        Log.i(TAG, "deleteExistsIfNeed ignore " + files[i2].getCanonicalPath());
                                    } else if (oldCodePath.startsWith("/version/special_cust") || oldCodePath.startsWith("/version/cust") || oldCodePath.startsWith("/data/cota")) {
                                        Log.i(TAG, "removePackageLI pkgName= " + pkgName + " ,oldApkName= " + oldApkName);
                                        this.mIPmsInner.killApplicationInner(pkgName, ps.appId, "killed by cota");
                                        z = true;
                                        this.mIPmsInner.removePackageLIInner(oldPkg, true);
                                        deletePackageCache(oldApkName);
                                        i2++;
                                        z2 = z;
                                        list2 = list;
                                        i = 0;
                                    }
                                }
                            }
                            z = true;
                            i2++;
                            z2 = z;
                            list2 = list;
                            i = 0;
                        } catch (PackageParser.PackageParserException e) {
                            Log.e(TAG, "PackageParserException");
                            return;
                        } catch (Exception e2) {
                            ex = e2;
                            Log.e(TAG, "Exception " + ex.getMessage());
                            return;
                        }
                    }
                }
                list = list2;
                z = z2;
                i2++;
                z2 = z;
                list2 = list;
                i = 0;
            } catch (PackageParser.PackageParserException e3) {
                ArrayList<ArrayList<File>> arrayList = list2;
                Log.e(TAG, "PackageParserException");
                return;
            } catch (Exception e4) {
                ex = e4;
                ArrayList<ArrayList<File>> arrayList2 = list2;
                Log.e(TAG, "Exception " + ex.getMessage());
                return;
            }
        }
    }

    public ArrayList<String> getDataApkShouldNotUpdateByCota() {
        return this.mDataApkShouldNotUpdateByCota;
    }

    private static Set<String> getAPKInstallPathList(File scanApk) {
        Set<String> apkInstallPathList = new HashSet<>();
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(scanApk), "UTF-8"));
            while (true) {
                String readLine = reader2.readLine();
                String line = readLine;
                if (readLine != null) {
                    String packagePath = HwPackageManagerServiceUtils.getCustPackagePath(line.trim().split(",")[0]);
                    Log.i(TAG, "getAPKInstallPathList packagePath= " + packagePath);
                    apkInstallPathList.add(packagePath);
                } else {
                    try {
                        break;
                    } catch (Exception e) {
                        Log.e(TAG, "HWPMEX.getAPKInstallPathList error for closing IO");
                    }
                }
            }
            reader2.close();
        } catch (Exception e2) {
            Log.e(TAG, "HWPMEX.getAPKInstallPathList error for IO , " + e2.getMessage());
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e3) {
                    Log.e(TAG, "HWPMEX.getAPKInstallPathList error for closing IO");
                }
            }
            throw th;
        }
        return apkInstallPathList;
    }

    private static Set<String> getAllCotaApkPath(ArrayList<File> installPath, ArrayList<File> delInstallPath) {
        Set<String> allCotaApkPath = new HashSet<>();
        int installPathSize = installPath.size();
        for (int i = 0; i < installPathSize; i++) {
            File file = installPath.get(i);
            if (file != null && file.exists()) {
                allCotaApkPath.addAll(getAPKInstallPathList(file));
            }
        }
        int i2 = delInstallPath.size();
        for (int i3 = 0; i3 < i2; i3++) {
            File file2 = delInstallPath.get(i3);
            if (file2 != null && file2.exists()) {
                allCotaApkPath.addAll(getAPKInstallPathList(file2));
            }
        }
        return allCotaApkPath;
    }

    private static File[] getCotaApkInstallXMLFile(Set<String> allCotaApkPath) {
        int fileSize = allCotaApkPath.size();
        File[] files = new File[fileSize];
        int i = 0;
        for (String installPath : allCotaApkPath) {
            File file = new File(installPath);
            if (i < fileSize) {
                files[i] = file;
                i++;
            }
        }
        return files;
    }

    public static ArrayList<ArrayList<File>> getCotaApkInstallXMLPath() {
        ArrayList<File> apkInstallList = new ArrayList<>();
        ArrayList<File> apkDelInstallList = new ArrayList<>();
        File apkInstallFile = new File(SIMPLE_COTA_APK_XML_PATH);
        File apkDelInstallListFile = new File(SIMPLE_COTA_DEL_APK_XML_PATH);
        if (apkInstallFile.exists() || apkDelInstallListFile.exists()) {
            apkInstallList.add(apkInstallFile);
            apkDelInstallList.add(apkDelInstallListFile);
        } else {
            apkInstallList = getCotaXMLFile(COTA_APK_XML_PATH);
            apkDelInstallList = getCotaXMLFile(COTA_DEL_APK_XML_PATH);
        }
        int size = apkInstallList.size();
        ArrayList<ArrayList<File>> result = new ArrayList<>();
        result.add(apkInstallList);
        result.add(apkDelInstallList);
        return result;
    }

    private static void deletePackageCache(String apkName) {
        File[] allPackageNameFile = new File(PACKAGE_CACHE_DIR).listFiles();
        for (int i = 0; i < allPackageNameFile.length; i++) {
            String name = allPackageNameFile[i].getName();
            if (name.startsWith(apkName + "-")) {
                Log.e(TAG, "deletePackageCache " + allPackageNameFile[i]);
                allPackageNameFile[i].delete();
            }
        }
    }

    public static ArrayList<File> getSysdllInstallXMLPath() {
        ArrayList<File> result = new ArrayList<>();
        File sysdllFile = null;
        try {
            sysdllFile = HwCfgFilePolicy.getCfgFile(HwPackageManagerService.SYSDLL_PATH, 0);
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "getSysdllInstallXMLPath getCfgFile NoClassDefFoundError");
        }
        if (sysdllFile != null) {
            result.add(sysdllFile);
        }
        return result;
    }

    public static ArrayList<File> getCotaXMLFile(String xmlType) {
        ArrayList<File> cotaFile = new ArrayList<>();
        try {
            String[] policyDir = HwCfgFilePolicy.getCfgPolicyDir(0);
            for (int i = 0; i < policyDir.length; i++) {
                if (policyDir[i] != null && (policyDir[i].startsWith("/version/cust") || policyDir[i].startsWith("/version/special_cust"))) {
                    cotaFile.add(new File(policyDir[i] + "/" + xmlType));
                    Log.i(TAG, "getCotaXMLFile add = " + policyDir[i] + "/" + xmlType);
                }
            }
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "HwCfgFilePolicy getCotaXMLFile NoClassDefFoundError");
        }
        return cotaFile;
    }
}
