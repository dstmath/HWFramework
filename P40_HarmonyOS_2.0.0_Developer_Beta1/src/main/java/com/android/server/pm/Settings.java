package com.android.server.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.PackageUserState;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.SuspendDialogInfo;
import android.content.pm.UserInfo;
import android.content.pm.VerifierDeviceIdentity;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.PatternMatcher;
import android.os.PersistableBundle;
import android.os.SELinux;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Log;
import android.util.LogPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.util.Xml;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.EventLogTags;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.CollectionUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.JournaledFile;
import com.android.internal.util.XmlUtils;
import com.android.server.BatteryService;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.UiModeManagerService;
import com.android.server.os.HwBootFail;
import com.android.server.pm.Installer;
import com.android.server.pm.permission.BasePermission;
import com.android.server.pm.permission.PermissionSettings;
import com.android.server.pm.permission.PermissionsState;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.voiceinteraction.DatabaseHelper;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class Settings {
    private static final String ATTR_APP_LINK_GENERATION = "app-link-generation";
    private static final String ATTR_BLOCKED = "blocked";
    @Deprecated
    private static final String ATTR_BLOCK_UNINSTALL = "blockUninstall";
    private static final String ATTR_CE_DATA_INODE = "ceDataInode";
    private static final String ATTR_DATABASE_VERSION = "databaseVersion";
    private static final String ATTR_DISTRACTION_FLAGS = "distraction_flags";
    private static final String ATTR_DOMAIN_VERIFICATON_STATE = "domainVerificationStatus";
    private static final String ATTR_EMUI_VERSION = "emuiVersion";
    private static final String ATTR_ENABLED = "enabled";
    private static final String ATTR_ENABLED_CALLER = "enabledCaller";
    private static final String ATTR_ENFORCEMENT = "enforcement";
    private static final String ATTR_FINGERPRINT = "fingerprint";
    private static final String ATTR_FINGERPRINTEX = "fingerprintEx";
    private static final String ATTR_FLAGS = "flags";
    private static final String ATTR_GRANTED = "granted";
    private static final String ATTR_HARMFUL_APP_WARNING = "harmful-app-warning";
    private static final String ATTR_HIDDEN = "hidden";
    private static final String ATTR_HWFINGERPRINT = "hwFingerprint";
    private static final String ATTR_INSTALLED = "inst";
    private static final String ATTR_INSTALL_REASON = "install-reason";
    private static final String ATTR_INSTANT_APP = "instant-app";
    public static final String ATTR_NAME = "name";
    private static final String ATTR_NOT_LAUNCHED = "nl";
    public static final String ATTR_PACKAGE = "package";
    private static final String ATTR_PACKAGE_NAME = "packageName";
    private static final String ATTR_SDK_VERSION = "sdkVersion";
    private static final String ATTR_STOPPED = "stopped";
    private static final String ATTR_SUSPENDED = "suspended";
    private static final String ATTR_SUSPENDING_PACKAGE = "suspending-package";
    @Deprecated
    private static final String ATTR_SUSPEND_DIALOG_MESSAGE = "suspend_dialog_message";
    private static final String ATTR_VERSION = "version";
    private static final String ATTR_VIRTUAL_PRELOAD = "virtual-preload";
    private static final String ATTR_VOLUME_UUID = "volumeUuid";
    public static final int CURRENT_DATABASE_VERSION = 3;
    private static final boolean DEBUG_KERNEL = false;
    private static final boolean DEBUG_MU = false;
    private static final boolean DEBUG_PARSER = false;
    private static final boolean DEBUG_STOPPED = false;
    private static final String DIR_CUST_XML = "/data/cust/xml/";
    private static final String DIR_ETC_XML = "/system/etc/xml/";
    private static final String FILE_SUB_USER_DELAPPS_LIST = "hw_subuser_delapps_config.xml";
    static final Object[] FLAG_DUMP_SPEC = {1, "SYSTEM", 2, "DEBUGGABLE", 4, "HAS_CODE", 8, "PERSISTENT", 16, "FACTORY_TEST", 32, "ALLOW_TASK_REPARENTING", 64, "ALLOW_CLEAR_USER_DATA", 128, "UPDATED_SYSTEM_APP", 256, "TEST_ONLY", Integer.valueOf((int) DumpState.DUMP_KEYSETS), "VM_SAFE_MODE", 32768, "ALLOW_BACKUP", 65536, "KILL_AFTER_RESTORE", Integer.valueOf((int) DumpState.DUMP_INTENT_FILTER_VERIFIERS), "RESTORE_ANY_VERSION", Integer.valueOf((int) DumpState.DUMP_DOMAIN_PREFERRED), "EXTERNAL_STORAGE", Integer.valueOf((int) DumpState.DUMP_DEXOPT), "LARGE_HEAP"};
    static final Object[] HW_FLAG_DUMP_SPEC = {Integer.valueOf((int) DumpState.DUMP_VOLUMES), "PARSE_IS_BOTH_APK", Integer.valueOf((int) DumpState.DUMP_SERVICE_PERMISSIONS), "PARSE_IS_MAPLE_APK", Integer.valueOf((int) DumpState.DUMP_FROZEN), "PARSE_IS_MAPLE_GC_ONLY", Integer.valueOf((int) DumpState.DUMP_APEX), "PARSE_IS_REMOVABLE_PREINSTALLED_APK", Integer.valueOf((int) DumpState.DUMP_HANDLE), "FLAG_UPDATED_REMOVEABLE_APP", Integer.valueOf((int) DumpState.DUMP_DEXOPT), "PARSE_IS_ZIDANE_APK"};
    private static final String KEY_PACKAGE_SETTINS_ERROR = "persist.sys.package_settings_error";
    private static int PRE_M_APP_INFO_FLAG_CANT_SAVE_STATE = 268435456;
    private static int PRE_M_APP_INFO_FLAG_HIDDEN = DumpState.DUMP_HWFEATURES;
    private static int PRE_M_APP_INFO_FLAG_PRIVILEGED = 1073741824;
    private static final Object[] PRIVATE_FLAG_DUMP_SPEC = {1024, "PRIVATE_FLAG_ACTIVITIES_RESIZE_MODE_RESIZEABLE", 4096, "PRIVATE_FLAG_ACTIVITIES_RESIZE_MODE_RESIZEABLE_VIA_SDK_VERSION", 2048, "PRIVATE_FLAG_ACTIVITIES_RESIZE_MODE_UNRESIZEABLE", Integer.valueOf((int) DumpState.DUMP_HWFEATURES), "ALLOW_AUDIO_PLAYBACK_CAPTURE", 536870912, "PRIVATE_FLAG_REQUEST_LEGACY_EXTERNAL_STORAGE", 8192, "BACKUP_IN_FOREGROUND", 2, "CANT_SAVE_STATE", 32, "DEFAULT_TO_DEVICE_PROTECTED_STORAGE", 64, "DIRECT_BOOT_AWARE", 16, "HAS_DOMAIN_URLS", 1, "HIDDEN", 128, "EPHEMERAL", 32768, "ISOLATED_SPLIT_LOADING", Integer.valueOf((int) DumpState.DUMP_INTENT_FILTER_VERIFIERS), "OEM", 256, "PARTIALLY_DIRECT_BOOT_AWARE", 8, "PRIVILEGED", 512, "REQUIRED_FOR_SYSTEM_USER", Integer.valueOf((int) DumpState.DUMP_KEYSETS), "STATIC_SHARED_LIBRARY", Integer.valueOf((int) DumpState.DUMP_DOMAIN_PREFERRED), "VENDOR", Integer.valueOf((int) DumpState.DUMP_FROZEN), "PRODUCT", Integer.valueOf((int) DumpState.DUMP_COMPILER_STATS), "PRODUCT_SERVICES", 65536, "VIRTUAL_PRELOAD", 1073741824, "ODM"};
    private static final String RUNTIME_PERMISSIONS_FILE_NAME = "runtime-permissions.xml";
    private static final String TAG = "PackageSettings";
    private static final String TAG_ALL_INTENT_FILTER_VERIFICATION = "all-intent-filter-verifications";
    private static final String TAG_BLOCK_UNINSTALL = "block-uninstall";
    private static final String TAG_BLOCK_UNINSTALL_PACKAGES = "block-uninstall-packages";
    private static final String TAG_CHILD_PACKAGE = "child-package";
    static final String TAG_CROSS_PROFILE_INTENT_FILTERS = "crossProfile-intent-filters";
    private static final String TAG_DEFAULT_APPS = "default-apps";
    private static final String TAG_DEFAULT_BROWSER = "default-browser";
    private static final String TAG_DEFAULT_DIALER = "default-dialer";
    private static final String TAG_DISABLED_COMPONENTS = "disabled-components";
    private static final String TAG_DISABLE_PLUGIN = "disable-plugin";
    private static final String TAG_DOMAIN_VERIFICATION = "domain-verification";
    private static final String TAG_ENABLED_COMPONENTS = "enabled-components";
    public static final String TAG_ITEM = "item";
    private static final String TAG_PACKAGE = "pkg";
    private static final String TAG_PACKAGE_RESTRICTIONS = "package-restrictions";
    private static final String TAG_PERMISSIONS = "perms";
    private static final String TAG_PERSISTENT_PREFERRED_ACTIVITIES = "persistent-preferred-activities";
    private static final String TAG_READ_EXTERNAL_STORAGE = "read-external-storage";
    private static final String TAG_RUNTIME_PERMISSIONS = "runtime-permissions";
    private static final String TAG_SHARED_USER = "shared-user";
    private static final String TAG_SUSPENDED_APP_EXTRAS = "suspended-app-extras";
    private static final String TAG_SUSPENDED_DIALOG_INFO = "suspended-dialog-info";
    private static final String TAG_SUSPENDED_LAUNCHER_EXTRAS = "suspended-launcher-extras";
    private static final String TAG_USES_STATIC_LIB = "uses-static-lib";
    private static final String TAG_VERSION = "version";
    private static int mFirstAvailableUid = 0;
    private static AtomicBoolean mIsCheckDelAppsFinished = new AtomicBoolean(false);
    private boolean isNeedRetryNewUserId = true;
    private final ArrayList<SettingBase> mAppIds = new ArrayList<>();
    private final File mBackupSettingsFilename;
    private final File mBackupStoppedPackagesFilename;
    private final SparseArray<ArraySet<String>> mBlockUninstallPackages = new SparseArray<>();
    final SparseArray<CrossProfileIntentResolver> mCrossProfileIntentResolvers = new SparseArray<>();
    final SparseArray<String> mDefaultBrowserApp = new SparseArray<>();
    private ArrayList<String> mDelAppLists = new ArrayList<>();
    private final ArrayMap<String, PackageSetting> mDisabledSysPackages = new ArrayMap<>();
    final ArraySet<String> mInstallerPackages = new ArraySet<>();
    private boolean mIsPackageSettingsError = false;
    private final ArrayMap<String, KernelPackageState> mKernelMapping = new ArrayMap<>();
    private final File mKernelMappingFilename;
    public final KeySetManagerService mKeySetManagerService = new KeySetManagerService(this.mPackages);
    private final ArrayMap<Long, Integer> mKeySetRefs = new ArrayMap<>();
    private final Object mLock;
    final SparseIntArray mNextAppLinkGeneration = new SparseIntArray();
    private final SparseArray<SettingBase> mOtherAppIds = new SparseArray<>();
    private final File mPackageListFilename;
    final ArrayMap<String, PackageSetting> mPackages = new ArrayMap<>();
    private final ArrayList<Signature> mPastSignatures = new ArrayList<>();
    private final ArrayList<PackageSetting> mPendingPackages = new ArrayList<>();
    final PermissionSettings mPermissions;
    final SparseArray<PersistentPreferredIntentResolver> mPersistentPreferredActivities = new SparseArray<>();
    final SparseArray<PreferredIntentResolver> mPreferredActivities = new SparseArray<>();
    Boolean mReadExternalStorageEnforced;
    final StringBuilder mReadMessages = new StringBuilder();
    private final ArrayMap<String, String> mRenamedPackages = new ArrayMap<>();
    private final ArrayMap<String, Integer> mReservedPackageUserIds = new ArrayMap<>(1);
    private final ArrayList<Boolean> mReservedUserIds = new ArrayList<>(1);
    private final ArrayMap<String, IntentFilterVerificationInfo> mRestoredIntentFilterVerifications = new ArrayMap<>();
    private final RuntimePermissionPersistence mRuntimePermissionsPersistence;
    private final File mSettingsFilename;
    final ArrayMap<String, SharedUserSetting> mSharedUsers = new ArrayMap<>();
    private final File mStoppedPackagesFilename;
    private final File mSystemDir;
    private VerifierDeviceIdentity mVerifierDeviceIdentity;
    private ArrayMap<String, VersionInfo> mVersion = new ArrayMap<>();

    public static class DatabaseVersion {
        public static final int FIRST_VERSION = 1;
        public static final int SIGNATURE_END_ENTITY = 2;
        public static final int SIGNATURE_MALFORMED_RECOVER = 3;
    }

    /* access modifiers changed from: private */
    public static final class KernelPackageState {
        int appId;
        int[] excludedUserIds;

        private KernelPackageState() {
        }
    }

    public static class VersionInfo {
        int databaseVersion;
        int emuiVersion;
        String fingerprint;
        String fingerprintEx;
        String hwFingerprint;
        int sdkVersion;

        public void forceCurrent() {
            this.sdkVersion = Build.VERSION.SDK_INT;
            this.databaseVersion = 3;
            this.fingerprint = Build.FINGERPRINT;
            this.hwFingerprint = Build.HWFINGERPRINT;
            this.fingerprintEx = Build.FINGERPRINTEX;
            this.emuiVersion = SystemProperties.getInt("ro.build.hw_emui_api_level", 0);
        }
    }

    Settings(File dataDir, PermissionSettings permission, Object lock) {
        this.mLock = lock;
        this.mPermissions = permission;
        this.mRuntimePermissionsPersistence = new RuntimePermissionPersistence(this.mLock);
        this.mSystemDir = new File(dataDir, "system");
        this.mSystemDir.mkdirs();
        FileUtils.setPermissions(this.mSystemDir.toString(), 509, -1, -1);
        this.mSettingsFilename = new File(this.mSystemDir, "packages.xml");
        this.mBackupSettingsFilename = new File(this.mSystemDir, "packages-backup.xml");
        this.mPackageListFilename = new File(this.mSystemDir, "packages.list");
        FileUtils.setPermissions(this.mPackageListFilename, 416, 1000, 1032);
        File kernelDir = new File("/config/sdcardfs");
        this.mKernelMappingFilename = kernelDir.exists() ? kernelDir : null;
        this.mStoppedPackagesFilename = new File(this.mSystemDir, "packages-stopped.xml");
        this.mBackupStoppedPackagesFilename = new File(this.mSystemDir, "packages-stopped-backup.xml");
        loadReservedUidsMap();
    }

    /* access modifiers changed from: package-private */
    public PackageSetting getPackageLPr(String pkgName) {
        return this.mPackages.get(pkgName);
    }

    /* access modifiers changed from: package-private */
    public String getRenamedPackageLPr(String pkgName) {
        return this.mRenamedPackages.get(pkgName);
    }

    /* access modifiers changed from: package-private */
    public String addRenamedPackageLPw(String pkgName, String origPkgName) {
        return this.mRenamedPackages.put(pkgName, origPkgName);
    }

    public boolean canPropagatePermissionToInstantApp(String permName) {
        return this.mPermissions.canPropagatePermissionToInstantApp(permName);
    }

    /* access modifiers changed from: package-private */
    public void setInstallerPackageName(String pkgName, String installerPkgName) {
        PackageSetting p = this.mPackages.get(pkgName);
        if (p != null) {
            p.setInstallerPackageName(installerPkgName);
            if (installerPkgName != null) {
                this.mInstallerPackages.add(installerPkgName);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public SharedUserSetting getSharedUserLPw(String name, int pkgFlags, int pkgPrivateFlags, boolean create) throws PackageManagerException {
        SharedUserSetting s = this.mSharedUsers.get(name);
        if (s == null && create) {
            s = new SharedUserSetting(name, pkgFlags, pkgPrivateFlags);
            s.userId = acquireAndRegisterNewAppIdLPw(s);
            if (s.userId >= 0) {
                Log.i("PackageManager", "New shared user " + name + ": id=" + s.userId);
                this.mSharedUsers.put(name, s);
            } else {
                throw new PackageManagerException(-4, "Creating shared user " + name + " failed");
            }
        }
        return s;
    }

    /* access modifiers changed from: package-private */
    public Collection<SharedUserSetting> getAllSharedUsersLPw() {
        return this.mSharedUsers.values();
    }

    /* access modifiers changed from: package-private */
    public String getDisabledSysPackagesPath(String name) {
        PackageSetting dp = this.mDisabledSysPackages.get(name);
        if (dp == null) {
            return null;
        }
        return dp.codePathString;
    }

    /* access modifiers changed from: package-private */
    public boolean disableSystemPackageLPw(String name) {
        return disableSystemPackageLPw(name, true);
    }

    /* access modifiers changed from: package-private */
    public boolean disableSystemPackageLPw(String name, boolean replaced) {
        PackageSetting disabled;
        PackageSetting p = this.mPackages.get(name);
        if (p == null) {
            Log.w("PackageManager", "Package " + name + " is not an installed package");
            return false;
        } else if (this.mDisabledSysPackages.get(name) != null || p.pkg == null || !p.pkg.isSystem() || p.pkg.isUpdatedSystemApp()) {
            return false;
        } else {
            if (!(p.pkg == null || p.pkg.applicationInfo == null)) {
                p.pkg.applicationInfo.flags |= 128;
            }
            if (replaced) {
                disabled = new PackageSetting(p);
            } else {
                disabled = p;
            }
            this.mDisabledSysPackages.put(name, disabled);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public PackageSetting enableSystemPackageLPw(String name) {
        PackageSetting p = this.mDisabledSysPackages.get(name);
        if (p == null) {
            Log.w("PackageManager", "Package " + name + " is not disabled");
            return null;
        }
        if (!(p.pkg == null || p.pkg.applicationInfo == null)) {
            p.pkg.applicationInfo.flags &= -129;
        }
        PackageSetting ret = addPackageLPw(name, p.realName, p.codePath, p.resourcePath, p.legacyNativeLibraryPathString, p.primaryCpuAbiString, p.secondaryCpuAbiString, p.cpuAbiOverrideString, p.appId, p.versionCode, p.pkgFlags, p.pkgPrivateFlags, p.parentPackageName, p.childPackageNames, p.usesStaticLibraries, p.usesStaticLibrariesVersions);
        this.mDisabledSysPackages.remove(name);
        return ret;
    }

    public ArrayMap<String, PackageSetting> getDisabledSysPackages() {
        return new ArrayMap<>(this.mDisabledSysPackages);
    }

    /* access modifiers changed from: package-private */
    public boolean isDisabledSystemPackageLPr(String name) {
        return this.mDisabledSysPackages.containsKey(name);
    }

    /* access modifiers changed from: package-private */
    public void removeDisabledSystemPackageLPw(String name) {
        this.mDisabledSysPackages.remove(name);
    }

    /* access modifiers changed from: package-private */
    public PackageSetting addPackageLPw(String name, String realName, File codePath, File resourcePath, String legacyNativeLibraryPathString, String primaryCpuAbiString, String secondaryCpuAbiString, String cpuAbiOverrideString, int uid, long vc, int pkgFlags, int pkgPrivateFlags, String parentPackageName, List<String> childPackageNames, String[] usesStaticLibraries, long[] usesStaticLibraryNames) {
        PackageSetting p = this.mPackages.get(name);
        if (p == null) {
            PackageSetting p2 = new PackageSetting(name, realName, codePath, resourcePath, legacyNativeLibraryPathString, primaryCpuAbiString, secondaryCpuAbiString, cpuAbiOverrideString, vc, pkgFlags, pkgPrivateFlags, parentPackageName, childPackageNames, 0, usesStaticLibraries, usesStaticLibraryNames);
            p2.appId = uid;
            if (!registerExistingAppIdLPw(uid, p2, name)) {
                return null;
            }
            this.mPackages.put(name, p2);
            return p2;
        } else if (p.appId == uid) {
            return p;
        } else {
            PackageManagerService.reportSettingsProblem(6, "Adding duplicate package, keeping first: " + name);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void addAppOpPackage(String permName, String packageName) {
        this.mPermissions.addAppOpPackage(permName, packageName);
    }

    /* access modifiers changed from: package-private */
    public SharedUserSetting addSharedUserLPw(String name, int uid, int pkgFlags, int pkgPrivateFlags) {
        SharedUserSetting s = this.mSharedUsers.get(name);
        if (s == null) {
            SharedUserSetting s2 = new SharedUserSetting(name, pkgFlags, pkgPrivateFlags);
            s2.userId = uid;
            if (!registerExistingAppIdLPw(uid, s2, name)) {
                return null;
            }
            this.mSharedUsers.put(name, s2);
            return s2;
        } else if (s.userId == uid) {
            return s;
        } else {
            PackageManagerService.reportSettingsProblem(6, "Adding duplicate shared user, keeping first: " + name);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void pruneSharedUsersLPw() {
        ArrayList<String> removeStage = new ArrayList<>();
        for (Map.Entry<String, SharedUserSetting> entry : this.mSharedUsers.entrySet()) {
            SharedUserSetting sus = entry.getValue();
            if (sus == null) {
                removeStage.add(entry.getKey());
            } else {
                Iterator<PackageSetting> iter = sus.packages.iterator();
                while (iter.hasNext()) {
                    if (this.mPackages.get(iter.next().name) == null) {
                        iter.remove();
                    }
                }
                if (sus.packages.size() == 0) {
                    removeStage.add(entry.getKey());
                }
            }
        }
        for (int i = 0; i < removeStage.size(); i++) {
            this.mSharedUsers.remove(removeStage.get(i));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0104, code lost:
        if (isAdbInstallDisallowed(r63, r7.id) != false) goto L_0x0109;
     */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x011f  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0122  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0157  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x015e  */
    static PackageSetting createNewSetting(String pkgName, PackageSetting originalPkg, PackageSetting disabledPkg, String realPkgName, SharedUserSetting sharedUser, File codePath, File resourcePath, String legacyNativeLibraryPath, String primaryCpuAbi, String secondaryCpuAbi, long versionCode, int pkgFlags, int pkgPrivateFlags, UserHandle installUser, boolean allowInstall, boolean instantApp, boolean virtualPreload, String parentPkgName, List<String> childPkgNames, UserManagerService userManager, String[] usesStaticLibraries, long[] usesStaticLibrariesVersions) {
        PackageSetting packageSetting;
        boolean z;
        PackageSetting pkgSetting;
        List<UserInfo> users;
        boolean installed;
        if (originalPkg != null) {
            if (PackageManagerService.DEBUG_UPGRADE) {
                Log.v("PackageManager", "Package " + pkgName + " is adopting original package " + originalPkg.name);
            }
            pkgSetting = new PackageSetting(originalPkg, pkgName);
            pkgSetting.childPackageNames = childPkgNames != null ? new ArrayList(childPkgNames) : null;
            pkgSetting.codePath = codePath;
            pkgSetting.legacyNativeLibraryPathString = legacyNativeLibraryPath;
            pkgSetting.parentPackageName = parentPkgName;
            pkgSetting.pkgFlags = pkgFlags;
            pkgSetting.pkgPrivateFlags = pkgPrivateFlags;
            pkgSetting.primaryCpuAbiString = primaryCpuAbi;
            pkgSetting.resourcePath = resourcePath;
            pkgSetting.secondaryCpuAbiString = secondaryCpuAbi;
            pkgSetting.signatures = new PackageSignatures();
            pkgSetting.versionCode = versionCode;
            pkgSetting.usesStaticLibraries = usesStaticLibraries;
            pkgSetting.usesStaticLibrariesVersions = usesStaticLibrariesVersions;
            pkgSetting.setTimeStamp(codePath.lastModified());
            packageSetting = disabledPkg;
            z = false;
        } else {
            pkgSetting = new PackageSetting(pkgName, realPkgName, codePath, resourcePath, legacyNativeLibraryPath, primaryCpuAbi, secondaryCpuAbi, null, versionCode, pkgFlags, pkgPrivateFlags, parentPkgName, childPkgNames, 0, usesStaticLibraries, usesStaticLibrariesVersions);
            pkgSetting.setTimeStamp(codePath.lastModified());
            pkgSetting.sharedUser = sharedUser;
            if ((pkgFlags & 1) != 0) {
                z = false;
                if (!HwServiceFactory.isCustedCouldStopped(pkgName, false, false)) {
                    if (sharedUser != null) {
                        pkgSetting.appId = sharedUser.userId;
                        packageSetting = disabledPkg;
                    } else {
                        packageSetting = disabledPkg;
                        if (packageSetting != null) {
                            pkgSetting.signatures = new PackageSignatures(packageSetting.signatures);
                            pkgSetting.appId = packageSetting.appId;
                            pkgSetting.getPermissionsState().copyFrom(disabledPkg.getPermissionsState());
                            List<UserInfo> users2 = getAllUsers(userManager);
                            if (users2 != null) {
                                for (UserInfo user : users2) {
                                    int userId = user.id;
                                    pkgSetting.setDisabledComponentsCopy(packageSetting.getDisabledComponents(userId), userId);
                                    pkgSetting.setEnabledComponentsCopy(packageSetting.getEnabledComponents(userId), userId);
                                }
                            }
                        }
                    }
                }
            } else {
                z = false;
            }
            List<UserInfo> users3 = getAllUsers(userManager);
            int installUserId = installUser != null ? installUser.getIdentifier() : z;
            if (users3 != null && allowInstall) {
                Iterator<UserInfo> it = users3.iterator();
                while (it.hasNext()) {
                    UserInfo user2 = it.next();
                    if (installUser != null) {
                        if (installUserId == -1) {
                        }
                        if (installUserId != user2.id) {
                            installed = z;
                            pkgSetting.setUserState(user2.id, 0, 0, !user2.isClonedProfile() ? z : installed, true, true, false, 0, false, null, null, null, null, instantApp, virtualPreload, null, null, null, 0, 0, 0, null);
                        }
                    }
                    installed = true;
                    pkgSetting.setUserState(user2.id, 0, 0, !user2.isClonedProfile() ? z : installed, true, true, false, 0, false, null, null, null, null, instantApp, virtualPreload, null, null, null, 0, 0, 0, null);
                }
                if (sharedUser != null) {
                }
            } else if (sharedUser != null) {
            }
        }
        if ((pkgFlags & 1) != 0 && packageSetting == null && (users = getAllUsers(userManager)) != null) {
            Iterator<UserInfo> it2 = users.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                UserInfo userInfo = it2.next();
                if (userInfo.isClonedProfile()) {
                    pkgSetting.setInstalled(z, userInfo.id);
                    break;
                }
            }
        }
        return pkgSetting;
    }

    static void updatePackageSetting(PackageSetting pkgSetting, PackageSetting disabledPkg, SharedUserSetting sharedUser, File codePath, File resourcePath, String legacyNativeLibraryPath, String primaryCpuAbi, String secondaryCpuAbi, int pkgFlags, int pkgPrivateFlags, List<String> childPkgNames, UserManagerService userManager, String[] usesStaticLibraries, long[] usesStaticLibrariesVersions) throws PackageManagerException {
        String str;
        boolean isSystem;
        String pkgName = pkgSetting.name;
        if (pkgSetting.sharedUser != sharedUser) {
            StringBuilder sb = new StringBuilder();
            sb.append("Package ");
            sb.append(pkgName);
            sb.append(" shared user changed from ");
            String str2 = "<nothing>";
            sb.append(pkgSetting.sharedUser != null ? pkgSetting.sharedUser.name : str2);
            sb.append(" to ");
            if (sharedUser != null) {
                str2 = sharedUser.name;
            }
            sb.append(str2);
            PackageManagerService.reportSettingsProblem(5, sb.toString());
            throw new PackageManagerException(-8, "Updating application package " + pkgName + " failed");
        }
        String str3 = " system";
        String str4 = "";
        if (!pkgSetting.codePath.equals(codePath)) {
            boolean isSystem2 = pkgSetting.isSystem();
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Update");
            str = str3;
            if (!isSystem2) {
                str3 = str4;
            }
            sb2.append(str3);
            sb2.append(" package ");
            sb2.append(pkgName);
            sb2.append(" code path from ");
            sb2.append(pkgSetting.codePathString);
            sb2.append(" to ");
            sb2.append(codePath.toString());
            sb2.append("; Retain data and using new");
            Slog.i("PackageManager", sb2.toString());
            if (!isSystem2) {
                if ((pkgFlags & 1) != 0 && disabledPkg == null) {
                    UserInfo userInfo = getAllUsers(userManager);
                    if (userInfo != null) {
                        for (UserInfo userInfo2 : userInfo) {
                            if (userInfo2.isClonedProfile()) {
                                isSystem = isSystem2;
                                if (!pkgSetting.getInstalled(userInfo2.id)) {
                                    isSystem2 = isSystem;
                                    userInfo = userInfo;
                                }
                            } else {
                                isSystem = isSystem2;
                            }
                            pkgSetting.setInstalled(true, userInfo2.id);
                            isSystem2 = isSystem;
                            userInfo = userInfo;
                        }
                    }
                }
                pkgSetting.legacyNativeLibraryPathString = legacyNativeLibraryPath;
            }
            pkgSetting.codePath = codePath;
            pkgSetting.codePathString = codePath.toString();
        } else {
            str = str3;
        }
        if (!pkgSetting.resourcePath.equals(resourcePath)) {
            boolean isSystem3 = pkgSetting.isSystem();
            StringBuilder sb3 = new StringBuilder();
            sb3.append("Update");
            if (isSystem3) {
                str4 = str;
            }
            sb3.append(str4);
            sb3.append(" package ");
            sb3.append(pkgName);
            sb3.append(" resource path from ");
            sb3.append(pkgSetting.resourcePathString);
            sb3.append(" to ");
            sb3.append(resourcePath.toString());
            sb3.append("; Retain data and using new");
            Slog.i("PackageManager", sb3.toString());
            pkgSetting.resourcePath = resourcePath;
            pkgSetting.resourcePathString = resourcePath.toString();
        }
        pkgSetting.pkgFlags &= -2;
        pkgSetting.pkgPrivateFlags &= -1076756489;
        pkgSetting.pkgFlags |= pkgFlags & 1;
        pkgSetting.pkgPrivateFlags |= pkgPrivateFlags & 8;
        pkgSetting.pkgPrivateFlags |= pkgPrivateFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS;
        pkgSetting.pkgPrivateFlags |= pkgPrivateFlags & DumpState.DUMP_DOMAIN_PREFERRED;
        pkgSetting.pkgPrivateFlags |= pkgPrivateFlags & DumpState.DUMP_FROZEN;
        pkgSetting.pkgPrivateFlags |= pkgPrivateFlags & DumpState.DUMP_COMPILER_STATS;
        pkgSetting.pkgPrivateFlags |= pkgPrivateFlags & 1073741824;
        pkgSetting.primaryCpuAbiString = primaryCpuAbi;
        pkgSetting.secondaryCpuAbiString = secondaryCpuAbi;
        if (childPkgNames != null) {
            pkgSetting.childPackageNames = new ArrayList(childPkgNames);
        }
        if (usesStaticLibraries == null || usesStaticLibrariesVersions == null || usesStaticLibraries.length != usesStaticLibrariesVersions.length) {
            pkgSetting.usesStaticLibraries = null;
            pkgSetting.usesStaticLibrariesVersions = null;
            return;
        }
        pkgSetting.usesStaticLibraries = usesStaticLibraries;
        pkgSetting.usesStaticLibrariesVersions = usesStaticLibrariesVersions;
    }

    /* access modifiers changed from: package-private */
    public boolean registerAppIdLPw(PackageSetting p) throws PackageManagerException {
        boolean createdNew;
        if (p.appId == 0) {
            p.appId = acquireAndRegisterNewAppIdLPw(p);
            createdNew = true;
        } else {
            createdNew = registerExistingAppIdLPw(p.appId, p, p.name);
        }
        if (p.appId >= 0) {
            return createdNew;
        }
        PackageManagerService.reportSettingsProblem(5, "Package " + p.name + " could not be assigned a valid UID");
        throw new PackageManagerException(-4, "Package " + p.name + " could not be assigned a valid UID");
    }

    /* access modifiers changed from: package-private */
    public void writeUserRestrictionsLPw(PackageSetting newPackage, PackageSetting oldPackage) {
        List<UserInfo> allUsers;
        PackageUserState oldUserState;
        if (!(getPackageLPr(newPackage.name) == null || (allUsers = getAllUsers(UserManagerService.getInstance())) == null)) {
            for (UserInfo user : allUsers) {
                if (oldPackage == null) {
                    oldUserState = PackageSettingBase.DEFAULT_USER_STATE;
                } else {
                    oldUserState = oldPackage.readUserState(user.id);
                }
                if (!oldUserState.equals(newPackage.readUserState(user.id))) {
                    writePackageRestrictionsLPr(user.id);
                }
            }
        }
    }

    static boolean isAdbInstallDisallowed(UserManagerService userManager, int userId) {
        return userManager.hasUserRestriction("no_debugging_features", userId);
    }

    /* access modifiers changed from: package-private */
    public void insertPackageSettingLPw(PackageSetting p, PackageParser.Package pkg) {
        if (p.signatures.mSigningDetails.signatures == null) {
            p.signatures.mSigningDetails = pkg.mSigningDetails;
        }
        if (p.sharedUser != null && p.sharedUser.signatures.mSigningDetails.signatures == null) {
            p.sharedUser.signatures.mSigningDetails = pkg.mSigningDetails;
        }
        addPackageSettingLPw(p, p.sharedUser);
    }

    private void addPackageSettingLPw(PackageSetting p, SharedUserSetting sharedUser) {
        this.mPackages.put(p.name, p);
        if (sharedUser != null) {
            if (p.sharedUser != null && p.sharedUser != sharedUser) {
                PackageManagerService.reportSettingsProblem(6, "Package " + p.name + " was user " + p.sharedUser + " but is now " + sharedUser + "; I am not changing its files so it will probably fail!");
                p.sharedUser.removePackage(p);
            } else if (p.appId != sharedUser.userId) {
                PackageManagerService.reportSettingsProblem(6, "Package " + p.name + " was user id " + p.appId + " but is now user " + sharedUser + " with id " + sharedUser.userId + "; I am not changing its files so it will probably fail!");
            }
            sharedUser.addPackage(p);
            p.sharedUser = sharedUser;
            p.appId = sharedUser.userId;
        }
        Object userIdPs = getSettingLPr(p.appId);
        if (sharedUser == null) {
            if (!(userIdPs == null || userIdPs == p)) {
                replaceAppIdLPw(p.appId, p);
            }
        } else if (!(userIdPs == null || userIdPs == sharedUser)) {
            replaceAppIdLPw(p.appId, sharedUser);
        }
        IntentFilterVerificationInfo ivi = this.mRestoredIntentFilterVerifications.get(p.name);
        if (ivi != null) {
            if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                Slog.i(TAG, "Applying restored IVI for " + p.name + " : " + ivi.getStatusString());
            }
            this.mRestoredIntentFilterVerifications.remove(p.name);
            p.setIntentFilterVerificationInfo(ivi);
        }
    }

    /* access modifiers changed from: package-private */
    public int updateSharedUserPermsLPw(PackageSetting deletedPs, int userId) {
        if (deletedPs == null || deletedPs.pkg == null) {
            Slog.i("PackageManager", "Trying to update info for null package. Just ignoring");
            return -10000;
        } else if (deletedPs.sharedUser == null) {
            return -10000;
        } else {
            SharedUserSetting sus = deletedPs.sharedUser;
            int affectedUserId = -10000;
            Iterator it = deletedPs.pkg.requestedPermissions.iterator();
            while (it.hasNext()) {
                String eachPerm = (String) it.next();
                BasePermission bp = this.mPermissions.getPermission(eachPerm);
                if (bp != null) {
                    boolean used = false;
                    Iterator<PackageSetting> it2 = sus.packages.iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        }
                        PackageSetting pkg = it2.next();
                        if (pkg.pkg != null && !pkg.pkg.packageName.equals(deletedPs.pkg.packageName) && pkg.pkg.requestedPermissions.contains(eachPerm)) {
                            used = true;
                            break;
                        }
                    }
                    if (!used) {
                        PermissionsState permissionsState = sus.getPermissionsState();
                        PackageSetting disabledPs = getDisabledSystemPkgLPr(deletedPs.pkg.packageName);
                        if (disabledPs != null) {
                            boolean reqByDisabledSysPkg = false;
                            Iterator it3 = disabledPs.pkg.requestedPermissions.iterator();
                            while (true) {
                                if (it3.hasNext()) {
                                    if (((String) it3.next()).equals(eachPerm)) {
                                        reqByDisabledSysPkg = true;
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                            if (reqByDisabledSysPkg) {
                            }
                        }
                        permissionsState.updatePermissionFlags(bp, userId, 130047, 0);
                        if (permissionsState.revokeInstallPermission(bp) == 1) {
                            affectedUserId = -1;
                        }
                        if (permissionsState.revokeRuntimePermission(bp, userId) == 1) {
                            if (affectedUserId == -10000) {
                                affectedUserId = userId;
                            } else if (affectedUserId != userId) {
                                affectedUserId = -1;
                            }
                        }
                    }
                }
            }
            return affectedUserId;
        }
    }

    /* access modifiers changed from: package-private */
    public int removePackageLPw(String name) {
        PackageSetting p = this.mPackages.get(name);
        if (p == null) {
            return -1;
        }
        this.mPackages.remove(name);
        removeInstallerPackageStatus(name);
        if (p.sharedUser != null) {
            p.sharedUser.removePackage(p);
            if (p.sharedUser.packages.size() != 0 || p.sharedUser.userId == 5513) {
                return -1;
            }
            this.mSharedUsers.remove(p.sharedUser.name);
            removeAppIdLPw(p.sharedUser.userId);
            return p.sharedUser.userId;
        }
        removeAppIdLPw(p.appId);
        return p.appId;
    }

    private void removeInstallerPackageStatus(String packageName) {
        if (this.mInstallerPackages.contains(packageName)) {
            for (int i = 0; i < this.mPackages.size(); i++) {
                PackageSetting ps = this.mPackages.valueAt(i);
                String installerPackageName = ps.getInstallerPackageName();
                if (installerPackageName != null && installerPackageName.equals(packageName)) {
                    ps.setInstallerPackageName(null);
                    ps.isOrphaned = true;
                }
            }
            this.mInstallerPackages.remove(packageName);
        }
    }

    private boolean registerExistingAppIdLPw(int appId, SettingBase obj, Object name) {
        if (appId > 19999) {
            return false;
        }
        if (appId >= 10000) {
            int index = appId - 10000;
            for (int size = this.mAppIds.size(); index >= size; size++) {
                this.mAppIds.add(null);
            }
            if (this.mAppIds.get(index) == null || isReservedUid(index)) {
                this.mAppIds.set(index, obj);
                return true;
            }
            PackageManagerService.reportSettingsProblem(6, "Adding duplicate app id: " + appId + " name=" + name);
            return false;
        } else if (this.mOtherAppIds.get(appId) != null) {
            PackageManagerService.reportSettingsProblem(6, "Adding duplicate shared id: " + appId + " name=" + name);
            return false;
        } else {
            this.mOtherAppIds.put(appId, obj);
            return true;
        }
    }

    public SettingBase getSettingLPr(int appId) {
        if (appId < 10000) {
            return this.mOtherAppIds.get(appId);
        }
        int index = appId - 10000;
        if (index < this.mAppIds.size()) {
            return this.mAppIds.get(index);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void removeAppIdLPw(int appId) {
        if (appId >= 10000) {
            int index = appId - 10000;
            if (index < this.mAppIds.size()) {
                this.mAppIds.set(index, null);
            }
        } else {
            this.mOtherAppIds.remove(appId);
        }
        setFirstAvailableUid(appId + 1);
    }

    private void replaceAppIdLPw(int appId, SettingBase obj) {
        if (appId >= 10000) {
            int index = appId - 10000;
            if (index < this.mAppIds.size()) {
                this.mAppIds.set(index, obj);
                return;
            }
            return;
        }
        this.mOtherAppIds.put(appId, obj);
    }

    /* access modifiers changed from: package-private */
    public PreferredIntentResolver editPreferredActivitiesLPw(int userId) {
        PreferredIntentResolver pir = this.mPreferredActivities.get(userId);
        if (pir != null) {
            return pir;
        }
        PreferredIntentResolver pir2 = new PreferredIntentResolver();
        this.mPreferredActivities.put(userId, pir2);
        return pir2;
    }

    /* access modifiers changed from: package-private */
    public PersistentPreferredIntentResolver editPersistentPreferredActivitiesLPw(int userId) {
        PersistentPreferredIntentResolver ppir = this.mPersistentPreferredActivities.get(userId);
        if (ppir != null) {
            return ppir;
        }
        PersistentPreferredIntentResolver ppir2 = new PersistentPreferredIntentResolver();
        this.mPersistentPreferredActivities.put(userId, ppir2);
        return ppir2;
    }

    /* access modifiers changed from: package-private */
    public CrossProfileIntentResolver editCrossProfileIntentResolverLPw(int userId) {
        CrossProfileIntentResolver cpir = this.mCrossProfileIntentResolvers.get(userId);
        if (cpir != null) {
            return cpir;
        }
        CrossProfileIntentResolver cpir2 = new CrossProfileIntentResolver();
        this.mCrossProfileIntentResolvers.put(userId, cpir2);
        return cpir2;
    }

    /* access modifiers changed from: package-private */
    public IntentFilterVerificationInfo getIntentFilterVerificationLPr(String packageName) {
        PackageSetting ps = this.mPackages.get(packageName);
        if (ps != null) {
            return ps.getIntentFilterVerificationInfo();
        }
        if (!PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
            return null;
        }
        Slog.w("PackageManager", "No package known: " + packageName);
        return null;
    }

    /* access modifiers changed from: package-private */
    public IntentFilterVerificationInfo createIntentFilterVerificationIfNeededLPw(String packageName, ArraySet<String> domains) {
        PackageSetting ps = this.mPackages.get(packageName);
        if (ps != null) {
            IntentFilterVerificationInfo ivi = ps.getIntentFilterVerificationInfo();
            if (ivi == null) {
                ivi = new IntentFilterVerificationInfo(packageName, domains);
                ps.setIntentFilterVerificationInfo(ivi);
                if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                    Slog.d("PackageManager", "Creating new IntentFilterVerificationInfo for pkg: " + packageName);
                }
            } else {
                ivi.setDomains(domains);
                if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                    Slog.d("PackageManager", "Setting domains to existing IntentFilterVerificationInfo for pkg: " + packageName + " and with domains: " + ivi.getDomainsString());
                }
            }
            return ivi;
        } else if (!PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
            return null;
        } else {
            Slog.w("PackageManager", "No package known: " + packageName);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public int getIntentFilterVerificationStatusLPr(String packageName, int userId) {
        PackageSetting ps = this.mPackages.get(packageName);
        if (ps != null) {
            return (int) (ps.getDomainVerificationStatusForUser(userId) >> 32);
        }
        if (!PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
            return 0;
        }
        Slog.w("PackageManager", "No package known: " + packageName);
        return 0;
    }

    /* access modifiers changed from: package-private */
    public boolean updateIntentFilterVerificationStatusLPw(String packageName, int status, int userId) {
        int alwaysGeneration;
        PackageSetting current = this.mPackages.get(packageName);
        if (current != null) {
            if (status == 2) {
                alwaysGeneration = this.mNextAppLinkGeneration.get(userId) + 1;
                this.mNextAppLinkGeneration.put(userId, alwaysGeneration);
            } else {
                alwaysGeneration = 0;
            }
            current.setDomainVerificationStatusForUser(status, alwaysGeneration, userId);
            return true;
        } else if (!PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
            return false;
        } else {
            Slog.w("PackageManager", "No package known: " + packageName);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public List<IntentFilterVerificationInfo> getIntentFilterVerificationsLPr(String packageName) {
        if (packageName == null) {
            return Collections.emptyList();
        }
        ArrayList<IntentFilterVerificationInfo> result = new ArrayList<>();
        for (PackageSetting ps : this.mPackages.values()) {
            IntentFilterVerificationInfo ivi = ps.getIntentFilterVerificationInfo();
            if (ivi != null && !TextUtils.isEmpty(ivi.getPackageName()) && ivi.getPackageName().equalsIgnoreCase(packageName)) {
                result.add(ivi);
            }
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public boolean removeIntentFilterVerificationLPw(String packageName, int userId, boolean alsoResetStatus) {
        PackageSetting ps = this.mPackages.get(packageName);
        if (ps != null) {
            if (alsoResetStatus) {
                ps.clearDomainVerificationStatusForUser(userId);
            }
            ps.setIntentFilterVerificationInfo(null);
            return true;
        } else if (!PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
            return false;
        } else {
            Slog.w("PackageManager", "No package known: " + packageName);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean removeIntentFilterVerificationLPw(String packageName, int[] userIds) {
        boolean result = false;
        for (int userId : userIds) {
            result |= removeIntentFilterVerificationLPw(packageName, userId, true);
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public String removeDefaultBrowserPackageNameLPw(int userId) {
        if (userId == -1) {
            return null;
        }
        return (String) this.mDefaultBrowserApp.removeReturnOld(userId);
    }

    private File getUserPackagesStateFile(int userId) {
        return new File(new File(new File(this.mSystemDir, DatabaseHelper.SoundModelContract.KEY_USERS), Integer.toString(userId)), "package-restrictions.xml");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private File getUserRuntimePermissionsFile(int userId) {
        return new File(new File(new File(this.mSystemDir, DatabaseHelper.SoundModelContract.KEY_USERS), Integer.toString(userId)), RUNTIME_PERMISSIONS_FILE_NAME);
    }

    private File getUserPackagesStateBackupFile(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), "package-restrictions-backup.xml");
    }

    /* access modifiers changed from: package-private */
    public void writeAllUsersPackageRestrictionsLPr() {
        List<UserInfo> users = getAllUsers(UserManagerService.getInstance());
        if (users != null) {
            for (UserInfo user : users) {
                writePackageRestrictionsLPr(user.id);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeAllRuntimePermissionsLPr() {
        for (int userId : UserManagerService.getInstance().getUserIds()) {
            this.mRuntimePermissionsPersistence.writePermissionsForUserAsyncLPr(userId);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean areDefaultRuntimePermissionsGrantedLPr(int userId) {
        return this.mRuntimePermissionsPersistence.areDefaultRuntimePermissionsGrantedLPr(userId);
    }

    /* access modifiers changed from: package-private */
    public void setRuntimePermissionsFingerPrintLPr(String fingerPrint, int userId) {
        this.mRuntimePermissionsPersistence.setRuntimePermissionsFingerPrintLPr(fingerPrint, userId);
    }

    /* access modifiers changed from: package-private */
    public int getDefaultRuntimePermissionsVersionLPr(int userId) {
        return this.mRuntimePermissionsPersistence.getVersionLPr(userId);
    }

    /* access modifiers changed from: package-private */
    public void setDefaultRuntimePermissionsVersionLPr(int version, int userId) {
        this.mRuntimePermissionsPersistence.setVersionLPr(version, userId);
    }

    public VersionInfo findOrCreateVersion(String volumeUuid) {
        VersionInfo ver = this.mVersion.get(volumeUuid);
        if (ver != null) {
            return ver;
        }
        VersionInfo ver2 = new VersionInfo();
        this.mVersion.put(volumeUuid, ver2);
        return ver2;
    }

    public VersionInfo getInternalVersion() {
        return this.mVersion.get(StorageManager.UUID_PRIVATE_INTERNAL);
    }

    public VersionInfo getExternalVersion() {
        return this.mVersion.get("primary_physical");
    }

    public void onVolumeForgotten(String fsUuid) {
        this.mVersion.remove(fsUuid);
    }

    /* access modifiers changed from: package-private */
    public void readPreferredActivitiesLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    PreferredActivity pa = new PreferredActivity(parser);
                    if (pa.mPref.getParseError() == null) {
                        editPreferredActivitiesLPw(userId).addFilter(pa);
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <preferred-activity> " + pa.mPref.getParseError() + " at " + parser.getPositionDescription());
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <preferred-activities>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    private void readPersistentPreferredActivitiesLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    editPersistentPreferredActivitiesLPw(userId).addFilter(new PersistentPreferredActivity(parser));
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <persistent-preferred-activities>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    private void readCrossProfileIntentFiltersLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        synchronized (this.mCrossProfileIntentResolvers) {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                    break;
                } else if (type != 3) {
                    if (type != 4) {
                        String tagName = parser.getName();
                        if (tagName.equals(TAG_ITEM)) {
                            editCrossProfileIntentResolverLPw(userId).addFilter(new CrossProfileIntentFilter(parser));
                        } else {
                            PackageManagerService.reportSettingsProblem(5, "Unknown element under crossProfile-intent-filters: " + tagName);
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                }
            }
        }
    }

    private void readDomainVerificationLPw(XmlPullParser parser, PackageSettingBase packageSetting) throws XmlPullParserException, IOException {
        packageSetting.setIntentFilterVerificationInfo(new IntentFilterVerificationInfo(parser));
    }

    private void readRestoredIntentFilterVerifications(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(TAG_DOMAIN_VERIFICATION)) {
                    IntentFilterVerificationInfo ivi = new IntentFilterVerificationInfo(parser);
                    if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                        Slog.i(TAG, "Restored IVI for " + ivi.getPackageName() + " status=" + ivi.getStatusString());
                    }
                    this.mRestoredIntentFilterVerifications.put(ivi.getPackageName(), ivi);
                } else {
                    Slog.w(TAG, "Unknown element: " + tagName);
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readDefaultAppsLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(TAG_DEFAULT_BROWSER)) {
                    this.mDefaultBrowserApp.put(userId, parser.getAttributeValue(null, ATTR_PACKAGE_NAME));
                } else if (!tagName.equals(TAG_DEFAULT_DIALER)) {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under default-apps: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readBlockUninstallPackagesLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        ArraySet<String> packages = new ArraySet<>();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                break;
            } else if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_BLOCK_UNINSTALL)) {
                    packages.add(parser.getAttributeValue(null, ATTR_PACKAGE_NAME));
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under block-uninstall-packages: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        if (packages.isEmpty()) {
            this.mBlockUninstallPackages.remove(userId);
        } else {
            this.mBlockUninstallPackages.put(userId, packages);
        }
    }

    /* JADX INFO: Multiple debug info for r2v30 'fileName'  java.lang.String: [D('exceptionName' java.lang.String), D('fileName' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r13v15 'exceptionName'  java.lang.String: [D('exceptionName' java.lang.String), D('backupFile' java.io.File)] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x030c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x030d, code lost:
        r11 = r2;
        r54 = r12;
        r3 = r14;
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:0x0316, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x0317, code lost:
        r4 = r0;
        r11 = r2;
        r26 = r3;
        r54 = r12;
        r2 = r13;
        r3 = r14;
        r7 = r33;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x0323, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x0324, code lost:
        r4 = r0;
        r11 = r2;
        r54 = r12;
        r3 = r14;
        r5 = r29;
        r7 = r33;
        r6 = r36;
        r8 = 6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x0335, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x0336, code lost:
        r4 = r0;
        r11 = r2;
        r54 = r12;
        r3 = r14;
        r5 = r30;
        r7 = r33;
        r6 = r36;
        r8 = 6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:257:0x0775, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:258:0x0776, code lost:
        r54 = r12;
        r1 = r63;
        r3 = r64;
        r2 = r0;
        r11 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:259:0x0787, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:260:0x0788, code lost:
        r54 = r12;
        r38 = r3;
        r58 = r2;
        r1 = r63;
        r3 = r64;
        r4 = r0;
        r2 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:262:0x079a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:263:0x079b, code lost:
        r54 = r12;
        r38 = r3;
        r58 = r2;
        r1 = r63;
        r3 = r64;
        r4 = r0;
        r5 = r29;
        r6 = r36;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:265:0x07b2, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:266:0x07b3, code lost:
        r54 = r12;
        r38 = r3;
        r58 = r2;
        r1 = r63;
        r3 = r64;
        r4 = r0;
        r5 = r30;
        r6 = r36;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:268:0x07f1, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:269:0x07f2, code lost:
        r58 = r2;
        r54 = r12;
        r1 = r63;
        r3 = r64;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:309:0x090c, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:310:0x090f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:311:0x0910, code lost:
        r4 = r0;
        r5 = r29;
        r6 = r36;
        r11 = r58;
        r8 = 6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:312:0x091e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:313:0x091f, code lost:
        r4 = r0;
        r5 = r30;
        r6 = r36;
        r11 = r58;
        r8 = 6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:314:0x092d, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:315:0x092f, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:316:0x0931, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:317:0x0932, code lost:
        r7 = r37;
        r4 = r0;
        r5 = r29;
        r6 = r36;
        r11 = r58;
        r8 = 6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:318:0x0942, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:319:0x0943, code lost:
        r7 = r37;
        r4 = r0;
        r5 = r30;
        r6 = r36;
        r11 = r58;
        r8 = 6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:320:0x0953, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:321:0x0954, code lost:
        r1 = r63;
        r3 = r64;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:322:0x0958, code lost:
        r2 = r0;
        r11 = r58;
        r13 = r59;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:323:0x095f, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:324:0x0960, code lost:
        r1 = r63;
        r3 = r64;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:325:0x0964, code lost:
        r7 = r37;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:326:0x0966, code lost:
        r4 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:328:0x096f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:329:0x0970, code lost:
        r1 = r63;
        r3 = r64;
        r7 = r37;
        r4 = r0;
        r5 = r29;
        r6 = r36;
        r11 = r58;
        r8 = 6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:330:0x0984, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:331:0x0985, code lost:
        r1 = r63;
        r3 = r64;
        r7 = r37;
        r4 = r0;
        r5 = r30;
        r6 = r36;
        r11 = r58;
        r8 = 6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:385:0x0afa, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:386:0x0afb, code lost:
        r11 = r2;
        r54 = r12;
        r3 = r14;
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:387:0x0b05, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:388:0x0b06, code lost:
        r11 = r2;
        r54 = r12;
        r2 = r13;
        r3 = r14;
        r7 = r33;
        r4 = r0;
        r26 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:400:0x0b6c, code lost:
        if (r34.exists() != false) goto L_0x0b6e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:401:0x0b6e, code lost:
        r34.delete();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:402:0x0b71, code lost:
        r1.reportReadFileError(r11, r10, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:403:0x0b76, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:404:0x0b77, code lost:
        r11 = r2;
        r54 = r12;
        r3 = r14;
        r5 = r29;
        r7 = r33;
        r6 = r36;
        r8 = 6;
        r4 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:411:0x0bcc, code lost:
        if (r34.exists() != false) goto L_0x0b6e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:412:0x0bcf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:413:0x0bd0, code lost:
        r11 = r2;
        r54 = r12;
        r3 = r14;
        r5 = r30;
        r7 = r33;
        r6 = r36;
        r8 = 6;
        r4 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:420:0x0c25, code lost:
        if (r34.exists() != false) goto L_0x0b6e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:452:?, code lost:
        return;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x030c A[ExcHandler: all (r0v64 'th' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:107:0x02f1] */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0775 A[ExcHandler: all (r0v46 'th' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:140:0x03e0] */
    /* JADX WARNING: Removed duplicated region for block: B:268:0x07f1 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:125:0x0375] */
    /* JADX WARNING: Removed duplicated region for block: B:314:0x092d A[ExcHandler: all (th java.lang.Throwable), PHI: r1 r3 
      PHI: (r1v24 com.android.server.pm.Settings) = (r1v26 com.android.server.pm.Settings), (r1v26 com.android.server.pm.Settings), (r1v26 com.android.server.pm.Settings), (r1v26 com.android.server.pm.Settings), (r1v27 com.android.server.pm.Settings), (r1v27 com.android.server.pm.Settings) binds: [B:291:0x089c, B:292:?, B:305:0x08e9, B:306:?, B:281:0x0863, B:282:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r3v42 int) = (r3v44 int), (r3v44 int), (r3v44 int), (r3v44 int), (r3v45 int), (r3v45 int) binds: [B:291:0x089c, B:292:?, B:305:0x08e9, B:306:?, B:281:0x0863, B:282:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:281:0x0863] */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0953 A[ExcHandler: all (th java.lang.Throwable), PHI: r54 r58 r59 
      PHI: (r54v24 'userPackagesStateFile' java.io.File) = (r54v27 'userPackagesStateFile' java.io.File), (r54v27 'userPackagesStateFile' java.io.File), (r54v50 'userPackagesStateFile' java.io.File), (r54v50 'userPackagesStateFile' java.io.File) binds: [B:277:0x0856, B:278:?, B:234:0x069d, B:235:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r58v13 'fileName' java.lang.String) = (r58v16 'fileName' java.lang.String), (r58v16 'fileName' java.lang.String), (r58v35 'fileName' java.lang.String), (r58v35 'fileName' java.lang.String) binds: [B:277:0x0856, B:278:?, B:234:0x069d, B:235:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r59v6 'exceptionName' java.lang.String) = (r59v9 'exceptionName' java.lang.String), (r59v9 'exceptionName' java.lang.String), (r59v13 'exceptionName' java.lang.String), (r59v13 'exceptionName' java.lang.String) binds: [B:277:0x0856, B:278:?, B:234:0x069d, B:235:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:234:0x069d] */
    /* JADX WARNING: Removed duplicated region for block: B:385:0x0afa A[ExcHandler: all (r0v5 'th' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:91:0x02c2] */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0b05 A[ExcHandler: Exception (r0v4 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:91:0x02c2] */
    /* JADX WARNING: Removed duplicated region for block: B:396:0x0b5f  */
    /* JADX WARNING: Removed duplicated region for block: B:407:0x0bbf  */
    /* JADX WARNING: Removed duplicated region for block: B:416:0x0c18  */
    /* JADX WARNING: Removed duplicated region for block: B:426:0x0c37  */
    /* JADX WARNING: Removed duplicated region for block: B:451:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:453:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:454:? A[RETURN, SYNTHETIC] */
    public void readPackageRestrictionsLPr(int userId) {
        FileInputStream str;
        String fileName;
        File userPackagesStateFile;
        File backupFile;
        String exceptionName;
        Throwable th;
        int i;
        String str2;
        String str3;
        String str4;
        XmlPullParserException e;
        String exceptionName2;
        String str5;
        int i2;
        String str6;
        String str7;
        IOException e2;
        String str8;
        String str9;
        Exception e3;
        String fileName2;
        String str10;
        int i3;
        File userPackagesStateFile2;
        FileInputStream str11;
        String fileName3;
        XmlPullParser parser;
        int type;
        int i4;
        String fileName4;
        FileInputStream str12;
        int maxAppLinkGeneration;
        String exceptionName3;
        String exceptionName4;
        String str13;
        FileInputStream fileInputStream;
        String str14;
        FileInputStream fileInputStream2;
        String str15;
        FileInputStream fileInputStream3;
        String exceptionName5;
        String fileName5;
        FileInputStream str16;
        String str17;
        int i5;
        String str18;
        int i6;
        char c;
        int outerDepth;
        XmlPullParser parser2;
        int maxAppLinkGeneration2;
        String str19;
        Exception e4;
        int maxAppLinkGeneration3;
        int packageDepth;
        int i7;
        int packageDepth2;
        int type2;
        int packageDepth3;
        int type3;
        char c2;
        int packageDepth4;
        Settings settings = this;
        int i8 = userId;
        String str20 = "pkg";
        File userPackagesStateFile3 = getUserPackagesStateFile(userId);
        File backupFile2 = getUserPackagesStateBackupFile(userId);
        String str21 = "PackageManager";
        int i9 = 4;
        if (backupFile2.exists()) {
            try {
                FileInputStream str22 = new FileInputStream(backupFile2);
                String fileName6 = backupFile2.getName();
                settings.mReadMessages.append("Reading from backup stopped packages file\n");
                PackageManagerService.reportSettingsProblem(4, "Need to read from backup stopped packages file");
                if (userPackagesStateFile3.exists()) {
                    Slog.w(str21, "Cleaning up stopped packages file " + userPackagesStateFile3);
                    userPackagesStateFile3.delete();
                }
                str = str22;
                fileName = fileName6;
            } catch (IOException e5) {
                str = null;
                fileName = "";
            }
        } else {
            str = null;
            fileName = "";
        }
        boolean isSuccess = true;
        String exceptionName6 = "";
        String str23 = "Error reading package manager stopped packages";
        String str24 = "Error reading settings: ";
        int i10 = 6;
        String fileName7 = "Error reading: ";
        if (str == null) {
            try {
                if (!userPackagesStateFile3.exists()) {
                    try {
                        settings.mReadMessages.append("No stopped packages file found\n");
                        PackageManagerService.reportSettingsProblem(4, "No stopped packages file; assuming all started");
                        for (PackageSetting pkg : settings.mPackages.values()) {
                            try {
                                str9 = str24;
                                str5 = str23;
                                backupFile = backupFile2;
                                str8 = fileName7;
                                try {
                                    pkg.setUserState(userId, 0, 0, true, false, false, false, 0, false, null, null, null, null, false, false, null, null, null, 0, 0, 0, null);
                                    i8 = i8;
                                    str24 = str9;
                                    str23 = str5;
                                    exceptionName6 = exceptionName6;
                                    fileName = fileName;
                                    str21 = str21;
                                    backupFile2 = backupFile;
                                    userPackagesStateFile3 = userPackagesStateFile3;
                                    fileName7 = str8;
                                    i10 = 6;
                                } catch (XmlPullParserException e6) {
                                    e = e6;
                                    i8 = i8;
                                    str4 = str5;
                                    fileName = fileName;
                                    str2 = str21;
                                    userPackagesStateFile = userPackagesStateFile3;
                                    str3 = str8;
                                    i = 6;
                                    settings = this;
                                    exceptionName2 = "XmlPullParserException";
                                    settings.mReadMessages.append(str3 + e.toString());
                                    PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                    Slog.wtf(str2, str4, e);
                                    if (0 != 0) {
                                    }
                                } catch (IOException e7) {
                                    e2 = e7;
                                    i8 = i8;
                                    str7 = str9;
                                    fileName = fileName;
                                    str2 = str21;
                                    userPackagesStateFile = userPackagesStateFile3;
                                    str6 = str8;
                                    i2 = 6;
                                    settings = this;
                                    exceptionName2 = "IOException";
                                    settings.mReadMessages.append(str6 + e2.toString());
                                    PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                                    Slog.wtf(str2, str5, e2);
                                    if (0 != 0) {
                                    }
                                } catch (Exception e8) {
                                    e3 = e8;
                                    i8 = i8;
                                    fileName2 = exceptionName6;
                                    fileName = fileName;
                                    str2 = str21;
                                    userPackagesStateFile = userPackagesStateFile3;
                                    settings = this;
                                    isSuccess = false;
                                    try {
                                        exceptionName2 = e3.getClass().toString();
                                        try {
                                            settings.mReadMessages.append(str8 + exceptionName2);
                                            PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                                            Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                                            if (0 == 0) {
                                            }
                                        } catch (Throwable th2) {
                                            th = th2;
                                            exceptionName = exceptionName2;
                                        }
                                    } catch (Throwable th3) {
                                        exceptionName = fileName2;
                                        th = th3;
                                        if (!isSuccess) {
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    i8 = i8;
                                    exceptionName = exceptionName6;
                                    fileName = fileName;
                                    userPackagesStateFile = userPackagesStateFile3;
                                    settings = this;
                                    if (!isSuccess) {
                                    }
                                    throw th;
                                }
                            } catch (XmlPullParserException e9) {
                                backupFile = backupFile2;
                                settings = this;
                                str3 = fileName7;
                                i = i10;
                                str4 = str23;
                                str2 = str21;
                                userPackagesStateFile = userPackagesStateFile3;
                                e = e9;
                                exceptionName2 = "XmlPullParserException";
                                settings.mReadMessages.append(str3 + e.toString());
                                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                Slog.wtf(str2, str4, e);
                                if (0 != 0) {
                                }
                            } catch (IOException e10) {
                                str5 = str23;
                                backupFile = backupFile2;
                                settings = this;
                                str6 = fileName7;
                                str7 = str24;
                                userPackagesStateFile = userPackagesStateFile3;
                                e2 = e10;
                                i2 = i10;
                                str2 = str21;
                                exceptionName2 = "IOException";
                                settings.mReadMessages.append(str6 + e2.toString());
                                PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                                Slog.wtf(str2, str5, e2);
                                if (0 != 0) {
                                }
                            } catch (Exception e11) {
                                str8 = fileName7;
                                str9 = str24;
                                backupFile = backupFile2;
                                settings = this;
                                e3 = e11;
                                fileName2 = exceptionName6;
                                str2 = str21;
                                userPackagesStateFile = userPackagesStateFile3;
                                isSuccess = false;
                                exceptionName2 = e3.getClass().toString();
                                settings.mReadMessages.append(str8 + exceptionName2);
                                PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                                Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                                if (0 == 0) {
                                }
                            } catch (Throwable th5) {
                                backupFile = backupFile2;
                                settings = this;
                                th = th5;
                                userPackagesStateFile = userPackagesStateFile3;
                                exceptionName = exceptionName6;
                                if (!isSuccess) {
                                }
                                throw th;
                            }
                        }
                        if (1 == 0) {
                            if (userPackagesStateFile3.exists()) {
                                userPackagesStateFile3.delete();
                            }
                            if (backupFile2.exists()) {
                                backupFile2.delete();
                            }
                            reportReadFileError(fileName, exceptionName6, i8);
                            return;
                        }
                        return;
                    } catch (XmlPullParserException e12) {
                        backupFile = backupFile2;
                        i = 6;
                        str4 = str23;
                        str2 = str21;
                        userPackagesStateFile = userPackagesStateFile3;
                        str3 = fileName7;
                        e = e12;
                        exceptionName2 = "XmlPullParserException";
                        settings.mReadMessages.append(str3 + e.toString());
                        PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                        Slog.wtf(str2, str4, e);
                        if (0 != 0) {
                            return;
                        }
                        if (userPackagesStateFile.exists()) {
                            userPackagesStateFile.delete();
                        }
                    } catch (IOException e13) {
                        str5 = str23;
                        backupFile = backupFile2;
                        str7 = str24;
                        userPackagesStateFile = userPackagesStateFile3;
                        str6 = fileName7;
                        e2 = e13;
                        i2 = 6;
                        str2 = str21;
                        exceptionName2 = "IOException";
                        settings.mReadMessages.append(str6 + e2.toString());
                        PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                        Slog.wtf(str2, str5, e2);
                        if (0 != 0) {
                            return;
                        }
                        if (userPackagesStateFile.exists()) {
                            userPackagesStateFile.delete();
                        }
                    } catch (Exception e14) {
                        str8 = fileName7;
                        str9 = str24;
                        backupFile = backupFile2;
                        fileName2 = exceptionName6;
                        str2 = str21;
                        userPackagesStateFile = userPackagesStateFile3;
                        e3 = e14;
                        isSuccess = false;
                        exceptionName2 = e3.getClass().toString();
                        settings.mReadMessages.append(str8 + exceptionName2);
                        PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                        Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                        if (0 == 0) {
                            return;
                        }
                        if (userPackagesStateFile.exists()) {
                            userPackagesStateFile.delete();
                        }
                    } catch (Throwable th6) {
                        backupFile = backupFile2;
                        exceptionName = exceptionName6;
                        th = th6;
                        userPackagesStateFile = userPackagesStateFile3;
                        if (!isSuccess) {
                            if (userPackagesStateFile.exists()) {
                                userPackagesStateFile.delete();
                            }
                            if (backupFile.exists()) {
                                backupFile.delete();
                            }
                            settings.reportReadFileError(fileName, exceptionName, i8);
                        }
                        throw th;
                    }
                } else {
                    str8 = fileName7;
                    str9 = str24;
                    str5 = str23;
                    str10 = str21;
                    backupFile = backupFile2;
                    i3 = i8;
                    exceptionName = exceptionName6;
                    try {
                        userPackagesStateFile2 = userPackagesStateFile3;
                        try {
                            str = new FileInputStream(userPackagesStateFile2);
                            fileName3 = userPackagesStateFile2.getName();
                            str11 = str;
                        } catch (XmlPullParserException e15) {
                            fileName = fileName;
                            userPackagesStateFile = userPackagesStateFile2;
                            i8 = i3;
                            str4 = str5;
                            str2 = str10;
                            str3 = str8;
                            i = 6;
                            e = e15;
                            exceptionName2 = "XmlPullParserException";
                            settings.mReadMessages.append(str3 + e.toString());
                            PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                            Slog.wtf(str2, str4, e);
                            if (0 != 0) {
                            }
                        } catch (IOException e16) {
                            fileName = fileName;
                            userPackagesStateFile = userPackagesStateFile2;
                            i8 = i3;
                            str7 = str9;
                            str2 = str10;
                            str6 = str8;
                            i2 = 6;
                            e2 = e16;
                            exceptionName2 = "IOException";
                            settings.mReadMessages.append(str6 + e2.toString());
                            PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                            Slog.wtf(str2, str5, e2);
                            if (0 != 0) {
                            }
                        } catch (Exception e17) {
                            fileName = fileName;
                            userPackagesStateFile = userPackagesStateFile2;
                            fileName2 = exceptionName;
                            i8 = i3;
                            str2 = str10;
                            e3 = e17;
                            isSuccess = false;
                            exceptionName2 = e3.getClass().toString();
                            settings.mReadMessages.append(str8 + exceptionName2);
                            PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                            Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                            if (0 == 0) {
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            fileName = fileName;
                            userPackagesStateFile = userPackagesStateFile2;
                            i8 = i3;
                            if (!isSuccess) {
                            }
                            throw th;
                        }
                    } catch (XmlPullParserException e18) {
                        fileName = fileName;
                        i8 = i3;
                        str4 = str5;
                        str2 = str10;
                        userPackagesStateFile = userPackagesStateFile3;
                        str3 = str8;
                        i = 6;
                        e = e18;
                        exceptionName2 = "XmlPullParserException";
                        settings.mReadMessages.append(str3 + e.toString());
                        PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                        Slog.wtf(str2, str4, e);
                        if (0 != 0) {
                        }
                    } catch (IOException e19) {
                        fileName = fileName;
                        i8 = i3;
                        str7 = str9;
                        str2 = str10;
                        userPackagesStateFile = userPackagesStateFile3;
                        str6 = str8;
                        i2 = 6;
                        e2 = e19;
                        exceptionName2 = "IOException";
                        settings.mReadMessages.append(str6 + e2.toString());
                        PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                        Slog.wtf(str2, str5, e2);
                        if (0 != 0) {
                        }
                    } catch (Exception e20) {
                        fileName = fileName;
                        fileName2 = exceptionName;
                        i8 = i3;
                        str2 = str10;
                        userPackagesStateFile = userPackagesStateFile3;
                        e3 = e20;
                        isSuccess = false;
                        exceptionName2 = e3.getClass().toString();
                        settings.mReadMessages.append(str8 + exceptionName2);
                        PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                        Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                        if (0 == 0) {
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        fileName = fileName;
                        i8 = i3;
                        userPackagesStateFile = userPackagesStateFile3;
                        if (!isSuccess) {
                        }
                        throw th;
                    }
                }
            } catch (XmlPullParserException e21) {
                backupFile = backupFile2;
                i = 6;
                str4 = str23;
                str2 = str21;
                userPackagesStateFile = userPackagesStateFile3;
                str3 = fileName7;
                e = e21;
                exceptionName2 = "XmlPullParserException";
                settings.mReadMessages.append(str3 + e.toString());
                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                Slog.wtf(str2, str4, e);
                if (0 != 0) {
                }
            } catch (IOException e22) {
                str5 = str23;
                backupFile = backupFile2;
                str7 = str24;
                userPackagesStateFile = userPackagesStateFile3;
                str6 = fileName7;
                e2 = e22;
                i2 = 6;
                str2 = str21;
                exceptionName2 = "IOException";
                settings.mReadMessages.append(str6 + e2.toString());
                PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                Slog.wtf(str2, str5, e2);
                if (0 != 0) {
                }
            } catch (Exception e23) {
                str8 = fileName7;
                str9 = str24;
                backupFile = backupFile2;
                fileName2 = exceptionName6;
                str2 = str21;
                userPackagesStateFile = userPackagesStateFile3;
                e3 = e23;
                isSuccess = false;
                exceptionName2 = e3.getClass().toString();
                settings.mReadMessages.append(str8 + exceptionName2);
                PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                if (0 == 0) {
                }
            } catch (Throwable th9) {
                backupFile = backupFile2;
                exceptionName = exceptionName6;
                th = th9;
                userPackagesStateFile = userPackagesStateFile3;
                if (!isSuccess) {
                }
                throw th;
            }
        } else {
            str8 = fileName7;
            str9 = str24;
            str5 = str23;
            str10 = str21;
            backupFile = backupFile2;
            userPackagesStateFile2 = userPackagesStateFile3;
            i3 = i8;
            exceptionName = exceptionName6;
            fileName3 = fileName;
            str11 = str;
        }
        try {
            parser = Xml.newPullParser();
            parser.setInput(str11, StandardCharsets.UTF_8.name());
            if (type == 2) {
                int maxAppLinkGeneration4 = 0;
                int outerDepth2 = parser.getDepth();
                String str25 = null;
                while (true) {
                    int type4 = parser.next();
                    if (type4 != i4) {
                        if (type4 == 3) {
                            try {
                                if (parser.getDepth() <= outerDepth2) {
                                    fileName4 = fileName3;
                                    str12 = str11;
                                    maxAppLinkGeneration = maxAppLinkGeneration4;
                                    userPackagesStateFile = userPackagesStateFile2;
                                    exceptionName3 = exceptionName;
                                    i8 = i3;
                                    str2 = str10;
                                }
                            } catch (XmlPullParserException e24) {
                                e = e24;
                                fileName = fileName3;
                                str2 = str10;
                                userPackagesStateFile = userPackagesStateFile2;
                                i8 = i3;
                                str4 = str5;
                                str3 = str8;
                                i = 6;
                                exceptionName2 = "XmlPullParserException";
                                settings.mReadMessages.append(str3 + e.toString());
                                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                Slog.wtf(str2, str4, e);
                                if (0 != 0) {
                                }
                            } catch (IOException e25) {
                                e2 = e25;
                                fileName = fileName3;
                                str2 = str10;
                                userPackagesStateFile = userPackagesStateFile2;
                                i8 = i3;
                                str7 = str9;
                                str6 = str8;
                                i2 = 6;
                                exceptionName2 = "IOException";
                                settings.mReadMessages.append(str6 + e2.toString());
                                PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                                Slog.wtf(str2, str5, e2);
                                if (0 != 0) {
                                }
                            } catch (Exception e26) {
                                e3 = e26;
                                fileName = fileName3;
                                str = str11;
                                str2 = str10;
                                userPackagesStateFile = userPackagesStateFile2;
                                fileName2 = exceptionName;
                                i8 = i3;
                                isSuccess = false;
                                exceptionName2 = e3.getClass().toString();
                                settings.mReadMessages.append(str8 + exceptionName2);
                                PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                                Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                                if (0 == 0) {
                                }
                            } catch (Throwable th10) {
                            }
                        }
                        if (type4 == 3 || type4 == i9) {
                            i3 = i3;
                            parser = parser;
                            str10 = str10;
                            outerDepth2 = outerDepth2;
                            str11 = str11;
                            i9 = i9;
                            str25 = str25;
                            i4 = 1;
                            userPackagesStateFile2 = userPackagesStateFile2;
                            str20 = str20;
                            exceptionName = exceptionName;
                            maxAppLinkGeneration4 = maxAppLinkGeneration4;
                            fileName3 = fileName3;
                        } else {
                            try {
                                String tagName = parser.getName();
                                if (tagName.equals(str20)) {
                                    try {
                                        String name = parser.getAttributeValue(str25, ATTR_NAME);
                                        PackageSetting ps = settings.mPackages.get(name);
                                        if (ps == null) {
                                            Slog.w(str10, "No package known for stopped package " + name);
                                            XmlUtils.skipCurrentTag(parser);
                                            str10 = str10;
                                            i9 = 4;
                                            str25 = null;
                                            i4 = 1;
                                        } else {
                                            str2 = str10;
                                            try {
                                                long ceDataInode = XmlUtils.readLongAttribute(parser, ATTR_CE_DATA_INODE, 0);
                                                boolean installed = XmlUtils.readBooleanAttribute(parser, ATTR_INSTALLED, true);
                                                str19 = str2;
                                                boolean stopped = XmlUtils.readBooleanAttribute(parser, ATTR_STOPPED, false);
                                                outerDepth = outerDepth2;
                                                boolean notLaunched = XmlUtils.readBooleanAttribute(parser, ATTR_NOT_LAUNCHED, false);
                                                String blockedStr = parser.getAttributeValue(null, ATTR_BLOCKED);
                                                boolean hidden = blockedStr == null ? false : Boolean.parseBoolean(blockedStr);
                                                String hiddenStr = parser.getAttributeValue(null, ATTR_HIDDEN);
                                                boolean hidden2 = hiddenStr == null ? hidden : Boolean.parseBoolean(hiddenStr);
                                                int distractionFlags = XmlUtils.readIntAttribute(parser, ATTR_DISTRACTION_FLAGS, 0);
                                                boolean suspended = XmlUtils.readBooleanAttribute(parser, ATTR_SUSPENDED, false);
                                                String suspendingPackage = parser.getAttributeValue(null, ATTR_SUSPENDING_PACKAGE);
                                                String dialogMessage = parser.getAttributeValue(null, ATTR_SUSPEND_DIALOG_MESSAGE);
                                                String suspendingPackage2 = (!suspended || suspendingPackage != null) ? suspendingPackage : PackageManagerService.PLATFORM_PACKAGE_NAME;
                                                boolean blockUninstall = XmlUtils.readBooleanAttribute(parser, ATTR_BLOCK_UNINSTALL, false);
                                                boolean instantApp = XmlUtils.readBooleanAttribute(parser, ATTR_INSTANT_APP, false);
                                                boolean virtualPreload = XmlUtils.readBooleanAttribute(parser, ATTR_VIRTUAL_PRELOAD, false);
                                                int enabled = XmlUtils.readIntAttribute(parser, ATTR_ENABLED, 0);
                                                String enabledCaller = parser.getAttributeValue(null, ATTR_ENABLED_CALLER);
                                                String harmfulAppWarning = parser.getAttributeValue(null, ATTR_HARMFUL_APP_WARNING);
                                                int verifState = XmlUtils.readIntAttribute(parser, ATTR_DOMAIN_VERIFICATON_STATE, 0);
                                                int linkGeneration = XmlUtils.readIntAttribute(parser, ATTR_APP_LINK_GENERATION, 0);
                                                if (linkGeneration > maxAppLinkGeneration4) {
                                                    maxAppLinkGeneration3 = linkGeneration;
                                                } else {
                                                    maxAppLinkGeneration3 = maxAppLinkGeneration4;
                                                }
                                                try {
                                                    int installReason = XmlUtils.readIntAttribute(parser, ATTR_INSTALL_REASON, 0);
                                                    SuspendDialogInfo suspendDialogInfo = null;
                                                    int type5 = parser.getDepth();
                                                    PersistableBundle suspendedAppExtras = null;
                                                    PersistableBundle suspendedLauncherExtras = null;
                                                    ArraySet<String> enabledComponents = null;
                                                    ArraySet<String> disabledComponents = null;
                                                    while (true) {
                                                        int type6 = parser.next();
                                                        if (type6 != 1) {
                                                            if (type6 == 3) {
                                                                try {
                                                                    packageDepth2 = type5;
                                                                    if (parser.getDepth() <= packageDepth2) {
                                                                        packageDepth = type6;
                                                                    }
                                                                } catch (XmlPullParserException e27) {
                                                                    e = e27;
                                                                    i8 = i3;
                                                                    userPackagesStateFile = userPackagesStateFile2;
                                                                    fileName = fileName3;
                                                                    str4 = str5;
                                                                    str3 = str8;
                                                                    str2 = str19;
                                                                    i = 6;
                                                                    exceptionName2 = "XmlPullParserException";
                                                                    settings.mReadMessages.append(str3 + e.toString());
                                                                    PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                                                    Slog.wtf(str2, str4, e);
                                                                    if (0 != 0) {
                                                                    }
                                                                } catch (IOException e28) {
                                                                    e2 = e28;
                                                                    i8 = i3;
                                                                    userPackagesStateFile = userPackagesStateFile2;
                                                                    fileName = fileName3;
                                                                    str7 = str9;
                                                                    str6 = str8;
                                                                    str2 = str19;
                                                                    i2 = 6;
                                                                    exceptionName2 = "IOException";
                                                                    settings.mReadMessages.append(str6 + e2.toString());
                                                                    PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                                                                    Slog.wtf(str2, str5, e2);
                                                                    if (0 != 0) {
                                                                    }
                                                                } catch (Exception e29) {
                                                                    e3 = e29;
                                                                    fileName2 = exceptionName;
                                                                    i8 = i3;
                                                                    userPackagesStateFile = userPackagesStateFile2;
                                                                    str = str11;
                                                                    fileName = fileName3;
                                                                    str2 = str19;
                                                                    isSuccess = false;
                                                                    exceptionName2 = e3.getClass().toString();
                                                                    settings.mReadMessages.append(str8 + exceptionName2);
                                                                    PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                                                                    Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                                                                    if (0 == 0) {
                                                                    }
                                                                } catch (Throwable th11) {
                                                                    th = th11;
                                                                    i8 = i3;
                                                                    userPackagesStateFile = userPackagesStateFile2;
                                                                    fileName = fileName3;
                                                                    if (!isSuccess) {
                                                                    }
                                                                    throw th;
                                                                }
                                                            } else {
                                                                packageDepth2 = type5;
                                                            }
                                                            if (type6 == 3) {
                                                                type2 = type6;
                                                                packageDepth3 = packageDepth2;
                                                            } else if (type6 == 4) {
                                                                type2 = type6;
                                                                packageDepth3 = packageDepth2;
                                                            } else {
                                                                String name2 = parser.getName();
                                                                switch (name2.hashCode()) {
                                                                    case -2027581689:
                                                                        type3 = type6;
                                                                        if (name2.equals(TAG_DISABLED_COMPONENTS)) {
                                                                            c2 = 1;
                                                                            break;
                                                                        }
                                                                        c2 = 65535;
                                                                        break;
                                                                    case -1963032286:
                                                                        type3 = type6;
                                                                        if (name2.equals(TAG_ENABLED_COMPONENTS)) {
                                                                            c2 = 0;
                                                                            break;
                                                                        }
                                                                        c2 = 65535;
                                                                        break;
                                                                    case -1592287551:
                                                                        type3 = type6;
                                                                        if (name2.equals(TAG_SUSPENDED_APP_EXTRAS)) {
                                                                            c2 = 2;
                                                                            break;
                                                                        }
                                                                        c2 = 65535;
                                                                        break;
                                                                    case -1422791362:
                                                                        type3 = type6;
                                                                        if (name2.equals(TAG_SUSPENDED_LAUNCHER_EXTRAS)) {
                                                                            c2 = 3;
                                                                            break;
                                                                        }
                                                                        c2 = 65535;
                                                                        break;
                                                                    case 1660896545:
                                                                        type3 = type6;
                                                                        if (name2.equals(TAG_SUSPENDED_DIALOG_INFO)) {
                                                                            c2 = 4;
                                                                            break;
                                                                        }
                                                                        c2 = 65535;
                                                                        break;
                                                                    default:
                                                                        type3 = type6;
                                                                        c2 = 65535;
                                                                        break;
                                                                }
                                                                if (c2 == 0) {
                                                                    packageDepth4 = packageDepth2;
                                                                    enabledComponents = settings.readComponentsLPr(parser);
                                                                } else if (c2 == 1) {
                                                                    packageDepth4 = packageDepth2;
                                                                    disabledComponents = settings.readComponentsLPr(parser);
                                                                } else if (c2 == 2) {
                                                                    packageDepth4 = packageDepth2;
                                                                    suspendedAppExtras = PersistableBundle.restoreFromXml(parser);
                                                                } else if (c2 == 3) {
                                                                    packageDepth4 = packageDepth2;
                                                                    suspendedLauncherExtras = PersistableBundle.restoreFromXml(parser);
                                                                } else if (c2 != 4) {
                                                                    StringBuilder sb = new StringBuilder();
                                                                    packageDepth4 = packageDepth2;
                                                                    sb.append("Unknown tag ");
                                                                    sb.append(parser.getName());
                                                                    sb.append(" under tag ");
                                                                    sb.append(str20);
                                                                    Slog.wtf(TAG, sb.toString());
                                                                } else {
                                                                    packageDepth4 = packageDepth2;
                                                                    suspendDialogInfo = SuspendDialogInfo.restoreFromXml(parser);
                                                                }
                                                                enabled = enabled;
                                                                type5 = packageDepth4;
                                                            }
                                                            enabled = enabled;
                                                            type5 = packageDepth3;
                                                        } else {
                                                            packageDepth = type6;
                                                        }
                                                    }
                                                    if (suspendDialogInfo == null && !TextUtils.isEmpty(dialogMessage)) {
                                                        suspendDialogInfo = new SuspendDialogInfo.Builder().setMessage(dialogMessage).build();
                                                    }
                                                    if (blockUninstall) {
                                                        i7 = 1;
                                                        settings.setBlockUninstallLPw(i3, name, true);
                                                    } else {
                                                        i7 = 1;
                                                    }
                                                    str16 = str11;
                                                    fileName5 = fileName3;
                                                    userPackagesStateFile = userPackagesStateFile2;
                                                    exceptionName5 = exceptionName;
                                                    str17 = str20;
                                                    i5 = i7;
                                                    c = 2;
                                                    i6 = 4;
                                                    str18 = null;
                                                    try {
                                                        ps.setUserState(userId, ceDataInode, enabled, installed, stopped, notLaunched, hidden2, distractionFlags, suspended, suspendingPackage2, suspendDialogInfo, suspendedAppExtras, suspendedLauncherExtras, instantApp, virtualPreload, enabledCaller, enabledComponents, disabledComponents, verifState, linkGeneration, installReason, harmfulAppWarning);
                                                        settings = this;
                                                        i8 = userId;
                                                        str2 = str19;
                                                        maxAppLinkGeneration2 = maxAppLinkGeneration3;
                                                        parser2 = parser;
                                                    } catch (XmlPullParserException e30) {
                                                        settings = this;
                                                        i8 = userId;
                                                        e = e30;
                                                        str4 = str5;
                                                        str3 = str8;
                                                        str2 = str19;
                                                        fileName = fileName5;
                                                        i = 6;
                                                        exceptionName2 = "XmlPullParserException";
                                                        settings.mReadMessages.append(str3 + e.toString());
                                                        PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                                        Slog.wtf(str2, str4, e);
                                                        if (0 != 0) {
                                                        }
                                                    } catch (IOException e31) {
                                                        settings = this;
                                                        i8 = userId;
                                                        e2 = e31;
                                                        str7 = str9;
                                                        str6 = str8;
                                                        str2 = str19;
                                                        fileName = fileName5;
                                                        i2 = 6;
                                                        exceptionName2 = "IOException";
                                                        settings.mReadMessages.append(str6 + e2.toString());
                                                        PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                                                        Slog.wtf(str2, str5, e2);
                                                        if (0 != 0) {
                                                        }
                                                    } catch (Exception e32) {
                                                        e4 = e32;
                                                        settings = this;
                                                        i8 = userId;
                                                        e3 = e4;
                                                        str2 = str19;
                                                        str = str16;
                                                        fileName = fileName5;
                                                        fileName2 = exceptionName5;
                                                        isSuccess = false;
                                                        exceptionName2 = e3.getClass().toString();
                                                        settings.mReadMessages.append(str8 + exceptionName2);
                                                        PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                                                        Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                                                        if (0 == 0) {
                                                        }
                                                    } catch (Throwable th12) {
                                                    }
                                                } catch (XmlPullParserException e33) {
                                                    userPackagesStateFile = userPackagesStateFile2;
                                                    settings = this;
                                                    i8 = userId;
                                                    e = e33;
                                                    str4 = str5;
                                                    str3 = str8;
                                                    str2 = str19;
                                                    fileName = fileName3;
                                                    i = 6;
                                                    exceptionName2 = "XmlPullParserException";
                                                    settings.mReadMessages.append(str3 + e.toString());
                                                    PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                                    Slog.wtf(str2, str4, e);
                                                    if (0 != 0) {
                                                    }
                                                } catch (IOException e34) {
                                                    userPackagesStateFile = userPackagesStateFile2;
                                                    settings = this;
                                                    i8 = userId;
                                                    e2 = e34;
                                                    str7 = str9;
                                                    str6 = str8;
                                                    str2 = str19;
                                                    fileName = fileName3;
                                                    i2 = 6;
                                                    exceptionName2 = "IOException";
                                                    settings.mReadMessages.append(str6 + e2.toString());
                                                    PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                                                    Slog.wtf(str2, str5, e2);
                                                    if (0 != 0) {
                                                    }
                                                } catch (Exception e35) {
                                                    userPackagesStateFile = userPackagesStateFile2;
                                                    settings = this;
                                                    i8 = userId;
                                                    e3 = e35;
                                                    fileName2 = exceptionName;
                                                    str2 = str19;
                                                    str = str11;
                                                    fileName = fileName3;
                                                    isSuccess = false;
                                                    exceptionName2 = e3.getClass().toString();
                                                    settings.mReadMessages.append(str8 + exceptionName2);
                                                    PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                                                    Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                                                    if (0 == 0) {
                                                    }
                                                } catch (Throwable th13) {
                                                    userPackagesStateFile = userPackagesStateFile2;
                                                    settings = this;
                                                    i8 = userId;
                                                    th = th13;
                                                    fileName = fileName3;
                                                    if (!isSuccess) {
                                                    }
                                                    throw th;
                                                }
                                            } catch (XmlPullParserException e36) {
                                                userPackagesStateFile = userPackagesStateFile2;
                                                FileInputStream fileInputStream4 = str11;
                                                String str26 = fileName3;
                                                settings = this;
                                                i8 = userId;
                                                e = e36;
                                                str4 = str5;
                                                str3 = str8;
                                                str2 = str19;
                                                fileName = str26;
                                                i = 6;
                                                exceptionName2 = "XmlPullParserException";
                                                settings.mReadMessages.append(str3 + e.toString());
                                                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                                Slog.wtf(str2, str4, e);
                                                if (0 != 0) {
                                                }
                                            } catch (IOException e37) {
                                                userPackagesStateFile = userPackagesStateFile2;
                                                FileInputStream fileInputStream5 = str11;
                                                String str27 = fileName3;
                                                settings = this;
                                                i8 = userId;
                                                e2 = e37;
                                                str7 = str9;
                                                str6 = str8;
                                                str2 = str19;
                                                fileName = str27;
                                                i2 = 6;
                                                exceptionName2 = "IOException";
                                                settings.mReadMessages.append(str6 + e2.toString());
                                                PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                                                Slog.wtf(str2, str5, e2);
                                                if (0 != 0) {
                                                }
                                            } catch (Exception e38) {
                                                userPackagesStateFile = userPackagesStateFile2;
                                                FileInputStream fileInputStream6 = str11;
                                                String str28 = fileName3;
                                                settings = this;
                                                i8 = userId;
                                                e3 = e38;
                                                fileName2 = exceptionName;
                                                str2 = str19;
                                                str = fileInputStream6;
                                                fileName = str28;
                                                isSuccess = false;
                                                exceptionName2 = e3.getClass().toString();
                                                settings.mReadMessages.append(str8 + exceptionName2);
                                                PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                                                Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                                                if (0 == 0) {
                                                }
                                            } catch (Throwable th14) {
                                            }
                                        }
                                    } catch (XmlPullParserException e39) {
                                        str13 = fileName3;
                                        fileInputStream = str11;
                                        userPackagesStateFile = userPackagesStateFile2;
                                        settings = this;
                                        i8 = userId;
                                        e = e39;
                                        str4 = str5;
                                        str2 = str10;
                                        str3 = str8;
                                        fileName = str13;
                                        i = 6;
                                        exceptionName2 = "XmlPullParserException";
                                        settings.mReadMessages.append(str3 + e.toString());
                                        PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                        Slog.wtf(str2, str4, e);
                                        if (0 != 0) {
                                        }
                                    } catch (IOException e40) {
                                        str14 = fileName3;
                                        fileInputStream2 = str11;
                                        userPackagesStateFile = userPackagesStateFile2;
                                        settings = this;
                                        i8 = userId;
                                        e2 = e40;
                                        str7 = str9;
                                        str2 = str10;
                                        str6 = str8;
                                        fileName = str14;
                                        i2 = 6;
                                        exceptionName2 = "IOException";
                                        settings.mReadMessages.append(str6 + e2.toString());
                                        PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                                        Slog.wtf(str2, str5, e2);
                                        if (0 != 0) {
                                        }
                                    } catch (Exception e41) {
                                        str15 = fileName3;
                                        fileInputStream3 = str11;
                                        userPackagesStateFile = userPackagesStateFile2;
                                        settings = this;
                                        i8 = userId;
                                        e3 = e41;
                                        fileName2 = exceptionName;
                                        str2 = str10;
                                        str = fileInputStream3;
                                        fileName = str15;
                                        isSuccess = false;
                                        exceptionName2 = e3.getClass().toString();
                                        settings.mReadMessages.append(str8 + exceptionName2);
                                        PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                                        Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                                        if (0 == 0) {
                                        }
                                    } catch (Throwable th15) {
                                    }
                                } else {
                                    fileName5 = fileName3;
                                    str16 = str11;
                                    maxAppLinkGeneration2 = maxAppLinkGeneration4;
                                    i6 = i9;
                                    str18 = str25;
                                    outerDepth = outerDepth2;
                                    userPackagesStateFile = userPackagesStateFile2;
                                    exceptionName5 = exceptionName;
                                    str17 = str20;
                                    str19 = str10;
                                    c = 2;
                                    i5 = 1;
                                    if (tagName.equals("preferred-activities")) {
                                        i8 = userId;
                                        parser2 = parser;
                                        settings = this;
                                        try {
                                            settings.readPreferredActivitiesLPw(parser2, i8);
                                            str2 = str19;
                                        } catch (XmlPullParserException e42) {
                                            e = e42;
                                            str4 = str5;
                                            str3 = str8;
                                            str2 = str19;
                                            fileName = fileName5;
                                            i = 6;
                                            exceptionName2 = "XmlPullParserException";
                                            settings.mReadMessages.append(str3 + e.toString());
                                            PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                            Slog.wtf(str2, str4, e);
                                            if (0 != 0) {
                                            }
                                        } catch (IOException e43) {
                                            e2 = e43;
                                            str7 = str9;
                                            str6 = str8;
                                            str2 = str19;
                                            fileName = fileName5;
                                            i2 = 6;
                                            exceptionName2 = "IOException";
                                            settings.mReadMessages.append(str6 + e2.toString());
                                            PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                                            Slog.wtf(str2, str5, e2);
                                            if (0 != 0) {
                                            }
                                        } catch (Exception e44) {
                                            e4 = e44;
                                            e3 = e4;
                                            str2 = str19;
                                            str = str16;
                                            fileName = fileName5;
                                            fileName2 = exceptionName5;
                                            isSuccess = false;
                                            exceptionName2 = e3.getClass().toString();
                                            settings.mReadMessages.append(str8 + exceptionName2);
                                            PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                                            Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                                            if (0 == 0) {
                                            }
                                        } catch (Throwable th16) {
                                        }
                                    } else {
                                        i8 = userId;
                                        parser2 = parser;
                                        settings = this;
                                        if (tagName.equals(TAG_PERSISTENT_PREFERRED_ACTIVITIES)) {
                                            settings.readPersistentPreferredActivitiesLPw(parser2, i8);
                                            str2 = str19;
                                        } else if (tagName.equals(TAG_CROSS_PROFILE_INTENT_FILTERS)) {
                                            settings.readCrossProfileIntentFiltersLPw(parser2, i8);
                                            str2 = str19;
                                        } else if (tagName.equals(TAG_DEFAULT_APPS)) {
                                            settings.readDefaultAppsLPw(parser2, i8);
                                            str2 = str19;
                                        } else if (tagName.equals(TAG_BLOCK_UNINSTALL_PACKAGES)) {
                                            settings.readBlockUninstallPackagesLPw(parser2, i8);
                                            str2 = str19;
                                        } else {
                                            str2 = str19;
                                            Slog.w(str2, "Unknown element under <stopped-packages>: " + parser2.getName());
                                            XmlUtils.skipCurrentTag(parser2);
                                        }
                                    }
                                }
                                i3 = i8;
                                parser = parser2;
                                str10 = str2;
                                outerDepth2 = outerDepth;
                                str11 = str16;
                                i9 = i6;
                                str25 = str18;
                                i4 = i5;
                                userPackagesStateFile2 = userPackagesStateFile;
                                str20 = str17;
                                exceptionName = exceptionName5;
                                maxAppLinkGeneration4 = maxAppLinkGeneration2;
                                fileName3 = fileName5;
                            } catch (XmlPullParserException e45) {
                                str13 = fileName3;
                                fileInputStream = str11;
                                userPackagesStateFile = userPackagesStateFile2;
                                i8 = i3;
                                str2 = str10;
                                e = e45;
                                str4 = str5;
                                str3 = str8;
                                fileName = str13;
                                i = 6;
                                exceptionName2 = "XmlPullParserException";
                                settings.mReadMessages.append(str3 + e.toString());
                                PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                                Slog.wtf(str2, str4, e);
                                if (0 != 0) {
                                }
                            } catch (IOException e46) {
                                str14 = fileName3;
                                fileInputStream2 = str11;
                                userPackagesStateFile = userPackagesStateFile2;
                                i8 = i3;
                                str2 = str10;
                                e2 = e46;
                                str7 = str9;
                                str6 = str8;
                                fileName = str14;
                                i2 = 6;
                                exceptionName2 = "IOException";
                                settings.mReadMessages.append(str6 + e2.toString());
                                PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                                Slog.wtf(str2, str5, e2);
                                if (0 != 0) {
                                }
                            } catch (Exception e47) {
                                str15 = fileName3;
                                fileInputStream3 = str11;
                                userPackagesStateFile = userPackagesStateFile2;
                                i8 = i3;
                                str2 = str10;
                                e3 = e47;
                                fileName2 = exceptionName;
                                str = fileInputStream3;
                                fileName = str15;
                                isSuccess = false;
                                exceptionName2 = e3.getClass().toString();
                                settings.mReadMessages.append(str8 + exceptionName2);
                                PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                                Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                                if (0 == 0) {
                                }
                            } catch (Throwable th17) {
                                Throwable th18 = th17;
                                String str29 = fileName3;
                                userPackagesStateFile = userPackagesStateFile2;
                                i8 = i3;
                                th = th18;
                                fileName = str29;
                                if (!isSuccess) {
                                }
                                throw th;
                            }
                        }
                    } else {
                        fileName4 = fileName3;
                        str12 = str11;
                        maxAppLinkGeneration = maxAppLinkGeneration4;
                        userPackagesStateFile = userPackagesStateFile2;
                        exceptionName3 = exceptionName;
                        i8 = i3;
                        str2 = str10;
                    }
                }
                try {
                    str12.close();
                    settings.mNextAppLinkGeneration.put(i8, maxAppLinkGeneration + 1);
                    if (1 == 0) {
                        if (userPackagesStateFile.exists()) {
                            userPackagesStateFile.delete();
                        }
                        if (backupFile.exists()) {
                            backupFile.delete();
                        }
                        exceptionName4 = exceptionName3;
                        settings.reportReadFileError(fileName4, exceptionName4, i8);
                    } else {
                        exceptionName4 = exceptionName3;
                    }
                    return;
                } catch (XmlPullParserException e48) {
                    fileName = fileName4;
                    e = e48;
                    str4 = str5;
                    str3 = str8;
                    i = 6;
                    exceptionName2 = "XmlPullParserException";
                    settings.mReadMessages.append(str3 + e.toString());
                    PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                    Slog.wtf(str2, str4, e);
                    if (0 != 0) {
                    }
                } catch (IOException e49) {
                    fileName = fileName4;
                    e2 = e49;
                    str7 = str9;
                    str6 = str8;
                    i2 = 6;
                    exceptionName2 = "IOException";
                    settings.mReadMessages.append(str6 + e2.toString());
                    PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                    Slog.wtf(str2, str5, e2);
                    if (0 != 0) {
                    }
                } catch (Exception e50) {
                    fileName = fileName4;
                    fileName2 = exceptionName3;
                    e3 = e50;
                    str = str12;
                    isSuccess = false;
                    exceptionName2 = e3.getClass().toString();
                    settings.mReadMessages.append(str8 + exceptionName2);
                    PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                    Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                    if (0 == 0) {
                    }
                } catch (Throwable th19) {
                    fileName = fileName4;
                    exceptionName = exceptionName3;
                    th = th19;
                    if (!isSuccess) {
                    }
                    throw th;
                }
            } else {
                userPackagesStateFile = userPackagesStateFile2;
                i8 = i3;
                str2 = str10;
                fileName = fileName3;
                fileName2 = exceptionName;
                try {
                    settings.mReadMessages.append("No start tag found in package restrictions file\n");
                    PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
                    throw new XmlPullParserException("package restrictions file can't find start tag");
                } catch (XmlPullParserException e51) {
                    e = e51;
                    str4 = str5;
                    str3 = str8;
                    i = 6;
                    exceptionName2 = "XmlPullParserException";
                    settings.mReadMessages.append(str3 + e.toString());
                    PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
                    Slog.wtf(str2, str4, e);
                    if (0 != 0) {
                    }
                } catch (IOException e52) {
                    e2 = e52;
                    str7 = str9;
                    str6 = str8;
                    i2 = 6;
                    exceptionName2 = "IOException";
                    settings.mReadMessages.append(str6 + e2.toString());
                    PackageManagerService.reportSettingsProblem(i2, str7 + e2);
                    Slog.wtf(str2, str5, e2);
                    if (0 != 0) {
                    }
                } catch (Exception e53) {
                    e3 = e53;
                    str = str11;
                    isSuccess = false;
                    exceptionName2 = e3.getClass().toString();
                    settings.mReadMessages.append(str8 + exceptionName2);
                    PackageManagerService.reportSettingsProblem(6, str9 + exceptionName2);
                    Slog.wtf(str2, "Error reading package restrictions " + exceptionName2);
                    if (0 == 0) {
                    }
                } catch (Throwable th20) {
                    exceptionName = fileName2;
                    th = th20;
                    if (!isSuccess) {
                    }
                    throw th;
                }
            }
        } catch (XmlPullParserException e54) {
            fileName = fileName3;
            userPackagesStateFile = userPackagesStateFile2;
            i8 = i3;
            str2 = str10;
            e = e54;
            str4 = str5;
            str3 = str8;
            i = 6;
            exceptionName2 = "XmlPullParserException";
            settings.mReadMessages.append(str3 + e.toString());
            PackageManagerService.reportSettingsProblem(i, "Error reading stopped packages: " + e);
            Slog.wtf(str2, str4, e);
            if (0 != 0) {
            }
        } catch (IOException e55) {
            fileName = fileName3;
            userPackagesStateFile = userPackagesStateFile2;
            i8 = i3;
            str2 = str10;
            e2 = e55;
            str7 = str9;
            str6 = str8;
            i2 = 6;
            exceptionName2 = "IOException";
            settings.mReadMessages.append(str6 + e2.toString());
            PackageManagerService.reportSettingsProblem(i2, str7 + e2);
            Slog.wtf(str2, str5, e2);
            if (0 != 0) {
            }
        } catch (Exception e56) {
        } catch (Throwable th21) {
        }
        while (true) {
            type = parser.next();
            i4 = 1;
            if (type == 2 || type == 1) {
                break;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportReadFileError(String fileName, String exceptionName, int userId) {
        Slog.w("PackageManager", " Error reading file:" + fileName + " of user:" + userId + ", occur to " + exceptionName);
        HwPackageManagerServiceUtils.reportPmsParseFileException(fileName, exceptionName, userId, null);
    }

    /* access modifiers changed from: package-private */
    public void setBlockUninstallLPw(int userId, String packageName, boolean blockUninstall) {
        ArraySet<String> packages = this.mBlockUninstallPackages.get(userId);
        if (blockUninstall) {
            if (packages == null) {
                packages = new ArraySet<>();
                this.mBlockUninstallPackages.put(userId, packages);
            }
            packages.add(packageName);
        } else if (packages != null) {
            packages.remove(packageName);
            if (packages.isEmpty()) {
                this.mBlockUninstallPackages.remove(userId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getBlockUninstallLPr(int userId, String packageName) {
        ArraySet<String> packages = this.mBlockUninstallPackages.get(userId);
        if (packages == null) {
            return false;
        }
        return packages.contains(packageName);
    }

    private ArraySet<String> readComponentsLPr(XmlPullParser parser) throws IOException, XmlPullParserException {
        String componentName;
        ArraySet<String> components = null;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                break;
            } else if (!(type == 3 || type == 4 || !parser.getName().equals(TAG_ITEM) || (componentName = parser.getAttributeValue(null, ATTR_NAME)) == null)) {
                if (components == null) {
                    components = new ArraySet<>();
                }
                components.add(componentName);
            }
        }
        return components;
    }

    /* access modifiers changed from: package-private */
    public void writePreferredActivitiesLPr(XmlSerializer serializer, int userId, boolean full) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, "preferred-activities");
        PreferredIntentResolver pir = this.mPreferredActivities.get(userId);
        if (pir != null) {
            for (PreferredActivity pa : pir.filterSet()) {
                serializer.startTag(null, TAG_ITEM);
                pa.writeToXml(serializer, full);
                serializer.endTag(null, TAG_ITEM);
            }
        }
        serializer.endTag(null, "preferred-activities");
    }

    /* access modifiers changed from: package-private */
    public void writePersistentPreferredActivitiesLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_PERSISTENT_PREFERRED_ACTIVITIES);
        PersistentPreferredIntentResolver ppir = this.mPersistentPreferredActivities.get(userId);
        if (ppir != null) {
            for (PersistentPreferredActivity ppa : ppir.filterSet()) {
                serializer.startTag(null, TAG_ITEM);
                ppa.writeToXml(serializer);
                serializer.endTag(null, TAG_ITEM);
            }
        }
        serializer.endTag(null, TAG_PERSISTENT_PREFERRED_ACTIVITIES);
    }

    /* access modifiers changed from: package-private */
    public void writeCrossProfileIntentFiltersLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_CROSS_PROFILE_INTENT_FILTERS);
        CrossProfileIntentResolver cpir = this.mCrossProfileIntentResolvers.get(userId);
        if (cpir != null) {
            for (CrossProfileIntentFilter cpif : cpir.filterSet()) {
                serializer.startTag(null, TAG_ITEM);
                cpif.writeToXml(serializer);
                serializer.endTag(null, TAG_ITEM);
            }
        }
        serializer.endTag(null, TAG_CROSS_PROFILE_INTENT_FILTERS);
    }

    /* access modifiers changed from: package-private */
    public void writeDomainVerificationsLPr(XmlSerializer serializer, IntentFilterVerificationInfo verificationInfo) throws IllegalArgumentException, IllegalStateException, IOException {
        if (verificationInfo != null && verificationInfo.getPackageName() != null) {
            serializer.startTag(null, TAG_DOMAIN_VERIFICATION);
            verificationInfo.writeToXml(serializer);
            if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                Slog.d(TAG, "Wrote domain verification for package: " + verificationInfo.getPackageName());
            }
            serializer.endTag(null, TAG_DOMAIN_VERIFICATION);
        }
    }

    /* access modifiers changed from: package-private */
    public void writeAllDomainVerificationsLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_ALL_INTENT_FILTER_VERIFICATION);
        int N = this.mPackages.size();
        for (int i = 0; i < N; i++) {
            IntentFilterVerificationInfo ivi = this.mPackages.valueAt(i).getIntentFilterVerificationInfo();
            if (ivi != null) {
                writeDomainVerificationsLPr(serializer, ivi);
            }
        }
        serializer.endTag(null, TAG_ALL_INTENT_FILTER_VERIFICATION);
    }

    /* access modifiers changed from: package-private */
    public void readAllDomainVerificationsLPr(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        this.mRestoredIntentFilterVerifications.clear();
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_DOMAIN_VERIFICATION)) {
                    IntentFilterVerificationInfo ivi = new IntentFilterVerificationInfo(parser);
                    String pkgName = ivi.getPackageName();
                    PackageSetting ps = this.mPackages.get(pkgName);
                    if (ps != null) {
                        ps.setIntentFilterVerificationInfo(ivi);
                        if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                            Slog.d(TAG, "Restored IVI for existing app " + pkgName + " status=" + ivi.getStatusString());
                        }
                    } else {
                        this.mRestoredIntentFilterVerifications.put(pkgName, ivi);
                        if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                            Slog.d(TAG, "Restored IVI for pending app " + pkgName + " status=" + ivi.getStatusString());
                        }
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <all-intent-filter-verification>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeDefaultAppsLPr(XmlSerializer serializer, int userId) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(null, TAG_DEFAULT_APPS);
        String defaultBrowser = this.mDefaultBrowserApp.get(userId);
        if (!TextUtils.isEmpty(defaultBrowser)) {
            serializer.startTag(null, TAG_DEFAULT_BROWSER);
            serializer.attribute(null, ATTR_PACKAGE_NAME, defaultBrowser);
            serializer.endTag(null, TAG_DEFAULT_BROWSER);
        }
        serializer.endTag(null, TAG_DEFAULT_APPS);
    }

    /* access modifiers changed from: package-private */
    public void writeBlockUninstallPackagesLPr(XmlSerializer serializer, int userId) throws IOException {
        ArraySet<String> packages = this.mBlockUninstallPackages.get(userId);
        if (packages != null) {
            serializer.startTag(null, TAG_BLOCK_UNINSTALL_PACKAGES);
            for (int i = 0; i < packages.size(); i++) {
                serializer.startTag(null, TAG_BLOCK_UNINSTALL);
                serializer.attribute(null, ATTR_PACKAGE_NAME, packages.valueAt(i));
                serializer.endTag(null, TAG_BLOCK_UNINSTALL);
            }
            serializer.endTag(null, TAG_BLOCK_UNINSTALL_PACKAGES);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x0304  */
    /* JADX WARNING: Removed duplicated region for block: B:139:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    public void writePackageRestrictionsLPr(int userId) {
        String str;
        File userPackagesStateFile;
        IOException e;
        FileOutputStream fstr;
        String str2;
        Settings settings = this;
        int i = userId;
        String str3 = TAG_SUSPENDED_DIALOG_INFO;
        long startTime = SystemClock.uptimeMillis();
        File userPackagesStateFile2 = getUserPackagesStateFile(userId);
        PackageUserState ustate = getUserPackagesStateBackupFile(userId);
        new File(userPackagesStateFile2.getParent()).mkdirs();
        if (userPackagesStateFile2.exists()) {
            if (ustate.exists()) {
                userPackagesStateFile2.delete();
                Slog.w("PackageManager", "Preserving older stopped packages backup");
            } else if (!userPackagesStateFile2.renameTo(ustate)) {
                Slog.wtf("PackageManager", "Unable to backup user packages state file, current changes will be lost at reboot");
                return;
            }
        }
        try {
            str = "PackageManager";
            FileOutputStream fstr2 = new FileOutputStream(userPackagesStateFile2);
            try {
                BufferedOutputStream str4 = new BufferedOutputStream(fstr2);
                XmlSerializer serializer = new FastXmlSerializer();
                try {
                    serializer.setOutput(str4, StandardCharsets.UTF_8.name());
                    userPackagesStateFile = userPackagesStateFile2;
                    try {
                        serializer.startDocument(null, true);
                        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                        serializer.startTag(null, TAG_PACKAGE_RESTRICTIONS);
                        Iterator<PackageSetting> it = settings.mPackages.values().iterator();
                        while (it.hasNext()) {
                            try {
                                PackageSetting pkg = it.next();
                                PackageUserState ustate2 = pkg.readUserState(i);
                                try {
                                    serializer.startTag(null, "pkg");
                                    serializer.attribute(null, ATTR_NAME, pkg.name);
                                    if (ustate2.ceDataInode != 0) {
                                        XmlUtils.writeLongAttribute(serializer, ATTR_CE_DATA_INODE, ustate2.ceDataInode);
                                    }
                                    if (!ustate2.installed) {
                                        serializer.attribute(null, ATTR_INSTALLED, "false");
                                    }
                                    if (ustate2.stopped) {
                                        serializer.attribute(null, ATTR_STOPPED, "true");
                                    }
                                    if (ustate2.notLaunched) {
                                        serializer.attribute(null, ATTR_NOT_LAUNCHED, "true");
                                    }
                                    if (ustate2.hidden) {
                                        serializer.attribute(null, ATTR_HIDDEN, "true");
                                    }
                                    if (ustate2.distractionFlags != 0) {
                                        fstr = fstr2;
                                        serializer.attribute(null, ATTR_DISTRACTION_FLAGS, Integer.toString(ustate2.distractionFlags));
                                    } else {
                                        fstr = fstr2;
                                    }
                                    if (ustate2.suspended) {
                                        serializer.attribute(null, ATTR_SUSPENDED, "true");
                                        if (ustate2.suspendingPackage != null) {
                                            serializer.attribute(null, ATTR_SUSPENDING_PACKAGE, ustate2.suspendingPackage);
                                        }
                                        if (ustate2.dialogInfo != null) {
                                            serializer.startTag(null, str3);
                                            ustate2.dialogInfo.saveToXml(serializer);
                                            serializer.endTag(null, str3);
                                        }
                                        if (ustate2.suspendedAppExtras != null) {
                                            serializer.startTag(null, TAG_SUSPENDED_APP_EXTRAS);
                                            try {
                                                ustate2.suspendedAppExtras.saveToXml(serializer);
                                                str2 = str3;
                                            } catch (XmlPullParserException xmle) {
                                                StringBuilder sb = new StringBuilder();
                                                str2 = str3;
                                                sb.append("Exception while trying to write suspendedAppExtras for ");
                                                sb.append(pkg);
                                                sb.append(". Will be lost on reboot");
                                                Slog.wtf(TAG, sb.toString(), xmle);
                                            }
                                            serializer.endTag(null, TAG_SUSPENDED_APP_EXTRAS);
                                        } else {
                                            str2 = str3;
                                        }
                                        if (ustate2.suspendedLauncherExtras != null) {
                                            serializer.startTag(null, TAG_SUSPENDED_LAUNCHER_EXTRAS);
                                            try {
                                                ustate2.suspendedLauncherExtras.saveToXml(serializer);
                                            } catch (XmlPullParserException xmle2) {
                                                Slog.wtf(TAG, "Exception while trying to write suspendedLauncherExtras for " + pkg + ". Will be lost on reboot", xmle2);
                                            }
                                            serializer.endTag(null, TAG_SUSPENDED_LAUNCHER_EXTRAS);
                                        }
                                    } else {
                                        str2 = str3;
                                    }
                                    if (ustate2.instantApp) {
                                        serializer.attribute(null, ATTR_INSTANT_APP, "true");
                                    }
                                    if (ustate2.virtualPreload) {
                                        serializer.attribute(null, ATTR_VIRTUAL_PRELOAD, "true");
                                    }
                                    if (ustate2.enabled != 0) {
                                        serializer.attribute(null, ATTR_ENABLED, Integer.toString(ustate2.enabled));
                                        if (ustate2.lastDisableAppCaller != null) {
                                            serializer.attribute(null, ATTR_ENABLED_CALLER, ustate2.lastDisableAppCaller);
                                        }
                                    }
                                    if (ustate2.domainVerificationStatus != 0) {
                                        XmlUtils.writeIntAttribute(serializer, ATTR_DOMAIN_VERIFICATON_STATE, ustate2.domainVerificationStatus);
                                    }
                                    if (ustate2.appLinkGeneration != 0) {
                                        XmlUtils.writeIntAttribute(serializer, ATTR_APP_LINK_GENERATION, ustate2.appLinkGeneration);
                                    }
                                    if (ustate2.installReason != 0) {
                                        serializer.attribute(null, ATTR_INSTALL_REASON, Integer.toString(ustate2.installReason));
                                    }
                                    if (ustate2.harmfulAppWarning != null) {
                                        serializer.attribute(null, ATTR_HARMFUL_APP_WARNING, ustate2.harmfulAppWarning);
                                    }
                                    if (!ArrayUtils.isEmpty(ustate2.enabledComponents)) {
                                        serializer.startTag(null, TAG_ENABLED_COMPONENTS);
                                        Iterator it2 = ustate2.enabledComponents.iterator();
                                        while (it2.hasNext()) {
                                            serializer.startTag(null, TAG_ITEM);
                                            serializer.attribute(null, ATTR_NAME, (String) it2.next());
                                            serializer.endTag(null, TAG_ITEM);
                                        }
                                        serializer.endTag(null, TAG_ENABLED_COMPONENTS);
                                    }
                                    if (!ArrayUtils.isEmpty(ustate2.disabledComponents)) {
                                        serializer.startTag(null, TAG_DISABLED_COMPONENTS);
                                        Iterator it3 = ustate2.disabledComponents.iterator();
                                        while (it3.hasNext()) {
                                            serializer.startTag(null, TAG_ITEM);
                                            serializer.attribute(null, ATTR_NAME, (String) it3.next());
                                            serializer.endTag(null, TAG_ITEM);
                                        }
                                        serializer.endTag(null, TAG_DISABLED_COMPONENTS);
                                    }
                                    serializer.endTag(null, "pkg");
                                    i = userId;
                                    fstr2 = fstr;
                                    it = it;
                                    ustate = ustate;
                                    str3 = str2;
                                } catch (IOException e2) {
                                    e = e2;
                                    settings = this;
                                    Slog.wtf(str, "Unable to write package manager user packages state,  current changes will be lost at reboot", e);
                                    if (!userPackagesStateFile.exists()) {
                                        return;
                                    }
                                    return;
                                }
                            } catch (IOException e3) {
                                e = e3;
                                settings = this;
                                Slog.wtf(str, "Unable to write package manager user packages state,  current changes will be lost at reboot", e);
                                if (!userPackagesStateFile.exists()) {
                                }
                            }
                        }
                        settings = this;
                    } catch (IOException e4) {
                        e = e4;
                        Slog.wtf(str, "Unable to write package manager user packages state,  current changes will be lost at reboot", e);
                        if (!userPackagesStateFile.exists() && !userPackagesStateFile.delete()) {
                            Log.i(str, "Failed to clean up mangled file: " + settings.mStoppedPackagesFilename);
                            return;
                        }
                        return;
                    }
                } catch (IOException e5) {
                    e = e5;
                    userPackagesStateFile = userPackagesStateFile2;
                    Slog.wtf(str, "Unable to write package manager user packages state,  current changes will be lost at reboot", e);
                    if (!userPackagesStateFile.exists()) {
                    }
                }
                try {
                    settings.writePreferredActivitiesLPr(serializer, userId, true);
                    settings.writePersistentPreferredActivitiesLPr(serializer, userId);
                    settings.writeCrossProfileIntentFiltersLPr(serializer, userId);
                    settings.writeDefaultAppsLPr(serializer, userId);
                    settings.writeBlockUninstallPackagesLPr(serializer, userId);
                    serializer.endTag(null, TAG_PACKAGE_RESTRICTIONS);
                    serializer.endDocument();
                    str4.flush();
                    FileUtils.sync(fstr2);
                    str4.close();
                    ustate.delete();
                    FileUtils.setPermissions(userPackagesStateFile.toString(), 432, -1, -1);
                    EventLogTags.writeCommitSysConfigFile("package-user-" + userId, SystemClock.uptimeMillis() - startTime);
                } catch (IOException e6) {
                    e = e6;
                    Slog.wtf(str, "Unable to write package manager user packages state,  current changes will be lost at reboot", e);
                    if (!userPackagesStateFile.exists()) {
                    }
                }
            } catch (IOException e7) {
                e = e7;
                userPackagesStateFile = userPackagesStateFile2;
                Slog.wtf(str, "Unable to write package manager user packages state,  current changes will be lost at reboot", e);
                if (!userPackagesStateFile.exists()) {
                }
            }
        } catch (IOException e8) {
            e = e8;
            userPackagesStateFile = userPackagesStateFile2;
            str = "PackageManager";
            Slog.wtf(str, "Unable to write package manager user packages state,  current changes will be lost at reboot", e);
            if (!userPackagesStateFile.exists()) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readInstallPermissionsLPr(XmlPullParser parser, PermissionsState permissionsState) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            boolean granted = true;
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (type != 3) {
                if (type != 4) {
                    if (parser.getName().equals(TAG_ITEM)) {
                        String name = parser.getAttributeValue(null, ATTR_NAME);
                        BasePermission bp = this.mPermissions.getPermission(name);
                        if (bp == null) {
                            Slog.w("PackageManager", "Unknown permission: " + name);
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            String grantedStr = parser.getAttributeValue(null, ATTR_GRANTED);
                            int flags = 0;
                            if (grantedStr != null && !Boolean.parseBoolean(grantedStr)) {
                                granted = false;
                            }
                            String flagsStr = parser.getAttributeValue(null, ATTR_FLAGS);
                            if (flagsStr != null) {
                                flags = Integer.parseInt(flagsStr, 16);
                            }
                            if (granted) {
                                if (permissionsState.grantInstallPermission(bp) == -1) {
                                    Slog.w("PackageManager", "Permission already added: " + name);
                                    XmlUtils.skipCurrentTag(parser);
                                } else {
                                    permissionsState.updatePermissionFlags(bp, -1, 130047, flags);
                                }
                            } else if (permissionsState.revokeInstallPermission(bp) == -1) {
                                Slog.w("PackageManager", "Permission already added: " + name);
                                XmlUtils.skipCurrentTag(parser);
                            } else {
                                permissionsState.updatePermissionFlags(bp, -1, 130047, flags);
                            }
                        }
                    } else {
                        Slog.w("PackageManager", "Unknown element under <permissions>: " + parser.getName());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writePermissionsLPr(XmlSerializer serializer, List<PermissionsState.PermissionState> permissionStates) throws IOException {
        if (!permissionStates.isEmpty()) {
            serializer.startTag(null, TAG_PERMISSIONS);
            for (PermissionsState.PermissionState permissionState : permissionStates) {
                serializer.startTag(null, TAG_ITEM);
                serializer.attribute(null, ATTR_NAME, permissionState.getName());
                serializer.attribute(null, ATTR_GRANTED, String.valueOf(permissionState.isGranted()));
                serializer.attribute(null, ATTR_FLAGS, Integer.toHexString(permissionState.getFlags()));
                serializer.endTag(null, TAG_ITEM);
            }
            serializer.endTag(null, TAG_PERMISSIONS);
        }
    }

    /* access modifiers changed from: package-private */
    public void writeChildPackagesLPw(XmlSerializer serializer, List<String> childPackageNames) throws IOException {
        if (childPackageNames != null) {
            int childCount = childPackageNames.size();
            for (int i = 0; i < childCount; i++) {
                serializer.startTag(null, TAG_CHILD_PACKAGE);
                serializer.attribute(null, ATTR_NAME, childPackageNames.get(i));
                serializer.endTag(null, TAG_CHILD_PACKAGE);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readUsesStaticLibLPw(XmlPullParser parser, PackageSetting outPs) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String libName = parser.getAttributeValue(null, ATTR_NAME);
                long libVersion = -1;
                try {
                    libVersion = Long.parseLong(parser.getAttributeValue(null, "version"));
                } catch (NumberFormatException e) {
                }
                if (libName != null && libVersion >= 0) {
                    outPs.usesStaticLibraries = (String[]) ArrayUtils.appendElement(String.class, outPs.usesStaticLibraries, libName);
                    outPs.usesStaticLibrariesVersions = ArrayUtils.appendLong(outPs.usesStaticLibrariesVersions, libVersion);
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeUsesStaticLibLPw(XmlSerializer serializer, String[] usesStaticLibraries, long[] usesStaticLibraryVersions) throws IOException {
        if (!(ArrayUtils.isEmpty(usesStaticLibraries) || ArrayUtils.isEmpty(usesStaticLibraryVersions) || usesStaticLibraries.length != usesStaticLibraryVersions.length)) {
            int libCount = usesStaticLibraries.length;
            for (int i = 0; i < libCount; i++) {
                String libName = usesStaticLibraries[i];
                long libVersion = usesStaticLibraryVersions[i];
                serializer.startTag(null, TAG_USES_STATIC_LIB);
                serializer.attribute(null, ATTR_NAME, libName);
                serializer.attribute(null, "version", Long.toString(libVersion));
                serializer.endTag(null, TAG_USES_STATIC_LIB);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readStoppedLPw() {
        int type;
        int i;
        int i2;
        FileInputStream str = null;
        String fileName = "";
        int i3 = 4;
        if (this.mBackupStoppedPackagesFilename.exists()) {
            try {
                str = new FileInputStream(this.mBackupStoppedPackagesFilename);
                fileName = this.mBackupStoppedPackagesFilename.getName();
                this.mReadMessages.append("Reading from backup stopped packages file\n");
                PackageManagerService.reportSettingsProblem(4, "Need to read from backup stopped packages file");
                if (this.mSettingsFilename.exists()) {
                    Slog.w("PackageManager", "Cleaning up stopped packages file " + this.mStoppedPackagesFilename);
                    this.mStoppedPackagesFilename.delete();
                }
            } catch (IOException e) {
            }
        }
        boolean isSuccess = true;
        String exceptionName = "";
        int i4 = 0;
        if (str == null) {
            try {
                if (!this.mStoppedPackagesFilename.exists()) {
                    this.mReadMessages.append("No stopped packages file found\n");
                    PackageManagerService.reportSettingsProblem(4, "No stopped packages file file; assuming all started");
                    for (PackageSetting pkg : this.mPackages.values()) {
                        pkg.setStopped(false, 0);
                        pkg.setNotLaunched(false, 0);
                    }
                    if (1 == 0) {
                        reportReadFileError(fileName, exceptionName, 0);
                        return;
                    }
                    return;
                }
                str = new FileInputStream(this.mStoppedPackagesFilename);
                fileName = this.mStoppedPackagesFilename.getName();
            } catch (XmlPullParserException e2) {
                exceptionName = "XmlPullParserException";
                StringBuilder sb = this.mReadMessages;
                sb.append("Error reading: " + e2.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading stopped packages: " + e2);
                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e2);
                if (0 != 0) {
                    return;
                }
            } catch (IOException e3) {
                exceptionName = "IOException";
                StringBuilder sb2 = this.mReadMessages;
                sb2.append("Error reading: " + e3.toString());
                PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e3);
                Slog.wtf("PackageManager", "Error reading package manager stopped packages", e3);
                if (0 != 0) {
                    return;
                }
            } catch (Exception e4) {
                isSuccess = false;
                exceptionName = e4.getClass().toString();
                StringBuilder sb3 = this.mReadMessages;
                sb3.append("Error reading: " + exceptionName);
                PackageManagerService.reportSettingsProblem(6, "Error reading stopped packages file: " + exceptionName);
                if (0 != 0) {
                    return;
                }
            } catch (Throwable th) {
                if (!isSuccess) {
                    reportReadFileError(fileName, exceptionName, 0);
                }
                throw th;
            }
        }
        XmlPullParser parser = Xml.newPullParser();
        String str2 = null;
        parser.setInput(str, null);
        do {
            type = parser.next();
            i = 1;
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type == 2) {
            int outerDepth = parser.getDepth();
            while (true) {
                int type2 = parser.next();
                if (type2 == i || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                    break;
                }
                if (type2 == 3) {
                    i2 = 1;
                } else if (type2 == i3) {
                    i2 = 1;
                } else if (parser.getName().equals("pkg")) {
                    String name = parser.getAttributeValue(str2, ATTR_NAME);
                    PackageSetting ps = this.mPackages.get(name);
                    if (ps != null) {
                        ps.setStopped(true, i4);
                        if ("1".equals(parser.getAttributeValue(null, ATTR_NOT_LAUNCHED))) {
                            i2 = 1;
                            ps.setNotLaunched(true, 0);
                        } else {
                            i2 = 1;
                        }
                    } else {
                        i2 = 1;
                        Slog.w("PackageManager", "No package known for stopped package " + name);
                    }
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    i2 = 1;
                    Slog.w("PackageManager", "Unknown element under <stopped-packages>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
                i = i2;
                i3 = 4;
                i4 = 0;
                str2 = null;
            }
            str.close();
            if (1 != 0) {
                return;
            }
            reportReadFileError(fileName, exceptionName, 0);
            return;
        }
        this.mReadMessages.append("No start tag found in stopped packages file\n");
        PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager stopped packages");
        throw new XmlPullParserException("stopped packages xml file can't find start tag");
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x027e  */
    /* JADX WARNING: Removed duplicated region for block: B:81:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    public void writeLPr() {
        String str;
        IOException e;
        long startTime = SystemClock.uptimeMillis();
        if (this.mSettingsFilename.exists()) {
            if (this.mBackupSettingsFilename.exists()) {
                this.mSettingsFilename.delete();
                Slog.w("PackageManager", "Preserving older settings backup");
            } else if (!this.mSettingsFilename.renameTo(this.mBackupSettingsFilename)) {
                Slog.wtf("PackageManager", "Unable to backup package manager settings,  current changes will be lost at reboot");
                return;
            }
        }
        this.mPastSignatures.clear();
        try {
            FileOutputStream fstr = new FileOutputStream(this.mSettingsFilename);
            BufferedOutputStream str2 = new BufferedOutputStream(fstr);
            XmlSerializer serializer = new FastXmlSerializer();
            str = "PackageManager";
            try {
                serializer.setOutput(str2, StandardCharsets.UTF_8.name());
            } catch (IOException e2) {
                e = e2;
                Slog.wtf(str, "Unable to write package manager settings, current changes will be lost at reboot", e);
                if (this.mSettingsFilename.exists()) {
                    return;
                }
            }
            try {
                serializer.startDocument(null, true);
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startTag(null, "packages");
                int i = 0;
                while (i < this.mVersion.size()) {
                    VersionInfo ver = this.mVersion.valueAt(i);
                    serializer.startTag(null, "version");
                    XmlUtils.writeStringAttribute(serializer, ATTR_VOLUME_UUID, this.mVersion.keyAt(i));
                    XmlUtils.writeIntAttribute(serializer, ATTR_SDK_VERSION, ver.sdkVersion);
                    XmlUtils.writeIntAttribute(serializer, ATTR_DATABASE_VERSION, ver.databaseVersion);
                    XmlUtils.writeIntAttribute(serializer, ATTR_EMUI_VERSION, ver.emuiVersion);
                    XmlUtils.writeStringAttribute(serializer, ATTR_FINGERPRINT, ver.fingerprint);
                    XmlUtils.writeStringAttribute(serializer, ATTR_HWFINGERPRINT, ver.hwFingerprint);
                    XmlUtils.writeStringAttribute(serializer, ATTR_FINGERPRINTEX, ver.fingerprintEx);
                    serializer.endTag(null, "version");
                    i++;
                    fstr = fstr;
                }
                if (this.mVerifierDeviceIdentity != null) {
                    serializer.startTag(null, "verifier");
                    serializer.attribute(null, "device", this.mVerifierDeviceIdentity.toString());
                    serializer.endTag(null, "verifier");
                }
                if (this.mReadExternalStorageEnforced != null) {
                    serializer.startTag(null, TAG_READ_EXTERNAL_STORAGE);
                    serializer.attribute(null, ATTR_ENFORCEMENT, this.mReadExternalStorageEnforced.booleanValue() ? "1" : "0");
                    serializer.endTag(null, TAG_READ_EXTERNAL_STORAGE);
                }
                serializer.startTag(null, "permission-trees");
                this.mPermissions.writePermissionTrees(serializer);
                serializer.endTag(null, "permission-trees");
                serializer.startTag(null, "permissions");
                this.mPermissions.writePermissions(serializer);
                serializer.endTag(null, "permissions");
                for (PackageSetting pkg : this.mPackages.values()) {
                    writePackageLPr(serializer, pkg);
                }
                for (PackageSetting pkg2 : this.mDisabledSysPackages.values()) {
                    writeDisabledSysPackageLPr(serializer, pkg2);
                }
                for (SharedUserSetting usr : this.mSharedUsers.values()) {
                    serializer.startTag(null, TAG_SHARED_USER);
                    serializer.attribute(null, ATTR_NAME, usr.name);
                    serializer.attribute(null, "userId", Integer.toString(usr.userId));
                    usr.signatures.writeXml(serializer, "sigs", this.mPastSignatures);
                    writePermissionsLPr(serializer, usr.getPermissionsState().getInstallPermissionStates());
                    serializer.endTag(null, TAG_SHARED_USER);
                }
                if (this.mRenamedPackages.size() > 0) {
                    for (Map.Entry<String, String> e3 : this.mRenamedPackages.entrySet()) {
                        serializer.startTag(null, "renamed-package");
                        serializer.attribute(null, "new", e3.getKey());
                        serializer.attribute(null, "old", e3.getValue());
                        serializer.endTag(null, "renamed-package");
                    }
                }
                int numIVIs = this.mRestoredIntentFilterVerifications.size();
                if (numIVIs > 0) {
                    if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                        Slog.i(TAG, "Writing restored-ivi entries to packages.xml");
                    }
                    serializer.startTag(null, "restored-ivi");
                    for (int i2 = 0; i2 < numIVIs; i2++) {
                        writeDomainVerificationsLPr(serializer, this.mRestoredIntentFilterVerifications.valueAt(i2));
                    }
                    serializer.endTag(null, "restored-ivi");
                } else if (PackageManagerService.DEBUG_DOMAIN_VERIFICATION) {
                    Slog.i(TAG, "  no restored IVI entries to write");
                }
                this.mKeySetManagerService.writeKeySetManagerServiceLPr(serializer);
                serializer.endTag(null, "packages");
                serializer.endDocument();
                str2.flush();
                FileUtils.sync(fstr);
                str2.close();
                this.mBackupSettingsFilename.delete();
                FileUtils.setPermissions(this.mSettingsFilename.toString(), 432, -1, -1);
                writeKernelMappingLPr();
                writePackageListLPr();
                writeAllUsersPackageRestrictionsLPr();
                writeAllRuntimePermissionsLPr();
                EventLogTags.writeCommitSysConfigFile("package", SystemClock.uptimeMillis() - startTime);
            } catch (IOException e4) {
                e = e4;
                Slog.wtf(str, "Unable to write package manager settings, current changes will be lost at reboot", e);
                if (this.mSettingsFilename.exists()) {
                }
            }
        } catch (IOException e5) {
            e = e5;
            str = "PackageManager";
            Slog.wtf(str, "Unable to write package manager settings, current changes will be lost at reboot", e);
            if (!(this.mSettingsFilename.exists() || this.mSettingsFilename.delete())) {
                Slog.wtf(str, "Failed to clean up mangled file: " + this.mSettingsFilename);
            }
        }
    }

    private void writeKernelRemoveUserLPr(int userId) {
        File file = this.mKernelMappingFilename;
        if (file != null) {
            writeIntToFile(new File(file, "remove_userid"), userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void writeKernelMappingLPr() {
        File file = this.mKernelMappingFilename;
        if (file != null) {
            String[] known = file.list();
            ArraySet<String> knownSet = new ArraySet<>(known.length);
            for (String name : known) {
                knownSet.add(name);
            }
            for (PackageSetting ps : this.mPackages.values()) {
                knownSet.remove(ps.name);
                writeKernelMappingLPr(ps);
            }
            for (int i = 0; i < knownSet.size(); i++) {
                String name2 = knownSet.valueAt(i);
                this.mKernelMapping.remove(name2);
                new File(this.mKernelMappingFilename, name2).delete();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeKernelMappingLPr(PackageSetting ps) {
        if (this.mKernelMappingFilename != null && ps != null && ps.name != null) {
            writeKernelMappingLPr(ps.name, ps.appId, ps.getNotInstalledUserIds());
        }
    }

    /* access modifiers changed from: package-private */
    public void writeKernelMappingLPr(String name, int appId, int[] excludedUserIds) {
        KernelPackageState cur = this.mKernelMapping.get(name);
        boolean userIdsChanged = false;
        boolean firstTime = cur == null;
        if (firstTime || !Arrays.equals(excludedUserIds, cur.excludedUserIds)) {
            userIdsChanged = true;
        }
        File dir = new File(this.mKernelMappingFilename, name);
        if (firstTime) {
            dir.mkdir();
            cur = new KernelPackageState();
            this.mKernelMapping.put(name, cur);
        }
        if (cur.appId != appId) {
            writeIntToFile(new File(dir, "appid"), appId);
        }
        if (userIdsChanged) {
            for (int i = 0; i < excludedUserIds.length; i++) {
                if (cur.excludedUserIds == null || !ArrayUtils.contains(cur.excludedUserIds, excludedUserIds[i])) {
                    writeIntToFile(new File(dir, "excluded_userids"), excludedUserIds[i]);
                }
            }
            if (cur.excludedUserIds != null) {
                for (int i2 = 0; i2 < cur.excludedUserIds.length; i2++) {
                    if (!ArrayUtils.contains(excludedUserIds, cur.excludedUserIds[i2])) {
                        writeIntToFile(new File(dir, "clear_userid"), cur.excludedUserIds[i2]);
                    }
                }
            }
            cur.excludedUserIds = excludedUserIds;
        }
    }

    private void writeIntToFile(File file, int value) {
        try {
            FileUtils.bytesToFile(file.getAbsolutePath(), Integer.toString(value).getBytes(StandardCharsets.US_ASCII));
        } catch (IOException e) {
            Slog.w(TAG, "Couldn't write " + value + " to " + file.getAbsolutePath());
        }
    }

    /* access modifiers changed from: package-private */
    public void writePackageListLPr() {
        writePackageListLPr(-1);
    }

    /* access modifiers changed from: package-private */
    public void writePackageListLPr(int creatingUserId) {
        String ctx = SELinux.fileSelabelLookup(this.mPackageListFilename.getAbsolutePath());
        if (ctx == null) {
            Slog.wtf(TAG, "Failed to get SELinux context for " + this.mPackageListFilename.getAbsolutePath());
        }
        if (!SELinux.setFSCreateContext(ctx)) {
            Slog.wtf(TAG, "Failed to set packages.list SELinux context");
        }
        try {
            writePackageListLPrInternal(creatingUserId);
        } finally {
            SELinux.setFSCreateContext((String) null);
        }
    }

    private void writePackageListLPrInternal(int creatingUserId) {
        Exception e;
        List<UserInfo> users;
        String str;
        String str2 = " ";
        List<UserInfo> users2 = getUsers(UserManagerService.getInstance(), true);
        int[] userIds = new int[users2.size()];
        for (int i = 0; i < userIds.length; i++) {
            userIds[i] = users2.get(i).id;
        }
        if (creatingUserId != -1) {
            userIds = ArrayUtils.appendInt(userIds, creatingUserId);
        }
        JournaledFile journal = new JournaledFile(this.mPackageListFilename, new File(this.mPackageListFilename.getAbsolutePath() + ".tmp"));
        BufferedWriter writer = null;
        try {
            FileOutputStream fstr = new FileOutputStream(journal.chooseForWrite());
            writer = new BufferedWriter(new OutputStreamWriter(fstr, Charset.defaultCharset()));
            FileUtils.setPermissions(fstr.getFD(), 416, 1000, 1032);
            StringBuilder sb = new StringBuilder();
            for (PackageSetting pkg : this.mPackages.values()) {
                if (pkg.pkg == null || pkg.pkg.applicationInfo == null) {
                    users = users2;
                    str = str2;
                } else if (pkg.pkg.applicationInfo.dataDir == null) {
                    users = users2;
                    str = str2;
                } else {
                    ApplicationInfo ai = pkg.pkg.applicationInfo;
                    String dataPath = ai.dataDir;
                    boolean isDebug = (ai.flags & 2) != 0;
                    int[] gids = pkg.getPermissionsState().computeGids(userIds);
                    try {
                        if (dataPath.indexOf(32) >= 0) {
                            users2 = users2;
                        } else {
                            sb.setLength(0);
                            sb.append(ai.packageName);
                            sb.append(str2);
                            sb.append(ai.uid);
                            sb.append(isDebug ? " 1 " : " 0 ");
                            sb.append(dataPath);
                            sb.append(str2);
                            sb.append(ai.seInfo);
                            sb.append(str2);
                            if (gids == null || gids.length <= 0) {
                                sb.append("none");
                            } else {
                                sb.append(gids[0]);
                                int i2 = 1;
                                while (i2 < gids.length) {
                                    sb.append(",");
                                    sb.append(gids[i2]);
                                    i2++;
                                    isDebug = isDebug;
                                }
                            }
                            sb.append(str2);
                            sb.append(ai.isProfileableByShell() ? "1" : "0");
                            sb.append(str2);
                            sb.append(String.valueOf(ai.longVersionCode));
                            sb.append("\n");
                            writer.append((CharSequence) sb);
                            str2 = str2;
                            users2 = users2;
                        }
                    } catch (Exception e2) {
                        e = e2;
                        Slog.wtf(TAG, "Failed to write packages.list", e);
                        IoUtils.closeQuietly(writer);
                        journal.rollback();
                    }
                }
                if (!PackageManagerService.PLATFORM_PACKAGE_NAME.equals(pkg.name)) {
                    Slog.w(TAG, "Skipping " + pkg + " due to missing metadata");
                    str2 = str;
                    users2 = users;
                } else {
                    str2 = str;
                    users2 = users;
                }
            }
            writer.flush();
            FileUtils.sync(fstr);
            writer.close();
            journal.commit();
        } catch (Exception e3) {
            e = e3;
            Slog.wtf(TAG, "Failed to write packages.list", e);
            IoUtils.closeQuietly(writer);
            journal.rollback();
        }
    }

    /* access modifiers changed from: package-private */
    public void writeDisablePluginsLPw(XmlSerializer serializer, String[] disablePlugins) throws IOException {
        if (!ArrayUtils.isEmpty(disablePlugins)) {
            serializer.startTag(null, TAG_DISABLE_PLUGIN);
            for (String pluginName : disablePlugins) {
                serializer.startTag(null, TAG_ITEM);
                serializer.attribute(null, ATTR_NAME, pluginName);
                serializer.attribute(null, ATTR_FLAGS, "0");
                serializer.endTag(null, TAG_ITEM);
            }
            serializer.endTag(null, TAG_DISABLE_PLUGIN);
        }
    }

    /* access modifiers changed from: package-private */
    public void readDisablePluginsLPw(XmlPullParser parser, PackageSetting outPs) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    outPs.disablePlugins = (String[]) ArrayUtils.appendElement(String.class, outPs.usesStaticLibraries, parser.getAttributeValue(null, ATTR_NAME));
                } else {
                    Slog.w("PackageManager", "Unknown element under disable-plugins: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeDisabledSysPackageLPr(XmlSerializer serializer, PackageSetting pkg) throws IOException {
        serializer.startTag(null, "updated-package");
        serializer.attribute(null, ATTR_NAME, pkg.name);
        if (pkg.realName != null) {
            serializer.attribute(null, "realName", pkg.realName);
        }
        serializer.attribute(null, "codePath", pkg.codePathString);
        serializer.attribute(null, "ft", Long.toHexString(pkg.timeStamp));
        serializer.attribute(null, "it", Long.toHexString(pkg.firstInstallTime));
        serializer.attribute(null, "ut", Long.toHexString(pkg.lastUpdateTime));
        serializer.attribute(null, "version", String.valueOf(pkg.versionCode));
        if (!pkg.resourcePathString.equals(pkg.codePathString)) {
            serializer.attribute(null, "resourcePath", pkg.resourcePathString);
        }
        if (pkg.legacyNativeLibraryPathString != null) {
            serializer.attribute(null, "nativeLibraryPath", pkg.legacyNativeLibraryPathString);
        }
        if (pkg.primaryCpuAbiString != null) {
            serializer.attribute(null, "primaryCpuAbi", pkg.primaryCpuAbiString);
        }
        if (pkg.secondaryCpuAbiString != null) {
            serializer.attribute(null, "secondaryCpuAbi", pkg.secondaryCpuAbiString);
        }
        if (pkg.cpuAbiOverrideString != null) {
            serializer.attribute(null, "cpuAbiOverride", pkg.cpuAbiOverrideString);
        }
        if (pkg.sharedUser == null) {
            serializer.attribute(null, "userId", Integer.toString(pkg.appId));
        } else {
            serializer.attribute(null, "sharedUserId", Integer.toString(pkg.appId));
        }
        if (pkg.parentPackageName != null) {
            serializer.attribute(null, "parentPackageName", pkg.parentPackageName);
        }
        serializer.attribute(null, "hwExtraFlags", Integer.toString(pkg.hw_extra_flags));
        writeChildPackagesLPw(serializer, pkg.childPackageNames);
        writeUsesStaticLibLPw(serializer, pkg.usesStaticLibraries, pkg.usesStaticLibrariesVersions);
        writeDisablePluginsLPw(serializer, pkg.disablePlugins);
        if (pkg.sharedUser == null) {
            writePermissionsLPr(serializer, pkg.getPermissionsState().getInstallPermissionStates());
        }
        serializer.endTag(null, "updated-package");
    }

    /* access modifiers changed from: package-private */
    public void writePackageLPr(XmlSerializer serializer, PackageSetting pkg) throws IOException {
        serializer.startTag(null, "package");
        serializer.attribute(null, ATTR_NAME, pkg.name);
        if (pkg.realName != null) {
            serializer.attribute(null, "realName", pkg.realName);
        }
        serializer.attribute(null, "codePath", pkg.codePathString);
        if (!pkg.resourcePathString.equals(pkg.codePathString)) {
            serializer.attribute(null, "resourcePath", pkg.resourcePathString);
        }
        if (pkg.legacyNativeLibraryPathString != null) {
            serializer.attribute(null, "nativeLibraryPath", pkg.legacyNativeLibraryPathString);
        }
        if (pkg.primaryCpuAbiString != null) {
            serializer.attribute(null, "primaryCpuAbi", pkg.primaryCpuAbiString);
        }
        if (pkg.secondaryCpuAbiString != null) {
            serializer.attribute(null, "secondaryCpuAbi", pkg.secondaryCpuAbiString);
        }
        if (pkg.cpuAbiOverrideString != null) {
            serializer.attribute(null, "cpuAbiOverride", pkg.cpuAbiOverrideString);
        }
        serializer.attribute(null, "publicFlags", Integer.toString(pkg.pkgFlags));
        serializer.attribute(null, "privateFlags", Integer.toString(pkg.pkgPrivateFlags));
        serializer.attribute(null, "ft", Long.toHexString(pkg.timeStamp));
        serializer.attribute(null, "it", Long.toHexString(pkg.firstInstallTime));
        serializer.attribute(null, "ut", Long.toHexString(pkg.lastUpdateTime));
        serializer.attribute(null, "version", String.valueOf(pkg.versionCode));
        if (pkg.sharedUser == null) {
            serializer.attribute(null, "userId", Integer.toString(pkg.appId));
        } else {
            serializer.attribute(null, "sharedUserId", Integer.toString(pkg.appId));
        }
        if (pkg.uidError) {
            serializer.attribute(null, "uidError", "true");
        }
        if (pkg.installerPackageName != null) {
            serializer.attribute(null, "installer", pkg.installerPackageName);
        }
        if (pkg.maxAspectRatio > 0.0f) {
            serializer.attribute(null, "maxAspectRatio", Float.toString(pkg.maxAspectRatio));
        }
        if (pkg.minAspectRatio > 0.0f) {
            serializer.attribute(null, "minAspectRatio", Float.toString(pkg.minAspectRatio));
        }
        serializer.attribute(null, "appUseNotchMode", Integer.toString(pkg.appUseNotchMode));
        serializer.attribute(null, "appUseSideMode", Integer.toString(pkg.appUseSideMode));
        serializer.attribute(null, "hwExtraFlags", Integer.toString(pkg.hw_extra_flags));
        if (pkg.isOrphaned) {
            serializer.attribute(null, "isOrphaned", "true");
        }
        if (pkg.volumeUuid != null) {
            serializer.attribute(null, ATTR_VOLUME_UUID, pkg.volumeUuid);
        }
        if (pkg.categoryHint != -1) {
            serializer.attribute(null, "categoryHint", Integer.toString(pkg.categoryHint));
        }
        if (pkg.parentPackageName != null) {
            serializer.attribute(null, "parentPackageName", pkg.parentPackageName);
        }
        if (pkg.updateAvailable) {
            serializer.attribute(null, "updateAvailable", "true");
        }
        if (pkg.forceDarkMode >= 0) {
            serializer.attribute(null, "forceDarkMode", Integer.toString(pkg.forceDarkMode));
        }
        writeChildPackagesLPw(serializer, pkg.childPackageNames);
        writeUsesStaticLibLPw(serializer, pkg.usesStaticLibraries, pkg.usesStaticLibrariesVersions);
        if (pkg.pkg == null || !pkg.pkg.mRealSigningDetails.hasSignatures()) {
            pkg.signatures.writeXml(serializer, "sigs", this.mPastSignatures);
        } else {
            new PackageSignatures(pkg.pkg.mRealSigningDetails).writeXml(serializer, "sigs", this.mPastSignatures);
        }
        writePermissionsLPr(serializer, pkg.getPermissionsState().getInstallPermissionStates());
        writeSigningKeySetLPr(serializer, pkg.keySetData);
        writeUpgradeKeySetsLPr(serializer, pkg.keySetData);
        writeKeySetAliasesLPr(serializer, pkg.keySetData);
        writeDomainVerificationsLPr(serializer, pkg.verificationInfo);
        serializer.endTag(null, "package");
    }

    /* access modifiers changed from: package-private */
    public void writeSigningKeySetLPr(XmlSerializer serializer, PackageKeySetData data) throws IOException {
        serializer.startTag(null, "proper-signing-keyset");
        serializer.attribute(null, "identifier", Long.toString(data.getProperSigningKeySet()));
        serializer.endTag(null, "proper-signing-keyset");
    }

    /* access modifiers changed from: package-private */
    public void writeUpgradeKeySetsLPr(XmlSerializer serializer, PackageKeySetData data) throws IOException {
        if (data.isUsingUpgradeKeySets()) {
            long[] upgradeKeySets = data.getUpgradeKeySets();
            for (long id : upgradeKeySets) {
                serializer.startTag(null, "upgrade-keyset");
                serializer.attribute(null, "identifier", Long.toString(id));
                serializer.endTag(null, "upgrade-keyset");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeKeySetAliasesLPr(XmlSerializer serializer, PackageKeySetData data) throws IOException {
        for (Map.Entry<String, Long> e : data.getAliases().entrySet()) {
            serializer.startTag(null, "defined-keyset");
            serializer.attribute(null, "alias", e.getKey());
            serializer.attribute(null, "identifier", Long.toString(e.getValue().longValue()));
            serializer.endTag(null, "defined-keyset");
        }
    }

    /* access modifiers changed from: package-private */
    public void writePermissionLPr(XmlSerializer serializer, BasePermission bp) throws IOException {
        bp.writeLPr(serializer);
    }

    /* access modifiers changed from: package-private */
    public boolean isPackageSettingsError() {
        return this.mIsPackageSettingsError || "1".equals(SystemProperties.get(KEY_PACKAGE_SETTINS_ERROR, "0"));
    }

    static void setPackageSettingsError() {
        SystemProperties.set(KEY_PACKAGE_SETTINS_ERROR, "1");
    }

    static void resetPackageSettingsError() {
        SystemProperties.set(KEY_PACKAGE_SETTINS_ERROR, "0");
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x0367, code lost:
        if (1 == 0) goto L_0x0369;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x0369, code lost:
        reportReadSettingsFileError(r3, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x0397, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:0x0398, code lost:
        r19 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:0x045e, code lost:
        if (0 != 0) goto L_0x0462;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x046b, code lost:
        r9 = r20.mPendingPackages.get(r6);
        r10 = r9.getSharedUserId();
        r11 = getSettingLPr(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x047d, code lost:
        if ((r11 instanceof com.android.server.pm.SharedUserSetting) != false) goto L_0x047f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x047f, code lost:
        r12 = (com.android.server.pm.SharedUserSetting) r11;
        r9.sharedUser = r12;
        r9.appId = r12.userId;
        addPackageSettingLPw(r9, r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x0491, code lost:
        if (r11 != null) goto L_0x0493;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x0493, code lost:
        r12 = "Bad package setting: package " + r9.name + " has shared uid " + r10 + " that is not a shared uid\n";
        r20.mReadMessages.append(r12);
        com.android.server.pm.PackageManagerService.reportSettingsProblem(6, r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x04ba, code lost:
        r12 = "Bad package setting: package " + r9.name + " has shared uid " + r10 + " that is not defined\n";
        r20.mReadMessages.append(r12);
        com.android.server.pm.PackageManagerService.reportSettingsProblem(6, r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x04df, code lost:
        r6 = r6 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x04f8, code lost:
        r6 = r21.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x0500, code lost:
        if (r6.hasNext() != false) goto L_0x0502;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x0502, code lost:
        readPackageRestrictionsLPr(r6.next().id);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:0x0529, code lost:
        r20.mRuntimePermissionsPersistence.readStateForUserSyncLPr(r6.next().id);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:0x055c, code lost:
        r7 = r6.next();
        r9 = getSettingLPr(r7.appId);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x00b1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:181:0x056e, code lost:
        r7.sharedUser = (com.android.server.pm.SharedUserSetting) r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00b2, code lost:
        r2 = r0;
        r19 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00b7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00b8, code lost:
        r2 = r0;
        r6 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00bc, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00bd, code lost:
        r2 = r0;
        r6 = r9;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x0397 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:23:0x00c1] */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x03db A[Catch:{ XmlPullParserException -> 0x0419, IOException -> 0x03dc, Exception -> 0x0397, all -> 0x0393 }] */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x0417 A[Catch:{ XmlPullParserException -> 0x0419, IOException -> 0x03dc, Exception -> 0x0397, all -> 0x0393 }] */
    /* JADX WARNING: Removed duplicated region for block: B:150:0x046b  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x04ef  */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x0529 A[Catch:{ IllegalStateException -> 0x0538 }, LOOP:4: B:169:0x0523->B:171:0x0529, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x055c  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x00b7 A[ExcHandler: IOException (r0v11 'e' java.io.IOException A[CUSTOM_DECLARE]), PHI: r3 
      PHI: (r3v9 'fileName' java.lang.String) = (r3v7 'fileName' java.lang.String), (r3v1 'fileName' java.lang.String) binds: [B:25:0x00c5, B:9:0x0076] A[DONT_GENERATE, DONT_INLINE], Splitter:B:9:0x0076] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00bc A[ExcHandler: XmlPullParserException (r0v10 'e' org.xmlpull.v1.XmlPullParserException A[CUSTOM_DECLARE]), PHI: r3 
      PHI: (r3v8 'fileName' java.lang.String) = (r3v7 'fileName' java.lang.String), (r3v1 'fileName' java.lang.String) binds: [B:25:0x00c5, B:9:0x0076] A[DONT_GENERATE, DONT_INLINE], Splitter:B:9:0x0076] */
    public boolean readLPw(List<UserInfo> users) {
        int N;
        int i;
        Iterator<PackageSetting> disabledIt;
        Iterator<UserInfo> it;
        String str;
        XmlPullParserException e;
        String str2;
        IOException e2;
        String str3;
        int type;
        int type2;
        int outerDepth;
        String fileName = "";
        FileInputStream str4 = null;
        if (this.mBackupSettingsFilename.exists()) {
            try {
                str4 = new FileInputStream(this.mBackupSettingsFilename);
                fileName = this.mBackupSettingsFilename.getName();
                this.mReadMessages.append("Reading from backup settings file\n");
                PackageManagerService.reportSettingsProblem(4, "Need to read from backup settings file");
                if (this.mSettingsFilename.exists()) {
                    Slog.w("PackageManager", "Cleaning up settings file " + this.mSettingsFilename);
                    this.mSettingsFilename.delete();
                }
            } catch (IOException e3) {
            }
        }
        this.mPendingPackages.clear();
        this.mPastSignatures.clear();
        this.mKeySetRefs.clear();
        this.mInstallerPackages.clear();
        boolean isSuccess = true;
        String exceptionName = "";
        String str5 = "Error reading package manager settings";
        int i2 = 1;
        if (str4 == null) {
            try {
                if (!this.mSettingsFilename.exists()) {
                    this.mReadMessages.append("No settings file found\n");
                    PackageManagerService.reportSettingsProblem(4, "No settings file; creating initial state");
                    findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL).forceCurrent();
                    findOrCreateVersion("primary_physical").forceCurrent();
                    if (1 == 0) {
                        reportReadSettingsFileError(fileName, exceptionName);
                    }
                    return false;
                }
                str4 = new FileInputStream(this.mSettingsFilename);
                fileName = this.mSettingsFilename.getName();
            } catch (XmlPullParserException e4) {
            } catch (IOException e5) {
            } catch (Exception e6) {
            }
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(str4, StandardCharsets.UTF_8.name());
            while (true) {
                int type3 = parser.next();
                if (type3 == 2) {
                    type = type3;
                    break;
                }
                type = type3;
                if (type == 1) {
                    break;
                }
            }
            if (type == 2) {
                int outerDepth2 = parser.getDepth();
                while (true) {
                    int type4 = parser.next();
                    if (type4 != i2) {
                        if (type4 == 3 && parser.getDepth() <= outerDepth2) {
                            break;
                        }
                        if (type4 == 3) {
                            outerDepth = outerDepth2;
                            str3 = str5;
                            type2 = type4;
                        } else if (type4 == 4) {
                            outerDepth = outerDepth2;
                            str3 = str5;
                            type2 = type4;
                        } else {
                            String tagName = parser.getName();
                            if (tagName.equals("package")) {
                                readPackageLPw(parser);
                                outerDepth = outerDepth2;
                                str3 = str5;
                                type2 = type4;
                            } else if (tagName.equals("permissions")) {
                                this.mPermissions.readPermissions(parser);
                                outerDepth = outerDepth2;
                                str3 = str5;
                                type2 = type4;
                            } else if (tagName.equals("permission-trees")) {
                                this.mPermissions.readPermissionTrees(parser);
                                outerDepth = outerDepth2;
                                str3 = str5;
                                type2 = type4;
                            } else if (tagName.equals(TAG_SHARED_USER)) {
                                readSharedUserLPw(parser);
                                outerDepth = outerDepth2;
                                str3 = str5;
                                type2 = type4;
                            } else if (tagName.equals("preferred-packages")) {
                                outerDepth = outerDepth2;
                                str3 = str5;
                                type2 = type4;
                            } else if (tagName.equals("preferred-activities")) {
                                readPreferredActivitiesLPw(parser, 0);
                                outerDepth = outerDepth2;
                                str3 = str5;
                                type2 = type4;
                            } else if (tagName.equals(TAG_PERSISTENT_PREFERRED_ACTIVITIES)) {
                                readPersistentPreferredActivitiesLPw(parser, 0);
                                outerDepth = outerDepth2;
                                str3 = str5;
                                type2 = type4;
                            } else if (tagName.equals(TAG_CROSS_PROFILE_INTENT_FILTERS)) {
                                readCrossProfileIntentFiltersLPw(parser, 0);
                                outerDepth = outerDepth2;
                                str3 = str5;
                                type2 = type4;
                            } else if (tagName.equals(TAG_DEFAULT_BROWSER)) {
                                readDefaultAppsLPw(parser, 0);
                                outerDepth = outerDepth2;
                                str3 = str5;
                                type2 = type4;
                            } else if (tagName.equals("updated-package")) {
                                readDisabledSysPackageLPw(parser);
                                outerDepth = outerDepth2;
                                str3 = str5;
                                type2 = type4;
                            } else {
                                outerDepth = outerDepth2;
                                if (tagName.equals("renamed-package")) {
                                    String nname = parser.getAttributeValue(null, "new");
                                    type2 = type4;
                                    String oname = parser.getAttributeValue(null, "old");
                                    if (!(nname == null || oname == null)) {
                                        this.mRenamedPackages.put(nname, oname);
                                    }
                                    str3 = str5;
                                } else {
                                    type2 = type4;
                                    if (tagName.equals("restored-ivi")) {
                                        readRestoredIntentFilterVerifications(parser);
                                        str3 = str5;
                                    } else if (tagName.equals("last-platform-version")) {
                                        VersionInfo internal = findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL);
                                        VersionInfo external = findOrCreateVersion("primary_physical");
                                        str3 = str5;
                                        try {
                                            internal.sdkVersion = XmlUtils.readIntAttribute(parser, "internal", 0);
                                            external.sdkVersion = XmlUtils.readIntAttribute(parser, "external", 0);
                                            String readStringAttribute = XmlUtils.readStringAttribute(parser, ATTR_FINGERPRINT);
                                            external.fingerprint = readStringAttribute;
                                            internal.fingerprint = readStringAttribute;
                                            String readStringAttribute2 = XmlUtils.readStringAttribute(parser, ATTR_HWFINGERPRINT);
                                            external.hwFingerprint = readStringAttribute2;
                                            internal.hwFingerprint = readStringAttribute2;
                                            String readStringAttribute3 = XmlUtils.readStringAttribute(parser, ATTR_FINGERPRINTEX);
                                            external.fingerprintEx = readStringAttribute3;
                                            internal.fingerprintEx = readStringAttribute3;
                                        } catch (XmlPullParserException e7) {
                                            e = e7;
                                            str = str3;
                                            exceptionName = "XmlPullParserException";
                                            this.mIsPackageSettingsError = true;
                                            this.mReadMessages.append("Error reading: " + e.toString());
                                            PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
                                            Slog.wtf("PackageManager", str, e);
                                            HwBootFail.brokenFileBootFail(HwBootFail.PACKAGE_MANAGER_SETTING_FILE_DAMAGED, "/data/system/packages.xml", new Throwable());
                                        } catch (IOException e8) {
                                            e2 = e8;
                                            str2 = str3;
                                            exceptionName = "IOException";
                                            this.mIsPackageSettingsError = true;
                                            this.mReadMessages.append("Error reading: " + e2.toString());
                                            PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
                                            Slog.wtf("PackageManager", str2, e2);
                                            if (0 == 0) {
                                            }
                                            N = this.mPendingPackages.size();
                                            i = 0;
                                            while (i < N) {
                                            }
                                            this.mPendingPackages.clear();
                                            if (!this.mBackupStoppedPackagesFilename.exists()) {
                                            }
                                            readStoppedLPw();
                                            this.mBackupStoppedPackagesFilename.delete();
                                            this.mStoppedPackagesFilename.delete();
                                            writePackageRestrictionsLPr(0);
                                            try {
                                                it = users.iterator();
                                                while (it.hasNext()) {
                                                }
                                            } catch (IllegalStateException e9) {
                                                HwBootFail.brokenFileBootFail(HwBootFail.RUNTIME_PERMISSION_SETTING_FILE_DAMAGED, "/data/system/users/0/runtime-permissions.xml", new Throwable());
                                                Log.wtf("PackageManager", "Error reading state for user", e9);
                                            }
                                            disabledIt = this.mDisabledSysPackages.values().iterator();
                                            while (disabledIt.hasNext()) {
                                            }
                                            this.mReadMessages.append("Read completed successfully: " + this.mPackages.size() + " packages, " + this.mSharedUsers.size() + " shared uids\n");
                                            writeKernelMappingLPr();
                                            return true;
                                        } catch (Exception e10) {
                                            Exception e11 = e10;
                                            Exception e12 = e11;
                                            isSuccess = false;
                                            exceptionName = e12.getClass().toString();
                                            this.mIsPackageSettingsError = true;
                                            this.mReadMessages.append("Error reading: " + e12.toString());
                                            PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e12);
                                            Log.wtf("PackageManager", str3, e12);
                                            if (0 == 0) {
                                            }
                                            N = this.mPendingPackages.size();
                                            i = 0;
                                            while (i < N) {
                                            }
                                            this.mPendingPackages.clear();
                                            if (!this.mBackupStoppedPackagesFilename.exists()) {
                                            }
                                            readStoppedLPw();
                                            this.mBackupStoppedPackagesFilename.delete();
                                            this.mStoppedPackagesFilename.delete();
                                            writePackageRestrictionsLPr(0);
                                            it = users.iterator();
                                            while (it.hasNext()) {
                                            }
                                            disabledIt = this.mDisabledSysPackages.values().iterator();
                                            while (disabledIt.hasNext()) {
                                            }
                                            this.mReadMessages.append("Read completed successfully: " + this.mPackages.size() + " packages, " + this.mSharedUsers.size() + " shared uids\n");
                                            writeKernelMappingLPr();
                                            return true;
                                        }
                                    } else {
                                        str3 = str5;
                                        if (tagName.equals("database-version")) {
                                            VersionInfo internal2 = findOrCreateVersion(StorageManager.UUID_PRIVATE_INTERNAL);
                                            VersionInfo external2 = findOrCreateVersion("primary_physical");
                                            internal2.databaseVersion = XmlUtils.readIntAttribute(parser, "internal", 0);
                                            external2.databaseVersion = XmlUtils.readIntAttribute(parser, "external", 0);
                                        } else if (tagName.equals("verifier")) {
                                            try {
                                                this.mVerifierDeviceIdentity = VerifierDeviceIdentity.parse(parser.getAttributeValue(null, "device"));
                                            } catch (IllegalArgumentException e13) {
                                                Slog.w("PackageManager", "Discard invalid verifier device id: " + e13.getMessage());
                                            }
                                        } else if (TAG_READ_EXTERNAL_STORAGE.equals(tagName)) {
                                            this.mReadExternalStorageEnforced = "1".equals(parser.getAttributeValue(null, ATTR_ENFORCEMENT)) ? Boolean.TRUE : Boolean.FALSE;
                                        } else if (tagName.equals("keyset-settings")) {
                                            this.mKeySetManagerService.readKeySetsLPw(parser, this.mKeySetRefs);
                                        } else if ("version".equals(tagName)) {
                                            VersionInfo ver = findOrCreateVersion(XmlUtils.readStringAttribute(parser, ATTR_VOLUME_UUID));
                                            ver.sdkVersion = XmlUtils.readIntAttribute(parser, ATTR_SDK_VERSION);
                                            ver.databaseVersion = XmlUtils.readIntAttribute(parser, ATTR_DATABASE_VERSION);
                                            ver.emuiVersion = XmlUtils.readIntAttribute(parser, ATTR_EMUI_VERSION, 0);
                                            ver.fingerprint = XmlUtils.readStringAttribute(parser, ATTR_FINGERPRINT);
                                            ver.hwFingerprint = XmlUtils.readStringAttribute(parser, ATTR_HWFINGERPRINT);
                                            ver.fingerprintEx = XmlUtils.readStringAttribute(parser, ATTR_FINGERPRINTEX);
                                        } else {
                                            Slog.w("PackageManager", "Unknown element under <packages>: " + parser.getName());
                                            XmlUtils.skipCurrentTag(parser);
                                        }
                                    }
                                }
                            }
                        }
                        outerDepth2 = outerDepth;
                        str5 = str3;
                        i2 = 1;
                    } else {
                        break;
                    }
                }
                str4.close();
            } else {
                this.mReadMessages.append("No start tag found in settings file\n");
                PackageManagerService.reportSettingsProblem(5, "No start tag found in package manager settings");
                Slog.wtf("PackageManager", "No start tag found in package manager settings");
                throw new XmlPullParserException(" No start tag found in settings file");
            }
        } catch (XmlPullParserException e14) {
            str = str5;
            e = e14;
            exceptionName = "XmlPullParserException";
            this.mIsPackageSettingsError = true;
            this.mReadMessages.append("Error reading: " + e.toString());
            PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e);
            Slog.wtf("PackageManager", str, e);
            HwBootFail.brokenFileBootFail(HwBootFail.PACKAGE_MANAGER_SETTING_FILE_DAMAGED, "/data/system/packages.xml", new Throwable());
        } catch (IOException e15) {
            str2 = str5;
            e2 = e15;
            exceptionName = "IOException";
            this.mIsPackageSettingsError = true;
            this.mReadMessages.append("Error reading: " + e2.toString());
            PackageManagerService.reportSettingsProblem(6, "Error reading settings: " + e2);
            Slog.wtf("PackageManager", str2, e2);
            if (0 == 0) {
            }
            N = this.mPendingPackages.size();
            i = 0;
            while (i < N) {
            }
            this.mPendingPackages.clear();
            if (!this.mBackupStoppedPackagesFilename.exists()) {
            }
            readStoppedLPw();
            this.mBackupStoppedPackagesFilename.delete();
            this.mStoppedPackagesFilename.delete();
            writePackageRestrictionsLPr(0);
            it = users.iterator();
            while (it.hasNext()) {
            }
            disabledIt = this.mDisabledSysPackages.values().iterator();
            while (disabledIt.hasNext()) {
            }
            this.mReadMessages.append("Read completed successfully: " + this.mPackages.size() + " packages, " + this.mSharedUsers.size() + " shared uids\n");
            writeKernelMappingLPr();
            return true;
        } catch (Exception e62) {
        } catch (Throwable th) {
            if (!isSuccess) {
                reportReadSettingsFileError(fileName, exceptionName);
            }
            throw th;
        }
    }

    private void reportReadSettingsFileError(String fileName, String exceptionName) {
        int times = SystemProperties.getInt("persist.sys.hwpms_error_reboot_count", 0);
        Slog.i("PackageManager", " Error reading file:" + fileName + ", occur to " + exceptionName + ", reboot " + times + " times.");
        HwPackageManagerServiceUtils.reportPmsInitException(fileName, times, exceptionName, null);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0160, code lost:
        if (1 != 0) goto L_0x0221;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x01f0, code lost:
        if (0 == 0) goto L_0x0216;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x0214, code lost:
        if (0 == 0) goto L_0x0216;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x0216, code lost:
        reportReadPreferredAppsError(r9.getName(), r9.getPath(), r13);
     */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x022a A[SYNTHETIC, Splitter:B:104:0x022a] */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0231  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x01bc A[SYNTHETIC, Splitter:B:78:0x01bc] */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x01c3  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01eb A[SYNTHETIC, Splitter:B:87:0x01eb] */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x020f A[SYNTHETIC, Splitter:B:95:0x020f] */
    public void applyDefaultPreferredAppsLPw(int userId) {
        Throwable th;
        PackageManagerInternal pmInternal;
        XmlPullParserException e;
        IOException e2;
        Exception e3;
        int type;
        PackageManagerInternal pmInternal2 = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        Iterator<PackageSetting> it = this.mPackages.values().iterator();
        while (true) {
            boolean isPreinstalledApk = false;
            if (!it.hasNext()) {
                break;
            }
            PackageSetting ps = it.next();
            boolean isSystemApp = (ps.pkgFlags & 1) != 0;
            if (ps.codePathString != null && !ps.codePathString.startsWith("/data/app/")) {
                isPreinstalledApk = true;
            }
            if (!(!(isSystemApp || isPreinstalledApk) || ps.pkg == null || ps.pkg.preferredActivityFilters == null)) {
                ArrayList<PackageParser.ActivityIntentInfo> intents = ps.pkg.preferredActivityFilters;
                for (int i = 0; i < intents.size(); i++) {
                    PackageParser.ActivityIntentInfo aii = intents.get(i);
                    applyDefaultPreferredActivityLPw(pmInternal2, aii, new ComponentName(ps.name, aii.activity.className), userId);
                }
            }
        }
        ArrayList<File> allFiles = new ArrayList<>();
        ArrayList<File> custDirs = HwCfgFilePolicy.getCfgFileList("preferred-apps", 0);
        custDirs.add(new File(Environment.getRootDirectory(), "etc/preferred-apps"));
        Iterator<File> it2 = custDirs.iterator();
        while (it2.hasNext()) {
            File dir = it2.next();
            if (dir.isDirectory() && dir.canRead()) {
                Collections.addAll(allFiles, dir.listFiles());
            }
        }
        Iterator<File> it3 = allFiles.iterator();
        while (it3.hasNext()) {
            File f = it3.next();
            if (!f.getPath().endsWith(".xml")) {
                Slog.i(TAG, "Non-xml file " + f + ", ignoring");
            } else if (!f.canRead()) {
                Slog.w(TAG, "Preferred apps file " + f + " cannot be read");
            } else {
                if (PackageManagerService.DEBUG_PREFERRED) {
                    Log.d(TAG, "Reading default preferred " + f);
                }
                InputStream str = null;
                boolean isSuccess = true;
                String exceptionName = "";
                try {
                    str = new BufferedInputStream(new FileInputStream(f));
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(str, null);
                    while (true) {
                        type = parser.next();
                        if (type == 2) {
                            break;
                        }
                        if (type == 1) {
                            break;
                        }
                    }
                    if (type == 2) {
                        pmInternal = pmInternal2;
                        try {
                            if ("preferred-activities".equals(parser.getName())) {
                                readDefaultPreferredActivitiesLPw(parser, userId);
                                try {
                                    str.close();
                                } catch (IOException e4) {
                                }
                            } else {
                                throw new XmlPullParserException("Preferred apps file:" + f.getName() + " does not start with 'preferred-activities'");
                            }
                        } catch (XmlPullParserException e5) {
                            e = e5;
                            exceptionName = "XmlPullParserException";
                            Slog.w(TAG, "Error reading apps file " + f, e);
                            if (str != null) {
                            }
                        } catch (IOException e6) {
                            e2 = e6;
                            exceptionName = "IOException";
                            Slog.w(TAG, "Error reading apps file " + f, e2);
                            if (str != null) {
                            }
                        } catch (Exception e7) {
                            e3 = e7;
                            isSuccess = false;
                            try {
                                String exceptionName2 = e3.getClass().toString();
                                if (str != null) {
                                }
                                if (0 == 0) {
                                }
                                pmInternal2 = pmInternal;
                            } catch (Throwable th2) {
                                th = th2;
                                if (str != null) {
                                    try {
                                        str.close();
                                    } catch (IOException e8) {
                                    }
                                }
                                if (!isSuccess) {
                                    reportReadPreferredAppsError(f.getName(), f.getPath(), exceptionName);
                                }
                                throw th;
                            }
                        }
                    } else {
                        throw new XmlPullParserException("Preferred apps file:" + f.getName() + " does not have start tag");
                    }
                } catch (XmlPullParserException e9) {
                    e = e9;
                    pmInternal = pmInternal2;
                    exceptionName = "XmlPullParserException";
                    Slog.w(TAG, "Error reading apps file " + f, e);
                    if (str != null) {
                        try {
                            str.close();
                        } catch (IOException e10) {
                        }
                    }
                } catch (IOException e11) {
                    e2 = e11;
                    pmInternal = pmInternal2;
                    exceptionName = "IOException";
                    Slog.w(TAG, "Error reading apps file " + f, e2);
                    if (str != null) {
                        try {
                            str.close();
                        } catch (IOException e12) {
                        }
                    }
                } catch (Exception e13) {
                    e3 = e13;
                    pmInternal = pmInternal2;
                    isSuccess = false;
                    String exceptionName22 = e3.getClass().toString();
                    if (str != null) {
                        try {
                            str.close();
                        } catch (IOException e14) {
                        }
                    }
                    if (0 == 0) {
                        reportReadPreferredAppsError(f.getName(), f.getPath(), exceptionName22);
                    }
                    pmInternal2 = pmInternal;
                } catch (Throwable th3) {
                    th = th3;
                    if (str != null) {
                    }
                    if (!isSuccess) {
                    }
                    throw th;
                }
            }
        }
    }

    private void reportReadPreferredAppsError(String fileName, String filePath, String exceptionName) {
        if (fileName != null && filePath != null) {
            String[] subPaths = filePath.split(SliceClientPermissions.SliceAuthority.DELIMITER);
            String rootPath = (subPaths.length <= 1 || !"".equals(subPaths[0])) ? subPaths[0] : subPaths[1];
            Slog.e(TAG, exceptionName + " Error reading preferred-apps file:" + fileName + " in " + rootPath);
            HwPackageManagerServiceUtils.reportPmsParseFileException(fileName, exceptionName, -1, rootPath);
        }
    }

    private void applyDefaultPreferredActivityLPw(PackageManagerInternal pmInternal, IntentFilter tmpPa, ComponentName cn, int userId) {
        int ischeme;
        if (PackageManagerService.DEBUG_PREFERRED) {
            Log.d(TAG, "Processing preferred:");
            tmpPa.dump(new LogPrinter(3, TAG), "  ");
        }
        Intent intent = new Intent();
        intent.setAction(tmpPa.getAction(0));
        int flags = 786432;
        for (int i = 0; i < tmpPa.countCategories(); i++) {
            String cat = tmpPa.getCategory(i);
            if (cat.equals("android.intent.category.DEFAULT")) {
                flags |= 65536;
            } else {
                intent.addCategory(cat);
            }
        }
        int dataSchemesCount = tmpPa.countDataSchemes();
        boolean hasSchemes = false;
        boolean doNonData = true;
        int ischeme2 = 0;
        while (ischeme2 < dataSchemesCount) {
            String scheme = tmpPa.getDataScheme(ischeme2);
            if (scheme != null && !scheme.isEmpty()) {
                hasSchemes = true;
            }
            boolean doScheme = true;
            int issp = 0;
            for (int dataSchemeSpecificPartsCount = tmpPa.countDataSchemeSpecificParts(); issp < dataSchemeSpecificPartsCount; dataSchemeSpecificPartsCount = dataSchemeSpecificPartsCount) {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme(scheme);
                PatternMatcher ssp = tmpPa.getDataSchemeSpecificPart(issp);
                builder.opaquePart(ssp.getPath());
                Intent finalIntent = new Intent(intent);
                finalIntent.setData(builder.build());
                applyDefaultPreferredActivityLPw(pmInternal, finalIntent, flags, cn, scheme, ssp, null, null, userId);
                doScheme = false;
                issp++;
                scheme = scheme;
                dataSchemesCount = dataSchemesCount;
            }
            int dataAuthoritiesCount = tmpPa.countDataAuthorities();
            int iauth = 0;
            while (iauth < dataAuthoritiesCount) {
                IntentFilter.AuthorityEntry auth = tmpPa.getDataAuthority(iauth);
                int dataPathsCount = tmpPa.countDataPaths();
                int ipath = 0;
                boolean doScheme2 = doScheme;
                boolean doAuth = true;
                while (ipath < dataPathsCount) {
                    Uri.Builder builder2 = new Uri.Builder();
                    builder2.scheme(scheme);
                    if (auth.getHost() != null) {
                        builder2.authority(auth.getHost());
                    }
                    PatternMatcher path = tmpPa.getDataPath(ipath);
                    builder2.path(path.getPath());
                    Intent finalIntent2 = new Intent(intent);
                    finalIntent2.setData(builder2.build());
                    applyDefaultPreferredActivityLPw(pmInternal, finalIntent2, flags, cn, scheme, null, auth, path, userId);
                    doScheme2 = false;
                    doAuth = false;
                    ipath++;
                    dataPathsCount = dataPathsCount;
                    iauth = iauth;
                    dataAuthoritiesCount = dataAuthoritiesCount;
                }
                if (doAuth) {
                    Uri.Builder builder3 = new Uri.Builder();
                    builder3.scheme(scheme);
                    if (auth.getHost() != null) {
                        builder3.authority(auth.getHost());
                    }
                    Intent finalIntent3 = new Intent(intent);
                    finalIntent3.setData(builder3.build());
                    applyDefaultPreferredActivityLPw(pmInternal, finalIntent3, flags, cn, scheme, null, auth, null, userId);
                    doScheme = false;
                } else {
                    doScheme = doScheme2;
                }
                iauth++;
                dataAuthoritiesCount = dataAuthoritiesCount;
            }
            if (doScheme) {
                Uri.Builder builder4 = new Uri.Builder();
                builder4.scheme(scheme);
                Intent finalIntent4 = new Intent(intent);
                finalIntent4.setData(builder4.build());
                applyDefaultPreferredActivityLPw(pmInternal, finalIntent4, flags, cn, scheme, null, null, null, userId);
            }
            doNonData = false;
            ischeme2++;
            dataSchemesCount = dataSchemesCount;
        }
        boolean doNonData2 = doNonData;
        for (int idata = 0; idata < tmpPa.countDataTypes(); idata++) {
            String mimeType = tmpPa.getDataType(idata);
            if (hasSchemes) {
                Uri.Builder builder5 = new Uri.Builder();
                int ischeme3 = 0;
                while (ischeme3 < tmpPa.countDataSchemes()) {
                    String scheme2 = tmpPa.getDataScheme(ischeme3);
                    if (scheme2 == null || scheme2.isEmpty()) {
                        ischeme = ischeme3;
                    } else {
                        Intent finalIntent5 = new Intent(intent);
                        builder5.scheme(scheme2);
                        finalIntent5.setDataAndType(builder5.build(), mimeType);
                        ischeme = ischeme3;
                        applyDefaultPreferredActivityLPw(pmInternal, finalIntent5, flags, cn, scheme2, null, null, null, userId);
                    }
                    ischeme3 = ischeme + 1;
                }
            } else {
                Intent finalIntent6 = new Intent(intent);
                finalIntent6.setType(mimeType);
                applyDefaultPreferredActivityLPw(pmInternal, finalIntent6, flags, cn, null, null, null, null, userId);
            }
            doNonData2 = false;
        }
        if (doNonData2) {
            applyDefaultPreferredActivityLPw(pmInternal, intent, flags, cn, null, null, null, null, userId);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x0150  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0171 A[SYNTHETIC] */
    private void applyDefaultPreferredActivityLPw(PackageManagerInternal pmInternal, Intent intent, int flags, ComponentName cn, String scheme, PatternMatcher ssp, IntentFilter.AuthorityEntry auth, PatternMatcher path, int userId) {
        boolean isNonPreinstalledApk;
        List<ResolveInfo> ri = pmInternal.queryIntentActivities(intent, intent.getType(), flags, Binder.getCallingUid(), 0);
        if (PackageManagerService.DEBUG_PREFERRED) {
            Log.d(TAG, "Queried " + intent + " results: " + ri);
        }
        int numMatches = ri == null ? 0 : ri.size();
        if (numMatches <= 1) {
            Slog.w(TAG, "No potential matches found for " + intent + " while setting preferred " + cn.flattenToShortString());
            return;
        }
        ComponentName haveNonSys = null;
        ComponentName[] set = new ComponentName[ri.size()];
        int i = 0;
        boolean haveAct = false;
        int systemMatch = 0;
        while (true) {
            if (i >= numMatches) {
                break;
            }
            ActivityInfo ai = ri.get(i).activityInfo;
            set[i] = new ComponentName(ai.packageName, ai.name);
            PackageSetting packageSetting = this.mPackages.get(ai.packageName);
            boolean isNonSystemApp = (ai.applicationInfo.flags & 1) == 0;
            if (packageSetting != null && packageSetting.codePathString != null) {
                if (!packageSetting.codePathString.startsWith("/data/app/")) {
                    isNonPreinstalledApk = false;
                    if (isNonSystemApp || !isNonPreinstalledApk) {
                        if (!cn.getPackageName().equals(ai.packageName) && cn.getClassName().equals(ai.name)) {
                            if (PackageManagerService.DEBUG_PREFERRED) {
                                Log.d(TAG, "Result " + ai.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + ai.name + ": default!");
                            }
                            haveAct = true;
                            systemMatch = ri.get(i).match;
                        } else if (!PackageManagerService.DEBUG_PREFERRED) {
                            Log.d(TAG, "Result " + ai.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + ai.name + ": skipped");
                        }
                    } else if (ri.get(i).match >= 0) {
                        if (PackageManagerService.DEBUG_PREFERRED) {
                            Log.d(TAG, "Result " + ai.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + ai.name + ": non-system!");
                        }
                        haveNonSys = set[i];
                    }
                    i++;
                    haveNonSys = haveNonSys;
                    numMatches = numMatches;
                }
            }
            isNonPreinstalledApk = true;
            if (isNonSystemApp) {
            }
            if (!cn.getPackageName().equals(ai.packageName)) {
            }
            if (!PackageManagerService.DEBUG_PREFERRED) {
            }
            i++;
            haveNonSys = haveNonSys;
            numMatches = numMatches;
        }
        if (haveNonSys != null && 0 < systemMatch) {
            haveNonSys = null;
        }
        if (haveAct && haveNonSys == null) {
            IntentFilter filter = new IntentFilter();
            if (intent.getAction() != null) {
                filter.addAction(intent.getAction());
            }
            if (intent.getCategories() != null) {
                for (String cat : intent.getCategories()) {
                    filter.addCategory(cat);
                }
            }
            if ((flags & 65536) != 0) {
                filter.addCategory("android.intent.category.DEFAULT");
            }
            if (scheme != null) {
                filter.addDataScheme(scheme);
            }
            if (ssp != null) {
                filter.addDataSchemeSpecificPart(ssp.getPath(), ssp.getType());
            }
            if (auth != null) {
                filter.addDataAuthority(auth);
            }
            if (path != null) {
                filter.addDataPath(path);
            }
            if (intent.getType() != null) {
                try {
                    String type = intent.getType();
                    if (type.indexOf(47) == -1) {
                        type = type + "/*";
                    }
                    filter.addDataType(type);
                } catch (IntentFilter.MalformedMimeTypeException e) {
                    Slog.w(TAG, "Malformed mimetype " + intent.getType() + " for " + cn);
                }
            }
            editPreferredActivitiesLPw(userId).addFilter(new PreferredActivity(filter, systemMatch, set, cn, true));
        } else if (haveNonSys == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("No component ");
            sb.append(cn.flattenToShortString());
            sb.append(" found setting preferred ");
            sb.append(intent);
            sb.append("; possible matches are ");
            for (int i2 = 0; i2 < set.length; i2++) {
                if (i2 > 0) {
                    sb.append(", ");
                }
                sb.append(set[i2].flattenToShortString());
            }
            Slog.w(TAG, sb.toString());
        } else {
            Slog.i(TAG, "Not setting preferred " + intent + "; found third party match " + haveNonSys.flattenToShortString());
        }
    }

    private void readDefaultPreferredActivitiesLPw(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        PackageManagerInternal pmInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    PreferredActivity tmpPa = new PreferredActivity(parser);
                    if (tmpPa.mPref.getParseError() == null) {
                        applyDefaultPreferredActivityLPw(pmInternal, tmpPa, tmpPa.mPref.mComponent, userId);
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <preferred-activity> " + tmpPa.mPref.getParseError() + " at " + parser.getPositionDescription());
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <preferred-activities>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x007b  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0080  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00bf A[SYNTHETIC, Splitter:B:21:0x00bf] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00c9  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00e4 A[SYNTHETIC, Splitter:B:31:0x00e4] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00f5 A[SYNTHETIC, Splitter:B:36:0x00f5] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0107  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x010c  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0113  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x012b A[SYNTHETIC, Splitter:B:51:0x012b] */
    private void readDisabledSysPackageLPw(XmlPullParser parser) throws XmlPullParserException, IOException {
        String primaryCpuAbiStr;
        String resourcePathStr;
        long versionCode;
        int pkgPrivateFlags;
        PackageSetting ps;
        String timeStampStr;
        String timeStampStr2;
        String timeStampStr3;
        String hwExtraFlagsStr;
        int type;
        String name = parser.getAttributeValue(null, ATTR_NAME);
        String realName = parser.getAttributeValue(null, "realName");
        String codePathStr = parser.getAttributeValue(null, "codePath");
        String resourcePathStr2 = parser.getAttributeValue(null, "resourcePath");
        String legacyCpuAbiStr = parser.getAttributeValue(null, "requiredCpuAbi");
        String legacyNativeLibraryPathStr = parser.getAttributeValue(null, "nativeLibraryPath");
        String parentPackageName = parser.getAttributeValue(null, "parentPackageName");
        String primaryCpuAbiStr2 = parser.getAttributeValue(null, "primaryCpuAbi");
        String secondaryCpuAbiStr = parser.getAttributeValue(null, "secondaryCpuAbi");
        String cpuAbiOverrideStr = parser.getAttributeValue(null, "cpuAbiOverride");
        if (primaryCpuAbiStr2 != null || legacyCpuAbiStr == null) {
            primaryCpuAbiStr = primaryCpuAbiStr2;
        } else {
            primaryCpuAbiStr = legacyCpuAbiStr;
        }
        if (resourcePathStr2 == null) {
            resourcePathStr = codePathStr;
        } else {
            resourcePathStr = resourcePathStr2;
        }
        String version = parser.getAttributeValue(null, "version");
        if (version != null) {
            try {
                versionCode = Long.parseLong(version);
            } catch (NumberFormatException e) {
            }
            int pkgFlags = 0 | 1;
            if (!PackageManagerService.locationIsPrivileged(codePathStr)) {
                pkgPrivateFlags = 0 | 8;
            } else {
                pkgPrivateFlags = 0;
            }
            ps = new PackageSetting(name, realName, new File(codePathStr), new File(resourcePathStr), legacyNativeLibraryPathStr, primaryCpuAbiStr, secondaryCpuAbiStr, cpuAbiOverrideStr, versionCode, pkgFlags, pkgPrivateFlags, parentPackageName, null, 0, null, null);
            timeStampStr = parser.getAttributeValue(null, "ft");
            if (timeStampStr == null) {
                try {
                    ps.setTimeStamp(Long.parseLong(timeStampStr, 16));
                } catch (NumberFormatException e2) {
                }
            } else {
                String timeStampStr4 = parser.getAttributeValue(null, "ts");
                if (timeStampStr4 != null) {
                    try {
                        ps.setTimeStamp(Long.parseLong(timeStampStr4));
                    } catch (NumberFormatException e3) {
                    }
                }
            }
            timeStampStr2 = parser.getAttributeValue(null, "it");
            if (timeStampStr2 != null) {
                try {
                    ps.firstInstallTime = Long.parseLong(timeStampStr2, 16);
                } catch (NumberFormatException e4) {
                }
            }
            timeStampStr3 = parser.getAttributeValue(null, "ut");
            if (timeStampStr3 != null) {
                try {
                    ps.lastUpdateTime = Long.parseLong(timeStampStr3, 16);
                } catch (NumberFormatException e5) {
                }
            }
            String idStr = parser.getAttributeValue(null, "userId");
            int i = 0;
            ps.appId = idStr == null ? Integer.parseInt(idStr) : 0;
            if (ps.appId <= 0) {
                String sharedIdStr = parser.getAttributeValue(null, "sharedUserId");
                if (sharedIdStr != null) {
                    i = Integer.parseInt(sharedIdStr);
                }
                ps.appId = i;
            }
            hwExtraFlagsStr = parser.getAttributeValue(null, "hwExtraFlags");
            if (hwExtraFlagsStr != null) {
                try {
                    ps.hw_extra_flags = Integer.parseInt(hwExtraFlagsStr);
                } catch (NumberFormatException e6) {
                }
            }
            int outerDepth = parser.getDepth();
            while (true) {
                type = parser.next();
                if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                    break;
                } else if (!(type == 3 || type == 4)) {
                    if (parser.getName().equals(TAG_PERMISSIONS)) {
                        readInstallPermissionsLPr(parser, ps.getPermissionsState());
                    } else if (parser.getName().equals(TAG_CHILD_PACKAGE)) {
                        String childPackageName = parser.getAttributeValue(null, ATTR_NAME);
                        if (ps.childPackageNames == null) {
                            ps.childPackageNames = new ArrayList();
                        }
                        ps.childPackageNames.add(childPackageName);
                    } else if (parser.getName().equals(TAG_USES_STATIC_LIB)) {
                        readUsesStaticLibLPw(parser, ps);
                    } else if (parser.getName().equals(TAG_DISABLE_PLUGIN)) {
                        readDisablePluginsLPw(parser, ps);
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Unknown element under <updated-package>: " + parser.getName());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
            this.mDisabledSysPackages.put(name, ps);
        }
        versionCode = 0;
        int pkgFlags2 = 0 | 1;
        if (!PackageManagerService.locationIsPrivileged(codePathStr)) {
        }
        ps = new PackageSetting(name, realName, new File(codePathStr), new File(resourcePathStr), legacyNativeLibraryPathStr, primaryCpuAbiStr, secondaryCpuAbiStr, cpuAbiOverrideStr, versionCode, pkgFlags2, pkgPrivateFlags, parentPackageName, null, 0, null, null);
        timeStampStr = parser.getAttributeValue(null, "ft");
        if (timeStampStr == null) {
        }
        timeStampStr2 = parser.getAttributeValue(null, "it");
        if (timeStampStr2 != null) {
        }
        timeStampStr3 = parser.getAttributeValue(null, "ut");
        if (timeStampStr3 != null) {
        }
        String idStr2 = parser.getAttributeValue(null, "userId");
        int i2 = 0;
        ps.appId = idStr2 == null ? Integer.parseInt(idStr2) : 0;
        if (ps.appId <= 0) {
        }
        hwExtraFlagsStr = parser.getAttributeValue(null, "hwExtraFlags");
        if (hwExtraFlagsStr != null) {
        }
        int outerDepth2 = parser.getDepth();
        while (true) {
            type = parser.next();
            if (type == 1) {
                break;
            }
            break;
        }
        this.mDisabledSysPackages.put(name, ps);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:201:0x03b8 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r9v19 'codePathStr'  java.lang.String: [D('cpuAbiOverrideString' java.lang.String), D('codePathStr' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r6v40 'legacyNativeLibraryPathStr'  java.lang.String: [D('resourcePathStr' java.lang.String), D('legacyNativeLibraryPathStr' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r5v45 'name'  java.lang.String: [D('name' java.lang.String), D('sharedUserId' int)] */
    /* JADX INFO: Multiple debug info for r6v65 'legacyNativeLibraryPathStr'  java.lang.String: [D('legacyNativeLibraryPathStr' java.lang.String), D('resourcePathStr' java.lang.String)] */
    /* JADX WARN: Type inference failed for: r5v64, types: [java.io.File] */
    /* JADX WARN: Type inference failed for: r5v74 */
    /* JADX WARN: Type inference failed for: r2v29, types: [java.lang.StringBuilder] */
    /* JADX WARN: Type inference failed for: r5v78, types: [java.lang.String] */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x0213  */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x0227 A[SYNTHETIC, Splitter:B:138:0x0227] */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x024e A[Catch:{ NumberFormatException -> 0x0257 }] */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x0252 A[Catch:{ NumberFormatException -> 0x0257 }] */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x026b A[Catch:{ NumberFormatException -> 0x0257 }] */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x026e A[Catch:{ NumberFormatException -> 0x0257 }] */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x0273  */
    /* JADX WARNING: Removed duplicated region for block: B:150:0x0276  */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x027d  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x0283 A[SYNTHETIC, Splitter:B:153:0x0283] */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x02a0  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x02a4 A[SYNTHETIC, Splitter:B:160:0x02a4] */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x02fa  */
    /* JADX WARNING: Removed duplicated region for block: B:372:0x0ae0  */
    /* JADX WARNING: Removed duplicated region for block: B:394:0x0b75  */
    /* JADX WARNING: Removed duplicated region for block: B:395:0x0b7d  */
    /* JADX WARNING: Removed duplicated region for block: B:398:0x0b88  */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0b9c  */
    /* JADX WARNING: Removed duplicated region for block: B:406:0x0bac A[SYNTHETIC, Splitter:B:406:0x0bac] */
    /* JADX WARNING: Removed duplicated region for block: B:412:0x0bc2 A[SYNTHETIC, Splitter:B:412:0x0bc2] */
    /* JADX WARNING: Removed duplicated region for block: B:418:0x0bd9 A[SYNTHETIC, Splitter:B:418:0x0bd9] */
    /* JADX WARNING: Removed duplicated region for block: B:423:0x0bea A[SYNTHETIC, Splitter:B:423:0x0bea] */
    /* JADX WARNING: Removed duplicated region for block: B:430:0x0c05  */
    /* JADX WARNING: Removed duplicated region for block: B:484:0x0e1a  */
    /* JADX WARNING: Removed duplicated region for block: B:486:0x0e0f A[SYNTHETIC] */
    /* JADX WARNING: Unknown variable types count: 2 */
    private void readPackageLPw(XmlPullParser parser) throws XmlPullParserException, IOException {
        String str;
        String str2;
        String parentPackageName;
        Settings settings;
        String updateAvailable;
        String secondaryCpuAbiString;
        String idStr;
        String idStr2;
        String cpuAbiOverrideString;
        int categoryHint;
        String str3;
        String legacyNativeLibraryPathStr;
        String name;
        String sharedIdStr;
        String installerPackageName;
        String uidError;
        PackageSetting packageSetting;
        Settings settings2;
        String maxAspectRatioStr;
        String minAspectRatioStr;
        String appUseNotchModeStr;
        String appUseSideModeStr;
        String systemStr;
        String forceDarkModeStr;
        int type;
        String legacyNativeLibraryPathStr2;
        String maxAspectRatioStr2;
        String enabledStr;
        int outerDepth;
        String str4;
        int categoryHint2;
        String legacyNativeLibraryPathStr3;
        String resourcePathStr;
        String uidError2;
        String primaryCpuAbiString;
        int i;
        String idStr3;
        String codePathStr;
        String secondaryCpuAbiString2;
        String idStr4;
        String idStr5;
        String legacyCpuAbiString;
        long timeStamp;
        long firstInstallTime;
        String timeStampStr;
        long lastUpdateTime;
        int userId;
        String legacyNativeLibraryPathStr4;
        String realName;
        String str5;
        String idStr6;
        long lastUpdateTime2;
        long firstInstallTime2;
        long timeStamp2;
        String resourcePathStr2;
        String name2;
        String codePathStr2;
        String str6;
        int i2;
        StringBuilder sb;
        String str7;
        String intern;
        File file;
        ?? file2;
        long lastUpdateTime3;
        StringBuilder sb2;
        int i3;
        String name3 = null;
        String realName2 = null;
        String sharedIdStr2 = null;
        String legacyNativeLibraryPathStr5 = null;
        String primaryCpuAbiString2 = null;
        String installerPackageName2 = null;
        String isOrphaned = null;
        String volumeUuid = null;
        String updateAvailable2 = null;
        int categoryHint3 = -1;
        int pkgFlags = 0;
        int pkgPrivateFlags = 0;
        long timeStamp3 = 0;
        PackageSetting packageSetting2 = null;
        long versionCode = 0;
        try {
            name3 = parser.getAttributeValue(null, ATTR_NAME);
            try {
                realName2 = parser.getAttributeValue(null, "realName");
                idStr5 = parser.getAttributeValue(null, "userId");
                try {
                    uidError2 = parser.getAttributeValue(null, "uidError");
                    try {
                        sharedIdStr2 = parser.getAttributeValue(null, "sharedUserId");
                        legacyNativeLibraryPathStr5 = parser.getAttributeValue(null, "codePath");
                        try {
                            resourcePathStr = parser.getAttributeValue(null, "resourcePath");
                            try {
                                legacyCpuAbiString = parser.getAttributeValue(null, "requiredCpuAbi");
                            } catch (NumberFormatException e) {
                                settings = this;
                                str2 = "true";
                                str3 = " at ";
                                idStr4 = " has bad userId ";
                                str = ATTR_NAME;
                                legacyNativeLibraryPathStr3 = null;
                                secondaryCpuAbiString2 = null;
                                codePathStr = null;
                                idStr3 = idStr5;
                                name = "Error in package manager settings: package ";
                                i = 5;
                                primaryCpuAbiString = null;
                                PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                parentPackageName = name3;
                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                idStr = idStr3;
                                idStr2 = primaryCpuAbiString;
                                installerPackageName = installerPackageName2;
                                sharedIdStr = isOrphaned;
                                legacyNativeLibraryPathStr = volumeUuid;
                                updateAvailable = updateAvailable2;
                                categoryHint = categoryHint3;
                                packageSetting = packageSetting2;
                                uidError = uidError2;
                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                if (packageSetting != null) {
                                }
                            }
                        } catch (NumberFormatException e2) {
                            settings = this;
                            str2 = "true";
                            str3 = " at ";
                            idStr4 = " has bad userId ";
                            str = ATTR_NAME;
                            resourcePathStr = null;
                            legacyNativeLibraryPathStr3 = null;
                            secondaryCpuAbiString2 = null;
                            codePathStr = null;
                            idStr3 = idStr5;
                            name = "Error in package manager settings: package ";
                            i = 5;
                            primaryCpuAbiString = null;
                            PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                            parentPackageName = name3;
                            secondaryCpuAbiString = secondaryCpuAbiString2;
                            idStr = idStr3;
                            idStr2 = primaryCpuAbiString;
                            installerPackageName = installerPackageName2;
                            sharedIdStr = isOrphaned;
                            legacyNativeLibraryPathStr = volumeUuid;
                            updateAvailable = updateAvailable2;
                            categoryHint = categoryHint3;
                            packageSetting = packageSetting2;
                            uidError = uidError2;
                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                            if (packageSetting != null) {
                            }
                        }
                    } catch (NumberFormatException e3) {
                        settings = this;
                        str2 = "true";
                        str3 = " at ";
                        idStr4 = " has bad userId ";
                        str = ATTR_NAME;
                        resourcePathStr = null;
                        legacyNativeLibraryPathStr3 = null;
                        secondaryCpuAbiString2 = null;
                        codePathStr = null;
                        idStr3 = idStr5;
                        name = "Error in package manager settings: package ";
                        i = 5;
                        primaryCpuAbiString = null;
                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                        parentPackageName = name3;
                        secondaryCpuAbiString = secondaryCpuAbiString2;
                        idStr = idStr3;
                        idStr2 = primaryCpuAbiString;
                        installerPackageName = installerPackageName2;
                        sharedIdStr = isOrphaned;
                        legacyNativeLibraryPathStr = volumeUuid;
                        updateAvailable = updateAvailable2;
                        categoryHint = categoryHint3;
                        packageSetting = packageSetting2;
                        uidError = uidError2;
                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                        if (packageSetting != null) {
                        }
                    }
                } catch (NumberFormatException e4) {
                    settings = this;
                    str2 = "true";
                    str3 = " at ";
                    idStr4 = " has bad userId ";
                    str = ATTR_NAME;
                    resourcePathStr = null;
                    uidError2 = null;
                    legacyNativeLibraryPathStr3 = null;
                    secondaryCpuAbiString2 = null;
                    codePathStr = null;
                    idStr3 = idStr5;
                    name = "Error in package manager settings: package ";
                    i = 5;
                    primaryCpuAbiString = null;
                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                    parentPackageName = name3;
                    secondaryCpuAbiString = secondaryCpuAbiString2;
                    idStr = idStr3;
                    idStr2 = primaryCpuAbiString;
                    installerPackageName = installerPackageName2;
                    sharedIdStr = isOrphaned;
                    legacyNativeLibraryPathStr = volumeUuid;
                    updateAvailable = updateAvailable2;
                    categoryHint = categoryHint3;
                    packageSetting = packageSetting2;
                    uidError = uidError2;
                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                    if (packageSetting != null) {
                    }
                }
            } catch (NumberFormatException e5) {
                settings = this;
                str2 = "true";
                str3 = " at ";
                str = ATTR_NAME;
                idStr3 = null;
                idStr4 = " has bad userId ";
                i = 5;
                resourcePathStr = null;
                uidError2 = null;
                legacyNativeLibraryPathStr3 = null;
                secondaryCpuAbiString2 = null;
                codePathStr = null;
                name = "Error in package manager settings: package ";
                primaryCpuAbiString = null;
                PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                parentPackageName = name3;
                secondaryCpuAbiString = secondaryCpuAbiString2;
                idStr = idStr3;
                idStr2 = primaryCpuAbiString;
                installerPackageName = installerPackageName2;
                sharedIdStr = isOrphaned;
                legacyNativeLibraryPathStr = volumeUuid;
                updateAvailable = updateAvailable2;
                categoryHint = categoryHint3;
                packageSetting = packageSetting2;
                uidError = uidError2;
                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                if (packageSetting != null) {
                }
            }
            try {
                String parentPackageName2 = parser.getAttributeValue(null, "parentPackageName");
                String legacyNativeLibraryPathStr6 = parser.getAttributeValue(null, "nativeLibraryPath");
                try {
                    primaryCpuAbiString2 = parser.getAttributeValue(null, "primaryCpuAbi");
                    secondaryCpuAbiString2 = parser.getAttributeValue(null, "secondaryCpuAbi");
                    try {
                        codePathStr = parser.getAttributeValue(null, "cpuAbiOverride");
                        try {
                            updateAvailable2 = parser.getAttributeValue(null, "updateAvailable");
                            if (primaryCpuAbiString2 != null || legacyCpuAbiString == null) {
                                primaryCpuAbiString = primaryCpuAbiString2;
                            } else {
                                primaryCpuAbiString = legacyCpuAbiString;
                            }
                            try {
                                String version = parser.getAttributeValue(null, "version");
                                if (version != null) {
                                    try {
                                        versionCode = Long.parseLong(version);
                                    } catch (NumberFormatException e6) {
                                    }
                                }
                                installerPackageName2 = parser.getAttributeValue(null, "installer");
                                isOrphaned = parser.getAttributeValue(null, "isOrphaned");
                                volumeUuid = parser.getAttributeValue(null, ATTR_VOLUME_UUID);
                                String categoryHintString = parser.getAttributeValue(null, "categoryHint");
                                if (categoryHintString != null) {
                                    try {
                                        categoryHint3 = Integer.parseInt(categoryHintString);
                                    } catch (NumberFormatException e7) {
                                    }
                                }
                                String systemStr2 = parser.getAttributeValue(null, "publicFlags");
                                if (systemStr2 != null) {
                                    try {
                                        pkgFlags = Integer.parseInt(systemStr2);
                                    } catch (NumberFormatException e8) {
                                    }
                                    try {
                                        String systemStr3 = parser.getAttributeValue(null, "privateFlags");
                                        if (systemStr3 != null) {
                                            try {
                                                pkgPrivateFlags = Integer.parseInt(systemStr3);
                                            } catch (NumberFormatException e9) {
                                            }
                                        }
                                    } catch (NumberFormatException e10) {
                                        settings = this;
                                        str2 = "true";
                                        legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                        name = "Error in package manager settings: package ";
                                        str3 = " at ";
                                        str = ATTR_NAME;
                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                        idStr3 = idStr5;
                                        idStr4 = " has bad userId ";
                                        i = 5;
                                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                        parentPackageName = name3;
                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                        idStr = idStr3;
                                        idStr2 = primaryCpuAbiString;
                                        installerPackageName = installerPackageName2;
                                        sharedIdStr = isOrphaned;
                                        legacyNativeLibraryPathStr = volumeUuid;
                                        updateAvailable = updateAvailable2;
                                        categoryHint = categoryHint3;
                                        packageSetting = packageSetting2;
                                        uidError = uidError2;
                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                        if (packageSetting != null) {
                                        }
                                    }
                                } else {
                                    String systemStr4 = parser.getAttributeValue(null, ATTR_FLAGS);
                                    if (systemStr4 != null) {
                                        try {
                                            pkgFlags = Integer.parseInt(systemStr4);
                                        } catch (NumberFormatException e11) {
                                        }
                                        if ((pkgFlags & PRE_M_APP_INFO_FLAG_HIDDEN) != 0) {
                                            pkgPrivateFlags = 0 | 1;
                                        }
                                        if ((pkgFlags & PRE_M_APP_INFO_FLAG_CANT_SAVE_STATE) != 0) {
                                            pkgPrivateFlags |= 2;
                                        }
                                        if ((pkgFlags & PRE_M_APP_INFO_FLAG_PRIVILEGED) != 0) {
                                            pkgPrivateFlags |= 8;
                                        }
                                        pkgFlags &= ~(PRE_M_APP_INFO_FLAG_HIDDEN | PRE_M_APP_INFO_FLAG_CANT_SAVE_STATE | PRE_M_APP_INFO_FLAG_PRIVILEGED);
                                    } else {
                                        String systemStr5 = parser.getAttributeValue(null, "system");
                                        if (systemStr5 != null) {
                                            try {
                                                if ("true".equalsIgnoreCase(systemStr5)) {
                                                    i3 = 1;
                                                } else {
                                                    i3 = 0;
                                                }
                                                pkgFlags = 0 | i3;
                                            } catch (NumberFormatException e12) {
                                                str2 = "true";
                                                legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                                name = "Error in package manager settings: package ";
                                                str3 = " at ";
                                                str = ATTR_NAME;
                                                legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                settings = this;
                                                idStr3 = idStr5;
                                                idStr4 = " has bad userId ";
                                                i = 5;
                                                PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                parentPackageName = name3;
                                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                                idStr = idStr3;
                                                idStr2 = primaryCpuAbiString;
                                                installerPackageName = installerPackageName2;
                                                sharedIdStr = isOrphaned;
                                                legacyNativeLibraryPathStr = volumeUuid;
                                                updateAvailable = updateAvailable2;
                                                categoryHint = categoryHint3;
                                                packageSetting = packageSetting2;
                                                uidError = uidError2;
                                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                if (packageSetting != null) {
                                                }
                                            }
                                        } else {
                                            pkgFlags = 0 | 1;
                                        }
                                    }
                                }
                                String timeStampStr2 = parser.getAttributeValue(null, "ft");
                                if (timeStampStr2 != null) {
                                    try {
                                        timeStamp3 = Long.parseLong(timeStampStr2, 16);
                                    } catch (NumberFormatException e13) {
                                    }
                                    timeStamp = timeStamp3;
                                } else {
                                    try {
                                        String timeStampStr3 = parser.getAttributeValue(null, "ts");
                                        if (timeStampStr3 != null) {
                                            try {
                                                timeStamp = Long.parseLong(timeStampStr3);
                                            } catch (NumberFormatException e14) {
                                            }
                                        }
                                        timeStamp = 0;
                                    } catch (NumberFormatException e15) {
                                        settings = this;
                                        str3 = " at ";
                                        idStr4 = " has bad userId ";
                                        str = ATTR_NAME;
                                        str2 = "true";
                                        legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                        idStr3 = idStr5;
                                        name = "Error in package manager settings: package ";
                                        i = 5;
                                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                        parentPackageName = name3;
                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                        idStr = idStr3;
                                        idStr2 = primaryCpuAbiString;
                                        installerPackageName = installerPackageName2;
                                        sharedIdStr = isOrphaned;
                                        legacyNativeLibraryPathStr = volumeUuid;
                                        updateAvailable = updateAvailable2;
                                        categoryHint = categoryHint3;
                                        packageSetting = packageSetting2;
                                        uidError = uidError2;
                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                        if (packageSetting != null) {
                                        }
                                    }
                                }
                                try {
                                    String timeStampStr4 = parser.getAttributeValue(null, "it");
                                    if (timeStampStr4 != null) {
                                        try {
                                            firstInstallTime = Long.parseLong(timeStampStr4, 16);
                                        } catch (NumberFormatException e16) {
                                        }
                                        timeStampStr = parser.getAttributeValue(null, "ut");
                                        if (timeStampStr != null) {
                                            try {
                                                lastUpdateTime = Long.parseLong(timeStampStr, 16);
                                            } catch (NumberFormatException e17) {
                                            }
                                            if (PackageManagerService.DEBUG_SETTINGS) {
                                                try {
                                                    Log.v("PackageManager", "Reading package: " + name3 + " userId=" + idStr5 + " sharedUserId=" + sharedIdStr2);
                                                } catch (NumberFormatException e18) {
                                                    settings = this;
                                                    legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                                    name = "Error in package manager settings: package ";
                                                    str3 = " at ";
                                                    str = ATTR_NAME;
                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                    str2 = "true";
                                                    idStr3 = idStr5;
                                                    idStr4 = " has bad userId ";
                                                    i = 5;
                                                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                    parentPackageName = name3;
                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                    idStr = idStr3;
                                                    idStr2 = primaryCpuAbiString;
                                                    installerPackageName = installerPackageName2;
                                                    sharedIdStr = isOrphaned;
                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                    updateAvailable = updateAvailable2;
                                                    categoryHint = categoryHint3;
                                                    packageSetting = packageSetting2;
                                                    uidError = uidError2;
                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                    if (packageSetting != null) {
                                                    }
                                                }
                                            }
                                            if (idStr5 != null) {
                                                userId = Integer.parseInt(idStr5);
                                            } else {
                                                userId = 0;
                                            }
                                            int sharedUserId = sharedIdStr2 != null ? Integer.parseInt(sharedIdStr2) : 0;
                                            if (resourcePathStr == null) {
                                                legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                                legacyNativeLibraryPathStr4 = legacyNativeLibraryPathStr5;
                                            } else {
                                                legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                                legacyNativeLibraryPathStr4 = resourcePathStr;
                                            }
                                            if (realName2 != null) {
                                                try {
                                                    realName = realName2.intern();
                                                } catch (NumberFormatException e19) {
                                                    settings = this;
                                                    resourcePathStr = legacyNativeLibraryPathStr4;
                                                    name = "Error in package manager settings: package ";
                                                    str3 = " at ";
                                                    str = ATTR_NAME;
                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                    str2 = "true";
                                                    idStr3 = idStr5;
                                                    idStr4 = " has bad userId ";
                                                    i = 5;
                                                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                    parentPackageName = name3;
                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                    idStr = idStr3;
                                                    idStr2 = primaryCpuAbiString;
                                                    installerPackageName = installerPackageName2;
                                                    sharedIdStr = isOrphaned;
                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                    updateAvailable = updateAvailable2;
                                                    categoryHint = categoryHint3;
                                                    packageSetting = packageSetting2;
                                                    uidError = uidError2;
                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                    if (packageSetting != null) {
                                                    }
                                                }
                                            } else {
                                                realName = realName2;
                                            }
                                            if (name3 == null) {
                                                try {
                                                    try {
                                                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <package> has no name at " + parser.getPositionDescription());
                                                        settings = this;
                                                        name2 = name3;
                                                        idStr6 = idStr5;
                                                        str5 = "Error in package manager settings: package ";
                                                        codePathStr2 = " at ";
                                                        str = ATTR_NAME;
                                                        str2 = "true";
                                                        timeStamp2 = timeStamp;
                                                        firstInstallTime2 = firstInstallTime;
                                                        lastUpdateTime2 = lastUpdateTime;
                                                        resourcePathStr2 = legacyNativeLibraryPathStr3;
                                                    } catch (NumberFormatException e20) {
                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                        name = "Error in package manager settings: package ";
                                                        str3 = " at ";
                                                        str = ATTR_NAME;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        str2 = "true";
                                                        idStr3 = idStr5;
                                                        idStr4 = " has bad userId ";
                                                        i = 5;
                                                        settings = this;
                                                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                        parentPackageName = name3;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        idStr = idStr3;
                                                        idStr2 = primaryCpuAbiString;
                                                        installerPackageName = installerPackageName2;
                                                        sharedIdStr = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                } catch (NumberFormatException e21) {
                                                    settings = this;
                                                    resourcePathStr = legacyNativeLibraryPathStr4;
                                                    name = "Error in package manager settings: package ";
                                                    str3 = " at ";
                                                    str = ATTR_NAME;
                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                    str2 = "true";
                                                    idStr3 = idStr5;
                                                    idStr4 = " has bad userId ";
                                                    i = 5;
                                                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                    parentPackageName = name3;
                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                    idStr = idStr3;
                                                    idStr2 = primaryCpuAbiString;
                                                    installerPackageName = installerPackageName2;
                                                    sharedIdStr = isOrphaned;
                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                    updateAvailable = updateAvailable2;
                                                    categoryHint = categoryHint3;
                                                    packageSetting = packageSetting2;
                                                    uidError = uidError2;
                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                    if (packageSetting != null) {
                                                    }
                                                }
                                            } else if (legacyNativeLibraryPathStr5 == null) {
                                                try {
                                                    sb2 = new StringBuilder();
                                                } catch (NumberFormatException e22) {
                                                    settings = this;
                                                    resourcePathStr = legacyNativeLibraryPathStr4;
                                                    name = "Error in package manager settings: package ";
                                                    str3 = " at ";
                                                    idStr4 = " has bad userId ";
                                                    str = ATTR_NAME;
                                                    idStr3 = idStr5;
                                                    str2 = "true";
                                                    i = 5;
                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                    parentPackageName = name3;
                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                    idStr = idStr3;
                                                    idStr2 = primaryCpuAbiString;
                                                    installerPackageName = installerPackageName2;
                                                    sharedIdStr = isOrphaned;
                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                    updateAvailable = updateAvailable2;
                                                    categoryHint = categoryHint3;
                                                    packageSetting = packageSetting2;
                                                    uidError = uidError2;
                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                    if (packageSetting != null) {
                                                    }
                                                }
                                                try {
                                                    sb2.append("Error in package manager settings: <package> has no codePath at ");
                                                    sb2.append(parser.getPositionDescription());
                                                    try {
                                                        PackageManagerService.reportSettingsProblem(5, sb2.toString());
                                                        name2 = name3;
                                                        str5 = "Error in package manager settings: package ";
                                                        str = ATTR_NAME;
                                                        idStr6 = idStr5;
                                                        str2 = "true";
                                                        firstInstallTime2 = firstInstallTime;
                                                        lastUpdateTime2 = lastUpdateTime;
                                                        resourcePathStr2 = legacyNativeLibraryPathStr3;
                                                        settings = this;
                                                        codePathStr2 = " at ";
                                                        timeStamp2 = timeStamp;
                                                    } catch (NumberFormatException e23) {
                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                        name = "Error in package manager settings: package ";
                                                        str3 = " at ";
                                                        idStr4 = " has bad userId ";
                                                        str = ATTR_NAME;
                                                        i = 5;
                                                        idStr3 = idStr5;
                                                        str2 = "true";
                                                        settings = this;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                        parentPackageName = name3;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        idStr = idStr3;
                                                        idStr2 = primaryCpuAbiString;
                                                        installerPackageName = installerPackageName2;
                                                        sharedIdStr = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                } catch (NumberFormatException e24) {
                                                    settings = this;
                                                    resourcePathStr = legacyNativeLibraryPathStr4;
                                                    name = "Error in package manager settings: package ";
                                                    str3 = " at ";
                                                    idStr4 = " has bad userId ";
                                                    str = ATTR_NAME;
                                                    idStr3 = idStr5;
                                                    str2 = "true";
                                                    i = 5;
                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                    parentPackageName = name3;
                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                    idStr = idStr3;
                                                    idStr2 = primaryCpuAbiString;
                                                    installerPackageName = installerPackageName2;
                                                    sharedIdStr = isOrphaned;
                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                    updateAvailable = updateAvailable2;
                                                    categoryHint = categoryHint3;
                                                    packageSetting = packageSetting2;
                                                    uidError = uidError2;
                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                    if (packageSetting != null) {
                                                    }
                                                }
                                            } else if (userId > 0) {
                                                try {
                                                    intern = name3.intern();
                                                    try {
                                                        file = new File(legacyNativeLibraryPathStr5);
                                                        file2 = new File(legacyNativeLibraryPathStr4);
                                                        idStr6 = idStr5;
                                                        str2 = "true";
                                                        str5 = "Error in package manager settings: package ";
                                                        str = ATTR_NAME;
                                                        resourcePathStr2 = legacyNativeLibraryPathStr3;
                                                    } catch (NumberFormatException e25) {
                                                        str = ATTR_NAME;
                                                        str2 = "true";
                                                        settings = this;
                                                        name = "Error in package manager settings: package ";
                                                        i = 5;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        idStr3 = idStr5;
                                                        sharedIdStr2 = sharedIdStr2;
                                                        str3 = " at ";
                                                        idStr4 = " has bad userId ";
                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                        parentPackageName = name3;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        idStr = idStr3;
                                                        idStr2 = primaryCpuAbiString;
                                                        installerPackageName = installerPackageName2;
                                                        sharedIdStr = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                } catch (NumberFormatException e26) {
                                                    str = ATTR_NAME;
                                                    str2 = "true";
                                                    settings = this;
                                                    name = "Error in package manager settings: package ";
                                                    i = 5;
                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                    idStr3 = idStr5;
                                                    str3 = " at ";
                                                    idStr4 = " has bad userId ";
                                                    resourcePathStr = legacyNativeLibraryPathStr4;
                                                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                    parentPackageName = name3;
                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                    idStr = idStr3;
                                                    idStr2 = primaryCpuAbiString;
                                                    installerPackageName = installerPackageName2;
                                                    sharedIdStr = isOrphaned;
                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                    updateAvailable = updateAvailable2;
                                                    categoryHint = categoryHint3;
                                                    packageSetting = packageSetting2;
                                                    uidError = uidError2;
                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                    if (packageSetting != null) {
                                                    }
                                                }
                                                try {
                                                    PackageSetting packageSetting3 = addPackageLPw(intern, realName, file, file2, resourcePathStr2, primaryCpuAbiString, secondaryCpuAbiString2, codePathStr, userId, versionCode, pkgFlags, pkgPrivateFlags, parentPackageName2, null, null, null);
                                                    try {
                                                        if (PackageManagerService.DEBUG_SETTINGS) {
                                                            try {
                                                                ?? sb3 = new StringBuilder();
                                                                sb3.append("Reading package ");
                                                                file2 = name3;
                                                                try {
                                                                    sb3.append(file2);
                                                                    sb3.append(": userId=");
                                                                    sb3.append(userId);
                                                                    sb3.append(" pkg=");
                                                                    sb3.append(packageSetting3);
                                                                    Log.i("PackageManager", sb3.toString());
                                                                    name2 = file2;
                                                                } catch (NumberFormatException e27) {
                                                                    i = 5;
                                                                    settings = this;
                                                                    packageSetting2 = packageSetting3;
                                                                    name3 = file2;
                                                                    legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                    idStr3 = idStr6;
                                                                    sharedIdStr2 = sharedIdStr2;
                                                                    name = str5;
                                                                    str3 = " at ";
                                                                    idStr4 = " has bad userId ";
                                                                    resourcePathStr = legacyNativeLibraryPathStr4;
                                                                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                                    parentPackageName = name3;
                                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                    idStr = idStr3;
                                                                    idStr2 = primaryCpuAbiString;
                                                                    installerPackageName = installerPackageName2;
                                                                    sharedIdStr = isOrphaned;
                                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                                    updateAvailable = updateAvailable2;
                                                                    categoryHint = categoryHint3;
                                                                    packageSetting = packageSetting2;
                                                                    uidError = uidError2;
                                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                    if (packageSetting != null) {
                                                                    }
                                                                }
                                                            } catch (NumberFormatException e28) {
                                                                i = 5;
                                                                settings = this;
                                                                packageSetting2 = packageSetting3;
                                                                name3 = name3;
                                                                legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                idStr3 = idStr6;
                                                                sharedIdStr2 = sharedIdStr2;
                                                                name = str5;
                                                                str3 = " at ";
                                                                idStr4 = " has bad userId ";
                                                                resourcePathStr = legacyNativeLibraryPathStr4;
                                                                PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                                parentPackageName = name3;
                                                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                idStr = idStr3;
                                                                idStr2 = primaryCpuAbiString;
                                                                installerPackageName = installerPackageName2;
                                                                sharedIdStr = isOrphaned;
                                                                legacyNativeLibraryPathStr = volumeUuid;
                                                                updateAvailable = updateAvailable2;
                                                                categoryHint = categoryHint3;
                                                                packageSetting = packageSetting2;
                                                                uidError = uidError2;
                                                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                if (packageSetting != null) {
                                                                }
                                                            }
                                                        } else {
                                                            name2 = name3;
                                                        }
                                                        if (packageSetting3 == null) {
                                                            PackageManagerService.reportSettingsProblem(6, "Failure adding uid " + userId + " while parsing settings at " + parser.getPositionDescription());
                                                            timeStamp2 = timeStamp;
                                                            firstInstallTime2 = firstInstallTime;
                                                            lastUpdateTime3 = lastUpdateTime;
                                                        } else {
                                                            timeStamp2 = timeStamp;
                                                            try {
                                                                packageSetting3.setTimeStamp(timeStamp2);
                                                                firstInstallTime2 = firstInstallTime;
                                                                try {
                                                                    packageSetting3.firstInstallTime = firstInstallTime2;
                                                                    lastUpdateTime3 = lastUpdateTime;
                                                                    try {
                                                                        packageSetting3.lastUpdateTime = lastUpdateTime3;
                                                                    } catch (NumberFormatException e29) {
                                                                        settings = this;
                                                                        packageSetting2 = packageSetting3;
                                                                        name3 = name2;
                                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                        idStr3 = idStr6;
                                                                        sharedIdStr2 = sharedIdStr2;
                                                                        name = str5;
                                                                        str3 = " at ";
                                                                        idStr4 = " has bad userId ";
                                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                                        i = 5;
                                                                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                                        parentPackageName = name3;
                                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                        idStr = idStr3;
                                                                        idStr2 = primaryCpuAbiString;
                                                                        installerPackageName = installerPackageName2;
                                                                        sharedIdStr = isOrphaned;
                                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                                        updateAvailable = updateAvailable2;
                                                                        categoryHint = categoryHint3;
                                                                        packageSetting = packageSetting2;
                                                                        uidError = uidError2;
                                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                        if (packageSetting != null) {
                                                                        }
                                                                    }
                                                                } catch (NumberFormatException e30) {
                                                                    settings = this;
                                                                    packageSetting2 = packageSetting3;
                                                                    name3 = name2;
                                                                    legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                    idStr3 = idStr6;
                                                                    sharedIdStr2 = sharedIdStr2;
                                                                    name = str5;
                                                                    str3 = " at ";
                                                                    idStr4 = " has bad userId ";
                                                                    resourcePathStr = legacyNativeLibraryPathStr4;
                                                                    i = 5;
                                                                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                                    parentPackageName = name3;
                                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                    idStr = idStr3;
                                                                    idStr2 = primaryCpuAbiString;
                                                                    installerPackageName = installerPackageName2;
                                                                    sharedIdStr = isOrphaned;
                                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                                    updateAvailable = updateAvailable2;
                                                                    categoryHint = categoryHint3;
                                                                    packageSetting = packageSetting2;
                                                                    uidError = uidError2;
                                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                    if (packageSetting != null) {
                                                                    }
                                                                }
                                                            } catch (NumberFormatException e31) {
                                                                settings = this;
                                                                packageSetting2 = packageSetting3;
                                                                name3 = name2;
                                                                legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                idStr3 = idStr6;
                                                                sharedIdStr2 = sharedIdStr2;
                                                                name = str5;
                                                                str3 = " at ";
                                                                idStr4 = " has bad userId ";
                                                                resourcePathStr = legacyNativeLibraryPathStr4;
                                                                i = 5;
                                                                PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                                parentPackageName = name3;
                                                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                idStr = idStr3;
                                                                idStr2 = primaryCpuAbiString;
                                                                installerPackageName = installerPackageName2;
                                                                sharedIdStr = isOrphaned;
                                                                legacyNativeLibraryPathStr = volumeUuid;
                                                                updateAvailable = updateAvailable2;
                                                                categoryHint = categoryHint3;
                                                                packageSetting = packageSetting2;
                                                                uidError = uidError2;
                                                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                if (packageSetting != null) {
                                                                }
                                                            }
                                                        }
                                                        settings = this;
                                                        packageSetting2 = packageSetting3;
                                                        lastUpdateTime2 = lastUpdateTime3;
                                                        sharedIdStr2 = sharedIdStr2;
                                                        codePathStr2 = " at ";
                                                    } catch (NumberFormatException e32) {
                                                        settings = this;
                                                        packageSetting2 = packageSetting3;
                                                        name3 = name3;
                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        idStr3 = idStr6;
                                                        sharedIdStr2 = sharedIdStr2;
                                                        name = str5;
                                                        str3 = " at ";
                                                        idStr4 = " has bad userId ";
                                                        resourcePathStr = legacyNativeLibraryPathStr4;
                                                        i = 5;
                                                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                        parentPackageName = name3;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        idStr = idStr3;
                                                        idStr2 = primaryCpuAbiString;
                                                        installerPackageName = installerPackageName2;
                                                        sharedIdStr = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                } catch (NumberFormatException e33) {
                                                    settings = this;
                                                    name3 = name3;
                                                    legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                    idStr3 = idStr6;
                                                    sharedIdStr2 = sharedIdStr2;
                                                    name = str5;
                                                    str3 = " at ";
                                                    idStr4 = " has bad userId ";
                                                    resourcePathStr = legacyNativeLibraryPathStr4;
                                                    i = 5;
                                                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                    parentPackageName = name3;
                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                    idStr = idStr3;
                                                    idStr2 = primaryCpuAbiString;
                                                    installerPackageName = installerPackageName2;
                                                    sharedIdStr = isOrphaned;
                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                    updateAvailable = updateAvailable2;
                                                    categoryHint = categoryHint3;
                                                    packageSetting = packageSetting2;
                                                    uidError = uidError2;
                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                    if (packageSetting != null) {
                                                    }
                                                }
                                            } else {
                                                String resourcePathStr3 = legacyNativeLibraryPathStr4;
                                                str5 = "Error in package manager settings: package ";
                                                str = ATTR_NAME;
                                                idStr6 = idStr5;
                                                str2 = "true";
                                                timeStamp2 = timeStamp;
                                                firstInstallTime2 = firstInstallTime;
                                                resourcePathStr2 = legacyNativeLibraryPathStr3;
                                                name2 = name3;
                                                if (sharedIdStr2 == null) {
                                                    settings = this;
                                                    lastUpdateTime2 = lastUpdateTime;
                                                    str6 = str5;
                                                    codePathStr2 = " at ";
                                                    try {
                                                        StringBuilder sb4 = new StringBuilder();
                                                        sb4.append(str6);
                                                        sb4.append(name2);
                                                        idStr4 = " has bad userId ";
                                                        try {
                                                            sb4.append(idStr4);
                                                            str5 = str6;
                                                            try {
                                                                sb4.append(idStr6);
                                                                sb4.append(codePathStr2);
                                                                idStr6 = idStr6;
                                                                try {
                                                                    sb4.append(parser.getPositionDescription());
                                                                    i2 = 5;
                                                                    try {
                                                                        PackageManagerService.reportSettingsProblem(5, sb4.toString());
                                                                    } catch (NumberFormatException e34) {
                                                                        str3 = codePathStr2;
                                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                        idStr3 = idStr6;
                                                                        resourcePathStr = resourcePathStr3;
                                                                        i = i2;
                                                                        name3 = name2;
                                                                        name = str5;
                                                                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                                        parentPackageName = name3;
                                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                        idStr = idStr3;
                                                                        idStr2 = primaryCpuAbiString;
                                                                        installerPackageName = installerPackageName2;
                                                                        sharedIdStr = isOrphaned;
                                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                                        updateAvailable = updateAvailable2;
                                                                        categoryHint = categoryHint3;
                                                                        packageSetting = packageSetting2;
                                                                        uidError = uidError2;
                                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                        if (packageSetting != null) {
                                                                        }
                                                                    }
                                                                } catch (NumberFormatException e35) {
                                                                    str3 = codePathStr2;
                                                                    name3 = name2;
                                                                    legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                    idStr3 = idStr6;
                                                                    name = str5;
                                                                    resourcePathStr = resourcePathStr3;
                                                                    i = 5;
                                                                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                                    parentPackageName = name3;
                                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                    idStr = idStr3;
                                                                    idStr2 = primaryCpuAbiString;
                                                                    installerPackageName = installerPackageName2;
                                                                    sharedIdStr = isOrphaned;
                                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                                    updateAvailable = updateAvailable2;
                                                                    categoryHint = categoryHint3;
                                                                    packageSetting = packageSetting2;
                                                                    uidError = uidError2;
                                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                    if (packageSetting != null) {
                                                                    }
                                                                }
                                                            } catch (NumberFormatException e36) {
                                                                str3 = codePathStr2;
                                                                name3 = name2;
                                                                legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                idStr3 = idStr6;
                                                                name = str5;
                                                                resourcePathStr = resourcePathStr3;
                                                                i = 5;
                                                                PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                                parentPackageName = name3;
                                                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                idStr = idStr3;
                                                                idStr2 = primaryCpuAbiString;
                                                                installerPackageName = installerPackageName2;
                                                                sharedIdStr = isOrphaned;
                                                                legacyNativeLibraryPathStr = volumeUuid;
                                                                updateAvailable = updateAvailable2;
                                                                categoryHint = categoryHint3;
                                                                packageSetting = packageSetting2;
                                                                uidError = uidError2;
                                                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                if (packageSetting != null) {
                                                                }
                                                            }
                                                        } catch (NumberFormatException e37) {
                                                            str3 = codePathStr2;
                                                            legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                            idStr3 = idStr6;
                                                            resourcePathStr = resourcePathStr3;
                                                            i = 5;
                                                            name = str6;
                                                            name3 = name2;
                                                            PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                            parentPackageName = name3;
                                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                                            idStr = idStr3;
                                                            idStr2 = primaryCpuAbiString;
                                                            installerPackageName = installerPackageName2;
                                                            sharedIdStr = isOrphaned;
                                                            legacyNativeLibraryPathStr = volumeUuid;
                                                            updateAvailable = updateAvailable2;
                                                            categoryHint = categoryHint3;
                                                            packageSetting = packageSetting2;
                                                            uidError = uidError2;
                                                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                            if (packageSetting != null) {
                                                            }
                                                        }
                                                    } catch (NumberFormatException e38) {
                                                        str5 = str6;
                                                        i2 = 5;
                                                        idStr4 = " has bad userId ";
                                                        str3 = codePathStr2;
                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        idStr3 = idStr6;
                                                        resourcePathStr = resourcePathStr3;
                                                        i = i2;
                                                        name3 = name2;
                                                        name = str5;
                                                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                        parentPackageName = name3;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        idStr = idStr3;
                                                        idStr2 = primaryCpuAbiString;
                                                        installerPackageName = installerPackageName2;
                                                        sharedIdStr = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                } else if (sharedUserId > 0) {
                                                    try {
                                                    } catch (NumberFormatException e39) {
                                                        settings = this;
                                                        resourcePathStr = resourcePathStr3;
                                                        name3 = name2;
                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        idStr3 = idStr6;
                                                        name = str5;
                                                        str3 = " at ";
                                                        idStr4 = " has bad userId ";
                                                        i = 5;
                                                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                        parentPackageName = name3;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        idStr = idStr3;
                                                        idStr2 = primaryCpuAbiString;
                                                        installerPackageName = installerPackageName2;
                                                        sharedIdStr = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                    try {
                                                        try {
                                                            try {
                                                                PackageSetting packageSetting4 = new PackageSetting(name2.intern(), realName, new File(legacyNativeLibraryPathStr5), new File(resourcePathStr3), resourcePathStr2, primaryCpuAbiString, secondaryCpuAbiString2, codePathStr, versionCode, pkgFlags, pkgPrivateFlags, parentPackageName2, null, sharedUserId, null, null);
                                                                try {
                                                                    packageSetting4.setTimeStamp(timeStamp2);
                                                                    packageSetting4.firstInstallTime = firstInstallTime2;
                                                                    packageSetting4.lastUpdateTime = lastUpdateTime;
                                                                    settings = this;
                                                                    try {
                                                                        settings.mPendingPackages.add(packageSetting4);
                                                                        if (PackageManagerService.DEBUG_SETTINGS) {
                                                                            StringBuilder sb5 = new StringBuilder();
                                                                            lastUpdateTime2 = lastUpdateTime;
                                                                            try {
                                                                                sb5.append("Reading package ");
                                                                                sb5.append(name2);
                                                                                sb5.append(": sharedUserId=");
                                                                                sb5.append(sharedUserId);
                                                                                sb5.append(" pkg=");
                                                                                sb5.append(packageSetting4);
                                                                                Log.i("PackageManager", sb5.toString());
                                                                            } catch (NumberFormatException e40) {
                                                                                resourcePathStr = resourcePathStr3;
                                                                                packageSetting2 = packageSetting4;
                                                                                name3 = name2;
                                                                                legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                                legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                                idStr3 = idStr6;
                                                                                sharedIdStr2 = sharedIdStr2;
                                                                                name = str5;
                                                                                str3 = " at ";
                                                                                idStr4 = " has bad userId ";
                                                                                i = 5;
                                                                            }
                                                                        } else {
                                                                            lastUpdateTime2 = lastUpdateTime;
                                                                        }
                                                                        packageSetting2 = packageSetting4;
                                                                        sharedIdStr2 = sharedIdStr2;
                                                                        codePathStr2 = " at ";
                                                                    } catch (NumberFormatException e41) {
                                                                        resourcePathStr = resourcePathStr3;
                                                                        packageSetting2 = packageSetting4;
                                                                        name3 = name2;
                                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                        idStr3 = idStr6;
                                                                        sharedIdStr2 = sharedIdStr2;
                                                                        name = str5;
                                                                        str3 = " at ";
                                                                        idStr4 = " has bad userId ";
                                                                        i = 5;
                                                                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                                        parentPackageName = name3;
                                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                        idStr = idStr3;
                                                                        idStr2 = primaryCpuAbiString;
                                                                        installerPackageName = installerPackageName2;
                                                                        sharedIdStr = isOrphaned;
                                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                                        updateAvailable = updateAvailable2;
                                                                        categoryHint = categoryHint3;
                                                                        packageSetting = packageSetting2;
                                                                        uidError = uidError2;
                                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                        if (packageSetting != null) {
                                                                        }
                                                                    }
                                                                } catch (NumberFormatException e42) {
                                                                    settings = this;
                                                                    resourcePathStr = resourcePathStr3;
                                                                    packageSetting2 = packageSetting4;
                                                                    name3 = name2;
                                                                    legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                    idStr3 = idStr6;
                                                                    sharedIdStr2 = sharedIdStr2;
                                                                    name = str5;
                                                                    str3 = " at ";
                                                                    idStr4 = " has bad userId ";
                                                                    i = 5;
                                                                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                                    parentPackageName = name3;
                                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                    idStr = idStr3;
                                                                    idStr2 = primaryCpuAbiString;
                                                                    installerPackageName = installerPackageName2;
                                                                    sharedIdStr = isOrphaned;
                                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                                    updateAvailable = updateAvailable2;
                                                                    categoryHint = categoryHint3;
                                                                    packageSetting = packageSetting2;
                                                                    uidError = uidError2;
                                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                    if (packageSetting != null) {
                                                                    }
                                                                }
                                                            } catch (NumberFormatException e43) {
                                                                settings = this;
                                                                resourcePathStr = resourcePathStr3;
                                                                name3 = name2;
                                                                legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                idStr3 = idStr6;
                                                                sharedIdStr2 = sharedIdStr2;
                                                                name = str5;
                                                                str3 = " at ";
                                                                idStr4 = " has bad userId ";
                                                                i = 5;
                                                                PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                                parentPackageName = name3;
                                                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                idStr = idStr3;
                                                                idStr2 = primaryCpuAbiString;
                                                                installerPackageName = installerPackageName2;
                                                                sharedIdStr = isOrphaned;
                                                                legacyNativeLibraryPathStr = volumeUuid;
                                                                updateAvailable = updateAvailable2;
                                                                categoryHint = categoryHint3;
                                                                packageSetting = packageSetting2;
                                                                uidError = uidError2;
                                                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                if (packageSetting != null) {
                                                                }
                                                            }
                                                        } catch (NumberFormatException e44) {
                                                            settings = this;
                                                            resourcePathStr = resourcePathStr3;
                                                            name3 = name2;
                                                            legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                            idStr3 = idStr6;
                                                            sharedIdStr2 = sharedIdStr2;
                                                            name = str5;
                                                            str3 = " at ";
                                                            idStr4 = " has bad userId ";
                                                            i = 5;
                                                            PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                            parentPackageName = name3;
                                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                                            idStr = idStr3;
                                                            idStr2 = primaryCpuAbiString;
                                                            installerPackageName = installerPackageName2;
                                                            sharedIdStr = isOrphaned;
                                                            legacyNativeLibraryPathStr = volumeUuid;
                                                            updateAvailable = updateAvailable2;
                                                            categoryHint = categoryHint3;
                                                            packageSetting = packageSetting2;
                                                            uidError = uidError2;
                                                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                            if (packageSetting != null) {
                                                            }
                                                        }
                                                    } catch (NumberFormatException e45) {
                                                        settings = this;
                                                        resourcePathStr = resourcePathStr3;
                                                        name3 = name2;
                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        idStr3 = idStr6;
                                                        sharedIdStr2 = sharedIdStr2;
                                                        name = str5;
                                                        str3 = " at ";
                                                        idStr4 = " has bad userId ";
                                                        i = 5;
                                                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                        parentPackageName = name3;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        idStr = idStr3;
                                                        idStr2 = primaryCpuAbiString;
                                                        installerPackageName = installerPackageName2;
                                                        sharedIdStr = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                } else {
                                                    settings = this;
                                                    lastUpdateTime2 = lastUpdateTime;
                                                    try {
                                                        sb = new StringBuilder();
                                                        str6 = str5;
                                                        try {
                                                            sb.append(str6);
                                                            sb.append(name2);
                                                            sb.append(" has bad sharedId ");
                                                            sharedIdStr2 = sharedIdStr2;
                                                        } catch (NumberFormatException e46) {
                                                            sharedIdStr2 = sharedIdStr2;
                                                            legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                            idStr3 = idStr6;
                                                            str3 = " at ";
                                                            idStr4 = " has bad userId ";
                                                            resourcePathStr = resourcePathStr3;
                                                            i = 5;
                                                            name = str6;
                                                            name3 = name2;
                                                            PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                            parentPackageName = name3;
                                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                                            idStr = idStr3;
                                                            idStr2 = primaryCpuAbiString;
                                                            installerPackageName = installerPackageName2;
                                                            sharedIdStr = isOrphaned;
                                                            legacyNativeLibraryPathStr = volumeUuid;
                                                            updateAvailable = updateAvailable2;
                                                            categoryHint = categoryHint3;
                                                            packageSetting = packageSetting2;
                                                            uidError = uidError2;
                                                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                            if (packageSetting != null) {
                                                            }
                                                        }
                                                    } catch (NumberFormatException e47) {
                                                        sharedIdStr2 = sharedIdStr2;
                                                        name3 = name2;
                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        idStr3 = idStr6;
                                                        name = str5;
                                                        str3 = " at ";
                                                        idStr4 = " has bad userId ";
                                                        resourcePathStr = resourcePathStr3;
                                                        i = 5;
                                                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                        parentPackageName = name3;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        idStr = idStr3;
                                                        idStr2 = primaryCpuAbiString;
                                                        installerPackageName = installerPackageName2;
                                                        sharedIdStr = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                    try {
                                                        sb.append(sharedIdStr2);
                                                        codePathStr2 = " at ";
                                                        try {
                                                            sb.append(codePathStr2);
                                                            resourcePathStr3 = resourcePathStr3;
                                                            try {
                                                                sb.append(parser.getPositionDescription());
                                                                try {
                                                                    PackageManagerService.reportSettingsProblem(5, sb.toString());
                                                                    str5 = str6;
                                                                } catch (NumberFormatException e48) {
                                                                    str3 = codePathStr2;
                                                                    legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                    idStr3 = idStr6;
                                                                    resourcePathStr = resourcePathStr3;
                                                                    i = 5;
                                                                    idStr4 = " has bad userId ";
                                                                    name = str6;
                                                                    name3 = name2;
                                                                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                                    parentPackageName = name3;
                                                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                    idStr = idStr3;
                                                                    idStr2 = primaryCpuAbiString;
                                                                    installerPackageName = installerPackageName2;
                                                                    sharedIdStr = isOrphaned;
                                                                    legacyNativeLibraryPathStr = volumeUuid;
                                                                    updateAvailable = updateAvailable2;
                                                                    categoryHint = categoryHint3;
                                                                    packageSetting = packageSetting2;
                                                                    uidError = uidError2;
                                                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                    if (packageSetting != null) {
                                                                    }
                                                                }
                                                            } catch (NumberFormatException e49) {
                                                                str3 = codePathStr2;
                                                                legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                                legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                                idStr3 = idStr6;
                                                                idStr4 = " has bad userId ";
                                                                resourcePathStr = resourcePathStr3;
                                                                i = 5;
                                                                name = str6;
                                                                name3 = name2;
                                                                PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                                parentPackageName = name3;
                                                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                                                idStr = idStr3;
                                                                idStr2 = primaryCpuAbiString;
                                                                installerPackageName = installerPackageName2;
                                                                sharedIdStr = isOrphaned;
                                                                legacyNativeLibraryPathStr = volumeUuid;
                                                                updateAvailable = updateAvailable2;
                                                                categoryHint = categoryHint3;
                                                                packageSetting = packageSetting2;
                                                                uidError = uidError2;
                                                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                                if (packageSetting != null) {
                                                                }
                                                            }
                                                        } catch (NumberFormatException e50) {
                                                            str7 = resourcePathStr3;
                                                            str3 = codePathStr2;
                                                            legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                            idStr3 = idStr6;
                                                            idStr4 = " has bad userId ";
                                                            resourcePathStr = str7;
                                                            i = 5;
                                                            name = str6;
                                                            name3 = name2;
                                                            PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                            parentPackageName = name3;
                                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                                            idStr = idStr3;
                                                            idStr2 = primaryCpuAbiString;
                                                            installerPackageName = installerPackageName2;
                                                            sharedIdStr = isOrphaned;
                                                            legacyNativeLibraryPathStr = volumeUuid;
                                                            updateAvailable = updateAvailable2;
                                                            categoryHint = categoryHint3;
                                                            packageSetting = packageSetting2;
                                                            uidError = uidError2;
                                                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                            if (packageSetting != null) {
                                                            }
                                                        }
                                                    } catch (NumberFormatException e51) {
                                                        str7 = resourcePathStr3;
                                                        legacyNativeLibraryPathStr3 = resourcePathStr2;
                                                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                                        idStr3 = idStr6;
                                                        str3 = " at ";
                                                        idStr4 = " has bad userId ";
                                                        resourcePathStr = str7;
                                                        i = 5;
                                                        name = str6;
                                                        name3 = name2;
                                                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                                        parentPackageName = name3;
                                                        secondaryCpuAbiString = secondaryCpuAbiString2;
                                                        idStr = idStr3;
                                                        idStr2 = primaryCpuAbiString;
                                                        installerPackageName = installerPackageName2;
                                                        sharedIdStr = isOrphaned;
                                                        legacyNativeLibraryPathStr = volumeUuid;
                                                        updateAvailable = updateAvailable2;
                                                        categoryHint = categoryHint3;
                                                        packageSetting = packageSetting2;
                                                        uidError = uidError2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                                        if (packageSetting != null) {
                                                        }
                                                    }
                                                }
                                            }
                                            str3 = codePathStr2;
                                            parentPackageName = name2;
                                            idStr2 = primaryCpuAbiString;
                                            installerPackageName = installerPackageName2;
                                            sharedIdStr = isOrphaned;
                                            updateAvailable = updateAvailable2;
                                            packageSetting = packageSetting2;
                                            uidError = uidError2;
                                            idStr = idStr6;
                                            name = str5;
                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                            categoryHint = categoryHint3;
                                            cpuAbiOverrideString = resourcePathStr2;
                                            legacyNativeLibraryPathStr = volumeUuid;
                                            if (packageSetting != null) {
                                                packageSetting.uidError = str2.equals(uidError);
                                                packageSetting.installerPackageName = installerPackageName;
                                                packageSetting.isOrphaned = str2.equals(sharedIdStr);
                                                packageSetting.volumeUuid = legacyNativeLibraryPathStr;
                                                packageSetting.categoryHint = categoryHint;
                                                packageSetting.legacyNativeLibraryPathString = cpuAbiOverrideString;
                                                packageSetting.primaryCpuAbiString = idStr2;
                                                packageSetting.secondaryCpuAbiString = secondaryCpuAbiString;
                                                packageSetting.updateAvailable = str2.equals(updateAvailable);
                                                String enabledStr2 = parser.getAttributeValue(null, ATTR_ENABLED);
                                                if (enabledStr2 != null) {
                                                    try {
                                                        categoryHint2 = 0;
                                                        try {
                                                            packageSetting.setEnabled(Integer.parseInt(enabledStr2), 0, null);
                                                        } catch (NumberFormatException e52) {
                                                        }
                                                    } catch (NumberFormatException e53) {
                                                        categoryHint2 = 0;
                                                        if (enabledStr2.equalsIgnoreCase(str2)) {
                                                            packageSetting.setEnabled(1, categoryHint2, null);
                                                        } else if (enabledStr2.equalsIgnoreCase("false")) {
                                                            packageSetting.setEnabled(2, categoryHint2, null);
                                                        } else if (enabledStr2.equalsIgnoreCase(BatteryService.HealthServiceWrapper.INSTANCE_VENDOR)) {
                                                            packageSetting.setEnabled(categoryHint2, categoryHint2, null);
                                                        } else {
                                                            PackageManagerService.reportSettingsProblem(5, name + parentPackageName + " has bad enabled value: " + idStr + str3 + parser.getPositionDescription());
                                                        }
                                                        if (installerPackageName == null) {
                                                        }
                                                        maxAspectRatioStr = parser.getAttributeValue(null, "maxAspectRatio");
                                                        if (maxAspectRatioStr != null) {
                                                        }
                                                        minAspectRatioStr = parser.getAttributeValue(null, "minAspectRatio");
                                                        if (minAspectRatioStr != null) {
                                                        }
                                                        appUseNotchModeStr = parser.getAttributeValue(null, "appUseNotchMode");
                                                        if (appUseNotchModeStr != null) {
                                                        }
                                                        appUseSideModeStr = parser.getAttributeValue(null, "appUseSideMode");
                                                        if (appUseSideModeStr != null) {
                                                        }
                                                        systemStr = parser.getAttributeValue(null, "hwExtraFlags");
                                                        if (systemStr != null) {
                                                        }
                                                        forceDarkModeStr = parser.getAttributeValue(null, "forceDarkMode");
                                                        if (forceDarkModeStr != null) {
                                                        }
                                                        int outerDepth2 = parser.getDepth();
                                                        while (true) {
                                                            type = parser.next();
                                                            if (type != 1) {
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    packageSetting.setEnabled(0, 0, null);
                                                }
                                                if (installerPackageName == null) {
                                                    settings2 = this;
                                                    settings2.mInstallerPackages.add(installerPackageName);
                                                } else {
                                                    settings2 = this;
                                                }
                                                maxAspectRatioStr = parser.getAttributeValue(null, "maxAspectRatio");
                                                if (maxAspectRatioStr != null) {
                                                    float maxAspectRatio = Float.parseFloat(maxAspectRatioStr);
                                                    if (maxAspectRatio > 0.0f) {
                                                        packageSetting.maxAspectRatio = maxAspectRatio;
                                                    }
                                                }
                                                minAspectRatioStr = parser.getAttributeValue(null, "minAspectRatio");
                                                if (minAspectRatioStr != null) {
                                                    packageSetting.minAspectRatio = Float.parseFloat(minAspectRatioStr);
                                                }
                                                appUseNotchModeStr = parser.getAttributeValue(null, "appUseNotchMode");
                                                if (appUseNotchModeStr != null) {
                                                    try {
                                                        packageSetting.appUseNotchMode = Integer.parseInt(appUseNotchModeStr);
                                                    } catch (NumberFormatException e54) {
                                                        Slog.e(TAG, "Error while parsing the value of appUseNotchMode from string to int.");
                                                    }
                                                }
                                                appUseSideModeStr = parser.getAttributeValue(null, "appUseSideMode");
                                                if (appUseSideModeStr != null) {
                                                    try {
                                                        packageSetting.appUseSideMode = Integer.parseInt(appUseSideModeStr);
                                                    } catch (NumberFormatException e55) {
                                                        Slog.e(TAG, "Error while parsing the value of appUseSideMode from string to int.");
                                                    }
                                                }
                                                systemStr = parser.getAttributeValue(null, "hwExtraFlags");
                                                if (systemStr != null) {
                                                    try {
                                                        packageSetting.hw_extra_flags = Integer.parseInt(systemStr);
                                                    } catch (NumberFormatException e56) {
                                                    }
                                                }
                                                forceDarkModeStr = parser.getAttributeValue(null, "forceDarkMode");
                                                if (forceDarkModeStr != null) {
                                                    try {
                                                        packageSetting.forceDarkMode = Integer.parseInt(forceDarkModeStr);
                                                    } catch (NumberFormatException e57) {
                                                        Slog.e(TAG, "Error while parsing the value of forcedarkMode from string to int.");
                                                    }
                                                }
                                                int outerDepth22 = parser.getDepth();
                                                while (true) {
                                                    type = parser.next();
                                                    if (type != 1) {
                                                        return;
                                                    }
                                                    if (type == 3 && parser.getDepth() <= outerDepth22) {
                                                        return;
                                                    }
                                                    if (type == 3) {
                                                        installerPackageName = installerPackageName;
                                                        enabledStr2 = enabledStr2;
                                                        maxAspectRatioStr = maxAspectRatioStr;
                                                    } else if (type == 4) {
                                                        installerPackageName = installerPackageName;
                                                    } else {
                                                        String tagName = parser.getName();
                                                        if (tagName.equals(TAG_DISABLED_COMPONENTS)) {
                                                            settings2.readDisabledComponentsLPw(packageSetting, parser, 0);
                                                            outerDepth = outerDepth22;
                                                            enabledStr = enabledStr2;
                                                            maxAspectRatioStr2 = maxAspectRatioStr;
                                                            legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                            str4 = str;
                                                        } else if (tagName.equals(TAG_ENABLED_COMPONENTS)) {
                                                            settings2.readEnabledComponentsLPw(packageSetting, parser, 0);
                                                            outerDepth = outerDepth22;
                                                            enabledStr = enabledStr2;
                                                            maxAspectRatioStr2 = maxAspectRatioStr;
                                                            legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                            str4 = str;
                                                        } else if (tagName.equals("sigs")) {
                                                            outerDepth = outerDepth22;
                                                            packageSetting.signatures.readXml(parser, settings2.mPastSignatures);
                                                            enabledStr = enabledStr2;
                                                            maxAspectRatioStr2 = maxAspectRatioStr;
                                                            legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                            str4 = str;
                                                        } else {
                                                            outerDepth = outerDepth22;
                                                            if (tagName.equals(TAG_PERMISSIONS)) {
                                                                settings2.readInstallPermissionsLPr(parser, packageSetting.getPermissionsState());
                                                                packageSetting.installPermissionsFixed = true;
                                                                enabledStr = enabledStr2;
                                                                maxAspectRatioStr2 = maxAspectRatioStr;
                                                                legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                                str4 = str;
                                                            } else {
                                                                if (tagName.equals("proper-signing-keyset")) {
                                                                    enabledStr = enabledStr2;
                                                                    maxAspectRatioStr2 = maxAspectRatioStr;
                                                                    long id = Long.parseLong(parser.getAttributeValue(null, "identifier"));
                                                                    Integer refCt = settings2.mKeySetRefs.get(Long.valueOf(id));
                                                                    if (refCt != null) {
                                                                        settings2.mKeySetRefs.put(Long.valueOf(id), Integer.valueOf(refCt.intValue() + 1));
                                                                    } else {
                                                                        settings2.mKeySetRefs.put(Long.valueOf(id), 1);
                                                                    }
                                                                    packageSetting.keySetData.setProperSigningKeySet(id);
                                                                } else {
                                                                    enabledStr = enabledStr2;
                                                                    maxAspectRatioStr2 = maxAspectRatioStr;
                                                                    if (!tagName.equals("signing-keyset")) {
                                                                        if (tagName.equals("upgrade-keyset")) {
                                                                            packageSetting.keySetData.addUpgradeKeySetById(Long.parseLong(parser.getAttributeValue(null, "identifier")));
                                                                            legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                                            str4 = str;
                                                                        } else if (tagName.equals("defined-keyset")) {
                                                                            long id2 = Long.parseLong(parser.getAttributeValue(null, "identifier"));
                                                                            String alias = parser.getAttributeValue(null, "alias");
                                                                            Integer refCt2 = settings2.mKeySetRefs.get(Long.valueOf(id2));
                                                                            if (refCt2 != null) {
                                                                                legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                                                settings2.mKeySetRefs.put(Long.valueOf(id2), Integer.valueOf(refCt2.intValue() + 1));
                                                                            } else {
                                                                                legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                                                settings2.mKeySetRefs.put(Long.valueOf(id2), 1);
                                                                            }
                                                                            packageSetting.keySetData.addDefinedKeySet(id2, alias);
                                                                            str4 = str;
                                                                        } else {
                                                                            legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                                            if (tagName.equals(TAG_DOMAIN_VERIFICATION)) {
                                                                                settings2.readDomainVerificationLPw(parser, packageSetting);
                                                                                str4 = str;
                                                                            } else if (tagName.equals(TAG_CHILD_PACKAGE)) {
                                                                                str4 = str;
                                                                                String childPackageName = parser.getAttributeValue(null, str4);
                                                                                if (packageSetting.childPackageNames == null) {
                                                                                    packageSetting.childPackageNames = new ArrayList();
                                                                                }
                                                                                packageSetting.childPackageNames.add(childPackageName);
                                                                            } else {
                                                                                str4 = str;
                                                                                PackageManagerService.reportSettingsProblem(5, "Unknown element under <package>: " + parser.getName());
                                                                                XmlUtils.skipCurrentTag(parser);
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                legacyNativeLibraryPathStr2 = cpuAbiOverrideString;
                                                                str4 = str;
                                                            }
                                                        }
                                                        str = str4;
                                                        installerPackageName = installerPackageName;
                                                        outerDepth22 = outerDepth;
                                                        enabledStr2 = enabledStr;
                                                        maxAspectRatioStr = maxAspectRatioStr2;
                                                        cpuAbiOverrideString = legacyNativeLibraryPathStr2;
                                                    }
                                                }
                                            } else {
                                                XmlUtils.skipCurrentTag(parser);
                                                return;
                                            }
                                        }
                                        lastUpdateTime = 0;
                                        try {
                                            if (PackageManagerService.DEBUG_SETTINGS) {
                                            }
                                            if (idStr5 != null) {
                                            }
                                            if (sharedIdStr2 != null) {
                                            }
                                            if (resourcePathStr == null) {
                                            }
                                            if (realName2 != null) {
                                            }
                                            if (name3 == null) {
                                            }
                                            str3 = codePathStr2;
                                            parentPackageName = name2;
                                            idStr2 = primaryCpuAbiString;
                                            installerPackageName = installerPackageName2;
                                            sharedIdStr = isOrphaned;
                                            updateAvailable = updateAvailable2;
                                            packageSetting = packageSetting2;
                                            uidError = uidError2;
                                            idStr = idStr6;
                                            name = str5;
                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                            categoryHint = categoryHint3;
                                            cpuAbiOverrideString = resourcePathStr2;
                                            legacyNativeLibraryPathStr = volumeUuid;
                                        } catch (NumberFormatException e58) {
                                            settings = this;
                                            str3 = " at ";
                                            idStr4 = " has bad userId ";
                                            str = ATTR_NAME;
                                            str2 = "true";
                                            legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                            idStr3 = idStr5;
                                            name = "Error in package manager settings: package ";
                                            i = 5;
                                            PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                            parentPackageName = name3;
                                            secondaryCpuAbiString = secondaryCpuAbiString2;
                                            idStr = idStr3;
                                            idStr2 = primaryCpuAbiString;
                                            installerPackageName = installerPackageName2;
                                            sharedIdStr = isOrphaned;
                                            legacyNativeLibraryPathStr = volumeUuid;
                                            updateAvailable = updateAvailable2;
                                            categoryHint = categoryHint3;
                                            packageSetting = packageSetting2;
                                            uidError = uidError2;
                                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                            if (packageSetting != null) {
                                            }
                                        }
                                        if (packageSetting != null) {
                                        }
                                    }
                                    firstInstallTime = 0;
                                } catch (NumberFormatException e59) {
                                    settings = this;
                                    str3 = " at ";
                                    idStr4 = " has bad userId ";
                                    str = ATTR_NAME;
                                    str2 = "true";
                                    legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                    idStr3 = idStr5;
                                    name = "Error in package manager settings: package ";
                                    i = 5;
                                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                    parentPackageName = name3;
                                    secondaryCpuAbiString = secondaryCpuAbiString2;
                                    idStr = idStr3;
                                    idStr2 = primaryCpuAbiString;
                                    installerPackageName = installerPackageName2;
                                    sharedIdStr = isOrphaned;
                                    legacyNativeLibraryPathStr = volumeUuid;
                                    updateAvailable = updateAvailable2;
                                    categoryHint = categoryHint3;
                                    packageSetting = packageSetting2;
                                    uidError = uidError2;
                                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                    if (packageSetting != null) {
                                    }
                                }
                            } catch (NumberFormatException e60) {
                                settings = this;
                                str2 = "true";
                                str3 = " at ";
                                idStr4 = " has bad userId ";
                                str = ATTR_NAME;
                                legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                                legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                                idStr3 = idStr5;
                                name = "Error in package manager settings: package ";
                                i = 5;
                                PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                                parentPackageName = name3;
                                secondaryCpuAbiString = secondaryCpuAbiString2;
                                idStr = idStr3;
                                idStr2 = primaryCpuAbiString;
                                installerPackageName = installerPackageName2;
                                sharedIdStr = isOrphaned;
                                legacyNativeLibraryPathStr = volumeUuid;
                                updateAvailable = updateAvailable2;
                                categoryHint = categoryHint3;
                                packageSetting = packageSetting2;
                                uidError = uidError2;
                                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                                if (packageSetting != null) {
                                }
                            }
                        } catch (NumberFormatException e61) {
                            settings = this;
                            str2 = "true";
                            str3 = " at ";
                            idStr4 = " has bad userId ";
                            str = ATTR_NAME;
                            legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                            primaryCpuAbiString = primaryCpuAbiString2;
                            legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                            idStr3 = idStr5;
                            name = "Error in package manager settings: package ";
                            i = 5;
                            PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                            parentPackageName = name3;
                            secondaryCpuAbiString = secondaryCpuAbiString2;
                            idStr = idStr3;
                            idStr2 = primaryCpuAbiString;
                            installerPackageName = installerPackageName2;
                            sharedIdStr = isOrphaned;
                            legacyNativeLibraryPathStr = volumeUuid;
                            updateAvailable = updateAvailable2;
                            categoryHint = categoryHint3;
                            packageSetting = packageSetting2;
                            uidError = uidError2;
                            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                            if (packageSetting != null) {
                            }
                        }
                    } catch (NumberFormatException e62) {
                        settings = this;
                        str2 = "true";
                        str3 = " at ";
                        idStr4 = " has bad userId ";
                        str = ATTR_NAME;
                        legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                        codePathStr = null;
                        legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                        idStr3 = idStr5;
                        name = "Error in package manager settings: package ";
                        i = 5;
                        primaryCpuAbiString = primaryCpuAbiString2;
                        PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                        parentPackageName = name3;
                        secondaryCpuAbiString = secondaryCpuAbiString2;
                        idStr = idStr3;
                        idStr2 = primaryCpuAbiString;
                        installerPackageName = installerPackageName2;
                        sharedIdStr = isOrphaned;
                        legacyNativeLibraryPathStr = volumeUuid;
                        updateAvailable = updateAvailable2;
                        categoryHint = categoryHint3;
                        packageSetting = packageSetting2;
                        uidError = uidError2;
                        cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                        if (packageSetting != null) {
                        }
                    }
                } catch (NumberFormatException e63) {
                    settings = this;
                    str2 = "true";
                    str3 = " at ";
                    idStr4 = " has bad userId ";
                    str = ATTR_NAME;
                    legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                    secondaryCpuAbiString2 = null;
                    codePathStr = null;
                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                    idStr3 = idStr5;
                    name = "Error in package manager settings: package ";
                    i = 5;
                    primaryCpuAbiString = primaryCpuAbiString2;
                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                    parentPackageName = name3;
                    secondaryCpuAbiString = secondaryCpuAbiString2;
                    idStr = idStr3;
                    idStr2 = primaryCpuAbiString;
                    installerPackageName = installerPackageName2;
                    sharedIdStr = isOrphaned;
                    legacyNativeLibraryPathStr = volumeUuid;
                    updateAvailable = updateAvailable2;
                    categoryHint = categoryHint3;
                    packageSetting = packageSetting2;
                    uidError = uidError2;
                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                    if (packageSetting != null) {
                    }
                }
                try {
                    timeStampStr = parser.getAttributeValue(null, "ut");
                    if (timeStampStr != null) {
                    }
                    lastUpdateTime = 0;
                    if (PackageManagerService.DEBUG_SETTINGS) {
                    }
                    if (idStr5 != null) {
                    }
                    if (sharedIdStr2 != null) {
                    }
                    if (resourcePathStr == null) {
                    }
                    if (realName2 != null) {
                    }
                    if (name3 == null) {
                    }
                    str3 = codePathStr2;
                    parentPackageName = name2;
                    idStr2 = primaryCpuAbiString;
                    installerPackageName = installerPackageName2;
                    sharedIdStr = isOrphaned;
                    updateAvailable = updateAvailable2;
                    packageSetting = packageSetting2;
                    uidError = uidError2;
                    idStr = idStr6;
                    name = str5;
                    secondaryCpuAbiString = secondaryCpuAbiString2;
                    categoryHint = categoryHint3;
                    cpuAbiOverrideString = resourcePathStr2;
                    legacyNativeLibraryPathStr = volumeUuid;
                } catch (NumberFormatException e64) {
                    settings = this;
                    str3 = " at ";
                    idStr4 = " has bad userId ";
                    str = ATTR_NAME;
                    str2 = "true";
                    legacyNativeLibraryPathStr3 = legacyNativeLibraryPathStr6;
                    legacyNativeLibraryPathStr5 = legacyNativeLibraryPathStr5;
                    idStr3 = idStr5;
                    name = "Error in package manager settings: package ";
                    i = 5;
                    PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                    parentPackageName = name3;
                    secondaryCpuAbiString = secondaryCpuAbiString2;
                    idStr = idStr3;
                    idStr2 = primaryCpuAbiString;
                    installerPackageName = installerPackageName2;
                    sharedIdStr = isOrphaned;
                    legacyNativeLibraryPathStr = volumeUuid;
                    updateAvailable = updateAvailable2;
                    categoryHint = categoryHint3;
                    packageSetting = packageSetting2;
                    uidError = uidError2;
                    cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                    if (packageSetting != null) {
                    }
                }
            } catch (NumberFormatException e65) {
                settings = this;
                str2 = "true";
                str3 = " at ";
                idStr4 = " has bad userId ";
                str = ATTR_NAME;
                legacyNativeLibraryPathStr3 = null;
                secondaryCpuAbiString2 = null;
                codePathStr = null;
                idStr3 = idStr5;
                name = "Error in package manager settings: package ";
                i = 5;
                primaryCpuAbiString = null;
                PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
                parentPackageName = name3;
                secondaryCpuAbiString = secondaryCpuAbiString2;
                idStr = idStr3;
                idStr2 = primaryCpuAbiString;
                installerPackageName = installerPackageName2;
                sharedIdStr = isOrphaned;
                legacyNativeLibraryPathStr = volumeUuid;
                updateAvailable = updateAvailable2;
                categoryHint = categoryHint3;
                packageSetting = packageSetting2;
                uidError = uidError2;
                cpuAbiOverrideString = legacyNativeLibraryPathStr3;
                if (packageSetting != null) {
                }
            }
        } catch (NumberFormatException e66) {
            settings = this;
            str2 = "true";
            name = "Error in package manager settings: package ";
            str3 = " at ";
            str = ATTR_NAME;
            idStr3 = null;
            idStr4 = " has bad userId ";
            i = 5;
            resourcePathStr = null;
            uidError2 = null;
            legacyNativeLibraryPathStr3 = null;
            secondaryCpuAbiString2 = null;
            codePathStr = null;
            primaryCpuAbiString = null;
            PackageManagerService.reportSettingsProblem(i, name + name3 + idStr4 + idStr3 + str3 + parser.getPositionDescription());
            parentPackageName = name3;
            secondaryCpuAbiString = secondaryCpuAbiString2;
            idStr = idStr3;
            idStr2 = primaryCpuAbiString;
            installerPackageName = installerPackageName2;
            sharedIdStr = isOrphaned;
            legacyNativeLibraryPathStr = volumeUuid;
            updateAvailable = updateAvailable2;
            categoryHint = categoryHint3;
            packageSetting = packageSetting2;
            uidError = uidError2;
            cpuAbiOverrideString = legacyNativeLibraryPathStr3;
            if (packageSetting != null) {
            }
        }
        if (packageSetting != null) {
        }
    }

    private void readDisabledComponentsLPw(PackageSettingBase packageSetting, XmlPullParser parser, int userId) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    String name = parser.getAttributeValue(null, ATTR_NAME);
                    if (name != null) {
                        packageSetting.addDisabledComponent(name.intern(), userId);
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <disabled-components> has no name at " + parser.getPositionDescription());
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <disabled-components>: " + parser.getName());
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    private void readEnabledComponentsLPw(PackageSettingBase packageSetting, XmlPullParser parser, int userId) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(TAG_ITEM)) {
                    String name = parser.getAttributeValue(null, ATTR_NAME);
                    if (name != null) {
                        packageSetting.addEnabledComponent(name.intern(), userId);
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <enabled-components> has no name at " + parser.getPositionDescription());
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <enabled-components>: " + parser.getName());
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    private void readSharedUserLPw(XmlPullParser parser) throws XmlPullParserException, IOException {
        int pkgFlags = 0;
        SharedUserSetting su = null;
        try {
            String name = parser.getAttributeValue(null, ATTR_NAME);
            String idStr = parser.getAttributeValue(null, "userId");
            int userId = idStr != null ? Integer.parseInt(idStr) : 0;
            if ("true".equals(parser.getAttributeValue(null, "system"))) {
                pkgFlags = 0 | 1;
            }
            if (name == null) {
                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <shared-user> has no name at " + parser.getPositionDescription());
            } else if (userId == 0) {
                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: shared-user " + name + " has bad userId " + idStr + " at " + parser.getPositionDescription());
            } else {
                SharedUserSetting addSharedUserLPw = addSharedUserLPw(name.intern(), userId, pkgFlags, 0);
                su = addSharedUserLPw;
                if (addSharedUserLPw == null) {
                    PackageManagerService.reportSettingsProblem(6, "Occurred while parsing settings at " + parser.getPositionDescription());
                }
            }
        } catch (NumberFormatException e) {
            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: package " + ((String) null) + " has bad userId " + ((String) null) + " at " + parser.getPositionDescription());
        }
        if (su != null) {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    String tagName = parser.getName();
                    if (tagName.equals("sigs")) {
                        su.signatures.readXml(parser, this.mPastSignatures);
                    } else if (tagName.equals(TAG_PERMISSIONS)) {
                        readInstallPermissionsLPr(parser, su.getPermissionsState());
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Unknown element under <shared-user>: " + parser.getName());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
        } else {
            XmlUtils.skipCurrentTag(parser);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00b6  */
    public void createNewUserLI(PackageManagerService service, Installer installer, int userHandle, String[] disallowedPackages) {
        Throwable th;
        int packagesCount;
        String[] volumeUuids;
        String[] names;
        int[] appIds;
        String[] seinfos;
        int[] targetSdkVersions;
        int i;
        Installer.InstallerException e;
        int i2;
        Collection<PackageSetting> packages;
        boolean shouldInstall;
        boolean shouldInstall2;
        PackageSetting ps;
        synchronized (this.mPackages) {
            try {
                Collection<PackageSetting> packages2 = this.mPackages.values();
                packagesCount = packages2.size();
                volumeUuids = new String[packagesCount];
                names = new String[packagesCount];
                appIds = new int[packagesCount];
                seinfos = new String[packagesCount];
                targetSdkVersions = new int[packagesCount];
                Iterator<PackageSetting> packagesIterator = packages2.iterator();
                int i3 = 0;
                while (i3 < packagesCount) {
                    PackageSetting ps2 = packagesIterator.next();
                    if (ps2.pkg == null) {
                        packages = packages2;
                        i2 = i3;
                    } else if (ps2.pkg.applicationInfo == null) {
                        packages = packages2;
                        i2 = i3;
                    } else {
                        if (ps2.isSystem()) {
                            try {
                                if (!ArrayUtils.contains(disallowedPackages, ps2.name) && !ps2.pkg.applicationInfo.hiddenUntilInstalled) {
                                    shouldInstall = true;
                                    ps2.setInstalled(shouldInstall, userHandle);
                                    if (shouldInstall || !isInDelAppList(ps2.name)) {
                                        packages = packages2;
                                        shouldInstall2 = shouldInstall;
                                        ps = ps2;
                                        i2 = i3;
                                    } else {
                                        StringBuilder sb = new StringBuilder();
                                        packages = packages2;
                                        sb.append("disable application: ");
                                        sb.append(ps2.name);
                                        sb.append(" for user ");
                                        sb.append(userHandle);
                                        Slog.w(TAG, sb.toString());
                                        shouldInstall2 = shouldInstall;
                                        ps = ps2;
                                        i2 = i3;
                                        service.setApplicationEnabledSetting(ps2.name, 2, 0, userHandle, null);
                                    }
                                    if (!shouldInstall2) {
                                        writeKernelMappingLPr(ps);
                                    }
                                    volumeUuids[i2] = ps.volumeUuid;
                                    names[i2] = ps.name;
                                    appIds[i2] = ps.appId;
                                    seinfos[i2] = ps.pkg.applicationInfo.seInfo;
                                    targetSdkVersions[i2] = ps.pkg.applicationInfo.targetSdkVersion;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                        shouldInstall = false;
                        ps2.setInstalled(shouldInstall, userHandle);
                        if (shouldInstall) {
                        }
                        packages = packages2;
                        shouldInstall2 = shouldInstall;
                        ps = ps2;
                        i2 = i3;
                        if (!shouldInstall2) {
                        }
                        volumeUuids[i2] = ps.volumeUuid;
                        names[i2] = ps.name;
                        appIds[i2] = ps.appId;
                        seinfos[i2] = ps.pkg.applicationInfo.seInfo;
                        targetSdkVersions[i2] = ps.pkg.applicationInfo.targetSdkVersion;
                    }
                    i3 = i2 + 1;
                    packages2 = packages;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        int i4 = 0;
        while (i4 < packagesCount) {
            if (names[i4] == null) {
                i = i4;
            } else {
                try {
                    i = i4;
                    try {
                        installer.createAppData(volumeUuids[i4], names[i4], userHandle, 3, appIds[i4], seinfos[i4], targetSdkVersions[i4]);
                    } catch (Installer.InstallerException e2) {
                        e = e2;
                    }
                } catch (Installer.InstallerException e3) {
                    e = e3;
                    i = i4;
                    Slog.w(TAG, "Failed to prepare app data", e);
                    i4 = i + 1;
                }
            }
            i4 = i + 1;
        }
        synchronized (this.mPackages) {
            applyDefaultPreferredAppsLPw(userHandle);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeUserLPw(int userId) {
        for (Map.Entry<String, PackageSetting> entry : this.mPackages.entrySet()) {
            entry.getValue().removeUser(userId);
        }
        this.mPreferredActivities.remove(userId);
        getUserPackagesStateFile(userId).delete();
        getUserPackagesStateBackupFile(userId).delete();
        removeCrossProfileIntentFiltersLPw(userId);
        this.mRuntimePermissionsPersistence.onUserRemovedLPw(userId);
        writePackageListLPr();
        writeKernelRemoveUserLPr(userId);
    }

    /* access modifiers changed from: package-private */
    public void removeCrossProfileIntentFiltersLPw(int userId) {
        synchronized (this.mCrossProfileIntentResolvers) {
            if (this.mCrossProfileIntentResolvers.get(userId) != null) {
                this.mCrossProfileIntentResolvers.remove(userId);
                writePackageRestrictionsLPr(userId);
            }
            int count = this.mCrossProfileIntentResolvers.size();
            for (int i = 0; i < count; i++) {
                int sourceUserId = this.mCrossProfileIntentResolvers.keyAt(i);
                CrossProfileIntentResolver cpir = this.mCrossProfileIntentResolvers.get(sourceUserId);
                boolean needsWriting = false;
                Iterator<CrossProfileIntentFilter> it = new ArraySet<>(cpir.filterSet()).iterator();
                while (it.hasNext()) {
                    CrossProfileIntentFilter cpif = it.next();
                    if (cpif.getTargetUserId() == userId) {
                        needsWriting = true;
                        cpir.removeFilter(cpif);
                    }
                }
                if (needsWriting) {
                    writePackageRestrictionsLPr(sourceUserId);
                }
            }
        }
    }

    private void setFirstAvailableUid(int uid) {
        if (uid > mFirstAvailableUid) {
            mFirstAvailableUid = uid;
        }
    }

    private int acquireAndRegisterNewAppIdLPw(SettingBase obj) {
        int reservedUid = getReservedUidForPackage(obj);
        if (reservedUid != -1) {
            this.mAppIds.set(reservedUid - 10000, obj);
            return reservedUid;
        }
        int size = this.mAppIds.size();
        for (int i = mFirstAvailableUid; i < size; i++) {
            if (this.mAppIds.get(i) == null && !isReservedUid(i)) {
                this.mAppIds.set(i, obj);
                return i + 10000;
            }
        }
        if (size > 9999) {
            return retryNewUserIdLPw(obj);
        }
        this.mAppIds.add(obj);
        return size + 10000;
    }

    private int retryNewUserIdLPw(SettingBase obj) {
        int size = this.mAppIds.size();
        Slog.i(TAG, "retryNewUserIdLPw N:" + size + ",first available uid:" + mFirstAvailableUid);
        if (!this.isNeedRetryNewUserId) {
            Slog.i(TAG, "No need to retry to find an available UserId to assign!");
            return -1;
        }
        int appId = -1;
        int i = 0;
        while (true) {
            if (i < size) {
                if (this.mAppIds.get(i) == null && !isReservedUid(i)) {
                    this.mAppIds.set(i, obj);
                    appId = i + 10000;
                    mFirstAvailableUid = i + 1;
                    Slog.i(TAG, "we find an available UserId " + appId + " to assign after retry, change first available uid to " + mFirstAvailableUid);
                    break;
                }
                i++;
            } else {
                break;
            }
        }
        if (appId < 0) {
            this.isNeedRetryNewUserId = false;
            Slog.e(TAG, "we could not find an available UserId to assign after retry!");
        }
        return appId;
    }

    public VerifierDeviceIdentity getVerifierDeviceIdentityLPw() {
        if (this.mVerifierDeviceIdentity == null) {
            this.mVerifierDeviceIdentity = VerifierDeviceIdentity.generate();
            writeLPr();
        }
        return this.mVerifierDeviceIdentity;
    }

    /* access modifiers changed from: package-private */
    public boolean hasOtherDisabledSystemPkgWithChildLPr(String parentPackageName, String childPackageName) {
        int packageCount = this.mDisabledSysPackages.size();
        for (int i = 0; i < packageCount; i++) {
            PackageSetting disabledPs = this.mDisabledSysPackages.valueAt(i);
            if (disabledPs.childPackageNames != null && !disabledPs.childPackageNames.isEmpty() && !disabledPs.name.equals(parentPackageName)) {
                int childCount = disabledPs.childPackageNames.size();
                for (int j = 0; j < childCount; j++) {
                    if (((String) disabledPs.childPackageNames.get(j)).equals(childPackageName)) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    public PackageSetting getDisabledSystemPkgLPr(String name) {
        return this.mDisabledSysPackages.get(name);
    }

    public PackageSetting getDisabledSystemPkgLPr(PackageSetting enabledPackageSetting) {
        if (enabledPackageSetting == null) {
            return null;
        }
        return getDisabledSystemPkgLPr(enabledPackageSetting.name);
    }

    public PackageSetting[] getChildSettingsLPr(PackageSetting parentPackageSetting) {
        if (parentPackageSetting == null || !parentPackageSetting.hasChildPackages()) {
            return null;
        }
        int childCount = parentPackageSetting.childPackageNames.size();
        PackageSetting[] children = new PackageSetting[childCount];
        for (int i = 0; i < childCount; i++) {
            children[i] = this.mPackages.get(parentPackageSetting.childPackageNames.get(i));
        }
        return children;
    }

    /* access modifiers changed from: package-private */
    public boolean isEnabledAndMatchLPr(ComponentInfo componentInfo, int flags, int userId) {
        PackageSetting ps = this.mPackages.get(componentInfo.packageName);
        if (ps == null) {
            return false;
        }
        return ps.readUserState(userId).isMatch(componentInfo, flags);
    }

    /* access modifiers changed from: package-private */
    public String getInstallerPackageNameLPr(String packageName) {
        PackageSetting pkg = this.mPackages.get(packageName);
        if (pkg != null) {
            return pkg.installerPackageName;
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* access modifiers changed from: package-private */
    public boolean isOrphaned(String packageName) {
        PackageSetting pkg = this.mPackages.get(packageName);
        if (pkg != null) {
            return pkg.isOrphaned;
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* access modifiers changed from: package-private */
    public int getApplicationEnabledSettingLPr(String packageName, int userId) {
        PackageSetting pkg = this.mPackages.get(packageName);
        if (pkg != null) {
            return pkg.getEnabled(userId);
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* access modifiers changed from: package-private */
    public int getComponentEnabledSettingLPr(ComponentName componentName, int userId) {
        PackageSetting pkg = this.mPackages.get(componentName.getPackageName());
        if (pkg != null) {
            return pkg.getCurrentEnabledStateLPr(componentName.getClassName(), userId);
        }
        throw new IllegalArgumentException("Unknown component: " + componentName);
    }

    /* access modifiers changed from: package-private */
    public boolean wasPackageEverLaunchedLPr(String packageName, int userId) {
        PackageSetting pkgSetting = this.mPackages.get(packageName);
        if (pkgSetting != null) {
            return !pkgSetting.getNotLaunched(userId);
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* access modifiers changed from: package-private */
    public boolean setPackageStoppedStateLPw(PackageManagerService pm, String packageName, boolean stopped, boolean allowedByPermission, int uid, int userId) {
        int appId = UserHandle.getAppId(uid);
        PackageSetting pkgSetting = this.mPackages.get(packageName);
        if (pkgSetting == null) {
            throw new IllegalArgumentException("Unknown package: " + packageName);
        } else if (!allowedByPermission && appId != pkgSetting.appId) {
            throw new SecurityException("Permission Denial: attempt to change stopped state from pid=" + Binder.getCallingPid() + ", uid=" + uid + ", package uid=" + pkgSetting.appId);
        } else if (pkgSetting.getStopped(userId) == stopped) {
            return false;
        } else {
            pkgSetting.setStopped(stopped, userId);
            if (!pkgSetting.getNotLaunched(userId)) {
                return true;
            }
            if (pkgSetting.installerPackageName != null) {
                pm.notifyFirstLaunch(pkgSetting.name, pkgSetting.installerPackageName, userId);
            }
            pkgSetting.setNotLaunched(false, userId);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void setHarmfulAppWarningLPw(String packageName, CharSequence warning, int userId) {
        PackageSetting pkgSetting = this.mPackages.get(packageName);
        if (pkgSetting != null) {
            pkgSetting.setHarmfulAppWarning(userId, warning == null ? null : warning.toString());
            return;
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    /* access modifiers changed from: package-private */
    public String getHarmfulAppWarningLPr(String packageName, int userId) {
        PackageSetting pkgSetting = this.mPackages.get(packageName);
        if (pkgSetting != null) {
            return pkgSetting.getHarmfulAppWarning(userId);
        }
        throw new IllegalArgumentException("Unknown package: " + packageName);
    }

    private static List<UserInfo> getAllUsers(UserManagerService userManager) {
        return getUsers(userManager, false);
    }

    /* JADX INFO: finally extract failed */
    private static List<UserInfo> getUsers(UserManagerService userManager, boolean excludeDying) {
        long id = Binder.clearCallingIdentity();
        try {
            List<UserInfo> users = userManager.getUsers(excludeDying);
            Binder.restoreCallingIdentity(id);
            return users;
        } catch (NullPointerException e) {
            Binder.restoreCallingIdentity(id);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(id);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public List<PackageSetting> getVolumePackagesLPr(String volumeUuid) {
        ArrayList<PackageSetting> res = new ArrayList<>();
        for (int i = 0; i < this.mPackages.size(); i++) {
            PackageSetting setting = this.mPackages.valueAt(i);
            if (Objects.equals(volumeUuid, setting.volumeUuid)) {
                res.add(setting);
            }
        }
        return res;
    }

    static void printFlags(PrintWriter pw, int val, Object[] spec) {
        pw.print("[ ");
        for (int i = 0; i < spec.length; i += 2) {
            if ((val & ((Integer) spec[i]).intValue()) != 0) {
                pw.print(spec[i + 1]);
                pw.print(" ");
            }
        }
        pw.print("]");
    }

    /* access modifiers changed from: package-private */
    public void dumpVersionLPr(IndentingPrintWriter pw) {
        pw.increaseIndent();
        for (int i = 0; i < this.mVersion.size(); i++) {
            String volumeUuid = this.mVersion.keyAt(i);
            VersionInfo ver = this.mVersion.valueAt(i);
            if (Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, volumeUuid)) {
                pw.println("Internal:");
            } else if (Objects.equals("primary_physical", volumeUuid)) {
                pw.println("External:");
            } else {
                pw.println("UUID " + volumeUuid + ":");
            }
            pw.increaseIndent();
            pw.printPair(ATTR_SDK_VERSION, Integer.valueOf(ver.sdkVersion));
            pw.printPair(ATTR_DATABASE_VERSION, Integer.valueOf(ver.databaseVersion));
            pw.println();
            pw.printPair(ATTR_EMUI_VERSION, Integer.valueOf(ver.emuiVersion));
            pw.println();
            pw.printPair(ATTR_FINGERPRINT, ver.fingerprint);
            pw.println();
            pw.printPair(ATTR_HWFINGERPRINT, ver.hwFingerprint);
            pw.println();
            pw.printPair(ATTR_FINGERPRINTEX, ver.fingerprintEx);
            pw.println();
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
    }

    /* access modifiers changed from: package-private */
    public void dumpPackageLPr(PrintWriter pw, String prefix, String checkinTag, ArraySet<String> permissionNames, PackageSetting ps, SimpleDateFormat sdf, Date date, List<UserInfo> users, boolean dumpAll, boolean dumpAllComponents) {
        String str;
        if (checkinTag != null) {
            pw.print(checkinTag);
            pw.print(",");
            pw.print(ps.realName != null ? ps.realName : ps.name);
            pw.print(",");
            pw.print(ps.appId);
            pw.print(",");
            pw.print(ps.versionCode);
            pw.print(",");
            pw.print(ps.firstInstallTime);
            pw.print(",");
            pw.print(ps.lastUpdateTime);
            pw.print(",");
            pw.print(ps.installerPackageName != null ? ps.installerPackageName : "?");
            pw.println();
            if (ps.pkg != null) {
                pw.print(checkinTag);
                pw.print("-");
                pw.print("splt,");
                pw.print("base,");
                pw.println(ps.pkg.baseRevisionCode);
                if (ps.pkg.splitNames != null) {
                    for (int i = 0; i < ps.pkg.splitNames.length; i++) {
                        pw.print(checkinTag);
                        pw.print("-");
                        pw.print("splt,");
                        pw.print(ps.pkg.splitNames[i]);
                        pw.print(",");
                        if ((ps.pkg.splitPrivateFlags[i] & Integer.MIN_VALUE) != 0) {
                            pw.print(ps.pkg.splitVersionCodes[i]);
                            pw.print(",");
                        }
                        pw.println(ps.pkg.splitRevisionCodes[i]);
                    }
                }
            }
            for (UserInfo user : users) {
                pw.print(checkinTag);
                pw.print("-");
                pw.print("usr");
                pw.print(",");
                pw.print(user.id);
                pw.print(",");
                pw.print(ps.getInstalled(user.id) ? "I" : "i");
                pw.print(ps.getHidden(user.id) ? "B" : "b");
                pw.print(ps.getSuspended(user.id) ? "SU" : "su");
                pw.print(ps.getStopped(user.id) ? "S" : "s");
                pw.print(ps.getNotLaunched(user.id) ? "l" : "L");
                pw.print(ps.getInstantApp(user.id) ? "IA" : "ia");
                pw.print(ps.getVirtulalPreload(user.id) ? "VPI" : "vpi");
                pw.print(ps.getHarmfulAppWarning(user.id) != null ? "HA" : "ha");
                pw.print(",");
                pw.print(ps.getEnabled(user.id));
                String lastDisabledAppCaller = ps.getLastDisabledAppCaller(user.id);
                pw.print(",");
                if (lastDisabledAppCaller != null) {
                    str = lastDisabledAppCaller;
                } else {
                    str = "?";
                }
                pw.print(str);
                pw.print(",");
                pw.println();
            }
            return;
        }
        pw.print(prefix);
        pw.print("Package [");
        pw.print(ps.realName != null ? ps.realName : ps.name);
        pw.print("] (");
        pw.print(Integer.toHexString(System.identityHashCode(ps)));
        pw.println("):");
        if (ps.realName != null) {
            pw.print(prefix);
            pw.print("  compat name=");
            pw.println(ps.name);
        }
        pw.print(prefix);
        pw.print("  userId=");
        pw.println(ps.appId);
        if (ps.sharedUser != null) {
            pw.print(prefix);
            pw.print("  sharedUser=");
            pw.println(ps.sharedUser);
        }
        pw.print(prefix);
        pw.print("  pkg=");
        pw.println(ps.pkg);
        pw.print(prefix);
        pw.print("  codePath=");
        pw.println(ps.codePathString);
        if (permissionNames == null) {
            pw.print(prefix);
            pw.print("  resourcePath=");
            pw.println(ps.resourcePathString);
            pw.print(prefix);
            pw.print("  legacyNativeLibraryDir=");
            pw.println(ps.legacyNativeLibraryPathString);
            pw.print(prefix);
            pw.print("  primaryCpuAbi=");
            pw.println(ps.primaryCpuAbiString);
            pw.print(prefix);
            pw.print("  secondaryCpuAbi=");
            pw.println(ps.secondaryCpuAbiString);
        }
        pw.print(prefix);
        pw.print("  versionCode=");
        pw.print(ps.versionCode);
        if (ps.pkg != null) {
            pw.print(" minSdk=");
            pw.print(ps.pkg.applicationInfo.minSdkVersion);
            pw.print(" targetSdk=");
            pw.print(ps.pkg.applicationInfo.targetSdkVersion);
        }
        pw.println();
        if (ps.pkg != null) {
            if (ps.pkg.parentPackage != null) {
                PackageParser.Package parentPkg = ps.pkg.parentPackage;
                PackageSetting pps = this.mPackages.get(parentPkg.packageName);
                if (pps == null || !pps.codePathString.equals(parentPkg.codePath)) {
                    pps = this.mDisabledSysPackages.get(parentPkg.packageName);
                }
                if (pps != null) {
                    pw.print(prefix);
                    pw.print("  parentPackage=");
                    pw.println(pps.realName != null ? pps.realName : pps.name);
                }
            } else if (ps.pkg.childPackages != null) {
                pw.print(prefix);
                pw.print("  childPackages=[");
                int childCount = ps.pkg.childPackages.size();
                for (int i2 = 0; i2 < childCount; i2++) {
                    PackageParser.Package childPkg = (PackageParser.Package) ps.pkg.childPackages.get(i2);
                    PackageSetting cps = this.mPackages.get(childPkg.packageName);
                    if (cps == null || !cps.codePathString.equals(childPkg.codePath)) {
                        cps = this.mDisabledSysPackages.get(childPkg.packageName);
                    }
                    if (cps != null) {
                        if (i2 > 0) {
                            pw.print(", ");
                        }
                        pw.print(cps.realName != null ? cps.realName : cps.name);
                    }
                }
                pw.println("]");
            }
            pw.print(prefix);
            pw.print("  versionName=");
            pw.println(ps.pkg.mVersionName);
            pw.print(prefix);
            pw.print("  splits=");
            dumpSplitNames(pw, ps.pkg);
            pw.println();
            int apkSigningVersion = ps.pkg.mSigningDetails.signatureSchemeVersion;
            pw.print(prefix);
            pw.print("  apkSigningVersion=");
            pw.println(apkSigningVersion);
            pw.print(prefix);
            pw.print("  applicationInfo=");
            pw.println(ps.pkg.applicationInfo.toString());
            pw.print(prefix);
            pw.print("  applicationInfo.owns=");
            pw.println(ps.pkg.applicationInfo.owns);
            pw.print(prefix);
            pw.print("  flags=");
            printFlags(pw, ps.pkg.applicationInfo.flags, FLAG_DUMP_SPEC);
            pw.println();
            pw.print(prefix);
            pw.print("  hwflags=");
            printFlags(pw, ps.pkg.applicationInfo.hwFlags, HW_FLAG_DUMP_SPEC);
            pw.println();
            if (ps.pkg.applicationInfo.privateFlags != 0) {
                pw.print(prefix);
                pw.print("  privateFlags=");
                printFlags(pw, ps.pkg.applicationInfo.privateFlags, PRIVATE_FLAG_DUMP_SPEC);
                pw.println();
            }
            pw.print(prefix);
            pw.print("  dataDir=");
            pw.println(ps.pkg.applicationInfo.dataDir);
            pw.print(prefix);
            pw.print("  supportsScreens=[");
            boolean first = true;
            if ((ps.pkg.applicationInfo.flags & 512) != 0) {
                if (1 == 0) {
                    pw.print(", ");
                }
                first = false;
                pw.print("small");
            }
            if ((ps.pkg.applicationInfo.flags & 1024) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = false;
                pw.print("medium");
            }
            if ((ps.pkg.applicationInfo.flags & 2048) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = false;
                pw.print("large");
            }
            if ((ps.pkg.applicationInfo.flags & DumpState.DUMP_FROZEN) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = false;
                pw.print("xlarge");
            }
            if ((ps.pkg.applicationInfo.flags & 4096) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                first = false;
                pw.print("resizeable");
            }
            if ((ps.pkg.applicationInfo.flags & 8192) != 0) {
                if (!first) {
                    pw.print(", ");
                }
                pw.print("anyDensity");
            }
            pw.println("]");
            if (ps.pkg.libraryNames != null && ps.pkg.libraryNames.size() > 0) {
                pw.print(prefix);
                pw.println("  dynamic libraries:");
                for (int i3 = 0; i3 < ps.pkg.libraryNames.size(); i3++) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println((String) ps.pkg.libraryNames.get(i3));
                }
            }
            if (ps.pkg.staticSharedLibName != null) {
                pw.print(prefix);
                pw.println("  static library:");
                pw.print(prefix);
                pw.print("    ");
                pw.print("name:");
                pw.print(ps.pkg.staticSharedLibName);
                pw.print(" version:");
                pw.println(ps.pkg.staticSharedLibVersion);
            }
            if (ps.pkg.usesLibraries != null && ps.pkg.usesLibraries.size() > 0) {
                pw.print(prefix);
                pw.println("  usesLibraries:");
                for (int i4 = 0; i4 < ps.pkg.usesLibraries.size(); i4++) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println((String) ps.pkg.usesLibraries.get(i4));
                }
            }
            if (ps.pkg.usesStaticLibraries != null && ps.pkg.usesStaticLibraries.size() > 0) {
                pw.print(prefix);
                pw.println("  usesStaticLibraries:");
                for (int i5 = 0; i5 < ps.pkg.usesStaticLibraries.size(); i5++) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.print((String) ps.pkg.usesStaticLibraries.get(i5));
                    pw.print(" version:");
                    pw.println(ps.pkg.usesStaticLibrariesVersions[i5]);
                }
            }
            if (ps.pkg.usesOptionalLibraries != null && ps.pkg.usesOptionalLibraries.size() > 0) {
                pw.print(prefix);
                pw.println("  usesOptionalLibraries:");
                for (int i6 = 0; i6 < ps.pkg.usesOptionalLibraries.size(); i6++) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println((String) ps.pkg.usesOptionalLibraries.get(i6));
                }
            }
            if (ps.pkg.usesLibraryFiles != null && ps.pkg.usesLibraryFiles.length > 0) {
                pw.print(prefix);
                pw.println("  usesLibraryFiles:");
                for (int i7 = 0; i7 < ps.pkg.usesLibraryFiles.length; i7++) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println(ps.pkg.usesLibraryFiles[i7]);
                }
            }
        }
        pw.print(prefix);
        pw.print("  timeStamp=");
        date.setTime(ps.timeStamp);
        pw.println(sdf.format(date));
        pw.print(prefix);
        pw.print("  firstInstallTime=");
        date.setTime(ps.firstInstallTime);
        pw.println(sdf.format(date));
        pw.print(prefix);
        pw.print("  lastUpdateTime=");
        date.setTime(ps.lastUpdateTime);
        pw.println(sdf.format(date));
        if (ps.installerPackageName != null) {
            pw.print(prefix);
            pw.print("  installerPackageName=");
            pw.println(ps.installerPackageName);
        }
        if (ps.volumeUuid != null) {
            pw.print(prefix);
            pw.print("  volumeUuid=");
            pw.println(ps.volumeUuid);
        }
        pw.print(prefix);
        pw.print("  signatures=");
        pw.println(ps.signatures);
        pw.print(prefix);
        pw.print("  installPermissionsFixed=");
        pw.print(ps.installPermissionsFixed);
        pw.println();
        pw.print(prefix);
        pw.print("  pkgFlags=");
        printFlags(pw, ps.pkgFlags, FLAG_DUMP_SPEC);
        pw.println();
        if (!(ps.pkg == null || ps.pkg.mOverlayTarget == null)) {
            pw.print(prefix);
            pw.print("  overlayTarget=");
            pw.println(ps.pkg.mOverlayTarget);
            pw.print(prefix);
            pw.print("  overlayCategory=");
            pw.println(ps.pkg.mOverlayCategory);
        }
        if (!(ps.pkg == null || ps.pkg.permissions == null || ps.pkg.permissions.size() <= 0)) {
            ArrayList<PackageParser.Permission> perms = ps.pkg.permissions;
            pw.print(prefix);
            pw.println("  declared permissions:");
            for (int i8 = 0; i8 < perms.size(); i8++) {
                PackageParser.Permission perm = perms.get(i8);
                if (permissionNames == null || permissionNames.contains(perm.info.name)) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.print(perm.info.name);
                    pw.print(": prot=");
                    pw.print(PermissionInfo.protectionToString(perm.info.protectionLevel));
                    if ((perm.info.flags & 1) != 0) {
                        pw.print(", COSTS_MONEY");
                    }
                    if ((perm.info.flags & 2) != 0) {
                        pw.print(", HIDDEN");
                    }
                    if ((perm.info.flags & 1073741824) != 0) {
                        pw.print(", INSTALLED");
                    }
                    pw.println();
                }
            }
        }
        if ((permissionNames != null || dumpAll) && ps.pkg != null && ps.pkg.requestedPermissions != null && ps.pkg.requestedPermissions.size() > 0) {
            ArrayList<String> perms2 = ps.pkg.requestedPermissions;
            pw.print(prefix);
            pw.println("  requested permissions:");
            for (int i9 = 0; i9 < perms2.size(); i9++) {
                String perm2 = perms2.get(i9);
                if (permissionNames == null || permissionNames.contains(perm2)) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.print(perm2);
                    BasePermission bp = this.mPermissions.getPermission(perm2);
                    if (bp == null || !bp.isHardOrSoftRestricted()) {
                        pw.println();
                    } else {
                        pw.println(": restricted=true");
                    }
                }
            }
        }
        if (ps.sharedUser == null || permissionNames != null || dumpAll) {
            dumpInstallPermissionsLPr(pw, prefix + "  ", permissionNames, ps.getPermissionsState());
        }
        if (dumpAllComponents) {
            dumpComponents(pw, prefix + "  ", ps);
        }
        for (UserInfo user2 : users) {
            pw.print(prefix);
            pw.print("  User ");
            pw.print(user2.id);
            pw.print(": ");
            pw.print("ceDataInode=");
            pw.print(ps.getCeDataInode(user2.id));
            pw.print(" installed=");
            pw.print(ps.getInstalled(user2.id));
            pw.print(" hidden=");
            pw.print(ps.getHidden(user2.id));
            pw.print(" suspended=");
            pw.print(ps.getSuspended(user2.id));
            if (ps.getSuspended(user2.id)) {
                PackageUserState pus = ps.readUserState(user2.id);
                pw.print(" suspendingPackage=");
                pw.print(pus.suspendingPackage);
                pw.print(" dialogInfo=");
                pw.print(pus.dialogInfo);
            }
            pw.print(" stopped=");
            pw.print(ps.getStopped(user2.id));
            pw.print(" notLaunched=");
            pw.print(ps.getNotLaunched(user2.id));
            pw.print(" enabled=");
            pw.print(ps.getEnabled(user2.id));
            pw.print(" instant=");
            pw.print(ps.getInstantApp(user2.id));
            pw.print(" virtual=");
            pw.println(ps.getVirtulalPreload(user2.id));
            String[] overlayPaths = ps.getOverlayPaths(user2.id);
            if (overlayPaths != null && overlayPaths.length > 0) {
                pw.print(prefix);
                pw.println("  overlay paths:");
                for (String path : overlayPaths) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.println(path);
                }
            }
            String lastDisabledAppCaller2 = ps.getLastDisabledAppCaller(user2.id);
            if (lastDisabledAppCaller2 != null) {
                pw.print(prefix);
                pw.print("    lastDisabledCaller: ");
                pw.println(lastDisabledAppCaller2);
            }
            if (ps.sharedUser == null) {
                PermissionsState permissionsState = ps.getPermissionsState();
                dumpGidsLPr(pw, prefix + "    ", permissionsState.computeGids(user2.id));
                dumpRuntimePermissionsLPr(pw, prefix + "    ", permissionNames, permissionsState.getRuntimePermissionStates(user2.id), dumpAll);
            }
            String harmfulAppWarning = ps.getHarmfulAppWarning(user2.id);
            if (harmfulAppWarning != null) {
                pw.print(prefix);
                pw.print("      harmfulAppWarning: ");
                pw.println(harmfulAppWarning);
            }
            if (permissionNames == null) {
                ArraySet<String> cmp = ps.getDisabledComponents(user2.id);
                if (cmp != null && cmp.size() > 0) {
                    pw.print(prefix);
                    pw.println("    disabledComponents:");
                    Iterator<String> it = cmp.iterator();
                    while (it.hasNext()) {
                        pw.print(prefix);
                        pw.print("      ");
                        pw.println(it.next());
                    }
                }
                ArraySet<String> cmp2 = ps.getEnabledComponents(user2.id);
                if (cmp2 != null && cmp2.size() > 0) {
                    pw.print(prefix);
                    pw.println("    enabledComponents:");
                    Iterator<String> it2 = cmp2.iterator();
                    while (it2.hasNext()) {
                        pw.print(prefix);
                        pw.print("      ");
                        pw.println(it2.next());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpPackagesLPr(PrintWriter pw, String packageName, ArraySet<String> permissionNames, DumpState dumpState, boolean checkin) {
        boolean printedSomething;
        PrintWriter printWriter = pw;
        String str = packageName;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        boolean printedSomething2 = false;
        boolean dumpAllComponents = dumpState.isOptionEnabled(2);
        List<UserInfo> users = getAllUsers(UserManagerService.getInstance());
        Iterator<PackageSetting> it = this.mPackages.values().iterator();
        while (true) {
            String str2 = null;
            if (!it.hasNext()) {
                break;
            }
            PackageSetting ps = it.next();
            if ((str == null || str.equals(ps.realName) || str.equals(ps.name)) && (permissionNames == null || ps.getPermissionsState().hasRequestedPermission(permissionNames))) {
                if (!checkin && str != null) {
                    dumpState.setSharedUser(ps.sharedUser);
                }
                if (checkin || printedSomething2) {
                    printedSomething = printedSomething2;
                } else {
                    if (dumpState.onTitlePrinted()) {
                        pw.println();
                    }
                    printWriter.println("Packages:");
                    printedSomething = true;
                }
                if (checkin) {
                    str2 = "pkg";
                }
                dumpPackageLPr(pw, "  ", str2, permissionNames, ps, sdf, date, users, str != null, dumpAllComponents);
                printedSomething2 = printedSomething;
            }
        }
        boolean printedSomething3 = false;
        if (this.mRenamedPackages.size() > 0 && permissionNames == null) {
            for (Map.Entry<String, String> e : this.mRenamedPackages.entrySet()) {
                if (str == null || str.equals(e.getKey()) || str.equals(e.getValue())) {
                    if (!checkin) {
                        if (!printedSomething3) {
                            if (dumpState.onTitlePrinted()) {
                                pw.println();
                            }
                            printWriter.println("Renamed packages:");
                            printedSomething3 = true;
                        }
                        printWriter.print("  ");
                    } else {
                        printWriter.print("ren,");
                    }
                    printWriter.print(e.getKey());
                    printWriter.print(checkin ? " -> " : ",");
                    printWriter.println(e.getValue());
                }
            }
        }
        boolean printedSomething4 = false;
        if (this.mDisabledSysPackages.size() > 0 && permissionNames == null) {
            for (PackageSetting ps2 : this.mDisabledSysPackages.values()) {
                if (str == null || str.equals(ps2.realName) || str.equals(ps2.name)) {
                    if (!checkin && !printedSomething4) {
                        if (dumpState.onTitlePrinted()) {
                            pw.println();
                        }
                        printWriter.println("Hidden system packages:");
                        printedSomething4 = true;
                    }
                    dumpPackageLPr(pw, "  ", checkin ? "dis" : null, permissionNames, ps2, sdf, date, users, str != null, dumpAllComponents);
                    printWriter = pw;
                    str = packageName;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpPackagesProto(ProtoOutputStream proto) {
        List<UserInfo> users = getAllUsers(UserManagerService.getInstance());
        int count = this.mPackages.size();
        for (int i = 0; i < count; i++) {
            this.mPackages.valueAt(i).writeToProto(proto, 2246267895813L, users);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpPermissionsLPr(PrintWriter pw, String packageName, ArraySet<String> permissionNames, DumpState dumpState) {
        this.mPermissions.dumpPermissions(pw, packageName, permissionNames, this.mReadExternalStorageEnforced == Boolean.TRUE, dumpState);
    }

    /* access modifiers changed from: package-private */
    public void dumpSharedUsersLPr(PrintWriter pw, String packageName, ArraySet<String> permissionNames, DumpState dumpState, boolean checkin) {
        boolean printedSomething;
        PermissionsState permissionsState;
        int[] iArr;
        int i;
        int i2;
        boolean printedSomething2 = false;
        for (SharedUserSetting su : this.mSharedUsers.values()) {
            if ((packageName == null || su == dumpState.getSharedUser()) && (permissionNames == null || su.getPermissionsState().hasRequestedPermission(permissionNames))) {
                if (!checkin) {
                    if (!printedSomething2) {
                        if (dumpState.onTitlePrinted()) {
                            pw.println();
                        }
                        pw.println("Shared users:");
                        printedSomething = true;
                    } else {
                        printedSomething = printedSomething2;
                    }
                    pw.print("  SharedUser [");
                    pw.print(su.name);
                    pw.print("] (");
                    pw.print(Integer.toHexString(System.identityHashCode(su)));
                    pw.println("):");
                    pw.print("    ");
                    pw.print("userId=");
                    pw.println(su.userId);
                    pw.print("    ");
                    pw.println("Packages");
                    int numPackages = su.packages.size();
                    for (int i3 = 0; i3 < numPackages; i3++) {
                        PackageSetting ps = su.packages.valueAt(i3);
                        if (ps != null) {
                            pw.print("      ");
                            pw.println(ps.toString());
                        } else {
                            pw.print("      ");
                            pw.println("NULL?!");
                        }
                    }
                    if (dumpState.isOptionEnabled(4)) {
                        printedSomething2 = printedSomething;
                    } else {
                        PermissionsState permissionsState2 = su.getPermissionsState();
                        dumpInstallPermissionsLPr(pw, "    ", permissionNames, permissionsState2);
                        int[] userIds = UserManagerService.getInstance().getUserIds();
                        int length = userIds.length;
                        int i4 = 0;
                        while (i4 < length) {
                            int userId = userIds[i4];
                            int[] gids = permissionsState2.computeGids(userId);
                            List<PermissionsState.PermissionState> permissions = permissionsState2.getRuntimePermissionStates(userId);
                            if (!ArrayUtils.isEmpty(gids) || !permissions.isEmpty()) {
                                pw.print("    ");
                                i2 = i4;
                                pw.print("User ");
                                pw.print(userId);
                                pw.println(": ");
                                dumpGidsLPr(pw, "      ", gids);
                                i = length;
                                iArr = userIds;
                                permissionsState = permissionsState2;
                                dumpRuntimePermissionsLPr(pw, "      ", permissionNames, permissions, packageName != null);
                            } else {
                                i2 = i4;
                                i = length;
                                iArr = userIds;
                                permissionsState = permissionsState2;
                            }
                            i4 = i2 + 1;
                            length = i;
                            userIds = iArr;
                            permissionsState2 = permissionsState;
                        }
                        printedSomething2 = printedSomething;
                    }
                } else {
                    pw.print("suid,");
                    pw.print(su.userId);
                    pw.print(",");
                    pw.println(su.name);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpSharedUsersProto(ProtoOutputStream proto) {
        int count = this.mSharedUsers.size();
        for (int i = 0; i < count; i++) {
            this.mSharedUsers.valueAt(i).writeToProto(proto, 2246267895814L);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpReadMessagesLPr(PrintWriter pw, DumpState dumpState) {
        pw.println("Settings parse messages:");
        pw.print(this.mReadMessages.toString());
    }

    private static void dumpSplitNames(PrintWriter pw, PackageParser.Package pkg) {
        if (pkg == null) {
            pw.print(UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
            return;
        }
        pw.print("[");
        pw.print("base");
        if (pkg.baseRevisionCode != 0) {
            pw.print(":");
            pw.print(pkg.baseRevisionCode);
        }
        if (pkg.splitNames != null) {
            for (int i = 0; i < pkg.splitNames.length; i++) {
                pw.print(", ");
                pw.print(pkg.splitNames[i]);
                if ((pkg.splitPrivateFlags[i] & Integer.MIN_VALUE) != 0) {
                    pw.print(":");
                    pw.print(pkg.splitVersionCodes[i]);
                }
                if (pkg.splitRevisionCodes[i] != 0) {
                    pw.print(":");
                    pw.print(pkg.splitRevisionCodes[i]);
                }
            }
        }
        pw.print("]");
    }

    /* access modifiers changed from: package-private */
    public void dumpGidsLPr(PrintWriter pw, String prefix, int[] gids) {
        if (!ArrayUtils.isEmpty(gids)) {
            pw.print(prefix);
            pw.print("gids=");
            pw.println(PackageManagerService.arrayToString(gids));
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpRuntimePermissionsLPr(PrintWriter pw, String prefix, ArraySet<String> permissionNames, List<PermissionsState.PermissionState> permissionStates, boolean dumpAll) {
        if (!permissionStates.isEmpty() || dumpAll) {
            pw.print(prefix);
            pw.println("runtime permissions:");
            for (PermissionsState.PermissionState permissionState : permissionStates) {
                if (permissionNames == null || permissionNames.contains(permissionState.getName())) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.print(permissionState.getName());
                    pw.print(": granted=");
                    pw.print(permissionState.isGranted());
                    pw.println(permissionFlagsToString(", flags=", permissionState.getFlags()));
                }
            }
        }
    }

    private static String permissionFlagsToString(String prefix, int flags) {
        StringBuilder flagsString = null;
        while (flags != 0) {
            if (flagsString == null) {
                flagsString = new StringBuilder();
                flagsString.append(prefix);
                flagsString.append("[ ");
            }
            int flag = 1 << Integer.numberOfTrailingZeros(flags);
            flags &= ~flag;
            flagsString.append(PackageManager.permissionFlagToString(flag));
            if (flags != 0) {
                flagsString.append('|');
            }
        }
        if (flagsString == null) {
            return "";
        }
        flagsString.append(']');
        return flagsString.toString();
    }

    /* access modifiers changed from: package-private */
    public void dumpInstallPermissionsLPr(PrintWriter pw, String prefix, ArraySet<String> permissionNames, PermissionsState permissionsState) {
        List<PermissionsState.PermissionState> permissionStates = permissionsState.getInstallPermissionStates();
        if (!permissionStates.isEmpty()) {
            pw.print(prefix);
            pw.println("install permissions:");
            for (PermissionsState.PermissionState permissionState : permissionStates) {
                if (permissionNames == null || permissionNames.contains(permissionState.getName())) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.print(permissionState.getName());
                    pw.print(": granted=");
                    pw.print(permissionState.isGranted());
                    pw.println(permissionFlagsToString(", flags=", permissionState.getFlags()));
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpComponents(PrintWriter pw, String prefix, PackageSetting ps) {
        dumpComponents(pw, prefix, ps, "activities:", ps.pkg.activities);
        dumpComponents(pw, prefix, ps, "services:", ps.pkg.services);
        dumpComponents(pw, prefix, ps, "receivers:", ps.pkg.receivers);
        dumpComponents(pw, prefix, ps, "providers:", ps.pkg.providers);
        dumpComponents(pw, prefix, ps, "instrumentations:", ps.pkg.instrumentation);
    }

    /* access modifiers changed from: package-private */
    public void dumpComponents(PrintWriter pw, String prefix, PackageSetting ps, String label, List<? extends PackageParser.Component<?>> list) {
        int size = CollectionUtils.size(list);
        if (size != 0) {
            pw.print(prefix);
            pw.println(label);
            for (int i = 0; i < size; i++) {
                pw.print(prefix);
                pw.print("  ");
                pw.println(((PackageParser.Component) list.get(i)).getComponentName().flattenToShortString());
            }
        }
    }

    public void writeRuntimePermissionsForUserLPr(int userId, boolean sync) {
        if (sync) {
            this.mRuntimePermissionsPersistence.writePermissionsForUserSyncLPr(userId);
        } else {
            this.mRuntimePermissionsPersistence.writePermissionsForUserAsyncLPr(userId);
        }
    }

    /* access modifiers changed from: private */
    public final class RuntimePermissionPersistence {
        private static final int INITIAL_VERSION = 0;
        private static final long MAX_WRITE_PERMISSIONS_DELAY_MILLIS = 2000;
        private static final int UPGRADE_VERSION = -1;
        private static final long WRITE_PERMISSIONS_DELAY_MILLIS = 200;
        @GuardedBy({"mLock"})
        private final SparseBooleanArray mDefaultPermissionsGranted = new SparseBooleanArray();
        @GuardedBy({"mLock"})
        private final SparseArray<String> mFingerprints = new SparseArray<>();
        private final Handler mHandler = new MyHandler();
        @GuardedBy({"mLock"})
        private final SparseLongArray mLastNotWrittenMutationTimesMillis = new SparseLongArray();
        private final Object mPersistenceLock;
        @GuardedBy({"mLock"})
        private final SparseIntArray mVersions = new SparseIntArray();
        @GuardedBy({"mLock"})
        private final SparseBooleanArray mWriteScheduled = new SparseBooleanArray();

        public RuntimePermissionPersistence(Object persistenceLock) {
            this.mPersistenceLock = persistenceLock;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"Settings.this.mLock"})
        public int getVersionLPr(int userId) {
            return this.mVersions.get(userId, 0);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"Settings.this.mLock"})
        public void setVersionLPr(int version, int userId) {
            this.mVersions.put(userId, version);
            writePermissionsForUserAsyncLPr(userId);
        }

        @GuardedBy({"Settings.this.mLock"})
        public boolean areDefaultRuntimePermissionsGrantedLPr(int userId) {
            return this.mDefaultPermissionsGranted.get(userId);
        }

        @GuardedBy({"Settings.this.mLock"})
        public void setRuntimePermissionsFingerPrintLPr(String fingerPrint, int userId) {
            this.mFingerprints.put(userId, fingerPrint);
            writePermissionsForUserAsyncLPr(userId);
        }

        public void writePermissionsForUserSyncLPr(int userId) {
            this.mHandler.removeMessages(userId);
            writePermissionsSync(userId);
        }

        @GuardedBy({"Settings.this.mLock"})
        public void writePermissionsForUserAsyncLPr(int userId) {
            long currentTimeMillis = SystemClock.uptimeMillis();
            if (this.mWriteScheduled.get(userId)) {
                this.mHandler.removeMessages(userId);
                long lastNotWrittenMutationTimeMillis = this.mLastNotWrittenMutationTimesMillis.get(userId);
                if (currentTimeMillis - lastNotWrittenMutationTimeMillis >= MAX_WRITE_PERMISSIONS_DELAY_MILLIS) {
                    this.mHandler.obtainMessage(userId).sendToTarget();
                    return;
                }
                long writeDelayMillis = Math.min((long) WRITE_PERMISSIONS_DELAY_MILLIS, Math.max((MAX_WRITE_PERMISSIONS_DELAY_MILLIS + lastNotWrittenMutationTimeMillis) - currentTimeMillis, 0L));
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(userId), writeDelayMillis);
                return;
            }
            this.mLastNotWrittenMutationTimesMillis.put(userId, currentTimeMillis);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(userId), WRITE_PERMISSIONS_DELAY_MILLIS);
            this.mWriteScheduled.put(userId, true);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void writePermissionsSync(int userId) {
            File userRuntimePermissionsFile = Settings.this.getUserRuntimePermissionsFile(userId);
            AtomicFile destination = new AtomicFile(userRuntimePermissionsFile, "package-perms-" + userId);
            ArrayMap<String, List<PermissionsState.PermissionState>> permissionsForPackage = new ArrayMap<>();
            ArrayMap<String, List<PermissionsState.PermissionState>> permissionsForSharedUser = new ArrayMap<>();
            synchronized (this.mPersistenceLock) {
                this.mWriteScheduled.delete(userId);
                int packageCount = Settings.this.mPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    String packageName = Settings.this.mPackages.keyAt(i);
                    PackageSetting packageSetting = Settings.this.mPackages.valueAt(i);
                    if (packageSetting.sharedUser == null) {
                        List<PermissionsState.PermissionState> permissionsStates = packageSetting.getPermissionsState().getRuntimePermissionStates(userId);
                        if (!permissionsStates.isEmpty()) {
                            permissionsForPackage.put(packageName, permissionsStates);
                        }
                    }
                }
                int sharedUserCount = Settings.this.mSharedUsers.size();
                for (int i2 = 0; i2 < sharedUserCount; i2++) {
                    String sharedUserName = Settings.this.mSharedUsers.keyAt(i2);
                    List<PermissionsState.PermissionState> permissionsStates2 = Settings.this.mSharedUsers.valueAt(i2).getPermissionsState().getRuntimePermissionStates(userId);
                    if (!permissionsStates2.isEmpty()) {
                        permissionsForSharedUser.put(sharedUserName, permissionsStates2);
                    }
                }
            }
            FileOutputStream out = null;
            try {
                out = destination.startWrite();
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(out, StandardCharsets.UTF_8.name());
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startDocument(null, true);
                serializer.startTag(null, Settings.TAG_RUNTIME_PERMISSIONS);
                serializer.attribute(null, "version", Integer.toString(this.mVersions.get(userId, 0)));
                String fingerprint = this.mFingerprints.get(userId);
                if (fingerprint != null) {
                    serializer.attribute(null, Settings.ATTR_FINGERPRINT, fingerprint);
                }
                int packageCount2 = permissionsForPackage.size();
                for (int i3 = 0; i3 < packageCount2; i3++) {
                    serializer.startTag(null, "pkg");
                    serializer.attribute(null, Settings.ATTR_NAME, permissionsForPackage.keyAt(i3));
                    writePermissions(serializer, permissionsForPackage.valueAt(i3));
                    serializer.endTag(null, "pkg");
                }
                int sharedUserCount2 = permissionsForSharedUser.size();
                for (int i4 = 0; i4 < sharedUserCount2; i4++) {
                    serializer.startTag(null, Settings.TAG_SHARED_USER);
                    serializer.attribute(null, Settings.ATTR_NAME, permissionsForSharedUser.keyAt(i4));
                    writePermissions(serializer, permissionsForSharedUser.valueAt(i4));
                    serializer.endTag(null, Settings.TAG_SHARED_USER);
                }
                serializer.endTag(null, Settings.TAG_RUNTIME_PERMISSIONS);
                serializer.endDocument();
                destination.finishWrite(out);
                if (Build.FINGERPRINT.equals(fingerprint)) {
                    this.mDefaultPermissionsGranted.put(userId, true);
                }
            } catch (Throwable th) {
                IoUtils.closeQuietly((AutoCloseable) null);
                throw th;
            }
            IoUtils.closeQuietly(out);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        @GuardedBy({"Settings.this.mLock"})
        private void onUserRemovedLPw(int userId) {
            this.mHandler.removeMessages(userId);
            for (SettingBase sb : Settings.this.mPackages.values()) {
                revokeRuntimePermissionsAndClearFlags(sb, userId);
            }
            for (SettingBase sb2 : Settings.this.mSharedUsers.values()) {
                revokeRuntimePermissionsAndClearFlags(sb2, userId);
            }
            this.mDefaultPermissionsGranted.delete(userId);
            this.mVersions.delete(userId);
            this.mFingerprints.remove(userId);
        }

        private void revokeRuntimePermissionsAndClearFlags(SettingBase sb, int userId) {
            PermissionsState permissionsState = sb.getPermissionsState();
            for (PermissionsState.PermissionState permissionState : permissionsState.getRuntimePermissionStates(userId)) {
                BasePermission bp = Settings.this.mPermissions.getPermission(permissionState.getName());
                if (bp != null) {
                    permissionsState.revokeRuntimePermission(bp, userId);
                    permissionsState.updatePermissionFlags(bp, userId, 130047, 0);
                }
            }
        }

        public void deleteUserRuntimePermissionsFile(int userId) {
            Settings.this.getUserRuntimePermissionsFile(userId).delete();
        }

        @GuardedBy({"Settings.this.mLock"})
        public void readStateForUserSyncLPr(int userId) {
            File permissionsFile = Settings.this.getUserRuntimePermissionsFile(userId);
            if (permissionsFile.exists()) {
                try {
                    FileInputStream in = new AtomicFile(permissionsFile).openRead();
                    String exceptionName = "";
                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(in, null);
                        parseRuntimePermissionsLPr(parser, userId);
                        IoUtils.closeQuietly(in);
                        if (1 != 0) {
                            return;
                        }
                    } catch (IOException | XmlPullParserException e) {
                        e.getClass().toString();
                        throw new IllegalStateException("Failed parsing permissions file: " + permissionsFile, e);
                    } catch (Exception e2) {
                        exceptionName = e2.getClass().toString();
                        IoUtils.closeQuietly(in);
                        if (0 != 0) {
                            return;
                        }
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(in);
                        if (0 == 0) {
                            Settings.this.mIsPackageSettingsError = true;
                            deleteUserRuntimePermissionsFile(userId);
                            Settings.this.reportReadFileError(Settings.RUNTIME_PERMISSIONS_FILE_NAME, exceptionName, userId);
                        }
                        throw th;
                    }
                    Settings.this.mIsPackageSettingsError = true;
                    deleteUserRuntimePermissionsFile(userId);
                    Settings.this.reportReadFileError(Settings.RUNTIME_PERMISSIONS_FILE_NAME, exceptionName, userId);
                } catch (FileNotFoundException e3) {
                    Slog.i("PackageManager", "No permissions state");
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:28:0x005a  */
        /* JADX WARNING: Removed duplicated region for block: B:39:0x00c7  */
        @GuardedBy({"Settings.this.mLock"})
        private void parseRuntimePermissionsLPr(XmlPullParser parser, int userId) throws IOException, XmlPullParserException {
            boolean z;
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    String name = parser.getName();
                    int hashCode = name.hashCode();
                    if (hashCode != 111052) {
                        if (hashCode != 160289295) {
                            if (hashCode == 485578803 && name.equals(Settings.TAG_SHARED_USER)) {
                                z = true;
                                if (z) {
                                    this.mVersions.put(userId, XmlUtils.readIntAttribute(parser, "version", -1));
                                    String fingerprint = parser.getAttributeValue(null, Settings.ATTR_FINGERPRINT);
                                    this.mFingerprints.put(userId, fingerprint);
                                    this.mDefaultPermissionsGranted.put(userId, Build.FINGERPRINT.equals(fingerprint));
                                } else if (z) {
                                    String name2 = parser.getAttributeValue(null, Settings.ATTR_NAME);
                                    PackageSetting ps = Settings.this.mPackages.get(name2);
                                    if (ps == null) {
                                        Slog.w("PackageManager", "Unknown package:" + name2);
                                        XmlUtils.skipCurrentTag(parser);
                                    } else {
                                        parsePermissionsLPr(parser, ps.getPermissionsState(), userId);
                                    }
                                } else if (z) {
                                    String name3 = parser.getAttributeValue(null, Settings.ATTR_NAME);
                                    SharedUserSetting sus = Settings.this.mSharedUsers.get(name3);
                                    if (sus == null) {
                                        Slog.w("PackageManager", "Unknown shared user:" + name3);
                                        XmlUtils.skipCurrentTag(parser);
                                    } else {
                                        parsePermissionsLPr(parser, sus.getPermissionsState(), userId);
                                    }
                                }
                            }
                        } else if (name.equals(Settings.TAG_RUNTIME_PERMISSIONS)) {
                            z = false;
                            if (z) {
                            }
                        }
                    } else if (name.equals("pkg")) {
                        z = true;
                        if (z) {
                        }
                    }
                    z = true;
                    if (z) {
                    }
                }
            }
        }

        private void parsePermissionsLPr(XmlPullParser parser, PermissionsState permissionsState, int userId) throws IOException, XmlPullParserException {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                boolean granted = true;
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    String name = parser.getName();
                    char c = 65535;
                    int flags = 0;
                    if (name.hashCode() == 3242771 && name.equals(Settings.TAG_ITEM)) {
                        c = 0;
                    }
                    if (c == 0) {
                        String name2 = parser.getAttributeValue(null, Settings.ATTR_NAME);
                        BasePermission bp = Settings.this.mPermissions.getPermission(name2);
                        if (bp == null) {
                            Slog.w("PackageManager", "Unknown permission:" + name2);
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            String grantedStr = parser.getAttributeValue(null, Settings.ATTR_GRANTED);
                            if (grantedStr != null && !Boolean.parseBoolean(grantedStr)) {
                                granted = false;
                            }
                            String flagsStr = parser.getAttributeValue(null, Settings.ATTR_FLAGS);
                            if (flagsStr != null) {
                                flags = Integer.parseInt(flagsStr, 16);
                            }
                            if ((65536 & flags) != 0) {
                                granted = false;
                            }
                            if (granted) {
                                permissionsState.grantRuntimePermission(bp, userId);
                                permissionsState.updatePermissionFlags(bp, userId, 130047, flags);
                            } else {
                                permissionsState.updatePermissionFlags(bp, userId, 130047, flags);
                            }
                        }
                    }
                }
            }
        }

        private void writePermissions(XmlSerializer serializer, List<PermissionsState.PermissionState> permissionStates) throws IOException {
            for (PermissionsState.PermissionState permissionState : permissionStates) {
                serializer.startTag(null, Settings.TAG_ITEM);
                serializer.attribute(null, Settings.ATTR_NAME, permissionState.getName());
                serializer.attribute(null, Settings.ATTR_GRANTED, String.valueOf(permissionState.isGranted()));
                serializer.attribute(null, Settings.ATTR_FLAGS, Integer.toHexString(permissionState.getFlags()));
                serializer.endTag(null, Settings.TAG_ITEM);
            }
        }

        private final class MyHandler extends Handler {
            public MyHandler() {
                super(BackgroundThread.getHandler().getLooper());
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int userId = message.what;
                Runnable callback = (Runnable) message.obj;
                RuntimePermissionPersistence.this.writePermissionsSync(userId);
                if (callback != null) {
                    callback.run();
                }
            }
        }
    }

    public boolean isInDelAppList(String packageName) {
        if (mIsCheckDelAppsFinished.compareAndSet(false, true)) {
            readDelAppsFiles();
        }
        return this.mDelAppLists.contains(packageName);
    }

    private void readDelAppsFiles() {
        File file;
        ArrayList<File> delAppsFileList = new ArrayList<>();
        try {
            delAppsFileList = HwCfgFilePolicy.getCfgFileList("xml/hw_subuser_delapps_config.xml", 0);
            file = new File(getCustomizedFileName(FILE_SUB_USER_DELAPPS_LIST));
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            file = new File(getCustomizedFileName(FILE_SUB_USER_DELAPPS_LIST));
        } catch (Throwable th) {
            delAppsFileList.add(new File(getCustomizedFileName(FILE_SUB_USER_DELAPPS_LIST)));
            throw th;
        }
        delAppsFileList.add(file);
        Iterator<File> it = delAppsFileList.iterator();
        while (it.hasNext()) {
            loadDelAppsFromXml(it.next());
        }
    }

    private void loadDelAppsFromXml(File configFile) {
        StringBuilder sb;
        if (configFile.exists()) {
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(configFile);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                int depth = parser.getDepth();
                while (true) {
                    int type = parser.next();
                    if ((type == 3 && parser.getDepth() <= depth) || type == 1) {
                        try {
                            stream2.close();
                            return;
                        } catch (IOException e) {
                            e = e;
                            sb = new StringBuilder();
                        }
                    } else if (type == 2) {
                        if (parser.getName().equals("del_app")) {
                            this.mDelAppLists.add(parser.getAttributeValue(0));
                        }
                    }
                }
            } catch (FileNotFoundException e2) {
                Slog.e(TAG, "file is not exist " + e2);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e3) {
                        e = e3;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (XmlPullParserException e4) {
                Slog.e(TAG, "failed parsing " + configFile + " " + e4);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e5) {
                        e = e5;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IOException e6) {
                Slog.e(TAG, "failed parsing " + configFile + " " + e6);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e7) {
                        e = e7;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e8) {
                        Slog.e(TAG, "failed close stream " + e8);
                    }
                }
                throw th;
            }
        } else {
            return;
        }
        sb.append("failed close stream ");
        sb.append(e);
        Slog.e(TAG, sb.toString());
    }

    private String getCustomizedFileName(String xmlName) {
        String path = "/data/cust/xml/" + xmlName;
        if (new File(path).exists()) {
            return path;
        }
        return DIR_ETC_XML + xmlName;
    }

    /* access modifiers changed from: package-private */
    public void loadReservedUidsMap() {
        if (this.mSettingsFilename.exists()) {
            Slog.d(TAG, this.mSettingsFilename.getPath() + " exsites, no need to restore the uid!");
        } else if (this.mBackupSettingsFilename.exists()) {
            Slog.d(TAG, this.mBackupSettingsFilename.getPath() + " exsites, no need to restore the uid!");
        } else if (!this.mPackageListFilename.exists()) {
            Slog.e(TAG, this.mPackageListFilename.getPath() + " not exsites, can't restore the uid!");
        } else {
            clearReservedUidMap();
            Slog.i(TAG, "loadReservedUidsMap");
            FileReader reader = null;
            BufferedReader bufferedReader = null;
            try {
                FileReader reader2 = new FileReader(this.mPackageListFilename);
                BufferedReader bufferedReader2 = new BufferedReader(reader2);
                while (true) {
                    String line = bufferedReader2.readLine();
                    if (line != null) {
                        String[] packageInfos = line.split(" ");
                        if (packageInfos.length > 2) {
                            String packageName = packageInfos[0];
                            int uid = Integer.valueOf(packageInfos[1]).intValue();
                            if (uid >= 10000) {
                                if (uid <= 19999) {
                                    this.mReservedPackageUserIds.put(packageName, Integer.valueOf(uid));
                                    Slog.d(TAG, "loadReservedUidsMap found " + uid + " for " + packageName);
                                    int index = uid + -10000;
                                    for (int reservedSize = this.mReservedUserIds.size(); index >= reservedSize; reservedSize++) {
                                        this.mReservedUserIds.add(false);
                                    }
                                    this.mReservedUserIds.set(index, true);
                                    for (int reservedSize2 = this.mAppIds.size(); index >= reservedSize2; reservedSize2++) {
                                        this.mAppIds.add(null);
                                    }
                                }
                            }
                        }
                    } else {
                        try {
                            break;
                        } catch (IOException e) {
                            Slog.e(TAG, "loadReservedUidsMap io error");
                        }
                    }
                }
                bufferedReader2.close();
                try {
                    reader2.close();
                } catch (FileNotFoundException e2) {
                    Slog.e(TAG, "loadReservedUidsMap close file not found");
                } catch (IOException e3) {
                    Slog.e(TAG, "loadReservedUidsMap close file io error");
                }
            } catch (FileNotFoundException e4) {
                Slog.e(TAG, "loadReservedUidsMap file not found");
                if (0 != 0) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e5) {
                        Slog.e(TAG, "loadReservedUidsMap io error");
                    }
                }
                if (0 != 0) {
                    reader.close();
                }
            } catch (IOException e6) {
                Slog.e(TAG, "loadReservedUidsMap io error");
                if (0 != 0) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e7) {
                        Slog.e(TAG, "loadReservedUidsMap io error");
                    }
                }
                if (0 != 0) {
                    reader.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e8) {
                        Slog.e(TAG, "loadReservedUidsMap io error");
                    }
                }
                if (0 != 0) {
                    try {
                        reader.close();
                    } catch (FileNotFoundException e9) {
                        Slog.e(TAG, "loadReservedUidsMap close file not found");
                    } catch (IOException e10) {
                        Slog.e(TAG, "loadReservedUidsMap close file io error");
                    }
                }
                throw th;
            }
            Slog.i(TAG, "loadReservedUidsMap complete, found " + this.mReservedPackageUserIds.size());
        }
    }

    private int getReservedUidForPackage(Object obj) {
        if (this.mReservedPackageUserIds.size() == 0 || obj == null) {
            return -1;
        }
        String pkgName = "";
        if (obj instanceof PackageSetting) {
            PackageSetting ps = (PackageSetting) obj;
            if (ps.pkg == null || ps.pkg.packageName == null) {
                return -1;
            }
            pkgName = ps.pkg.packageName;
        } else if (obj instanceof SharedUserSetting) {
            pkgName = ((SharedUserSetting) obj).name;
        } else {
            Slog.w(TAG, "getReservedUidForPackage bad object!");
        }
        return getReservedUidForName(pkgName);
    }

    private int getReservedUidForName(String pkgName) {
        if (TextUtils.isEmpty(pkgName) || this.mReservedPackageUserIds.size() == 0 || !this.mReservedPackageUserIds.containsKey(pkgName)) {
            return -1;
        }
        int reservedUid = this.mReservedPackageUserIds.get(pkgName).intValue();
        Object existedSettings = this.mAppIds.get(reservedUid - 10000);
        if (existedSettings != null) {
            String conflictPackage = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
            if (existedSettings instanceof PackageSetting) {
                PackageSetting tmpPs = (PackageSetting) existedSettings;
                if (!(tmpPs.pkg == null || tmpPs.pkg.packageName == null)) {
                    conflictPackage = tmpPs.pkg.packageName;
                }
                Slog.w(TAG, conflictPackage + " has used the reserved uid:" + reservedUid + " for package:" + pkgName);
                return -1;
            } else if (existedSettings instanceof SharedUserSetting) {
                SharedUserSetting tmpSus = (SharedUserSetting) existedSettings;
                if (tmpSus.name != null) {
                    conflictPackage = tmpSus.name;
                }
                Slog.w(TAG, conflictPackage + " has used the reserved shared uid:" + reservedUid + " for package:" + pkgName);
                return -1;
            }
        }
        Slog.d(TAG, "getReservedUidForName return reseved uid:" + reservedUid + " for package:" + pkgName);
        return reservedUid;
    }

    private boolean isReservedUid(int index) {
        if (index >= this.mReservedUserIds.size()) {
            return false;
        }
        return this.mReservedUserIds.get(index).booleanValue();
    }

    public void clearReservedUidMap() {
        this.mReservedUserIds.clear();
        this.mReservedPackageUserIds.clear();
    }

    public void addReservedUidForSharedUser(String sharedUser, String packageName) {
        int reservedUid;
        if (!TextUtils.isEmpty(sharedUser) && this.mReservedPackageUserIds.size() != 0 && !this.mReservedPackageUserIds.containsKey(sharedUser) && (reservedUid = getReservedUidForName(packageName)) != -1) {
            this.mReservedPackageUserIds.put(sharedUser, Integer.valueOf(reservedUid));
            Slog.d(TAG, "addReservedUidForSharedUser found uid:" + reservedUid + " for sharedUser:" + sharedUser + " from package:" + packageName);
        }
    }
}
